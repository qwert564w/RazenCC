package org.ryzen.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10039;
import org.ryzen.utils.render.ItemEntityRenderStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(class_10039.class)
public abstract class ItemEntityRenderStateMixin implements ItemEntityRenderStateAccess {
   @Unique
   private boolean onGround;

   @Override
   public boolean isOnGround() {
      return this.onGround;
   }

   @Override
   public void setOnGround(boolean onGround) {
      this.onGround = onGround;
   }
}
