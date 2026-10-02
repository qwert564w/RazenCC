package org.ryzen.feature.impl.pve;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
final class ConsumableInventory {
   private ConsumableInventory() {
   }

   static List<ConsumableSelector.Candidate<class_1799>> collect(class_746 player, Function<class_1799, ConsumableInventory.Profile> classifier) {
      List<ConsumableSelector.Candidate<class_1799>> candidates = new ArrayList<>();
      int selectedHotbar = player.method_31548().method_67532();
      int selectedContainer = 36 + selectedHotbar;
      add(candidates, player.method_6079(), ConsumableSelector.Location.OFF_HAND, 45, classifier);
      add(candidates, player.method_6047(), ConsumableSelector.Location.MAIN_HAND, selectedContainer, classifier);

      for (int slot = 36; slot < 45; slot++) {
         if (slot != selectedContainer) {
            add(candidates, player.field_7498.method_7611(slot).method_7677(), ConsumableSelector.Location.HOTBAR, slot, classifier);
         }
      }

      for (int slot = 9; slot < 36; slot++) {
         add(candidates, player.field_7498.method_7611(slot).method_7677(), ConsumableSelector.Location.INVENTORY, slot, classifier);
      }

      return List.copyOf(candidates);
   }

   private static void add(
      List<ConsumableSelector.Candidate<class_1799>> candidates,
      class_1799 stack,
      ConsumableSelector.Location location,
      int containerSlot,
      Function<class_1799, ConsumableInventory.Profile> classifier
   ) {
      if (!stack.method_7960()) {
         ConsumableInventory.Profile profile = classifier.apply(stack);
         if (profile != null) {
            candidates.add(
               new ConsumableSelector.Candidate<>(
                  stack.method_7972(), profile.kind(), location, containerSlot, profile.nutrition(), profile.saturation(), profile.safe()
               )
            );
         }
      }
   }

   @Environment(EnvType.CLIENT)
   record Profile(ConsumableSelector.Kind kind, int nutrition, float saturation, boolean safe) {
   }
}
