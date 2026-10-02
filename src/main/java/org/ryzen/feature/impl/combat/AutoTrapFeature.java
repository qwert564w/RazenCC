package org.ryzen.feature.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_2828.class_2831;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.FriendManager;

@Environment(EnvType.CLIENT)
public final class AutoTrapFeature extends Feature {
   private static final String TARGET_SINGLE = "Single";
   private static final String TARGET_MULTI = "Multi";
   private static final String BLOCK_COBWEB = "Cobweb";
   private static final String BLOCK_OBSIDIAN = "Obsidian";
   private static final String BLOCK_BOTH = "Both";
   private static final int HOTBAR_SIZE = 9;
   private static final int INVENTORY_SIZE = 36;
   public final ModeSetting targetMode = this.register(new ModeSetting("Targets", "Single", "Single", "Multi"));
   public final ModeSetting blockMode = this.register(new ModeSetting("Blocks", "Cobweb", "Cobweb", "Obsidian", "Both"));
   public final NumberSetting range = this.register(new NumberSetting("Range", 4.0, 1.0, 8.0, 0.5, " blocks"));
   public final BooleanSetting jumpForUpper = this.register(new BooleanSetting("Jump For Upper", true).visibleWhen(() -> !this.blockMode.is("Cobweb")));
   public final BooleanSetting useInventory = this.register(new BooleanSetting("Use Inventory", false));
   public final InputBindSetting placeKey = this.register(new InputBindSetting("Place Key", -1));
   private final List<class_2338> placementQueue = new ArrayList<>();
   private final List<class_2338> placedPositions = new ArrayList<>();
   private int placementIndex;
   private boolean keyHeld;
   private boolean pendingPlacement;
   private boolean pendingCobweb;
   private boolean jumpBeforePlacement;
   private class_2338 pendingPos;
   private class_2350 pendingFace;
   private int borrowFrom = -1;
   private int borrowTo = -1;

   public AutoTrapFeature() {
      super("AutoTrap", "Seals a nearby player in cobwebs or obsidian while the key is held", FeatureCategory.COMBAT, -1);
   }

