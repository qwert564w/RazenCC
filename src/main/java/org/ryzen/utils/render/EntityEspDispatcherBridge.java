package org.ryzen.utils.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_4587;

@Environment(EnvType.CLIENT)
public interface EntityEspDispatcherBridge {
   <S extends class_10017> void submitForGlow(S var1, class_12075 var2, double var3, double var5, double var7, class_4587 var9, class_11659 var10);
}
