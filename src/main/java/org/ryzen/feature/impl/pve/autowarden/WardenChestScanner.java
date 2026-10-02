package org.ryzen.feature.impl.pve.autowarden;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1531;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2625;
import net.minecraft.class_2680;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_2350.class_2353;

@Environment(EnvType.CLIENT)
final class WardenChestScanner {
   private static final long OBSERVATION_TTL_MILLIS = 1800000L;
   private static final long FAILED_TTL_MILLIS = 90000L;
   private static final int TIMER_CHEST_SEARCH_RADIUS = 2;
   private final Map<class_2338, WardenChestScanner.MutableChest> known = new HashMap<>();
   private final Map<class_2338, Long> failedUntil = new HashMap<>();
   private final Set<class_2338> looted = new HashSet<>();

   List<WardenChestScanner.ChestObservation> scanLootChests(class_638 level, class_746 player, int horizontalRadius, int crowdRadius, long nowMillis) {
      int radius = Math.max(8, Math.min(128, horizontalRadius));
      class_238 entityBounds = player.method_5829().method_1009(radius, 32.0, radius);

      for (class_1531 stand : level.method_8390(class_1531.class, entityBounds, entity -> entity.method_16914() && entity.method_5797() != null)) {
         AutoWardenParsers.parseChestTimer(stand.method_5797().getString())
            .ifPresent(timer -> findContainerNear(level, stand.method_24515()).ifPresent(position -> this.observeTimer(position, timer, nowMillis)));
      }

      int directRadius = Math.min(48, radius);
      class_2338 origin = player.method_24515();

      for (int x = -directRadius; x <= directRadius; x++) {
         for (int z = -directRadius; z <= directRadius; z++) {
            for (int y = -10; y <= 10; y++) {
               class_2338 position = origin.method_10069(x, y, z);
               if (level.method_22340(position) && isContainer(level.method_8320(position))) {
                  class_2338 canonical = canonicalContainer(level, position);
                  this.known.computeIfAbsent(canonical, ignored -> new WardenChestScanner.MutableChest(canonical, -1, nowMillis, false, false));
               }
            }
         }
      }

      this.known.values().removeIf(valuex -> nowMillis - valuex.lastSeenAt > 1800000L);
      this.failedUntil.entrySet().removeIf(entry -> entry.getValue() <= nowMillis);
      List<WardenChestScanner.ChestObservation> result = new ArrayList<>();

      for (WardenChestScanner.MutableChest value : this.known.values()) {
         if (!this.looted.contains(value.position) && this.failedUntil.getOrDefault(value.position, 0L) <= nowMillis) {
            int crowd = nearbyPlayers(level, player, value.position, crowdRadius);
            result.add(value.snapshot(crowd));
         }
      }

      result.sort(Comparator.comparingLong(valuex -> valuex.position().method_10063()));
      return List.copyOf(result);
   }

   Optional<WardenChestScanner.ChestObservation> selectBest(
      Collection<WardenChestScanner.ChestObservation> candidates,
      class_243 origin,
      int maxWaitSeconds,
      boolean avoidCrowded,
      double crowdPenalty,
      long nowMillis
   ) {
      WardenChestScanner.ChestObservation selected = null;
      double selectedScore = Double.POSITIVE_INFINITY;

      for (WardenChestScanner.ChestObservation candidate : candidates) {
         int remaining = candidate.remainingSeconds(nowMillis);
         if (!candidate.timerKnown() || remaining <= Math.max(0, maxWaitSeconds)) {
            double distance = class_243.method_24953(candidate.position()).method_1022(origin);
            double unknownPenalty = candidate.timerKnown() ? 0.0 : 30.0;
            double score = remaining * 4.0 + distance + unknownPenalty + (avoidCrowded ? candidate.nearbyPlayers() * Math.max(0.0, crowdPenalty) : 0.0);
            if (score < selectedScore) {
               selected = candidate;
               selectedScore = score;
            }
         }
      }

      return Optional.ofNullable(selected);
   }

