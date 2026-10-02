package org.ryzen.feature.impl.pve;

import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1536;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1887;
import net.minecraft.class_1893;
import net.minecraft.class_2765;
import net.minecraft.class_2767;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldJoinEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.mixin.accessor.FishingHookAccessor;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;

@Environment(EnvType.CLIENT)
public final class AutoFishFeature extends PveFeature {
   private static final int SAVE_DURABILITY = 10;
   private static final int INVENTORY_SIZE = 36;
   private static final int HOTBAR_SIZE = 9;
   private static final long HOOK_APPEAR_TIMEOUT_TICKS = 40L;
   private static final long RECAST_DELAY_TICKS = 8L;
   private static final long BITE_LATCH_TIMEOUT_TICKS = 40L;
   private static final long SPLASH_MAX_AGE_NANOS = 2000000000L;
   private static final double SPLASH_DISTANCE_SQUARED = 9.0;
   public final BooleanSetting saveRod = this.register(new BooleanSetting("Save Rod", false));
   private final AtomicReference<AutoFishFeature.SplashSignal> splashSignal = new AtomicReference<>();
   private AutoFishFeature.FishingState state = AutoFishFeature.FishingState.READY_TO_CAST;
   private long tick;
   private long stateSinceTick;
   private int observedHookId = -1;
   private boolean wasBiting;
   private boolean biteLatched;
   private long biteLatchedAtTick;
   private class_1268 activeHand;
   private int swapSettleTicks;
   private int managedHotbarSlot = -1;
   private int managedInventorySlot = -1;
   private int restoreSelectedSlot = -1;
   private class_1799 displacedStack = class_1799.field_8037;

   public AutoFishFeature() {
      super("AutoFish", "Casts and reels a fishing rod automatically", -1, AutomationPriority.FEATURE);
   }

   @Override
   protected void onPveEnable() {
      this.resetRuntime();
   }

