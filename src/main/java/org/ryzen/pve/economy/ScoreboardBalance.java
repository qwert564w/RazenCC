package org.ryzen.pve.economy;

import java.util.OptionalLong;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_746;
import net.minecraft.class_8646;
import net.minecraft.class_9011;

@Environment(EnvType.CLIENT)
public final class ScoreboardBalance {
   private static final String[] LABELS = new String[]{"balance", "money", "coins", "баланс", "монет"};

   private ScoreboardBalance() {
   }

   public static OptionalLong read(class_746 player) {
      if (player == null) {
         return OptionalLong.empty();
      }

      class_269 scoreboard = player.method_73183().method_8428();
      class_266 objective = scoreboard.method_1189(class_8646.field_45157);
      if (objective == null) {
         return OptionalLong.empty();
      }

      for (class_9011 entry : scoreboard.method_1184(objective)) {
         if (!entry.method_55385()) {
            class_2561 name = (class_2561)(entry.comp_2129() == null ? class_2561.method_43470(entry.comp_2127()) : entry.comp_2129());
            class_268 team = scoreboard.method_1164(entry.comp_2127());
            String line = class_268.method_1142(team, name).getString();
            if (EconomyTextParser.containsAny(line, LABELS)) {
               OptionalLong amount = EconomyTextParser.amountNearAnyLabel(line, LABELS);
               if (amount.isEmpty()) {
                  amount = EconomyTextParser.largestAmount(line);
               }

               if (amount.isPresent()) {
                  return amount;
               }
            }
         }
      }

      return OptionalLong.empty();
   }
}
