package org.ryzen.feature.impl.pve;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10192;
import net.minecraft.class_1304;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_490;
import net.minecraft.class_5134;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_9285;
import net.minecraft.class_9334;
import net.minecraft.class_9285.class_9287;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.utils.inventory.DropAllInventoryController;
import org.ryzen.utils.inventory.InventorySwap;

@Environment(EnvType.CLIENT)
public final class AutoArmorFeature extends PveFeature implements MinecraftContext {
   private static final class_1304[] ARMOR_SLOTS = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
   public final NumberSetting swapDelay = this.register(new NumberSetting("Swap Delay", 100.0, 0.0, 1000.0, 25.0, "ms"));
   private long lastSwapNanos;

   public AutoArmorFeature() {
      super("AutoArmor", "Equips the strongest armor in your inventory", -1, AutomationPriority.FEATURE);
   }

   @Override
   protected void onPveEnable() {
      this.lastSwapNanos = 0L;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (this.canManageInventory(player) && !InventorySwap.isBusy() && this.delayElapsed(System.nanoTime())) {
         List<AutoArmorFeature.Upgrade> upgrades = this.findUpgrades(player);
         if (!upgrades.isEmpty() && this.claim(AutomationResource.INVENTORY)) {
            boolean swapped = false;

            try {
               if (this.canManageInventory(player) && !InventorySwap.isBusy()) {
                  for (AutoArmorFeature.Upgrade upgrade : upgrades) {
                     swapped |= this.equipUpgrade(player, upgrade);
                  }
               }
            } finally {
               PveAutomationCoordinator.INSTANCE.release(this);
            }

            if (swapped) {
               this.lastSwapNanos = System.nanoTime();
            }
         }
      }
   }

   private boolean canManageInventory(class_746 player) {
      return player != null
         && player.method_5805()
         && mc.field_1761 != null
         && player.field_7512 == player.field_7498
         && player.field_7498.method_34255().method_7960()
         && !DropAllInventoryController.blocksInventoryOperations()
         && (mc.field_1755 == null || mc.field_1755 instanceof class_490);
   }

   private boolean delayElapsed(long nowNanos) {
      long delayNanos = (long)(this.swapDelay.getValue() * 1000000.0);
      return this.lastSwapNanos == 0L || nowNanos - this.lastSwapNanos >= delayNanos;
   }

   private List<AutoArmorFeature.Upgrade> findUpgrades(class_746 player) {
      List<AutoArmorFeature.Upgrade> upgrades = new ArrayList<>(ARMOR_SLOTS.length);

      for (class_1304 equipmentSlot : ARMOR_SLOTS) {
         int armorSlot = armorMenuSlot(equipmentSlot);
         class_1799 equipped = player.field_7498.method_7611(armorSlot).method_7677();
         if (!hasBindingCurse(equipped)) {
            double equippedScore = equipped.method_7960() ? Double.NEGATIVE_INFINITY : armorScore(equipped);
            List<AutoArmorFeature.ScoredSlot> candidates = new ArrayList<>();

            for (int slot = 9; slot < 45; slot++) {
               class_1799 candidate = player.field_7498.method_7611(slot).method_7677();
               if (isArmorFor(candidate, equipmentSlot) && !hasBindingCurse(candidate)) {
                  candidates.add(new AutoArmorFeature.ScoredSlot(slot, armorScore(candidate)));
               }
            }

            OptionalInt best = selectBestUpgrade(equippedScore, candidates);
            if (best.isPresent()) {
               upgrades.add(new AutoArmorFeature.Upgrade(best.getAsInt(), armorSlot, equipmentSlot));
            }
         }
      }

      return upgrades;
   }

