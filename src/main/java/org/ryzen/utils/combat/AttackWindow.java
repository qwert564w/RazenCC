package org.ryzen.utils.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12125;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_9334;
import org.ryzen.context.PlayerContext;
import org.ryzen.feature.impl.visual.HoldMyItemsCompat;

@Environment(EnvType.CLIENT)
public class AttackWindow implements PlayerContext {
   public static void attack(class_1297 target) {
      if (mc.field_1761 != null && mc.field_1724 != null) {
         HoldMyItemsCompat.beginMainHandAttack(mc.field_1724);
         class_12125 piercing = (class_12125)mc.field_1724.method_59958().method_58694(class_9334.field_63631);
         if (piercing != null) {
            mc.field_1761.method_75407(piercing);
         } else {
            mc.field_1761.method_2918(mc.field_1724, target);
         }

         mc.field_1724.method_6104(class_1268.field_5808);
         SprintManager.onAttack();
      }
   }
}
