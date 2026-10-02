package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
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
public final class HitParticlesFeature extends Feature implements MinecraftContext {
   private static final int HARD_PARTICLE_CAP = 450;
   public final ModeSetting shape = this.register(
      new ModeSetting("Shape", ProceduralParticleRenderer.Shape.RANDOM.displayName(), ProceduralParticleRenderer.Shape.displayNames())
         .configKey("render.particles.texture")
   );
   public final NumberSetting amount = this.register(new NumberSetting("Hit Amount", 15.0, 1.0, 50.0, 1.0, "").configKey("render.particles.hitamount"));
   public final NumberSetting lifetime = this.register(new NumberSetting("Lifetime", 60.0, 10.0, 200.0, 1.0, " ticks").configKey("render.particles.lifetime"));
   public final NumberSetting size = this.register(new NumberSetting("Size", 0.15, 0.05, 0.5, 0.05, " blocks").configKey("render.particles.size"));
   public final ColorSetting color = this.register(new ColorSetting("Color", -1).configKey("render.particles.color"));
   public final BooleanSetting natural = this.register(new BooleanSetting("Themed Colors", false).configKey("render.particles.natural"));
   public final NumberSetting glow = this.register(new NumberSetting("Glow", 0.0, 0.0, 1.0, 0.05, "").configKey("render.particles.glow"));
   private final List<ProceduralParticleState> particles = new ArrayList<>();
   private final List<ProceduralParticleRenderer.Sprite> sprites = new ArrayList<>();
   private final ProceduralParticleRenderer renderer = new ProceduralParticleRenderer();
   private final Random random = new Random();

   public HitParticlesFeature() {
      super("HitParticles", "Spawns procedural particles when you attack an entity", FeatureCategory.VISUAL, -1);
   }

   public static HitParticlesFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(HitParticlesFeature.class);
   }

   public void onAttack(class_1297 target) {
      if (target instanceof class_1309 living) {
         this.spawnHit(living);
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (mc.field_1687 == null) {
         this.clear();
      } else if (!this.particles.isEmpty()) {
         float configuredSize = this.size.getValue().floatValue();
         Iterator<ProceduralParticleState> iterator = this.particles.iterator();

         while (iterator.hasNext()) {
            if (!iterator.next().update(mc.field_1687, configuredSize, 1.0F, this.random)) {
               iterator.remove();
            }
         }
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
   }

   public void renderWorld(float tickDelta) {
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

   private void spawnHit(class_1309 entity) {
      int count = Math.min(this.amount.getValue().intValue(), 450 - this.particles.size());
      if (count > 0) {
         class_243 base = entity.method_73189();
         int configuredLifetime = this.lifetime.getValue().intValue();

         for (int index = 0; index < count; index++) {
            class_243 position = base.method_1031(
               (this.random.nextDouble() - 0.5) * entity.method_17681(),
               this.random.nextDouble() * entity.method_17682(),
               (this.random.nextDouble() - 0.5) * entity.method_17681()
            );
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

   private ProceduralParticleRenderer.Shape selectedShape() {
      return ProceduralParticleRenderer.Shape.fromDisplayName(this.shape.getValue()).resolve(this.random);
   }
}