   @Override
   protected void onPveDisable() {
      this.restoreManagedRod();
      this.resetRuntime();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      this.tick++;
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null && client.field_1761 != null) {
         if (client.field_1755 == null) {
            class_1536 hook = player.field_7513;
            if (hook != null && hook.method_6947() == player && !hook.method_31481()) {
               this.handleHookPresent(client, player, hook);
            } else {
               this.handleHookAbsent(client, player);
            }
         }
      } else {
         if (player != null && this.hasManagedRod()) {
            this.restoreManagedRod();
         }

         this.resetForMissingWorld();
      }
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE) {
         if (event.getPacket() instanceof class_2767 packet && isBobberSplash(packet.method_11894())) {
            this.splashSignal.set(new AutoFishFeature.SplashSignal(packet.method_11890(), packet.method_11889(), packet.method_11893(), -1, System.nanoTime()));
         } else {
            if (event.getPacket() instanceof class_2765 packet && isBobberSplash(packet.method_11882())) {
               this.splashSignal.set(new AutoFishFeature.SplashSignal(0.0, 0.0, 0.0, packet.method_11883(), System.nanoTime()));
            }
         }
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.restoreManagedRod();
      this.resetRuntime();
   }

   @EventTarget
   public void onWorldJoin(WorldJoinEvent event) {
      this.resetRuntime();
   }

   private void handleHookAbsent(class_310 client, class_746 player) {
      this.clearObservedHook();
      if (this.state == AutoFishFeature.FishingState.REEL_SENT || this.state == AutoFishFeature.FishingState.WAITING_FOR_BITE) {
         this.transition(AutoFishFeature.FishingState.RECAST_COOLDOWN);
      } else if (this.state == AutoFishFeature.FishingState.WAITING_FOR_HOOK) {
         if (this.ticksInState() >= 40L) {
            this.transition(AutoFishFeature.FishingState.READY_TO_CAST);
         }
      } else if (this.state == AutoFishFeature.FishingState.RECAST_COOLDOWN) {
         if (this.ticksInState() >= 8L) {
            this.transition(AutoFishFeature.FishingState.READY_TO_CAST);
         }
      } else {
         class_1268 hand = this.prepareRod(player);
         if (hand != null) {
            this.splashSignal.set(null);
            this.useRod(client, player, hand);
            this.activeHand = hand;
            this.transition(AutoFishFeature.FishingState.WAITING_FOR_HOOK);
         }
      }
   }

   private void handleHookPresent(class_310 client, class_746 player, class_1536 hook) {
      if (hook.method_5628() != this.observedHookId) {
         this.observedHookId = hook.method_5628();
         this.wasBiting = false;
         this.biteLatched = false;
         this.transition(AutoFishFeature.FishingState.WAITING_FOR_BITE);
      } else if (this.state != AutoFishFeature.FishingState.REEL_SENT && this.state != AutoFishFeature.FishingState.WAITING_FOR_BITE) {
         this.transition(AutoFishFeature.FishingState.WAITING_FOR_BITE);
      }

      boolean biting = ((FishingHookAccessor)hook).blade$isBiting();
      if (biting && !this.wasBiting) {
         this.latchBite();
      }

      if (this.consumeSplashNear(hook)) {
         this.latchBite();
      }

      this.wasBiting = biting;
      if (this.state != AutoFishFeature.FishingState.REEL_SENT && this.biteLatched) {
         if (this.tick - this.biteLatchedAtTick > 40L) {
            this.biteLatched = false;
         } else {
            class_1268 hand = this.prepareRod(player);
            if (hand != null) {
               this.useRod(client, player, hand);
               this.activeHand = hand;
               this.biteLatched = false;
               this.transition(AutoFishFeature.FishingState.REEL_SENT);
            }
         }
      }
   }

   private class_1268 prepareRod(class_746 player) {
      if (this.swapSettleTicks > 0) {
         this.swapSettleTicks--;
         return null;
      }

      class_1268 heldHand = this.findHeldRodHand(player);
      if (heldHand == null) {
         return null;
      }

      class_1799 held = player.method_5998(heldHand);
      if (!this.saveRod.getValue() || remainingDurability(held) > 10) {
         return heldHand;
      }

      if (this.hasManagedRod()) {
         if (this.restoreManagedRod()) {
            this.swapSettleTicks = Math.max(this.swapSettleTicks, 1);
         }

         return null;
      } else {
         AutoFishFeature.RodCandidate candidate = this.findBestUsableRod(player);
         if (candidate == null) {
            return null;
         } else {
            return candidate.offhand() ? class_1268.field_5810 : this.activateInventoryRod(player, candidate.inventoryIndex());
         }
      }
   }

   private class_1268 findHeldRodHand(class_746 player) {
      if (this.managedHotbarSlot >= 0 && player.method_31548().method_67532() == this.managedHotbarSlot && isRod(player.method_6047())) {
         return class_1268.field_5808;
      } else if (this.activeHand != null && isRod(player.method_5998(this.activeHand))) {
         return this.activeHand;
      } else if (isRod(player.method_6047())) {
         return class_1268.field_5808;
      } else {
         return isRod(player.method_6079()) ? class_1268.field_5810 : null;
      }
   }

   private AutoFishFeature.RodCandidate findBestUsableRod(class_746 player) {
      AutoFishFeature.RodCandidate best = null;
      int selectedSlot = player.method_31548().method_67532();

      for (int index = 0; index < 36; index++) {
         class_1799 stack = player.method_31548().method_5438(index);
         if (isUsableReplacement(stack)) {
            int accessibility = index == selectedSlot ? 3 : (index < 9 ? 2 : 1);
            AutoFishFeature.RodCandidate candidate = new AutoFishFeature.RodCandidate(
               index, false, remainingDurability(stack), unbreakingLevel(stack), accessibility
            );
            if (isBetter(candidate, best)) {
               best = candidate;
            }
         }
      }

      class_1799 offhand = player.method_6079();
      if (isUsableReplacement(offhand)) {
         AutoFishFeature.RodCandidate candidate = new AutoFishFeature.RodCandidate(-1, true, remainingDurability(offhand), unbreakingLevel(offhand), 3);
         if (isBetter(candidate, best)) {
            best = candidate;
         }
      }

      return best;
   }

   private class_1268 activateInventoryRod(class_746 player, int inventoryIndex) {
      int selectedSlot = player.method_31548().method_67532();
      if (inventoryIndex == selectedSlot) {
         return class_1268.field_5808;
      }

      if (!this.claim(AutomationResource.INVENTORY)) {
         return null;
      }

      try {
         if (!isUsableReplacement(player.method_31548().method_5438(inventoryIndex))) {
            return null;
         } else if (inventoryIndex < 9) {
            this.restoreSelectedSlot = selectedSlot;
            this.managedHotbarSlot = inventoryIndex;
            player.method_31548().method_61496(inventoryIndex);
            return class_1268.field_5808;
         } else if (player.field_7512 == player.field_7498 && player.field_7498.method_34255().method_7960()) {
            this.displacedStack = player.method_31548().method_5438(selectedSlot).method_7972();
            class_310.method_1551().field_1761.method_2906(player.field_7498.field_7763, inventoryIndex, selectedSlot, class_1713.field_7791, player);
            this.managedInventorySlot = inventoryIndex;
            this.managedHotbarSlot = selectedSlot;
            this.swapSettleTicks = 1;
            return null;
         } else {
            return null;
         }
      } finally {
         PveAutomationCoordinator.INSTANCE.release(this);
      }
   }

   private boolean restoreManagedRod() {
      if (!this.hasManagedRod()) {
         return true;
      }

      class_310 client = class_310.method_1551();
      class_746 player = client.field_1724;
      if (player != null && client.field_1761 != null && this.claim(AutomationResource.INVENTORY)) {
         try {
            if (this.managedInventorySlot >= 0) {
               if (player.field_7512 != player.field_7498 || !player.field_7498.method_34255().method_7960()) {
                  return false;
               }

               class_1799 source = player.method_31548().method_5438(this.managedInventorySlot);
               class_1799 managed = player.method_31548().method_5438(this.managedHotbarSlot);
               if (class_1799.method_7973(source, this.displacedStack) && isRod(managed)) {
                  client.field_1761.method_2906(player.field_7498.field_7763, this.managedInventorySlot, this.managedHotbarSlot, class_1713.field_7791, player);
                  this.swapSettleTicks = 1;
               }
            }

            if (this.restoreSelectedSlot >= 0 && player.method_31548().method_67532() == this.managedHotbarSlot) {
               player.method_31548().method_61496(this.restoreSelectedSlot);
            }

            this.clearManagedRod();
            return true;
         } finally {
            PveAutomationCoordinator.INSTANCE.release(this);
         }
      } else {
         return false;
      }
   }

   private void useRod(class_310 client, class_746 player, class_1268 hand) {
      if (isRod(player.method_5998(hand))) {
         class_1269 result = client.field_1761.method_2919(player, hand);
         if (result.method_23665()) {
            player.method_6104(hand);
         }
      }
   }

   private boolean consumeSplashNear(class_1536 hook) {
      AutoFishFeature.SplashSignal signal = this.splashSignal.getAndSet(null);
      if (signal == null || System.nanoTime() - signal.receivedAtNanos() > 2000000000L) {
         return false;
      } else {
         return signal.entityId() >= 0 ? signal.entityId() == hook.method_5628() : hook.method_5649(signal.x(), signal.y(), signal.z()) <= 9.0;
      }
   }

   private void latchBite() {
      this.biteLatched = true;
      this.biteLatchedAtTick = this.tick;
   }

   private void transition(AutoFishFeature.FishingState next) {
      if (this.state != next) {
         this.state = next;
         this.stateSinceTick = this.tick;
      }
   }

   private long ticksInState() {
      return Math.max(0L, this.tick - this.stateSinceTick);
   }

   private void clearObservedHook() {
      this.observedHookId = -1;
      this.wasBiting = false;
      this.biteLatched = false;
   }

   private void resetForMissingWorld() {
      this.splashSignal.set(null);
      this.state = AutoFishFeature.FishingState.READY_TO_CAST;
      this.stateSinceTick = this.tick;
      this.activeHand = null;
      this.swapSettleTicks = 0;
      this.clearObservedHook();
      this.clearManagedRod();
   }

   private void resetRuntime() {
      this.tick = 0L;
      this.state = AutoFishFeature.FishingState.READY_TO_CAST;
      this.stateSinceTick = 0L;
      this.activeHand = null;
      this.swapSettleTicks = 0;
      this.splashSignal.set(null);
      this.clearObservedHook();
      this.clearManagedRod();
   }

   private boolean hasManagedRod() {
      return this.managedHotbarSlot >= 0;
   }

   private void clearManagedRod() {
      this.managedHotbarSlot = -1;
      this.managedInventorySlot = -1;
      this.restoreSelectedSlot = -1;
      this.displacedStack = class_1799.field_8037;
   }

   private static boolean isBobberSplash(class_6880<class_3414> sound) {
      return sound != null && sound.comp_349() == class_3417.field_14660;
   }

   private static boolean isRod(class_1799 stack) {
      return stack != null && stack.method_31574(class_1802.field_8378);
   }

   private static boolean isUsableReplacement(class_1799 stack) {
      return isRod(stack) && isUsableRodDurability(remainingDurability(stack));
   }

   static boolean isUsableRodDurability(int remainingDurability) {
      return remainingDurability > 10;
   }

   private static int remainingDurability(class_1799 stack) {
      return Math.max(0, stack.method_7936() - stack.method_7919());
   }

   private static int unbreakingLevel(class_1799 stack) {
      for (class_6880<class_1887> enchantment : stack.method_58657().method_57534()) {
         if (enchantment.method_40225(class_1893.field_9119)) {
            return stack.method_58657().method_57536(enchantment);
         }
      }

      return 0;
   }

   static int compareRodQuality(int remainingA, int unbreakingA, int remainingB, int unbreakingB) {
      int durability = Integer.compare(remainingA, remainingB);
      return durability != 0 ? durability : Integer.compare(unbreakingA, unbreakingB);
   }

   private static boolean isBetter(AutoFishFeature.RodCandidate candidate, AutoFishFeature.RodCandidate currentBest) {
      if (currentBest == null) {
         return true;
      } else {
         int quality = compareRodQuality(
            candidate.remainingDurability(), candidate.unbreakingLevel(), currentBest.remainingDurability(), currentBest.unbreakingLevel()
         );
         if (quality != 0) {
            return quality > 0;
         } else {
            return candidate.accessibility() != currentBest.accessibility()
               ? candidate.accessibility() > currentBest.accessibility()
               : candidate.inventoryIndex() < currentBest.inventoryIndex();
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private enum FishingState {
      READY_TO_CAST,
      WAITING_FOR_HOOK,
      WAITING_FOR_BITE,
      REEL_SENT,
      RECAST_COOLDOWN;
   }

   @Environment(EnvType.CLIENT)
   private record RodCandidate(int inventoryIndex, boolean offhand, int remainingDurability, int unbreakingLevel, int accessibility) {
   }

   @Environment(EnvType.CLIENT)
   private record SplashSignal(double x, double y, double z, int entityId, long receivedAtNanos) {
   }
}
