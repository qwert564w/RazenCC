package org.ryzen.feature.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.BindMode;

@Environment(EnvType.CLIENT)
public final class BindSetting extends Setting<List<Integer>> {
   public static final int UNBOUND = -1;
   private static final int MOUSE_FLAG = 1073741824;
   private List<BindSetting.Meta> meta = List.of();

   public BindSetting(String name) {
      this(name, List.of());
   }

   public BindSetting(String name, int defaultValue) {
      this(name, defaultValue == -1 ? List.of() : List.of(defaultValue));
   }

   public BindSetting(String name, List<Integer> defaultValue) {
      super(name, defaultValue == null ? List.of() : List.copyOf(defaultValue));
   }

   public static int key(int keyCode) {
      return keyCode;
   }

   public static int mouse(int button) {
      return 1073741824 | button;
   }

   public static boolean isMouse(int bindCode) {
      return bindCode != -1 && (bindCode & 1073741824) != 0;
   }

   public static boolean isKeyboard(int bindCode) {
      return bindCode != -1 && !isMouse(bindCode);
   }

   public static int rawButton(int bindCode) {
      return bindCode & -1073741825;
   }

   public boolean isBound() {
      return !this.getValue().isEmpty();
   }

   public int size() {
      return this.getValue().size();
   }

   public boolean isEmpty() {
      return this.getValue().isEmpty();
   }

   public boolean hasIndex(int index) {
      return index >= 0 && index < this.getValue().size();
   }

   public int get(int index) {
      return this.getValue().get(index);
   }

   public boolean contains(int bindCode) {
      return this.getValue().contains(bindCode);
   }

   public boolean matches(int keyCode) {
      return this.matchesCode(key(keyCode));
   }

   public boolean matchesMouse(int button) {
      return this.matchesCode(mouse(button));
   }

   public boolean matchesCode(int bindCode) {
      return bindCode != -1 && this.getValue().contains(bindCode);
   }

   public void setSingle(int bindCode) {
      BindSetting.Meta preserved = this.getMeta(0);
      this.setValue(bindCode == -1 ? List.of() : List.of(bindCode));
      this.meta = this.getValue().isEmpty() ? List.of() : List.of(preserved);
   }

   public int add(int bindCode) {
      int normalized = this.normalizeCode(bindCode);
      if (normalized == -1) {
         return -1;
      }

      List<Integer> binds = new ArrayList<>(this.getValue());
      int existingIndex = binds.indexOf(normalized);
      if (existingIndex >= 0) {
         return existingIndex;
      }

      List<BindSetting.Meta> nextMeta = new ArrayList<>(this.alignedMeta());
      binds.add(normalized);
      this.setValue(binds);
      nextMeta.add(BindSetting.Meta.DEFAULT);
      this.meta = this.fitMeta(nextMeta, this.getValue().size());
      return binds.size() - 1;
   }

   public int setAt(int index, int bindCode) {
      if (index < 0) {
         return this.add(bindCode);
      }

      int normalized = this.normalizeCode(bindCode);
      List<Integer> binds = new ArrayList<>(this.getValue());
      if (normalized == -1) {
         this.removeAt(index);
         return -1;
      }

      List<BindSetting.Meta> bindMeta = new ArrayList<>(this.alignedMeta());
      if (index >= binds.size()) {
         binds.add(normalized);
         bindMeta.add(BindSetting.Meta.DEFAULT);
      } else {
         binds.set(index, normalized);
      }

      LinkedHashSet<Integer> unique = new LinkedHashSet<>();
      List<Integer> normalizedBinds = new ArrayList<>();
      List<BindSetting.Meta> normalizedMeta = new ArrayList<>();
      int resolvedIndex = -1;

      for (int i = 0; i < binds.size(); i++) {
         int code = binds.get(i);
         BindSetting.Meta entry = i < bindMeta.size() ? bindMeta.get(i) : BindSetting.Meta.DEFAULT;
         boolean added = unique.add(code);
         if (added) {
            normalizedBinds.add(code);
            normalizedMeta.add(entry);
            if (i == index) {
               resolvedIndex = normalizedBinds.size() - 1;
            }
         }

         if (!added && code == normalized && i == index) {
            resolvedIndex = normalizedBinds.indexOf(normalized);
            if (resolvedIndex >= 0) {
               normalizedMeta.set(resolvedIndex, entry);
            }
         }
      }

      this.setValue(normalizedBinds);
      this.meta = this.fitMeta(normalizedMeta, this.getValue().size());
      return resolvedIndex >= 0 ? resolvedIndex : this.getValue().indexOf(normalized);
   }

