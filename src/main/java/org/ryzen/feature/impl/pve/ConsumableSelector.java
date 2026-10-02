package org.ryzen.feature.impl.pve;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class ConsumableSelector {
   private ConsumableSelector() {
   }

   static <T> Optional<ConsumableSelector.Candidate<T>> selectFood(
      Collection<ConsumableSelector.Candidate<T>> candidates, boolean ignoreGoldenApples, boolean ignoreEnchantedGoldenApples
   ) {
      Optional<ConsumableSelector.Candidate<T>> held = candidates.stream()
         .filter(candidate -> isAllowedFood((ConsumableSelector.Candidate<?>)candidate, ignoreGoldenApples, ignoreEnchantedGoldenApples))
         .filter(candidate -> candidate.location() == ConsumableSelector.Location.OFF_HAND || candidate.location() == ConsumableSelector.Location.MAIN_HAND)
         .min(Comparator.comparingInt(candidate -> candidate.location() == ConsumableSelector.Location.OFF_HAND ? 0 : 1));
      return held.isPresent()
         ? held
         : candidates.stream()
            .filter(candidate -> isAllowedFood((ConsumableSelector.Candidate<?>)candidate, ignoreGoldenApples, ignoreEnchantedGoldenApples))
            .filter(candidate -> candidate.location() == ConsumableSelector.Location.HOTBAR || candidate.location() == ConsumableSelector.Location.INVENTORY)
            .min(foodComparator());
   }

   static <T> Optional<ConsumableSelector.Candidate<T>> selectApple(
      Collection<ConsumableSelector.Candidate<T>> candidates, boolean allowGoldenApples, boolean allowEnchantedGoldenApples
   ) {
      return candidates.stream()
         .filter(
            candidate -> candidate.kind() == ConsumableSelector.Kind.GOLDEN_APPLE && allowGoldenApples
               || candidate.kind() == ConsumableSelector.Kind.ENCHANTED_GOLDEN_APPLE && allowEnchantedGoldenApples
         )
         .min(
            Comparator.<ConsumableSelector.Candidate<T>>comparingInt(candidate -> candidate.kind() == ConsumableSelector.Kind.ENCHANTED_GOLDEN_APPLE ? 0 : 1)
               .thenComparingInt(candidate -> locationRank(candidate.location()))
               .thenComparingInt(ConsumableSelector.Candidate::containerSlot)
         );
   }

   private static <T> Comparator<ConsumableSelector.Candidate<T>> foodComparator() {
      return Comparator.<ConsumableSelector.Candidate<T>>comparingInt(candidate -> foodRank((ConsumableSelector.Candidate<?>)candidate))
         .thenComparing(Comparator.<ConsumableSelector.Candidate<T>>comparingDouble(candidate -> candidate.foodValue()).reversed())
         .thenComparingInt(candidate -> locationRank(candidate.location()))
         .thenComparingInt(ConsumableSelector.Candidate::containerSlot);
   }

   private static boolean isAllowedFood(ConsumableSelector.Candidate<?> candidate, boolean ignoreGoldenApples, boolean ignoreEnchantedGoldenApples) {
      return switch (candidate.kind()) {
         case FOOD -> true;
         case GOLDEN_APPLE -> !ignoreGoldenApples;
         case ENCHANTED_GOLDEN_APPLE -> !ignoreEnchantedGoldenApples;
         default -> false;
      };
   }

   private static int foodRank(ConsumableSelector.Candidate<?> candidate) {
      if (candidate.kind() == ConsumableSelector.Kind.FOOD) {
         return candidate.safe() ? 0 : 2;
      } else {
         return 1;
      }
   }

   private static int locationRank(ConsumableSelector.Location location) {
      return switch (location) {
         case OFF_HAND -> 0;
         case MAIN_HAND -> 1;
         case HOTBAR -> 2;
         case INVENTORY -> 3;
      };
   }

   @Environment(EnvType.CLIENT)
   record Candidate<T>(
      T value, ConsumableSelector.Kind kind, ConsumableSelector.Location location, int containerSlot, int nutrition, float saturation, boolean safe
   ) {
      Candidate {
         Objects.requireNonNull(value, "value");
         Objects.requireNonNull(kind, "kind");
         Objects.requireNonNull(location, "location");
      }

      double foodValue() {
         return (double)this.nutrition * this.saturation;
      }
   }

   @Environment(EnvType.CLIENT)
   enum Kind {
      FOOD,
      GOLDEN_APPLE,
      ENCHANTED_GOLDEN_APPLE,
      INVISIBILITY_POTION;
   }

   @Environment(EnvType.CLIENT)
   enum Location {
      OFF_HAND,
      MAIN_HAND,
      HOTBAR,
      INVENTORY;
   }
}