   @Override
   protected void onDisable() {
      this.keyHeld = false;
      this.reset();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.keyHeld = false;
      this.reset();
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (this.placeKey.matches(event.getKey())) {
         this.setKeyHeld(event.getAction() != 0);
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      if (this.placeKey.matchesMouse(event.getButton())) {
         this.setKeyHeld(event.getAction() != 0);
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      class_638 level = client.field_1687;
      if (player != null && level != null && client.field_1761 != null && client.field_1755 == null) {
         if (!this.keyHeld || !this.hasRequiredBlocks(player)) {
            this.reset();
         } else if (this.pendingPlacement && this.pendingPos != null && this.pendingFace != null) {
            this.runPendingPlacement(client, player);
         } else {
            List<class_1657> targets = this.findTargets(player, level);
            if (targets.isEmpty()) {
               this.reset();
            } else {
               if (this.placementQueue.isEmpty() || this.placementIndex >= this.placementQueue.size()) {
                  this.rebuildQueue(level, targets);
               }

               this.scheduleNext(level, player, targets);
            }
         }
      }
   }

   private void rebuildQueue(class_638 level, List<class_1657> targets) {
      this.placementQueue.clear();
      this.placedPositions.clear();
      this.placementIndex = 0;

      for (class_1657 target : targets) {
         for (class_2338 pos : this.collectTrapPositions(level, target)) {
            if (!this.placementQueue.contains(pos)) {
               this.placementQueue.add(pos);
            }
         }
      }
   }

   private void scheduleNext(class_638 level, class_746 player, List<class_1657> targets) {
      while (this.placementIndex < this.placementQueue.size()) {
         class_2338 pos = this.placementQueue.get(this.placementIndex);
         if (!level.method_8320(pos).method_45474()) {
            this.placementIndex++;
         } else {
            boolean insideTarget = intersectsTarget(pos, targets);
            boolean cobweb = insideTarget && !this.blockMode.is("Obsidian") && this.hasCobweb(player);
            class_2350 face = findSupportFace(level, pos);
            if (face != null) {
               this.jumpBeforePlacement = this.jumpForUpper.getValue() && !cobweb && pos.method_10264() >= class_3532.method_15357(player.method_23320());
               this.schedulePlacement(player, pos, face, cobweb);
               if (this.jumpBeforePlacement && player.method_24828()) {
                  player.method_6043();
               }

               return;
            }

            class_2338 below = pos.method_10074();
            class_2350 belowFace = level.method_8320(below).method_45474() && !this.placedPositions.contains(below) ? findSupportFace(level, below) : null;
            if (belowFace != null) {
               this.placementQueue.add(this.placementIndex, below);
               this.schedulePlacement(player, below, belowFace, false);
               return;
            }

            this.placementIndex++;
         }
      }

      this.placementQueue.clear();
      this.placementIndex = 0;
   }

   private void schedulePlacement(class_746 player, class_2338 pos, class_2350 face, boolean cobweb) {
      this.pendingPos = pos;
      this.pendingFace = face;
      this.pendingCobweb = cobweb;
      this.pendingPlacement = true;
      class_2338 support = pos.method_10093(face);
      class_2350 opposite = face.method_10153();
      class_243 aim = class_243.method_24953(support).method_1031(opposite.method_10148() * 0.5, opposite.method_10164() * 0.5, opposite.method_10165() * 0.5);
      sendLookAt(player, aim);
   }

   private void runPendingPlacement(class_310 client, class_746 player) {
      if (!this.jumpBeforePlacement) {
         this.placeBlock(client, player, this.pendingPos, this.pendingFace, this.pendingCobweb);
         this.finishPending();
      } else {
         if (!player.method_24828() && player.method_18798().field_1351 > 0.0) {
            this.placeBlock(client, player, this.pendingPos, this.pendingFace, this.pendingCobweb);
            this.finishPending();
         } else if (player.method_24828()) {
            player.method_6043();
         }
      }
   }

   private void finishPending() {
      this.placedPositions.add(this.pendingPos);
      this.pendingPos = null;
      this.pendingFace = null;
      this.pendingPlacement = false;
      this.jumpBeforePlacement = false;
      this.placementIndex++;
   }

   private void placeBlock(class_310 client, class_746 player, class_2338 pos, class_2350 face, boolean cobweb) {
      int originalSlot = player.method_31548().method_67532();
      int slot = this.selectBlockSlot(client, player, cobweb);
      if (slot != -1) {
         class_2338 support = pos.method_10093(face);
         class_2350 hitFace = face.method_10153();
         class_243 hitVec = class_243.method_24953(support)
            .method_1031(hitFace.method_10148() * 0.5, hitFace.method_10164() * 0.5, hitFace.method_10165() * 0.5);
         client.field_1761.method_2896(player, class_1268.field_5808, new class_3965(hitVec, hitFace, support, false));
         player.method_6104(class_1268.field_5808);
         select(player, originalSlot);
         this.returnBorrowed(client, player);
      }
   }

   private int selectBlockSlot(class_310 client, class_746 player, boolean cobweb) {
      int slot = cobweb ? findInHotbar(player, AutoTrapFeature::isCobweb) : findInHotbar(player, AutoTrapFeature::isObsidian);
      if (slot == -1 && this.useInventory.getValue()) {
         int source = cobweb ? findInInventory(player, AutoTrapFeature::isCobweb) : findInInventory(player, AutoTrapFeature::isObsidian);
         if (source != -1) {
            int destination = (player.method_31548().method_67532() + 1) % 9;
            client.field_1761.method_2906(player.field_7498.field_7763, source, destination, class_1713.field_7791, player);
            this.borrowFrom = source;
            this.borrowTo = destination;
            slot = destination;
         }
      }

      if (slot == -1) {
         return -1;
      }

      select(player, slot);
      return slot;
   }

   private void returnBorrowed(class_310 client, class_746 player) {
      if (this.borrowFrom != -1 && client.field_1761 != null) {
         client.field_1761.method_2906(player.field_7498.field_7763, this.borrowFrom, this.borrowTo, class_1713.field_7791, player);
         this.borrowFrom = -1;
         this.borrowTo = -1;
      }
   }

   private List<class_2338> collectTrapPositions(class_638 level, class_1657 target) {
      class_2338 feet = target.method_24515();
      class_2338 head = feet.method_10084();
      class_2338 above = head.method_10084();
      List<class_2338> positions = new ArrayList<>();
      if (!this.blockMode.is("Obsidian")) {
         if (level.method_8320(feet).method_45474()) {
            positions.add(feet);
         }

         if (level.method_8320(head).method_45474()) {
            positions.add(head);
         }
      }

      if (!this.blockMode.is("Cobweb")) {
         for (class_2338 pos : List.of(
            feet.method_10095(),
            feet.method_10072(),
            feet.method_10078(),
            feet.method_10067(),
            head.method_10095(),
            head.method_10072(),
            head.method_10078(),
            head.method_10067(),
            above,
            above.method_10084()
         )) {
            if (level.method_8320(pos).method_45474() && !positions.contains(pos)) {
               positions.add(pos);
            }
         }
      }

      return positions;
   }

   private static class_2350 findSupportFace(class_638 level, class_2338 pos) {
      for (class_2350 direction : class_2350.values()) {
         class_2680 state = level.method_8320(pos.method_10093(direction));
         if (!state.method_26215() && !state.method_45474()) {
            return direction;
         }
      }

      return null;
   }

   private static boolean intersectsTarget(class_2338 pos, List<class_1657> targets) {
      class_238 box = new class_238(
         pos.method_10263(), pos.method_10264(), pos.method_10260(), pos.method_10263() + 1.0, pos.method_10264() + 1.0, pos.method_10260() + 1.0
      );

      for (class_1657 target : targets) {
         if (target.method_5829().method_994(box)) {
            return true;
         }
      }

      return false;
   }

   private List<class_1657> findTargets(class_746 player, class_638 level) {
      double range = this.range.getValue();
      class_238 box = player.method_5829().method_1014(range);
      List<class_1657> targets = new ArrayList<>(
         level.method_8390(
            class_1657.class,
            box,
            candidate -> candidate.method_5805() && candidate != player && !FriendManager.INSTANCE.isFriend(candidate.method_7334().name())
         )
      );
      targets.sort(Comparator.comparingDouble(player::method_5858));
      if (this.targetMode.is("Single")) {
         return targets.isEmpty() ? List.of() : List.of((class_1657)targets.getFirst());
      } else {
         return targets;
      }
   }

   private boolean hasRequiredBlocks(class_746 player) {
      if (this.blockMode.is("Cobweb")) {
         return this.hasCobweb(player);
      } else {
         return this.blockMode.is("Obsidian") ? this.hasObsidian(player) : this.hasCobweb(player) || this.hasObsidian(player);
      }
   }

   private boolean hasCobweb(class_746 player) {
      return findInHotbar(player, AutoTrapFeature::isCobweb) != -1 || this.useInventory.getValue() && findInInventory(player, AutoTrapFeature::isCobweb) != -1;
   }

   private boolean hasObsidian(class_746 player) {
      return findInHotbar(player, AutoTrapFeature::isObsidian) != -1
         || this.useInventory.getValue() && findInInventory(player, AutoTrapFeature::isObsidian) != -1;
   }

   private static int findInHotbar(class_746 player, Predicate<class_1792> match) {
      for (int slot = 0; slot < 9; slot++) {
         class_1799 stack = player.method_31548().method_5438(slot);
         if (!stack.method_7960() && match.test(stack.method_7909())) {
            return slot;
         }
      }

      return -1;
   }

   private static int findInInventory(class_746 player, Predicate<class_1792> match) {
      for (int slot = 9; slot < 36; slot++) {
         class_1799 stack = player.method_31548().method_5438(slot);
         if (!stack.method_7960() && match.test(stack.method_7909())) {
            return slot;
         }
      }

      return -1;
   }

   private static boolean isCobweb(class_1792 item) {
      return item == class_1802.field_8786;
   }

   private static boolean isObsidian(class_1792 item) {
      return item == class_1802.field_8281 || item == class_1802.field_22421;
   }

   private static void select(class_746 player, int slot) {
      if (slot >= 0 && slot < 9 && player.method_31548().method_67532() != slot) {
         player.method_31548().method_61496(slot);
         if (player.field_3944 != null) {
            player.field_3944.method_52787(new class_2868(slot));
         }
      }
   }

   private static void sendLookAt(class_746 player, class_243 aim) {
      if (player.field_3944 != null) {
         class_243 delta = aim.method_1020(player.method_33571());
         float yaw = class_3532.method_15393((float)(Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0));
         float pitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, Math.hypot(delta.field_1352, delta.field_1350))));
         player.field_3944.method_52787(new class_2831(yaw, pitch, player.method_24828(), player.field_5976));
      }
   }

   private void setKeyHeld(boolean held) {
      this.keyHeld = held;
      if (!held) {
         this.reset();
      }
   }

   private void reset() {
      this.pendingPlacement = false;
      this.jumpBeforePlacement = false;
      this.pendingPos = null;
      this.pendingFace = null;
      this.placementQueue.clear();
      this.placedPositions.clear();
      this.placementIndex = 0;
   }
}
