package org.ryzen.utils.render.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class BladeGuiPipelines {
   public static final RenderPipeline RECT = GuiPipelines.RECT;
   public static final RenderPipeline TEXT = GuiPipelines.TEXT;
   public static final RenderPipeline TEXTURE = GuiPipelines.TEXTURE;
   public static final RenderPipeline GLASS_SHADOW = GuiPipelines.GLASS_SHADOW;
   public static final RenderPipeline BLUR_RECT = GuiPipelines.BLUR_RECT;
   public static final RenderPipeline COLOR_GRID = GuiPipelines.COLOR_GRID;

   private BladeGuiPipelines() {
   }
}
