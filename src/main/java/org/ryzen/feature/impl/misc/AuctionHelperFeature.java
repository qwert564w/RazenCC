package org.ryzen.feature.impl.misc;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1799;
import net.minecraft.class_1844;
import net.minecraft.class_1887;
import net.minecraft.class_2561;
import net.minecraft.class_3489;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_9290;
import net.minecraft.class_9334;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.ColorUtil;

@Environment(EnvType.CLIENT)
public final class AuctionHelperFeature extends Feature {
   private static final Pattern PAGE_FRACTION = Pattern.compile("(?U)\\b\\d+\\s*/\\s*\\d+\\b");
   private static final Pattern AMOUNT = Pattern.compile("(?iu)(\\d+(?:[\\s\\u00A0,_.'’]\\d+)*)(?:\\s*([kкmмbб]))?");
   private static final Pattern EXPIRED = Pattern.compile("(?iu)(?:^|\\W)(?:ист[её]к|истекло|просрочен(?:а|о|ы)?|expired|outdated)(?:\\W|$)");
   private static AuctionHelperFeature instance;
   public final ModeSetting server = this.register(new ModeSetting("Server", "FunTime", "FunTime", "HolyWorld"));
   public final BooleanSetting priceForOneItem = this.register(new BooleanSetting("Price For 1 Item", true));
   public final BooleanSetting donItemInfo = this.register(new BooleanSetting("Don Item Info", true));
   public final BooleanSetting potionEffects = this.register(new BooleanSetting("Potion Effects", true));
   public final BooleanSetting effectDuration = this.register(new BooleanSetting("Effect Duration", true).visibleWhen(() -> this.potionEffects.getValue()));
   public final BooleanSetting highlightDonItems = this.register(new BooleanSetting("Highlight Don Items", true));
   public final BooleanSetting highlightPotions = this.register(new BooleanSetting("Highlight Potions", true));
   public final BooleanSetting highlightExpired = this.register(new BooleanSetting("Highlight Expired", true));
   public final BooleanSetting armorFilter = this.register(new BooleanSetting("Armor Filter", false));
   public final BooleanSetting swordFilter = this.register(new BooleanSetting("Sword Filter", false));
   public final BooleanSetting potionFilter = this.register(new BooleanSetting("Potion Filter", false));
   public final BooleanSetting knownPotionsOnly = this.register(new BooleanSetting("Known Potions Only", true).visibleWhen(() -> this.potionFilter.getValue()));
   public final BooleanSetting maxEffectLevel = this.register(new BooleanSetting("Max Effect Level", true).visibleWhen(() -> this.potionFilter.getValue()));
   public final BooleanSetting fullEffectDuration = this.register(
      new BooleanSetting("Full Effect Duration", true).visibleWhen(() -> this.potionFilter.getValue())
   );
   public final NumberSetting minDurability = this.register(
      new NumberSetting("Min Durability", 0.0, 0.0, 100.0, 1.0, "%").visibleWhen(() -> this.armorFilter.getValue() || this.swordFilter.getValue())
   );
   public final NumberSetting minUnbreaking = this.register(
      new NumberSetting("Min Unbreaking", 0.0, 0.0, 10.0, 1.0, "").visibleWhen(() -> this.armorFilter.getValue() || this.swordFilter.getValue())
   );
   public final BooleanSetting requireMending = this.register(
      new BooleanSetting("Require Mending", false).visibleWhen(() -> this.armorFilter.getValue() || this.swordFilter.getValue())
   );
   public final NumberSetting minProtection = this.register(
      new NumberSetting("Min Protection", 0.0, 0.0, 10.0, 1.0, "").visibleWhen(() -> this.armorFilter.getValue())
   );
   public final BooleanSetting noThorns = this.register(new BooleanSetting("No Thorns", false).visibleWhen(() -> this.armorFilter.getValue()));
   public final NumberSetting minDepthStrider = this.register(
      new NumberSetting("Min Depth Strider", 0.0, 0.0, 5.0, 1.0, "").visibleWhen(() -> this.armorFilter.getValue())
   );
   public final NumberSetting minSharpness = this.register(
      new NumberSetting("Min Sharpness", 0.0, 0.0, 10.0, 1.0, "").visibleWhen(() -> this.swordFilter.getValue())
   );
   public final BooleanSetting noKnockback = this.register(new BooleanSetting("No Knockback", false).visibleWhen(() -> this.swordFilter.getValue()));
   public final BooleanSetting dimRejected = this.register(new BooleanSetting("Dim Rejected", true).visibleWhen(this::hasAnyFilter));
   public final BooleanSetting filterReason = this.register(new BooleanSetting("Filter Reason", true).visibleWhen(this::hasAnyFilter));

