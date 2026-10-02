package org.ryzen.feature.impl.misc.autobuy;

import java.util.Locale;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum AutoBuyServer {
   FUNTIME("FunTime", "FT", AutoBuyServer.Family.FUNTIME),
   SPOOKYTIME("SpookyTime", "SP", AutoBuyServer.Family.FUNTIME),
   HOLYWORLD("HolyWorld", "HW", AutoBuyServer.Family.HOLYWORLD);

   private final String id;
   private final String shortLabel;
   private final AutoBuyServer.Family family;

   AutoBuyServer(String id, String shortLabel, AutoBuyServer.Family family) {
      this.id = id;
      this.shortLabel = shortLabel;
      this.family = family;
   }

   public static AutoBuyServer of(String id) {
      if (id != null) {
         for (AutoBuyServer server : values()) {
            if (server.id.equalsIgnoreCase(id.trim())) {
               return server;
            }
         }
      }

      return FUNTIME;
   }

   public static String[] ids() {
      AutoBuyServer[] servers = values();
      String[] ids = new String[servers.length];

      for (int index = 0; index < servers.length; index++) {
         ids[index] = servers[index].id;
      }

      return ids;
   }

   public static String[] shortLabels() {
      AutoBuyServer[] servers = values();
      String[] labels = new String[servers.length];

      for (int index = 0; index < servers.length; index++) {
         labels[index] = servers[index].shortLabel;
      }

      return labels;
   }

   public static boolean isHolyFamily(String id) {
      return of(id).family == AutoBuyServer.Family.HOLYWORLD;
   }

   public static String shortLabelOf(String id) {
      return of(id).shortLabel;
   }

   public boolean isHolyFamily() {
      return this.family == AutoBuyServer.Family.HOLYWORLD;
   }

   public String searchCommand(String query) {
      return command("ah search", query);
   }

   private static String command(String prefix, String query) {
      return (prefix + " " + (query == null ? "" : query.trim())).trim();
   }

   public String key() {
      return this.id.toLowerCase(Locale.ROOT);
   }

   @Generated
   public String getId() {
      return this.id;
   }

   @Generated
   public String getShortLabel() {
      return this.shortLabel;
   }

   @Generated
   public AutoBuyServer.Family getFamily() {
      return this.family;
   }

   @Environment(EnvType.CLIENT)
   public enum Family {
      FUNTIME,
      HOLYWORLD;
   }
}
