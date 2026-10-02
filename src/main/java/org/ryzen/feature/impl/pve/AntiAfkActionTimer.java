package org.ryzen.feature.impl.pve;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class AntiAfkActionTimer {
   private long elapsedTicks;

   void advance() {
      if (this.elapsedTicks < Long.MAX_VALUE) {
         this.elapsedTicks++;
      }
   }

   boolean isDue(long intervalTicks) {
      return this.elapsedTicks >= Math.max(1L, intervalTicks);
   }

   void actionPerformed() {
      this.reset();
   }

   void reset() {
      this.elapsedTicks = 0L;
   }

   long elapsedTicks() {
      return this.elapsedTicks;
   }

   static long secondsToTicks(double seconds) {
      return !Double.isFinite(seconds) ? 1L : Math.max(1L, Math.round(seconds * 20.0));
   }
}