   public AuctionHelperFeature() {
      super("AuctionHelper", "FunTime/HolyWorld auction prices and custom items", FeatureCategory.MISC, -1);
      instance = this;
   }

   public static boolean activeFor(String title) {
      return instance != null && instance.isEnabled() && isAuctionTitle(title);
   }

   public static List<class_2561> augmentTooltip(String title, class_1799 stack, List<class_2561> original) {
      if (activeFor(title) && stack != null && !stack.method_7960() && original != null) {
         AuctionHelperFeature.AuctionServer server = instance.selectedServer();
         long totalPrice = server.extractPrice(stack);
         if (totalPrice <= 0L && !server.hasListingMarker(stack)) {
            return original;
         }

         List<class_2561> tooltip = new ArrayList<>(original);
         Optional<DonItems.DonItem> donItem = DonItems.find(stack, server.catalogue);
         if (instance.donItemInfo.getValue()) {
            donItem.ifPresent(item -> insertDonItemInfo(tooltip, item));
         }

         if (instance.priceForOneItem.getValue() && totalPrice > 0L && stack.method_7947() > 1) {
            long unit = Math.max(1L, totalPrice / stack.method_7947());
            server.insertAfterPrice(tooltip, class_2561.method_43470(server.unitPriceLabel(formatPrice(unit))).method_27692(class_124.field_1080));
         }

         if (instance.potionEffects.getValue()) {
            appendPotionEffects(tooltip, stack, server, donItem, instance.effectDuration.getValue());
         }

         AuctionHelperFeature.FilterResult filter = instance.evaluateFilters(stack, donItem);
         if (!filter.accepted && instance.filterReason.getValue()) {
            tooltip.add(class_2561.method_43470("Фильтр: " + filter.reason).method_27692(class_124.field_1061));
         }

         return tooltip;
      } else {
         return original;
      }
   }

   public static int slotOverlayColor(String title, class_1799 stack) {
      if (activeFor(title) && stack != null && !stack.method_7960()) {
         AuctionHelperFeature.AuctionServer server = instance.selectedServer();
         if (server.extractPrice(stack) <= 0L && !server.hasListingMarker(stack)) {
            return 0;
         }

         if (instance.highlightExpired.getValue() && isOutdated(stack)) {
            return ColorUtil.rgba(255, 52, 62, 105);
         }

         Optional<DonItems.DonItem> donItem = DonItems.find(stack, server.catalogue);
         if (instance.dimRejected.getValue() && !instance.evaluateFilters(stack, donItem).accepted) {
            return ColorUtil.rgba(12, 12, 14, 145);
         }

         if (donItem.isPresent()) {
            DonItems.Category category = donItem.get().category();
            boolean potionLike = category == DonItems.Category.POTION || category == DonItems.Category.ARROW;
            if (potionLike && instance.highlightPotions.getValue() || !potionLike && instance.highlightDonItems.getValue()) {
               return categoryOverlay(category);
            }
         }

         return instance.highlightPotions.getValue() && hasRealPotionEffects(stack, server, donItem) ? ColorUtil.rgba(185, 85, 255, 68) : 0;
      } else {
         return 0;
      }
   }

   public static boolean isAuctionTitle(String title) {
      return instance != null && instance.selectedServer().matchesTitle(title);
   }

   public static long extractPrice(class_1799 stack) {
      return instance == null ? -1L : instance.selectedServer().extractPrice(stack);
   }

