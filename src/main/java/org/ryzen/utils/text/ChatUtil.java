package org.ryzen.utils.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import org.ryzen.context.MinecraftContext;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Theme;

@Environment(EnvType.CLIENT)
public final class ChatUtil {
   private static final Pattern EMOJI_ALIAS = Pattern.compile(":[A-Za-z0-9_+\\-]+:");
   private static final String PREFIX = "Ryzen → ";

   public static void print(String message) {
      send(message);
   }

   public static void header(String message) {
      send(message);
   }

   public static void info(String message) {
      send(message);
   }

   public static void success(String message) {
      send(message);
   }

   public static void error(String message) {
      send(message);
   }

   public static void usage(String message) {
      send(message);
   }

   public static void entry(String emoji, String title, String detail) {
      send(title + (detail != null && !detail.isBlank() ? "  •  " + detail : ""));
   }

   public static void send(String message) {
      class_310 mc = MinecraftContext.mc;
      if (mc.field_1705 != null) {
         mc.field_1705.method_1743().method_1812(gradient("Ryzen → " + message));
      }
   }

   private static class_5250 gradient(String text) {
      class_5250 result = class_2561.method_43473();
      int startColor = Theme.getAccent();
      int endColor = ColorUtil.lerp(startColor, -1, 0.62F);
      Matcher matcher = EMOJI_ALIAS.matcher(text);
      int cursor = 0;

      while (cursor < text.length()) {
         if (matcher.find(cursor) && matcher.start() == cursor) {
            appendColored(result, matcher.group(), gradientColor(startColor, endColor, cursor, text.length()));
            cursor = matcher.end();
         } else {
            int codePoint = text.codePointAt(cursor);
            appendColored(result, new String(Character.toChars(codePoint)), gradientColor(startColor, endColor, cursor, text.length()));
            cursor += Character.charCount(codePoint);
         }
      }

      return result;
   }

   private static int gradientColor(int start, int end, int index, int length) {
      return ColorUtil.lerp(start, end, length <= 1 ? 0.0F : (float)index / (length - 1));
   }

   private static void appendColored(class_5250 target, String text, int color) {
      target.method_10852(class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(color & 16777215)));
   }

   @Generated
   private ChatUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
