package org.ryzen.utils.render.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class UiFonts {
   private UiFonts() {
   }

   public static MsdfFontFamily sfPro() {
      return UiFonts.Holder.SF_PRO;
   }

   public static MsdfFont sfPro(int weight) {
      return UiFonts.Holder.SF_PRO.resolve(weight);
   }

   public static MsdfFont sfProDisplay() {
      return UiFonts.Holder.SF_PRO.regular();
   }

   public static MsdfFontFamily suisse() {
      return UiFonts.Holder.SUISSE;
   }

   public static MsdfFontFamily stationText() {
      return UiFonts.Holder.STATION_TEXT;
   }

   public static MsdfFontFamily stationIcons() {
      return UiFonts.Holder.STATION_ICONS;
   }

   @Environment(EnvType.CLIENT)
   private static final class Holder {
      private static final MsdfFontFamily SF_PRO = MsdfFontFamily.builder()
         .variant(400, "ryzen:fonts/sf_pro_regular.json")
         .variant(500, "ryzen:fonts/sf_pro_medium.json")
         .variant(600, "ryzen:fonts/sf_pro_semibold.json")
         .variant(700, "ryzen:fonts/sf_pro_bold.json")
         .build();
      private static final MsdfFontFamily SUISSE = MsdfFontFamily.builder()
         .variant(400, "ryzen:fonts/suisse_regular.json")
         .variant(500, "ryzen:fonts/suisse_medium.json")
         .variant(600, "ryzen:fonts/suisse_semibold.json")
         .build();
      private static final MsdfFontFamily STATION_TEXT = MsdfFontFamily.builder().variant(400, "ryzen:fonts/station/onest_regular.json").build();
      private static final MsdfFontFamily STATION_ICONS = MsdfFontFamily.builder().variant(400, "ryzen:fonts/station/icons.json").build();
   }
}