   public static String formatPrice(long value) {
      return String.format(Locale.ROOT, "%,d", value).replace(',', ' ');
   }

   private AuctionHelperFeature.AuctionServer selectedServer() {
      return this.server.is("HolyWorld") ? AuctionHelperFeature.AuctionServer.HOLYWORLD : AuctionHelperFeature.AuctionServer.FUNTIME;
   }

   private boolean hasAnyFilter() {
      return this.armorFilter.getValue() || this.swordFilter.getValue() || this.potionFilter.getValue();
   }

   private AuctionHelperFeature.FilterResult evaluateFilters(class_1799 stack, Optional<DonItems.DonItem> donItem) {
      if (this.armorFilter.getValue() && isArmor(stack)) {
         AuctionHelperFeature.FilterResult equipment = this.evaluateEquipmentRequirements(stack);
         if (!equipment.accepted) {
            return equipment;
         }

         int protection = enchantmentLevel(stack, "protection");
         int requiredProtection = this.minProtection.getValue().intValue();
         if (protection < requiredProtection) {
            return AuctionHelperFeature.FilterResult.reject("Protection " + protection + " < " + requiredProtection);
         }

         if (this.noThorns.getValue() && enchantmentLevel(stack, "thorns") > 0) {
            return AuctionHelperFeature.FilterResult.reject("есть Thorns");
         }

         if (stack.method_31573(class_3489.field_48294)) {
            int depthStrider = enchantmentLevel(stack, "depth_strider");
            int requiredDepthStrider = this.minDepthStrider.getValue().intValue();
            if (depthStrider < requiredDepthStrider) {
               return AuctionHelperFeature.FilterResult.reject("Depth Strider " + depthStrider + " < " + requiredDepthStrider);
            }
         }
      }

      if (this.swordFilter.getValue() && stack.method_31573(class_3489.field_42611)) {
         AuctionHelperFeature.FilterResult equipment = this.evaluateEquipmentRequirements(stack);
         if (!equipment.accepted) {
            return equipment;
         }

         int sharpness = enchantmentLevel(stack, "sharpness");
         int requiredSharpness = this.minSharpness.getValue().intValue();
         if (sharpness < requiredSharpness) {
            return AuctionHelperFeature.FilterResult.reject("Sharpness " + sharpness + " < " + requiredSharpness);
         }

         if (this.noKnockback.getValue() && enchantmentLevel(stack, "knockback") > 0) {
            return AuctionHelperFeature.FilterResult.reject("есть Knockback");
         }
      }

      return this.potionFilter.getValue() && isPotionOrArrow(stack) ? this.evaluatePotionProfile(stack, donItem) : AuctionHelperFeature.FilterResult.PASS;
   }

   private AuctionHelperFeature.FilterResult evaluateEquipmentRequirements(class_1799 stack) {
      if (stack.method_7963()) {
         double remaining = (stack.method_7936() - stack.method_7919()) * 100.0 / Math.max(1, stack.method_7936());
         if (remaining + 1.0E-6 < this.minDurability.getValue()) {
            return AuctionHelperFeature.FilterResult.reject("прочность " + Math.round(remaining) + "% < " + this.minDurability.getValue().intValue() + "%");
         }
      }

      int unbreaking = enchantmentLevel(stack, "unbreaking");
      int requiredUnbreaking = this.minUnbreaking.getValue().intValue();
      if (unbreaking < requiredUnbreaking) {
         return AuctionHelperFeature.FilterResult.reject("Unbreaking " + unbreaking + " < " + requiredUnbreaking);
      } else {
         return this.requireMending.getValue() && enchantmentLevel(stack, "mending") <= 0
            ? AuctionHelperFeature.FilterResult.reject("нет Mending")
            : AuctionHelperFeature.FilterResult.PASS;
      }
   }

