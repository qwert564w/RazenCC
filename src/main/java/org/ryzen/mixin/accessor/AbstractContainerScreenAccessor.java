package org.ryzen.mixin.accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1735;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(class_465.class)
public interface AbstractContainerScreenAccessor {
   @Accessor("field_2800")
   int getTopPos();

   @Accessor("field_2776")
   int getLeftPos();

   @Accessor("field_2792")
   int getImageWidth();

   @Accessor("field_2779")
   int getImageHeight();

   @Accessor("field_2787")
   class_1735 getHoveredSlot();
}
