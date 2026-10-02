package org.ryzen.feature.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_418;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;

@Environment(EnvType.CLIENT)
public final class AutoRespawnFeature extends Feature {
   public final NumberSetting delay = this.register(new NumberSetting("Respawn Delay", 0.0, 0.0, 100.0, 1.0, " ticks"));
   public final BooleanSetting teleportHome = this.register(new BooleanSetting("Teleport Home", false));
   public final TextSetting command = this.register(new TextSetting("Command", "/home", 128).visibleWhen(this.teleportHome::getValue));
   private int deathTicks;
   private int commandDelay = -1;

   public AutoRespawnFeature() {
      super("AutoRespawn", "Respawns automatically after death", FeatureCategory.PLAYER, -1);
   }

   @Override
   protected void onDisable() {
      this.reset();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player == null) {
         this.reset();
      } else if (event.getClient().field_1755 instanceof class_418) {
         if (this.deathTicks++ > this.delay.getValue().intValue()) {
            player.method_7331();
            event.getClient().method_1507(null);
            this.deathTicks = 0;
            this.commandDelay = this.teleportHome.getValue() ? 1 : -1;
         }
      } else {
         this.deathTicks = 0;
         if (this.commandDelay > 0) {
            this.commandDelay--;
         } else {
            if (this.commandDelay == 0) {
               this.commandDelay = -1;
               String value = this.command.getValue().trim();
               if (value.startsWith("/")) {
                  value = value.substring(1);
               }

               if (!value.isBlank()) {
                  player.field_3944.method_45730(value);
               }
            }
         }
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.reset();
   }

   private void reset() {
      this.deathTicks = 0;
      this.commandDelay = -1;
   }
}
