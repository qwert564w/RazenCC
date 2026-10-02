package org.ryzen.feature.impl.movement;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1304;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_2848;
import net.minecraft.class_746;
import net.minecraft.class_2848.class_2849;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class ElytraJumpFeature extends Feature implements MinecraftContext {
   private static final List<class_1792> CHESTPLATES = List.of(
      class_1802.field_22028, class_1802.field_8058, class_1802.field_8523, class_1802.field_8678, class_1802.field_8873, class_1802.field_8577
   );
   private static final int CHEST_MENU_SLOT = 6;
   private static final double BOOST_STEP = 0.03062;
   public final BooleanSetting autoSwap = this.register(new BooleanSetting("Auto Swap", false));

   public ElytraJumpFeature() {
      super("ElytraJump", "Auto-deploys and holds an elytra glide", FeatureCategory.MOVEMENT, -1);
   }

   @Override
   protected void onEnable() {
      if (this.autoSwap.getValue() && mc.field_1724 != null) {
         this.equipElytra();
      }
   }

   @Override
   protected void onDisable() {
      class_746 player = mc.field_1724;
      if (player != null) {
         mc.field_1690.field_1903.method_23481(false);
         if (this.autoSwap.getValue() && this.hasElytra(player)) {
            this.equipChestplate();
         }
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null) {
         boolean grounded = player.method_24828();
         boolean inFluid = player.method_5799() || player.method_5771();
         if (!player.method_31549().field_7479 && grounded && !inFluid && this.hasElytra(player) && !mc.field_1690.field_1903.method_1434()) {
            player.method_6043();
         }

         if (!player.method_31549().field_7479 && !grounded && !inFluid && !player.method_6128() && this.canDeploy(player)) {
            this.deploy(player);
         }

         if (grounded || inFluid) {
            mc.field_1690.field_1903.method_23481(false);
         }

         if (player.field_6235 > 0 && this.autoSwap.getValue() && this.hasElytra(player)) {
            this.equipChestplate();
         } else {
            if (this.hasElytra(player)) {
               mc.field_1690.field_1903.method_23481(true);
               if (player.method_6128()) {
                  this.boost(player);
               }
            } else if (this.autoSwap.getValue()) {
               this.equipElytra();
            } else {
               mc.field_1690.field_1903.method_23481(false);
            }
         }
      }
   }

   private void boost(class_746 player) {
      class_243 velocity = player.method_18798();
      boolean rising = velocity.field_1351 > 0.08 || player.field_6017 > 0.1F;
      boolean still = Math.abs(velocity.field_1352) <= 0.01 && Math.abs(velocity.field_1350) <= 0.01;
      if (rising && still) {
         player.method_18800(0.0, velocity.field_1351 + 0.03062, 0.0);
         player.field_6037 = true;
      }
   }

   private void deploy(class_746 player) {
      if (player.field_3944 != null) {
         player.field_3944.method_52787(new class_2848(player, class_2849.field_12982));
         player.method_23668();
      }
   }

   private boolean canDeploy(class_746 player) {
      if (this.hasElytra(player) && !player.method_5799() && !player.method_5771() && !player.method_5765()) {
         class_1799 chest = player.method_6118(class_1304.field_6174);
         return chest.method_31574(class_1802.field_8833) && chest.method_7919() < chest.method_7936() - 1;
      } else {
         return false;
      }
   }

   private boolean hasElytra(class_746 player) {
      return player.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833);
   }

   private void equipElytra() {
      class_746 player = mc.field_1724;
      if (player != null && mc.field_1761 != null) {
         int slot = InventoryUtil.findPlayerMenuSlot(player, stack -> stack.method_31574(class_1802.field_8833));
         if (slot != -1) {
            this.swapToChest(player, slot);
         }
      }
   }

   private void equipChestplate() {
      class_746 player = mc.field_1724;
      if (player != null && mc.field_1761 != null) {
         int slot = InventoryUtil.findPlayerMenuSlot(player, stack -> CHESTPLATES.contains(stack.method_7909()));
         if (slot != -1) {
            this.swapToChest(player, slot);
         }
      }
   }

   private void swapToChest(class_746 player, int slot) {
      InventoryUtil.leftClickSlot(slot);
      InventoryUtil.leftClickSlot(6);
      InventoryUtil.leftClickSlot(slot);
   }
}
