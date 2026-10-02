package org.ryzen.utils.combat;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SprintManager {
   private static final long DEFAULT_HOLD_MS = 75L;
   private static final long MIN_HOLD_MS = 60L;
   private static final long MAX_HOLD_MS = 91L;
   private static long lastImminentNanos = Long.MIN_VALUE;
   private static long holdMillis = 75L;

   private SprintManager() {
   }

   public static void markAttackImminent() {
      lastImminentNanos = System.nanoTime();
   }

   public static void onAttack() {
      holdMillis = ThreadLocalRandom.current().nextLong(60L, 92L);
      lastImminentNanos = System.nanoTime();
   }

   public static boolean shouldFreezeMovementInput() {
      return lastImminentNanos != Long.MIN_VALUE && System.nanoTime() - lastImminentNanos < holdMillis * 1000000L;
   }

   public static void reset() {
      lastImminentNanos = Long.MIN_VALUE;
      holdMillis = 75L;
   }
}
