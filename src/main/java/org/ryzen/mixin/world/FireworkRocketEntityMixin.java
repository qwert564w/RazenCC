package org.ryzen.mixin.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_1671;
import net.minecraft.class_243;
import net.minecraft.class_310;
import org.ryzen.feature.impl.movement.SuperFireworkFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(class_1671.class)
public abstract class FireworkRocketEntityMixin {
   @WrapOperation(method = "method_5773", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1309;method_18799(Lnet/minecraft/class_243;)V"))
   private void ryzen$superFireworkBoost(class_1309 rider, class_243 movement, Operation<Void> original) {
      SuperFireworkFeature feature = SuperFireworkFeature.getEnabled();
      if (feature != null && rider == class_310.method_1551().field_1724) {
         original.call(new Object[]{rider, feature.boostedMovement(rider)});
      } else {
         original.call(new Object[]{rider, movement});
      }
   }
}