   private AuctionHelperFeature.FilterResult evaluatePotionProfile(class_1799 stack, Optional<DonItems.DonItem> donItem) {
      Optional<DonItems.DonItem> potionItem = donItem.filter(
         itemx -> itemx.category() == DonItems.Category.POTION || itemx.category() == DonItems.Category.ARROW
      );
      if (potionItem.isEmpty()) {
         return this.knownPotionsOnly.getValue()
            ? AuctionHelperFeature.FilterResult.reject("зелье отсутствует в DonItems")
            : AuctionHelperFeature.FilterResult.PASS;
      } else {
         DonItems.DonItem item = potionItem.get();
         if (!DonItems.hasPotionProfile(item)) {
            return AuctionHelperFeature.FilterResult.PASS;
         } else {
            Optional<Boolean> effectIds = DonItems.matchesPotionProfile(stack, item, false, false);
            if (effectIds.isPresent() && !effectIds.get()) {
               return AuctionHelperFeature.FilterResult.reject("набор эффектов не совпадает с " + item.displayName());
            } else if (this.maxEffectLevel.getValue() && !DonItems.matchesPotionProfile(stack, item, true, false).orElse(true)) {
               return AuctionHelperFeature.FilterResult.reject("уровень эффектов ниже максимального");
            } else {
               return this.fullEffectDuration.getValue() && !DonItems.matchesPotionProfile(stack, item, false, true).orElse(true)
                  ? AuctionHelperFeature.FilterResult.reject("длительность эффектов ниже максимальной")
                  : AuctionHelperFeature.FilterResult.PASS;
            }
         }
      }
   }

   private static boolean isArmor(class_1799 stack) {
      return stack.method_31573(class_3489.field_48297)
         || stack.method_31573(class_3489.field_48296)
         || stack.method_31573(class_3489.field_48295)
         || stack.method_31573(class_3489.field_48294);
   }

   private static boolean isPotionOrArrow(class_1799 stack) {
      return stack.method_58694(class_9334.field_49651) != null;
   }

   private static int enchantmentLevel(class_1799 stack, String path) {
      for (Entry<class_6880<class_1887>> entry : stack.method_58657().method_57539()) {
         Optional<class_5321<class_1887>> key = ((class_6880)entry.getKey()).method_40230();
         if (key.isPresent() && key.get().method_29177().method_12832().equals(path)) {
            return entry.getIntValue();
         }
      }

      return 0;
   }

   private static void insertDonItemInfo(List<class_2561> tooltip, DonItems.DonItem item) {
      String serverName = item.server() == DonItems.Server.FUNTIME ? "FunTime" : "HolyWorld";
      String label = serverName + " • " + item.displayName();
      if (!tooltip.stream().anyMatch(line -> line.getString().equals(label))) {
         tooltip.add(Math.min(1, tooltip.size()), class_2561.method_43470(label).method_27692(categoryFormatting(item.category())));
      }
   }

   private static void appendPotionEffects(
      List<class_2561> tooltip, class_1799 stack, AuctionHelperFeature.AuctionServer server, Optional<DonItems.DonItem> donItem, boolean showDuration
   ) {
      if (hasRealPotionEffects(stack, server, donItem)) {
         class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
         if (contents != null) {
            tooltip.add(class_2561.method_43470("Эффекты:").method_27692(class_124.field_1063));

            for (class_1293 effect : contents.method_57397()) {
               String effectName = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
               String duration = !showDuration ? "" : (effect.method_48559() ? "∞" : formatDuration(effect.method_5584()));
               String suffix = duration.isEmpty() ? "" : " • " + duration;
               tooltip.add(
                  class_2561.method_43470(" • " + effectName + " " + roman(effect.method_5578() + 1) + suffix)
                     .method_27692(((class_1291)effect.method_5579().comp_349()).method_18792().method_18793())
               );
            }
         }
      }
   }

