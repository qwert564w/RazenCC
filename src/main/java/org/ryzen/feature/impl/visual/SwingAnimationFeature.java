package org.ryzen.feature.impl.visual;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class SwingAnimationFeature extends Feature {
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Slice", "Slice", "Spiral", "Thrust", "Spear"));
   public final NumberSetting speed = this.register(new NumberSetting("Speed", 6.0, 1.0, 20.0, 1.0, ""));

   public SwingAnimationFeature() {
      super("SwingAnimation", "Custom hand swing animation", FeatureCategory.VISUAL, -1);
   }

   public SwingAnimationFeature.Style style() {
      return SwingAnimationFeature.Style.valueOf(this.mode.getValue().toUpperCase(Locale.ROOT));
   }

   public int swingDurationTicks() {
      return this.speed.getValue().intValue();
   }

   @Environment(EnvType.CLIENT)
   public enum Style {
      SLICE,
      SPIRAL,
      THRUST,
      SPEAR;
   }
}
