package org.ryzen.feature.impl.visual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_3532;
import net.minecraft.class_7285;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorMode;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.math.Animation;

@Environment(EnvType.CLIENT)
public final class WorldTweaksFeature extends Feature {
   public static final String SKY_DEEP_SPACE = "Deep Space";
   public static final String SKY_NEBULA = "Nebula";
   public static final String SKY_PLASMA = "Plasma";
   private static final long DAY_LENGTH = 24000L;
   private static final long TIME_TRANSITION_MILLIS = 5000L;
   public final BooleanSetting changeTime = this.register(new BooleanSetting("Change Time", false).configKey("render.worldtweaks.changetime"));
   public final ModeSetting timeOfDay = this.register(
      new ModeSetting("Time of Day", "Day", "Day", "Night", "Sunset", "Midnight", "Sunrise")
         .configKey("render.worldtweaks.timeofday")
         .visibleWhen(() -> this.changeTime.getValue())
   );
   public final BooleanSetting changeFog = this.register(new BooleanSetting("Change Fog", false).configKey("render.worldtweaks.fogcolor"));
   public final NumberSetting fogDistance = this.register(
      new NumberSetting("Fog Distance", 90.0, 10.0, 200.0, 1.0, " blocks")
         .configKey("render.worldtweaks.fogcolor.distance")
         .visibleWhen(() -> this.changeFog.getValue())
   );
   public final ModeSetting fogColorMode = this.register(
      ColorMode.setting().configKey("render.worldtweaks.fogcolor.colorMode").visibleWhen(() -> this.changeFog.getValue())
   );
   public final ColorSetting fogColor = this.register(
      new ColorSetting("Fog Color", -9538333)
         .configKey("render.worldtweaks.fogcolor.color")
         .visibleWhen(() -> this.changeFog.getValue() && ColorMode.isCustom(this.fogColorMode))
   );
   public final BooleanSetting changeSky = this.register(new BooleanSetting("Change Sky", false).configKey("render.worldtweaks.sky"));
   public final ModeSetting skyEffect = this.register(
      new ModeSetting("Effect", "Deep Space", "Deep Space", "Nebula", "Plasma")
         .configKey("render.worldtweaks.sky.effect")
         .visibleWhen(() -> this.changeSky.getValue())
   );
   public final ColorSetting skyColor1 = this.register(
      new ColorSetting("Primary Color", -8812853).configKey("render.worldtweaks.sky.color1").visibleWhen(() -> this.changeSky.getValue())
   );
   public final ColorSetting skyColor2 = this.register(
      new ColorSetting("Secondary Color", -8812853).configKey("render.worldtweaks.sky.color2").visibleWhen(() -> this.changeSky.getValue())
   );
   public final NumberSetting skySpeed = this.register(
      new NumberSetting("Speed", 4.0, 0.1, 8.0, 0.1, "x").configKey("render.worldtweaks.sky.speed").visibleWhen(() -> this.changeSky.getValue())
   );
   public final NumberSetting skyIntensity = this.register(
      new NumberSetting("Intensity", 3.0, 0.1, 5.0, 0.1, "x").configKey("render.worldtweaks.sky.intensity").visibleWhen(() -> this.changeSky.getValue())
   );
   public final BooleanSetting changeSaturation = this.register(new BooleanSetting("Change Saturation", false).configKey("render.worldtweaks.saturation"));
   public final NumberSetting saturationAmount = this.register(
      new NumberSetting("Saturation", 0.5, -1.0, 2.0, 0.05, "")
         .configKey("render.worldtweaks.saturation.amount")
         .visibleWhen(() -> this.changeSaturation.getValue())
   );
   public final BooleanSetting changeWorldColor = this.register(
      new BooleanSetting("Change World Color", false).configKey("render.worldtweaks.changeworldcolor")
   );
   public final ModeSetting worldColorMode = this.register(
      ColorMode.setting().configKey("render.worldtweaks.changeworldcolor.mode").visibleWhen(() -> this.changeWorldColor.getValue())
   );
   public final ColorSetting worldColor = this.register(
      new ColorSetting("World Color", -1)
         .configKey("render.worldtweaks.changeworldcolor.color")
         .visibleWhen(() -> this.changeWorldColor.getValue() && ColorMode.isCustom(this.worldColorMode))
   );
   private final Animation timeAnimation = new Animation(0L, Animation.Easing.EASE_OUT_CUBIC);
   private String lastSelectedTime;

   public WorldTweaksFeature() {
      super("WorldTweaks", "Time, fog, sky, saturation, and world lighting", FeatureCategory.VISUAL, -1);
      this.resetTimeAnimation();
   }

   public static WorldTweaksFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(WorldTweaksFeature.class);
   }

   @Override
   protected void onEnable() {
      this.resetTimeAnimation();
   }

   public long getCustomTime() {
      String selected = this.timeOfDay.getValue();
      if (!selected.equals(this.lastSelectedTime)) {
         float current = this.timeAnimation.getValue();
         float target = targetTime(selected);
         this.timeAnimation.animate(current, current + shortestDifference(current, target), 5000L, Animation.Easing.EASE_OUT_CUBIC);
         this.lastSelectedTime = selected;
      }

      return normalizeTime(this.timeAnimation.getValue());
   }

   public void applyFog(class_7285 fog) {
      float end = this.fogDistance.getValue().floatValue();
      float span = class_3532.method_15363(end / 10.0F, 4.0F, 64.0F);
      float start = Math.max(0.0F, end - span);
      fog.field_60582 = start;
      fog.field_60584 = end;
      fog.field_60583 = start;
      fog.field_60585 = end;
      fog.field_60099 = end;
      fog.field_60100 = end;
   }

   public int resolvedFogColor() {
      return ColorMode.resolve(this.fogColorMode, this.fogColor);
   }

   public boolean usesSky() {
      return this.changeSky.getValue();
   }

   public boolean usesSaturation() {
      return this.changeSaturation.getValue() && Math.abs(this.saturationAmount.getValue()) > 1.0E-4;
   }

   public boolean usesWorldColor() {
      return this.changeWorldColor.getValue();
   }

   public int resolvedWorldColor() {
      return ColorMode.resolve(this.worldColorMode, this.worldColor);
   }

   private void resetTimeAnimation() {
      float target = targetTime(this.timeOfDay.getValue());
      this.timeAnimation.animate(target, target, 0L, Animation.Easing.EASE_OUT_CUBIC);
      this.lastSelectedTime = this.timeOfDay.getValue();
   }

   private static float targetTime(String time) {
      return switch (time) {
         case "Night" -> 13000.0F;
         case "Sunset" -> 12000.0F;
         case "Midnight" -> 18000.0F;
         case "Sunrise" -> 23000.0F;
         default -> 1000.0F;
      };
   }

   private static float shortestDifference(float current, float target) {
      double difference = (target - current % 24000.0F + 36000.0) % 24000.0 - 12000.0;
      return (float)difference;
   }

   private static long normalizeTime(float time) {
      long normalized = (long)time % 24000L;
      return normalized < 0L ? normalized + 24000L : normalized;
   }
}