   private static boolean hasRealPotionEffects(class_1799 stack, AuctionHelperFeature.AuctionServer server, Optional<DonItems.DonItem> donItem) {
      class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
      if (contents == null) {
         return false;
      } else {
         List<class_1293> effects = new ArrayList<>();
         contents.method_57397().forEach(effects::add);
         if (effects.isEmpty()) {
            return false;
         } else if (server == AuctionHelperFeature.AuctionServer.HOLYWORLD
            && donItem.filter(item -> item.category() == DonItems.Category.POTION).isPresent()
            && effects.size() == 1) {
            class_1293 effect = (class_1293)effects.getFirst();
            return !effect.method_5586().endsWith("instant_health") || effect.method_5584() > 1;
         } else {
            return true;
         }
      }
   }

   private static String formatDuration(int ticks) {
      if (ticks <= 0) {
         return "";
      }

      long seconds = Math.max(1L, (ticks + 19L) / 20L);
      long hours = seconds / 3600L;
      long minutes = seconds % 3600L / 60L;
      long remainder = seconds % 60L;
      return hours > 0L ? String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, remainder) : String.format(Locale.ROOT, "%d:%02d", minutes, remainder);
   }

   private static int categoryOverlay(DonItems.Category category) {
      return switch (category) {
         case WEAPON -> ColorUtil.rgba(255, 74, 84, 62);
         case ARMOR -> ColorUtil.rgba(70, 145, 255, 62);
         case COMBAT_ITEM, EXPLOSIVE -> ColorUtil.rgba(255, 130, 45, 68);
         case POTION -> ColorUtil.rgba(190, 80, 255, 72);
         case ARROW -> ColorUtil.rgba(55, 210, 255, 68);
         case TALISMAN -> ColorUtil.rgba(255, 205, 55, 72);
         case SPHERE -> ColorUtil.rgba(215, 100, 255, 72);
         case CUSTOM_ENCHANTMENT -> ColorUtil.rgba(170, 105, 255, 62);
         case RUNE -> ColorUtil.rgba(85, 255, 145, 62);
         case BACKPACK -> ColorUtil.rgba(50, 190, 205, 58);
         case TOOL -> ColorUtil.rgba(90, 180, 255, 58);
         case MISC -> ColorUtil.rgba(210, 210, 220, 42);
      };
   }

   private static class_124 categoryFormatting(DonItems.Category category) {
      return switch (category) {
         case WEAPON, COMBAT_ITEM, EXPLOSIVE -> class_124.field_1061;
         case ARMOR, TOOL -> class_124.field_1078;
         case POTION, SPHERE, CUSTOM_ENCHANTMENT -> class_124.field_1076;
         case ARROW, BACKPACK -> class_124.field_1075;
         case TALISMAN -> class_124.field_1065;
         case RUNE -> class_124.field_1060;
         case MISC -> class_124.field_1080;
      };
   }

   private static boolean isOutdated(class_1799 stack) {
      for (class_2561 line : lore(stack)) {
         String raw = line.getString();
         String normalized = DonItems.normalizeName(raw);
         if (EXPIRED.matcher(raw).find()
            || normalized.contains("снят с продажи")
            || normalized.contains("уже куплен")
            || normalized.contains("товар недоступен")
            || normalized.contains("listing unavailable")) {
            return true;
         }
      }

      return false;
   }

   private static List<class_2561> lore(class_1799 stack) {
      class_9290 lore = (class_9290)stack.method_58694(class_9334.field_49632);
      return lore == null ? List.of() : lore.comp_2400();
   }

   private static long parseLargestAmount(String line) {
      long largest = -1L;
      Matcher matcher = AMOUNT.matcher(line);

      while (matcher.find()) {
         long parsed = parseAmount(matcher.group(1), matcher.group(2));
         largest = Math.max(largest, parsed);
      }

      return largest;
   }

   private static long parseAmount(String token, String suffix) {
      long multiplier = switch (suffix == null ? "" : suffix.toLowerCase(Locale.ROOT)) {
         case "k", "к" -> 1000L;
         case "m", "м" -> 1000000L;
         case "b", "б" -> 1000000000L;
         default -> 1L;
      };

      try {
         if (multiplier > 1L) {
            String compact = token.replaceAll("[\\s\\u00A0_'’]", "");
            int separator = Math.max(compact.lastIndexOf(46), compact.lastIndexOf(44));
            if (separator >= 0 && compact.length() - separator - 1 <= 2) {
               double value = Double.parseDouble(compact.replace(',', '.'));
               return Math.round(value * multiplier);
            }
         }

         String digits = token.replaceAll("\\D", "");
         if (digits.isEmpty()) {
            return -1L;
         }

         long value = Long.parseLong(digits);
         return multiplier != 1L && value > Long.MAX_VALUE / multiplier ? Long.MAX_VALUE : value * multiplier;
      } catch (NumberFormatException ignored) {
         return -1L;
      }
   }

   private static String roman(int level) {
      return switch (level) {
         case 1 -> "I";
         case 2 -> "II";
         case 3 -> "III";
         case 4 -> "IV";
         case 5 -> "V";
         case 6 -> "VI";
         case 7 -> "VII";
         case 8 -> "VIII";
         case 9 -> "IX";
         case 10 -> "X";
         default -> String.valueOf(level);
      };
   }

   @Environment(EnvType.CLIENT)
   private enum AuctionServer {
      FUNTIME(DonItems.Server.FUNTIME),
      HOLYWORLD(DonItems.Server.HOLYWORLD);

      private final DonItems.Server catalogue;

      AuctionServer(DonItems.Server catalogue) {
         this.catalogue = catalogue;
      }

      private boolean matchesTitle(String title) {
         if (title != null && !title.isBlank()) {
            String normalized = DonItems.normalizeName(title);
            if (!normalized.contains("донат магазин")
               && !normalized.contains("все для pvp")
               && !normalized.startsWith("помощь")
               && !normalized.contains("премиум магазин")) {
               boolean explicitAuction = normalized.contains("аукцион")
                  || normalized.contains("auction")
                  || normalized.contains("поиск")
                  || normalized.contains("search");
               return this == FUNTIME
                  ? explicitAuction || title.contains("漢:") || title.contains(":") && AuctionHelperFeature.PAGE_FRACTION.matcher(title).find()
                  : explicitAuction || normalized.contains("торговая площадка");
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private long extractPrice(class_1799 stack) {
         long price = -1L;

         for (class_2561 line : AuctionHelperFeature.lore(stack)) {
            String raw = line.getString();
            if (this.isPriceLine(raw)) {
               price = Math.max(price, AuctionHelperFeature.parseLargestAmount(raw));
            }
         }

         return price;
      }

      private boolean hasListingMarker(class_1799 stack) {
         for (class_2561 line : AuctionHelperFeature.lore(stack)) {
            String normalized = DonItems.normalizeName(line.getString());
            if (normalized.contains("нажмите чтобы купить")
               || normalized.contains("продавец")
               || normalized.contains("seller")
               || normalized.contains("click to buy")
               || normalized.contains("купить сейчас")) {
               return true;
            }
         }

         return false;
      }

      private boolean isPriceLine(String line) {
         String normalized = DonItems.normalizeName(line);
         return normalized.contains("цен")
            || normalized.contains("стоимост")
            || normalized.contains("price")
            || normalized.contains("cost")
            || normalized.contains("за штуку")
            || line.contains("$")
            || line.contains("⛃");
      }

      private void insertAfterPrice(List<class_2561> tooltip, class_2561 addition) {
         for (int index = 0; index < tooltip.size(); index++) {
            if (this.isPriceLine(tooltip.get(index).getString())) {
               tooltip.add(index + 1, addition);
               return;
            }
         }

         tooltip.add(addition);
      }

      private String unitPriceLabel(String formattedPrice) {
         return this == FUNTIME ? "$ За штуку: $" + formattedPrice : "Цена за штуку: " + formattedPrice;
      }
   }

   @Environment(EnvType.CLIENT)
   private record FilterResult(boolean accepted, String reason) {
      private static final AuctionHelperFeature.FilterResult PASS = new AuctionHelperFeature.FilterResult(true, "");

      private static AuctionHelperFeature.FilterResult reject(String reason) {
         return new AuctionHelperFeature.FilterResult(false, reason);
      }
   }
}
