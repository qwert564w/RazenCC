package org.ryzen.pve.economy;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2868;
import net.minecraft.class_746;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class EconomyInventory {
   private EconomyInventory() {
   }

   public static int count(class_746 player, class_1792 item) {
      if (player != null && item != null) {
         int count = 0;

         for (int slot = 0; slot < 36; slot++) {
            class_1799 stack = player.method_31548().method_5438(slot);
            if (stack.method_31574(item)) {
               count += stack.method_7947();
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   public static int findHotbar(class_746 player, Predicate<class_1799> predicate) {
      if (player == null) {
         return -1;
      }

      for (int slot = 0; slot < 9; slot++) {
         if (predicate.test(player.method_31548().method_5438(slot))) {
            return slot;
         }
      }

      return -1;
   }

   public static boolean selectHotbar(class_746 player, int slot) {
      if (player != null && slot >= 0 && slot <= 8) {
         if (player.method_31548().method_67532() != slot) {
            player.method_31548().method_61496(slot);
            player.field_3944.method_52787(new class_2868(slot));
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean moveFirstToSelectedHotbar(class_746 player, Predicate<class_1799> predicate) {
      if (player != null && player.field_7512 == player.field_7498) {
         int menuSlot = InventoryUtil.findPlayerMenuSlot(player, predicate);
         if (menuSlot < 0) {
            return false;
         }

         int selected = player.method_31548().method_67532();
         return menuSlot >= 36 && menuSlot <= 44 ? selectHotbar(player, menuSlot - 36) : InventoryUtil.swapWithHotbar(menuSlot, selected);
      } else {
         return false;
      }
   }

   public static boolean hasFreeSlot(class_746 player) {
      if (player == null) {
         return false;
      }

      for (int slot = 0; slot < 36; slot++) {
         if (player.method_31548().method_5438(slot).method_7960()) {
            return true;
         }
      }

      return false;
   }
}
