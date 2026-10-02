package org.ryzen.mixin.accessor;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_630;
import net.minecraft.class_630.class_628;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(class_630.class)
public interface ModelPartAccessor {
   @Accessor("field_3663")
   List<class_628> blade$getCubes();
}
