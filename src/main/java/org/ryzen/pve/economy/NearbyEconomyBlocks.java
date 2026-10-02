package org.ryzen.pve.economy;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2561;
import net.minecraft.class_2625;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2745;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_8242;
import net.minecraft.class_2338.class_2339;

@Environment(EnvType.CLIENT)
public final class NearbyEconomyBlocks {
   private NearbyEconomyBlocks() {
   }

   public static class_2338 nearestCraftingTable(class_638 level, class_746 player, int radius) {
      return nearest(level, player, radius, Math.min(16, radius), state -> state.method_27852(class_2246.field_9980));
   }

   public static class_2338 nearestSignedChest(class_638 level, class_746 player, int radius, String label) {
      String target = EconomyTextParser.normalize(label);
      return target.isEmpty()
         ? null
         : nearest(
            level,
            player,
            radius,
            Math.min(16, radius),
            state -> state.method_27852(class_2246.field_10034) || state.method_27852(class_2246.field_10380) || state.method_27852(class_2246.field_16328),
            target
         );
   }

   public static boolean adjacentSignContains(class_638 level, class_2338 block, String label) {
      if (level != null && block != null && !EconomyTextParser.normalize(label).isEmpty()) {
         for (class_2350 direction : class_2350.values()) {
            if (level.method_8321(block.method_10093(direction)) instanceof class_2625 sign
               && (contains(sign.method_49853(), label) || contains(sign.method_49854(), label))) {
               return true;
            }
         }

         class_2680 state = level.method_8320(block);
         if (state.method_26204() instanceof class_2281 && state.method_28498(class_2741.field_12506) && state.method_28498(class_2741.field_12481)) {
            class_2745 type = (class_2745)state.method_11654(class_2741.field_12506);
            if (type != class_2745.field_12569) {
               class_2350 facing = (class_2350)state.method_11654(class_2741.field_12481);
               class_2350 partnerDirection = type == class_2745.field_12574 ? facing.method_10170() : facing.method_10160();
               class_2338 partner = block.method_10093(partnerDirection);

               for (class_2350 direction : class_2350.values()) {
                  if (level.method_8321(partner.method_10093(direction)) instanceof class_2625 sign
                     && (contains(sign.method_49853(), label) || contains(sign.method_49854(), label))) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static class_2338 nearest(class_638 level, class_746 player, int horizontalRadius, int verticalRadius, Predicate<class_2680> predicate) {
      return nearest(level, player, horizontalRadius, verticalRadius, predicate, null);
   }

   private static class_2338 nearest(
      class_638 level, class_746 player, int horizontalRadius, int verticalRadius, Predicate<class_2680> predicate, String requiredSign
   ) {
      if (level != null && player != null) {
         int horizontal = Math.max(1, Math.min(32, horizontalRadius));
         int vertical = Math.max(1, Math.min(16, verticalRadius));
         class_2338 origin = player.method_24515();
         class_2339 cursor = new class_2339();
         class_2338 nearest = null;
         double nearestDistance = Double.POSITIVE_INFINITY;

         for (int y = -vertical; y <= vertical; y++) {
            for (int x = -horizontal; x <= horizontal; x++) {
               for (int z = -horizontal; z <= horizontal; z++) {
                  if (x * x + z * z <= horizontal * horizontal) {
                     cursor.method_10103(origin.method_10263() + x, origin.method_10264() + y, origin.method_10260() + z);
                     if (level.method_8393(cursor.method_10263() >> 4, cursor.method_10260() >> 4)) {
                        class_2680 state = level.method_8320(cursor);
                        if (predicate.test(state) && (requiredSign == null || adjacentSignContains(level, cursor, requiredSign))) {
                           double distance = cursor.method_10268(player.method_23317(), player.method_23318() + player.method_5751(), player.method_23321());
                           if (distance < nearestDistance) {
                              nearestDistance = distance;
                              nearest = cursor.method_10062();
                           }
                        }
                     }
                  }
               }
            }
         }

         return nearest;
      } else {
         return null;
      }
   }

   private static boolean contains(class_8242 text, String label) {
      if (text == null) {
         return false;
      }

      for (class_2561 line : text.method_49877(false)) {
         if (line != null && EconomyTextParser.containsAny(line.getString(), label)) {
            return true;
         }
      }

      return false;
   }
}
