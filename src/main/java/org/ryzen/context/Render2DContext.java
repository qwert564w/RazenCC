package org.ryzen.context;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;

@Environment(EnvType.CLIENT)
public interface Render2DContext extends MinecraftContext {
   default boolean isInRender2D() {
      return RenderContext.state2D().isActive();
   }

   default class_329 gui() {
      return RenderContext.state2D().getGui();
   }

   default class_332 guiGraphicsExtractor() {
      return RenderContext.state2D().getGuiGraphicsExtractor();
   }

   default class_9779 deltaTracker() {
      return RenderContext.state2D().getDeltaTracker();
   }
}