   List<WardenChestScanner.StorageChest> scanStorage(class_638 level, class_2338 center, int radius, String restockKeyword, String sellKeyword) {
      int scanRadius = Math.max(4, Math.min(48, radius));
      String restock = AutoWardenParsers.normalize(restockKeyword);
      String sell = AutoWardenParsers.normalize(sellKeyword);
      Map<class_2338, WardenChestScanner.StorageChest> result = new LinkedHashMap<>();

      for (int x = -scanRadius; x <= scanRadius; x++) {
         for (int z = -scanRadius; z <= scanRadius; z++) {
            for (int y = -8; y <= 8; y++) {
               class_2338 position = center.method_10069(x, y, z);
               if (level.method_22340(position) && isContainer(level.method_8320(position))) {
                  class_2338 canonical = canonicalContainer(level, position);
                  if (!result.containsKey(canonical)) {
                     String sign = signText(level, canonical);
                     boolean signed = !sign.isBlank();
                     WardenChestScanner.StorageKind kind;
                     if (signed && !sell.isBlank() && sign.contains(sell)) {
                        kind = WardenChestScanner.StorageKind.SELL;
                     } else if (!signed || !restock.isBlank() && !sign.contains(restock)) {
                        if (signed) {
                           kind = WardenChestScanner.StorageKind.OTHER_SIGNED;
                        } else {
                           kind = WardenChestScanner.StorageKind.DEPOSIT;
                        }
                     } else {
                        kind = WardenChestScanner.StorageKind.RESTOCK;
                     }

                     result.put(canonical, new WardenChestScanner.StorageChest(canonical, kind, sign));
                  }
               }
            }
         }
      }

      return result.values().stream().sorted(Comparator.comparingDouble(value -> value.position().method_10262(center))).toList();
   }

   void markLooted(class_2338 position) {
      if (position != null) {
         this.looted.add(position.method_10062());
         WardenChestScanner.MutableChest value = this.known.get(position);
         if (value != null) {
            value.stolen = true;
         }
      }
   }

   void markFailed(class_2338 position, long nowMillis) {
      if (position != null) {
         this.failedUntil.put(position.method_10062(), nowMillis + 90000L);
      }
   }

   void clearLootedCycle() {
      this.looted.clear();
      this.known.values().forEach(value -> value.stolen = false);
   }

   void clearWorld() {
      this.known.clear();
      this.failedUntil.clear();
      this.looted.clear();
   }

   private void observeTimer(class_2338 position, AutoWardenParsers.TimerReading timer, long nowMillis) {
      class_2338 immutable = position.method_10062();
      WardenChestScanner.MutableChest existing = this.known.get(immutable);
      if (existing == null) {
         this.known.put(immutable, new WardenChestScanner.MutableChest(immutable, timer.remainingSeconds(), nowMillis, true, timer.openNow()));
      } else {
         existing.observedSeconds = timer.remainingSeconds();
         existing.observedAt = nowMillis;
         existing.lastSeenAt = nowMillis;
         existing.timerKnown = true;
         existing.openNow = timer.openNow();
      }
   }

   private static Optional<class_2338> findContainerNear(class_638 level, class_2338 stand) {
      class_2338 selected = null;
      double selectedDistance = Double.POSITIVE_INFINITY;

      for (int x = -2; x <= 2; x++) {
         for (int z = -2; z <= 2; z++) {
            for (int y = -3; y <= 1; y++) {
               class_2338 position = stand.method_10069(x, y, z);
               if (isContainer(level.method_8320(position))) {
                  double distance = position.method_10262(stand);
                  if (distance < selectedDistance) {
                     selected = canonicalContainer(level, position);
                     selectedDistance = distance;
                  }
               }
            }
         }
      }

      return Optional.ofNullable(selected);
   }

   private static int nearbyPlayers(class_638 level, class_746 self, class_2338 chest, int radius) {
      if (radius <= 0) {
         return 0;
      }

      class_238 bounds = new class_238(chest).method_1014(radius);
      return level.method_8390(class_1657.class, bounds, player -> player != self).size();
   }

   private static boolean isContainer(class_2680 state) {
      class_2248 block = state.method_26204();
      return block == class_2246.field_10034 || block == class_2246.field_10380 || block == class_2246.field_16328;
   }

