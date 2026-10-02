package org.ryzen.utils.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_3489;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class AuraAttackController {
   private int restoreSlot = -1;
   private int temporarySlot = -1;
   private int useKeyRestoreTicks = -1;

   public void releaseShieldBeforeAttack(class_746 player) {
      class_310 minecraft = class_310.method_1551();
      if (minecraft.field_1761 != null
         && this.useKeyRestoreTicks < 0
         && player.method_6115()
         && player.method_62821() != null
         && minecraft.field_1690.field_1904.method_1434()) {
         minecraft.field_1761.method_2897(player);
         minecraft.field_1690.field_1904.method_23481(false);
         this.useKeyRestoreTicks = 0;
      }
   }

   public boolean attack(class_746 player, class_1309 target, boolean breakShield) {
      class_310 minecraft = class_310.method_1551();
      if (minecraft.field_1761 != null && player != null && target != null && target.method_5805()) {
         AttackWindow.attack(target);
         if (breakShield && target.method_6039()) {
            this.axeFollowUp(minecraft, player, target);
         }

         return true;
      } else {
         return false;
      }
   }

   public void tick(class_746 player) {
      if (this.useKeyRestoreTicks >= 0 && --this.useKeyRestoreTicks < 0) {
         class_310.method_1551().field_1690.field_1904.method_23481(true);
      }

      if (this.restoreSlot >= 0 && player != null) {
         if (player.method_31548().method_67532() == this.temporarySlot) {
            player.method_31548().method_61496(this.restoreSlot);
         }

         this.clearRestore();
      }
   }

   public void reset(class_746 player) {
      if (player != null && this.restoreSlot >= 0 && player.method_31548().method_67532() == this.temporarySlot) {
         player.method_31548().method_61496(this.restoreSlot);
      }

      this.clearRestore();
      this.useKeyRestoreTicks = -1;
   }

   private void axeFollowUp(class_310 minecraft, class_746 player, class_1309 target) {
      int axeSlot = this.findHotbarAxe(player);
      if (axeSlot >= 0) {
         int selectedSlot = player.method_31548().method_67532();
         if (axeSlot != selectedSlot) {
            player.method_31548().method_61496(axeSlot);
            this.restoreSlot = selectedSlot;
            this.temporarySlot = axeSlot;
         }

         minecraft.field_1761.method_2918(player, target);
         player.method_6104(class_1268.field_5808);
      }
   }

   private int findHotbarAxe(class_746 player) {
      int selectedSlot = player.method_31548().method_67532();
      if (this.isAxe(player.method_31548().method_5438(selectedSlot))) {
         return selectedSlot;
      }

      for (int slot = 0; slot < 9; slot++) {
         if (this.isAxe(player.method_31548().method_5438(slot))) {
            return slot;
         }
      }

      return -1;
   }

   private boolean isAxe(class_1799 stack) {
      return !stack.method_7960() && stack.method_31573(class_3489.field_42612);
   }

   private void clearRestore() {
      this.restoreSlot = -1;
      this.temporarySlot = -1;
   }
}
