package org.ryzen.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_642;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class StaffManager {
   public static final StaffManager INSTANCE = new StaffManager();
   private static final Logger LOGGER = LoggerFactory.getLogger(StaffManager.class);
   private final Map<String, Map<String, String>> servers = new TreeMap<>();
   private Path path;
   private boolean initialized;
   private String cachedAddress = "";
   private String cachedServer = "";

   private StaffManager() {
   }

   public synchronized void initialize() {
      if (!this.initialized) {
         this.load();
         this.initialized = true;
      }
   }

   public synchronized String currentServer() {
      class_642 server = class_310.method_1551().method_1558();
      String address = server != null && server.field_3761 != null ? server.field_3761 : "";
      if (!address.equals(this.cachedAddress)) {
         this.cachedAddress = address;
         this.cachedServer = serverKey(address);
      }

      return this.cachedServer;
   }

   public synchronized boolean add(String server, String name) {
      String nick = sanitize(name);
      if (!server.isEmpty() && isValidName(nick)) {
         Map<String, String> names = this.servers.computeIfAbsent(server, ignored -> new LinkedHashMap<>());
         if (names.putIfAbsent(normalize(nick), nick) != null) {
            return false;
         }

         this.save();
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean remove(String server, String name) {
      if (name == null) {
         return false;
      }

      Map<String, String> names = this.servers.get(server);
      if (names != null && names.remove(normalize(name)) != null) {
         if (names.isEmpty()) {
            this.servers.remove(server);
         }

         this.save();
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean isStaff(String server, String name) {
      if (name != null && !server.isEmpty()) {
         Map<String, String> names = this.servers.get(server);
         return names != null && names.containsKey(normalize(name));
      } else {
         return false;
      }
   }

   public boolean isStaff(String name) {
      return this.isStaff(this.currentServer(), name);
   }

   public synchronized List<String> names(String server) {
      Map<String, String> names = this.servers.get(server);
      if (names == null) {
         return List.of();
      }

      List<String> sorted = new ArrayList<>(names.values());
      sorted.sort(String.CASE_INSENSITIVE_ORDER);
      return List.copyOf(sorted);
   }

   public synchronized Map<String, List<String>> all() {
      Map<String, List<String>> result = new LinkedHashMap<>();

      for (String server : this.servers.keySet()) {
         result.put(server, this.names(server));
      }

      return result;
   }

   public static String serverKey(String address) {
      if (address == null) {
         return "";
      }

      String host = address.trim().toLowerCase(Locale.ROOT);
      if (host.startsWith("[")) {
         int end = host.indexOf(93);
         return end < 0 ? host : host.substring(0, end + 1);
      }

      int port = host.indexOf(58);
      if (port >= 0) {
         host = host.substring(0, port);
      }

      if (!host.isEmpty() && !host.matches("[0-9.]+")) {
         String[] labels = host.split("\\.");
         return labels.length <= 2 ? host : labels[labels.length - 2] + "." + labels[labels.length - 1];
      } else {
         return host;
      }
   }

   public static boolean isValidName(String name) {
      return name != null && name.matches("[A-Za-z0-9_]{1,16}");
   }

   private Path path() {
      if (this.path == null) {
         this.path = ConfigIO.resolve("staff.json");
      }

      return this.path;
   }

   private void load() {
      JsonObject root = ConfigIO.read(this.path());
      if (root != null) {
         try {
            JsonElement element = root.get("servers");
            if (element == null || !element.isJsonObject()) {
               return;
            }

            for (Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
               String server = serverKey(entry.getKey());
               if (!server.isEmpty() && entry.getValue().isJsonArray()) {
                  Map<String, String> names = this.servers.computeIfAbsent(server, ignored -> new LinkedHashMap<>());

                  for (JsonElement name : entry.getValue().getAsJsonArray()) {
                     String nick = name.isJsonPrimitive() ? sanitize(name.getAsString()) : null;
                     if (isValidName(nick)) {
                        names.putIfAbsent(normalize(nick), nick);
                     }
                  }

                  if (names.isEmpty()) {
                     this.servers.remove(server);
                  }
               }
            }
         } catch (Exception exception) {
            LOGGER.error("Failed to load staff from {}", this.path(), exception);
         }
      }
   }

   private void save() {
      JsonObject entries = new JsonObject();

      for (Entry<String, Map<String, String>> entry : this.servers.entrySet()) {
         JsonArray names = new JsonArray();
         entry.getValue().values().forEach(names::add);
         entries.add(entry.getKey(), names);
      }

      JsonObject root = new JsonObject();
      root.add("servers", entries);
      ConfigIO.write(this.path(), root);
   }

   private static String sanitize(String value) {
      if (value == null) {
         return null;
      }

      String name = value.trim();
      return name.isEmpty() ? null : name;
   }

   private static String normalize(String value) {
      return value.trim().toLowerCase(Locale.ROOT);
   }
}
