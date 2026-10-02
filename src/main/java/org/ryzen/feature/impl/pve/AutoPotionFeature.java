package org.ryzen.feature.impl.pve;

import java.util.Arrays;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_638;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_9334;
import org.ryzen.context.MinecraftContext;
import org.ryzen.context.RotationContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.input.PlayerInputEvent;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.PvpStateTracker;
import org.ryzen.utils.inventory.DropAllInventoryController;
import org.ryzen.utils.inventory.InventorySwap;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class AutoPotionFeature extends PveFeature implements MinecraftContext {
   private static final String FIRE_RESISTANCE = "Fire Resistance";
   private static final String STRENGTH = "Strength";
   private static final String SPEED = "Speed";
   private static final long THROW_THROTTLE_NANOS = 600000000L;
   private static final int MIN_WORLD_AGE_TICKS = 100;
   private static final int INVENTORY_SWAP_RETURN_TICKS = 4;
   private static final double GROUND_PROBE_DEPTH = 0.5;
   public final MultiSelectSetting potions = this.register(new MultiSelectSetting("Potions", Set.of(), "Fire Resistance", "Strength", "Speed"));
   public final BooleanSetting onlyPvp = this.register(new BooleanSetting("Only PvP", false));
   private boolean throwing;
   private int operationTicks;
   private int potionContainerSlot = -1;
   private int originalHotbarSlot = -1;
   private class_1799 originalHotbarStack = class_1799.field_8037;
   private float throwYaw;
   private boolean rotationApplied;
   private long lastThrowNanos;

   public AutoPotionFeature() {
      super("AutoPotion", "Throws selected splash potions when their effects are missing", -1, AutomationPriority.FEATURE);
   }

   @Override
   protected void onPveEnable() {
      this.resetOperationState();
      this.lastThrowNanos = 0L;
   }

   @Override
   protected void onPveDisable() {
      this.cancelOperation();
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.cancelOperation();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (this.throwing) {
         this.tickOperation(player);
      } else {
         class_638 level = event.getClient().field_1687;
         long nowNanos = System.nanoTime();
         if (this.canStart(player, level) && !InventorySwap.isBusy() && this.throttleElapsed(nowNanos) && (!this.onlyPvp.getValue() || hasActivePlayerTarget())
            )
          {
            AutoPotionFeature.PotionChoice choice = this.findPotion(player);
            if (choice != null && this.claimOperationResources()) {
               this.beginThrow(player, choice, nowNanos);
            }
         }
      }
   }

   @EventTarget
   public void onPlayerInput(PlayerInputEvent event) {
      if (this.throwing) {
         event.clearMovement(false, true);
      }
   }

   private boolean canStart(class_746 player, class_638 level) {
      return player != null
         && level != null
         && player.method_5805()
         && player.field_6012 > 100
         && !player.method_6115()
         && player.field_7512 == player.field_7498
         && player.field_7498.method_34255().method_7960()
         && mc.field_1761 != null
         && !DropAllInventoryController.blocksInventoryOperations()
         && isOnOrNearGround(player, level);
   }

   private boolean throttleElapsed(long nowNanos) {
      return this.lastThrowNanos == 0L || nowNanos - this.lastThrowNanos >= 600000000L;
   }

   private AutoPotionFeature.PotionChoice findPotion(class_746 player) {
      AutoPotionFeature.PotionKind[] kinds = AutoPotionFeature.PotionKind.values();
      boolean[] selected = new boolean[kinds.length];
      boolean[] active = new boolean[kinds.length];
      int[] slots = new int[kinds.length];
      Arrays.fill(slots, -1);

      for (int index = 0; index < kinds.length; index++) {
         AutoPotionFeature.PotionKind kind = kinds[index];
         selected[index] = this.potions.isSelected(kind.settingName());
         active[index] = player.method_6059(kind.effect());
         if (selected[index] && !active[index]) {
            slots[index] = InventoryUtil.findPlayerMenuSlot(player, stack -> containsEffect(stack, kind.effect()));
         }
      }

      int selectedIndex = selectPotionIndex(selected, active, slots);
      return selectedIndex < 0 ? null : new AutoPotionFeature.PotionChoice(kinds[selectedIndex], slots[selectedIndex]);
   }

   private void beginThrow(class_746 player, AutoPotionFeature.PotionChoice choice, long nowNanos) {
      this.potionContainerSlot = choice.containerSlot();
      this.originalHotbarSlot = player.method_31548().method_67532();
      this.originalHotbarStack = player.method_31548().method_5438(this.originalHotbarSlot).method_7972();
      this.throwYaw = player.method_36454();
      this.operationTicks = 0;
      if (PveManagerFeature.INSTANCE.rotate.getValue()) {
         RotationContext.setRotation(this.throwYaw, 90.0F);
         this.rotationApplied = true;
      }

      InventorySwap.useFromSlot(this.potionContainerSlot);
      if (!InventorySwap.isBusy()) {
         this.clearAppliedRotation();
         PveAutomationCoordinator.INSTANCE.release(this);
         this.resetOperationState();
      } else {
         this.throwing = true;
         this.lastThrowNanos = nowNanos;
      }
   }

   private void tickOperation(class_746 player) {
      if (player == null) {
         this.cancelOperation();
      } else {
         if (this.rotationApplied) {
            RotationContext.setRotation(this.throwYaw, 90.0F);
         }

         this.operationTicks++;
         if (!InventorySwap.isBusy() || this.operationTicks >= 4) {
            this.finishOperation(player);
         }
      }
   }

   private void finishOperation(class_746 player) {
      InventorySwap.abort();
      this.restoreOriginalSlot(player);
      this.clearAppliedRotation();
      PveAutomationCoordinator.INSTANCE.release(this);
      this.resetOperationState();
   }

   private void cancelOperation() {
      if (this.throwing) {
         class_746 player = this.player();
         InventorySwap.abort();
         if (player != null) {
            this.restoreOriginalSlot(player);
         }

         this.clearAppliedRotation();
         PveAutomationCoordinator.INSTANCE.release(this);
         this.resetOperationState();
      }
   }

   private void restoreOriginalSlot(class_746 player) {
      if (this.originalHotbarSlot >= 0
         && this.originalHotbarSlot <= 8
         && this.potionContainerSlot >= 0
         && player.field_7512 == player.field_7498
         && mc.field_1761 != null) {
         int selectedContainerSlot = 36 + this.originalHotbarSlot;
         if (this.potionContainerSlot != selectedContainerSlot && player.field_7498.method_40442(this.potionContainerSlot)) {
            class_1799 source = player.field_7498.method_7611(this.potionContainerSlot).method_7677();
            class_1799 selected = player.field_7498.method_7611(selectedContainerSlot).method_7677();
            if (class_1799.method_7973(source, this.originalHotbarStack) && !class_1799.method_7973(selected, this.originalHotbarStack)) {
               mc.field_1761.method_2906(player.field_7498.field_7763, this.potionContainerSlot, this.originalHotbarSlot, class_1713.field_7791, player);
            }
         }

         if (player.method_31548().method_67532() != this.originalHotbarSlot) {
            player.method_31548().method_61496(this.originalHotbarSlot);
         }
      }
   }

   private boolean claimOperationResources() {
      return PveManagerFeature.INSTANCE.rotate.getValue()
         ? this.claim(AutomationResource.INVENTORY, AutomationResource.ROTATION, AutomationResource.MOVEMENT, AutomationResource.SCREEN)
         : this.claim(AutomationResource.INVENTORY, AutomationResource.MOVEMENT, AutomationResource.SCREEN);
   }

   private void clearAppliedRotation() {
      if (this.rotationApplied) {
         if (this.owns(AutomationResource.ROTATION) || !PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.ROTATION)) {
            RotationContext.clear();
         }

         this.rotationApplied = false;
      }
   }

   private void resetOperationState() {
      this.throwing = false;
      this.operationTicks = 0;
      this.potionContainerSlot = -1;
      this.originalHotbarSlot = -1;
      this.originalHotbarStack = class_1799.field_8037;
      this.throwYaw = 0.0F;
      this.rotationApplied = false;
   }

   private static boolean isOnOrNearGround(class_746 player, class_638 level) {
      return player.method_24828() ? true : level.method_20812(player, player.method_5829().method_1012(0.0, -0.5, 0.0)).iterator().hasNext();
   }

   private static boolean containsEffect(class_1799 stack, class_6880<class_1291> expected) {
      if (!stack.method_31574(class_1802.field_8436)) {
         return false;
      }

      class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
      if (contents == null) {
         return false;
      }

      for (class_1293 effect : contents.method_57397()) {
         if (effect.method_5579().equals(expected)) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasActivePlayerTarget() {
      if (PvpStateTracker.INSTANCE.isActive()) {
         return true;
      }

      AuraFeature aura = FeatureManager.INSTANCE.getEnabled(AuraFeature.class);
      return aura != null && aura.getCurrentTarget() instanceof class_1657 target && target.method_5805();
   }

   static int selectPotionIndex(boolean[] selected, boolean[] active, int[] slots) {
      if (selected.length == active.length && selected.length == slots.length) {
         for (int index = 0; index < selected.length; index++) {
            if (selected[index] && !active[index] && slots[index] >= 0) {
               return index;
            }
         }

         return -1;
      } else {
         throw new IllegalArgumentException("Potion state arrays must have equal lengths");
      }
   }

   @Environment(EnvType.CLIENT)
   private record PotionChoice(AutoPotionFeature.PotionKind kind, int containerSlot) {
   }

   @Environment(EnvType.CLIENT)
   private enum PotionKind {
      FIRE_RESISTANCE("Fire Resistance", class_1294.field_5918),
      STRENGTH("Strength", class_1294.field_5910),
      SPEED("Speed", class_1294.field_5904);

      private final String settingName;
      private final class_6880<class_1291> effect;

      PotionKind(String settingName, class_6880<class_1291> effect) {
         this.settingName = settingName;
         this.effect = effect;
      }

      String settingName() {
         return this.settingName;
      }

      class_6880<class_1291> effect() {
         return this.effect;
      }
   }
}
