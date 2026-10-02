package org.ryzen.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1293;
import net.minecraft.class_155;
import net.minecraft.class_1703;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1844;
import net.minecraft.class_2509;
import net.minecraft.class_2520;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_5455;
import net.minecraft.class_642;
import net.minecraft.class_6880;
import net.minecraft.class_6903;
import net.minecraft.class_7923;
import net.minecraft.class_8824;
import net.minecraft.class_9290;
import net.minecraft.class_9326;
import net.minecraft.class_9331;
import net.minecraft.class_9334;
import net.minecraft.class_9336;
import net.minecraft.class_7225.class_7874;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.ShutdownEvent;
import org.ryzen.event.events.screen.ScreenCloseEvent;
import org.ryzen.feature.impl.misc.DonItems;
import org.ryzen.utils.text.ChatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class ScreenNbtParser {
   public static final ScreenNbtParser INSTANCE = new ScreenNbtParser();
   public static final String OUTPUT_FILE = "blade-nbt-parser.jsonl";
   private static final Logger LOGGER = LoggerFactory.getLogger(ScreenNbtParser.class);
   private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
   private static final int STABLE_TICKS = 2;
   private static final int MAX_DIRTY_TICKS = 20;
   private static final int SCHEMA_VERSION = 1;
   private boolean capturing;
   private Path outputPath;
   private class_437 trackedScreen;
   private String observedFingerprint;
   private String dumpedFingerprint;
   private int stableTicks;
   private int dirtyTicks;
   private int snapshotCount;
   private String lastError;

   private ScreenNbtParser() {
   }

   public boolean start(class_310 client) {
      Path path = this.resolveOutputPath(client);

      try {
         Files.createDirectories(path.getParent());
         Files.writeString(path, "", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
         this.outputPath = path;
         this.capturing = true;
         this.snapshotCount = 0;
         this.lastError = null;
         this.resetTrackedScreen();
         this.writeRecord(this.sessionRecord("session_start", client));
         return true;
      } catch (IOException exception) {
         return this.fail("Unable to start NBT parser", exception);
      }
   }

   public boolean stop(class_310 client) {
      if (!this.capturing) {
         this.lastError = "NBT parser is not running";
         return false;
      }

      try {
         JsonObject record = this.sessionRecord("session_end", client);
         record.addProperty("snapshots", this.snapshotCount);
         this.writeRecord(record);
         this.capturing = false;
         this.resetTrackedScreen();
         return true;
      } catch (IOException exception) {
         this.capturing = false;
         return this.fail("Unable to finish NBT parser log", exception);
      }
   }

   public boolean clear(class_310 client) {
      Path path = this.resolveOutputPath(client);

      try {
         Files.createDirectories(path.getParent());
         Files.writeString(path, "", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
         this.outputPath = path;
         this.snapshotCount = 0;
         this.lastError = null;
         this.resetTrackedScreen();
         if (this.capturing) {
            this.writeRecord(this.sessionRecord("session_start", client));
         }

         return true;
      } catch (IOException exception) {
         return this.fail("Unable to clear NBT parser log", exception);
      }
   }

   public boolean captureNow(class_310 client) {
      if (!this.capturing) {
         this.lastError = "NBT parser is not running";
         return false;
      }

      if (client.field_1755 instanceof class_465<?> screen) {
         try {
            this.track(screen);
            this.dumpSnapshot(client, screen, "manual");
            return true;
         } catch (Exception exception) {
            return this.failAndStop("Unable to capture container screen", exception);
         }
      } else {
         this.lastError = "No container screen is currently open";
         return false;
      }
   }

   public boolean isCapturing() {
      return this.capturing;
   }

   public int getSnapshotCount() {
      return this.snapshotCount;
   }

   public String getLastError() {
      return this.lastError;
   }

   public Path getOutputPath(class_310 client) {
      return this.outputPath == null ? this.resolveOutputPath(client) : this.outputPath;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (this.capturing) {
         if (event.getClient().field_1755 instanceof class_465<?> screen) {
            try {
               this.track(screen);
               String fingerprint = this.fingerprint(screen);
               if (Objects.equals(fingerprint, this.observedFingerprint)) {
                  this.stableTicks++;
               } else {
                  this.observedFingerprint = fingerprint;
                  this.stableTicks = 0;
               }

               if (Objects.equals(fingerprint, this.dumpedFingerprint)) {
                  this.dirtyTicks = 0;
                  return;
               }

               this.dirtyTicks++;
               if (this.stableTicks >= 2 || this.dirtyTicks >= 20) {
                  this.dumpSnapshot(event.getClient(), screen, this.stableTicks >= 2 ? "stable" : "timeout");
               }
            } catch (Exception exception) {
               this.failAndStop("NBT parser stopped after a capture error", exception);
               ChatUtil.error("NBT parser stopped  •  " + this.lastError);
            }
         } else {
            this.resetTrackedScreen();
         }
      }
   }

   @EventTarget
   public void onScreenClose(ScreenCloseEvent event) {
      if (this.capturing && event.getScreen() instanceof class_465<?> screen && screen == this.trackedScreen) {
         try {
            String fingerprint = this.fingerprint(screen);
            if (!Objects.equals(fingerprint, this.dumpedFingerprint)) {
               this.observedFingerprint = fingerprint;
               this.dumpSnapshot(event.getClient(), screen, "screen_close");
            }
         } catch (Exception exception) {
            this.failAndStop("NBT parser stopped while saving the closing screen", exception);
            ChatUtil.error("NBT parser stopped  •  " + this.lastError);
         } finally {
            this.resetTrackedScreen();
         }
      }
   }

   @EventTarget
   public void onShutdown(ShutdownEvent event) {
      if (this.capturing) {
         try {
            JsonObject record = this.sessionRecord("session_end", event.getClient());
            record.addProperty("snapshots", this.snapshotCount);
            record.addProperty("reason", "client_shutdown");
            this.writeRecord(record);
         } catch (IOException exception) {
            LOGGER.error("Unable to finish NBT parser log during shutdown", exception);
         } finally {
            this.capturing = false;
         }
      }
   }

   private void track(class_465<?> screen) {
      if (screen != this.trackedScreen) {
         this.trackedScreen = screen;
         this.observedFingerprint = null;
         this.dumpedFingerprint = null;
         this.stableTicks = 0;
         this.dirtyTicks = 0;
      }
   }

   private void resetTrackedScreen() {
      this.trackedScreen = null;
      this.observedFingerprint = null;
      this.dumpedFingerprint = null;
      this.stableTicks = 0;
      this.dirtyTicks = 0;
   }

   private String fingerprint(class_465<?> screen) {
      class_1703 menu = screen.method_17577();
      StringBuilder value = new StringBuilder(64 + menu.field_7761.size() * 16);
      value.append(screen.getClass().getName()).append('\u0000').append(screen.method_25440().getString()).append('\u0000').append(menu.getClass().getName());

      for (int index = 0; index < menu.field_7761.size(); index++) {
         class_1799 stack = ((class_1735)menu.field_7761.get(index)).method_7677();
         value.append('|').append(index).append(':');
         this.appendStackFingerprint(value, stack);
      }

      value.append("|carried:");
      this.appendStackFingerprint(value, menu.method_34255());
      return value.toString();
   }

   private void appendStackFingerprint(StringBuilder target, class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         target.append(stack.method_7947()).append(':').append(class_1799.method_57355(stack));
      } else {
         target.append('0');
      }
   }

   private void dumpSnapshot(class_310 client, class_465<?> screen, String reason) throws IOException {
      class_7874 registries = (class_7874)(client.field_1687 == null ? class_5455.field_40585 : client.field_1687.method_30349());
      class_6903<JsonElement> jsonOps = class_6903.method_46632(JsonOps.INSTANCE, registries);
      class_6903<class_2520> nbtOps = class_6903.method_46632(class_2509.field_11560, registries);
      List<String> snapshotErrors = new ArrayList<>();
      class_1703 menu = screen.method_17577();
      JsonObject record = this.baseRecord("screen_snapshot");
      record.addProperty("sequence", ++this.snapshotCount);
      record.addProperty("reason", reason);
      record.addProperty("screen_class", screen.getClass().getName());
      record.addProperty("title", screen.method_25440().getString());
      record.add("title_json", this.encodeComponent(screen.method_25440(), jsonOps, snapshotErrors));
      record.addProperty("menu_class", menu.getClass().getName());
      record.addProperty("menu_type", this.menuTypeId(menu));
      record.addProperty("container_id", menu.field_7763);
      record.addProperty("state_id", menu.method_37421());
      record.addProperty("slot_count", menu.field_7761.size());
      int occupiedSlots = 0;
      JsonArray slots = new JsonArray();

      for (int index = 0; index < menu.field_7761.size(); index++) {
         class_1735 slot = (class_1735)menu.field_7761.get(index);
         if (slot.method_7681()) {
            occupiedSlots++;
         }

         slots.add(this.encodeSlot(index, slot, jsonOps, nbtOps));
      }

      record.addProperty("occupied_slot_count", occupiedSlots);
      record.add("slots", slots);
      class_1799 carried = menu.method_34255();
      if (carried != null && !carried.method_7960()) {
         record.add("carried", this.encodeItem(carried, jsonOps, nbtOps));
      } else {
         record.add("carried", JsonNull.INSTANCE);
      }

      if (!snapshotErrors.isEmpty()) {
         record.add("serialization_errors", this.strings(snapshotErrors));
      }

      this.writeRecord(record);
      this.dumpedFingerprint = this.fingerprint(screen);
      this.observedFingerprint = this.dumpedFingerprint;
      this.stableTicks = 0;
      this.dirtyTicks = 0;
   }

   private JsonObject encodeSlot(int menuIndex, class_1735 slot, class_6903<JsonElement> jsonOps, class_6903<class_2520> nbtOps) {
      JsonObject result = new JsonObject();
      result.addProperty("menu_index", menuIndex);
      result.addProperty("container_slot", slot.method_34266());
      result.addProperty("x", slot.field_7873);
      result.addProperty("y", slot.field_7872);
      result.addProperty("container_class", slot.field_7871.getClass().getName());
      result.addProperty("active", slot.method_7682());
      result.addProperty("fake", slot.method_55059());
      class_1799 stack = slot.method_7677();
      boolean empty = stack == null || stack.method_7960();
      result.addProperty("empty", empty);
      if (!empty) {
         try {
            result.add("item", this.encodeItem(stack, jsonOps, nbtOps));
         } catch (RuntimeException exception) {
            JsonObject failed = new JsonObject();
            failed.addProperty("item_id", this.itemId(stack));
            failed.addProperty("count", stack.method_7947());
            failed.addProperty("error", exception.toString());
            result.add("item", failed);
         }
      }

      return result;
   }

   private JsonObject encodeItem(class_1799 stack, class_6903<JsonElement> jsonOps, class_6903<class_2520> nbtOps) {
      List<String> errors = new ArrayList<>();
      JsonObject result = new JsonObject();
      result.addProperty("item_id", this.itemId(stack));
      result.addProperty("item_class", stack.method_7909().getClass().getName());
      result.addProperty("count", stack.method_7947());
      result.addProperty("hover_name", stack.method_7964().getString());
      result.add("hover_name_json", this.encodeComponent(stack.method_7964(), jsonOps, errors));
      result.addProperty("item_name", stack.method_63693().getString());
      result.add("item_name_json", this.encodeComponent(stack.method_63693(), jsonOps, errors));
      result.addProperty("foil", stack.method_7958());
      result.addProperty("damageable", stack.method_7963());
      if (stack.method_7963()) {
         result.addProperty("damage", stack.method_7919());
         result.addProperty("max_damage", stack.method_7936());
      }

      result.addProperty("component_patch_size", stack.method_57380().method_57847());
      result.add("component_patch", this.encodeComponentPatch(stack.method_57380(), jsonOps, errors));
      result.add("resolved_component_ids", this.resolvedComponentIds(stack));
      result.add("lore", this.encodeLore(stack, jsonOps, errors));
      result.add("enchantments", this.encodeEnchantments(stack));
      result.add("potion", this.encodePotion(stack));
      DonItems.findAny(stack).ifPresent(item -> {
         JsonObject recognized = new JsonObject();
         recognized.addProperty("server", item.server().name());
         recognized.addProperty("category", item.category().name());
         recognized.addProperty("enum", ((Enum)item).name());
         recognized.addProperty("display_name", item.displayName());
         result.add("don_item", recognized);
      });
      result.add("stack_json", this.valueOrFallback(class_1799.field_24671.encodeStart(jsonOps, stack), errors, new JsonPrimitive(stack.toString())));
      class_2520 encodedNbt = this.valueOrFallback(class_1799.field_24671.encodeStart(nbtOps, stack), errors, null);
      result.addProperty("stack_snbt", encodedNbt == null ? stack.toString() : encodedNbt.toString());
      if (!errors.isEmpty()) {
         result.add("serialization_errors", this.strings(errors));
      }

      return result;
   }

   private JsonObject encodeComponentPatch(class_9326 patch, class_6903<JsonElement> jsonOps, List<String> errors) {
      List<Entry<class_9331<?>, Optional<?>>> entries = new ArrayList<>(patch.method_57846());
      entries.sort(Comparator.comparing(entryx -> this.componentId((class_9331<?>)entryx.getKey())));
      JsonObject result = new JsonObject();

      for (Entry<class_9331<?>, Optional<?>> entry : entries) {
         String id = this.componentId(entry.getKey());
         if (entry.getValue().isEmpty()) {
            result.add(id, JsonNull.INSTANCE);
         } else {
            class_9336<?> component = class_9336.method_57945(entry.getKey(), entry.getValue().get());
            result.add(id, this.valueOrFallback(component.method_57943(jsonOps), errors, new JsonPrimitive(String.valueOf(entry.getValue().get()))));
         }
      }

      return result;
   }

   private JsonArray resolvedComponentIds(class_1799 stack) {
      List<String> ids = new ArrayList<>(stack.method_57353().method_57835());

      for (class_9336<?> component : stack.method_57353()) {
         ids.add(this.componentId(component.comp_2443()));
      }

      ids.sort(String::compareTo);
      return this.strings(ids);
   }

   private JsonArray encodeLore(class_1799 stack, class_6903<JsonElement> jsonOps, List<String> errors) {
      JsonArray result = new JsonArray();
      class_9290 lore = (class_9290)stack.method_58694(class_9334.field_49632);
      if (lore == null) {
         return result;
      }

      List<class_2561> lines = lore.comp_2401();

      for (int index = 0; index < lines.size(); index++) {
         class_2561 line = lines.get(index);
         JsonObject encoded = new JsonObject();
         encoded.addProperty("index", index);
         encoded.addProperty("text", line.getString());
         encoded.add("json", this.encodeComponent(line, jsonOps, errors));
         result.add(encoded);
      }

      return result;
   }

   private JsonArray encodeEnchantments(class_1799 stack) {
      JsonArray result = new JsonArray();
      stack.method_58657().method_57539().stream().sorted(Comparator.comparing(entry -> this.holderId((class_6880<?>)entry.getKey()))).forEach(entry -> {
         JsonObject enchantment = new JsonObject();
         enchantment.addProperty("id", this.holderId((class_6880<?>)entry.getKey()));
         enchantment.addProperty("level", entry.getIntValue());
         result.add(enchantment);
      });
      return result;
   }

   private JsonElement encodePotion(class_1799 stack) {
      class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
      if (contents == null) {
         return JsonNull.INSTANCE;
      }

      JsonObject result = new JsonObject();
      contents.comp_2378()
         .ifPresentOrElse(potion -> result.addProperty("base_potion", this.holderId((class_6880<?>)potion)), () -> result.add("base_potion", JsonNull.INSTANCE));
      contents.comp_2379().ifPresentOrElse(color -> result.addProperty("custom_color", color), () -> result.add("custom_color", JsonNull.INSTANCE));
      contents.comp_3209().ifPresentOrElse(name -> result.addProperty("custom_name", name), () -> result.add("custom_name", JsonNull.INSTANCE));
      JsonArray effects = new JsonArray();

      for (class_1293 effect : contents.method_57397()) {
         JsonObject encoded = new JsonObject();
         encoded.addProperty("id", this.holderId(effect.method_5579()));
         encoded.addProperty("description_id", effect.method_5586());
         encoded.addProperty("amplifier", effect.method_5578());
         encoded.addProperty("level", effect.method_5578() + 1);
         encoded.addProperty("duration_ticks", effect.method_5584());
         encoded.addProperty("infinite", effect.method_48559());
         encoded.addProperty("ambient", effect.method_5591());
         encoded.addProperty("visible", effect.method_5581());
         effects.add(encoded);
      }

      result.add("effects", effects);
      return result;
   }

   private JsonElement encodeComponent(class_2561 component, class_6903<JsonElement> jsonOps, List<String> errors) {
      return this.valueOrFallback(class_8824.field_46597.encodeStart(jsonOps, component), errors, new JsonPrimitive(component.getString()));
   }

   private <T> T valueOrFallback(DataResult<T> result, List<String> errors, T fallback) {
      return (T)result.resultOrPartial(errors::add).orElse(fallback);
   }

   private JsonObject sessionRecord(String type, class_310 client) {
      JsonObject record = this.baseRecord(type);
      record.addProperty("schema_version", 1);
      record.addProperty("minecraft_version", class_155.method_16673().comp_4025());
      record.addProperty("output_file", "blade-nbt-parser.jsonl");
      class_642 server = client.method_1558();
      if (server != null) {
         record.addProperty("server_name", server.field_3752);
         record.addProperty("server_address", server.field_3761);
      }

      return record;
   }

   private JsonObject baseRecord(String type) {
      JsonObject record = new JsonObject();
      record.addProperty("type", type);
      record.addProperty("captured_at", Instant.now().toString());
      return record;
   }

   private JsonArray strings(List<String> values) {
      JsonArray result = new JsonArray();
      values.forEach(result::add);
      return result;
   }

   private String menuTypeId(class_1703 menu) {
      if (menu.method_17358() == null) {
         return "unregistered";
      }

      class_2960 id = class_7923.field_41187.method_10221(menu.method_17358());
      return id == null ? "unregistered" : id.toString();
   }

   private String itemId(class_1799 stack) {
      class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
      return id == null ? "unregistered" : id.toString();
   }

   private String componentId(class_9331<?> type) {
      class_2960 id = class_7923.field_49658.method_10221(type);
      return id == null ? "unregistered@" + Integer.toHexString(System.identityHashCode(type)) : id.toString();
   }

   private String holderId(class_6880<?> holder) {
      return holder.method_40230().map(key -> key.method_29177().toString()).orElseGet(() -> "direct:" + holder.comp_349());
   }

   private Path resolveOutputPath(class_310 client) {
      return client.field_1697.toPath().resolve("logs").resolve("blade-nbt-parser.jsonl");
   }

   private void writeRecord(JsonObject record) throws IOException {
      if (this.outputPath == null) {
         throw new IOException("Output path is not initialized");
      }

      Files.writeString(
         this.outputPath, GSON.toJson(record) + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND
      );
   }

   private boolean fail(String message, Exception exception) {
      this.lastError = message + ": " + exception.getMessage();
      LOGGER.error(message, exception);
      return false;
   }

   private boolean failAndStop(String message, Exception exception) {
      this.capturing = false;
      return this.fail(message, exception);
   }
}
