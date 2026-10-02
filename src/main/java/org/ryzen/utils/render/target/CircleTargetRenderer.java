package org.ryzen.utils.render.target;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.render.HurtUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.particles.WorldParticleRenderer;

@Environment(EnvType.CLIENT)
public final class CircleTargetRenderer {
   private static final float PERIOD_MILLIS = 1500.0F;
   private static final int ARMS = 4;
   private static final int PARTICLES_PER_ARM = 15;
   private final Animation appear = new Animation(650L, Animation.Easing.EASE_OUT_EXPO);
   private final Animation inner = new Animation(850L, Animation.Easing.EASE_IN_OUT_QUAD);
   private final List<WorldParticleRenderer.Sprite> sprites = new ArrayList<>(60);
   private final WorldParticleRenderer renderer = new WorldParticleRenderer();
   private final TargetDeathDissolve deathDissolve = new TargetDeathDissolve();
   private class_1309 lastTarget;
   private int dissolvedTargetId = Integer.MIN_VALUE;
   private boolean animationTarget;
   private long lastFrameNanos;
   private float clock;
   private float hurtPhase;

   public void render(class_1309 activeTarget, float tickDelta, int color) {
      long nowNanos = System.nanoTime();
      long nowMillis = System.currentTimeMillis();
      float delta = this.lastFrameNanos == 0L ? 0.016F : Math.min(0.1F, (float)(nowNanos - this.lastFrameNanos) / 1.0E9F);
      this.lastFrameNanos = nowNanos;
      boolean present = valid(activeTarget);
      if (present) {
         this.lastTarget = activeTarget;
         if (activeTarget.method_5628() == this.dissolvedTargetId) {
            this.dissolvedTargetId = Integer.MIN_VALUE;
         }
      }

      if (dead(this.lastTarget) && this.lastTarget.method_5628() != this.dissolvedTargetId) {
         this.deathDissolve.burst(this.sprites, this.lastTarget, color, nowMillis);
         this.dissolvedTargetId = this.lastTarget.method_5628();
         present = false;
      }

      this.updateAnimations(present);
      float appearValue = this.appear.getValue();
      float innerValue = this.inner.getValue();
      if (this.lastTarget != null && !(appearValue <= 0.01F) && !(innerValue <= 0.001F)) {
         this.clock += delta * 1000.0F;
         float hurt = this.lastTarget.field_6235 > 0 ? Math.max(0.0F, (this.lastTarget.field_6235 - tickDelta) / 10.0F) : 0.0F;
         this.hurtPhase += hurt * delta * 500.0F;
         class_243 base = Render3DUtil.interpolatedPosition(this.lastTarget, tickDelta);
         float height = this.lastTarget.method_17682();
         float cycle = this.clock % 1500.0F / 1500.0F;
         float progress = 0.5F + 0.5F * (float)Math.cos(cycle * Math.PI * 2.0);
         double movingY = base.field_1351 + height * progress;
         float movingAngle = this.clock / 2.5F;
         float baseAlpha = Math.min(0.8F, innerValue * 1.2F);
         int animatedColor = HurtUtil.blend(color, this.lastTarget, 1.0F);
         this.sprites.clear();

         for (int arm = 0; arm < 4; arm++) {
            for (int particle = 0; particle < 15; particle++) {
               float particleProgress = particle / 14.0F;
               int index = arm * 15 + particle;
               float assembly = TargetEffectMotion.assembly(innerValue, index, 60);
               float worldSize = 0.5F * innerValue * (0.6F + assembly * 0.4F);
               float angle = 0.2F * (movingAngle + this.hurtPhase - particle * 3.5F) / 15.0F;
               float triangle = particleProgress < 0.5F ? particleProgress * 2.0F : (1.0F - particleProgress) * 2.0F;
               double amplitude = Math.sin(triangle * Math.PI) * 2.0;
               Random random = new Random(particle * 12345L);
               double offsetX = (random.nextDouble() - 0.5) * amplitude;
               double offsetY = (random.nextDouble() - 0.5) * amplitude;
               double offsetZ = (random.nextDouble() - 0.5) * amplitude;
               double animatedOffsetX = offsetX * assembly - offsetX;
               double animatedOffsetY = offsetY * assembly - offsetY;
               double animatedOffsetZ = offsetZ * assembly - offsetZ;
               double radius = 0.7;
               double localX;
               double localZ;
               switch (arm) {
                  case 0:
                     localX = Math.cos(angle) * radius + animatedOffsetX;
                     localZ = Math.sin(angle) * radius + animatedOffsetZ;
                     break;
                  case 1:
                     localX = -Math.sin(angle) * radius + animatedOffsetX;
                     localZ = Math.cos(angle) * radius + animatedOffsetZ;
                     break;
                  case 2:
                     localX = -Math.cos(angle) * radius + animatedOffsetX;
                     localZ = -Math.sin(angle) * radius + animatedOffsetZ;
                     break;
                  default:
                     localX = Math.sin(angle) * radius + animatedOffsetX;
                     localZ = -Math.cos(angle) * radius + animatedOffsetZ;
               }

               float tail = (float)Math.pow(1.0F - particleProgress, 1.3);
               float alpha = class_3532.method_15363(baseAlpha * tail * appearValue, 0.0F, 1.0F);
               if (!(alpha <= 0.004F)) {
                  this.sprites
                     .add(
                        new WorldParticleRenderer.Sprite(
                           new class_243(
                              base.field_1352 + localX + TargetEffectMotion.scatter(index, 0, 1.4, assembly),
                              movingY + animatedOffsetY + TargetEffectMotion.scatter(index, 1, 1.15, assembly) + (1.0F - assembly) * 0.25F,
                              base.field_1350 + localZ + TargetEffectMotion.scatter(index, 2, 1.4, assembly)
                           ),
                           worldSize * 0.5F,
                           ColorUtil.multiplyAlpha(animatedColor, alpha)
                        )
                     );
               }
            }
         }

         this.renderer.render(this.sprites, 1.0F, true);
         this.deathDissolve.render(delta, nowMillis);
      } else {
         if (!present && appearValue <= 0.01F) {
            this.lastTarget = null;
            this.sprites.clear();
         }

         this.deathDissolve.render(delta, nowMillis);
      }
   }

   private void updateAnimations(boolean present) {
      if (present != this.animationTarget) {
         this.animationTarget = present;
         float target = present ? 1.0F : 0.0F;
         this.appear.animate(this.appear.getValue(), target, 650L, Animation.Easing.EASE_OUT_EXPO);
         this.inner.animate(this.inner.getValue(), target, 850L, Animation.Easing.EASE_IN_OUT_QUAD);
      }
   }

   private static boolean valid(class_1309 entity) {
      return entity != null && entity.method_5805() && !entity.method_31481();
   }

   private static boolean dead(class_1309 entity) {
      return entity != null && (entity.method_29504() || entity.field_6213 > 0 || entity.method_6032() <= 0.0F);
   }
}
