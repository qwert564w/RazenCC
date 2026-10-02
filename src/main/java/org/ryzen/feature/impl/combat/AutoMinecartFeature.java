package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1665;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_2828.class_2831;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class AutoMinecartFeature extends Feature {
   private static final int SIMULATION_STEPS = 150;
   private static final double WATER_DRAG = 0.6;
   private static final double AIR_DRAG = 0.99;
   private static final double GRAVITY = 0.05;
   private static final double UNOWNED_ARROW_RANGE_SQR = 36.0;
   private static final double MIN_ARROW_SPEED_SQR = 0.01;
   private static final int HOTBAR_SIZE = 9;
   private static final int INVENTORY_SIZE = 36;
   public final NumberSetting distance = this.register(new NumberSetting("Distance", 4.5, 1.0, 6.0, 0.1, " blocks"));
   public final BooleanSetting useInventory = this.register(new BooleanSetting("Take From Inventory", true));
   private class_2338 pendingPos;
   private boolean placingRail;
   private boolean restoreSlot;
   private int originalSlot;
   private int railSlot = -1;
   private int cartSlot = -1;
   private int trackedArrowId = -1;
   private int railBorrowFrom = -1;
   private int railBorrowTo = -1;
   private int cartBorrowFrom = -1;
   private int cartBorrowTo = -1;

   public AutoMinecartFeature() {
      super("AutoMinecart", "Places a rail and a TNT minecart where your arrow will land", FeatureCategory.COMBAT, -1);
   }

   @Override
   protected void onEnable() {
      this.clearPending();
   }

   @Override
   protected void onDisable() {
      class_310 client = class_310.method_1551();
      class_746 player = client.field_1724;
      if (player != null && client.field_1761 != null && (this.pendingPos != null || this.restoreSlot)) {
         select(player, this.originalSlot);
         this.returnBorrowedSlots(client, player);
      }

      this.clearPending();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.clearPending();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      class_638 level = client.field_1687;
      if (player == null || level == null || client.field_1761 == null) {
         this.clearPending();
      } else if (this.restoreSlot) {
         this.restoreSlot = false;
         select(player, this.originalSlot);
         this.returnBorrowedSlots(client, player);
      } else if (this.pendingPos == null) {
         this.scanForArrow(client, player, level);
      } else {
         this.continuePlacement(client, player, level);
      }
   }

   private void scanForArrow(class_310 client, class_746 player, class_638 level) {
      for (class_1297 entity : level.method_18112()) {
         if (entity instanceof class_1665 arrow && arrow.method_5628() != this.trackedArrowId) {
            class_1297 owner = arrow.method_24921();
            if ((owner == null || owner == player)
               && (owner != null || !(arrow.method_5858(player) > 36.0))
               && !(arrow.method_18798().method_1027() < 0.01)
               && this.beginPlacement(client, player, level, arrow)) {
               this.trackedArrowId = arrow.method_5628();
               return;
            }
         }
      }
   }

   private boolean beginPlacement(class_310 client, class_746 player, class_638 level, class_1665 arrow) {
      class_3965 impact = this.simulate(level, player, arrow);
      if (impact != null && impact.method_17783() == class_240.field_1332) {
         class_2350 face = impact.method_17780();
         class_243 nudged = impact.method_17784().method_1031(face.method_10148() * 0.02, face.method_10164() * 0.02, face.method_10165() * 0.02);
         class_2338 target = this.findPlaceablePos(level, class_2338.method_49638(nudged));
         if (target != null && this.inReach(player, target)) {
            int rail = findInHotbar(player, class_1802.field_8129);
            int cart = findInHotbar(player, class_1802.field_8069);
            if (rail == -1 && this.useInventory.getValue()) {
               rail = this.borrow(client, player, class_1802.field_8129, cart, true);
            }

            if (cart == -1 && this.useInventory.getValue()) {
               cart = this.borrow(client, player, class_1802.field_8069, rail, false);
            }

            if (rail != -1 && cart != -1) {
               this.pendingPos = target;
               this.railSlot = rail;
               this.cartSlot = cart;
               this.placingRail = true;
               this.originalSlot = player.method_31548().method_67532();
               return true;
            } else {
               this.returnBorrowedSlots(client, player);
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private class_3965 simulate(class_638 level, class_746 player, class_1665 arrow) {
      class_243 position = arrow.method_73189();
      class_243 velocity = arrow.method_18798();

      for (int step = 0; step <= 150; step++) {
         class_243 from = position;
         position = position.method_1019(velocity);
         boolean inWater = arrow.method_5799() || level.method_8320(class_2338.method_49638(from)).method_27852(class_2246.field_10382);
         velocity = velocity.method_1021(inWater ? 0.6 : 0.99);
         if (!arrow.method_5740()) {
            velocity = new class_243(velocity.field_1352, velocity.field_1351 - 0.05, velocity.field_1350);
         }

         class_3965 hit = level.method_17742(new class_3959(from, position, class_3960.field_17558, class_242.field_1348, player));
         if (hit.method_17783() == class_240.field_1332) {
            return hit;
         }

         if (position.field_1351 <= level.method_31607()) {
            break;
         }
      }

      return null;
   }

   private class_2338 findPlaceablePos(class_638 level, class_2338 impact) {
      int[][] offsets = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

      for (int drop = 0; drop <= 6; drop++) {
         int y = impact.method_10264() - drop;

         for (int[] offset : offsets) {
            class_2338 candidate = new class_2338(impact.method_10263() + offset[0], y, impact.method_10260() + offset[1]);
            if (isPlaceable(level, candidate)) {
               return candidate;
            }
         }
      }

      return null;
   }

   private static boolean isPlaceable(class_638 level, class_2338 pos) {
      class_2338 below = pos.method_10074();
      return level.method_8320(pos).method_45474() && level.method_8320(below).method_26206(level, below, class_2350.field_11036);
   }

   private void continuePlacement(class_310 client, class_746 player, class_638 level) {
      class_2338 pos = this.pendingPos;
      if (!this.inReach(player, pos)) {
         this.abortPlacement();
      } else if (this.placingRail) {
         class_2338 below = pos.method_10074();
         if (level.method_8320(pos).method_45474() && level.method_8320(below).method_26206(level, below, class_2350.field_11036)) {
            this.faceAndSelect(player, pos, this.railSlot);
            class_243 hitVec = new class_243(below.method_10263() + 0.5, below.method_10264() + 1.0, below.method_10260() + 0.5);
            client.field_1761.method_2896(player, class_1268.field_5808, new class_3965(hitVec, class_2350.field_11036, below, false));
            player.method_6104(class_1268.field_5808);
            this.placingRail = false;
         } else {
            this.abortPlacement();
         }
      } else {
         this.faceAndSelect(player, pos, this.cartSlot);
         class_243 hitVec = class_243.method_24953(pos).method_1031(0.0, 0.5, 0.0);
         client.field_1761.method_2896(player, class_1268.field_5808, new class_3965(hitVec, class_2350.field_11036, pos, false));
         player.method_6104(class_1268.field_5808);
         this.pendingPos = null;
         this.restoreSlot = true;
      }
   }

   private void abortPlacement() {
      this.pendingPos = null;
      this.placingRail = false;
      this.restoreSlot = true;
   }

   private void faceAndSelect(class_746 player, class_2338 pos, int slot) {
      class_243 center = class_243.method_24953(pos);
      class_243 delta = center.method_1020(player.method_33571());
      float yaw = class_3532.method_15393((float)(Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0));
      float pitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, Math.hypot(delta.field_1352, delta.field_1350))));
      if (player.field_3944 != null) {
         player.field_3944.method_52787(new class_2831(yaw, pitch, player.method_24828(), player.field_5976));
      }

      select(player, slot);
   }

   private boolean inReach(class_746 player, class_2338 pos) {
      double reach = this.distance.getValue();
      return player.method_33571().method_1025(class_243.method_24953(pos)) <= reach * reach;
   }

   private int borrow(class_310 client, class_746 player, class_1792 item, int avoidSlot, boolean rail) {
      int source = -1;

      for (int slot = 9; slot < 36; slot++) {
         if (player.method_31548().method_5438(slot).method_31574(item)) {
            source = slot;
            break;
         }
      }

      if (source == -1) {
         return -1;
      }

      int destination = -1;

      for (int slot = 0; slot < 9; slot++) {
         if (slot != avoidSlot && player.method_31548().method_5438(slot).method_7960()) {
            destination = slot;
            break;
         }
      }

      if (destination == -1) {
         destination = avoidSlot == 8 ? 7 : 8;
      }

      client.field_1761.method_2906(player.field_7498.field_7763, source, destination, class_1713.field_7791, player);
      if (rail) {
         this.railBorrowFrom = source;
         this.railBorrowTo = destination;
      } else {
         this.cartBorrowFrom = source;
         this.cartBorrowTo = destination;
      }

      return destination;
   }

   private void returnBorrowedSlots(class_310 client, class_746 player) {
      if (client.field_1761 != null) {
         int containerId = player.field_7498.field_7763;
         if (this.railBorrowFrom != -1) {
            client.field_1761.method_2906(containerId, this.railBorrowFrom, this.railBorrowTo, class_1713.field_7791, player);
            this.railBorrowFrom = -1;
         }

         if (this.cartBorrowFrom != -1) {
            client.field_1761.method_2906(containerId, this.cartBorrowFrom, this.cartBorrowTo, class_1713.field_7791, player);
            this.cartBorrowFrom = -1;
         }
      }
   }

   private static void select(class_746 player, int slot) {
      if (slot >= 0 && slot < 9) {
         player.method_31548().method_61496(slot);
         if (player.field_3944 != null) {
            player.field_3944.method_52787(new class_2868(slot));
         }
      }
   }

   private static int findInHotbar(class_746 player, class_1792 item) {
      for (int slot = 0; slot < 9; slot++) {
         if (player.method_31548().method_5438(slot).method_31574(item)) {
            return slot;
         }
      }

      return -1;
   }

   private void clearPending() {
      this.pendingPos = null;
      this.placingRail = false;
      this.restoreSlot = false;
      this.railSlot = -1;
      this.cartSlot = -1;
      this.trackedArrowId = -1;
      this.railBorrowFrom = -1;
      this.railBorrowTo = -1;
      this.cartBorrowFrom = -1;
      this.cartBorrowTo = -1;
   }
}
