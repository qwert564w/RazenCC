package org.ryzen.menu.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.math.Animation;

@Environment(EnvType.CLIENT)
public final class AnimatedValue {
   private final long durationMs;
   private final Animation.Easing easing;
   private final Animation animation;
   private boolean initialized;
   private float target;

   public AnimatedValue(long durationMs, Animation.Easing easing) {
      this.durationMs = durationMs;
      this.easing = easing;
      this.animation = new Animation(durationMs, easing);
   }

   public float toward(float target) {
      if (!this.initialized) {
         this.initialized = true;
         this.target = target;
         this.animation.animate(target, target, 0L, this.easing);
         return target;
      }

      if (Math.abs(this.target - target) > 0.001F) {
         this.target = target;
         this.animation.animate(this.animation.getValue(), target, this.durationMs, this.easing);
      }

      return this.animation.getValue();
   }

   public float value() {
      return this.animation.getValue();
   }
}
