package org.ryzen.utils.irc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public final class IrcProfile {
   private static final String UNKNOWN = "Unknown";
   private static volatile IrcRole role = IrcRole.MEMBER;

   private IrcProfile() {
   }

   public static String name() {
      class_310 mc = class_310.method_1551();
      if (mc.method_1548() == null) {
         return "Unknown";
      }

      String name = mc.method_1548().method_1676();
      return name != null && !name.isBlank() ? name : "Unknown";
   }

   public static IrcRole role() {
      return role;
   }

   public static void setRole(IrcRole newRole) {
      role = newRole == null ? IrcRole.MEMBER : newRole;
   }
}
