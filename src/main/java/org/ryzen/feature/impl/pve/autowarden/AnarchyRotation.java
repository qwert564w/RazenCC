package org.ryzen.feature.impl.pve.autowarden;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class AnarchyRotation {
   private static final double DEATH_COST = 24.0;
   private static final double FAILED_CYCLE_COST = 8.0;
   private static final double DEPOSIT_VALUE = 4.0;
   private static final double CHEST_VALUE = 2.0;
   private static final double EXPLORATION_VALUE = 12.0;
   private final Map<Integer, AnarchyRotation.MutableMetrics> metrics = new HashMap<>();
   private int active = -1;
   private int tieCursor;

   public int active() {
      return this.active;
   }

   public int pickNext(List<Integer> candidates, double crowdPenalty, long nowMillis) {
      Objects.requireNonNull(candidates, "candidates");
      if (candidates.isEmpty()) {
         this.active = -1;
         return -1;
      }

      boolean hasAvailable = candidates.stream().anyMatch(value -> !this.isAvoided(value, nowMillis));
      int size = candidates.size();
      int selected = -1;
      double selectedScore = Double.NEGATIVE_INFINITY;

      for (int offset = 0; offset < size; offset++) {
         int index = Math.floorMod(this.tieCursor + offset, size);
         int candidate = candidates.get(index);
         if (!hasAvailable || !this.isAvoided(candidate, nowMillis)) {
            double score = this.score(candidate, crowdPenalty, nowMillis);
            if (score > selectedScore) {
               selected = candidate;
               selectedScore = score;
            }
         }
      }

      this.active = selected;
      if (selected >= 0) {
         this.tieCursor = Math.floorMod(candidates.indexOf(selected) + 1, size);
         this.mutable(selected).visits++;
      }

      return selected;
   }

   public double score(int anarchy, double crowdPenalty, long nowMillis) {
      AnarchyRotation.MutableMetrics value = this.mutable(anarchy);
      double crowd = value.crowdSamples == 0 ? 0.0 : (double)value.crowdTotal / value.crowdSamples;
      double exploration = 12.0 / (value.visits + 1.0);
      double avoided = this.isAvoided(anarchy, nowMillis) ? 1000000.0 : 0.0;
      return value.depositedItems * 4.0
         + value.lootedChests * 2.0
         + exploration
         - value.deaths * 24.0
         - value.failedCycles * 8.0
         - crowd * Math.max(0.0, crowdPenalty)
         - avoided;
   }

   public void onChestLooted(int anarchy) {
      if (anarchy >= 0) {
         this.mutable(anarchy).lootedChests++;
      }
   }

   public void onDeposited(int anarchy, int itemCount) {
      if (anarchy >= 0 && itemCount > 0) {
         AnarchyRotation.MutableMetrics value = this.mutable(anarchy);
         value.depositedItems += itemCount;
         value.completedCycles++;
      }
   }

   public void onDeath(int anarchy) {
      if (anarchy >= 0) {
         this.mutable(anarchy).deaths++;
      }
   }

   public void onFailedCycle(int anarchy) {
      if (anarchy >= 0) {
         this.mutable(anarchy).failedCycles++;
      }
   }

   public void observeCrowd(int anarchy, int nearbyPlayers) {
      if (anarchy >= 0) {
         AnarchyRotation.MutableMetrics value = this.mutable(anarchy);
         value.crowdTotal = value.crowdTotal + Math.max(0, nearbyPlayers);
         value.crowdSamples++;
      }
   }

   public void avoidFor(int anarchy, long durationMillis, long nowMillis) {
      if (anarchy >= 0) {
         this.mutable(anarchy).avoidUntil = Math.max(this.mutable(anarchy).avoidUntil, nowMillis + Math.max(0L, durationMillis));
      }
   }

   public boolean isAvoided(int anarchy, long nowMillis) {
      AnarchyRotation.MutableMetrics value = this.metrics.get(anarchy);
      return value != null && value.avoidUntil > nowMillis;
   }

   public AnarchyRotation.Snapshot snapshot(int anarchy) {
      AnarchyRotation.MutableMetrics value = this.mutable(anarchy);
      return new AnarchyRotation.Snapshot(
         value.visits,
         value.deaths,
         value.lootedChests,
         value.depositedItems,
         value.completedCycles,
         value.failedCycles,
         value.crowdSamples == 0 ? 0.0 : (double)value.crowdTotal / value.crowdSamples,
         value.avoidUntil
      );
   }

   public Map<Integer, AnarchyRotation.Snapshot> snapshots(Collection<Integer> anarchies) {
      Map<Integer, AnarchyRotation.Snapshot> result = new LinkedHashMap<>();

      for (Integer anarchy : anarchies) {
         if (anarchy != null) {
            result.put(anarchy, this.snapshot(anarchy));
         }
      }

      return Map.copyOf(result);
   }

   public void reset() {
      this.metrics.clear();
      this.active = -1;
      this.tieCursor = 0;
   }

   private AnarchyRotation.MutableMetrics mutable(int anarchy) {
      return this.metrics.computeIfAbsent(anarchy, ignored -> new AnarchyRotation.MutableMetrics());
   }

   @Environment(EnvType.CLIENT)
   private static final class MutableMetrics {
      private int visits;
      private int deaths;
      private int lootedChests;
      private int depositedItems;
      private int completedCycles;
      private int failedCycles;
      private long crowdTotal;
      private int crowdSamples;
      private long avoidUntil;
   }

   @Environment(EnvType.CLIENT)
   public record Snapshot(
      int visits, int deaths, int lootedChests, int depositedItems, int completedCycles, int failedCycles, double averageCrowd, long avoidUntilMillis
   ) {
   }
}