   private static class_2338 canonicalContainer(class_638 level, class_2338 position) {
      class_2248 block = level.method_8320(position).method_26204();
      if (block != class_2246.field_10034 && block != class_2246.field_10380) {
         return position.method_10062();
      }

      class_2338 selected = position;

      for (class_2350 direction : class_2353.field_11062) {
         class_2338 adjacent = position.method_10093(direction);
         if (level.method_8320(adjacent).method_26204() == block && adjacent.method_10063() < selected.method_10063()) {
            selected = adjacent;
         }
      }

      return selected.method_10062();
   }

   private static String signText(class_638 level, class_2338 container) {
      StringBuilder result = new StringBuilder();
      appendAdjacentSigns(level, container, result);
      class_2248 block = level.method_8320(container).method_26204();
      if (block == class_2246.field_10034 || block == class_2246.field_10380) {
         for (class_2350 direction : class_2353.field_11062) {
            class_2338 adjacent = container.method_10093(direction);
            if (level.method_8320(adjacent).method_26204() == block) {
               appendAdjacentSigns(level, adjacent, result);
            }
         }
      }

      return AutoWardenParsers.normalize(result.toString());
   }

   private static void appendAdjacentSigns(class_638 level, class_2338 container, StringBuilder output) {
      for (class_2350 direction : class_2350.values()) {
         if (level.method_8321(container.method_10093(direction)) instanceof class_2625 sign) {
            appendSignSide(sign, "getFrontText", output);
            appendSignSide(sign, "getBackText", output);
         }
      }
   }

   private static void appendSignSide(class_2625 sign, String accessor, StringBuilder output) {
      try {
         Method sideMethod = class_2625.class.getMethod(accessor);
         Object side = sideMethod.invoke(sign);
         Method messages = findMethod(side.getClass(), "getMessages", boolean.class);
         if (messages != null) {
            appendComponents(messages.invoke(side, false), output);
            return;
         }

         Method message = findMethod(side.getClass(), "getMessage", int.class, boolean.class);
         if (message != null) {
            for (int line = 0; line < 4; line++) {
               appendComponent(message.invoke(side, line, false), output);
            }
         }
      } catch (ReflectiveOperationException | RuntimeException var8) {
      }
   }

   private static Method findMethod(Class<?> type, String name, Class<?>... parameters) {
      try {
         return type.getMethod(name, parameters);
      } catch (NoSuchMethodException ignored) {
         return null;
      }
   }

   private static void appendComponents(Object value, StringBuilder output) {
      if (value != null && value.getClass().isArray()) {
         for (int index = 0; index < Array.getLength(value); index++) {
            appendComponent(Array.get(value, index), output);
         }
      }
   }

   private static void appendComponent(Object value, StringBuilder output) {
      if (value instanceof class_2561 component) {
         output.append(' ').append(component.getString());
      }
   }

   @Environment(EnvType.CLIENT)
   record ChestObservation(
      class_2338 position, int observedSeconds, long observedAtMillis, boolean timerKnown, boolean openNow, boolean stolen, int nearbyPlayers
   ) {
      int remainingSeconds(long nowMillis) {
         if (this.timerKnown && !this.openNow) {
            long elapsed = Math.max(0L, nowMillis - this.observedAtMillis) / 1000L;
            return (int)Math.max(0L, this.observedSeconds - elapsed);
         } else {
            return 0;
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class MutableChest {
      private final class_2338 position;
      private int observedSeconds;
      private long observedAt;
      private long lastSeenAt;
      private boolean timerKnown;
      private boolean openNow;
      private boolean stolen;

      private MutableChest(class_2338 position, int observedSeconds, long observedAt, boolean timerKnown, boolean openNow) {
         this.position = position;
         this.observedSeconds = observedSeconds;
         this.observedAt = observedAt;
         this.lastSeenAt = observedAt;
         this.timerKnown = timerKnown;
         this.openNow = openNow;
      }

      private WardenChestScanner.ChestObservation snapshot(int nearbyPlayers) {
         return new WardenChestScanner.ChestObservation(
            this.position, this.observedSeconds, this.observedAt, this.timerKnown, this.openNow, this.stolen, nearbyPlayers
         );
      }
   }

   @Environment(EnvType.CLIENT)
   record StorageChest(class_2338 position, WardenChestScanner.StorageKind kind, String signText) {
   }

   @Environment(EnvType.CLIENT)
   enum StorageKind {
      DEPOSIT,
      RESTOCK,
      SELL,
      OTHER_SIGNED;
   }
}
