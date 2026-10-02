package org.ryzen.utils.combat;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1421;
import net.minecraft.class_1429;
import net.minecraft.class_1588;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_1304.class_1305;
import org.ryzen.context.RotationContext;
import org.ryzen.utils.FriendManager;

@Environment(EnvType.CLIENT)
public final class TargetUtil {
   private TargetUtil() {
   }

   public static class_1309 getBestTarget(class_746 player, class_638 level, double range, TargetFilter filter) {
      return getBestTarget(player, level, range, filter, entity -> true);
   }

   public static class_1309 getBestTarget(class_746 player, class_638 level, double range, TargetFilter filter, Predicate<class_1309> extraFilter) {
      List<class_1309> targets = getTargets(player, level, range, filter);
      targets.removeIf(extraFilter.negate());
      if (targets.isEmpty()) {
         return null;
      }

      targets.sort(Comparator.comparingDouble(entity -> calculateTargetScore(player, entity, range, filter)));
      return targets.get(0);
   }

   public static List<class_1309> getTargets(class_746 player, class_638 level, double range, TargetFilter filter) {
      double rangeSqr = range * range;
      return StreamSupport.<class_1297>stream(level.method_18112().spliterator(), false)
         .filter(entity -> entity instanceof class_1309)
         .map(entity -> (class_1309)entity)
         .filter(entity -> isValidTarget(player, entity, rangeSqr, filter))
         .collect(Collectors.toList());
   }

   public static boolean isValidTarget(class_746 player, class_1309 entity, double rangeSqr, TargetFilter filter) {
      if (entity == player) {
         return false;
      }

      if (entity.method_5805() && entity.method_5732()) {
         if (player.method_5858(entity) > rangeSqr) {
            return false;
         }

         if (entity.method_5767() && !filter.isTargetsInvisibles()) {
            return false;
         }

         if (entity instanceof class_1657 targetPlayer) {
            if (targetPlayer.method_7325() || targetPlayer.method_68878()) {
               return false;
            }

            if (FriendManager.INSTANCE.isFriend(targetPlayer.method_7334().name()) && !filter.isTargetsFriends()) {
               return false;
            }

            if (isNaked(targetPlayer) && !filter.isTargetsNakedPlayers()) {
               return false;
            }

            if (!filter.isTargetsPlayers()) {
               return false;
            }
         } else if (entity instanceof class_1588) {
            if (!filter.isTargetsMonsters()) {
               return false;
            }
         } else if (!(entity instanceof class_1429) && !(entity instanceof class_1421)) {
            if (!(entity instanceof class_1646)) {
               return false;
            }

            if (!filter.isTargetsVillagers()) {
               return false;
            }
         } else if (!filter.isTargetsAnimals()) {
            return false;
         }

         return true;
      } else {
         return false;
      }
   }

   private static double calculateTargetScore(class_746 player, class_1309 entity, double maxRange, TargetFilter filter) {
      double distance = player.method_5739(entity);
      double normalizedDist = distance / maxRange;
      double normalizedHealth = entity.method_6032() / entity.method_6063();
      double normalizedArmor = entity.method_6096() / 20.0;
      double fov = RotationContext.getFovToEntity(player, entity);
      double normalizedFov = fov / 180.0;
      return normalizedDist * filter.getDistanceWeight()
         + normalizedHealth * filter.getHealthWeight()
         + normalizedArmor * filter.getArmorWeight()
         + normalizedFov * filter.getFovWeight();
   }

   private static boolean isNaked(class_1657 player) {
      for (class_1304 slot : class_1304.values()) {
         if (slot.method_5925() == class_1305.field_6178) {
            class_1799 stack = player.method_6118(slot);
            if (!stack.method_7960()) {
               return false;
            }
         }
      }

      return true;
   }
}
