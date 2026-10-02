package org.ryzen.utils.combat;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class TargetFilter {
   private final boolean targetsPlayers;
   private final boolean targetsFriends;
   private final boolean targetsNakedPlayers;
   private final boolean targetsInvisibles;
   private final boolean targetsVillagers;
   private final boolean targetsMonsters;
   private final boolean targetsAnimals;
   private final boolean targetsTeams;
   private final double distanceWeight;
   private final double healthWeight;
   private final double armorWeight;
   private final double fovWeight;

   @Generated
   TargetFilter(
      boolean targetsPlayers,
      boolean targetsFriends,
      boolean targetsNakedPlayers,
      boolean targetsInvisibles,
      boolean targetsVillagers,
      boolean targetsMonsters,
      boolean targetsAnimals,
      boolean targetsTeams,
      double distanceWeight,
      double healthWeight,
      double armorWeight,
      double fovWeight
   ) {
      this.targetsPlayers = targetsPlayers;
      this.targetsFriends = targetsFriends;
      this.targetsNakedPlayers = targetsNakedPlayers;
      this.targetsInvisibles = targetsInvisibles;
      this.targetsVillagers = targetsVillagers;
      this.targetsMonsters = targetsMonsters;
      this.targetsAnimals = targetsAnimals;
      this.targetsTeams = targetsTeams;
      this.distanceWeight = distanceWeight;
      this.healthWeight = healthWeight;
      this.armorWeight = armorWeight;
      this.fovWeight = fovWeight;
   }

   @Generated
   public static TargetFilter.TargetFilterBuilder builder() {
      return new TargetFilter.TargetFilterBuilder();
   }

   @Generated
   public boolean isTargetsPlayers() {
      return this.targetsPlayers;
   }

   @Generated
   public boolean isTargetsFriends() {
      return this.targetsFriends;
   }

   @Generated
   public boolean isTargetsNakedPlayers() {
      return this.targetsNakedPlayers;
   }

   @Generated
   public boolean isTargetsInvisibles() {
      return this.targetsInvisibles;
   }

   @Generated
   public boolean isTargetsVillagers() {
      return this.targetsVillagers;
   }

   @Generated
   public boolean isTargetsMonsters() {
      return this.targetsMonsters;
   }

   @Generated
   public boolean isTargetsAnimals() {
      return this.targetsAnimals;
   }

   @Generated
   public boolean isTargetsTeams() {
      return this.targetsTeams;
   }

   @Generated
   public double getDistanceWeight() {
      return this.distanceWeight;
   }

   @Generated
   public double getHealthWeight() {
      return this.healthWeight;
   }

   @Generated
   public double getArmorWeight() {
      return this.armorWeight;
   }

   @Generated
   public double getFovWeight() {
      return this.fovWeight;
   }

   @Environment(EnvType.CLIENT)
   @Generated
   public static class TargetFilterBuilder {
      @Generated
      private boolean targetsPlayers;
      @Generated
      private boolean targetsFriends;
      @Generated
      private boolean targetsNakedPlayers;
      @Generated
      private boolean targetsInvisibles;
      @Generated
      private boolean targetsVillagers;
      @Generated
      private boolean targetsMonsters;
      @Generated
      private boolean targetsAnimals;
      @Generated
      private boolean targetsTeams;
      @Generated
      private double distanceWeight;
      @Generated
      private double healthWeight;
      @Generated
      private double armorWeight;
      @Generated
      private double fovWeight;

      @Generated
      TargetFilterBuilder() {
      }

      @Generated
      public TargetFilter.TargetFilterBuilder targetsPlayers(boolean targetsPlayers) {
         this.targetsPlayers = targetsPlayers;
         return this;
      }

      @Generated
      public TargetFilter.TargetFilterBuilder targetsFriends(boolean targetsFriends) {
         this.targetsFriends = targetsFriends;
         return this;
      }

      @Generated
      public TargetFilter.TargetFilterBuilder targetsNakedPlayers(boolean targetsNakedPlayers) {
         this.targetsNakedPlayers = targetsNakedPlayers;
         return this;
      }

      @Generated
      public TargetFilter.TargetFilterBuilder targetsInvisibles(boolean targetsInvisibles) {
         this.targetsInvisibles = targetsInvisibles;
         return this;
      }

      @Generated
      public TargetFilter.TargetFilterBuilder targetsVillagers(boolean targetsVillagers) {
         this.targetsVillagers = targetsVillagers;
         return this;
      }

      @Generated
      public TargetFilter.TargetFilterBuilder targetsMonsters(boolean targetsMonsters) {
         this.targetsMonsters = targetsMonsters;
         return this;
      }

      @Generated
      public TargetFilter.TargetFilterBuilder targetsAnimals(boolean targetsAnimals) {
         this.targetsAnimals = targetsAnimals;
         return this;
      }

      @Generated
      public TargetFilter.TargetFilterBuilder targetsTeams(boolean targetsTeams) {
         this.targetsTeams = targetsTeams;
         return this;
      }

      @Generated
      public TargetFilter.TargetFilterBuilder distanceWeight(double distanceWeight) {
         this.distanceWeight = distanceWeight;
         return this;
      }

      @Generated
      public TargetFilter.TargetFilterBuilder healthWeight(double healthWeight) {
         this.healthWeight = healthWeight;
         return this;
      }

      @Generated
      public TargetFilter.TargetFilterBuilder armorWeight(double armorWeight) {
         this.armorWeight = armorWeight;
         return this;
      }

      @Generated
      public TargetFilter.TargetFilterBuilder fovWeight(double fovWeight) {
         this.fovWeight = fovWeight;
         return this;
      }

      @Generated
      public TargetFilter build() {
         return new TargetFilter(
            this.targetsPlayers,
            this.targetsFriends,
            this.targetsNakedPlayers,
            this.targetsInvisibles,
            this.targetsVillagers,
            this.targetsMonsters,
            this.targetsAnimals,
            this.targetsTeams,
            this.distanceWeight,
            this.healthWeight,
            this.armorWeight,
            this.fovWeight
         );
      }

      @Generated
      @Override
      public String toString() {
         return "TargetFilter.TargetFilterBuilder(targetsPlayers="
            + this.targetsPlayers
            + ", targetsFriends="
            + this.targetsFriends
            + ", targetsNakedPlayers="
            + this.targetsNakedPlayers
            + ", targetsInvisibles="
            + this.targetsInvisibles
            + ", targetsVillagers="
            + this.targetsVillagers
            + ", targetsMonsters="
            + this.targetsMonsters
            + ", targetsAnimals="
            + this.targetsAnimals
            + ", targetsTeams="
            + this.targetsTeams
            + ", distanceWeight="
            + this.distanceWeight
            + ", healthWeight="
            + this.healthWeight
            + ", armorWeight="
            + this.armorWeight
            + ", fovWeight="
            + this.fovWeight
            + ")";
      }
   }
}
