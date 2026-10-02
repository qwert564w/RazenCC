package org.ryzen.pve.mining;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record MineTimer(String nextType, int initialSeconds, long startedAtMillis) {
   public MineTimer {
      nextType = sanitizeDisplayText(nextType);
      nextType = nextType.isBlank() ? "Unknown" : nextType;
      initialSeconds = Math.max(0, initialSeconds);
   }

   public int secondsLeft(long nowMillis) {
      long elapsed = Math.max(0L, nowMillis - this.startedAtMillis) / 1000L;
      return (int)Math.max(0L, this.initialSeconds - elapsed);
   }

   public boolean isExpired(long nowMillis) {
      return this.secondsLeft(nowMillis) == 0;
   }

   public String formattedTime(long nowMillis) {
      int seconds = this.secondsLeft(nowMillis);
      return String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
   }

   static String sanitizeDisplayText(String value) {
      if (value == null) {
         return "";
      }

      String normalized = Normalizer.normalize(value, Form.NFKC).replaceAll("(?i)§[0-9A-FK-ORX]", "");
      StringBuilder result = new StringBuilder(normalized.length());
      boolean lastWasSpace = false;
      int offset = 0;

      while (offset < normalized.length()) {
         int codePoint = normalized.codePointAt(offset);
         offset += Character.charCount(codePoint);
         if (codePoint == 8211 || codePoint == 8212 || codePoint == 8722) {
            codePoint = 45;
         } else if (codePoint == 8216 || codePoint == 8217) {
            codePoint = 39;
         } else if (codePoint == 8220 || codePoint == 8221) {
            codePoint = 34;
         }

         if (Character.isWhitespace(codePoint)) {
            if (!lastWasSpace && !result.isEmpty()) {
               result.append(' ');
               lastWasSpace = true;
            }
         } else if (isUiGlyph(codePoint)) {
            result.appendCodePoint(codePoint);
            lastWasSpace = false;
         }
      }

      return result.toString().trim();
   }

   private static boolean isUiGlyph(int codePoint) {
      return codePoint >= 32 && codePoint <= 126 || codePoint >= 1024 && codePoint <= 1119;
   }
}
