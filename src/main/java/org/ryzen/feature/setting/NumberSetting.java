package org.ryzen.feature.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Locale;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public final class NumberSetting extends Setting<Double> {
   private final double min;
   private final double max;
   private final double step;
   private final String suffix;

   public NumberSetting(String name, double defaultValue, double min, double max, double step, String suffix) {
      super(name, defaultValue);
      this.min = min;
      this.max = max;
      this.step = step;
      this.suffix = suffix;
   }

   public String getDisplayValue() {
      return String.format(Locale.ROOT, "%.2f", this.getValue()) + this.suffix;
   }

   public double getProgress() {
      return this.max <= this.min ? 0.0 : (this.getValue() - this.min) / (this.max - this.min);
   }

   protected Double normalize(Double value) {
      double clamped = MathUtil.clamp(value, this.min, this.max);
      if (this.step <= 0.0) {
         return clamped;
      }

      double snapped = this.min + Math.round((clamped - this.min) / this.step) * this.step;
      return MathUtil.clamp(snapped, this.min, this.max);
   }

   protected JsonElement writeValue(Double value) {
      return new JsonPrimitive(value);
   }

   protected Double readValue(JsonElement element) {
      return element.getAsDouble();
   }

   @Generated
   public double getMin() {
      return this.min;
   }

   @Generated
   public double getMax() {
      return this.max;
   }

   @Generated
   public double getStep() {
      return this.step;
   }

   @Generated
   public String getSuffix() {
      return this.suffix;
   }
}
