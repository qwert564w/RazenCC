package org.ryzen.utils.math;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_3532;

@Environment(EnvType.CLIENT)
public final class Animation {
   private long startNanos;
   private long durationMillis;
   private float fromValue;
   private float toValue;
   private boolean reverse;
   private Animation.Easing easing;

   public Animation(long duration, Animation.Easing easing) {
      this.setDuration(duration);
      this.setEasing(easing);
      this.reset();
   }

   public void reset() {
      this.startNanos = System.nanoTime();
   }

   public void animate(float from, float to, long duration, Animation.Easing easing) {
      this.fromValue = from;
      this.toValue = to;
      this.setDuration(duration);
      this.setEasing(easing);
      this.reverse = false;
      this.reset();
   }

   public float getValue() {
      float progress = this.progress();
      if (this.reverse) {
         progress = 1.0F - progress;
      }

      return class_3532.method_16439(this.easing.ease(progress), this.fromValue, this.toValue);
   }

   public boolean isFinished() {
      return this.elapsedMillis() >= this.durationMillis;
   }

   public void setReverse(boolean reverse) {
      if (this.reverse != reverse) {
         long elapsed = Math.min(this.elapsedMillis(), this.durationMillis);
         this.reverse = reverse;
         this.startNanos = System.nanoTime() - TimeUnit.MILLISECONDS.toNanos(this.durationMillis - elapsed);
      }
   }

   public void setDuration(long duration) {
      if (duration < 0L) {
         throw new IllegalArgumentException("Animation duration cannot be negative");
      }

      this.durationMillis = duration;
   }

   public void setEasing(Animation.Easing easing) {
      this.easing = Objects.requireNonNull(easing, "easing");
   }

   private float progress() {
      return this.durationMillis == 0L ? 1.0F : class_3532.method_15363((float)this.elapsedMillis() / (float)this.durationMillis, 0.0F, 1.0F);
   }

   private long elapsedMillis() {
      return TimeUnit.NANOSECONDS.toMillis(Math.max(0L, System.nanoTime() - this.startNanos));
   }

   @Environment(EnvType.CLIENT)
   public enum Easing {
      LINEAR {
         @Override
         public float ease(float x) {
            return x;
         }
      },
      EASE_IN_QUAD {
         @Override
         public float ease(float x) {
            return x * x;
         }
      },
      EASE_OUT_QUAD {
         @Override
         public float ease(float x) {
            return 1.0F - (1.0F - x) * (1.0F - x);
         }
      },
      EASE_OUT_CUBIC {
         @Override
         public float ease(float x) {
            float inverse = 1.0F - x;
            return 1.0F - inverse * inverse * inverse;
         }
      },
      EASE_OUT_EXPO {
         @Override
         public float ease(float x) {
            return x >= 1.0F ? 1.0F : 1.0F - (float)Math.pow(2.0, -10.0F * x);
         }
      },
      EASE_IN_OUT_QUAD {
         @Override
         public float ease(float x) {
            return x < 0.5F ? 2.0F * x * x : 1.0F - (float)Math.pow(-2.0F * x + 2.0F, 2.0) / 2.0F;
         }
      },
      EASE_OUT_BACK {
         @Override
         public float ease(float x) {
            float overshoot = 1.70158F;
            float shifted = x - 1.0F;
            return 1.0F + (overshoot + 1.0F) * shifted * shifted * shifted + overshoot * shifted * shifted;
         }
      },
      EASE_OUT_BOUNCE {
         @Override
         public float ease(float x) {
            float factor = 7.5625F;
            float divisor = 2.75F;
            if (x < 1.0F / divisor) {
               return factor * x * x;
            } else if (x < 2.0F / divisor) {
               float var6;
               return factor * (var6 = x - 1.5F / divisor) * var6 + 0.75F;
            } else {
               float var4;
               float var5;
               return x < 2.5F / divisor ? factor * (var4 = x - 2.25F / divisor) * var4 + 0.9375F : factor * (var5 = x - 2.625F / divisor) * var5 + 0.984375F;
            }
         }
      };

      public abstract float ease(float var1);
   }
}
