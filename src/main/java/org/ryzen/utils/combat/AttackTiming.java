package org.ryzen.utils.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1802;
import net.minecraft.class_3532;
import net.minecraft.class_746;

import java.util.Random;

@Environment(EnvType.CLIENT)
public final class AttackTiming {
   private static final int[] BASE_PATTERN = new int[]{10, 10, 10, 13};
   private static final float READY_CHARGE = 0.9F;
   private int extraDelayTicks;
   private int hitCounter;
   private final Random bioRandom = new Random();

   public void tick() {
      if (this.extraDelayTicks > 0) {
         this.extraDelayTicks--;
      }
   }

   public void onSwingPacket(boolean usePattern, boolean tpsSync) {
      float pattern;
      if (usePattern) {
         int base = BASE_PATTERN[this.hitCounter % BASE_PATTERN.length];
         // Biological noise: +/- 1-2 ticks with gaussian distribution
         float noise = (float) (bioRandom.nextGaussian() * 1.2);
         pattern = Math.max(8, base + noise);
      } else {
         pattern = 0.0F;
      }

      float scale = 1.0F;
      if (tpsSync) {
         float tps = class_3532.method_15363(ServerTickSync.INSTANCE.effectiveTps(), 1.0F, 20.0F);
         scale = 20.0F / tps;
      }

      this.extraDelayTicks = Math.max(0, Math.round(pattern * scale));
   }

   public boolean cooldownReady(class_746 player, int ticksAhead) {
      int delayLeft = this.extraDelayTicks - ticksAhead;
      return player.method_6047().method_31574(class_1802.field_49814) ? delayLeft <= 0 : player.method_7261(ticksAhead + 0.5F) > 0.9F && delayLeft <= 0;
   }

   public void onAttack() {
      this.hitCounter++;
   }

   public int hitCounter() {
      return this.hitCounter;
   }

   public void reset() {
      this.extraDelayTicks = 0;
   }
}
