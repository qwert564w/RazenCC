package org.ryzen.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2680;
import net.minecraft.class_3552;
import net.minecraft.class_631;
import org.ryzen.mixin.accessor.LightEngineAccessor;
import org.ryzen.utils.render.world.DynamicLightManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(class_3552.class)
public abstract class BlockLightEngineMixin {
   @ModifyExpressionValue(method = "method_15474", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_2680;method_26213()I"))
   private int virtualDynamicLight(int original, long packedPos, class_2680 state) {
      return !(((LightEngineAccessor)this).getChunkSource() instanceof class_631)
         ? original
         : Math.max(original, DynamicLightManager.virtualLuminance(packedPos));
   }
}