   public void removeAt(int index) {
      if (this.hasIndex(index)) {
         List<Integer> binds = new ArrayList<>(this.getValue());
         List<BindSetting.Meta> bindMeta = new ArrayList<>(this.alignedMeta());
         binds.remove(index);
         if (index < bindMeta.size()) {
            bindMeta.remove(index);
         }

         this.setValue(binds);
         this.meta = this.fitMeta(bindMeta, this.getValue().size());
      }
   }

   public void clear() {
      this.setValue(List.of());
      this.meta = List.of();
   }

   public BindMode getMode(int index, BindMode fallback) {
      if (!this.hasIndex(index)) {
         return fallback == null ? BindMode.TOGGLE : fallback;
      } else {
         return this.getMeta(index).mode();
      }
   }

   public BindMode getModeForCode(int bindCode, BindMode fallback) {
      int index = this.getValue().indexOf(bindCode);
      return this.getMode(index, fallback);
   }

   public void setMode(int index, BindMode mode) {
      if (this.hasIndex(index) && mode != null) {
         this.updateMeta(index, this.getMeta(index).withMode(mode));
      }
   }

   public void setAllModes(BindMode mode) {
      if (mode != null && !this.getValue().isEmpty()) {
         List<BindSetting.Meta> bindMeta = new ArrayList<>();

         for (BindSetting.Meta entry : this.alignedMeta()) {
            bindMeta.add(entry.withMode(mode));
         }

         this.meta = List.copyOf(bindMeta);
      } else {
         this.meta = List.of();
      }
   }

   public boolean isVisibleAt(int index) {
      return !this.hasIndex(index) || this.getMeta(index).visible();
   }

   public void setVisibleAt(int index, boolean visible) {
      if (this.hasIndex(index)) {
         this.updateMeta(index, this.getMeta(index).withVisible(visible));
      }
   }

   public JsonElement writeModes() {
      JsonArray array = new JsonArray();

      for (BindSetting.Meta entry : this.alignedMeta()) {
         array.add(entry.mode().name());
      }

      return array;
   }

   public void readModes(JsonElement element) {
      List<BindMode> bindModes = new ArrayList<>();
      if (element != null && element.isJsonArray()) {
         for (JsonElement item : element.getAsJsonArray()) {
            BindMode mode = this.readMode(item);
            if (mode != null) {
               bindModes.add(mode);
            }
         }
      } else if (element != null && !element.isJsonNull()) {
         BindMode mode = this.readMode(element);
         if (mode != null) {
            bindModes.add(mode);
         }
      }

      List<BindSetting.Meta> bindMeta = new ArrayList<>(this.alignedMeta());

      for (int i = 0; i < bindMeta.size() && i < bindModes.size(); i++) {
         bindMeta.set(i, bindMeta.get(i).withMode(bindModes.get(i)));
      }

      this.meta = List.copyOf(bindMeta);
   }

   public JsonElement writeVisibility() {
      JsonArray array = new JsonArray();

      for (BindSetting.Meta entry : this.alignedMeta()) {
         array.add(entry.visible());
      }

      return array;
   }

   public void readVisibility(JsonElement element) {
      if (element != null && element.isJsonArray()) {
         List<BindSetting.Meta> bindMeta = new ArrayList<>(this.alignedMeta());
         JsonArray array = element.getAsJsonArray();

         for (int i = 0; i < bindMeta.size() && i < array.size(); i++) {
            JsonElement item = array.get(i);
            if (item != null && item.isJsonPrimitive()) {
               bindMeta.set(i, bindMeta.get(i).withVisible(item.getAsBoolean()));
            }
         }

         this.meta = List.copyOf(bindMeta);
      }
   }

   public String getDisplayValue() {
      return this.isBound() ? this.getDisplayValue(0) : "None";
   }

   public String getDisplayValue(int index) {
      return !this.hasIndex(index) ? "None" : describe(this.get(index));
   }

