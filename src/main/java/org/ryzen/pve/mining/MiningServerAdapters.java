package org.ryzen.pve.mining;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.pve.server.ServerProfile;

@Environment(EnvType.CLIENT)
public final class MiningServerAdapters {
   private static final MiningServerAdapter GENERIC = new MiningServerAdapters.GenericMiningAdapter();
   private static final MiningServerAdapter FUNTIME = new MiningServerAdapters.FunTimeMiningAdapter();

   private MiningServerAdapters() {
   }

   public static MiningServerAdapter resolve(String mode, ServerProfile detectedProfile) {
      String normalized = mode == null ? "auto" : mode.trim().toLowerCase(Locale.ROOT);
      if ("generic".equals(normalized) || "holyworld".equals(normalized) || "none".equals(normalized)) {
         return GENERIC;
      } else if ("funtime".equals(normalized)) {
         return FUNTIME;
      } else {
         return detectedProfile == ServerProfile.FUNTIME ? FUNTIME : GENERIC;
      }
   }

   public static MiningServerAdapter forProfile(ServerProfile profile) {
      return profile == ServerProfile.FUNTIME ? FUNTIME : GENERIC;
   }

   private static String normalize(String value) {
      return value == null ? "" : Normalizer.normalize(value, Form.NFKC).replaceAll("(?i)§[0-9A-FK-ORX]", "").trim();
   }

   private static int parseSeconds(Matcher matcher) {
      return Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2));
   }

   private static boolean safeArgument(String value) {
      return value.length() <= 32 && value.matches("[A-Za-z0-9_.:-]+");
   }

   @Environment(EnvType.CLIENT)
   private static final class FunTimeMiningAdapter extends MiningServerAdapters.GenericMiningAdapter {
      private static final Pattern NEXT = Pattern.compile("(?iu)следующая\\s*:\\s*(.+)");
      private static final Pattern TIMER = Pattern.compile("(?<!\\d)(\\d{1,3}):([0-5]\\d)(?!\\d)");

      @Override
      public Optional<MineTimer> parseMineTimer(List<String> hologramLines, long nowMillis) {
         if (hologramLines != null && hologramLines.size() >= 4) {
            for (int index = 0; index <= hologramLines.size() - 4; index++) {
               String header = MiningServerAdapters.normalize(hologramLines.get(index)).toLowerCase(Locale.ROOT);
               String nextLine = MiningServerAdapters.normalize(hologramLines.get(index + 1));
               String refresh = MiningServerAdapters.normalize(hologramLines.get(index + 2)).toLowerCase(Locale.ROOT);
               String timerLine = MiningServerAdapters.normalize(hologramLines.get(index + 3));
               Matcher nextMatcher = NEXT.matcher(nextLine);
               Matcher timerMatcher = TIMER.matcher(timerLine);
               if ((header.contains("авто-шахта") || header.contains("авто шахта"))
                  && nextMatcher.find()
                  && refresh.contains("обновление через")
                  && timerMatcher.matches()) {
                  return Optional.of(new MineTimer(nextMatcher.group(1).trim(), MiningServerAdapters.parseSeconds(timerMatcher), nowMillis));
               }
            }

            return Optional.empty();
         } else {
            return Optional.empty();
         }
      }

      @Override
      public Optional<String> warpMineCommand() {
         return Optional.of("warp mine");
      }

      @Override
      public Optional<String> homeCommand(String homeName) {
         if (homeName != null && !homeName.isBlank()) {
            String value = homeName.trim();
            return MiningServerAdapters.safeArgument(value) ? Optional.of("home " + value) : Optional.empty();
         } else {
            return Optional.of("home");
         }
      }

      @Override
      public Optional<String> anarchyCommand(int number) {
         return number > 0 ? Optional.of("an " + number) : Optional.empty();
      }

      @Override
      public Optional<String> randomTeleportCommand(String size) {
         return "Big".equalsIgnoreCase(size) ? Optional.of("rtp far") : Optional.of("rtp");
      }
   }

   @Environment(EnvType.CLIENT)
   private static class GenericMiningAdapter implements MiningServerAdapter {
      private static final Pattern TIMER = Pattern.compile("(?<!\\d)(\\d{1,3}):([0-5]\\d)(?!\\d)");
      private static final Pattern NEXT = Pattern.compile("(?i)(?:next(?:\\s+mine)?|type)\\s*:\\s*(.+)");

      @Override
      public Optional<MineTimer> parseMineTimer(List<String> hologramLines, long nowMillis) {
         if (hologramLines != null && !hologramLines.isEmpty()) {
            String next = null;
            Integer seconds = null;

            for (String rawLine : hologramLines) {
               String line = MiningServerAdapters.normalize(rawLine);
               Matcher nextMatcher = NEXT.matcher(line);
               if (nextMatcher.find()) {
                  next = nextMatcher.group(1).trim();
               }

               Matcher timerMatcher = TIMER.matcher(line);
               if (timerMatcher.find()) {
                  seconds = MiningServerAdapters.parseSeconds(timerMatcher);
               }
            }

            return next != null && seconds != null ? Optional.of(new MineTimer(next, seconds, nowMillis)) : Optional.empty();
         } else {
            return Optional.empty();
         }
      }
   }
}
