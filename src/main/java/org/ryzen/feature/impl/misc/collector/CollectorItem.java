package org.ryzen.feature.impl.misc.collector;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1812;
import org.ryzen.feature.impl.misc.DonItems;
import org.ryzen.feature.impl.misc.autobuy.AuctionUtils;

@Environment(EnvType.CLIENT)
public final class CollectorItem {
   private final String id;
   private final String name;
   private final class_1792 item;
   private final boolean strictName;
   private final DonItems.DonItem profile;
   private final Predicate<class_1799> signature;
   private final List<CollectorCondition> conditions;
   private boolean enabled;
   private boolean scanCheapest;
   private int count;

   public CollectorItem(String name, class_1792 item, int count, boolean enabled, boolean scanCheapest, boolean strictName) {
      this(name, item, count, enabled, scanCheapest, strictName, null, null, List.of());
   }

   public CollectorItem(
      String name,
      class_1792 item,
      int count,
      boolean enabled,
      boolean scanCheapest,
      boolean strictName,
      DonItems.DonItem profile,
      Predicate<class_1799> signature,
      List<CollectorCondition> conditions
   ) {
      this.name = name;
      this.id = name.toLowerCase(Locale.ROOT).replaceAll("[^a-zа-я0-9]+", "-").replaceAll("(^-|-$)", "");
      this.item = item;
      this.count = Math.max(1, Math.min(this.maxTargetCount(), count));
      this.enabled = enabled;
      this.scanCheapest = scanCheapest;
      this.strictName = strictName;
      this.profile = profile;
      this.signature = signature;
      this.conditions = conditions == null ? List.of() : List.copyOf(conditions);
   }

   public class_1799 icon() {
      return new class_1799(this.item, Math.min(this.count, this.item.method_7882()));
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public void setScanCheapest(boolean scanCheapest) {
      this.scanCheapest = scanCheapest;
   }

   public void setCount(int count) {
      this.count = Math.max(1, Math.min(this.maxTargetCount(), count));
   }

   public boolean matches(class_1799 stack) {
      if (stack != null && !stack.method_7960() && stack.method_31574(this.item)) {
         if (this.profile != null && !this.profile.matches(stack)) {
            return false;
         }

         if (this.signature != null && !this.signature.test(stack)) {
            return false;
         }

         for (CollectorCondition condition : this.conditions) {
            if (!condition.matches(stack)) {
               return false;
            }
         }

         if (this.strictName && this.profile == null && this.signature == null && this.conditions.isEmpty()) {
            String lot = AuctionUtils.cleanName(stack.method_7964().getString());
            String expected = AuctionUtils.cleanName(this.name);
            if (!lot.equals(expected) && !lot.contains(expected)) {
               String[] words = expected.split(" ");
               int matched = 0;

               for (String word : words) {
                  if (word.length() >= 4 && lot.contains(word)) {
                     matched++;
                  }
               }

               return matched >= Math.min(2, words.length);
            } else {
               return true;
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public int maxTargetCount() {
      int vanilla = this.item.method_7882();
      if (vanilla > 1) {
         return vanilla;
      } else if (this.item == class_1802.field_8288 || this.item == class_1802.field_8436) {
         return 6;
      } else {
         return this.item instanceof class_1812 ? 16 : 1;
      }
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
   public class_1792 getItem() {
      return this.item;
   }

   @Generated
   public boolean isStrictName() {
      return this.strictName;
   }

   @Generated
   public DonItems.DonItem getProfile() {
      return this.profile;
   }

   @Generated
   public Predicate<class_1799> getSignature() {
      return this.signature;
   }

   @Generated
   public List<CollectorCondition> getConditions() {
      return this.conditions;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public boolean isScanCheapest() {
      return this.scanCheapest;
   }

   @Generated
   public int getCount() {
      return this.count;
   }
}
