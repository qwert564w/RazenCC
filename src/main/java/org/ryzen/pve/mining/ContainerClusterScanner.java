package org.ryzen.pve.mining;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_638;
import net.minecraft.class_2338.class_2339;

@Environment(EnvType.CLIENT)
public final class ContainerClusterScanner {
   private class_2338 center;
   private int radius;
   private int verticalRadius;
   private int cursor;
   private int volume;
   private Set<class_2248> targets = Set.of();
   private final List<class_2338> matches = new ArrayList<>();

   public void begin(class_2338 center, int radius, Set<class_2248> targets) {
      this.center = center == null ? null : center.method_10062();
      this.radius = Math.max(1, radius);
      this.verticalRadius = Math.min(12, this.radius);
      int width = this.radius * 2 + 1;
      int height = this.verticalRadius * 2 + 1;
      this.volume = width * width * height;
      this.cursor = 0;
      this.targets = targets == null ? Set.of() : Set.copyOf(targets);
      this.matches.clear();
   }

   public Optional<ContainerClusterScanner.Result> scan(class_638 level, int budget) {
      if (level != null && this.center != null && !this.targets.isEmpty()) {
         int width = this.radius * 2 + 1;
         int height = this.verticalRadius * 2 + 1;
         int remaining = Math.max(1, budget);
         class_2339 mutable = new class_2339();

         while (this.cursor < this.volume && remaining-- > 0) {
            int index = this.cursor++;
            int xIndex = index % width;
            int zIndex = index / width % width;
            int yIndex = index / (width * width) % height;
            mutable.method_10103(
               this.center.method_10263() + xIndex - this.radius,
               this.center.method_10264() + yIndex - this.verticalRadius,
               this.center.method_10260() + zIndex - this.radius
            );
            if (level.method_22340(mutable) && this.targets.contains(level.method_8320(mutable).method_26204())) {
               this.matches.add(mutable.method_10062());
            }
         }

         return this.cursor < this.volume ? Optional.empty() : Optional.of(new ContainerClusterScanner.Result(this.center, List.copyOf(this.matches)));
      } else {
         return Optional.empty();
      }
   }

   public boolean isRunning() {
      return this.center != null && this.cursor < this.volume;
   }

   public void reset() {
      this.center = null;
      this.cursor = 0;
      this.volume = 0;
      this.targets = Set.of();
      this.matches.clear();
   }

   @Environment(EnvType.CLIENT)
   public record Result(class_2338 scanCenter, List<class_2338> matches) {
      public Optional<class_2338> nearestTo(class_2338 position) {
         return position == null
            ? Optional.empty()
            : this.matches.stream().min((first, second) -> Double.compare(first.method_10262(position), second.method_10262(position)));
      }
   }
}
