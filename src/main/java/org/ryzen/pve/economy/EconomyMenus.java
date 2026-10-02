package org.ryzen.pve.economy;

import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_465;
import org.ryzen.utils.inventory.ContainerLootService;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class EconomyMenus {
   private EconomyMenus() {
   }

   public static String title(class_310 client) {
      return client != null && client.field_1755 instanceof class_465<?> screen ? screen.method_25440().getString() : "";
   }

   public static boolean titleContains(class_310 client, String... markers) {
      return EconomyTextParser.containsAny(title(client), markers);
   }

   public static int currentContainerId(class_310 client) {
      return client != null && client.field_1724 != null && client.field_1724.field_7512 != null ? client.field_1724.field_7512.field_7763 : -1;
   }

   public static boolean isCurrent(class_310 client, class_1703 menu) {
      return client != null
         && client.field_1724 != null
         && menu != null
         && client.field_1724.field_7512 == menu
         && currentContainerId(client) == menu.field_7763;
   }

   public static int findContainerSlot(class_1703 menu, Predicate<class_1799> predicate) {
      Objects.requireNonNull(menu, "menu");
      Objects.requireNonNull(predicate, "predicate");
      int count = ContainerLootService.containerSlotCount(menu);

      for (int slotId = 0; slotId < count; slotId++) {
         if (menu.method_40442(slotId)) {
            class_1735 slot = menu.method_7611(slotId);
            if (slot != null && slot.method_7681() && predicate.test(slot.method_7677())) {
               return slotId;
            }
         }
      }

      return -1;
   }

   public static boolean click(class_310 client, class_1703 menu, int slotId, int button, class_1713 input) {
      return isCurrent(client, menu) && menu.method_40442(slotId) && input != null ? InventoryUtil.clickSlot(slotId, button, input) : false;
   }

   public static boolean quickMove(class_310 client, class_1703 menu, int slotId) {
      return click(client, menu, slotId, 0, class_1713.field_7794);
   }

   public static boolean closeOwned(class_310 client, int containerId) {
      if (client != null
         && client.field_1724 != null
         && containerId >= 0
         && currentContainerId(client) == containerId
         && client.field_1724.field_7512 != client.field_1724.field_7498) {
         client.field_1724.method_7346();
         return true;
      } else {
         return false;
      }
   }
}
