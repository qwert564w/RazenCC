package org.ryzen.context;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_437;
import net.minecraft.class_465;

@Environment(EnvType.CLIENT)
public interface ScreenContext extends MinecraftContext {
   default class_437 currentScreen() {
      return this.screen();
   }

   default boolean hasScreen() {
      return this.currentScreen() != null;
   }

   default boolean hasContainerScreen() {
      return this.currentScreen() instanceof class_465;
   }

   default class_465<?> containerScreen() {
      return this.currentScreen() instanceof class_465<?> containerScreen ? containerScreen : null;
   }
}
