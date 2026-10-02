package org.ryzen.mixin.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import org.ryzen.utils.render.gui.GuiPipelines;
import org.ryzen.utils.render.particles.ProceduralParticleRenderer;
import org.ryzen.utils.render.post.PostPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_10799.class)
public abstract class RenderPipelinesMixin {
   @Shadow
   public static RenderPipeline method_67887(RenderPipeline renderPipeline) {
      throw new AssertionError();
   }

   @Inject(method = "<clinit>", at = @At("TAIL"))
   private static void registerGuiPipelines(CallbackInfo ci) {
      method_67887(GuiPipelines.RECT);
      method_67887(GuiPipelines.TEXT);
      method_67887(GuiPipelines.TEXTURE);
      method_67887(GuiPipelines.GLASS_SHADOW);
      method_67887(GuiPipelines.BLUR_RECT);
      method_67887(GuiPipelines.COLOR_GRID);
      method_67887(GuiPipelines.MENU_BACKGROUND);
      method_67887(GuiPipelines.GUI_BLUR_DOWN);
      method_67887(GuiPipelines.GUI_BLUR_UP);

      for (RenderPipeline pipeline : GuiPipelines.itemDownscaleVariants()) {
         method_67887(pipeline);
      }

      method_67887(PostPipelines.ESP_KAWASE_DOWN);
      method_67887(PostPipelines.ESP_KAWASE_UP);
      method_67887(ProceduralParticleRenderer.pipeline());
      method_67887(PostPipelines.HAND_PLASMA);
      method_67887(PostPipelines.POINT_LIGHTS);
      method_67887(PostPipelines.BLOCK_OUTLINE_CLASSIC);
      method_67887(PostPipelines.BLOCK_OUTLINE_CLASSIC_THROUGH);
      method_67887(PostPipelines.BLOCK_OUTLINE_CAUSTICS);
      method_67887(PostPipelines.BLOCK_OUTLINE_CAUSTICS_THROUGH);
      method_67887(PostPipelines.BLOCK_OUTLINE_PRISMATIC);
      method_67887(PostPipelines.BLOCK_OUTLINE_PRISMATIC_THROUGH);
      method_67887(PostPipelines.BLOCK_OUTLINE_GLOSSY);
      method_67887(PostPipelines.BLOCK_OUTLINE_GLOSSY_THROUGH);
      method_67887(PostPipelines.BLOCK_OUTLINE_DEEP_SPACE);
      method_67887(PostPipelines.BLOCK_OUTLINE_DEEP_SPACE_THROUGH);
      method_67887(PostPipelines.BLOCK_OUTLINE_NEBULA);
      method_67887(PostPipelines.BLOCK_OUTLINE_NEBULA_THROUGH);
      method_67887(PostPipelines.WORLD_SKY_CLOUDS_DEEP_SPACE);
      method_67887(PostPipelines.WORLD_SKY_CLOUDS_NEBULA);
      method_67887(PostPipelines.WORLD_SKY_CLOUDS_PLASMA);
      method_67887(PostPipelines.WORLD_SKY_DEEP_SPACE);
      method_67887(PostPipelines.WORLD_SKY_NEBULA);
      method_67887(PostPipelines.WORLD_SKY_PLASMA);
      method_67887(PostPipelines.WORLD_SATURATION);
      method_67887(PostPipelines.CHAMS_SOLID);
      method_67887(PostPipelines.CHAMS_PLASMA);
      method_67887(PostPipelines.CHAMS_NEBULA);
      method_67887(PostPipelines.CHAMS_GLASS);
      method_67887(PostPipelines.CHAMS_OUTLINE);
      method_67887(PostPipelines.CHAMS_INTERNAL);
      method_67887(PostPipelines.CHAMS_GLOW);
      method_67887(PostPipelines.CHAMS_GLOW_ADDITIVE);
      method_67887(PostPipelines.POPCHAMS_ADDITIVE);
      method_67887(PostPipelines.POPCHAMS_ADDITIVE_SOLID);
      method_67887(PostPipelines.POPCHAMS_TRANSLUCENT);
      method_67887(PostPipelines.POPCHAMS_TRANSLUCENT_SOLID);
      method_67887(PostPipelines.POPCHAMS_MASK);
      method_67887(PostPipelines.POPCHAMS_MASK_SOLID);
      method_67887(PostPipelines.POPCHAMS_COMPOSITE);
   }
}
