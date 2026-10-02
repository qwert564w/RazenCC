package org.ryzen.feature.impl.pve;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.DisconnectEvent;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.PveStateMachine;
import org.ryzen.pve.economy.CommandCooldown;
import org.ryzen.pve.economy.EconomyInventory;
import org.ryzen.pve.economy.ServerUiText;
import org.ryzen.pve.server.ServerAdapters;
import org.ryzen.pve.server.ServerProfile;

@Environment(EnvType.CLIENT)
public final class ClanUpgradeFeature extends PveFeature {
   private final PveStateMachine<ClanUpgradeFeature.State> machine = new PveStateMachine<>(ClanUpgradeFeature.State.FIND_ITEM);
   private final CommandCooldown actionCooldown = new CommandCooldown();
   private long lastTick;
   private int upgradeSlot = -1;
   private int restoreSlot = -1;
   private class_1792 upgradeItem;
   private class_2338 supportBlock;
   private boolean resourcesClaimed;
   private boolean rotationSaved;
   private float savedYaw;
   private float savedPitch;

   public ClanUpgradeFeature() {
      super("ClanUpgrade", "Uses the held clan-upgrade items on the block below", -1, AutomationPriority.FEATURE);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null && client.field_1761 != null) {
         long tick = client.field_1687.method_75260();
         this.lastTick = tick;
         if (ServerAdapters.current().profile() != ServerProfile.FUNTIME
            || ServerUiText.tabHeader(client).contains("lobby")
            || ServerUiText.tabHeader(client).contains("лобби")) {
            this.cancelAction(player, tick);
         } else if (client.field_1755 == null && player.field_7512 == player.field_7498 && !player.method_6115()) {
            switch ((ClanUpgradeFeature.State)this.machine.state()) {
               case FIND_ITEM:
                  this.findItem(player, tick);
                  break;
               case SELECT_ITEM:
                  this.selectItem(player, tick);
                  break;
               case PLACE:
                  this.placeItem(client, player, tick);
                  break;
               case BREAK:
                  this.breakPlacedItem(client, player, tick);
                  break;
               case COOLDOWN:
                  if (this.machine.ticksInState(tick) >= 5L) {
                     this.finishAction(player, tick);
                  }
            }
         } else {
            this.cancelAction(player, tick);
         }
      }
   }

   @EventTarget
   public void onDisconnect(DisconnectEvent event) {
      this.cancelAction(class_310.method_1551().field_1724, this.lastTick);
      this.resetRuntime();
   }

   @Override
   protected void onPveEnable() {
      this.resetRuntime();
   }

   @Override
   protected void onPveDisable() {
      this.cancelAction(class_310.method_1551().field_1724, this.lastTick);
      this.resetRuntime();
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.cancelAction(class_310.method_1551().field_1724, this.lastTick);
      this.resetRuntime();
   }

   private void findItem(class_746 player, long tick) {
      if (this.actionCooldown.ready(tick)) {
         int slot = EconomyInventory.findHotbar(player, stack -> stack.method_31574(class_1802.field_8810) || stack.method_31574(class_1802.field_8725));
         if (slot < 0) {
            this.actionCooldown.defer(tick, 100L);
         } else {
            class_2338 below = player.method_24515().method_10074();
            if (!player.method_73183().method_8320(below).method_26206(player.method_73183(), below, class_2350.field_11036)) {
               this.actionCooldown.defer(tick, 20L);
            } else {
               boolean claimed = PveManagerFeature.INSTANCE.rotate.getValue()
                  ? this.claim(AutomationResource.INVENTORY, AutomationResource.ROTATION, AutomationResource.COMBAT)
                  : this.claim(AutomationResource.INVENTORY, AutomationResource.COMBAT);
               if (!claimed) {
                  this.actionCooldown.defer(tick, 5L);
               } else {
                  this.resourcesClaimed = true;
                  this.restoreSlot = player.method_31548().method_67532();
                  this.upgradeSlot = slot;
                  this.upgradeItem = player.method_31548().method_5438(slot).method_7909();
                  this.supportBlock = below.method_10062();
                  this.machine.transition(ClanUpgradeFeature.State.SELECT_ITEM, tick);
               }
            }
         }
      }
   }

   private void selectItem(class_746 player, long tick) {
      if (!EconomyInventory.selectHotbar(player, this.upgradeSlot)) {
         this.cancelAction(player, tick);
      } else {
         this.machine.transition(ClanUpgradeFeature.State.PLACE, tick);
      }
   }

   private void placeItem(class_310 client, class_746 player, long tick) {
      if (this.supportBlock != null
         && this.upgradeItem != null
         && player.method_6047().method_31574(this.upgradeItem)
         && player.method_73183().method_8320(this.supportBlock).method_26206(player.method_73183(), this.supportBlock, class_2350.field_11036)) {
         this.rotateToward(player, class_243.method_24953(this.supportBlock));
         class_243 hitPosition = class_243.method_24953(this.supportBlock).method_1031(0.0, 0.5, 0.0);
         client.field_1761.method_2896(player, class_1268.field_5808, new class_3965(hitPosition, class_2350.field_11036, this.supportBlock, false));
         player.method_6104(class_1268.field_5808);
         this.machine.transition(ClanUpgradeFeature.State.BREAK, tick);
      } else {
         this.cancelAction(player, tick);
      }
   }

   private void breakPlacedItem(class_310 client, class_746 player, long tick) {
      if (this.supportBlock == null) {
         this.cancelAction(player, tick);
      } else {
         class_2338 placedPos = this.supportBlock.method_10084();
         class_2680 placedState = player.method_73183().method_8320(placedPos);
         boolean expectedBlock = this.upgradeItem == class_1802.field_8810
            ? placedState.method_27852(class_2246.field_10336) || placedState.method_27852(class_2246.field_10099)
            : this.upgradeItem == class_1802.field_8725 && placedState.method_27852(class_2246.field_10091);
         if (!expectedBlock) {
            if (this.machine.ticksInState(tick) >= 10L) {
               this.actionCooldown.defer(tick, 20L);
               this.cancelAction(player, tick);
            }
         } else {
            this.rotateToward(player, class_243.method_24953(placedPos));
            client.field_1761.method_2910(placedPos, class_2350.field_11036);
            this.actionCooldown.tryAcquire(tick, 5L);
            this.machine.transition(ClanUpgradeFeature.State.COOLDOWN, tick);
         }
      }
   }

   private void finishAction(class_746 player, long tick) {
      this.restoreRotation(player);
      this.restoreSlot(player);
      this.upgradeSlot = -1;
      this.upgradeItem = null;
      this.supportBlock = null;
      this.machine.transition(ClanUpgradeFeature.State.FIND_ITEM, tick);
      this.releaseResources();
   }

   private void cancelAction(class_746 player, long tick) {
      this.restoreRotation(player);
      this.restoreSlot(player);
      this.upgradeSlot = -1;
      this.upgradeItem = null;
      this.supportBlock = null;
      this.machine.transition(ClanUpgradeFeature.State.FIND_ITEM, tick);
      this.releaseResources();
   }

   private void restoreSlot(class_746 player) {
      if (this.restoreSlot >= 0 && this.restoreSlot < 9) {
         EconomyInventory.selectHotbar(player, this.restoreSlot);
      }

      this.restoreSlot = -1;
   }

   private void releaseResources() {
      if (this.resourcesClaimed) {
         this.resourcesClaimed = false;
         PveAutomationCoordinator.INSTANCE.release(this);
      }
   }

   private void rotateToward(class_746 player, class_243 target) {
      if (PveManagerFeature.INSTANCE.rotate.getValue()) {
         if (!this.rotationSaved) {
            this.savedYaw = player.method_36454();
            this.savedPitch = player.method_36455();
            this.rotationSaved = true;
         }

         class_243 delta = target.method_1020(player.method_33571());
         double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
         player.method_36456((float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F);
         player.method_36457((float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal))));
      }
   }

   private void restoreRotation(class_746 player) {
      if (this.rotationSaved) {
         if (player != null) {
            player.method_36456(this.savedYaw);
            player.method_36457(this.savedPitch);
         }

         this.rotationSaved = false;
      }
   }

   private void resetRuntime() {
      this.machine.reset(0L);
      this.actionCooldown.reset();
      this.upgradeSlot = -1;
      this.restoreSlot = -1;
      this.upgradeItem = null;
      this.supportBlock = null;
      this.rotationSaved = false;
      this.releaseResources();
      this.lastTick = 0L;
   }

   @Environment(EnvType.CLIENT)
   enum State {
      FIND_ITEM,
      SELECT_ITEM,
      PLACE,
      BREAK,
      COOLDOWN;
   }
}
