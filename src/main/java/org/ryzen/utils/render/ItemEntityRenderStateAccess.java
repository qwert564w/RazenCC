package org.ryzen.utils.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ItemEntityRenderStateAccess {
   boolean isOnGround();

   void setOnGround(boolean var1);
}
