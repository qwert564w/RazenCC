package org.ryzen.event.events.game;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class TickContext {
   private class_310 client;
   private class_746 player;
   private class_638 level;
   private boolean worldReady;
   private boolean closestAttackableResolved;
   private class_1297 closestAttackableTarget;
   private double closestAttackableDistance = Double.POSITIVE_INFINITY;

   public TickContext begin(class_310 client) {
      this.client = client;
      this.player = client.field_1724;
      this.level = client.field_1687;
      this.worldReady = this.player != null && this.level != null;
      this.closestAttackableResolved = false;
      this.closestAttackableTarget = null;
      this.closestAttackableDistance = Double.POSITIVE_INFINITY;
      return this;
   }

   @Generated
   public class_310 getClient() {
      return this.client;
   }

   @Generated
   public class_746 getPlayer() {
      return this.player;
   }

   @Generated
   public class_638 getLevel() {
      return this.level;
   }

   @Generated
   public boolean isWorldReady() {
      return this.worldReady;
   }

   @Generated
   public boolean isClosestAttackableResolved() {
      return this.closestAttackableResolved;
   }

   @Generated
   public class_1297 getClosestAttackableTarget() {
      return this.closestAttackableTarget;
   }

   @Generated
   public double getClosestAttackableDistance() {
      return this.closestAttackableDistance;
   }
}
