package org.ryzen.utils.render.chams;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12137;
import net.minecraft.class_276;
import org.ryzen.utils.render.post.KawaseBlur;
import org.ryzen.utils.render.post.PostFx;
import org.ryzen.utils.render.post.PostPipelines;

@Environment(EnvType.CLIENT)
public final class PopChamsCompositeEffect {
   private static final int MAX_MIPS = 3;
   private static final int COMPOSITE_SIZE = new Std140SizeCalculator().putVec4().get();
   private final KawaseBlur blur = new KawaseBlur("blade-popchams");
   private GpuBuffer compositeUniforms;

   public void render(class_276 mask, class_276 output, int radius) {
      if (mask != null && output != null && mask.method_71639() != null && output.method_71639() != null && output.field_1482 > 0 && output.field_1481 > 0) {
         if (this.compositeUniforms == null) {
            this.compositeUniforms = PostFx.createUniforms("Ryzen PopChams Composite UBO", COMPOSITE_SIZE);
         }

         float screenScale = Math.max(0.5F, output.field_1481 / 1080.0F);
         float scaledRadius = Math.max(0.05F, radius * screenScale);
         int levels = Math.clamp((int)Math.floor(Math.log(scaledRadius) / Math.log(2.0)) + 1, 1, 3);
         float offset = Math.min(2.2F, 1.1F * (float)Math.pow(scaledRadius, 0.32F));
         float intensity = Math.min(1.5F, radius * 0.15F + 0.45F);
         class_12137 sampler = PostFx.linearSampler();
         GpuTextureView blurred = this.blur.run(mask.method_71639(), output.field_1482, output.field_1481, levels, offset, sampler);
         PostFx.writeUniforms(this.compositeUniforms, COMPOSITE_SIZE, builder -> builder.putVec4(intensity, 0.0F, 0.0F, 0.0F));
         PostFx.pass("Ryzen PopChams Composite", PostPipelines.POPCHAMS_COMPOSITE, output, pass -> {
            pass.setUniform("PopChamsComposite", this.compositeUniforms);
            pass.bindTexture("BlurredSampler", blurred, sampler);
         });
      }
   }

   public void release() {
      this.blur.release();
      if (this.compositeUniforms != null) {
         this.compositeUniforms.close();
         this.compositeUniforms = null;
      }
   }
}
