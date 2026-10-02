package org.ryzen.pve.server;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_642;

@Environment(EnvType.CLIENT)
public enum ServerProfile {
   GENERIC,
   FUNTIME,
   HOLYWORLD,
   REALLYWORLD;

   public static ServerProfile detect(class_310 client) {
      class_642 server = client.method_1558();
      return server == null ? GENERIC : detect(server.field_3752, server.field_3761);
   }

   public static ServerProfile detect(String name, String address) {
      String identity = ((name == null ? "" : name) + " " + (address == null ? "" : address)).toLowerCase(Locale.ROOT);
      if (identity.contains("funtime")) {
         return FUNTIME;
      } else if (identity.contains("holyworld") || identity.contains("holy-world")) {
         return HOLYWORLD;
      } else {
         return !identity.contains("reallyworld") && !identity.contains("spookytime") ? GENERIC : REALLYWORLD;
      }
   }
}
