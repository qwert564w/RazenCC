package org.ryzen.mixin.accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2823;
import net.minecraft.class_3558;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(class_3558.class)
public interface LightEngineAccessor {
   @Accessor("field_15795")
   class_2823 getChunkSource();
}
