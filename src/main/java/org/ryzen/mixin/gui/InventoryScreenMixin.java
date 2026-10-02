package org.ryzen.mixin.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_490;
import org.ryzen.mixin.accessor.AbstractContainerScreenAccessor;
import org.ryzen.utils.inventory.DropAllInventoryController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_490.class)
public abstract class InventoryScreenMixin extends class_437 {
   protected InventoryScreenMixin(class_2561 title) {
      super(title);
   }

   @Inject(method = "method_25426", at = @At("TAIL"))
   private void addDropAllButton(CallbackInfo ci) {
      AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor)this;
      class_4185 button = class_4185.method_46430(class_2561.method_43470("Выбросить всё"), ignored -> DropAllInventoryController.toggle((class_490)(Object)this))
         .method_46434((this.field_22789 - 110) / 2, Math.max(4, accessor.getTopPos() - 24), 110, 20)
         .method_46431();
      this.method_37063(button);
      DropAllInventoryController.bindButton(button);
   }
}
