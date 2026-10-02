package org.ryzen.feature.impl.misc.autobuy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.ConfigIO;

@Environment(EnvType.CLIENT)
public final class AutoBuyManager {
   private static final AutoBuyManager INSTANCE = new AutoBuyManager();
   private static final long SAVE_DEBOUNCE_MS = 120L;
   private static final ScheduledExecutorService SAVE_EXECUTOR = Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable, "Ryzen AutoBuy Save");
      thread.setDaemon(true);
      return thread;
   });
   private final AtomicBoolean savePending = new AtomicBoolean();
   private volatile JsonObject pendingSnapshot;
   private volatile boolean loaded;

   private AutoBuyManager() {
   }

   public static AutoBuyManager get() {
      return INSTANCE;
   }

   public List<AutoBuyItem> allItems() {
      this.ensureLoaded();
      return AutoBuyCatalog.all();
   }

   public List<AutoBuyItem> itemsForServer(String serverMode) {
      this.ensureLoaded();
      return AutoBuyCatalog.forServer(serverMode);
   }

   public List<AutoBuyItem> enabledItems(String serverMode) {
      List<AutoBuyItem> result = new ArrayList<>();

      for (AutoBuyItem item : this.itemsForServerOrAll(serverMode)) {
         if (item.isEnabled()) {
            result.add(item);
         }
      }

      return result;
   }

   public List<AutoBuyItem> byCategoryForServer(AutoBuyItemCategory category, String serverMode) {
      this.ensureLoaded();
      return AutoBuyCatalog.byCategoryForServer(category, serverMode);
   }

   public AutoBuyItem findByName(String name, String serverMode) {
      this.ensureLoaded();
      if (name == null) {
         return null;
      }

      String key = AutoBuyItem.normalizeKey(name);
      AutoBuyItem best = null;
      int bestScore = 0;

      for (AutoBuyItem item : this.itemsForServerOrAll(serverMode)) {
         if (item.getId().equals(key)) {
            return item;
         }

         int score = item.matchScore(name);
         if (score > bestScore) {
            bestScore = score;
            best = item;
         }
      }

      return best;
   }

   public void setEnabled(AutoBuyItem item, boolean enabled) {
      if (item != null) {
         item.setEnabled(enabled);
         this.scheduleSave();
      }
   }

   public void setBuyPrice(AutoBuyItem item, int price) {
      if (item != null) {
         item.setBuyPrice(price);
         this.scheduleSave();
      }
   }

   public void disableAll(String serverMode) {
      for (AutoBuyItem item : this.itemsForServerOrAll(serverMode)) {
         item.setEnabled(false);
      }

      this.scheduleSave();
   }

   public int enabledCount(String serverMode) {
      int count = 0;

      for (AutoBuyItem item : this.itemsForServerOrAll(serverMode)) {
         if (item.isEnabled()) {
            count++;
         }
      }

      return count;
   }

   private List<AutoBuyItem> itemsForServerOrAll(String serverMode) {
      this.ensureLoaded();
      return serverMode != null && !serverMode.isBlank() ? AutoBuyCatalog.forServer(serverMode) : AutoBuyCatalog.all();
   }

   public synchronized void ensureLoaded() {
      if (!this.loaded) {
         this.loaded = true;
         AutoBuyCatalog.all();
         this.load();
      }
   }

   public void scheduleSave() {
      this.pendingSnapshot = this.buildSnapshot();
      if (this.savePending.compareAndSet(false, true)) {
         SAVE_EXECUTOR.schedule(() -> {
            this.savePending.set(false);
            JsonObject snapshot = this.pendingSnapshot;
            if (snapshot != null) {
               ConfigIO.write(configPath(), snapshot);
            }
         }, 120L, TimeUnit.MILLISECONDS);
      }
   }

   public synchronized void saveNow() {
      this.pendingSnapshot = this.buildSnapshot();
      ConfigIO.write(configPath(), this.pendingSnapshot);
   }

   public synchronized void reload() {
      this.ensureLoaded();
      this.load();
   }

   private JsonObject buildSnapshot() {
      JsonObject items = new JsonObject();

      for (AutoBuyItem item : AutoBuyCatalog.all()) {
         JsonObject entry = new JsonObject();
         entry.addProperty("enabled", item.isEnabled());
         entry.addProperty("buyPrice", item.getBuyPrice());
         entry.addProperty("minQty", item.getMinQty());
         items.add(item.getId(), entry);
      }

      JsonObject root = new JsonObject();
      root.add("items", items);
      return root;
   }

   private synchronized void load() {
      JsonObject root = ConfigIO.read(configPath());
      if (root != null && root.has("items") && root.get("items").isJsonObject()) {
         JsonObject items = root.getAsJsonObject("items");

         for (AutoBuyItem item : AutoBuyCatalog.all()) {
            JsonElement entry = items.get(item.getId());
            if (entry == null || !entry.isJsonObject()) {
               entry = items.get(item.getName());
            }

            if (entry != null && entry.isJsonObject()) {
               JsonObject values = entry.getAsJsonObject();
               item.setEnabled(readBoolean(values, "enabled", item.isEnabled()));
               item.setBuyPrice(readInt(values, "buyPrice", item.getBuyPrice()));
               item.setMinQty(readInt(values, "minQty", item.getMinQty()));
            }
         }
      }
   }

   private static int readInt(JsonObject values, String key, int fallback) {
      JsonElement element = values.get(key);
      if (element != null && element.isJsonPrimitive()) {
         try {
            return element.getAsInt();
         } catch (RuntimeException ignored) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   private static boolean readBoolean(JsonObject values, String key, boolean fallback) {
      JsonElement element = values.get(key);
      if (element != null && element.isJsonPrimitive()) {
         try {
            return element.getAsBoolean();
         } catch (RuntimeException ignored) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   private static Path configPath() {
      return ConfigIO.resolve("autobuy.json");
   }
}
