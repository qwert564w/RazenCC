package org.ryzen.pve.navigation;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2248;
import net.minecraft.class_2338;

@Environment(EnvType.CLIENT)
public interface Navigator {
   boolean isAvailable();

   void begin(NavigationOptions var1);

   void pathTo(class_2338 var1, int var2);

   void mine(int var1, class_2248... var2);

   void setMineBounds(class_2338 var1, class_2338 var2);

   default void setMineRenderColor(int argb) {
   }

   default void setMineAoeLevel(int level) {
   }

   boolean isPathing();

   boolean isMining();

   List<class_2338> miningTargets();

   Optional<Double> estimatedTicksToGoal();

   Optional<class_2338> currentGoal();

   String diagnostics();

   void cancel();

   void end();
}
