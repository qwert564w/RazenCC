package org.ryzen.mixin.accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(class_1309.class)
public interface LivingEntityAccessor {
   @Accessor("field_6228")
   int getNoJumpDelay();

   @Accessor("field_6228")
   void setNoJumpDelay(int var1);
}
