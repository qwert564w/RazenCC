package org.ryzen.feature.impl.pve;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.PlayerTickEvent;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.mining.MiningSessionSnapshot;

@Environment(EnvType.CLIENT)
public final class AutoTpLootFeature extends PveFeature {
   public final NumberSetting range = this.register(new NumberSetting("Range", 128.0, 4.0, 256.0, 4.0, " blocks"));
   public final NumberSetting maxSpeed = this.register(new NumberSetting("Max Speed", 35.0, 0.5, 35.0, 0.5, " blocks/tick"));
   public final NumberSetting acceleration = this.register(new NumberSetting("Acceleration", 0.5, 0.05, 1.0, 0.05, "x"));
   public final NumberSetting returnRadius = this.register(new NumberSetting("Return Radius", 0.5, 0.1, 2.0, 0.1, " blocks"));
   public final NumberSetting targetTimeout = this.register(new NumberSetting("Target Timeout", 15.0, 2.0, 60.0, 1.0, "s"));
   private final MiningSessionSnapshot snapshot = new MiningSessionSnapshot();
   private class_1542 target;
   private class_243 origin;
   private AutoTpLootFeature.State state = AutoTpLootFeature.State.IDLE;
   private long stateTick;
   private long tick;
   private boolean controlledVelocity;

   public AutoTpLootFeature() {
      super("AutoTpLoot", "Accelerates flight to the nearest grounded item and returns", -1, AutomationPriority.FEATURE, AutomationResource.MOVEMENT);
   }

   @Override
   protected void onPveEnable() {
      this.reset(false);
      this.snapshot.capture(class_310.method_1551().field_1724);
   }

   @Override
   protected void onPveDisable() {
      this.cleanup();
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.cleanup();
   }

   @EventTarget
   public void onPlayerTick(PlayerTickEvent event) {
      if (event.isPre()) {
         class_746 player = event.getPlayer();
         class_310 client = class_310.method_1551();
         this.tick++;
         if (player != null && client.field_1687 != null && player.method_5805() && player.method_31549().field_7479) {
            this.snapshot.capture(player);
            switch (this.state) {
               case IDLE:
                  this.acquireTarget(client, player);
                  break;
               case SEEKING:
                  this.seek(player);
                  break;
               case RETURNING:
                  this.returnToOrigin(player);
            }
         } else {
            this.reset(true);
         }
      }
   }

   public AutoTpLootFeature.State getState() {
      return this.state;
   }

   public class_1542 getTarget() {
      return this.target;
   }

   private void acquireTarget(class_310 client, class_746 player) {
      double rangeSquared = this.range.getValue() * this.range.getValue();
      double nearestDistance = rangeSquared;
      this.target = null;

      for (class_1297 entity : client.field_1687.method_18112()) {
         if (entity instanceof class_1542 item && item.method_5805() && item.method_24828()) {
            double distance = item.method_5858(player);
            if (distance <= nearestDistance) {
               nearestDistance = distance;
               this.target = item;
            }
         }
      }

      if (this.target != null) {
         this.origin = player.method_73189();
         this.transition(AutoTpLootFeature.State.SEEKING);
      }
   }

   private void seek(class_746 player) {
      long timeoutTicks = Math.max(1L, this.targetTimeout.getValue().longValue() * 20L);
      if (this.target != null && this.target.method_5805() && this.tick - this.stateTick < timeoutTicks) {
         this.accelerate(player, this.target.method_73189());
      } else {
         this.transition(AutoTpLootFeature.State.RETURNING);
      }
   }

   private void returnToOrigin(class_746 player) {
      if (this.origin == null) {
         this.reset(true);
      } else {
         double distance = player.method_73189().method_1022(this.origin);
         if (distance <= this.returnRadius.getValue()) {
            this.reset(true);
         } else {
            this.accelerate(player, this.origin);
         }
      }
   }

   private void accelerate(class_746 player, class_243 destination) {
      class_243 delta = destination.method_1020(player.method_73189());
      double distance = delta.method_1033();
      if (distance <= 1.0E-6) {
         player.method_18799(class_243.field_1353);
         this.controlledVelocity = true;
      } else {
         double speed = Math.min(this.maxSpeed.getValue(), distance * this.acceleration.getValue());
         player.method_18799(delta.method_1029().method_1021(speed));
         this.controlledVelocity = true;
      }
   }

   private void transition(AutoTpLootFeature.State next) {
      this.state = next;
      this.stateTick = this.tick;
      if (next == AutoTpLootFeature.State.RETURNING) {
         this.target = null;
      }
   }

   private void reset(boolean stopMovement) {
      class_746 player = class_310.method_1551().field_1724;
      if (stopMovement && this.controlledVelocity && player != null) {
         player.method_18799(class_243.field_1353);
      }

      this.target = null;
      this.origin = null;
      this.state = AutoTpLootFeature.State.IDLE;
      this.stateTick = this.tick;
      this.controlledVelocity = false;
   }

   private void cleanup() {
      class_310 client = class_310.method_1551();
      this.reset(true);
      this.snapshot.restore(client);
   }

   @Environment(EnvType.CLIENT)
   public enum State {
      IDLE,
      SEEKING,
      RETURNING;
   }
}
