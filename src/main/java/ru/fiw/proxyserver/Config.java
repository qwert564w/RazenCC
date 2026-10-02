package ru.fiw.proxyserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class Config {
   private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Type ACCOUNT_TYPE = (new TypeToken<LinkedHashMap<String, Proxy>>() {}).getType();
   private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
   public static Map<String, Proxy> accounts = new LinkedHashMap<>();
   public static String lastPlayerName = "";
   private static boolean loaded;

   private Config() {
   }

   public static synchronized void loadConfig() {
      if (!loaded) {
         loaded = true;
         Path path = configPath();
         if (!Files.exists(path)) {
            if (!migrateLegacyConfig()) {
               saveConfig();
            }
         } else {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
               JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
               lastPlayerName = stringValue(root, "lastPlayerName", "");
               ProxyServer.proxyEnabled = booleanValue(root, "proxy-enabled", false);
               if (root.has("proxy") && !root.get("proxy").isJsonNull()) {
                  ProxyServer.proxy = normalize((Proxy)GSON.fromJson(root.get("proxy"), Proxy.class));
               }

               if (root.has("accounts") && !root.get("accounts").isJsonNull()) {
                  Map<String, Proxy> loadedAccounts = (Map<String, Proxy>)GSON.fromJson(root.get("accounts"), ACCOUNT_TYPE);
                  if (loadedAccounts != null) {
                     loadedAccounts.forEach((name, proxy) -> accounts.put(name, normalize(proxy)));
                  }
               }
            } catch (Exception exception) {
               LOGGER.error("Proxy config {} is corrupt; preserving it and using defaults", path, exception);
               backupCorrupt(path);
               ProxyServer.proxyEnabled = false;
               ProxyServer.proxy = new Proxy();
               accounts = new LinkedHashMap<>();
               lastPlayerName = "";
               saveConfig();
            }
         }
      }
   }

   public static synchronized void saveConfig() {
      JsonObject root = new JsonObject();
      root.addProperty("lastPlayerName", lastPlayerName);
      root.addProperty("proxy-enabled", ProxyServer.proxyEnabled);
      root.add("proxy", GSON.toJsonTree(normalize(ProxyServer.proxy)));
      root.add("accounts", GSON.toJsonTree(accounts, ACCOUNT_TYPE));
      write(configPath(), root);
   }

   public static synchronized void activateForPlayer(String playerName) {
      loadConfig();
      String normalizedName = playerName == null ? "" : playerName;
      if (!Objects.equals(normalizedName, lastPlayerName)) {
         lastPlayerName = normalizedName;
         Proxy selected = accounts.get(normalizedName);
         if (selected == null) {
            selected = accounts.get("");
         }

         if (selected != null) {
            ProxyServer.proxy = selected.copy();
         }

         saveConfig();
      }
   }

   public static synchronized void putAccount(String name, Proxy proxy) {
      loadConfig();
      accounts.put(name, normalize(proxy));
      saveConfig();
   }

   public static synchronized void removeAccount(String name) {
      loadConfig();
      accounts.remove(name);
      saveConfig();
   }

   private static boolean migrateLegacyConfig() {
      Path legacy = FabricLoader.getInstance().getConfigDir().resolve("ryzen").resolve("proxy.json");
      if (!Files.exists(legacy)) {
         return false;
      }

      try (Reader reader = Files.newBufferedReader(legacy, StandardCharsets.UTF_8)) {
         JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
         String host = stringValue(root, "host", "").trim();
         int port = intValue(root, "port", 1080);
         String type = stringValue(root, "type", "SOCKS5");
         ProxyServer.proxy = new Proxy(
            "SOCKS4".equalsIgnoreCase(type), host.isEmpty() ? "" : host + ":" + port, stringValue(root, "username", ""), stringValue(root, "password", "")
         );
         ProxyServer.proxyEnabled = booleanValue(root, "enabled", false);
         saveConfig();
         LOGGER.info("Migrated the previous Ryzen proxy settings to {}", configPath());
         return true;
      } catch (Exception exception) {
         LOGGER.error("Could not migrate the previous proxy config {}", legacy, exception);
         return false;
      }
   }

   private static Proxy normalize(Proxy proxy) {
      return proxy == null ? new Proxy() : proxy.copy();
   }

   private static String stringValue(JsonObject root, String key, String fallback) {
      try {
         return root.has(key) && !root.get(key).isJsonNull() ? root.get(key).getAsString() : fallback;
      } catch (RuntimeException ignored) {
         return fallback;
      }
   }

   private static boolean booleanValue(JsonObject root, String key, boolean fallback) {
      try {
         return root.has(key) && !root.get(key).isJsonNull() ? root.get(key).getAsBoolean() : fallback;
      } catch (RuntimeException ignored) {
         return fallback;
      }
   }

   private static int intValue(JsonObject root, String key, int fallback) {
      try {
         return root.has(key) && !root.get(key).isJsonNull() ? root.get(key).getAsInt() : fallback;
      } catch (RuntimeException ignored) {
         return fallback;
      }
   }

   private static Path configPath() {
      return FabricLoader.getInstance().getConfigDir().resolve("ProxyServerConfig.json");
   }

   private static void write(Path path, JsonObject root) {
      try {
         Files.createDirectories(path.getParent());
         Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
         Files.writeString(temporary, GSON.toJson(root), StandardCharsets.UTF_8);

         try {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
         }
      } catch (Exception exception) {
         LOGGER.error("Failed to save proxy config {}", path, exception);
      }
   }

   private static void backupCorrupt(Path path) {
      Path backup = path.resolveSibling(path.getFileName() + ".corrupt-" + LocalDateTime.now().format(BACKUP_TIME));

      try {
         Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
      } catch (Exception exception) {
         LOGGER.error("Failed to preserve corrupt proxy config {}", path, exception);
      }
   }
}
