package org.ryzen.pve.economy;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_7438;
import net.minecraft.class_7439;
import net.minecraft.class_7827;

@Environment(EnvType.CLIENT)
public final class EconomyChat {
   private EconomyChat() {
   }

   public static String incomingText(class_2596<?> packet) {
      if (packet instanceof class_7439 systemChat) {
         return systemChat.comp_763().getString();
      } else if (packet instanceof class_7827 disguisedChat) {
         return disguisedChat.comp_1097().getString();
      } else if (packet instanceof class_7438 playerChat) {
         class_2561 unsigned = playerChat.comp_1103();
         return unsigned == null ? playerChat.comp_1102().comp_1090() : unsigned.getString();
      } else {
         return null;
      }
   }
}
