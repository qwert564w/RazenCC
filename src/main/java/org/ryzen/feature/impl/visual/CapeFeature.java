package org.ryzen.feature.impl.visual;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.ModeSetting;

@Environment(EnvType.CLIENT)
public final class CapeFeature extends Feature {
   public final ModeSetting mode = this.register(new ModeSetting("Style", "Ryzen", "AppIcon", "ASCII", "Barcode", "Motto", "Ryzen"));

   public CapeFeature() {
      super("Capes", "Custom cosmetic capes visible on you", FeatureCategory.VISUAL, -1);
   }

   public CapeFeature.CapeStyle currentStyle() {
      try {
         return CapeFeature.CapeStyle.valueOf(this.mode.getValue().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         return CapeFeature.CapeStyle.RYZEN;
      }
   }

   @Environment(EnvType.CLIENT)
   public enum CapeStyle {
      APPICON("appicon.png"),
      ASCII("ascii.png"),
      BARCODE("barcode.png"),
      MOTTO("motto.png"),
      RYZEN("ryzen.png");

      private final String fileName;

      CapeStyle(String fileName) {
         this.fileName = fileName;
      }

      public String getFileName() {
         return this.fileName;
      }
   }
}
