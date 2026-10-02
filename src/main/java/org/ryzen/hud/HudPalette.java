package org.ryzen.hud;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Theme;

@Environment(EnvType.CLIENT)
public final class HudPalette {
   public static final int PANEL = ColorUtil.rgba(18, 18, 20, 237);
   public static final int PANEL_BORDER = ColorUtil.rgba(255, 255, 255, 18);
   public static final int SURFACE = ColorUtil.rgba(33, 33, 38, 102);
   public static final int SURFACE_BORDER = ColorUtil.rgba(255, 255, 255, 18);
   public static final int DANGER = ColorUtil.rgb(255, 50, 32);
   public static final int PLAYING = ColorUtil.rgb(84, 255, 32);
   public static final int SUCCESS = ColorUtil.rgb(134, 255, 97);
   public static final int TEXT = -1;
   public static final int TEXT_SECONDARY = ColorUtil.rgba(255, 255, 255, 179);
   public static final int TEXT_MUTED = ColorUtil.rgba(255, 255, 255, 77);
   public static final int DIVIDER = ColorUtil.rgba(255, 255, 255, 25);
   public static final float CARD_RADIUS = 13.0F;
   public static final float INNER_RADIUS = 13.0F;
   public static final float HEADER_HEIGHT = 38.0F;
   public static final float CARD_INSET = 5.0F;
   public static final float CARD_FOOTER = 5.0F;
   public static final float BLUR_RADIUS = 50.0F;

   public static int accent() {
      return Theme.getAccent();
   }

   public static int accentAlt() {
      return ColorUtil.multiplyRgb(Theme.getAccent(), 1.08F);
   }

   @Generated
   private HudPalette() {
   }
}
