package org.ryzen.feature.impl.pve.autowarden;

import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_310;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_9334;
import org.ryzen.utils.inventory.ContainerLootService;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
final class AutoWardenInventory {
   private static final int PLAYER_INVENTORY_SIZE = 36;
   private static final Set<class_1792> RESTOCKABLE_FOOD = Set.of(
      class_1802.field_8176,
      class_1802.field_8261,
      class_1802.field_8544,
      class_1802.field_8347,
      class_1802.field_8752,
      class_1802.field_8071,
      class_1802.field_8229,
      class_1802.field_8512
   );

   private AutoWardenInventory() {
   }

   static int countInvisibility(class_746 player) {
      return count(player, AutoWardenInventory::isInvisibilityPotion);
   }

   static int countSpeed(class_746 player) {
      return count(player, AutoWardenInventory::isSpeedPotion);
   }

   static int countFood(class_746 player) {
      return count(player, AutoWardenInventory::isRestockableFood);
   }

   static int freeSlots(class_746 player) {
      int free = 0;

      for (int index = 0; index < 36; index++) {
         if (player.method_31548().method_5438(index).method_7960()) {
            free++;
         }
      }

      return free;
   }

   static int countValuables(class_746 player) {
      return count(player, AutoWardenInventory::isValuable);
   }

   static boolean carryingValuables(class_746 player) {
      for (int index = 0; index < 36; index++) {
         if (isValuable(player.method_31548().method_5438(index))) {
            return true;
         }
      }

      return false;
   }

   static boolean isInvisibilityPotion(class_1799 stack) {
      return isPotionWith(stack, class_1294.field_5905, false);
   }

   static boolean isSpeedPotion(class_1799 stack) {
      return isPotionWith(stack, class_1294.field_5904, true);
   }

   static boolean isRestockableFood(class_1799 stack) {
      return stack != null && !stack.method_7960() && stack.method_57826(class_9334.field_50075) && RESTOCKABLE_FOOD.contains(stack.method_7909());
   }

   static boolean isSupply(class_1799 stack) {
      return isInvisibilityPotion(stack) || isSpeedPotion(stack) || isRestockableFood(stack);
   }

   static boolean isValuable(class_1799 stack) {
      return stack != null
         && !stack.method_7960()
         && !stack.method_31574(class_1802.field_8366)
         && !stack.method_31574(class_1802.field_8469)
         && !isSupply(stack);
   }

   static boolean quickMoveFirstContainerItem(class_1703 menu, Predicate<class_1799> predicate) {
      return ContainerLootService.quickMoveFirst(menu, predicate);
   }

   static boolean quickMoveFirstPlayerItem(class_1703 menu, Predicate<class_1799> predicate) {
      int containerSlots = ContainerLootService.containerSlotCount(menu);

      for (int slotId = containerSlots; slotId < menu.field_7761.size(); slotId++) {
         class_1735 slot = menu.method_7611(slotId);
         if (slot.method_7681() && predicate.test(slot.method_7677())) {
            return InventoryUtil.quickMoveSlot(slotId);
         }
      }

      return false;
   }

   static int findPlayerMenuSlot(class_746 player, Predicate<class_1799> predicate) {
      return InventoryUtil.findPlayerMenuSlot(player, predicate);
   }

   static int findEmptyHotbarSlot(class_746 player, int excludedSlot) {
      for (int slot = 0; slot < 9; slot++) {
         if (slot != excludedSlot && player.method_31548().method_5438(slot).method_7960()) {
            return slot;
         }
      }

      return -1;
   }

   static int findSafeHotbarSlot(class_746 player, int excludedSlot) {
      int empty = findEmptyHotbarSlot(player, excludedSlot);
      if (empty >= 0) {
         return empty;
      }

      for (int slot = 0; slot < 9; slot++) {
         class_1799 stack = player.method_31548().method_5438(slot);
         if (slot != excludedSlot && !stack.method_31574(class_1802.field_8366) && !isSupply(stack)) {
            return slot;
         }
      }

      return -1;
   }

   static boolean dropFirstBottle(class_746 player) {
      class_310 client = class_310.method_1551();
      if (client.field_1761 != null && player.field_7512 == player.field_7498) {
         int menuSlot = InventoryUtil.findPlayerMenuSlot(player, stack -> stack.method_31574(class_1802.field_8469));
         if (menuSlot < 0) {
            return false;
         }

         client.field_1761.method_2906(player.field_7498.field_7763, menuSlot, 1, class_1713.field_7795, player);
         return true;
      } else {
         return false;
      }
   }

   private static int count(class_746 player, Predicate<class_1799> predicate) {
      int count = 0;

      for (int index = 0; index < 36; index++) {
         class_1799 stack = player.method_31548().method_5438(index);
         if (predicate.test(stack)) {
            count += stack.method_7947();
         }
      }

      return count;
   }

   private static boolean isPotionWith(class_1799 stack, class_6880<class_1291> wanted, boolean drinkableOnly) {
      if (stack == null || stack.method_7960()) {
         return false;
      }

      if (drinkableOnly && !stack.method_31574(class_1802.field_8574)) {
         return false;
      }

      if (!stack.method_31574(class_1802.field_8574) && !stack.method_31574(class_1802.field_8436) && !stack.method_31574(class_1802.field_8150)) {
         return false;
      }

      class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
      if (contents == null) {
         return false;
      }

      int effects = 0;
      boolean found = false;

      for (class_1293 effect : contents.method_57397()) {
         effects++;
         if (effect.method_5579().equals(wanted)) {
            found = true;
         }
      }

      return found && (!drinkableOnly || effects >= 1);
   }
}
