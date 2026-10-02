package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12075;
import net.minecraft.class_243;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.particles.ProceduralParticleRenderer;
import org.ryzen.utils.render.particles.ProceduralParticleState;

@Environment(EnvType.CLIENT)
public final class WorldParticlesFeature extends Feature implements MinecraftContext {
   private static final double SPAWN_PI = 3.1415928936382223;
   private static final int WORLD_BATCH_SIZE = 10;
   public final ModeSetting shape = this.register(
      new ModeSetting("Shape", ProceduralParticleRenderer.Shape.RANDOM.displayName(), ProceduralParticleRenderer.Shape.displayNames())
         .configKey("render.particles.texture")
   );
   public final NumberSetting spawnRate = this.register(new NumberSetting("Spawn Rate", 1.0, 1.0, 5.0, 1.0, " ticks").configKey("render.particles.spawnrate"));
   public final NumberSetting radius = this.register(new NumberSetting("Spawn Radius", 40.0, 10.0, 40.0, 1.0, " blocks").configKey("render.particles.radius"));
   public final NumberSetting lifetime = this.register(new NumberSetting("Lifetime", 60.0, 10.0, 200.0, 1.0, " ticks").configKey("render.particles.lifetime"));
   public final NumberSetting size = this.register(new NumberSetting("Size", 0.15, 0.05, 0.5, 0.05, " blocks").configKey("render.particles.size"));
   public final NumberSetting maxParticles = this.register(new NumberSetting("Max Particles", 100.0, 10.0, 200.0, 1.0, "").configKey("render.particles.max"));
   public final ColorSetting color = this.register(new ColorSetting("Color", -1).configKey("render.particles.color"));
   public final BooleanSetting natural = this.register(new BooleanSetting("Themed Colors", false).configKey("render.particles.natural"));
   public final NumberSetting glow = this.register(new NumberSetting("Glow", 0.0, 0.0, 1.0, 0.05, "").configKey("render.particles.glow"));
   private final List<ProceduralParticleState> particles = new ArrayList<>();
   private final List<ProceduralParticleRenderer.Sprite> sprites = new ArrayList<>();
   private final ProceduralParticleRenderer renderer = new ProceduralParticleRenderer();
   private final Random random = new Random();
   private int tickCounter;

   public WorldParticlesFeature() {
      super("WorldParticles", "Spawns procedural particles around the player", FeatureCategory.VISUAL, -1);
   }

   public static WorldParticlesFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(WorldParticlesFeature.class);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.tickCounter++;
         int interval = this.spawnRate.getValue().intValue();
         if (interval <= 1 || this.tickCounter % interval == 0) {
            this.spawnWorldBatch();
         }

         if (!this.particles.isEmpty()) {
            float configuredSize = this.size.getValue().floatValue();
            Iterator<ProceduralParticleState> iterator = this.particles.iterator();

            while (iterator.hasNext()) {
               if (!iterator.next().update(mc.field_1687, configuredSize, 1.0F, this.random)) {
                  iterator.remove();
               }
            }
         }
      } else {
         this.clear();
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.clear();
   }

   @Override
   protected void onDisable() {
      this.clear();
      this.renderer.release();
   }

   private void clear() {
      this.particles.clear();
      this.sprites.clear();
      this.tickCounter = 0;
   }

   public void renderWorld(class_12075 cameraState, float tickDelta) {
      if (mc.field_1724 != null && mc.field_1687 != null && !this.particles.isEmpty()) {
         int baseColor = this.color.getValue();
         float configuredSize = this.size.getValue().floatValue();
         this.sprites.clear();

         for (ProceduralParticleState particle : this.particles) {
            this.sprites
               .add(
                  new ProceduralParticleRenderer.Sprite(
                     particle.interpolatedPosition(tickDelta),
                     configuredSize,
                     ColorUtil.multiplyAlpha(baseColor, particle.opacityEnvelope()),
                     particle.shape(),
                     particle.renderRotation(tickDelta),
                     particle.normalizedLife(),
                     particle.seed(),
                     particle.phase()
                  )
               );
         }

         this.renderer.render(this.sprites, this.glow.getValue().floatValue(), this.natural.getValue());
      }
   }

   private void spawnWorldBatch() {
      int maximum = this.maxParticles.getValue().intValue();
      if (this.particles.size() < maximum) {
         class_243 center = mc.field_1724.method_73189();
         double configuredRadius = this.radius.getValue();
         int configuredLifetime = this.lifetime.getValue().intValue();

         for (int index = 0; index < 10 && this.particles.size() < maximum; index++) {
            double angle = this.random.nextDouble() * 3.1415928936382223 * 2.0;
            double distance = this.random.nextDouble() * configuredRadius;
            class_243 position = center.method_1031(Math.cos(angle) * distance, (this.random.nextDouble() - 0.5) * distance, Math.sin(angle) * distance);
            if (!ProceduralParticleState.isPositionBlocked(mc.field_1687, position)) {
               class_243 velocity = ProceduralParticleState.randomVelocity(this.random);
               this.particles
                  .add(
                     new ProceduralParticleState(
                        position.field_1352,
                        position.field_1351,
                        position.field_1350,
                        velocity.field_1352,
                        velocity.field_1351,
                        velocity.field_1350,
                        configuredLifetime,
                        this.selectedShape(),
                        this.random
                     )
                  );
            }
         }
      }
   }

   private ProceduralParticleRenderer.Shape selectedShape() {
      return ProceduralParticleRenderer.Shape.fromDisplayName(this.shape.getValue()).resolve(this.random);
   }
}
