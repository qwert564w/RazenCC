package org.ryzen.feature.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class BooleanSetting extends Setting<Boolean> {
   public BooleanSetting(String name, boolean defaultValue) {
      super(name, defaultValue);
   }

   protected JsonElement writeValue(Boolean value) {
      return new JsonPrimitive(value);
   }

   protected Boolean readValue(JsonElement element) {
      return element.getAsBoolean();
   }
}
