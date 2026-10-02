package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.TimerUtil;

@Environment(EnvType.CLIENT)
public final class ElytraTimerFeature extends Feature implements MinecraftContext {
   public final NumberSetting strength = this.register(new NumberSetting("Strength", 1.5, 1.0, 5.0, 0.1, "x"));
   public final BooleanSetting alternate = this.register(new BooleanSetting("Alternate", true));
   public final NumberSetting reset = this.register(new NumberSetting("Reset", 0.5, 0.0, 0.9, 0.05, "x").visibleWhen(this.alternate::getValue));
   private int phase;
   private boolean timerActive;

   public ElytraTimerFeature() {
      super("ElytraTimer", "Speeds up the client clock while gliding on an elytra", FeatureCategory.MOVEMENT, -1);
   }

   @Override
   protected void onEnable() {
      this.phase = 0;
      this.timerActive = false;
   }

   @Override
   protected void onDisable() {
      if (this.timerActive) {
         TimerUtil.resetTimer();
         this.timerActive = false;
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      boolean gliding = player != null && player.method_6128();
      if (!gliding) {
         if (this.timerActive) {
            TimerUtil.resetTimer();
            this.timerActive = false;
         }

         this.phase = 0;
      } else {
         float multiplier;
         if (this.alternate.getValue()) {
            multiplier = this.phase % 2 == 0 ? this.strength.getValue().floatValue() : this.reset.getValue().floatValue();
         } else {
            multiplier = this.strength.getValue().floatValue();
         }

         TimerUtil.setTimer(multiplier);
         this.timerActive = true;
         this.phase++;
      }
   }
}
