package org.ryzen.feature.impl.misc.collector;

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
public final class CollectorManager {
   private static final CollectorManager INSTANCE = new CollectorManager();
   private static final ScheduledExecutorService WRITER = Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable, "Ryzen Collector Save");
      thread.setDaemon(true);
      return thread;
   });
   private final AtomicBoolean savePending = new AtomicBoolean();
   private volatile JsonObject snapshot;
   private boolean loaded;

   private CollectorManager() {
   }

   public static CollectorManager get() {
      return INSTANCE;
   }

   public synchronized void ensureLoaded() {
      if (!this.loaded) {
         this.loaded = true;
         JsonObject root = ConfigIO.read(path());
         if (root != null && root.has("items") && root.get("items").isJsonObject()) {
            JsonObject values = root.getAsJsonObject("items");

            for (CollectorItem item : CollectorCatalog.all()) {
               JsonElement element = values.get(item.getId());
               if (element != null && element.isJsonObject()) {
                  JsonObject entry = element.getAsJsonObject();
                  item.setEnabled(bool(entry, "enabled", item.isEnabled()));
                  item.setScanCheapest(bool(entry, "scanCheapest", item.isScanCheapest()));
                  item.setCount(integer(entry, "count", item.getCount()));
                  if (entry.has("conditions") && entry.get("conditions").isJsonObject()) {
                     JsonObject conditions = entry.getAsJsonObject("conditions");

                     for (CollectorCondition condition : item.getConditions()) {
                        JsonElement conditionElement = conditions.get(condition.getId());
                        if (conditionElement != null && conditionElement.isJsonObject()) {
                           JsonObject conditionEntry = conditionElement.getAsJsonObject();
                           condition.setEnabled(bool(conditionEntry, "enabled", condition.isEnabled()));
                           condition.setLevel(integer(conditionEntry, "level", condition.getLevel()));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public List<CollectorItem> all() {
      this.ensureLoaded();
      return CollectorCatalog.all();
   }

   public List<CollectorItem> enabled() {
      List<CollectorItem> result = new ArrayList<>();

      for (CollectorItem item : this.all()) {
         if (item.isEnabled()) {
            result.add(item);
         }
      }

      return result;
   }

   public void setEnabled(CollectorItem item, boolean enabled) {
      item.setEnabled(enabled);
      this.save();
   }

   public void setCount(CollectorItem item, int count) {
      item.setCount(count);
      this.save();
   }

   public void setScanCheapest(CollectorItem item, boolean scan) {
      item.setScanCheapest(scan);
      this.save();
   }

   public void setConditionEnabled(CollectorCondition condition, boolean enabled) {
      condition.setEnabled(enabled);
      this.save();
   }

   public void setConditionLevel(CollectorCondition condition, int level) {
      condition.setLevel(level);
      this.save();
   }

   public int enabledCount() {
      return this.enabled().size();
   }

   private void save() {
      JsonObject items = new JsonObject();

      for (CollectorItem item : CollectorCatalog.all()) {
         JsonObject entry = new JsonObject();
         entry.addProperty("enabled", item.isEnabled());
         entry.addProperty("count", item.getCount());
         entry.addProperty("scanCheapest", item.isScanCheapest());
         JsonObject conditions = new JsonObject();

         for (CollectorCondition condition : item.getConditions()) {
            JsonObject conditionEntry = new JsonObject();
            conditionEntry.addProperty("enabled", condition.isEnabled());
            conditionEntry.addProperty("level", condition.getLevel());
            conditions.add(condition.getId(), conditionEntry);
         }

         entry.add("conditions", conditions);
         items.add(item.getId(), entry);
      }

      JsonObject root = new JsonObject();
      root.add("items", items);
      this.snapshot = root;
      if (this.savePending.compareAndSet(false, true)) {
         WRITER.schedule(() -> {
            this.savePending.set(false);
            ConfigIO.write(path(), this.snapshot);
         }, 120L, TimeUnit.MILLISECONDS);
      }
   }

   private static boolean bool(JsonObject object, String key, boolean fallback) {
      try {
         return object.has(key) ? object.get(key).getAsBoolean() : fallback;
      } catch (RuntimeException ignored) {
         return fallback;
      }
   }

   private static int integer(JsonObject object, String key, int fallback) {
      try {
         return object.has(key) ? object.get(key).getAsInt() : fallback;
      } catch (RuntimeException ignored) {
         return fallback;
      }
   }

   private static Path path() {
      return ConfigIO.resolve("collector.json");
   }
}
