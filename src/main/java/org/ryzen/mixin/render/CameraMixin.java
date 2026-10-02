package org.ryzen.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_4184;
import org.ryzen.context.MinecraftContext;
import org.ryzen.context.RotationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_4184.class)
public abstract class CameraMixin {
   @Inject(method = "method_19321", at = @At("HEAD"))
   private void onSetup(class_1937 level, class_1297 entity, boolean detached, boolean mirror, float partialTick, CallbackInfo ci) {
      RotationContext.applyRenderInterpolation();
      RotationContext.syncFreeLook(entity.method_5705(partialTick), entity.method_5695(partialTick));
   }

   @Redirect(method = "method_19321", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_5705(F)F"))
   private float redirectCameraYaw(class_1297 entity, float partialTick) {
      return this.cameraYaw(entity, partialTick);
   }

   @Redirect(method = "method_19321", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_5695(F)F"))
   private float redirectCameraPitch(class_1297 entity, float partialTick) {
      return this.cameraPitch(entity, partialTick);
   }

   @Unique
   private float cameraYaw(class_1297 entity, float partialTick) {
      return entity == MinecraftContext.mc.field_1724 && RotationContext.isActive() ? RotationContext.getFreeYaw() : entity.method_5705(partialTick);
   }

   @Unique
   private float cameraPitch(class_1297 entity, float partialTick) {
      return entity == MinecraftContext.mc.field_1724 && RotationContext.isActive() ? RotationContext.getFreePitch() : entity.method_5695(partialTick);
   }
}
