package org.ryzen.feature.impl.pve;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10124;
import net.minecraft.class_10132;
import net.minecraft.class_10134;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_310;
import net.minecraft.class_4174;
import net.minecraft.class_746;
import net.minecraft.class_9334;
import org.ryzen.context.RotationContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.feature.impl.misc.DonItems;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.utils.inventory.DropAllInventoryController;
import org.ryzen.utils.inventory.InventorySwap;

@Environment(EnvType.CLIENT)
public final class AutoUseFeature extends PveFeature {
   public static final String AUTO_EAT = "Auto Eat";
   public static final String AUTO_INVISIBILITY = "Auto Invisibility";
   private static final long INVISIBILITY_RETRY_NANOS = 4000000000L;
   public final MultiSelectSetting features = this.register(new MultiSelectSetting("Features", Set.of("Auto Eat"), "Auto Eat", "Auto Invisibility"));
   public final BooleanSetting ignoreGoldenApples = this.register(
      new BooleanSetting("Ignore Golden Apples", false).visibleWhen(() -> this.features.isSelected("Auto Eat"))
   );
   public final BooleanSetting ignoreEnchantedGoldenApples = this.register(
      new BooleanSetting("Ignore Enchanted Golden Apples", false).visibleWhen(() -> this.features.isSelected("Auto Eat"))
   );
   private final ConsumableUseController useController = new ConsumableUseController();
   private AutoUseFeature.Action activeAction;
   private boolean rotationApplied;
   private long invisibilityRetryAt;

   public AutoUseFeature() {
      super("AutoUse", "Automatically eats food and maintains invisibility", -1, AutomationPriority.BACKGROUND);
   }

   public boolean isEating() {
      return this.activeAction == AutoUseFeature.Action.EATING && this.useController.isActive();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (this.useController.isActive()) {
         if (isWorldUsable(client, player) && !this.combatActive()) {
            this.keepThrowableRotation(player);
            if (!this.useController.tick(client, player)) {
               if (this.activeAction == AutoUseFeature.Action.INVISIBILITY) {
                  this.invisibilityRetryAt = System.nanoTime() + 4000000000L;
               }

               this.finishTransaction();
            }
         } else {
            this.cancelActive(client, player);
         }
      } else if (isWorldUsable(client, player)
         && !this.combatActive()
         && client.field_1755 == null
         && player.field_7512 == player.field_7498
         && player.field_7498.method_34255().method_7960()
         && !player.method_6115()
         && !client.field_1690.field_1904.method_1434()
         && !DropAllInventoryController.blocksInventoryOperations()
         && !InventorySwap.isBusy()) {
         if (!this.features.isSelected("Auto Eat") || !player.method_7344().method_7587() || !this.tryAutoEat(client, player)) {
            if (this.features.isSelected("Auto Invisibility")) {
               this.tryAutoInvisibility(client, player);
            }
         }
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.cancelActive(class_310.method_1551(), class_310.method_1551().field_1724);
      this.invisibilityRetryAt = 0L;
   }

   @Override
   protected void onPveDisable() {
      this.cancelActive(class_310.method_1551(), class_310.method_1551().field_1724);
      this.invisibilityRetryAt = 0L;
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.cancelActive(class_310.method_1551(), class_310.method_1551().field_1724);
   }

   private boolean tryAutoEat(class_310 client, class_746 player) {
      List<ConsumableSelector.Candidate<class_1799>> candidates = ConsumableInventory.collect(player, AutoUseFeature::foodProfile)
         .stream()
         .filter(candidate -> !player.method_7357().method_7904(candidate.value()))
         .toList();
      Optional<ConsumableSelector.Candidate<class_1799>> selected = ConsumableSelector.selectFood(
         candidates, this.ignoreGoldenApples.getValue(), this.ignoreEnchantedGoldenApples.getValue()
      );
      return selected.isPresent() && this.startUse(client, player, selected.get(), AutoUseFeature.Action.EATING);
   }

   private boolean tryAutoInvisibility(class_310 client, class_746 player) {
      long now = System.nanoTime();
      if (now >= this.invisibilityRetryAt
         && player.method_24828()
         && player.field_6012 > 100
         && !player.method_6059(class_1294.field_5905)
         && !player.method_6059(class_1294.field_5912)) {
         Optional<ConsumableSelector.Candidate<class_1799>> selected = ConsumableInventory.collect(player, AutoUseFeature::invisibilityProfile)
            .stream()
            .filter(candidate -> !player.method_7357().method_7904(candidate.value()))
            .min(
               Comparator.<ConsumableSelector.Candidate<class_1799>>comparingInt(candidate -> locationRank(candidate.location()))
                  .thenComparingInt(ConsumableSelector.Candidate::containerSlot)
            );
         if (selected.isEmpty()) {
            return false;
         }

         boolean started = this.startUse(client, player, selected.get(), AutoUseFeature.Action.INVISIBILITY);
         if (started) {
            this.invisibilityRetryAt = now + 4000000000L;
         }

         return started;
      } else {
         return false;
      }
   }

   private boolean startUse(class_310 client, class_746 player, ConsumableSelector.Candidate<class_1799> selected, AutoUseFeature.Action action) {
      boolean throwable = action == AutoUseFeature.Action.INVISIBILITY && isThrowablePotion(selected.value());
      boolean rotateThrowable = throwable && PveManagerFeature.INSTANCE.rotate.getValue();
      if (rotateThrowable && RotationContext.isActive()) {
         return false;
      }

      boolean swappedUse = selected.location() != ConsumableSelector.Location.OFF_HAND;
      boolean claimed;
      if (rotateThrowable && swappedUse) {
         claimed = this.claim(AutomationResource.INVENTORY, AutomationResource.ROTATION, AutomationResource.MOVEMENT, AutomationResource.SCREEN);
      } else if (rotateThrowable) {
         claimed = this.claim(AutomationResource.INVENTORY, AutomationResource.ROTATION);
      } else if (swappedUse) {
         claimed = this.claim(AutomationResource.INVENTORY, AutomationResource.MOVEMENT, AutomationResource.SCREEN);
      } else {
         claimed = this.claim(AutomationResource.INVENTORY);
      }

      if (!claimed) {
         return false;
      }

      if (rotateThrowable) {
         RotationContext.setRotation(player.method_36454(), 90.0F);
         this.rotationApplied = true;
      }

      boolean heldUse = action == AutoUseFeature.Action.EATING || selected.value().method_31574(class_1802.field_8574);
      int directDelay = throwable && selected.location() == ConsumableSelector.Location.OFF_HAND ? 1 : 0;
      if (!this.useController.start(client, player, selected, heldUse, directDelay)) {
         this.finishTransaction();
         return false;
      } else {
         this.activeAction = action;
         return true;
      }
   }

   private void keepThrowableRotation(class_746 player) {
      if (this.rotationApplied && player != null) {
         RotationContext.setRotation(player.method_36454(), 90.0F);
      }
   }

   private void cancelActive(class_310 client, class_746 player) {
      this.useController.cancel(client, player);
      this.finishTransaction();
   }

   private void finishTransaction() {
      if (this.rotationApplied) {
         if (this.owns(AutomationResource.ROTATION) || !PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.ROTATION)) {
            RotationContext.clear();
         }

         this.rotationApplied = false;
      }

      this.activeAction = null;
      PveAutomationCoordinator.INSTANCE.release(this);
   }

