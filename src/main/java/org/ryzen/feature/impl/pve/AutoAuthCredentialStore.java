package org.ryzen.feature.impl.pve;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.ConfigIO;

@Environment(EnvType.CLIENT)
public final class AutoAuthCredentialStore {
   private static final int FORMAT_VERSION = 1;
   private static final int MASTER_KEY_BYTES = 32;
   private static final int GCM_IV_BYTES = 12;
   private static final int GENERATED_PASSWORD_LENGTH = 20;
   private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
   private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
   private static final String DIGITS = "23456789";
   private static final String ALPHANUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
   private static final byte[] DERIVATION_DOMAIN = "ryzen:auto-auth:generated:v1\u0000".getBytes(StandardCharsets.UTF_8);
   private static final EnumSet<PosixFilePermission> DIRECTORY_PERMISSIONS = EnumSet.of(
      PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE
   );
   private static final EnumSet<PosixFilePermission> FILE_PERMISSIONS = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
   private final Path directory;
   private final Path keyPath;
   private final Path credentialsPath;
   private final SecureRandom random = new SecureRandom();
   private final Map<String, AutoAuthCredentialStore.EncryptedCredential> credentials = new LinkedHashMap<>();
   private byte[] masterKey;
   private boolean credentialsLoaded;

   public AutoAuthCredentialStore() {
      this(ConfigIO.resolve("secrets/auto-auth"));
   }

   public AutoAuthCredentialStore(Path directory) {
      this.directory = directory.toAbsolutePath().normalize();
      this.keyPath = this.directory.resolve("master.key");
      this.credentialsPath = this.directory.resolve("credentials.json");
   }

