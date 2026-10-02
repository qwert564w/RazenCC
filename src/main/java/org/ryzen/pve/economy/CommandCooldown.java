package org.ryzen.pve.economy;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class CommandCooldown {
   private long nextTick;

   public boolean ready(long currentTick) {
      return currentTick >= this.nextTick;
   }

   public boolean tryAcquire(long currentTick, long cooldownTicks) {
      if (!this.ready(currentTick)) {
         return false;
      }

      this.nextTick = saturatedAdd(currentTick, Math.max(1L, cooldownTicks));
      return true;
   }

   public void defer(long currentTick, long delayTicks) {
      this.nextTick = Math.max(this.nextTick, saturatedAdd(currentTick, Math.max(1L, delayTicks)));
   }

   public void reset() {
      this.nextTick = 0L;
   }

   public long nextTick() {
      return this.nextTick;
   }

   private static long saturatedAdd(long left, long right) {
      return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
   }
}
