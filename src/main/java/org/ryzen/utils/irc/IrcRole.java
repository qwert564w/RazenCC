package org.ryzen.utils.irc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum IrcRole {
   MEMBER("Member", 0, -5592406),
   BETA("BETA", 1, -10773505),
   YT("YT", 2, -46004),
   ADMIN("Admin", 3, -8777954),
   OWNER("Owner", 4, -26066);

   private final String displayName;
   private final int priority;
   private final int color;

   IrcRole(String displayName, int priority, int color) {
      this.displayName = displayName;
      this.priority = priority;
      this.color = color;
   }

   public String displayName() {
      return this.displayName;
   }

   public int priority() {
      return this.priority;
   }

   public int color() {
      return this.color;
   }

   public boolean atLeast(IrcRole other) {
      return this.priority >= other.priority;
   }

   public static IrcRole fromName(String name) {
      if (name != null) {
         for (IrcRole role : values()) {
            if (role.displayName.equalsIgnoreCase(name)) {
               return role;
            }
         }
      }

      return MEMBER;
   }
}
