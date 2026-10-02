package org.ryzen.utils.render;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public final class Theme {
   private static int accentIndex;

   public static int getAccent() {
      return Theme.Colors.ACCENTS[accentIndex];
   }

   public static int accent(int index) {
      return Theme.Colors.ACCENTS[MathUtil.clamp(index, 0, Theme.Colors.ACCENTS.length - 1)];
   }

   public static int accentCount() {
      return Theme.Colors.ACCENTS.length;
   }

   public static void setAccentIndex(int index) {
      accentIndex = MathUtil.clamp(index, 0, Theme.Colors.ACCENTS.length - 1);
   }

   public static int accentIndex() {
      return accentIndex;
   }

   public static void saveAccentPreset(int index, int color) {
      Theme.Colors.ACCENTS[MathUtil.clamp(index, 0, Theme.Colors.ACCENTS.length - 1)] = ColorUtil.withAlpha(color, 255);
      accentIndex = MathUtil.clamp(index, 0, Theme.Colors.ACCENTS.length - 1);
   }

   @Generated
   private Theme() {
   }

   @Environment(EnvType.CLIENT)
   public static final class Colors {
      public static final int PANEL = ColorUtil.rgb(21, 21, 22);
      public static final int BACKGROUND_PRIMARY_50 = ColorUtil.rgba(21, 21, 22, 128);
      public static final int PANEL_BORDER = ColorUtil.rgba(255, 255, 255, 8);
      public static final int PANEL_SHADOW = ColorUtil.rgba(0, 0, 0, 38);
      public static final int PANEL_SHADOW_STRONG = ColorUtil.rgba(0, 0, 0, 89);
      public static final int POPUP_SHADOW = ColorUtil.rgba(0, 0, 0, 102);
      public static final int OVERLAY = ColorUtil.rgb(17, 17, 18);
      public static final int CARD = ColorUtil.rgb(29, 29, 30);
      public static final int CARD_HOVER = ColorUtil.rgb(36, 36, 38);
      public static final int MODULE_CARD = ColorUtil.rgb(27, 27, 29);
      public static final int MODULE_CARD_HOVER = ColorUtil.rgb(32, 32, 35);
      public static final int BACKGROUND_SURFACE_S = ColorUtil.rgb(27, 27, 28);
      public static final int BACKGROUND_SURFACE_M = ColorUtil.rgb(34, 34, 36);
      public static final int CONTROL = ColorUtil.rgb(40, 40, 42);
      public static final int CONTROL_ALT = ColorUtil.rgb(40, 40, 43);
      public static final int CONTROL_STRONG = ColorUtil.rgb(41, 41, 44);
      public static final int CONTROL_HOVER = ColorUtil.rgb(48, 48, 52);
      public static final int CONTROL_IDLE = ColorUtil.rgb(39, 39, 42);
      public static final int OUTLINES_SMALL = ColorUtil.rgba(255, 255, 255, 8);
      public static final int OUTLINES_MEDIUM = ColorUtil.rgba(255, 255, 255, 13);
      public static final int OUTLINES_LARGE = ColorUtil.rgba(255, 255, 255, 20);
      public static final int SYSTEM_INFORMATION = ColorUtil.rgb(0, 145, 255);
      public static final int SYSTEM_RED = ColorUtil.rgb(237, 106, 95);
      public static final int CONTROL_SHADOW = ColorUtil.rgba(0, 0, 0, 26);
      public static final int TEXT_TITLE = -1;
      public static final int TEXT_TEXT = ColorUtil.rgb(224, 224, 233);
      public static final int TEXT = ColorUtil.rgb(240, 240, 240);
      public static final int PRIMARY = ColorUtil.rgb(243, 243, 244);
      public static final int PRIMARY_BRIGHT = ColorUtil.rgb(244, 244, 244);
      public static final int SECONDARY = ColorUtil.rgb(162, 162, 168);
      public static final int SECONDARY_DARK = ColorUtil.rgb(143, 143, 150);
      public static final int SECTION = ColorUtil.rgb(133, 133, 139);
      public static final int DIVIDER = ColorUtil.rgba(255, 255, 255, 18);
      public static final int DIVIDER_SUBTLE = ColorUtil.rgba(255, 255, 255, 15);
      public static final int DIVIDER_HEADER = ColorUtil.rgba(255, 255, 255, 8);
      public static final int SEPARATOR = ColorUtil.rgba(255, 255, 255, 20);
      public static final int SURFACE_ACTIVE = ColorUtil.rgba(255, 255, 255, 24);
      public static final int SURFACE_HOVER = ColorUtil.rgba(255, 255, 255, 12);
      public static final int FOCUS = ColorUtil.rgba(255, 255, 255, 128);
      public static final int ICON = ColorUtil.rgb(134, 134, 139);
      public static final int ICON_MUTED = ColorUtil.rgb(184, 184, 188);
      public static final int ICON_GHOST = ColorUtil.rgb(80, 80, 83);
      public static final int TEXT_GHOST = ICON_GHOST;
      public static final int TRAFFIC_CLOSE = ColorUtil.rgb(237, 106, 95);
      public static final int TRAFFIC_MINIMIZE = ColorUtil.rgb(245, 191, 79);
      public static final int TRAFFIC_MAXIMIZE = ColorUtil.rgb(97, 197, 84);
      public static final int RISK = TRAFFIC_MINIMIZE;
      public static final int EXTRA_RISK = TRAFFIC_CLOSE;
      public static final int DOCK = ColorUtil.rgba(29, 29, 31, 194);
      public static final int DOCK_BORDER = ColorUtil.rgba(255, 255, 255, 36);
      public static final int DOCK_SHADOW = ColorUtil.rgba(0, 0, 0, 56);
      public static final int[] ACCENTS = new int[]{
         ColorUtil.rgb(21, 154, 242),
         ColorUtil.rgb(103, 71, 232),
         ColorUtil.rgb(155, 67, 228),
         ColorUtil.rgb(213, 68, 199),
         ColorUtil.rgb(242, 77, 114),
         ColorUtil.rgb(255, 125, 32),
         ColorUtil.rgb(255, 141, 80),
         ColorUtil.rgb(255, 189, 80),
         ColorUtil.rgb(255, 219, 101),
         ColorUtil.rgb(143, 208, 93)
      };

      @Generated
      private Colors() {
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class Sizes {
      public static final int HEADER_HEIGHT = 54;
      public static final int DEFAULT_PANEL_RADIUS_INDEX = 2;
      public static final int[] PANEL_RADII = new int[]{6, 12, 20};
      public static final int MODULE_CARD_RADIUS = 8;
      public static final int MODULE_CARD_HEADER_HEIGHT = 64;
      public static final int MODULE_CARD_ROW_HEIGHT = 40;
      public static final int MODULE_CARD_PADDING = 16;
      public static final int MODULE_CARD_SWITCH_WIDTH = 36;
      public static final int MODULE_CARD_SWITCH_HEIGHT = 16;
      public static final int MODULE_CARD_ACTION_ICON = 12;
      public static final int SLIDER_WIDTH = 128;
      public static final int SLIDER_HEIGHT = 16;
      public static final int SLIDER_PADDING = 2;
      public static final int SLIDER_TRACK_HEIGHT = 12;
      public static final int SLIDER_KNOB_SIZE = 14;
      public static final float SLIDER_SHADOW_BLUR = 7.5F;
      public static final int MODULE_CARD_SLIDER_WIDTH = 128;
      public static final int MODULE_CARD_SLIDER_HEIGHT = 16;
      public static final int COLOR_PREVIEW_WIDTH = 24;
      public static final int COLOR_PREVIEW_HEIGHT = 16;
      public static final int COLOR_PREVIEW_PADDING = 2;
      public static final int COLOR_PREVIEW_RADIUS = 6;
      public static final int COLOR_BLOCK_RADIUS = 4;
      public static final int COLOR_PICKER_WIDTH = 144;
      public static final int COLOR_PICKER_HEIGHT = 122;
      public static final int COLOR_PICKER_PADDING = 6;
      public static final int COLOR_PICKER_RADIUS = 12;
      public static final int COLOR_PICKER_GRID_WIDTH = 132;
      public static final int COLOR_PICKER_GRID_HEIGHT = 110;
      public static final int COLOR_PICKER_GRID_RADIUS = 10;
      public static final int COLOR_PICKER_CELL_SIZE = 11;
      public static final int COLOR_PICKER_COLUMNS = 12;
      public static final int COLOR_PICKER_OFFSET_Y = 4;
      public static final int DROPDOWN_VALUE_WIDTH = 112;
      public static final int DROPDOWN_VALUE_HEIGHT = 24;
      public static final int DROPDOWN_GAP = 4;
      public static final int DROPDOWN_ICON_SIZE = 24;
      public static final int DROPDOWN_ICON_PADDING = 4;
      public static final int DROPDOWN_POPUP_WIDTH = 144;
      public static final int DROPDOWN_POPUP_RADIUS = 12;
      public static final int DROPDOWN_POPUP_PADDING = 6;
      public static final int DROPDOWN_POPUP_GAP = 2;
      public static final int DROPDOWN_POPUP_ITEM_HEIGHT = 32;
      public static final int DROPDOWN_POPUP_ITEM_RADIUS = 8;
      public static final int DROPDOWN_POPUP_TEXT_WIDTH = 108;
      public static final int DROPDOWN_POPUP_OFFSET_Y = 4;
      public static final int INPUT_BIND_WIDTH = 44;
      public static final int INPUT_BIND_HEIGHT = 20;
      public static final int INPUT_BIND_RADIUS = 4;
      public static final int INPUT_BIND_PADDING_X = 8;
      public static final int INPUT_BIND_TEXT_SIZE = 10;
      public static final int INPUT_WIDTH = 128;
      public static final int INPUT_HEIGHT = 24;
      public static final int INPUT_RADIUS = 4;
      public static final int INPUT_PADDING_X = 8;
      public static final int INPUT_PADDING_Y = 4;
      public static final int INPUT_TEXT_WIDTH = 112;
      public static final int INPUT_TEXT_SIZE = 10;
      public static final float HEADER_BRAND_WIDTH = 330.66666F;
      public static final int HEADER_BRAND_HEIGHT = 32;
      public static final int HEADER_BRAND_RADIUS = 8;
      public static final int HEADER_CONTROLS_X = 16;
      public static final int HEADER_CONTROLS_Y = 12;
      public static final int HEADER_CONTROLS_HEIGHT = 32;
      public static final int HEADER_CONTROLS_GAP = 8;
      public static final int HEADER_TRAFFIC_SIZE = 12;
      public static final int HEADER_TRAFFIC_GAP = 10;
      public static final int HEADER_CONTROL_BUTTON_SIZE = 24;
      public static final int HEADER_CONTROL_BUTTON_PADDING = 4;
      public static final int HEADER_NAV_GROUP_WIDTH = 48;
      public static final int HEADER_ICON_SIZE = 16;
      public static final int HEADER_PROFILE_ICON_SIZE = 20;
      public static final int HEADER_DIVIDER_HEIGHT = 12;

      @Generated
      private Sizes() {
      }
   }
}
