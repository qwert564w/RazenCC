package org.ryzen.context;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_757;
import net.minecraft.class_9779;

@Environment(EnvType.CLIENT)
public interface Render3DContext extends MinecraftContext {
   default boolean isInRender3D() {
      return RenderContext.state3D().isActive();
   }

   default class_757 gameRenderer3D() {
      return RenderContext.state3D().getGameRenderer();
   }

   default class_9779 deltaTracker() {
      return RenderContext.state3D().getDeltaTracker();
   }
}
