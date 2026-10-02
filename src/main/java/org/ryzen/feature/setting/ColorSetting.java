package org.ryzen.feature.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.ColorUtil;

@Environment(EnvType.CLIENT)
public final class ColorSetting extends Setting<Integer> {
   public ColorSetting(String name, int defaultValue) {
      super(name, ColorUtil.withAlpha(defaultValue, 255));
   }

   protected Integer normalize(Integer value) {
      return ColorUtil.withAlpha(value == null ? this.getDefaultValue() : value, 255);
   }

   protected JsonElement writeValue(Integer value) {
      return new JsonPrimitive(ColorUtil.toHex(value));
   }

   protected Integer readValue(JsonElement element) {
      if (element != null && !element.isJsonNull()) {
         if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
               return ColorUtil.withAlpha(primitive.getAsInt(), 255);
            }

            Integer parsed = ColorUtil.parse(primitive.getAsString());
            if (parsed != null) {
               return parsed;
            }
         }

         return this.getDefaultValue();
      } else {
         return this.getDefaultValue();
      }
   }
}
