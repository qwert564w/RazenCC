package org.ryzen.utils.render.chams;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_10055;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.ryzen.feature.impl.visual.ChamsFeature;

@Environment(EnvType.CLIENT)
public final class ChamsTargetMatcher {
   private ChamsTargetMatcher() {
   }

   public static List<class_1297> collectTargets(class_310 minecraft, ChamsFeature feature) {
      if (minecraft.field_1687 != null && feature != null && feature.hasAnyVisual()) {
         List<class_1297> targets = new ArrayList<>();

         for (class_1297 entity : minecraft.field_1687.method_18112()) {
            if (feature.shouldRender(entity)) {
               targets.add(entity);
            }
         }

         return targets;
      } else {
         return List.of();
      }
   }

   public static class_1297 matchingTarget(class_10017 state, List<class_1297> targets) {
      if (state instanceof class_10055 avatarState) {
         for (class_1297 entity : targets) {
            if (entity.method_5628() == avatarState.field_53528) {
               return entity;
            }
         }
      }

      class_1297 best = null;
      double bestDistanceSq = Double.MAX_VALUE;

      for (class_1297 entity : targets) {
         if (entity.method_5864() == state.field_58171) {
            double dx = entity.method_23317() - state.field_53325;
            double dy = entity.method_23318() - state.field_53326;
            double dz = entity.method_23321() - state.field_53327;
            double distanceSq = dx * dx + dy * dy + dz * dz;
            if (distanceSq < bestDistanceSq) {
               best = entity;
               bestDistanceSq = distanceSq;
            }
         }
      }

      return bestDistanceSq <= 9.0 ? best : null;
   }
}
