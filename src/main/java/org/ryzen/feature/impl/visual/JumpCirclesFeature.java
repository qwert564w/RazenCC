package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.PlayerJumpEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorMode;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.render.jump.JumpCircleRenderer;
import org.ryzen.utils.render.jump.JumpGlowRenderer;
import org.ryzen.utils.render.jump.JumpWaveRenderer;

@Environment(EnvType.CLIENT)
public final class JumpCirclesFeature extends Feature implements MinecraftContext {
   public final NumberSetting lifetime = this.register(new NumberSetting("Lifetime", 800.0, 200.0, 3000.0, 10.0, " ms"));
   public final NumberSetting radius = this.register(new NumberSetting("Radius", 1.5, 0.5, 5.0, 0.1, " blocks"));
   public final BooleanSetting waveEffect = this.register(new BooleanSetting("Wave Effect", false));
   public final NumberSetting waveStrength = this.register(new NumberSetting("Wave Strength", 0.6, 0.0, 2.0, 0.05, ""));
   public final NumberSetting waveRadius = this.register(new NumberSetting("Wave Radius", 3.0, 0.5, 10.0, 0.1, " blocks"));
   public final NumberSetting waveTime = this.register(new NumberSetting("Wave Time", 600.0, 150.0, 2500.0, 10.0, " ms"));
   public final BooleanSetting glow = this.register(new BooleanSetting("Glow", false));
   public final NumberSetting distortionIntensity = this.register(new NumberSetting("Distortion", 1.0, 0.0, 3.0, 0.05, "x"));
   public final ModeSetting colorMode = this.register(ColorMode.setting());
   public final ColorSetting color = this.register(new ColorSetting("Color", 7824895).visibleWhen(() -> ColorMode.isCustom(this.colorMode)));
   private final List<JumpCirclesFeature.Circle> circles = new ArrayList<>();
   private final List<JumpCirclesFeature.Wave> waves = new ArrayList<>();
   private final JumpCircleRenderer circleRenderer = new JumpCircleRenderer();
   private final JumpWaveRenderer waveRenderer = new JumpWaveRenderer();
   private final JumpGlowRenderer glowRenderer = new JumpGlowRenderer();

   public JumpCirclesFeature() {
      super("JumpCircles", "Spawns expanding rings at jump positions", FeatureCategory.VISUAL, -1);
   }

   public static JumpCirclesFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(JumpCirclesFeature.class);
   }

   @EventTarget
   public void onJump(PlayerJumpEvent event) {
      this.circles.add(new JumpCirclesFeature.Circle(event.getPosition(), System.currentTimeMillis(), this.lifetime.getValue().floatValue()));
      if (this.waveEffect.getValue()) {
         this.waves
            .add(
               new JumpCirclesFeature.Wave(
                  event.getPosition(),
                  System.currentTimeMillis(),
                  this.waveTime.getValue().floatValue(),
                  (float)(this.waveStrength.getValue() * this.distortionIntensity.getValue()),
                  this.waveRadius.getValue().floatValue()
               )
            );
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.clear();
   }

   public void renderWorld() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         long now = System.currentTimeMillis();
         float time = (float)(now % 1000000L) / 1000.0F;
         boolean glowEnabled = this.glow.getValue();
         int fixedColor = ColorMode.resolve(this.colorMode, this.color);
         Iterator<JumpCirclesFeature.Circle> iterator = this.circles.iterator();

         while (iterator.hasNext()) {
            JumpCirclesFeature.Circle circle = iterator.next();
            if (circle.finished(now)) {
               iterator.remove();
            } else {
               float rawProgress = circle.rawProgress(now);
               float progress = easeOutCubic(rawProgress);
               float fade = 1.0F - rawProgress;
               this.circleRenderer.render(circle.position(), this.radius.getValue().floatValue() * progress, fade, time, fixedColor, glowEnabled);
            }
         }

         Iterator<JumpCirclesFeature.Wave> waveIterator = this.waves.iterator();

         while (waveIterator.hasNext()) {
            JumpCirclesFeature.Wave wave = waveIterator.next();
            if (wave.finished(now)) {
               waveIterator.remove();
            } else {
               float waveRaw = wave.rawProgress(now);
               float ringProgress = easeOutCubic(waveRaw);
               float waveFade = fadeEnvelope(waveRaw);
               this.waveRenderer.render(wave.position(), wave.maxRadius(), ringProgress, wave.strength(), waveFade, fixedColor);
               if (glowEnabled) {
                  this.glowRenderer.render(wave.position(), wave.maxRadius(), ringProgress, fixedColor, waveFade, time);
               }
            }
         }
      }
   }

   @Override
   protected void onDisable() {
      this.clear();
      this.waveRenderer.release();
      this.glowRenderer.release();
   }

   private void clear() {
      this.circles.clear();
      this.waves.clear();
   }

   private static float fadeEnvelope(float raw) {
      float clamped = Math.clamp(raw, 0.0F, 1.0F);
      float appear = smoothStep(clamped / 0.18F);
      float disappear = smoothStep((1.0F - clamped) / 0.82F);
      return Math.clamp(appear * disappear, 0.0F, 1.0F);
   }

   private static float smoothStep(float value) {
      float t = Math.clamp(value, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   private static float easeOutCubic(float value) {
      float inverse = 1.0F - Math.clamp(value, 0.0F, 1.0F);
      return 1.0F - inverse * inverse * inverse;
   }

   @Environment(EnvType.CLIENT)
   private record Circle(class_243 position, long createdAt, float lifetimeMs) {
      private float rawProgress(long now) {
         return Math.clamp((float)(now - this.createdAt) / this.lifetimeMs, 0.0F, 1.0F);
      }

      private boolean finished(long now) {
         return (float)(now - this.createdAt) >= this.lifetimeMs;
      }
   }

   @Environment(EnvType.CLIENT)
   private record Wave(class_243 position, long createdAt, float lifetimeMs, float strength, float maxRadius) {
      private float rawProgress(long now) {
         return Math.clamp((float)(now - this.createdAt) / this.lifetimeMs, 0.0F, 1.0F);
      }

      private boolean finished(long now) {
         return (float)(now - this.createdAt) >= this.lifetimeMs;
      }
   }
}