   public static String describe(int bindCode) {
      if (bindCode == -1) {
         return "None";
      }

      if (isMouse(bindCode)) {
         return switch (rawButton(bindCode)) {
            case 0 -> "Mouse Left";
            case 1 -> "Mouse Right";
            case 2 -> "Mouse Middle";
            case 3 -> "Mouse 4";
            case 4 -> "Mouse 5";
            case 5 -> "Mouse 6";
            case 6 -> "Mouse 7";
            case 7 -> "Mouse 8";
            default -> "Mouse " + rawButton(bindCode);
         };
      } else {
         if (bindCode > 32 && bindCode <= 96) {
            return String.valueOf((char)bindCode);
         }

         if (bindCode >= 290 && bindCode <= 314) {
            return "F" + (bindCode - 290 + 1);
         }

         if (bindCode >= 320 && bindCode <= 329) {
            return "Num " + (bindCode - 320);
         }

         return switch (bindCode) {
            case 32 -> "Space";
            case 256 -> "Escape";
            case 257 -> "Enter";
            case 258 -> "Tab";
            case 259 -> "Backspace";
            case 260 -> "Insert";
            case 261 -> "Delete";
            case 262 -> "Right";
            case 263 -> "Left";
            case 264 -> "Down";
            case 265 -> "Up";
            case 266 -> "Page Up";
            case 267 -> "Page Down";
            case 268 -> "Home";
            case 269 -> "End";
            case 280 -> "Caps Lock";
            case 330 -> "Num .";
            case 331 -> "Num /";
            case 332 -> "Num *";
            case 333 -> "Num -";
            case 334 -> "Num +";
            case 335 -> "Num Enter";
            case 336 -> "Num =";
            case 340 -> "LShift";
            case 341 -> "LCtrl";
            case 342 -> "LAlt";
            case 344 -> "RShift";
            case 345 -> "RCtrl";
            case 346 -> "RAlt";
            default -> "Key " + bindCode;
         };
      }
   }

   protected List<Integer> normalize(List<Integer> value) {
      if (value != null && !value.isEmpty()) {
         LinkedHashSet<Integer> unique = new LinkedHashSet<>();

         for (Integer bindCode : value) {
            int normalized = this.normalizeCode(bindCode);
            if (normalized != -1) {
               unique.add(normalized);
            }
         }

         return unique.isEmpty() ? List.of() : List.copyOf(unique);
      } else {
         return List.of();
      }
   }

   protected JsonElement writeValue(List<Integer> value) {
      JsonArray array = new JsonArray();

      for (Integer bindCode : this.normalize(value)) {
         array.add(bindCode);
      }

      return array;
   }

   protected List<Integer> readValue(JsonElement element) {
      if (element != null && !element.isJsonNull()) {
         if (element.isJsonArray()) {
            List<Integer> binds = new ArrayList<>();

            for (JsonElement item : element.getAsJsonArray()) {
               if (item != null && item.isJsonPrimitive()) {
                  binds.add(item.getAsInt());
               }
            }

            return binds;
         } else {
            if (element.isJsonPrimitive()) {
               JsonPrimitive primitive = element.getAsJsonPrimitive();
               if (primitive.isNumber()) {
                  int bindCode = primitive.getAsInt();
                  return bindCode == -1 ? List.of() : List.of(bindCode);
               }
            }

            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private BindMode readMode(JsonElement element) {
      if (element != null && element.isJsonPrimitive()) {
         try {
            return BindMode.valueOf(element.getAsString());
         } catch (IllegalArgumentException exception) {
            return null;
         }
      } else {
         return null;
      }
   }

   private BindSetting.Meta getMeta(int index) {
      return index >= 0 && index < this.meta.size() ? this.meta.get(index) : BindSetting.Meta.DEFAULT;
   }

   private void updateMeta(int index, BindSetting.Meta entry) {
      List<BindSetting.Meta> bindMeta = new ArrayList<>(this.alignedMeta());
      bindMeta.set(index, entry);
      this.meta = List.copyOf(bindMeta);
   }

   private List<BindSetting.Meta> alignedMeta() {
      return this.fitMeta(this.meta, this.getValue().size());
   }

   private List<BindSetting.Meta> fitMeta(List<BindSetting.Meta> source, int size) {
      if (size <= 0) {
         return List.of();
      }

      List<BindSetting.Meta> result = new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
         BindSetting.Meta entry = source != null && i < source.size() ? source.get(i) : null;
         result.add(entry == null ? BindSetting.Meta.DEFAULT : entry);
      }

      return List.copyOf(result);
   }

   private int normalizeCode(Integer value) {
      if (value == null) {
         return -1;
      } else {
         return value < -1 ? -1 : value;
      }
   }

   @Environment(EnvType.CLIENT)
   private record Meta(BindMode mode, boolean visible) {
      static final BindSetting.Meta DEFAULT = new BindSetting.Meta(BindMode.TOGGLE, true);

      BindSetting.Meta withMode(BindMode mode) {
         return new BindSetting.Meta(mode, this.visible);
      }

      BindSetting.Meta withVisible(boolean visible) {
         return new BindSetting.Meta(this.mode, visible);
      }
   }
}
