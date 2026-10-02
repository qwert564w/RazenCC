package org.ryzen.utils.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;

@Environment(EnvType.CLIENT)
public final class InventorySwap implements MinecraftContext {
   public static final InventorySwap INSTANCE = new InventorySwap();
   private static final int HOTBAR_START = 36;
   private static final int HOTBAR_END = 45;
   private static final int OFFHAND_BUTTON = 40;
   private InventorySwap.State state = InventorySwap.State.IDLE;
   private int sourceSlot = -1;
   private int hotbarButton = 40;
   private boolean useAndReturn;
   private boolean waitForUseCompletion;
   private boolean immediateReturn;
   private boolean directUse;
   private class_1713 clickInput = class_1713.field_7791;
   private boolean syntheticUseHeld;
   private int useWaitTicks;
   private static final int MAX_USE_WAIT_TICKS = 200;

   private InventorySwap() {
   }

   public static void equip(int containerSlot) {
      class_746 player = INSTANCE.player();
      if (player != null
         && INSTANCE.state == InventorySwap.State.IDLE
         && !DropAllInventoryController.blocksInventoryOperations()
         && player.field_7512 == player.field_7498) {
         if (containerSlot >= 36 && containerSlot < 45) {
            mc.field_1761.method_2906(player.field_7498.field_7763, containerSlot, 40, class_1713.field_7791, player);
         } else {
            INSTANCE.begin(containerSlot, 40, false, false, false, false, class_1713.field_7791);
         }
      }
   }

   public static void moveToHotbar(int containerSlot, int hotbarSlot) {
      if (INSTANCE.player() != null
         && INSTANCE.state == InventorySwap.State.IDLE
         && !DropAllInventoryController.blocksInventoryOperations()
         && hotbarSlot >= 0
         && hotbarSlot <= 8) {
         INSTANCE.begin(containerSlot, hotbarSlot, false, false, false, false, class_1713.field_7791);
      }
   }

   public static void useFromSlot(int containerSlot) {
      useFromSlot(containerSlot, false);
   }

   public static void useFromSlot(int containerSlot, boolean waitForUseCompletion) {
      useFromSlot(containerSlot, waitForUseCompletion, false);
   }

   public static void useFromSlot(int containerSlot, boolean waitForUseCompletion, boolean immediateReturn) {
      class_746 player = INSTANCE.player();
      if (player != null
         && INSTANCE.state == InventorySwap.State.IDLE
         && !DropAllInventoryController.blocksInventoryOperations()
         && player.field_7512 == player.field_7498) {
         INSTANCE.begin(containerSlot, player.method_31548().method_67532(), true, waitForUseCompletion, immediateReturn, false, class_1713.field_7791);
      }
   }

   public static void useSelected(boolean waitForUseCompletion) {
      class_746 player = INSTANCE.player();
      if (player != null
         && INSTANCE.state == InventorySwap.State.IDLE
         && !DropAllInventoryController.blocksInventoryOperations()
         && player.field_7512 == player.field_7498) {
         INSTANCE.begin(-1, player.method_31548().method_67532(), true, waitForUseCompletion, false, true, class_1713.field_7791);
      }
   }

   public static boolean dropStack(int containerSlot) {
      class_746 player = INSTANCE.player();
      if (player != null
         && INSTANCE.state == InventorySwap.State.IDLE
         && !DropAllInventoryController.blocksInventoryOperations()
         && player.field_7512 == player.field_7498
         && player.field_7498.method_40442(containerSlot)
         && !player.field_7498.method_7611(containerSlot).method_7677().method_7960()) {
         INSTANCE.begin(containerSlot, 1, false, false, false, false, class_1713.field_7795);
         return true;
      } else {
         return false;
      }
   }

   public static boolean isBusy() {
      return INSTANCE.state != InventorySwap.State.IDLE;
   }

   public static boolean shouldStopMovement() {
      return INSTANCE.state != InventorySwap.State.IDLE;
   }