   private boolean equipUpgrade(class_746 player, AutoArmorFeature.Upgrade upgrade) {
      if (player.field_7498.method_40442(upgrade.sourceSlot())
         && player.field_7498.method_40442(upgrade.armorSlot())
         && player.field_7498.method_34255().method_7960()) {
         class_1799 candidate = player.field_7498.method_7611(upgrade.sourceSlot()).method_7677();
         class_1799 equipped = player.field_7498.method_7611(upgrade.armorSlot()).method_7677();
         if (isArmorFor(candidate, upgrade.equipmentSlot())
            && !hasBindingCurse(candidate)
            && !hasBindingCurse(equipped)
            && (equipped.method_7960() || !(armorScore(candidate) <= armorScore(equipped)))) {
            this.click(player, upgrade.sourceSlot());
            if (player.field_7498.method_34255().method_7960()) {
               return false;
            }

            this.click(player, upgrade.armorSlot());
            if (!player.field_7498.method_34255().method_7960()) {
               this.click(player, upgrade.sourceSlot());
            }

            return player.field_7498.method_34255().method_7960();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void click(class_746 player, int slot) {
      mc.field_1761.method_2906(player.field_7498.field_7763, slot, 0, class_1713.field_7790, player);
   }

   private static int armorMenuSlot(class_1304 equipmentSlot) {
      return switch (equipmentSlot) {
         case field_6169 -> 5;
         case field_6174 -> 6;
         case field_6172 -> 7;
         case field_6166 -> 8;
         default -> throw new IllegalArgumentException("Not a humanoid armor slot: " + equipmentSlot);
      };
   }

   private static boolean isArmorFor(class_1799 stack, class_1304 equipmentSlot) {
      if (stack.method_7960()) {
         return false;
      }

      class_10192 equippable = (class_10192)stack.method_58694(class_9334.field_54196);
      return equippable != null && equippable.comp_3174() == equipmentSlot;
   }

   private static boolean hasBindingCurse(class_1799 stack) {
      return enchantmentLevel(stack, "binding_curse") > 0;
   }

   private static double armorScore(class_1799 stack) {
      double armor = 0.0;
      double toughness = 0.0;
      class_9285 modifiers = (class_9285)stack.method_58694(class_9334.field_49636);
      if (modifiers != null) {
         for (class_9287 entry : modifiers.comp_2393()) {
            if (entry.comp_2395().equals(class_5134.field_23724)) {
               armor += entry.comp_2396().comp_2449();
            } else if (entry.comp_2395().equals(class_5134.field_23725)) {
               toughness += entry.comp_2396().comp_2449();
            }
         }
      }

      return score(armor, toughness, enchantmentLevel(stack, "protection"), enchantmentLevel(stack, "unbreaking"), enchantmentLevel(stack, "mending"));
   }

   private static int enchantmentLevel(class_1799 stack, String path) {
      if (stack.method_7960()) {
         return 0;
      }

      for (Entry<class_6880<class_1887>> entry : stack.method_58657().method_57539()) {
         Optional<class_5321<class_1887>> key = ((class_6880)entry.getKey()).method_40230();
         if (key.isPresent() && key.get().method_29177().method_12832().equals(path)) {
            return entry.getIntValue();
         }
      }

      return 0;
   }

   static double score(double armor, double toughness, int protection, int unbreaking, int mending) {
      return armor + toughness + protection + unbreaking * 0.1 + mending * 0.2;
   }

   static OptionalInt selectBestUpgrade(double equippedScore, List<AutoArmorFeature.ScoredSlot> candidates) {
      int bestSlot = -1;
      double bestScore = equippedScore;

      for (AutoArmorFeature.ScoredSlot candidate : candidates) {
         if (candidate.score() > bestScore) {
            bestScore = candidate.score();
            bestSlot = candidate.slot();
         }
      }

      return bestSlot < 0 ? OptionalInt.empty() : OptionalInt.of(bestSlot);
   }

   @Environment(EnvType.CLIENT)
   record ScoredSlot(int slot, double score) {
   }

   @Environment(EnvType.CLIENT)
   private record Upgrade(int sourceSlot, int armorSlot, class_1304 equipmentSlot) {
   }
}
