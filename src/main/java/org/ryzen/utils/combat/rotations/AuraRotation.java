package org.ryzen.utils.combat.rotations;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public interface AuraRotation {
   void tick(class_746 var1, class_1309 var2, class_243 var3, boolean var4);

   default void onAttack() {
   }

   default void reset() {
   }

   default void setCurrentTarget(class_1309 target) {
   }
}
