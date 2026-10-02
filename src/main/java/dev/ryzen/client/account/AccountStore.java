package dev.ryzen.client.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class AccountStore {
   private static final Logger LOGGER = LoggerFactory.getLogger("Ryzen/AccountStore");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private static final Pattern VALID_NICKNAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
   private static final Comparator<AltAccount> UI_ORDER = Comparator.comparing(AltAccount::favorite)
      .reversed()
      .thenComparing(AltAccount::createdAt, Comparator.reverseOrder())
      .thenComparing(AltAccount::nickname, String.CASE_INSENSITIVE_ORDER)
      .thenComparing(AltAccount::id);
   private static final DateTimeFormatter LEGACY_DATE = DateTimeFormatter.ofPattern("dd.MM.yy", Locale.ROOT);
   private static final int SCHEMA_VERSION = 1;
   private static final int DEMO_ACCOUNT_COUNT = 11;
   private final Path file;
   private final List<AltAccount> accounts = new ArrayList<>();
   private UUID selectedId;

   private AccountStore(Path file) {
      this.file = file;
   }

   public static AccountStore load(String currentMinecraftNickname) {
      Path file = FabricLoader.getInstance().getConfigDir().resolve("ryzen").resolve("accounts.json");
      return load(file, currentMinecraftNickname);
   }

   public static AccountStore load(Path file, String currentMinecraftNickname) {
      AccountStore store = new AccountStore(file.toAbsolutePath().normalize());
      store.loadFromDisk(currentMinecraftNickname);
      return store;
   }

   public static boolean isValidNickname(String nickname) {
      return nickname != null && VALID_NICKNAME.matcher(nickname.trim()).matches();
   }

   public synchronized Path file() {
      return this.file;
   }

   public synchronized List<AltAccount> accountsForUi() {
      return this.accounts.stream().sorted(UI_ORDER).toList();
   }

   public synchronized Optional<AltAccount> selected() {
      return this.findInternal(this.selectedId);
   }

   public synchronized Optional<AltAccount> find(UUID id) {
      return this.findInternal(id);
   }

   public synchronized UUID selectedId() {
      return this.selectedId;
   }

   public synchronized AltAccount add(String nickname) {
      String normalized = normalizeAndValidateNickname(nickname);
      AltAccount account = new AltAccount(UUID.randomUUID(), normalized, Instant.now(), false);
      this.accounts.add(account);
      this.saveQuietly();
      return account;
   }

   public synchronized boolean select(UUID id) {
      if (id == null || this.findInternal(id).isEmpty()) {
         return false;
      }

      if (id.equals(this.selectedId)) {
         return true;
      }

      this.selectedId = id;
      this.saveQuietly();
      return true;
   }

   public synchronized boolean delete(UUID id) {
      if (id == null) {
         return false;
      }

      boolean removed = this.accounts.removeIf(account -> account.id().equals(id));
      if (!removed) {
         return false;
      }

      if (id.equals(this.selectedId)) {
         this.selectedId = this.accounts.isEmpty() ? null : this.accounts.stream().sorted(UI_ORDER).findFirst().orElseThrow().id();
      }

      this.saveQuietly();
      return true;
   }

   public synchronized boolean toggleFavorite(UUID id) {
      if (id == null) {
         return false;
      }

      for (int index = 0; index < this.accounts.size(); index++) {
         AltAccount account = this.accounts.get(index);
         if (account.id().equals(id)) {
            this.accounts.set(index, account.withFavorite(!account.favorite()));
            this.saveQuietly();
            return true;
         }
      }

      return false;
   }

   public synchronized void save() throws IOException {
      Path parent = this.file.getParent();
      if (parent != null) {
         Files.createDirectories(parent);
      }

      JsonObject root = new JsonObject();
      root.addProperty("schemaVersion", 1);
      root.addProperty("selectedId", this.selectedId.toString());
      JsonArray serializedAccounts = new JsonArray();

      for (AltAccount account : this.accounts) {
         JsonObject serialized = new JsonObject();
         serialized.addProperty("id", account.id().toString());
         serialized.addProperty("nickname", account.nickname());
         serialized.addProperty("createdAt", account.createdAt().toString());
         serialized.addProperty("favorite", account.favorite());
         serializedAccounts.add(serialized);
      }

      root.add("accounts", serializedAccounts);
      Path temporary = this.file.resolveSibling(this.file.getFileName() + ".tmp");

      try (Writer writer = Files.newBufferedWriter(
            temporary, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
         )) {
         GSON.toJson(root, writer);
      }

      try {
         Files.move(temporary, this.file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException exception) {
         Files.move(temporary, this.file, StandardCopyOption.REPLACE_EXISTING);
      }
   }

   private void loadFromDisk(String currentMinecraftNickname) {
      boolean shouldSeed = !Files.isRegularFile(this.file);
      if (!shouldSeed) {
         try {
            AccountStore.LoadPayload payload = this.readPayload();
            this.accounts.addAll(payload.accounts());
            this.selectedId = this.resolveSelected(payload.selectedToken());
            shouldSeed = this.accounts.isEmpty();
         } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Could not read local display accounts from {}. Demo data will be used.", this.file, exception);
            this.backupBrokenFile();
            shouldSeed = true;
         }
      }

      if (shouldSeed) {
         this.accounts.clear();
         this.seedDemoAccounts(currentMinecraftNickname);
      }

      if (this.findInternal(this.selectedId).isEmpty()) {
         this.selectedId = this.accounts.stream().sorted(UI_ORDER).findFirst().map(AltAccount::id).orElse(null);
      }

      this.saveQuietly();
   }

   private AccountStore.LoadPayload readPayload() throws IOException {
      JsonElement root;
      try (Reader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8)) {
         root = JsonParser.parseReader(reader);
      }

      if (root != null && !root.isJsonNull()) {
         String selectedToken = null;
         JsonArray serializedAccounts;
         if (root.isJsonArray()) {
            serializedAccounts = root.getAsJsonArray();
         } else {
            if (!root.isJsonObject()) {
               throw new JsonParseException("Unsupported account file root");
            }

            JsonObject object = root.getAsJsonObject();
            JsonElement accountsElement = first(object, "accounts", "profiles", "alts");
            if (accountsElement == null || !accountsElement.isJsonArray()) {
               throw new JsonParseException("Missing accounts array");
            }

            serializedAccounts = accountsElement.getAsJsonArray();
            selectedToken = stringValue(first(object, "selectedId", "selected", "activeId", "active"));
         }

         List<AltAccount> loadedAccounts = new ArrayList<>();
         Set<UUID> ids = new HashSet<>();
         Instant fallbackTime = Instant.now();

         for (int index = 0; index < serializedAccounts.size(); index++) {
            JsonElement element = serializedAccounts.get(index);

            try {
               AltAccount account = parseAccount(element, fallbackTime.minusSeconds(index));
               if (ids.add(account.id())) {
                  loadedAccounts.add(account);
               }
            } catch (RuntimeException exception) {
               LOGGER.warn("Skipping invalid display account {} in {}", index, this.file);
            }
         }

         return new AccountStore.LoadPayload(loadedAccounts, selectedToken);
      } else {
         throw new JsonParseException("Empty account file");
      }
   }

   private static AltAccount parseAccount(JsonElement element, Instant fallbackCreatedAt) {
      if (!element.isJsonObject()) {
         throw new JsonParseException("Account must be an object");
      }

      JsonObject object = element.getAsJsonObject();
      String nickname = normalizeStoredNickname(stringValue(first(object, "nickname", "name", "username")));
      UUID id = parseUuid(stringValue(first(object, "id", "uuid"))).orElseGet(UUID::randomUUID);
      Instant createdAt = parseCreatedAt(first(object, "createdAt", "created", "created_at", "createdAtEpochMillis")).orElse(fallbackCreatedAt);
      boolean favorite = booleanValue(first(object, "favorite", "top", "starred"));
      return new AltAccount(id, nickname, createdAt, favorite);
   }

   private UUID resolveSelected(String selectedToken) {
      Optional<UUID> selectedUuid = parseUuid(selectedToken);
      if (selectedUuid.isPresent() && this.findInternal(selectedUuid.get()).isPresent()) {
         return selectedUuid.get();
      }

      if (selectedToken != null) {
         for (AltAccount account : this.accounts) {
            if (account.nickname().equalsIgnoreCase(selectedToken)) {
               return account.id();
            }
         }
      }

      return null;
   }

   private void seedDemoAccounts(String currentMinecraftNickname) {
      String player = isValidNickname(currentMinecraftNickname) ? currentMinecraftNickname.trim() : "Player";
      String[] names = new String[]{
         player, "ShadowByte", "FrostRush", "VoidFox", "PixelCore", "AquaDrift", "NightWolf", "StormHawk", "EmberSpark", "GhostFlux", "NovaDash"
      };
      Instant figmaCreatedAt = LocalDate.of(2026, 6, 26).atStartOfDay().toInstant(ZoneOffset.UTC);

      for (int index = 0; index < 11; index++) {
         AltAccount account = new AltAccount(UUID.randomUUID(), names[index], figmaCreatedAt.minus(index, ChronoUnit.DAYS), index < 2);
         this.accounts.add(account);
      }

      this.selectedId = ((AltAccount)this.accounts.getFirst()).id();
   }

   private Optional<AltAccount> findInternal(UUID id) {
      return id == null ? Optional.empty() : this.accounts.stream().filter(account -> account.id().equals(id)).findFirst();
   }

   private void saveQuietly() {
      try {
         this.save();
      } catch (IOException exception) {
         LOGGER.error("Could not save local display accounts to {}", this.file, exception);
      }
   }

   private void backupBrokenFile() {
      if (Files.isRegularFile(this.file)) {
         Path backup = this.file.resolveSibling("accounts.corrupt-" + System.currentTimeMillis() + ".json");

         try {
            Files.copy(this.file, backup);
         } catch (IOException exception) {
            LOGGER.warn("Could not preserve broken account file {}", this.file, exception);
         }
      }
   }

   private static String normalizeAndValidateNickname(String nickname) {
      String normalized = nickname == null ? "" : nickname.trim();
      if (!VALID_NICKNAME.matcher(normalized).matches()) {
         throw new IllegalArgumentException("Nickname must match [A-Za-z0-9_]{3,16}");
      } else {
         return normalized;
      }
   }

   private static String normalizeStoredNickname(String nickname) {
      String normalized = nickname == null ? "" : nickname.trim();
      return normalizeAndValidateNickname(normalized);
   }

   private static JsonElement first(JsonObject object, String... names) {
      for (String name : names) {
         if (object.has(name) && !object.get(name).isJsonNull()) {
            return object.get(name);
         }
      }

      return null;
   }

   private static String stringValue(JsonElement element) {
      return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
   }

   private static boolean booleanValue(JsonElement element) {
      if (element != null && element.isJsonPrimitive()) {
         try {
            return element.getAsBoolean();
         } catch (RuntimeException exception) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static Optional<UUID> parseUuid(String value) {
      if (value != null && !value.isBlank()) {
         try {
            return Optional.of(UUID.fromString(value));
         } catch (IllegalArgumentException exception) {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   private static Optional<Instant> parseCreatedAt(JsonElement element) {
      if (element != null && element.isJsonPrimitive()) {
         try {
            if (element.getAsJsonPrimitive().isNumber()) {
               long timestamp = element.getAsLong();
               return Optional.of(timestamp < 10000000000L ? Instant.ofEpochSecond(timestamp) : Instant.ofEpochMilli(timestamp));
            }

            String value = element.getAsString();

            try {
               return Optional.of(Instant.parse(value));
            } catch (DateTimeParseException ignored) {
               LocalDate date = LocalDate.parse(value, LEGACY_DATE);
               return Optional.of(date.atStartOfDay().toInstant(ZoneOffset.UTC));
            }
         } catch (RuntimeException exception) {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   @Environment(EnvType.CLIENT)
   private record LoadPayload(List<AltAccount> accounts, String selectedToken) {
   }
}
