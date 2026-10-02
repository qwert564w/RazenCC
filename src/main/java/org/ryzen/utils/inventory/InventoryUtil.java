package org.ryzen.utils.inventory;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_5536;
import net.minecraft.class_636;
import net.minecraft.class_746;
import org.ryzen.context.InventoryContext;

@Environment(EnvType.CLIENT)
public final class InventoryUtil implements InventoryContext {
   private static final InventoryUtil CONTEXT = new InventoryUtil();
   private static final int INVENTORY_SLOTS_START = 9;

   private InventoryUtil() {
   }

   public static int findPlayerMenuSlot(class_746 player, Predicate<class_1799> predicate) {
      for (int slot = 36; slot < 45; slot++) {
         if (predicate.test(player.field_7498.method_7611(slot).method_7677())) {
            return slot;
         }
      }

      for (int slot = 9; slot < 36; slot++) {
         if (predicate.test(player.field_7498.method_7611(slot).method_7677())) {
            return slot;
         }
      }

      return -1;
   }

   public static int findInventorySlot(class_746 player, Predicate<class_1799> predicate) {
      for (int slot = 9; slot < 36; slot++) {
         if (predicate.test(player.field_7498.method_7611(slot).method_7677())) {
            return slot;
         }
      }

      return -1;
   }

   public static class_437 getCurrentScreen() {
      return CONTEXT.screen();
   }

   public static boolean isContainerScreenOpen() {
      return CONTEXT.hasContainerScreen();
   }

   public static class_465<?> getContainerScreen() {
      return CONTEXT.containerScreen();
   }

   public static class_1703 getOpenMenu() {
      return CONTEXT.menu();
   }

   public static boolean hasOpenMenu() {
      return CONTEXT.hasMenu();
   }

   public static boolean clickSlot(int slotId, class_5536 clickAction, class_1713 input) {
      return clickSlot(slotId, clickButton(clickAction), input);
   }

   public static boolean clickSlot(int slotId, int button, class_1713 input) {
      class_1703 menu = getOpenMenu();
      return menu != null && clickMenu(menu, slotId, button, input);
   }

   public static boolean leftClickSlot(int slotId) {
      return clickSlot(slotId, class_5536.field_27013, class_1713.field_7790);
   }

   public static boolean rightClickSlot(int slotId) {
      return clickSlot(slotId, class_5536.field_27014, class_1713.field_7790);
   }

   public static boolean quickMoveSlot(int slotId) {
      return clickSlot(slotId, 0, class_1713.field_7794);
   }

   public static boolean swapWithHotbar(int slotId, int hotbarSlot) {
      return hotbarSlot >= 0 && hotbarSlot <= 8 ? clickSlot(slotId, hotbarSlot, class_1713.field_7791) : false;
   }

   public static boolean dropOne(int slotId) {
      return clickSlot(slotId, 0, class_1713.field_7795);
   }

   public static boolean dropStack(int slotId) {
      return clickSlot(slotId, 1, class_1713.field_7795);
   }

   public static boolean dropPlayerStack(class_746 player, int slotId) {
      class_310 client = class_310.method_1551();
      if (client.field_1761 != null
         && player != null
         && player.field_7512 == player.field_7498
         && player.field_7498.method_40442(slotId)
         && !player.field_7498.method_7611(slotId).method_7677().method_7960()) {
         client.field_1761.method_2906(player.field_7498.field_7763, slotId, 1, class_1713.field_7795, player);
         return true;
      } else {
         return false;
      }
   }

   public static boolean pickupAll(int slotId) {
      return clickSlot(slotId, 0, class_1713.field_7793);
   }

   public static boolean pickupOutside() {
      class_1703 menu = getOpenMenu();
      return menu != null && clickMenu(menu, -999, 0, class_1713.field_7790);
   }

   public static boolean moveStack(int fromSlotId, int toSlotId) {
      class_1703 menu = getOpenMenu();
      if (menu == null || fromSlotId == toSlotId) {
         return false;
      }

      if (!menu.method_40442(fromSlotId) || !menu.method_40442(toSlotId)) {
         return false;
      }

      if (!menu.method_34255().method_7960()) {
         return false;
      }

      class_1735 fromSlot = menu.method_7611(fromSlotId);
      if (fromSlot == null || !fromSlot.method_7681()) {
         return false;
      }

      if (!clickMenu(menu, fromSlotId, 0, class_1713.field_7790)) {
         return false;
      }

      if (menu.method_34255().method_7960()) {
         return false;
      }

      if (!clickMenu(menu, toSlotId, 0, class_1713.field_7790)) {
         clickMenu(menu, fromSlotId, 0, class_1713.field_7790);
         return false;
      }

      if (!menu.method_34255().method_7960()) {
         clickMenu(menu, fromSlotId, 0, class_1713.field_7790);
      }

      return true;
   }

   public static boolean hasCarriedItem() {
      class_1703 menu = getOpenMenu();
      return menu != null && !menu.method_34255().method_7960();
   }

   public static class_1799 getCarriedItem() {
      class_1703 menu = getOpenMenu();
      return menu != null ? menu.method_34255() : class_1799.field_8037;
   }

   public static class_1799 getSlotItem(int slotId) {
      class_1703 menu = getOpenMenu();
      return menu != null && menu.method_40442(slotId) ? menu.method_7611(slotId).method_7677() : class_1799.field_8037;
   }

   private static boolean clickMenu(class_1703 menu, int slotId, int button, class_1713 input) {
      class_636 gameMode = CONTEXT.gameMode();
      class_746 player = CONTEXT.player();
      if (gameMode != null && player != null) {
         gameMode.method_2906(menu.field_7763, slotId, button, input, player);
         return true;
      } else {
         return false;
      }
   }

   private static int clickButton(class_5536 clickAction) {
      return clickAction == class_5536.field_27014 ? 1 : 0;
   }
}
