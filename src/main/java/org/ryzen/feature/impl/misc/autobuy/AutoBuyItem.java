package org.ryzen.feature.impl.misc.autobuy;

import java.util.Locale;
import java.util.function.Supplier;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

@Environment(EnvType.CLIENT)
public final class AutoBuyItem {
   private final String id;
   private final String name;
   private final AutoBuyItemCategory category;
   private final Supplier<class_1799> iconFactory;
   private final String[] matchAliases;
   private boolean enabled;
   private int buyPrice;
   private int minQty;
   private class_1799 cachedIcon;

   public AutoBuyItem(String name, AutoBuyItemCategory category, int defaultPrice, class_1792 iconItem, String... aliases) {
      this(name, category, defaultPrice, () -> new class_1799(iconItem != null ? iconItem : class_1802.field_8077), aliases);
   }

   public AutoBuyItem(String name, AutoBuyItemCategory category, int defaultPrice, Supplier<class_1799> iconFactory, String... aliases) {
      this.name = name == null ? "" : name;
      this.id = normalizeKey(this.name);
      this.category = category == null ? AutoBuyItemCategory.MISC : category;
      this.iconFactory = iconFactory != null ? iconFactory : () -> new class_1799(class_1802.field_8077);
      this.matchAliases = aliases == null ? new String[0] : aliases;
      this.enabled = false;
      this.buyPrice = Math.max(1, defaultPrice);
      this.minQty = 1;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public void setBuyPrice(int buyPrice) {
      this.buyPrice = Math.max(1, buyPrice);
   }

   public void setMinQty(int minQty) {
      this.minQty = Math.max(1, minQty);
   }

   public class_1799 icon() {
      class_1799 cached = this.cachedIcon;
      if (cached == null || cached.method_7960()) {
         cached = this.createIcon();
         this.cachedIcon = cached;
      }

      return cached;
   }

   public class_1799 createIcon() {
      try {
         class_1799 stack = this.iconFactory.get();
         return stack != null && !stack.method_7960() ? stack.method_7972() : new class_1799(class_1802.field_8077);
      } catch (Throwable ignored) {
         return new class_1799(class_1802.field_8077);
      }
   }

   public boolean matchesName(String rawName) {
      return this.matchScore(rawName) > 0;
   }

   public int matchScore(String rawName) {
      String cleaned = AuctionUtils.cleanName(rawName);
      if (cleaned.isEmpty()) {
         return 0;
      }

      int best = scoreAgainst(cleaned, AuctionUtils.cleanName(this.name));

      for (String alias : this.matchAliases) {
         best = Math.max(best, scoreAgainst(cleaned, AuctionUtils.cleanName(alias)));
      }

      return best;
   }

   private static int scoreAgainst(String auctionName, String catalogKey) {
      if (catalogKey.isEmpty()) {
         return 0;
      } else if (auctionName.equals(catalogKey)) {
         return 2000 + catalogKey.length();
      } else if (catalogKey.length() < 8) {
         return 0;
      } else if (auctionName.contains(catalogKey)) {
         return 1000 + catalogKey.length();
      } else {
         return catalogKey.contains(auctionName) && auctionName.length() >= 10 ? 500 + auctionName.length() : 0;
      }
   }

   public static String normalizeKey(String name) {
      return name == null ? "" : name.replaceAll("\\u00a7.", "").replaceAll("[★\\[\\]⚒❄\ud83c\udf79]", "").trim().toLowerCase(Locale.ROOT);
   }

   @Generated
   public String getId() {
      return this.id;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public AutoBuyItemCategory getCategory() {
      return this.category;
   }

   @Generated
   public String[] getMatchAliases() {
      return this.matchAliases;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public int getBuyPrice() {
      return this.buyPrice;
   }

   @Generated
   public int getMinQty() {
      return this.minQty;
   }
}
