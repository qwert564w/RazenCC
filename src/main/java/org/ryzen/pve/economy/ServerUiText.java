package org.ryzen.pve.economy;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_642;
import org.ryzen.mixin.accessor.PlayerTabOverlayAccessor;

@Environment(EnvType.CLIENT)
public final class ServerUiText {
   private ServerUiText() {
   }

   public static String tabHeader(class_310 client) {
      if (client != null && client.field_1705 != null && client.field_1705.method_1750() != null) {
         class_2561 header = ((PlayerTabOverlayAccessor)client.field_1705.method_1750()).getHeader();
         return header == null ? "" : EconomyTextParser.normalize(header.getString());
      } else {
         return "";
      }
   }

   public static String serverHost(class_310 client) {
      class_642 server = client == null ? null : client.method_1558();
      if (server != null && server.field_3761 != null) {
         String host = server.field_3761.trim().toLowerCase(Locale.ROOT);
         if (host.startsWith("[")) {
            int closing = host.indexOf(93);
            return closing > 0 ? host.substring(1, closing) : host;
         } else {
            int colon = host.indexOf(58);
            return colon < 0 ? host : host.substring(0, colon);
         }
      } else {
         return "";
      }
   }
}
