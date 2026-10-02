package org.ryzen.utils.render;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;

@Environment(EnvType.CLIENT)
public final class Textures {
   private static final List<class_2960> ALL = new ArrayList<>();
   public static final class_2960 TARGET = texture("target.png");
   public static final class_2960 TARGET_SKULL = texture("target_skull.png");

   private Textures() {
   }

   public static List<class_2960> all() {
      touch(
         Textures.Logos.BOOT,
         Textures.Hud.ARROW_OUTLINE,
         Textures.Shader.BLOOM,
         Textures.Header.SEARCH,
         Textures.Icons.BOXES,
         Textures.ClickGui.AVATAR,
         Textures.Title.LOGO,
         Textures.AltManager.LOGO
      );
      synchronized (ALL) {
         return List.copyOf(ALL);
      }
   }

   private static void touch(class_2960... constants) {
   }

   private static class_2960 svg(String menuPath) {
      return texture("menu/" + menuPath + ".svg");
   }

   private static class_2960 texture(String path) {
      return register(class_2960.method_60654("ryzen:textures/" + path));
   }

   private static class_2960 register(class_2960 id) {
      synchronized (ALL) {
         ALL.add(id);
         return id;
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class AltManager {
      public static final class_2960 ACCENT_SHAPE = Textures.texture("gui/alt_manager/icons/accent_shape.svg");
      public static final class_2960 ADD = Textures.texture("gui/alt_manager/icons/add.svg");
      public static final class_2960 BACK = Textures.texture("gui/alt_manager/icons/back.svg");
      public static final class_2960 CARD_STAR_ORANGE = Textures.texture("gui/alt_manager/icons/card_star_orange.svg");
      public static final class_2960 CARD_STAR_OUTLINE = Textures.texture("gui/alt_manager/icons/card_star_outline.svg");
      public static final class_2960 CARD_STAR_WHITE = Textures.texture("gui/alt_manager/icons/card_star_white.svg");
      public static final class_2960 DATE = Textures.texture("gui/alt_manager/icons/date.svg");
      public static final class_2960 DELETE = Textures.texture("gui/alt_manager/icons/delete.svg");
      public static final class_2960 FAVORITE_LARGE = Textures.texture("gui/alt_manager/icons/favorite_large.svg");
      public static final class_2960 LOGO = Textures.texture("gui/alt_manager/icons/logo.svg");
      public static final class_2960 PROFILE = Textures.texture("gui/alt_manager/icons/profile.svg");
      public static final class_2960 RANDOM = Textures.texture("gui/alt_manager/icons/random.svg");
      public static final class_2960 SEARCH = Textures.texture("gui/alt_manager/icons/search.svg");
      public static final class_2960 SELECT = Textures.texture("gui/alt_manager/icons/select.svg");
      public static final class_2960 SELECTED_CHECK = Textures.texture("gui/alt_manager/icons/selected_check.svg");
      public static final class_2960 USER_INPUT = Textures.texture("gui/alt_manager/icons/user_input.svg");

      private AltManager() {
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class ClickGui {
      public static final class_2960 AVATAR = Textures.texture("menu/clickgui/avatar.png");
      public static final class_2960 APP_ICON = Textures.texture("gui/icon_32.png");
      public static final class_2960 ENDER_PEARL = Textures.texture("menu/clickgui/ender_pearl.png");

      private ClickGui() {
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class Header {
      public static final class_2960 SEARCH = Textures.svg("header/search");
      public static final class_2960 CHEVRON_LEFT = Textures.svg("header/chevron_left");
      public static final class_2960 CHEVRON_RIGHT = Textures.svg("header/chevron_right");
      public static final class_2960 SETTINGS = Textures.svg("header/settings");
      public static final class_2960 FRIENDS = Textures.svg("header/friends");
      public static final class_2960 PROFILE_ADD = Textures.svg("header/profile_add");
      public static final class_2960 DOCUMENT = Textures.svg("header/document");
      public static final class_2960 HUD = Textures.svg("header/hud");
      public static final class_2960 DEV_AVATAR = Textures.texture("menu/header/dev.png");

      private Header() {
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class Hud {
      public static final class_2960 ARROW_OUTLINE = Textures.texture("hud/arrow.png");
      public static final class_2960 ARROW_FILLED = Textures.texture("hud/filled_arrow.png");

      private Hud() {
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class Icons {
      public static final class_2960 AUTOBUY = Textures.svg("icons/autobuy");
      public static final class_2960 BOXES = Textures.svg("icons/boxes");
      public static final class_2960 BRAIN = Textures.svg("icons/brain");
      public static final class_2960 CALLOUT_POINTER = Textures.svg("icons/callout_pointer");
      public static final class_2960 CHECK = Textures.svg("icons/check");
      public static final class_2960 CHEVRON_DOWN = Textures.svg("icons/chevron_down");
      public static final class_2960 CHEVRON_UP = Textures.svg("icons/chevron_up");
      public static final class_2960 CHEVRONS_LEFT_RIGHT = Textures.svg("icons/chevrons_left_right");
      public static final class_2960 CHEVRONS_LEFT_RIGHT_ELLIPSIS = Textures.svg("icons/chevrons_left_right_ellipsis");
      public static final class_2960 CIRCLE_PLUS = Textures.svg("icons/circle_plus");
      public static final class_2960 COMMAND = Textures.svg("icons/command");
      public static final class_2960 DELETE = Textures.svg("icons/delete");
      public static final class_2960 DELETE_LEFT = Textures.svg("icons/delete_left");
      public static final class_2960 DICES = Textures.svg("icons/dices");
      public static final class_2960 EYE = Textures.svg("icons/eye");
      public static final class_2960 FILTER_LINES = Textures.svg("icons/filter_lines");
      public static final class_2960 FOLDER = Textures.svg("icons/folder");
      public static final class_2960 UPLOAD = Textures.svg("icons/upload");
      public static final class_2960 GAMEPAD = Textures.svg("icons/gamepad_2");
      public static final class_2960 GIFT = Textures.svg("icons/gift");
      public static final class_2960 HARD_DRIVE = Textures.svg("icons/hard_drive");
      public static final class_2960 KEYBOARD = Textures.svg("icons/keyboard");
      public static final class_2960 MOVE_3D = Textures.svg("icons/move_3d");
      public static final class_2960 OPTION = Textures.svg("icons/option");
      public static final class_2960 PERSON_STANDING = Textures.svg("icons/person_standing");
      public static final class_2960 PALETTE = Textures.svg("icons/palette");
      public static final class_2960 GLOBE = Textures.svg("icons/globe");
      public static final class_2960 PIN = Textures.svg("icons/pin");
      public static final class_2960 POINTER_CLICK = Textures.svg("icons/pointer_click");
      public static final class_2960 PLUS = Textures.svg("icons/plus");
      public static final class_2960 REFRESH_CCW = Textures.svg("icons/refresh_ccw");
      public static final class_2960 SCAN_HEART = Textures.svg("icons/scan_heart");
      public static final class_2960 SHIRT = Textures.svg("icons/shirt");
      public static final class_2960 SPARKLES = Textures.svg("icons/sparkles");
      public static final class_2960 SWORDS = Textures.svg("icons/swords");
      public static final class_2960 TRIANGLE_ALERT = Textures.svg("icons/triangle_alert");
      public static final class_2960 USER_ROUND = Textures.svg("icons/user_round");
      public static final class_2960 USER_ROUND_CHECK = Textures.svg("icons/user_round_check");
      public static final class_2960 USER_ROUND_PLUS = Textures.svg("icons/user_round_plus");
      public static final class_2960 X = Textures.svg("icons/x");

      private Icons() {
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class Logos {
      public static final class_2960 BOOT = Textures.texture("menu/logo_boot.svg");
      public static final class_2960 BOLT = Textures.texture("hud/logo_bolt.svg");
      public static final class_2960 AUTOBUY = Textures.svg("autobuy/mark");

      private Logos() {
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class Shader {
      public static final class_2960 BLOOM = Textures.texture("shader/bloom.png");
      public static final class_2960 JUMP_FREQUENCY = Textures.texture("shader/jump_frequency.png");

      private Shader() {
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class Title {
      public static final class_2960 LOGO = Textures.svg("title/logo");
      public static final class_2960 AVATAR = Textures.texture("menu/title/avatar.png");
      public static final class_2960 SINGLEPLAYER_BG = Textures.texture("menu/title/singleplayer_background.png");
      public static final class_2960 SINGLEPLAYER = Textures.svg("title/singleplayer");
      public static final class_2960 MULTIPLAYER = Textures.svg("title/multiplayer");
      public static final class_2960 ACCOUNTS = Textures.svg("title/accounts");
      public static final class_2960 OPTIONS = Textures.svg("title/options");
      public static final class_2960 QUIT = Textures.svg("title/quit");
      public static final class_2960 PROFILE = Textures.svg("title/profile");
      public static final class_2960 CHEVRON = Textures.svg("title/chevron");
      public static final class_2960 DISCORD = Textures.svg("title/discord");
      public static final class_2960 TELEGRAM = Textures.svg("title/telegram");

      private Title() {
      }
   }
}
