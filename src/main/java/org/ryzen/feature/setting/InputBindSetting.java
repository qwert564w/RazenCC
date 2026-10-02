package org.ryzen.feature.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class InputBindSetting extends Setting<Integer> {
   public static final int UNBOUND = -1;

   public InputBindSetting(String name, int defaultKey) {
      super(name, defaultKey < 0 ? -1 : defaultKey);
   }

   public boolean isBound() {
      return this.getValue() != -1;
   }

   public boolean matches(int keyCode) {
      return this.isBound() && this.getValue() == BindSetting.key(keyCode);
   }

   public boolean matchesMouse(int button) {
      return this.isBound() && this.getValue() == BindSetting.mouse(button);
   }

   public void setKey(int keyCode) {
      this.setValue(BindSetting.key(keyCode));
   }

   public void setMouse(int button) {
      this.setValue(BindSetting.mouse(button));
   }

   public void clear() {
      this.setValue(-1);
   }

   public String getDisplayValue() {
      return this.isBound() ? BindSetting.describe(this.getValue()) : "None";
   }

   protected Integer normalize(Integer value) {
      return value != null && value >= 0 ? value : -1;
   }

   protected JsonElement writeValue(Integer value) {
      return new JsonPrimitive(value);
   }

   protected Integer readValue(JsonElement element) {
      return element.getAsInt();
   }
}
