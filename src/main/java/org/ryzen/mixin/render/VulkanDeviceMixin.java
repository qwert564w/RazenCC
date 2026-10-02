package org.ryzen.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12289;
import org.ryzen.utils.render.ShaderFallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Pseudo
@Mixin(targets = "com/mojang/blaze3d/vulkan/VulkanDevice", remap = false)
public class VulkanDeviceMixin {
   @ModifyVariable(method = "getOrCompileShader", at = @At("HEAD"), argsOnly = true)
   private class_12289 wrapShaderSource(class_12289 source) {
      return (shaderId, shaderType) -> {
         String result = null;

         try {
            result = source.get(shaderId, shaderType);
         } catch (Throwable var5) {
         }

         if (result == null) {
            result = ShaderFallback.load(shaderId, shaderType);
         }

         return result;
      };
   }
}
