package org.ryzen.utils.combat;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SprintManager {
   private static final long DEFAULT_HOLD_MS = 50L;
   private static final long MIN_HOLD_MS = 30L;
   private static final long MAX_HOLD_MS = 55L;
   private static long lastImminentNanos = Long.MIN_VALUE;
   private static long holdMillis = 50L;

   private SprintManager() {
   }

   public static void markAttackImminent() {
      lastImminentNanos = System.nanoTime();
   }

   public static boolean isImminent() {
      return lastImminentNanos != Long.MIN_VALUE && System.nanoTime() - lastImminentNanos < holdMillis * 1000000L;
   }

   public static void onAttack() {
      holdMillis = ThreadLocalRandom.current().nextLong(30L, 56L);
      lastImminentNanos = System.nanoTime();
   }

   public static boolean shouldFreezeMovementInput() {
      return lastImminentNanos != Long.MIN_VALUE && System.nanoTime() - lastImminentNanos < holdMillis * 1000000L;
   }

   public static void reset() {
      lastImminentNanos = Long.MIN_VALUE;
      holdMillis = 50L;
   }
}
