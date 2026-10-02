package org.ryzen.context;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1703;
import net.minecraft.class_465;

@Environment(EnvType.CLIENT)
public interface InventoryContext extends ScreenContext {
   default class_1703 menu() {
      class_465<?> containerScreen = this.containerScreen();
      return containerScreen != null ? containerScreen.method_17577() : null;
   }

   default boolean hasMenu() {
      return this.menu() != null;
   }
}
