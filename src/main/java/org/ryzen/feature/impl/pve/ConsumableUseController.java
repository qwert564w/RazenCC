package org.ryzen.feature.impl.pve;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10131;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_746;
import net.minecraft.class_9334;
import org.ryzen.utils.inventory.DropAllInventoryController;
import org.ryzen.utils.inventory.InventorySwap;

@Environment(EnvType.CLIENT)
final class ConsumableUseController {
   private static final int MAX_USE_TICKS = 200;
   private ConsumableUseController.Mode mode = ConsumableUseController.Mode.IDLE;
   private class_1268 directHand = class_1268.field_5808;
   private class_1799 expectedStack = class_1799.field_8037;
   private class_1799 expectedRemainder = class_1799.field_8037;
   private boolean waitForCompletion;
   private boolean useIssued;
   private boolean sawExpectedUse;
   private boolean syntheticUseHeld;
   private int directDelayTicks;
   private int activeTicks;
   private int sourceSlot = -1;
   private int destinationSlot = -1;
   private int destinationHotbarSlot = -1;
   private class_1799 sourceBefore = class_1799.field_8037;
   private class_1799 destinationBefore = class_1799.field_8037;

   boolean start(class_310 client, class_746 player, ConsumableSelector.Candidate<class_1799> candidate, boolean waitForCompletion, int directDelayTicks) {
      if (!this.isActive()
         && client.field_1761 != null
         && player != null
         && player.field_7512 == player.field_7498
         && player.field_7498.method_34255().method_7960()
         && !DropAllInventoryController.blocksInventoryOperations()
         && !InventorySwap.isBusy()) {
         this.expectedStack = candidate.value().method_7972();
         class_10131 useRemainder = (class_10131)this.expectedStack.method_58694(class_9334.field_53965);
         this.expectedRemainder = useRemainder == null ? class_1799.field_8037 : useRemainder.comp_3093().method_7972();
         this.waitForCompletion = waitForCompletion;
         this.directDelayTicks = Math.max(0, directDelayTicks);
         this.activeTicks = 0;
         this.useIssued = false;
         this.sawExpectedUse = false;
         if (candidate.location() == ConsumableSelector.Location.OFF_HAND) {
            this.mode = ConsumableUseController.Mode.DIRECT_HAND;
            this.directHand = class_1268.field_5810;
            if (this.directDelayTicks == 0) {
               this.issueDirectUse(client, player);
            }

            return true;
         } else {
            this.mode = ConsumableUseController.Mode.INVENTORY_SWAP;
            this.destinationHotbarSlot = player.method_31548().method_67532();
            this.destinationSlot = 36 + this.destinationHotbarSlot;
            if (candidate.location() == ConsumableSelector.Location.MAIN_HAND) {
               this.sourceSlot = -1;
               InventorySwap.useSelected(waitForCompletion);
            } else {
               this.sourceSlot = candidate.containerSlot();
               if (!player.field_7498.method_40442(this.sourceSlot) || this.sourceSlot == this.destinationSlot) {
                  this.clear();
                  return false;
               }

               this.sourceBefore = player.field_7498.method_7611(this.sourceSlot).method_7677().method_7972();
               this.destinationBefore = player.field_7498.method_7611(this.destinationSlot).method_7677().method_7972();
               InventorySwap.useFromSlot(this.sourceSlot, waitForCompletion);
            }

            if (!InventorySwap.isBusy()) {
               this.clear();
               return false;
            } else {
               return true;
            }
         }
      } else {
         return false;
      }
   }

   boolean tick(class_310 client, class_746 player) {
      if (!this.isActive()) {
         return false;
      }

      if (player != null && client.field_1761 != null) {
         this.activeTicks++;
         if (this.activeTicks >= 200) {
            this.cancel(client, player);
            return false;
         }

         if (this.mode == ConsumableUseController.Mode.DIRECT_HAND) {
            return this.tickDirect(client, player);
         }

         if (this.isExpectedUse(player)) {
            this.sawExpectedUse = true;
         }

         if (!InventorySwap.isBusy()) {
            if (this.restoreIfNeeded(client, player)) {
               this.clear();
               return false;
            } else {
               return true;
            }
         } else {
            if (this.isExpectedUse(player) || !this.positionsRestored(player) || !this.sawExpectedUse && this.activeTicks < 5) {
               return true;
            }

            this.clear();
            return false;
         }
      } else {
         this.cancel(client, player);
         return false;
      }
   }

   boolean isActive() {
      return this.mode != ConsumableUseController.Mode.IDLE;
   }

   void cancel(class_310 client, class_746 player) {
      if (this.isActive()) {
         if (this.mode == ConsumableUseController.Mode.DIRECT_HAND) {
            this.releaseExpectedUse(client, player);
            this.releaseSyntheticUse(client);
            this.clear();
         } else {
            boolean alreadyReturned = player != null
               && !this.isExpectedUse(player)
               && this.positionsRestored(player)
               && (this.sawExpectedUse || this.activeTicks >= 5);
            if (!alreadyReturned && InventorySwap.isBusy()) {
               InventorySwap.abort();
            }

            this.releaseExpectedUse(client, player);
            this.restoreIfNeeded(client, player);
            this.clear();
         }
      }
   }

