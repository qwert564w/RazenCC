package org.ryzen.feature.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.mixin.accessor.LivingEntityAccessor;
import org.ryzen.mixin.accessor.MinecraftAccessor;
import org.ryzen.mixin.accessor.MultiPlayerGameModeAccessor;

@Environment(EnvType.CLIENT)
public final class NoDelaysFeature extends Feature {
   private static final int DEFAULT_JUMP_DELAY = 10;
   private static final int DEFAULT_RIGHT_CLICK_DELAY = 4;
   private static final int DEFAULT_BLOCK_BREAK_DELAY = 5;
   public final BooleanSetting jump = this.register(new BooleanSetting("Jump", true));
   public final BooleanSetting rightClick = this.register(new BooleanSetting("Right Click", false));
   public final BooleanSetting experienceBottlesOnly = this.register(
      new BooleanSetting("Experience Bottles Only", false).visibleWhen(this.rightClick::getValue)
   );
   public final BooleanSetting blockBreak = this.register(new BooleanSetting("Block Break", false));

   public NoDelaysFeature() {
      super("NoDelays", "Removes selected player action delays", FeatureCategory.MISC, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      if (client.field_1724 != null) {
         if (this.jump.getValue()) {
            ((LivingEntityAccessor)client.field_1724).setNoJumpDelay(0);
         }

         if (this.rightClick.getValue()
            && (
               !this.experienceBottlesOnly.getValue()
                  || client.field_1724.method_6047().method_31574(class_1802.field_8287)
                  || client.field_1724.method_6079().method_31574(class_1802.field_8287)
            )) {
            ((MinecraftAccessor)client).setRightClickDelay(0);
         }

         if (this.blockBreak.getValue() && client.field_1761 != null) {
            ((MultiPlayerGameModeAccessor)client.field_1761).setDestroyDelay(0);
         }
      }
   }

   @Override
   protected void onDisable() {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null) {
         ((LivingEntityAccessor)client.field_1724).setNoJumpDelay(10);
      }

      ((MinecraftAccessor)client).setRightClickDelay(4);
      if (client.field_1761 != null) {
         ((MultiPlayerGameModeAccessor)client.field_1761).setDestroyDelay(5);
      }
   }
}
