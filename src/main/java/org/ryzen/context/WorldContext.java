package org.ryzen.context;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_638;

@Environment(EnvType.CLIENT)
public interface WorldContext extends MinecraftContext {
   default class_638 world() {
      return this.level();
   }

   default boolean hasWorld() {
      return this.world() != null;
   }
}