   private boolean combatActive() {
      AuraFeature aura = AuraFeature.getMarkerFeature();
      return aura != null && aura.getCurrentTarget() != null
         ? true
         : PveAutomationCoordinator.INSTANCE.isClaimedByOther(this, AutomationResource.COMBAT)
            || PveAutomationCoordinator.INSTANCE.isClaimedByOther(this, AutomationResource.ROTATION);
   }

   private static boolean isWorldUsable(class_310 client, class_746 player) {
      return player != null && client.field_1687 != null && client.field_1761 != null && player.method_5805() && !player.method_7325();
   }

   private static ConsumableInventory.Profile foodProfile(class_1799 stack) {
      class_4174 food = (class_4174)stack.method_58694(class_9334.field_50075);
      if (food != null && !stack.method_31574(class_1802.field_8551)) {
         ConsumableSelector.Kind kind;
         if (stack.method_31574(class_1802.field_8367)) {
            kind = ConsumableSelector.Kind.ENCHANTED_GOLDEN_APPLE;
         } else if (stack.method_31574(class_1802.field_8463)) {
            kind = ConsumableSelector.Kind.GOLDEN_APPLE;
         } else {
            kind = ConsumableSelector.Kind.FOOD;
         }

         return new ConsumableInventory.Profile(kind, food.comp_2491(), food.comp_2492(), hasNoHarmfulFoodEffect(stack));
      } else {
         return null;
      }
   }

   private static ConsumableInventory.Profile invisibilityProfile(class_1799 stack) {
      if (!stack.method_31574(class_1802.field_8574) && !stack.method_31574(class_1802.field_8436) && !stack.method_31574(class_1802.field_8150)) {
         return null;
      }

      boolean cataloguedInvisibility = DonItems.FunTime.ENHANCED_INVISIBILITY_POTION.matches(stack);
      class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
      int effectCount = 0;
      boolean invisibility = false;
      if (contents != null) {
         for (class_1293 effect : contents.method_57397()) {
            effectCount++;
            invisibility |= effect.method_55654(class_1294.field_5905);
         }
      }

      return cataloguedInvisibility || invisibility && effectCount == 1
         ? new ConsumableInventory.Profile(ConsumableSelector.Kind.INVISIBILITY_POTION, 0, 0.0F, true)
         : null;
   }

   private static boolean hasNoHarmfulFoodEffect(class_1799 stack) {
      class_10124 consumable = (class_10124)stack.method_58694(class_9334.field_53964);
      if (consumable == null) {
         return true;
      }

      for (class_10134 effect : consumable.comp_3089()) {
         if (effect instanceof class_10132 statusEffects) {
            for (class_1293 statusEffect : statusEffects.comp_3094()) {
               if (!((class_1291)statusEffect.method_5579().comp_349()).method_5573()) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private static boolean isThrowablePotion(class_1799 stack) {
      return stack.method_31574(class_1802.field_8436) || stack.method_31574(class_1802.field_8150);
   }

   private static int locationRank(ConsumableSelector.Location location) {
      return switch (location) {
         case OFF_HAND -> 0;
         case MAIN_HAND -> 1;
         case HOTBAR -> 2;
         case INVENTORY -> 3;
      };
   }

   @Environment(EnvType.CLIENT)
   private enum Action {
      EATING,
      INVISIBILITY;
   }
}
