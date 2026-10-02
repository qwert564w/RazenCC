package org.ryzen.utils.inventory;

import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1703;
import net.minecraft.class_1735;
import net.minecraft.class_1799;

@Environment(EnvType.CLIENT)
public final class ContainerLootService {
   private static final int PLAYER_INVENTORY_SLOTS = 36;

   private ContainerLootService() {
   }

   public static int containerSlotCount(class_1703 menu) {
      return Math.max(0, menu.field_7761.size() - 36);
   }

   public static int findFirst(class_1703 menu, Predicate<class_1799> predicate) {
      Objects.requireNonNull(menu, "menu");
      Objects.requireNonNull(predicate, "predicate");
      int slots = containerSlotCount(menu);

      for (int index = 0; index < slots; index++) {
         class_1735 slot = menu.method_7611(index);
         if (slot.method_7681() && predicate.test(slot.method_7677())) {
            return index;
         }
      }

      return -1;
   }

   public static boolean quickMoveFirst(class_1703 menu, Predicate<class_1799> predicate) {
      int slot = findFirst(menu, predicate);
      if (slot < 0) {
         return false;
      }

      InventoryUtil.quickMoveSlot(slot);
      return true;
   }
}
