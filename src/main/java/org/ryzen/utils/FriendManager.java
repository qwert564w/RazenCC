package org.ryzen.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_640;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class FriendManager {
   public static final FriendManager INSTANCE = new FriendManager();
   private static final Logger LOGGER = LoggerFactory.getLogger(FriendManager.class);
   private static final long LAST_SEEN_SAVE_INTERVAL_MS = 60000L;
   private final Path path = ConfigIO.resolve("friends.json");
   private final Map<String, FriendManager.FriendEntry> friends = new LinkedHashMap<>();
   private boolean initialized;
   private long nextPresenceUpdate;

   private FriendManager() {
   }

   public synchronized void initialize() {
      if (!this.initialized) {
         this.load();
         this.initialized = true;
      }
   }

   public synchronized boolean add(String name) {
      String displayName = sanitize(name);
      if (isValidName(displayName) && !this.friends.containsKey(normalize(displayName))) {
         this.friends.put(normalize(displayName), new FriendManager.FriendEntry(displayName, false, 0L));
         this.save();
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean remove(String name) {
      if (name != null && this.friends.remove(normalize(name)) != null) {
         this.save();
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean isFriend(String name) {
      return name != null && this.friends.containsKey(normalize(name));
   }

   public synchronized Collection<String> getFriends() {
      return this.getEntries().stream().map(FriendManager.FriendEntry::name).toList();
   }

   public synchronized List<FriendManager.FriendEntry> getEntries() {
      List<FriendManager.FriendEntry> entries = new ArrayList<>(this.friends.values());
      entries.sort(Comparator.comparing(FriendManager.FriendEntry::pinned).reversed());
      return List.copyOf(entries);
   }

   public synchronized boolean togglePinned(String name) {
      if (name == null) {
         return false;
      }

      String key = normalize(name);
      FriendManager.FriendEntry entry = this.friends.get(key);
      if (entry == null) {
         return false;
      }

      this.friends.put(key, new FriendManager.FriendEntry(entry.name(), !entry.pinned(), entry.lastSeen()));
      this.save();
      return true;
   }

   public synchronized void markSeen(String name) {
      if (name != null) {
         String key = normalize(name);
         FriendManager.FriendEntry entry = this.friends.get(key);
         if (entry != null) {
            long now = System.currentTimeMillis();
            if (now - entry.lastSeen() >= 60000L) {
               this.friends.put(key, new FriendManager.FriendEntry(entry.name(), entry.pinned(), now));
               this.save();
            }
         }
      }
   }

   public synchronized boolean clear() {
      if (this.friends.isEmpty()) {
         return false;
      }

      this.friends.clear();
      this.save();
      return true;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      long now = System.currentTimeMillis();
      if (now >= this.nextPresenceUpdate && event.getClient().method_1562() != null) {
         this.nextPresenceUpdate = now + 5000L;

         for (class_640 player : event.getClient().method_1562().method_2880()) {
            this.markSeen(player.method_2966().name());
         }
      }
   }

   public static boolean isValidName(String name) {
      return name != null && name.matches("[A-Za-z0-9_]{1,16}");
   }

   private void load() {
      JsonObject root = ConfigIO.read(this.path);
      if (root != null) {
         try {
            JsonElement entries = root.get("friends");
            if (entries == null || !entries.isJsonArray()) {
               return;
            }

            for (JsonElement element : entries.getAsJsonArray()) {
               FriendManager.FriendEntry entry = this.readEntry(element);
               if (entry != null) {
                  this.friends.putIfAbsent(normalize(entry.name()), entry);
               }
            }
         } catch (Exception exception) {
            LOGGER.error("Failed to load friends from {}", this.path, exception);
         }
      }
   }

   private FriendManager.FriendEntry readEntry(JsonElement element) {
      if (element.isJsonPrimitive()) {
         String name = sanitize(element.getAsString());
         return isValidName(name) ? new FriendManager.FriendEntry(name, false, 0L) : null;
      }

      if (!element.isJsonObject()) {
         return null;
      }

      JsonObject object = element.getAsJsonObject();
      String name = object.has("name") ? sanitize(object.get("name").getAsString()) : null;
      if (!isValidName(name)) {
         return null;
      }

      boolean pinned = object.has("pinned") && object.get("pinned").getAsBoolean();
      long lastSeen = object.has("lastSeen") ? Math.max(0L, object.get("lastSeen").getAsLong()) : 0L;
      return new FriendManager.FriendEntry(name, pinned, lastSeen);
   }

   private void save() {
      JsonArray entries = new JsonArray();

      for (FriendManager.FriendEntry friend : this.friends.values()) {
         JsonObject entry = new JsonObject();
         entry.addProperty("name", friend.name());
         entry.addProperty("pinned", friend.pinned());
         entry.addProperty("lastSeen", friend.lastSeen());
         entries.add(entry);
      }

      JsonObject root = new JsonObject();
      root.add("friends", entries);
      ConfigIO.write(this.path, root);
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

   @Environment(EnvType.CLIENT)
   public record FriendEntry(String name, boolean pinned, long lastSeen) {
   }
}
