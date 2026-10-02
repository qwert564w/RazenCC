package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1802;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.AttackEvent;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;

@Environment(EnvType.CLIENT)
public final class AutoMaceFeature extends Feature {
   private static final int HOTBAR_SIZE = 9;
   private int previousSlot = -1;
   public final BooleanSetting damageBoost = this.register(new BooleanSetting("Damage Boost", true));
   public final BooleanSetting autoSwitch = this.register(new BooleanSetting("Auto Switch Mace", false));

   public AutoMaceFeature() {
      super("Mace Helper", "Automates mace switching for airborne smash attacks", FeatureCategory.COMBAT, -1);
      this.renamedFrom("AutoMace");
   }

   @Override
   protected void onDisable() {
      this.restore(class_310.method_1551().field_1724);
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.previousSlot = -1;
   }

   @EventTarget
   public void onAttack(AttackEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (this.autoSwitch.getValue() && player != null && client.field_1687 != null && !player.method_24828()) {
         if (!player.method_6047().method_31574(class_1802.field_49814)) {
            if (!this.damageBoost.getValue() || !(player.field_6017 <= 1.5)) {
               int maceSlot = findMaceSlot(player);
               if (maceSlot != -1) {
                  this.previousSlot = player.method_31548().method_67532();
                  select(player, maceSlot);
               }
            }
         }
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null && this.previousSlot != -1) {
         this.restore(player);
      }
   }

   private void restore(class_746 player) {
      if (this.previousSlot != -1) {
         int slot = this.previousSlot;
         this.previousSlot = -1;
         if (player != null) {
            select(player, slot);
         }
      }
   }

   private static void select(class_746 player, int slot) {
      player.method_31548().method_61496(slot);
      if (player.field_3944 != null) {
         player.field_3944.method_52787(new class_2868(slot));
      }
   }

   private static int findMaceSlot(class_746 player) {
      for (int slot = 0; slot < 9; slot++) {
         if (player.method_31548().method_5438(slot).method_31574(class_1802.field_49814)) {
            return slot;
         }
      }

      return -1;
   }
}
