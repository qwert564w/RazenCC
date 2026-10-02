package org.ryzen.menu.ui.controls;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_156;
import net.minecraft.class_310;
import net.minecraft.class_156.class_158;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class MenuClipboard {
   private MenuClipboard() {
   }

   public static boolean shortcutDown() {
      class_310 client = class_310.method_1551();
      if (client != null && client.method_22683() != null) {
         long window = client.method_22683().method_4490();
         return class_156.method_668() == class_158.field_1137 ? keyDown(window, 343) || keyDown(window, 347) : keyDown(window, 341) || keyDown(window, 345);
      } else {
         return false;
      }
   }

   public static String get() {
      class_310 client = class_310.method_1551();
      return client == null ? "" : client.field_1774.method_1460();
   }

   public static void set(String value) {
      class_310 client = class_310.method_1551();
      if (client != null) {
         client.field_1774.method_1455(value == null ? "" : value);
      }
   }

   private static boolean keyDown(long window, int key) {
      return GLFW.glfwGetKey(window, key) == 1;
   }
}
