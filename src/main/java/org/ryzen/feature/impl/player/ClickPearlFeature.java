package org.ryzen.feature.impl.player;

import java.util.EnumSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1802;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.pve.AutomationOwner;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.utils.inventory.InventorySwap;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class ClickPearlFeature extends Feature implements AutomationOwner {
   private static final int HOTBAR_SIZE = 9;
   public final InputBindSetting key = this.register(new InputBindSetting("Key", -1));
   private boolean throwQueued;
   private boolean releasePending;
   private ClickPearlFeature.Stage stage = ClickPearlFeature.Stage.IDLE;
   private int restoreSlot = -1;

   public ClickPearlFeature() {
      super("ClickPearl", "Quickly throws an ender pearl", FeatureCategory.PLAYER, -1);
   }

   @Override
   protected void onDisable() {
      this.throwQueued = false;
      this.releasePending = false;
      this.finishHotbarThrow(class_310.method_1551().field_1724);
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (event.getAction() == 1 && this.key.matches(event.getKey())) {
         this.queueThrow();
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      if (event.getAction() == 1 && this.key.matchesMouse(event.getButton()) && this.queueThrow()) {
         event.cancel();
      }
   }

   private boolean queueThrow() {
      class_310 client = class_310.method_1551();
      if (client.field_1755 == null
         && client.field_1724 != null
         && PveAutomationCoordinator.INSTANCE
            .acquire(this, AutomationPriority.EMERGENCY, EnumSet.of(AutomationResource.INVENTORY, AutomationResource.ROTATION))) {
         this.throwQueued = true;
         return true;
      } else {
         return false;
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (this.stage != ClickPearlFeature.Stage.IDLE) {
         this.advanceHotbarThrow(event.getClient());
      } else {
         if (this.releasePending && !InventorySwap.isBusy()) {
            this.releasePending = false;
            PveAutomationCoordinator.INSTANCE.release(this);
         }

         if (this.throwQueued) {
            this.throwQueued = false;
            boolean asyncSwap = false;

            try {
               class_310 client = event.getClient();
               class_746 player = client.field_1724;
               if (player == null || client.field_1761 == null) {
                  return;
               }

               if (player.method_6047().method_31574(class_1802.field_8634)) {
                  this.throwFromMainHand(client, player);
                  return;
               }

               int hotbarSlot = this.findPearlHotbarSlot(player);
               if (hotbarSlot == -1) {
                  int containerSlot = this.findPearlContainerSlot(player);
                  if (containerSlot != -1) {
                     InventorySwap.useFromSlot(containerSlot);
                     this.releasePending = true;
                     asyncSwap = true;
                  }

                  return;
               }

               this.restoreSlot = player.method_31548().method_67532();
               player.method_31548().method_61496(hotbarSlot);
               this.stage = ClickPearlFeature.Stage.THROW;
               asyncSwap = true;
            } finally {
               if (!asyncSwap) {
                  PveAutomationCoordinator.INSTANCE.release(this);
               }
            }
         }
      }
   }

   private void advanceHotbarThrow(class_310 client) {
      class_746 player = client.field_1724;
      if (player == null || client.field_1761 == null) {
         this.finishHotbarThrow(player);
      } else if (this.stage == ClickPearlFeature.Stage.THROW) {
         if (player.method_6047().method_31574(class_1802.field_8634)) {
            this.throwFromMainHand(client, player);
         }

         this.stage = ClickPearlFeature.Stage.RESTORE;
      } else {
         this.finishHotbarThrow(player);
      }
   }

   private void finishHotbarThrow(class_746 player) {
      if (player != null && this.restoreSlot != -1) {
         player.method_31548().method_61496(this.restoreSlot);
         player.field_3944.method_52787(new class_2868(this.restoreSlot));
      }

      this.restoreSlot = -1;
      this.stage = ClickPearlFeature.Stage.IDLE;
      PveAutomationCoordinator.INSTANCE.release(this);
   }

   private void throwFromMainHand(class_310 client, class_746 player) {
      client.field_1761.method_2919(player, class_1268.field_5808);
      player.method_6104(class_1268.field_5808);
   }

   private int findPearlHotbarSlot(class_746 player) {
      for (int slot = 0; slot < 9; slot++) {
         if (player.method_31548().method_5438(slot).method_31574(class_1802.field_8634)) {
            return slot;
         }
      }

      return -1;
   }

   private int findPearlContainerSlot(class_746 player) {
      return InventoryUtil.findInventorySlot(player, stack -> stack.method_31574(class_1802.field_8634));
   }

   @Environment(EnvType.CLIENT)
   private enum Stage {
      IDLE,
      THROW,
      RESTORE;
   }
}
