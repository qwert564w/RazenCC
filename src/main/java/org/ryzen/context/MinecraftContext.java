package org.ryzen.context;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public interface MinecraftContext {
   class_310 mc = class_310.method_1551();

   default class_310 client() {
      return mc;
   }

   default class_746 player() {
      return mc.field_1724;
   }

   default class_638 level() {
      return mc.field_1687;
   }

   default class_636 gameMode() {
      return mc.field_1761;
   }

   default class_437 screen() {
      return mc.field_1755;
   }

   default boolean inGame() {
      return this.player() != null && this.level() != null;
   }
}