   private boolean tickDirect(class_310 client, class_746 player) {
      if (!this.useIssued) {
         if (this.directDelayTicks > 0) {
            this.directDelayTicks--;
         }

         if (this.directDelayTicks == 0) {
            this.issueDirectUse(client, player);
         }
      }

      if (!this.useIssued) {
         return true;
      } else if (this.waitForCompletion && this.isExpectedUse(player)) {
         this.sawExpectedUse = true;
         client.field_1690.field_1904.method_23481(true);
         this.syntheticUseHeld = true;
         return true;
      } else {
         this.releaseSyntheticUse(client);
         this.clear();
         return false;
      }
   }

   private void issueDirectUse(class_310 client, class_746 player) {
      class_1799 held = player.method_5998(this.directHand);
      if (!sameItemAndComponents(held, this.expectedStack)) {
         this.useIssued = true;
      } else {
         client.field_1761.method_2919(player, this.directHand);
         player.method_6104(this.directHand);
         this.useIssued = true;
         if (this.waitForCompletion && this.isExpectedUse(player)) {
            this.sawExpectedUse = true;
            client.field_1690.field_1904.method_23481(true);
            this.syntheticUseHeld = true;
         }
      }
   }

   private void releaseExpectedUse(class_310 client, class_746 player) {
      if (client.field_1761 != null && player != null && this.isExpectedUse(player)) {
         client.field_1761.method_2897(player);
      }
   }

   private void releaseSyntheticUse(class_310 client) {
      if (this.syntheticUseHeld) {
         client.field_1690.field_1904.method_23481(false);
         this.syntheticUseHeld = false;
      }
   }

   private boolean restoreIfNeeded(class_310 client, class_746 player) {
      if (this.sourceSlot < 0) {
         return true;
      } else if (player == null
         || client.field_1761 == null
         || player.field_7512 != player.field_7498
         || !player.field_7498.method_40442(this.sourceSlot)
         || !player.field_7498.method_40442(this.destinationSlot)) {
         return false;
      } else if (this.positionsRestored(player)) {
         return true;
      } else {
         class_1799 sourceNow = player.field_7498.method_7611(this.sourceSlot).method_7677();
         class_1799 destinationNow = player.field_7498.method_7611(this.destinationSlot).method_7677();
         if (class_1799.method_7973(sourceNow, this.destinationBefore) && this.matchesConsumedRemainder(destinationNow, this.sourceBefore)) {
            client.field_1761.method_2906(player.field_7498.field_7763, this.sourceSlot, this.destinationHotbarSlot, class_1713.field_7791, player);
            return this.positionsRestored(player);
         } else {
            return false;
         }
      }
   }

   private boolean positionsRestored(class_746 player) {
      if (this.sourceSlot < 0) {
         return true;
      } else if (player != null && player.field_7498.method_40442(this.sourceSlot) && player.field_7498.method_40442(this.destinationSlot)) {
         class_1799 sourceNow = player.field_7498.method_7611(this.sourceSlot).method_7677();
         class_1799 destinationNow = player.field_7498.method_7611(this.destinationSlot).method_7677();
         return this.matchesConsumedRemainder(sourceNow, this.sourceBefore) && class_1799.method_7973(destinationNow, this.destinationBefore);
      } else {
         return false;
      }
   }

   private boolean isExpectedUse(class_746 player) {
      return player != null && player.method_6115() && sameItemAndComponents(player.method_6030(), this.expectedStack);
   }

   private boolean matchesConsumedRemainder(class_1799 current, class_1799 original) {
      if (current.method_7960()) {
         return original.method_7947() == 1 && this.expectedRemainder.method_7960();
      } else {
         return sameItemAndComponents(current, original)
            ? current.method_7947() > 0 && current.method_7947() <= original.method_7947()
            : original.method_7947() == 1 && sameItemAndComponents(current, this.expectedRemainder);
      }
   }

   private static boolean sameItemAndComponents(class_1799 first, class_1799 second) {
      return !first.method_7960() && !second.method_7960() && class_1799.method_31577(first, second);
   }

   private void clear() {
      this.mode = ConsumableUseController.Mode.IDLE;
      this.directHand = class_1268.field_5808;
      this.expectedStack = class_1799.field_8037;
      this.expectedRemainder = class_1799.field_8037;
      this.waitForCompletion = false;
      this.useIssued = false;
      this.sawExpectedUse = false;
      this.syntheticUseHeld = false;
      this.directDelayTicks = 0;
      this.activeTicks = 0;
      this.sourceSlot = -1;
      this.destinationSlot = -1;
      this.destinationHotbarSlot = -1;
      this.sourceBefore = class_1799.field_8037;
      this.destinationBefore = class_1799.field_8037;
   }

   @Environment(EnvType.CLIENT)
   private enum Mode {
      IDLE,
      DIRECT_HAND,
      INVENTORY_SWAP;
   }
}
