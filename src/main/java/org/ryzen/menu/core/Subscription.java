package org.ryzen.menu.core;

import java.time.Duration;
import java.time.Instant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class Subscription {
   private static final String KEY = "subscriptionExpiry";
   private static final int PLACEHOLDER_DAYS = 18;
   private static Instant expiry;
   private static boolean loaded;

   private Subscription() {
   }

   public static int daysLeft() {
      Instant end = expiry();
      if (end == null) {
         return 18;
      }

      long days = Duration.between(Instant.now(), end).toDays();
      return (int)Math.max(0L, days);
   }

   public static boolean isExpired() {
      return expiry() != null && daysLeft() <= 0;
   }

   public static void setExpiry(Instant instant) {
      expiry = instant;
      loaded = true;
      MenuConfigStore.save(data -> {
         if (instant == null) {
            data.remove("subscriptionExpiry");
         } else {
            data.addProperty("subscriptionExpiry", instant.toEpochMilli());
         }
      });
   }

   private static Instant expiry() {
      if (!loaded) {
         loaded = true;
         long stored = MenuConfigStore.getLong("subscriptionExpiry", 0L);
         expiry = stored > 0L ? Instant.ofEpochMilli(stored) : null;
      }

      return expiry;
   }
}