   public synchronized String generatedPassword(AutoAuthCredentialStore.Scope scope) throws IOException, GeneralSecurityException {
      byte[] key = this.masterKey();
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));
      mac.update(DERIVATION_DOMAIN);
      byte[] digest = mac.doFinal(scope.canonical().getBytes(StandardCharsets.UTF_8));
      StringBuilder password = new StringBuilder(20);
      password.append(select("ABCDEFGHJKLMNPQRSTUVWXYZ", digest[0]));
      password.append(select("abcdefghijkmnopqrstuvwxyz", digest[1]));
      password.append(select("23456789", digest[2]));

      for (int index = 3; password.length() < 20; index++) {
         password.append(select("ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789", digest[index % digest.length]));
      }

      return password.toString();
   }

   public synchronized Optional<String> loadCustomPassword(AutoAuthCredentialStore.Scope scope) throws IOException, GeneralSecurityException {
      this.loadCredentials();
      String scopeHash = scopeHash(scope);
      AutoAuthCredentialStore.EncryptedCredential encrypted = this.credentials.get(scopeHash);
      if (encrypted == null) {
         return Optional.empty();
      }

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(2, new SecretKeySpec(this.masterKey(), "AES"), new GCMParameterSpec(128, encrypted.iv()));
      cipher.updateAAD(scopeHash.getBytes(StandardCharsets.US_ASCII));
      byte[] plaintext = cipher.doFinal(encrypted.ciphertext());
      return Optional.of(new String(plaintext, StandardCharsets.UTF_8));
   }

   public synchronized void saveCustomPassword(AutoAuthCredentialStore.Scope scope, String password) throws IOException, GeneralSecurityException {
      if (password != null && !password.isEmpty()) {
         this.loadCredentials();
         String scopeHash = scopeHash(scope);
         byte[] iv = new byte[12];
         this.random.nextBytes(iv);
         Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
         cipher.init(1, new SecretKeySpec(this.masterKey(), "AES"), new GCMParameterSpec(128, iv));
         cipher.updateAAD(scopeHash.getBytes(StandardCharsets.US_ASCII));
         byte[] ciphertext = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
         this.credentials.put(scopeHash, new AutoAuthCredentialStore.EncryptedCredential(iv, ciphertext));
         this.saveCredentials();
      } else {
         throw new IllegalArgumentException("Password cannot be empty");
      }
   }

   public Path keyPath() {
      return this.keyPath;
   }

   public Path credentialsPath() {
      return this.credentialsPath;
   }

   private byte[] masterKey() throws IOException {
      if (this.masterKey != null) {
         return this.masterKey;
      }

      this.secureDirectory();
      if (Files.exists(this.keyPath, LinkOption.NOFOLLOW_LINKS)) {
         rejectSymbolicLink(this.keyPath);
         applyFilePermissions(this.keyPath);
         byte[] existing = Files.readAllBytes(this.keyPath);
         if (existing.length != 32) {
            throw new IOException("AutoAuth master key has an invalid length");
         }

         this.masterKey = existing;
         return existing;
      } else {
         if (Files.exists(this.credentialsPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("AutoAuth master key is missing");
         }

         byte[] generated = new byte[32];
         this.random.nextBytes(generated);
         this.writeSecurely(this.keyPath, generated);
         this.masterKey = generated;
         return generated;
      }
   }

   private void loadCredentials() throws IOException {
      if (!this.credentialsLoaded) {
         this.credentialsLoaded = true;
         if (Files.exists(this.credentialsPath, LinkOption.NOFOLLOW_LINKS)) {
            rejectSymbolicLink(this.credentialsPath);
            applyFilePermissions(this.credentialsPath);
            this.credentials.clear();

            try {
               JsonObject root = JsonParser.parseString(Files.readString(this.credentialsPath, StandardCharsets.UTF_8)).getAsJsonObject();
               if (root.has("version") && root.get("version").getAsInt() == 1) {
                  JsonObject entries = root.getAsJsonObject("credentials");
                  if (entries != null) {
                     for (Entry<String, JsonElement> entry : entries.entrySet()) {
                        JsonObject value = entry.getValue().getAsJsonObject();
                        byte[] iv = Base64.getDecoder().decode(value.get("iv").getAsString());
                        byte[] ciphertext = Base64.getDecoder().decode(value.get("ciphertext").getAsString());
                        if (iv.length != 12 || ciphertext.length == 0) {
                           throw new IOException("Malformed AutoAuth credential entry");
                        }

                        this.credentials.put(entry.getKey(), new AutoAuthCredentialStore.EncryptedCredential(iv, ciphertext));
                     }
                  }
               } else {
                  throw new IOException("Unsupported AutoAuth credential format");
               }
            } catch (IOException exception) {
               this.credentials.clear();
               this.credentialsLoaded = false;
               throw exception;
            } catch (Exception exception) {
               this.credentials.clear();
               this.credentialsLoaded = false;
               throw new IOException("Failed to read AutoAuth credentials", exception);
            }
         }
      }
   }

   private void saveCredentials() throws IOException {
      JsonObject entries = new JsonObject();

      for (Entry<String, AutoAuthCredentialStore.EncryptedCredential> entry : this.credentials.entrySet()) {
         JsonObject value = new JsonObject();
         value.addProperty("iv", Base64.getEncoder().encodeToString(entry.getValue().iv()));
         value.addProperty("ciphertext", Base64.getEncoder().encodeToString(entry.getValue().ciphertext()));
         entries.add(entry.getKey(), value);
      }

      JsonObject root = new JsonObject();
      root.addProperty("version", 1);
      root.add("credentials", entries);
      this.writeSecurely(this.credentialsPath, root.toString().getBytes(StandardCharsets.UTF_8));
   }

   private void secureDirectory() throws IOException {
      Files.createDirectories(this.directory);
      rejectSymbolicLink(this.directory);
      PosixFileAttributeView view = Files.getFileAttributeView(this.directory, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
      if (view != null) {
         Files.setPosixFilePermissions(this.directory, DIRECTORY_PERMISSIONS);
      }
   }

   private void writeSecurely(Path path, byte[] value) throws IOException {
      this.secureDirectory();
      if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
         rejectSymbolicLink(path);
      }

      Path temp = path.resolveSibling(path.getFileName() + ".tmp");
      Files.deleteIfExists(temp);
      if (Files.getFileAttributeView(this.directory, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS) != null) {
         Files.createFile(temp, PosixFilePermissions.asFileAttribute(FILE_PERMISSIONS));
         Files.write(temp, value, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
      } else {
         Files.write(temp, value, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
      }

      applyFilePermissions(temp);

      try {
         Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException unsupported) {
         Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
      }

      applyFilePermissions(path);
   }

   private static void applyFilePermissions(Path path) throws IOException {
      PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
      if (view != null) {
         Files.setPosixFilePermissions(path, FILE_PERMISSIONS);
      }
   }

   private static void rejectSymbolicLink(Path path) throws IOException {
      if (Files.isSymbolicLink(path)) {
         throw new IOException("AutoAuth credential files cannot be symbolic links");
      }
   }

   private static String scopeHash(AutoAuthCredentialStore.Scope scope) throws GeneralSecurityException {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(scope.canonical().getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
   }

   private static char select(String alphabet, byte value) {
      return alphabet.charAt(Byte.toUnsignedInt(value) % alphabet.length());
   }

   @Environment(EnvType.CLIENT)
   private record EncryptedCredential(byte[] iv, byte[] ciphertext) {
   }

   @Environment(EnvType.CLIENT)
   public record Scope(String server, String account) {
      public Scope {
         server = normalizeIdentity(server, "server");
         account = normalizeIdentity(account, "account");
      }

      String canonical() {
         return this.server + "\u0000" + this.account;
      }

      private static String normalizeIdentity(String value, String label) {
         if (value == null) {
            throw new IllegalArgumentException(label + " cannot be null");
         } else {
            String normalized = Normalizer.normalize(value, Form.NFKC).trim().toLowerCase(Locale.ROOT);
            if (normalized.isEmpty()) {
               throw new IllegalArgumentException(label + " cannot be blank");
            } else {
               return normalized;
            }
         }
      }
   }
}
