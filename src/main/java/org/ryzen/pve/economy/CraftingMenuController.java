package org.ryzen.pve.economy;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1713;
import net.minecraft.class_1714;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public final class CraftingMenuController {
   private static final int RESULT_SLOT = 0;
   private static final int GRID_FIRST = 1;
   private static final int GRID_LAST = 9;
   private static final int PLAYER_FIRST = 10;
   private int sourceSlot = -1;
   private int actionCount;
   private int resultWaitTicks;

   public CraftingMenuController.Result tick(class_310 client, class_1714 menu, CraftingMenuController.Recipe recipe) {
      if (EconomyMenus.isCurrent(client, menu) && menu.field_7761.size() > 9 && this.actionCount++ <= 120) {
         int invalidGrid = invalidGridSlot(menu, recipe);
         if (invalidGrid < 0) {
            int missingGrid = missingGridSlot(menu, recipe);
            if (missingGrid >= 0) {
               class_1792 expected = recipe.ingredient(missingGrid - 1);
               class_1799 carried = menu.method_34255();
               if (!carried.method_7960() && !carried.method_31574(expected)) {
                  return this.returnCarried(client, menu) ? CraftingMenuController.Result.IN_PROGRESS : CraftingMenuController.Result.FAILED;
               }

               if (carried.method_7960()) {
                  int ingredient = findIngredient(menu, expected);
                  if (ingredient < 0) {
                     return CraftingMenuController.Result.FAILED;
                  }

                  this.sourceSlot = ingredient;
                  return EconomyMenus.click(client, menu, ingredient, 0, class_1713.field_7790)
                     ? CraftingMenuController.Result.IN_PROGRESS
                     : CraftingMenuController.Result.FAILED;
               } else {
                  return EconomyMenus.click(client, menu, missingGrid, 1, class_1713.field_7790)
                     ? CraftingMenuController.Result.IN_PROGRESS
                     : CraftingMenuController.Result.FAILED;
               }
            } else if (!menu.method_34255().method_7960()) {
               return this.returnCarried(client, menu) ? CraftingMenuController.Result.IN_PROGRESS : CraftingMenuController.Result.FAILED;
            } else {
               class_1799 result = menu.method_7611(0).method_7677();
               if (result.method_31574(recipe.output())) {
                  this.resultWaitTicks = 0;
                  return EconomyMenus.quickMove(client, menu, 0) ? CraftingMenuController.Result.CRAFTED : CraftingMenuController.Result.FAILED;
               } else {
                  return ++this.resultWaitTicks <= 20 ? CraftingMenuController.Result.IN_PROGRESS : CraftingMenuController.Result.FAILED;
               }
            }
         } else if (!menu.method_34255().method_7960()) {
            return this.returnCarried(client, menu) ? CraftingMenuController.Result.IN_PROGRESS : CraftingMenuController.Result.FAILED;
         } else {
            return EconomyMenus.quickMove(client, menu, invalidGrid) ? CraftingMenuController.Result.IN_PROGRESS : CraftingMenuController.Result.FAILED;
         }
      } else {
         return CraftingMenuController.Result.FAILED;
      }
   }

   public boolean cleanup(class_310 client, class_1714 menu) {
      return menu != null && !menu.method_34255().method_7960() ? this.returnCarried(client, menu) : true;
   }

   public void reset() {
      this.sourceSlot = -1;
      this.actionCount = 0;
      this.resultWaitTicks = 0;
   }

   private boolean returnCarried(class_310 client, class_1714 menu) {
      class_1799 carried = menu.method_34255();
      if (carried.method_7960()) {
         this.sourceSlot = -1;
         return true;
      }

      if (canAccept(menu, this.sourceSlot, carried)) {
         boolean clicked = EconomyMenus.click(client, menu, this.sourceSlot, 0, class_1713.field_7790);
         if (clicked) {
            this.sourceSlot = -1;
         }

         return clicked;
      } else {
         for (int slotId = 10; slotId < menu.field_7761.size(); slotId++) {
            if (canAccept(menu, slotId, carried)) {
               boolean clicked = EconomyMenus.click(client, menu, slotId, 0, class_1713.field_7790);
               if (clicked) {
                  this.sourceSlot = -1;
               }

               return clicked;
            }
         }

         return false;
      }
   }

   private static boolean canAccept(class_1714 menu, int slotId, class_1799 carried) {
      if (menu.method_40442(slotId) && slotId >= 10) {
         class_1735 slot = menu.method_7611(slotId);
         class_1799 existing = slot.method_7677();
         return slot.method_7680(carried)
            && (existing.method_7960() || class_1799.method_31577(existing, carried) && existing.method_7947() < existing.method_7914());
      } else {
         return false;
      }
   }

   private static int invalidGridSlot(class_1714 menu, CraftingMenuController.Recipe recipe) {
      for (int slotId = 1; slotId <= 9; slotId++) {
         class_1799 stack = menu.method_7611(slotId).method_7677();
         class_1792 expected = recipe.ingredient(slotId - 1);
         if (!stack.method_7960() && (!stack.method_31574(expected) || stack.method_7947() != 1)) {
            return slotId;
         }
      }

      return -1;
   }

   private static int missingGridSlot(class_1714 menu, CraftingMenuController.Recipe recipe) {
      for (int slotId = 1; slotId <= 9; slotId++) {
         if (!menu.method_7611(slotId).method_7677().method_31574(recipe.ingredient(slotId - 1))) {
            return slotId;
         }
      }

      return -1;
   }

   private static int findIngredient(class_1714 menu, class_1792 item) {
      for (int slotId = 10; slotId < menu.field_7761.size(); slotId++) {
         if (menu.method_7611(slotId).method_7677().method_31574(item)) {
            return slotId;
         }
      }

      return -1;
   }

   @Environment(EnvType.CLIENT)
   public enum Recipe {
      ENCHANTED_GOLDEN_APPLE(
         class_1802.field_8494,
         class_1802.field_8494,
         class_1802.field_8494,
         class_1802.field_8494,
         class_1802.field_8279,
         class_1802.field_8494,
         class_1802.field_8494,
         class_1802.field_8494,
         class_1802.field_8494
      ),
      GOLD_BLOCK(
         class_1802.field_8695,
         class_1802.field_8695,
         class_1802.field_8695,
         class_1802.field_8695,
         class_1802.field_8695,
         class_1802.field_8695,
         class_1802.field_8695,
         class_1802.field_8695,
         class_1802.field_8695
      );

      private final class_1792[] ingredients;

      Recipe(class_1792... ingredients) {
         this.ingredients = ingredients;
      }

      class_1792 ingredient(int index) {
         return this.ingredients[index];
      }

      class_1792 output() {
         return this == ENCHANTED_GOLDEN_APPLE ? class_1802.field_8367 : class_1802.field_8494;
      }
   }

   @Environment(EnvType.CLIENT)
   public enum Result {
      IN_PROGRESS,
      CRAFTED,
      FAILED;
   }
}
