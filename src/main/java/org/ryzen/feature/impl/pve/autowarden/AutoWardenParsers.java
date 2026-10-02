package org.ryzen.feature.impl.pve.autowarden;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class AutoWardenParsers {
   public static final int MIN_ANARCHY = 101;
   public static final int MAX_ANARCHY = 905;
   private static final Pattern LEGACY_FORMAT = Pattern.compile("§.");
   private static final Pattern ANARCHY = Pattern.compile("(?iu)(?:анарх(?:ия|ии)?|anarchy|\\bан\\b)[^0-9]{0,12}(\\d{3})");
   private static final Pattern COLON_DURATION = Pattern.compile("(?<!\\d)(?:(\\d{1,2}):)?(\\d{1,2}):(\\d{2})(?!\\d)");
   private static final Pattern SHORT_COLON_DURATION = Pattern.compile("(?<!\\d)(\\d{1,3}):(\\d{2})(?!\\d)");
   private static final Pattern UNIT_DURATION = Pattern.compile(
      "(?iu)(\\d+)\\s*(?:ч(?:ас(?:а|ов)?)?|h(?:ours?)?|м(?:ин(?:ут(?:а|ы)?)?)?|m(?:in(?:utes?)?)?|с(?:ек(?:унд(?:а|ы)?)?)?|s(?:ec(?:onds?)?)?)"
   );
   private static final Pattern NUMBER = Pattern.compile("(?<!\\d)(\\d{1,6})(?!\\d)");
   private static final Pattern COMBAT_SECONDS = Pattern.compile("(?iu)(\\d{1,4})\\s*(?:с(?:ек(?:унд(?:а|ы)?)?)?|s(?:ec(?:onds?)?)?)");
   private static final Pattern AMOUNT = Pattern.compile("(?iu)(\\d[\\d\\s\\u00a0_'.’,]*[\\d]|\\d)(?:[.,](\\d{1,2}))?\\s*([kкmмbб])?");
   private static final Pattern[] ATTACKER_PATTERNS = new Pattern[]{
      Pattern.compile("(?iu)(?:^|\\s)(?:убит(?:а|ы)?|убил(?:а)?)\\s+(?:игроком\\s+)?([A-Za-z0-9_]{3,16})(?=$|\\s|[.,!])"),
      Pattern.compile("(?iu)\\b(?:slain|killed)\\s+by\\s+([A-Za-z0-9_]{3,16})\\b"),
      Pattern.compile("(?iu)\\b([A-Za-z0-9_]{3,16})\\s+(?:убил|killed)\\s+вас\\b")
   };
   private static final Set<String> TIMER_MARKERS = Set.of("откр", "таймер", "сундук", "chest", "open", "timer", "remaining", "остал");

   private AutoWardenParsers() {
   }

   public static OptionalInt parseHomeAnarchy(String text) {
      if (text != null && !text.isBlank()) {
         try {
            int value = Integer.parseInt(text.trim());
            return isValidAnarchy(value) ? OptionalInt.of(value) : OptionalInt.empty();
         } catch (NumberFormatException ignored) {
            return OptionalInt.empty();
         }
      } else {
         return OptionalInt.empty();
      }
   }

   public static List<Integer> parseLootAnarchies(String text, int homeAnarchy) {
      if (text != null && !text.isBlank()) {
         LinkedHashSet<Integer> values = new LinkedHashSet<>();

         for (String token : text.split("[^0-9]+")) {
            if (!token.isEmpty()) {
               try {
                  int value = Integer.parseInt(token);
                  if (isValidAnarchy(value) && value != homeAnarchy) {
                     values.add(value);
                  }
               } catch (NumberFormatException var8) {
               }
            }
         }

         return List.copyOf(values);
      } else {
         return List.of();
      }
   }

   public static OptionalInt parseAnarchy(String text) {
      String plain = plain(text);
      Matcher matcher = ANARCHY.matcher(plain);

      while (matcher.find()) {
         int value = Integer.parseInt(matcher.group(1));
         if (isValidAnarchy(value)) {
            return OptionalInt.of(value);
         }
      }

      return OptionalInt.empty();
   }

   public static Optional<AutoWardenParsers.TimerReading> parseChestTimer(String text) {
      String plain = plain(text).trim();
      if (plain.isEmpty()) {
         return Optional.empty();
      }

      String normalized = normalize(plain);
      boolean hasMarker = TIMER_MARKERS.stream().anyMatch(normalized::contains);
      boolean durationOnly = normalized.matches("(?:\\d{1,2}:)?\\d{1,3}:\\d{2}|(?:\\d+\\s*(?:[hчmмsс])\\s*)+");
      if (!hasMarker && !durationOnly) {
         return Optional.empty();
      }

      if (!normalized.contains("открыт")
         && !normalized.contains("открыто")
         && !normalized.contains("open now")
         && !normalized.contains("opened")
         && !normalized.contains("доступен")) {
         Matcher longColon = COLON_DURATION.matcher(normalized);
         if (longColon.find()) {
            int hours = parseGroup(longColon, 1);
            int minutes = parseGroup(longColon, 2);
            int seconds = parseGroup(longColon, 3);
            if (minutes < 60 && seconds < 60) {
               return Optional.of(new AutoWardenParsers.TimerReading(saturatedSeconds(hours, minutes, seconds), false));
            }
         }

         Matcher shortColon = SHORT_COLON_DURATION.matcher(normalized);
         if (shortColon.find()) {
            int minutes = parseGroup(shortColon, 1);
            int seconds = parseGroup(shortColon, 2);
            if (seconds < 60) {
               return Optional.of(new AutoWardenParsers.TimerReading(saturatedSeconds(0, minutes, seconds), false));
            }
         }

         Matcher units = UNIT_DURATION.matcher(normalized);
         long seconds = 0L;
         boolean foundUnit = false;

         while (units.find()) {
            foundUnit = true;
            long value = Long.parseLong(units.group(1));
            String unit = units.group().substring(units.group(1).length()).trim();
            if (unit.startsWith("ч") || unit.startsWith("h")) {
               seconds += value * 3600L;
            } else if (!unit.startsWith("м") && !unit.startsWith("m")) {
               seconds += value;
            } else {
               seconds += value * 60L;
            }

            if (seconds >= 2147483647L) {
               return Optional.of(new AutoWardenParsers.TimerReading(Integer.MAX_VALUE, false));
            }
         }

         if (foundUnit) {
            return Optional.of(new AutoWardenParsers.TimerReading((int)seconds, seconds == 0L));
         }

         if (hasMarker) {
            Matcher number = NUMBER.matcher(normalized);
            if (number.find()) {
               return Optional.of(new AutoWardenParsers.TimerReading(Integer.parseInt(number.group(1)), false));
            }
         }

         return Optional.empty();
      } else {
         return Optional.of(new AutoWardenParsers.TimerReading(0, true));
      }
   }

   public static OptionalLong parseCombatHoldMillis(String text) {
      String normalized = normalize(plain(text));
      boolean combat = normalized.contains("недавно были в бою")
         || normalized.contains("режим pvp")
         || normalized.contains("combat")
         || normalized.contains("телепорт");
      if (!combat) {
         return OptionalLong.empty();
      }

      Matcher matcher = COMBAT_SECONDS.matcher(normalized);
      return matcher.find() ? OptionalLong.of((Long.parseLong(matcher.group(1)) + 1L) * 1000L) : OptionalLong.of(10000L);
   }

   public static boolean indicatesFullAnarchy(String text) {
      String normalized = normalize(plain(text));
      return normalized.contains("заполн") && (normalized.contains("кикнут") || normalized.contains("подключени") || normalized.contains("full"));
   }

   public static Optional<String> parseAttacker(String text) {
      String plain = plain(text);

      for (Pattern pattern : ATTACKER_PATTERNS) {
         Matcher matcher = pattern.matcher(plain);
         if (matcher.find()) {
            return Optional.of(matcher.group(1));
         }
      }

      return Optional.empty();
   }

   public static OptionalLong parseCurrencyAmount(String text) {
      if (text != null && !text.isBlank()) {
         long largest = -1L;
         Matcher matcher = AMOUNT.matcher(plain(text));

         while (matcher.find()) {
            String wholeDigits = matcher.group(1).replaceAll("\\D", "");
            if (!wholeDigits.isEmpty()) {
               try {
                  long whole = Long.parseLong(wholeDigits);
                  String fraction = matcher.group(2);
                  String suffix = matcher.group(3);

                  long multiplier = switch (suffix == null ? "" : suffix.toLowerCase(Locale.ROOT)) {
                     case "k", "к" -> 1000L;
                     case "m", "м" -> 1000000L;
                     case "b", "б" -> 1000000000L;
                     default -> 1L;
                  };
                  double value = whole;
                  if (fraction != null && multiplier > 1L) {
                     value += Integer.parseInt(fraction) / Math.pow(10.0, fraction.length());
                  }

                  largest = Math.max(largest, Math.round(value * multiplier));
               } catch (NumberFormatException var13) {
               }
            }
         }

         return largest < 0L ? OptionalLong.empty() : OptionalLong.of(largest);
      } else {
         return OptionalLong.empty();
      }
   }

   public static List<String> plainLines(Iterable<? extends CharSequence> lines) {
      List<String> result = new ArrayList<>();
      if (lines != null) {
         for (CharSequence line : lines) {
            if (line != null) {
               result.add(plain(line.toString()));
            }
         }
      }

      return List.copyOf(result);
   }

   public static String plain(String text) {
      return text == null ? "" : LEGACY_FORMAT.matcher(text).replaceAll("");
   }

   public static String normalize(String text) {
      return Normalizer.normalize(plain(text), Form.NFKC).toLowerCase(Locale.ROOT).replace('ё', 'е').trim();
   }

   private static boolean isValidAnarchy(int value) {
      return value >= 101 && value <= 905;
   }

   private static int parseGroup(Matcher matcher, int group) {
      String value = matcher.group(group);
      return value == null ? 0 : Integer.parseInt(value);
   }

   private static int saturatedSeconds(int hours, int minutes, int seconds) {
      long result = hours * 3600L + minutes * 60L + seconds;
      return result >= 2147483647L ? Integer.MAX_VALUE : (int)result;
   }

   @Environment(EnvType.CLIENT)
   public record TimerReading(int remainingSeconds, boolean openNow) {
      public TimerReading {
         remainingSeconds = Math.max(0, remainingSeconds);
         openNow = openNow || remainingSeconds == 0;
      }
   }
}
