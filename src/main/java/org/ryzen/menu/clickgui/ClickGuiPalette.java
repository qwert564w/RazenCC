package org.ryzen.menu.clickgui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Theme;

@Environment(EnvType.CLIENT)
public final class ClickGuiPalette {
   public static final int WINDOW = ColorUtil.rgba(18, 18, 20, 237);
   public static final int HOME_BAR = ColorUtil.rgb(19, 20, 22);
   public static final int SURFACE = ColorUtil.rgba(18, 18, 20, 51);
   public static final int SIDEBAR = ColorUtil.rgba(25, 26, 29, 13);
   public static final int SIDEBAR_STROKE = ColorUtil.rgba(229, 233, 242, 23);
   public static final int CONTROL = ColorUtil.rgba(255, 255, 255, 10);
   public static final int CONTROL_HOVER = ColorUtil.rgba(255, 255, 255, 17);
   public static final int CONTROL_ACTIVE = ColorUtil.rgba(255, 255, 255, 24);
   public static final int CONTROL_STROKE = ColorUtil.rgba(255, 255, 255, 18);
   public static final int STROKE = ColorUtil.rgba(217, 217, 217, 26);
   public static final int CARD_STROKE = ColorUtil.rgba(217, 217, 217, 48);
   public static final int STROKE_SOFT = ColorUtil.rgba(217, 217, 217, 18);
   public static final int DIVIDER = ColorUtil.rgba(217, 217, 217, 18);
   public static final int RULE = ColorUtil.rgb(217, 217, 217);
   public static final int TEXT = -1;
   public static final int TEXT_STRONG = ColorUtil.rgba(255, 255, 255, 204);
   public static final int TEXT_ACTIVE_VALUE = ColorUtil.rgba(255, 255, 255, 179);
   public static final int TEXT_SECONDARY = ColorUtil.rgba(255, 255, 255, 128);
   public static final int TEXT_MUTED = ColorUtil.rgba(255, 255, 255, 102);
   public static final int TEXT_FAINT = ColorUtil.rgba(255, 255, 255, 77);
   public static final int ICON_INACTIVE = ColorUtil.rgba(235, 237, 243, 115);
   public static final int OFF_TRACK = ColorUtil.rgb(45, 44, 45);
   public static final int OFF_KNOB = ColorUtil.rgb(75, 74, 75);
   public static final int SHADOW = ColorUtil.rgba(0, 0, 0, 64);

   private ClickGuiPalette() {
   }

   public static int accent() {
      return Theme.getAccent();
   }
}
