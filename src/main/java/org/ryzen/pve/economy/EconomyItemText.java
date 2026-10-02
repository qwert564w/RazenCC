package org.ryzen.pve.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_9290;
import net.minecraft.class_9334;

@Environment(EnvType.CLIENT)
public final class EconomyItemText {
   private EconomyItemText() {
   }

   public static List<String> lines(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         ArrayList<String> lines = new ArrayList<>();
         lines.add(stack.method_7964().getString());
         class_9290 lore = (class_9290)stack.method_58694(class_9334.field_49632);
         if (lore != null) {
            for (class_2561 line : lore.comp_2400()) {
               lines.add(line.getString());
            }
         }

         return List.copyOf(lines);
      } else {
         return List.of();
      }
   }

   public static String combined(class_1799 stack) {
      return String.join("\n", lines(stack));
   }

   public static boolean containsAny(class_1799 stack, String... markers) {
      return EconomyTextParser.containsAny(combined(stack), markers);
   }

   public static OptionalLong listingPrice(class_1799 stack) {
      long largest = -1L;

      for (String line : lines(stack)) {
         String normalized = EconomyTextParser.normalize(line);
         if (EconomyTextParser.containsAny(normalized, "price", "cost", "цена", "стоимость", "за все", "за штуку", "монет", "$")) {
            OptionalLong amount = EconomyTextParser.largestAmount(normalized);
            if (amount.isPresent()) {
               largest = Math.max(largest, amount.getAsLong());
            }
         }
      }

      return largest < 0L ? OptionalLong.empty() : OptionalLong.of(largest);
   }

   public static OptionalLong listingUnitPrice(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         long largest = -1L;

         for (String line : lines(stack)) {
            String normalized = EconomyTextParser.normalize(line);
            if (EconomyTextParser.containsAny(normalized, "price", "cost", "цена", "стоимость", "за все", "за штуку", "per item", "each", "монет", "$")) {
               OptionalLong parsed = EconomyTextParser.largestAmount(normalized);
               if (!parsed.isEmpty()) {
                  boolean explicitlyPerItem = EconomyTextParser.containsAny(normalized, "за штуку", "per item", "each", "1 шт");
                  long unit = explicitlyPerItem ? parsed.getAsLong() : Math.max(1L, parsed.getAsLong() / Math.max(1, stack.method_7947()));
                  largest = Math.max(largest, unit);
               }
            }
         }

         return largest < 0L ? OptionalLong.empty() : OptionalLong.of(largest);
      } else {
         return OptionalLong.empty();
      }
   }
}