   public static void abort() {
      INSTANCE.finish();
   }

   private void begin(
      int containerSlot,
      int hotbarButton,
      boolean useAndReturn,
      boolean waitForUseCompletion,
      boolean immediateReturn,
      boolean directUse,
      class_1713 clickInput
   ) {
      this.sourceSlot = containerSlot;
      this.hotbarButton = hotbarButton;
      this.useAndReturn = useAndReturn;
      this.waitForUseCompletion = waitForUseCompletion;
      this.immediateReturn = immediateReturn;
      this.directUse = directUse;
      this.clickInput = clickInput == null ? class_1713.field_7791 : clickInput;
      this.syntheticUseHeld = false;
      this.useWaitTicks = 0;
      this.state = InventorySwap.State.PREPARING;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (this.state != InventorySwap.State.IDLE) {
         class_746 player = this.player();
         if (player != null && (this.directUse || player.field_7498.method_40442(this.sourceSlot)) && player.field_7512 == player.field_7498) {
            switch (this.state) {
               case PREPARING:
                  this.state = InventorySwap.State.MOVING;
                  break;
               case MOVING:
                  if (!this.directUse) {
                     if (player.field_7498.method_7611(this.sourceSlot).method_7677().method_7960()) {
                        this.finish();
                        return;
                     }

                     this.click(player);
                  }

                  this.state = this.useAndReturn ? InventorySwap.State.USING : InventorySwap.State.SETTLING;
                  break;
               case USING:
                  int selected = player.method_31548().method_67532();
                  if (selected != this.hotbarButton) {
                     player.method_31548().method_61496(this.hotbarButton);
                  }

                  mc.field_1761.method_2919(player, class_1268.field_5808);
                  player.method_6104(class_1268.field_5808);
                  if (selected != this.hotbarButton) {
                     player.method_31548().method_61496(selected);
                  }

                  if (this.waitForUseCompletion && player.method_6115()) {
                     mc.field_1690.field_1904.method_23481(true);
                     this.syntheticUseHeld = true;
                     this.state = InventorySwap.State.WAITING_FOR_USE;
                  } else if (this.immediateReturn && !this.directUse) {
                     this.click(player);
                     this.state = InventorySwap.State.SETTLING;
                  } else {
                     this.state = InventorySwap.State.RETURNING;
                  }
                  break;
               case WAITING_FOR_USE:
                  this.useWaitTicks++;
                  if (!player.method_6115() || this.useWaitTicks >= 200) {
                     this.releaseSyntheticUse();
                     this.state = InventorySwap.State.RETURNING;
                  }
                  break;
               case RETURNING:
                  if (!this.directUse) {
                     this.click(player);
                  }

                  this.state = InventorySwap.State.SETTLING;
                  break;
               case SETTLING:
                  this.finish();
                  break;
               default:
                  this.finish();
            }
         } else {
            this.finish();
         }
      }
   }

   private void click(class_746 player) {
      mc.field_1761.method_2906(player.field_7498.field_7763, this.sourceSlot, this.hotbarButton, this.clickInput, player);
   }

   private void finish() {
      this.releaseSyntheticUse();
      this.state = InventorySwap.State.IDLE;
      this.sourceSlot = -1;
      this.hotbarButton = 40;
      this.useAndReturn = false;
      this.waitForUseCompletion = false;
      this.immediateReturn = false;
      this.directUse = false;
      this.clickInput = class_1713.field_7791;
      this.useWaitTicks = 0;
   }

   private void releaseSyntheticUse() {
      if (this.syntheticUseHeld) {
         mc.field_1690.field_1904.method_23481(false);
         this.syntheticUseHeld = false;
      }
   }

   @Environment(EnvType.CLIENT)
   private enum State {
      IDLE,
      PREPARING,
      MOVING,
      USING,
      WAITING_FOR_USE,
      RETURNING,
      SETTLING;
   }
}
