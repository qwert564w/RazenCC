package org.ryzen.feature.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_2680;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class AutoToolFeature extends Feature {
   private static final int HOTBAR_SIZE = 9;
   private static final int INVENTORY_SIZE = 36;
   public final BooleanSetting useInventory = this.register(new BooleanSetting("Use Inventory", true));
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Silent", "Silent", "Normal"));
   private int originalHotbarSlot = -1;
   private int swappedInventorySlot = -1;
   private int silentServerSlot = -1;
   private int lastClientSlot = -1;

   public AutoToolFeature() {
      super("AutoTool", "Picks the best tool for the targeted block", FeatureCategory.PLAYER, -1);
   }

   @Override
   protected void onDisable() {
      this.restore();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null && !player.method_68878() && client.field_1755 == null) {
         this.dropSilentSlotOnManualSwitch(player);
         if (!this.isBreakingBlock(client)) {
            this.restore();
         } else {
            class_3965 hit = (class_3965)client.field_1765;
            class_2680 state = client.field_1687.method_8320(hit.method_17777());
            int bestSlot = this.findBestToolSlot(player, state);
            if (bestSlot != -1) {
               if (this.originalHotbarSlot == -1) {
                  this.originalHotbarSlot = player.method_31548().method_67532();
               }

               if (bestSlot < 9) {
                  this.selectHotbarTool(player, bestSlot);
               } else {
                  if (this.swappedInventorySlot != bestSlot) {
                     this.restoreSilentServerSlot(player);
                     this.restoreSwappedItem();
                     InventoryUtil.swapWithHotbar(bestSlot, this.originalHotbarSlot);
                     this.swappedInventorySlot = bestSlot;
                  }

                  player.method_31548().method_61496(this.originalHotbarSlot);
                  this.lastClientSlot = player.method_31548().method_67532();
               }
            }
         }
      } else {
         this.restore();
      }
   }

   private void dropSilentSlotOnManualSwitch(class_746 player) {
      int clientSlot = player.method_31548().method_67532();
      if (this.lastClientSlot != clientSlot) {
         this.lastClientSlot = clientSlot;
         if (this.silentServerSlot != -1) {
            this.silentServerSlot = -1;
            this.originalHotbarSlot = clientSlot;
         }
      }
   }

   public boolean swapForBlock(class_2680 state) {
      class_746 player = class_310.method_1551().field_1724;
      if (this.isEnabled() && player != null && state != null) {
         int bestSlot = this.findBestToolSlot(player, state);
         if (bestSlot < 0) {
            return false;
         }

         if (this.originalHotbarSlot == -1) {
            this.originalHotbarSlot = player.method_31548().method_67532();
         }

         if (bestSlot < 9) {
            this.selectHotbarTool(player, bestSlot);
         } else if (this.useInventory.getValue()) {
            this.restoreSilentServerSlot(player);
            this.restoreSwappedItem();
            InventoryUtil.swapWithHotbar(bestSlot, this.originalHotbarSlot);
            this.swappedInventorySlot = bestSlot;
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isBreakingBlock(class_310 client) {
      return client.field_1690.field_1886.method_1434() && client.field_1765 != null && client.field_1765.method_17783() == class_240.field_1332;
   }

   private int findBestToolSlot(class_746 player, class_2680 state) {
      int limit = this.useInventory.getValue() ? 36 : 9;
      int bestSlot = -1;
      float bestSpeed = 1.0F;

      for (int slot = 0; slot < limit; slot++) {
         class_1799 stack = player.method_31548().method_5438(slot);
         if (!stack.method_7960()) {
            float speed = stack.method_7924(state);
            if (speed > bestSpeed) {
               bestSpeed = speed;
               bestSlot = slot;
            }
         }
      }

      return bestSlot;
   }

   private void selectHotbarTool(class_746 player, int slot) {
      if (this.mode.is("Normal")) {
         this.restoreSilentServerSlot(player);
         player.method_31548().method_61496(slot);
      } else if (slot == player.method_31548().method_67532()) {
         this.restoreSilentServerSlot(player);
      } else {
         if (this.silentServerSlot != slot) {
            player.field_3944.method_52787(new class_2868(slot));
            this.silentServerSlot = slot;
         }
      }
   }

   private void restore() {
      if (this.originalHotbarSlot != -1) {
         class_746 player = class_310.method_1551().field_1724;
         this.restoreSwappedItem();
         if (player != null) {
            this.restoreSilentServerSlot(player);
            player.method_31548().method_61496(this.originalHotbarSlot);
            this.lastClientSlot = player.method_31548().method_67532();
         }

         this.originalHotbarSlot = -1;
      }
   }

   private void restoreSwappedItem() {
      if (this.swappedInventorySlot != -1) {
         InventoryUtil.swapWithHotbar(this.swappedInventorySlot, this.originalHotbarSlot);
         this.swappedInventorySlot = -1;
      }
   }

   private void restoreSilentServerSlot(class_746 player) {
      if (this.silentServerSlot != -1) {
         player.field_3944.method_52787(new class_2868(player.method_31548().method_67532()));
         this.silentServerSlot = -1;
      }
   }
}
