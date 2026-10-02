package org.ryzen.pve.economy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.OptionalLong;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1703;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import org.ryzen.utils.inventory.ContainerLootService;

@Environment(EnvType.CLIENT)
public final class AuctionPriceScanner {
   private AuctionPriceScanner() {
   }

   public static OptionalLong competitivePrice(class_1703 menu, class_1792 target, String query, int saleCount) {
      if (menu != null && target != null && saleCount > 0) {
         ArrayList<Long> unitPrices = new ArrayList<>();
         int containerSlots = ContainerLootService.containerSlotCount(menu);

         for (int slotId = 0; slotId < containerSlots; slotId++) {
            if (menu.method_40442(slotId)) {
               class_1799 stack = menu.method_7611(slotId).method_7677();
               if (!stack.method_7960() && (stack.method_31574(target) || EconomyItemText.containsAny(stack, query))) {
                  EconomyItemText.listingUnitPrice(stack).ifPresent(unitPrices::add);
               }
            }
         }

         if (unitPrices.isEmpty()) {
            return OptionalLong.empty();
         }

         Collections.sort(unitPrices);
         long unitMedian = unitPrices.get(unitPrices.size() / 2);

         long total;
         try {
            total = Math.multiplyExact(unitMedian, saleCount);
         } catch (ArithmeticException ignored) {
            total = 2147483647L;
         }

         long competitive = Math.max(1L, Math.min(2147483647L, total - 1L));
         return OptionalLong.of(competitive);
      } else {
         return OptionalLong.empty();
      }
   }
}
