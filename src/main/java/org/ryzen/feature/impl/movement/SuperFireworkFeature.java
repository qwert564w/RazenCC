package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class SuperFireworkFeature extends Feature {
   private static final String MODE_REALLY_WORLD = "ReallyWorld";
   private static final String MODE_BRAVO = "Bravo";
   private static final String MODE_BRAVO_GRIEF = "BravoGrief";
   private static final String MODE_CUSTOM = "Custom";
   private static final float BAND_DEGREES = 5.0F;
   private static final float[] BRAVO_GRIEF_XZ = new float[]{1.67F, 1.68F, 1.69F, 1.72F, 1.72F, 1.73F, 1.74F, 1.75F, 1.76F};
   private static final float[] BRAVO_GRIEF_Y = new float[]{1.72F, 1.72F, 1.72F, 1.72F, 1.72F, 1.73F, 1.74F, 1.76F, 1.8F};
   private static final float[] REALLY_WORLD_XZ = new float[]{1.6F, 1.63F, 1.66F, 1.71F, 1.73F, 1.81F, 1.81F, 1.81F, 1.81F};
   private static final float[] REALLY_WORLD_Y = new float[]{1.6F, 1.62F, 1.6F, 1.62F, 1.64F, 1.68F, 1.75F, 1.9F, 2.16F};
   public final ModeSetting mode = this.register(new ModeSetting("Profile", "ReallyWorld", "ReallyWorld", "Bravo", "BravoGrief", "Custom"));
   private final NumberSetting[] horizontalBands = new NumberSetting[]{
      this.band("XZ 0-5°", 1.52),
      this.band("XZ 5-10°", 1.53),
      this.band("XZ 10-15°", 1.54),
      this.band("XZ 15-20°", 1.55),
      this.band("XZ 20-25°", 1.56),
      this.band("XZ 25-30°", 1.57),
      this.band("XZ 30-35°", 1.58),
      this.band("XZ 35-40°", 1.59),
      this.band("XZ 40-45°", 1.6)
   };
   private final NumberSetting[] verticalBands = new NumberSetting[]{
      this.band("Y 0-5°", 1.51),
      this.band("Y 5-10°", 1.52),
      this.band("Y 10-15°", 1.53),
      this.band("Y 15-20°", 1.54),
      this.band("Y 20-25°", 1.55),
      this.band("Y 25-30°", 1.56),
      this.band("Y 30-35°", 1.57),
      this.band("Y 35-40°", 1.58),
      this.band("Y 40-45°", 1.59)
   };

   public SuperFireworkFeature() {
      super("SuperFirework", "Custom firework boost strength for elytra flight", FeatureCategory.MOVEMENT, -1);
   }

   private NumberSetting band(String name, double preset) {
      return this.register(new NumberSetting(name, preset, 1.5, 3.0, 0.01, "x").visibleWhen(() -> this.mode.is("Custom")));
   }

   public static SuperFireworkFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(SuperFireworkFeature.class);
   }

   public class_243 boostedMovement(class_1309 rider) {
      class_243 look = rider.method_5720();
      class_243 delta = rider.method_18798();
      float yaw = class_3532.method_15393(rider.method_36454());
      float pitch = rider.method_36455();
      double vertical;
      double horizontal;
      switch ((String)this.mode.getValue()) {
         case "Custom":
            horizontal = this.horizontalBands[bandIndex(yaw)].getValue();
            vertical = this.verticalBands[bandIndex(pitch)].getValue();
            horizontal = Math.max(horizontal, vertical);
            break;
         case "Bravo":
            horizontal = bravoHorizontal(pitch, yaw);
            vertical = bravoVertical(pitch);
            break;
         case "BravoGrief":
            horizontal = BRAVO_GRIEF_XZ[bandIndex(yaw)];
            vertical = BRAVO_GRIEF_Y[bandIndex(pitch)];
            break;
         default:
            horizontal = REALLY_WORLD_XZ[bandIndex(yaw)];
            vertical = REALLY_WORLD_Y[bandIndex(pitch)];
            horizontal = Math.max(horizontal, vertical);
      }

      return delta.method_1031(
         look.field_1352 * 0.1 + (look.field_1352 * horizontal - delta.field_1352) * 0.5,
         look.field_1351 * 0.1 + (look.field_1351 * vertical - delta.field_1351) * 0.5,
         look.field_1350 * 0.1 + (look.field_1350 * horizontal - delta.field_1350) * 0.5
      );
   }

   private static int bandIndex(float angle) {
      float folded = Math.abs(angle);
      if (folded > 90.0F) {
         folded = 180.0F - folded;
      }

      if (folded > 45.0F) {
         folded = 90.0F - folded;
      }

      return class_3532.method_15340((int)(folded / 5.0F), 0, 8);
   }

   private static double bravoVertical(float pitch) {
      float value = Math.abs(pitch);
      if (value >= 37.0F && value <= 38.0F) {
         return 2.03;
      } else if (value >= 25.0F && value <= 30.0F) {
         return 2.0;
      } else if (value >= 35.0F && value <= 45.0F) {
         return 1.99;
      } else if (value >= 40.0F && value <= 50.0F) {
         return 1.97;
      } else if (value >= 50.0F && value <= 60.0F) {
         return 1.96;
      } else if (value >= 51.0F && value <= 61.0F) {
         return 1.85;
      } else {
         return value >= 52.0F && value <= 65.0F ? 1.8 : 1.59;
      }
   }

   private static double bravoHorizontal(float pitch, float yaw) {
      float absPitch = Math.abs(pitch);
      float quadrantYaw = Math.abs(class_3532.method_15393(yaw) % 90.0F);
      double value;
      if (between(absPitch, 38.0F, 52.0F)) {
         value = 2.0;
      } else if (between(absPitch, 32.0F, 58.0F)) {
         value = 1.96;
      } else if (between(absPitch, 28.0F, 62.0F)) {
         value = 1.95;
      } else if (between(quadrantYaw, 29.0F, 61.0F) || between(absPitch, 29.0F, 61.0F)) {
         value = 1.963;
      } else if (between(quadrantYaw, 28.0F, 60.0F) || between(absPitch, 28.0F, 60.0F)) {
         value = 1.954;
      } else if (between(quadrantYaw, 26.0F, 64.0F) || between(absPitch, 26.0F, 64.0F)) {
         value = 1.874;
      } else if (between(quadrantYaw, 24.0F, 66.0F) || between(absPitch, 24.0F, 66.0F)) {
         value = 1.75;
      } else if (between(quadrantYaw, 15.0F, 75.0F) || between(absPitch, 15.0F, 75.0F)) {
         value = 1.75;
      } else if (between(quadrantYaw, 13.0F, 77.0F) || between(absPitch, 13.0F, 77.0F)) {
         value = 1.75;
      } else if (between(quadrantYaw, 12.0F, 78.0F) || between(absPitch, 12.0F, 78.0F)) {
         value = 1.75;
      } else if (between(quadrantYaw, 8.0F, 82.0F) || between(absPitch, 11.0F, 79.0F)) {
         value = 1.75;
      } else if (between(quadrantYaw, 5.0F, 85.0F) || between(absPitch, 8.0F, 82.0F)) {
         value = 1.67;
      } else if (!(quadrantYaw <= 90.0F) && !(absPitch <= 90.0F)) {
         value = 1.66;
      } else {
         value = 1.67;
      }

      return pitch > 15.0F ? value - 0.068 : value;
   }

   private static boolean between(float value, float min, float max) {
      return value >= min && value <= max;
   }
}
