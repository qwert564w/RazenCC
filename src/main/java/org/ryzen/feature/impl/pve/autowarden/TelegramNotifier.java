package org.ryzen.feature.impl.pve.autowarden;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class TelegramNotifier implements AutoCloseable {
   private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5L);
   private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(8L);
   private static final String TOKEN_ENV = "BLADE_TELEGRAM_BOT_TOKEN";
   private static final String CHAT_ENV = "BLADE_TELEGRAM_CHAT_ID";
   private final Path secretFile;
   private final ExecutorService executor;
   private final HttpClient httpClient;
   private final AtomicReference<TelegramNotifier.LinkStatus> status = new AtomicReference<>(TelegramNotifier.LinkStatus.UNCONFIGURED);
   private volatile TelegramNotifier.Credentials credentials;
   private volatile boolean closed;

   public TelegramNotifier(Path gameDirectory) {
      this.secretFile = gameDirectory.resolve("blade-secrets").resolve("autowarden-telegram.properties");
      this.executor = Executors.newSingleThreadExecutor(runnable -> {
         Thread thread = new Thread(runnable, "blade-autowarden-telegram");
         thread.setDaemon(true);
         return thread;
      });
      this.httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).executor(this.executor).followRedirects(Redirect.NEVER).build();
   }

   public Path secretFile() {
      return this.secretFile;
   }

   public TelegramNotifier.LinkStatus status() {
      return this.status.get();
   }

   public boolean isLinked() {
      return this.status() == TelegramNotifier.LinkStatus.LINKED;
   }

   public String configurationHint() {
      return "Set BLADE_TELEGRAM_BOT_TOKEN and BLADE_TELEGRAM_CHAT_ID, or fill " + this.secretFile.toAbsolutePath();
   }

   public CompletableFuture<TelegramNotifier.LinkStatus> reloadAndVerify() {
      if (this.closed) {
         return CompletableFuture.completedFuture(TelegramNotifier.LinkStatus.CLOSED);
      }

      this.status.set(TelegramNotifier.LinkStatus.CHECKING);
      return CompletableFuture.supplyAsync(this::loadCredentials, this.executor)
         .thenCompose(
            loaded -> {
               if (loaded == null) {
                  this.status.set(TelegramNotifier.LinkStatus.UNCONFIGURED);
                  return CompletableFuture.completedFuture(TelegramNotifier.LinkStatus.UNCONFIGURED);
               } else {
                  this.replaceCredentials(loaded);
                  HttpRequest request = this.request("getMe", "");
                  return this.httpClient
                     .sendAsync(request, BodyHandlers.discarding())
                     .orTimeout(REQUEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                     .handle(
                        (response, error) -> {
                           TelegramNotifier.LinkStatus next = error == null && response.statusCode() >= 200 && response.statusCode() < 300
                              ? TelegramNotifier.LinkStatus.LINKED
                              : TelegramNotifier.LinkStatus.ERROR;
                           this.status.set(next);
                           return next;
                        }
                     );
               }
            }
         )
         .exceptionally(error -> {
            this.status.set(TelegramNotifier.LinkStatus.ERROR);
            return TelegramNotifier.LinkStatus.ERROR;
         });
   }

   public CompletableFuture<Boolean> send(String message) {
      if (!this.closed && message != null && !message.isBlank()) {
         TelegramNotifier.Credentials current = this.credentials;
         if (current != null && this.isLinked()) {
            String body = "chat_id=" + encode(current.chatId()) + "&disable_web_page_preview=true&text=" + encode(message);
            HttpRequest request = this.request("sendMessage", body);
            return this.httpClient
               .sendAsync(request, BodyHandlers.discarding())
               .orTimeout(REQUEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
               .handle((response, error) -> error == null && response.statusCode() >= 200 && response.statusCode() < 300);
         } else {
            return CompletableFuture.completedFuture(false);
         }
      } else {
         return CompletableFuture.completedFuture(false);
      }
   }

   @Override
   public void close() {
      this.closed = true;
      TelegramNotifier.Credentials current = this.credentials;
      this.credentials = null;
      if (current != null) {
         current.clear();
      }

      this.status.set(TelegramNotifier.LinkStatus.CLOSED);
      this.executor.shutdownNow();
   }

   private TelegramNotifier.Credentials loadCredentials() {
      String token = trimToNull(System.getenv("BLADE_TELEGRAM_BOT_TOKEN"));
      String chat = trimToNull(System.getenv("BLADE_TELEGRAM_CHAT_ID"));
      if (token == null) {
         token = trimToNull(System.getProperty("blade.telegram.botToken"));
      }

      if (chat == null) {
         chat = trimToNull(System.getProperty("blade.telegram.chatId"));
      }

      if (token == null || chat == null) {
         this.ensureSecretTemplate();
         Properties properties = new Properties();
         if (Files.isRegularFile(this.secretFile)) {
            try (InputStream input = Files.newInputStream(this.secretFile)) {
               properties.load(input);
               token = trimToNull(properties.getProperty("botToken"));
               chat = trimToNull(properties.getProperty("chatId"));
            } catch (IOException ignored) {
               return null;
            }
         }
      }

      return token != null && chat != null && validToken(token) && validChat(chat) ? new TelegramNotifier.Credentials(token.toCharArray(), chat) : null;
   }

   private void ensureSecretTemplate() {
      if (!Files.exists(this.secretFile)) {
         try {
            Files.createDirectories(this.secretFile.getParent());
            Files.writeString(this.secretFile, "# Auto Warden Telegram secrets. Keep this file private.\nbotToken=\nchatId=\n", StandardCharsets.UTF_8);
            setOwnerOnlyPermissions(this.secretFile);
         } catch (IOException | UnsupportedOperationException var2) {
         }
      }
   }

   private HttpRequest request(String method, String body) {
      TelegramNotifier.Credentials current = this.credentials;
      if (current == null) {
         throw new IllegalStateException("Telegram credentials are not loaded");
      }

      String token = new String(current.token());
      URI uri = URI.create("https://api.telegram.org/bot" + token + "/" + method);
      Builder builder = HttpRequest.newBuilder(uri).timeout(REQUEST_TIMEOUT).header("Accept", "application/json");
      return body.isEmpty()
         ? builder.GET().build()
         : builder.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8").POST(BodyPublishers.ofString(body)).build();
   }

   private void replaceCredentials(TelegramNotifier.Credentials next) {
      TelegramNotifier.Credentials previous = this.credentials;
      this.credentials = next;
      if (previous != null) {
         previous.clear();
      }
   }

   private static void setOwnerOnlyPermissions(Path file) throws IOException {
      Set<PosixFilePermission> permissions = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
      Files.setPosixFilePermissions(file, permissions);
   }

   private static boolean validToken(String token) {
      return token.length() >= 20 && token.length() <= 256 && token.matches("[0-9]{5,}:[A-Za-z0-9_-]{20,}");
   }

   private static boolean validChat(String chat) {
      return chat.length() <= 32 && chat.matches("-?[0-9]{1,24}");
   }

   private static String trimToNull(String value) {
      if (value == null) {
         return null;
      }

      String trimmed = value.trim();
      return trimmed.isEmpty() ? null : trimmed;
   }

   private static String encode(String value) {
      return URLEncoder.encode(value, StandardCharsets.UTF_8);
   }

   @Environment(EnvType.CLIENT)
   private record Credentials(char[] token, String chatId) {
      private Credentials {
         token = Arrays.copyOf(token, token.length);
      }

      private void clear() {
         Arrays.fill(this.token, '\u0000');
      }
   }

   @Environment(EnvType.CLIENT)
   public enum LinkStatus {
      UNCONFIGURED,
      CHECKING,
      LINKED,
      ERROR,
      CLOSED;
   }
}
