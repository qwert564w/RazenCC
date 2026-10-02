package org.ryzen.feature.impl.misc.collector;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_2561;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_9290;
import net.minecraft.class_9334;
import org.ryzen.feature.impl.misc.autobuy.AuctionUtils;

@Environment(EnvType.CLIENT)
public final class CollectorCondition {
   private static final String[] ROMAN = new String[]{"", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x"};
   private final String id;
   private final CollectorCondition.Kind kind;
   private final String key;
   private final String label;
   private final boolean adjustable;
   private boolean enabled;
   private int level;

   private CollectorCondition(CollectorCondition.Kind kind, String key, String label, int level, boolean enabled, boolean adjustable) {
      this.kind = kind;
      this.key = key == null ? "" : key.toLowerCase(Locale.ROOT);
      this.label = label == null ? this.key : label;
      this.id = kind.name().toLowerCase(Locale.ROOT) + "-" + this.key.replaceAll("[^a-zа-яё0-9]+", "-").replaceAll("(^-|-$)", "");
      this.level = Math.max(1, Math.min(10, level));
      this.enabled = enabled;
      this.adjustable = adjustable;
   }

   public static CollectorCondition enchant(String path, String label, int level) {
      return new CollectorCondition(CollectorCondition.Kind.ENCHANTMENT, path, label, level, true, true);
   }

   public static CollectorCondition lore(String marker, int level) {
      return new CollectorCondition(CollectorCondition.Kind.LORE, marker, marker, level, true, level > 0);
   }

   public static CollectorCondition lore(String marker, int level, boolean enabled) {
      return new CollectorCondition(CollectorCondition.Kind.LORE, marker, marker, Math.max(1, level), enabled, level > 0);
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public void setLevel(int level) {
      this.level = Math.max(1, Math.min(10, level));
   }

   public boolean matches(class_1799 stack) {
      if (!this.enabled) {
         return true;
      } else {
         return this.kind == CollectorCondition.Kind.ENCHANTMENT ? this.matchesEnchantment(stack) : this.matchesLore(stack);
      }
   }

   private boolean matchesEnchantment(class_1799 stack) {
      for (Entry<class_6880<class_1887>> entry : stack.method_58657().method_57539()) {
         Optional<class_5321<class_1887>> resourceKey = ((class_6880)entry.getKey()).method_40230();
         if (resourceKey.isPresent() && resourceKey.get().method_29177().method_12832().equals(this.key) && entry.getIntValue() >= this.level) {
            return true;
         }
      }

      return false;
   }

   private boolean matchesLore(class_1799 stack) {
      class_9290 lore = (class_9290)stack.method_58694(class_9334.field_49632);
      List<class_2561> lines = lore == null ? List.of() : lore.comp_2400();
      String expected = AuctionUtils.cleanName(this.key);

      for (class_2561 component : lines) {
         String line = AuctionUtils.cleanName(component.getString());
         if (line.contains(expected) && (!this.adjustable || containsLevel(line, this.level))) {
            return true;
         }
      }

      return false;
   }

   private static boolean containsLevel(String line, int level) {
      if (level <= 1) {
         return true;
      }

      String decimal = Integer.toString(level);
      String roman = level < ROMAN.length ? ROMAN[level] : "";
      return line.matches(".*(?:^|\\s)" + decimal + "(?:\\s|$).*") || !roman.isEmpty() && line.matches(".*(?:^|\\s)" + roman + "(?:\\s|$).*");
   }

   @Generated
   public String getId() {
      return this.id;
   }

   @Generated
   public CollectorCondition.Kind getKind() {
      return this.kind;
   }

   @Generated
   public String getKey() {
      return this.key;
   }

   @Generated
   public String getLabel() {
      return this.label;
   }

   @Generated
   public boolean isAdjustable() {
      return this.adjustable;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public int getLevel() {
      return this.level;
   }

   @Environment(EnvType.CLIENT)
   public enum Kind {
      ENCHANTMENT,
      LORE;
   }
}
