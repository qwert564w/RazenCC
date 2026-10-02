package org.ryzen.feature.impl.combat;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1294;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_746;
import net.minecraft.class_2828.class_2829;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.AttackEvent;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.input.PlayerInputEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.MultiSelectSetting;

@Environment(EnvType.CLIENT)
public final class CriticalsFeature extends Feature implements MinecraftContext {
   private static final String IN_WEB = "In Web";
   private static final String NEGATIVE_EFFECTS = "Negative Effects";
   private static final double WEB_EXPAND = 0.3;
   private static final double WEB_STEP = 0.5;
   private static final double CRIT_PACKET_OFFSET = 1.0E-9;
   public final MultiSelectSetting conditions = this.register(new MultiSelectSetting("Conditions", List.of("In Web"), "In Web", "Negative Effects"));
   private int stopMotionTicks;

   public CriticalsFeature() {
      super("Criticals", "Forces criticals from webs and while slow falling", FeatureCategory.COMBAT, -1);
   }

   @Override
   protected void onDisable() {
      this.stopMotionTicks = 0;
   }

   @EventTarget
   public void onInput(PlayerInputEvent event) {
      if (this.stopMotionTicks > 0) {
         class_746 player = mc.field_1724;
         if (player != null) {
            class_243 velocity = player.method_18798();
            player.method_18800(0.0, velocity.field_1351, 0.0);
         }

         event.clearMovement(false, true);
         this.stopMotionTicks--;
      }
   }

   @EventTarget
   public void onAttack(AttackEvent event) {
      class_746 player = mc.field_1724;
      if (player != null) {
         if (this.conditions.isSelected("In Web") && this.inWeb()) {
            this.stopMotionTicks = Math.max(this.stopMotionTicks, 1);
            this.sendCritPacket(player);
         } else if (this.conditions.isSelected("Negative Effects") && this.canSlowFallCrit(player)) {
            this.sendCritPacket(player);
         }
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = mc.field_1724;
      if (player != null && this.conditions.isSelected("In Web") && this.inWeb()) {
         this.sendCritPacket(player);
      }
   }

   private boolean canSlowFallCrit(class_746 player) {
      return player.method_6059(class_1294.field_5906) && player.field_6017 <= 0.0;
   }

   private void sendCritPacket(class_746 player) {
      if (player.field_3944 != null) {
         player.field_3944.method_52787(new class_2829(player.method_23317(), player.method_23318() - 1.0E-9, player.method_23321(), false, player.field_5976));
      }
   }

   private boolean inWeb() {
      class_746 player = mc.field_1724;
      if (player != null && mc.field_1687 != null) {
         class_238 box = player.method_5829().method_1014(0.3);

         for (double x = box.field_1323; x <= box.field_1320; x += 0.5) {
            for (double y = box.field_1322; y <= box.field_1325; y += 0.5) {
               for (double z = box.field_1321; z <= box.field_1324; z += 0.5) {
                  if (mc.field_1687.method_8320(class_2338.method_49637(x, y, z)).method_27852(class_2246.field_10343)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
