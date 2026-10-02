package org.ryzen.feature.impl.visual;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.TextSetting;

@Environment(EnvType.CLIENT)
public final class BoardSpooferFeature extends Feature {
   private static final String RANK = "Rank";
   private static final String COINS = "Coins";
   private static final String TOKENS = "Tokens";
   private static final Pattern RANK_LINE = Pattern.compile("(?iu)(ранг|rank)(\\s*[:：]\\s*)(.*)");
   private static final Pattern COINS_LINE = Pattern.compile("(?iu)(монет(?:ы|)|coins?)(\\s*[:：]\\s*)([-+0-9 ,.]+)");
   private static final Pattern TOKENS_LINE = Pattern.compile("(?iu)(токен(?:ы|ов|)|tokens?)(\\s*[:：]\\s*)([-+0-9 ,.]+)");
   public final MultiSelectSetting elements = this.register(new MultiSelectSetting("Elements", List.of("Rank", "Coins", "Tokens"), "Rank", "Coins", "Tokens"));
   public final ModeSetting rank = this.register(
      new ModeSetting("Privilege", "Игрок", "Игрок", "Барон", "Страж", "Герой", "Аспид", "Сквид", "Глава", "Элита", "Титан", "Принц", "Князь", "Герцог")
         .renamedFrom("Player", "Игрок")
         .renamedFrom("Baron", "Барон")
         .renamedFrom("Guardian", "Страж")
         .renamedFrom("Hero", "Герой")
         .renamedFrom("Aspid", "Аспид")
         .renamedFrom("Squid", "Сквид")
         .renamedFrom("Head", "Глава")
         .renamedFrom("Elite", "Элита")
         .renamedFrom("Titan", "Титан")
         .renamedFrom("Prince", "Принц")
         .renamedFrom("Duke", "Князь")
         .renamedFrom("Herzog", "Герцог")
         .visibleWhen(() -> this.elements.isSelected("Rank"))
   );
   public final TextSetting coins = this.register(new TextSetting("Coins", "0", 18).visibleWhen(() -> this.elements.isSelected("Coins")));
   public final TextSetting tokens = this.register(new TextSetting("Tokens", "0", 18).visibleWhen(() -> this.elements.isSelected("Tokens")));

   public BoardSpooferFeature() {
      super("Board Spoofer", "Changes rank and currency values shown in the scoreboard", FeatureCategory.VISUAL, -1);
   }

   public static class_2561 transform(class_2561 original) {
      BoardSpooferFeature feature = FeatureManager.INSTANCE.getEnabled(BoardSpooferFeature.class);
      if (feature != null && original != null) {
         String value = original.getString();
         String replaced = value;
         class_124 formatting = null;
         if (feature.elements.isSelected("Rank")) {
            Matcher matcher = RANK_LINE.matcher(replaced);
            if (matcher.find()) {
               replaced = matcher.replaceFirst(Matcher.quoteReplacement(matcher.group(1) + matcher.group(2) + feature.rank.getValue()));
               formatting = rankColor(feature.rank.getValue());
            }
         }

         if (feature.elements.isSelected("Coins")) {
            replaced = replace(COINS_LINE, replaced, sanitizeNumber(feature.coins.getValue()));
         }

         if (feature.elements.isSelected("Tokens")) {
            replaced = replace(TOKENS_LINE, replaced, sanitizeNumber(feature.tokens.getValue()));
         }

         if (replaced.equals(value)) {
            return original;
         }

         class_2561 result = class_2561.method_43470(replaced).method_27696(original.method_10866());
         return (class_2561)(formatting == null ? result : result.method_27661().method_27692(formatting));
      } else {
         return original;
      }
   }

   private static String replace(Pattern pattern, String input, String replacement) {
      Matcher matcher = pattern.matcher(input);
      return !matcher.find() ? input : matcher.replaceFirst(Matcher.quoteReplacement(matcher.group(1) + matcher.group(2) + replacement));
   }

   private static String sanitizeNumber(String raw) {
      String digits = raw == null ? "" : raw.replaceAll("[^0-9]", "");
      if (digits.isEmpty()) {
         return "0";
      }

      try {
         return String.format(Locale.US, "%,d", Long.parseLong(digits));
      } catch (NumberFormatException ignored) {
         return digits.substring(0, Math.min(18, digits.length()));
      }
   }

   private static class_124 rankColor(String rank) {
      return switch (rank.toLowerCase(Locale.ROOT)) {
         case "страж", "guardian" -> class_124.field_1054;
         case "барон", "сквид", "baron", "squid" -> class_124.field_1075;
         case "герой", "hero" -> class_124.field_1060;
         case "аспид", "aspid" -> class_124.field_1062;
         case "глава", "титан", "head", "titan" -> class_124.field_1065;
         case "элита", "elite" -> class_124.field_1064;
         case "принц", "князь", "prince", "duke" -> class_124.field_1061;
         case "герцог", "herzog" -> class_124.field_1079;
         default -> class_124.field_1068;
      };
   }
}
