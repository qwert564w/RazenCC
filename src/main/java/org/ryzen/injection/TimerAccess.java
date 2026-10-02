package org.ryzen.injection;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface TimerAccess {
   void ryzen$setSpeedMultiplier(float var1);

   float ryzen$getSpeedMultiplier();
}
