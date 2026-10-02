package org.ryzen.feature.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_638;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.render.world.DynamicLightManager;

@Environment(EnvType.CLIENT)
public final class FullBrightFeature extends Feature {
   public static final String LIGHT_SHADER = "Shader";
   public static final String LIGHT_ENGINE = "True Light";
   public static final String LIGHT_BOTH = "Both";
   public final BooleanSetting dynamic = this.register(new BooleanSetting("Dynamic", true).configKey("render.fullbright.dynamic"));
   public final NumberSetting brightness = this.register(
      new NumberSetting("Brightness", 0.5, 0.0, 10.0, 0.1, "").configKey("render.fullbright.brightness").visibleWhen(() -> !this.dynamic.getValue())
   );
   public final NumberSetting minBrightness = this.register(
      new NumberSetting("Min Brightness", 0.0, 0.0, 10.0, 0.1, "").configKey("render.fullbright.minBrightness").visibleWhen(() -> this.dynamic.getValue())
   );
   public final NumberSetting maxBrightness = this.register(
      new NumberSetting("Max Brightness", 10.0, 0.0, 10.0, 0.1, "").configKey("render.fullbright.maxBrightness").visibleWhen(() -> this.dynamic.getValue())
   );
   public final ModeSetting lightsStyle = this.register(
      new ModeSetting("Light Style", "Both", "Shader", "True Light", "Both").configKey("render.fullbright.lightsStyle")
   );
   public final NumberSetting lightIntensity = this.register(
      new NumberSetting("Light Intensity", 1.0, 0.0, 3.0, 0.05, "x").configKey("render.fullbright.lightIntensity")
   );
   public final NumberSetting lightRadius = this.register(
      new NumberSetting("Light Radius", 1.0, 0.25, 3.0, 0.05, "x").configKey("render.fullbright.lightRadius")
   );
   public final BooleanSetting lightOthers = this.register(new BooleanSetting("Light From Others", true).configKey("render.fullbright.lightOthers"));
   public final BooleanSetting lightItems = this.register(new BooleanSetting("Light From Items", true).configKey("render.fullbright.lightItems"));
   private double smoothedGamma = Double.NaN;

   public FullBrightFeature() {
      super("FullBright", "Adaptive brightness and dynamic item lights", FeatureCategory.PLAYER, -1);
   }

   public static FullBrightFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(FullBrightFeature.class);
   }

   public static double modifyGamma(double vanillaGamma) {
      FullBrightFeature feature = getEnabled();
      return feature != null && Double.isFinite(feature.smoothedGamma) ? Math.max(vanillaGamma, feature.smoothedGamma) : vanillaGamma;
   }

   public static boolean shouldSuppressDarkness() {
      return getEnabled() != null;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 minecraft = event.getClient();
      class_746 player = minecraft.field_1724;
      class_638 level = minecraft.field_1687;
      if (player != null && level != null) {
         double target = this.dynamic.getValue() ? this.adaptiveGamma(level, player) : this.brightness.getValue();
         double vanilla = (Double)minecraft.field_1690.method_42473().method_41753();
         target = Math.max(vanilla, target);
         if (!Double.isFinite(this.smoothedGamma)) {
            this.smoothedGamma = target;
         } else {
            this.smoothedGamma = this.smoothedGamma + (target - this.smoothedGamma) * 0.18;
         }

         DynamicLightManager.INSTANCE.tick(minecraft, this);
      } else {
         this.smoothedGamma = Double.NaN;
         DynamicLightManager.INSTANCE.clear();
      }
   }

   @Override
   protected void onEnable() {
      this.smoothedGamma = Double.NaN;
      DynamicLightManager.INSTANCE.clear();
   }

   @Override
   protected void onDisable() {
      this.smoothedGamma = Double.NaN;
      DynamicLightManager.INSTANCE.clear();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.smoothedGamma = Double.NaN;
      DynamicLightManager.INSTANCE.clear();
   }

   public boolean usesShaderLights() {
      return this.lightsStyle.is("Shader") || this.lightsStyle.is("Both");
   }

   public boolean usesEngineLights() {
      return this.lightsStyle.is("True Light") || this.lightsStyle.is("Both");
   }

   private double adaptiveGamma(class_638 level, class_746 player) {
      float light = level.method_22339(player.method_24515()) / 15.0F;
      float darkness = 1.0F - class_3532.method_15363(light, 0.0F, 1.0F);
      float eased = darkness * darkness * (3.0F - 2.0F * darkness);
      double minimum = Math.min(this.minBrightness.getValue(), this.maxBrightness.getValue());
      double maximum = Math.max(this.minBrightness.getValue(), this.maxBrightness.getValue());
      return class_3532.method_16436(eased, minimum, maximum);
   }
}
