package org.ryzen.utils.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12392;
import net.minecraft.class_746;
import net.minecraft.class_9334;

@Environment(EnvType.CLIENT)
public final class AttackReach {
   private AttackReach() {
   }

   public static class_12392 range(class_746 player) {
      return (class_12392)player.method_59958().method_58695(class_9334.field_64680, class_12392.method_76734(player));
   }

   public static double max(class_746 player) {
      return range(player).method_76739(player);
   }

   public static double min(class_746 player) {
      return range(player).method_76732(player);
   }

   public static double margin(class_746 player) {
      return range(player).comp_5262();
   }
}
