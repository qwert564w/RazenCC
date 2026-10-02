package org.ryzen.feature.impl.pve;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.utils.inventory.DropAllInventoryController;
import org.ryzen.utils.inventory.InventorySwap;

@Environment(EnvType.CLIENT)
public final class AutoGappleFeature extends PveFeature {
   private static final int RETRY_GUARD_TICKS = 10;
   public final NumberSetting health = this.register(new NumberSetting("Health", 15.0, 4.0, 20.0, 0.05, " HP"));
   public final BooleanSetting goldenApples = this.register(new BooleanSetting("Golden Apples", true));
   public final BooleanSetting enchantedGoldenApples = this.register(new BooleanSetting("Enchanted Golden Apples", true));
   private final ConsumableUseController useController = new ConsumableUseController();
   private int retryAfterTick;

   public AutoGappleFeature() {
      super("AutoGapple", "Eats the strongest allowed golden apple at low health", -1, AutomationPriority.EMERGENCY);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (this.useController.isActive()) {
         if (!isWorldUsable(client, player)) {
            this.cancelActive(client, player);
         } else if (!this.useController.tick(client, player)) {
            this.finishTransaction();
            this.retryAfterTick = player.field_6012 + 10;
         }
      } else if (isWorldUsable(client, player)
         && player.field_6012 >= this.retryAfterTick
         && client.field_1755 == null
         && player.field_7512 == player.field_7498
         && player.field_7498.method_34255().method_7960()
         && !player.method_6115()
         && !client.field_1690.field_1904.method_1434()
         && !DropAllInventoryController.blocksInventoryOperations()
         && !InventorySwap.isBusy()
         && !(effectiveHealth(player) > this.health.getValue())) {
         List<ConsumableSelector.Candidate<class_1799>> candidates = ConsumableInventory.collect(player, AutoGappleFeature::appleProfile)
            .stream()
            .filter(candidate -> !player.method_7357().method_7904(candidate.value()))
            .toList();
         Optional<ConsumableSelector.Candidate<class_1799>> selected = ConsumableSelector.selectApple(
            candidates, this.goldenApples.getValue(), this.enchantedGoldenApples.getValue()
         );
         selected.ifPresent(candidate -> this.startUse(client, player, (ConsumableSelector.Candidate<class_1799>)candidate));
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.cancelActive(class_310.method_1551(), class_310.method_1551().field_1724);
      this.retryAfterTick = 0;
   }

   @Override
   protected void onPveDisable() {
      this.cancelActive(class_310.method_1551(), class_310.method_1551().field_1724);
      this.retryAfterTick = 0;
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.cancelActive(class_310.method_1551(), class_310.method_1551().field_1724);
   }

   private void startUse(class_310 client, class_746 player, ConsumableSelector.Candidate<class_1799> selected) {
      boolean claimed = selected.location() == ConsumableSelector.Location.OFF_HAND
         ? this.claim(AutomationResource.INVENTORY)
         : this.claim(AutomationResource.INVENTORY, AutomationResource.MOVEMENT, AutomationResource.SCREEN);
      if (claimed) {
         if (!this.useController.start(client, player, selected, true, 0)) {
            this.finishTransaction();
         }
      }
   }

   private void cancelActive(class_310 client, class_746 player) {
      this.useController.cancel(client, player);
      this.finishTransaction();
   }

   private void finishTransaction() {
      PveAutomationCoordinator.INSTANCE.release(this);
   }

   private static ConsumableInventory.Profile appleProfile(class_1799 stack) {
      if (stack.method_31574(class_1802.field_8367)) {
         return new ConsumableInventory.Profile(ConsumableSelector.Kind.ENCHANTED_GOLDEN_APPLE, 0, 0.0F, true);
      } else {
         return stack.method_31574(class_1802.field_8463) ? new ConsumableInventory.Profile(ConsumableSelector.Kind.GOLDEN_APPLE, 0, 0.0F, true) : null;
      }
   }

   private static double effectiveHealth(class_746 player) {
      return player.method_6032() + player.method_6067();
   }

   private static boolean isWorldUsable(class_310 client, class_746 player) {
      return player != null && client.field_1687 != null && client.field_1761 != null && player.method_5805() && !player.method_7325();
   }
}
