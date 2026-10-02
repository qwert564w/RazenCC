package org.ryzen.feature.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.FeatureToggleEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class ToggleSoundsFeature extends Feature {
   private static final String TYPE_1 = "Type 1";
   private static final String TYPE_2 = "Type 2";
   private static final String TYPE_3 = "Type 3";
   private static final String TYPE_4 = "Type 4";
   public final ModeSetting type = this.register(new ModeSetting("Sound", "Type 1", "Type 1", "Type 2", "Type 3", "Type 4"));
   public final NumberSetting volume = this.register(new NumberSetting("Volume", 75.0, 0.0, 100.0, 1.0, "%"));

   public ToggleSoundsFeature() {
      super("ToggleSounds", "Plays a sound when a feature is toggled", FeatureCategory.MISC, -1);
   }

   @EventTarget
   public void onFeatureToggle(FeatureToggleEvent event) {
      if (event.getFeature() != this) {
         float volumeScale = this.volume.getValue().floatValue() / 100.0F;
         if (!(volumeScale <= 0.0F)) {
            boolean on = event.isEnabled();

            class_3414 sound = switch ((String)this.type.getValue()) {
               case "Type 2" -> (class_3414)class_3417.field_14622.comp_349();
               case "Type 3" -> (class_3414)class_3417.field_15015.comp_349();
               case "Type 4" -> class_3417.field_26980;
               default -> (class_3414)class_3417.field_15204.comp_349();
            };
            float pitch = on ? 1.4F : 0.8F;
            class_310 client = class_310.method_1551();
            client.method_1483().method_4873(class_1109.method_4757(sound, pitch, volumeScale));
         }
      }
   }
}
