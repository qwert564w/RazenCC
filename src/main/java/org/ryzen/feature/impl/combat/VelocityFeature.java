package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2708;
import net.minecraft.class_2743;
import net.minecraft.class_2828;
import net.minecraft.class_2828.class_5911;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.event.events.packet.PacketSendEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.ModeSetting;

@Environment(EnvType.CLIENT)
public final class VelocityFeature extends Feature implements MinecraftContext {
   private static final String MODE_VANILLA = "Vanilla";
   private static final String MODE_GRIM_TICKS = "Grim by ticks";
   private static final String MODE_GRIM = "Grim";
   private static final String MODE_GRIM_AIR = "Grim in air";
   private static final String MODE_OLD_GRIM = "Old grim";
   private static final int SWALLOW_TICKS = 6;
   private static final int GROUND_SPOOF_TICKS = 3;
   private static final float FALL_LIMIT = 15.0F;
   private static final float KNOCKBACK_MIN = 0.09F;
   private static final int OLD_GRIM_PERIOD = 2;
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Vanilla", "Vanilla", "Grim by ticks", "Grim", "Grim in air", "Old grim"));
   private int swallowTicks;
   private int groundTicks;
   private int oldGrimCount;
   private boolean airborne;

   public VelocityFeature() {
      super("Velocity", "Removes knockback", FeatureCategory.COMBAT, -1);
   }

   @Override
   protected void onDisable() {
      this.swallowTicks = 0;
      this.groundTicks = 0;
      this.oldGrimCount = 0;
      this.airborne = false;
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE && this.player() != null) {
         if (event.getPacket() instanceof class_2708 && this.mode.is("Grim")) {
            this.groundTicks = 3;
         } else if (event.getPacket() instanceof class_2743 packet && packet.method_11818() == this.player().method_5628()) {
            switch ((String)this.mode.getValue()) {
               case "Grim by ticks":
                  this.swallowTicks = 6;
                  event.cancel();
                  break;
               case "Grim":
                  this.airborne = true;
                  event.cancel();
                  break;
               case "Grim in air":
                  if (this.canRefuseInAir()) {
                     event.cancel();
                  }
                  break;
               case "Old grim":
                  if (this.oldGrimCount >= 2) {
                     this.oldGrimCount = 0;
                     return;
                  }

                  this.oldGrimCount++;
                  event.cancel();
                  break;
               default:
                  event.cancel();
            }
         }
      }
   }

   @EventTarget
   public void onPacketSend(PacketSendEvent event) {
      if (this.player() != null && event.getPacket() instanceof class_2828) {
         if (this.mode.is("Grim by ticks") && this.swallowTicks > 0) {
            this.swallowTicks--;
            event.cancel();
         } else if (this.mode.is("Grim") && this.airborne) {
            this.groundTicks--;
            if (this.groundTicks <= 0) {
               this.player().field_3944.method_52787(new class_5911(true, this.player().field_5976));
            }

            this.airborne = false;
         }
      }
   }

   private boolean canRefuseInAir() {
      return !this.player().method_6128() && !this.player().method_5799() && !this.player().method_5771()
         ? this.player().field_6017 > 0.09F && this.player().field_6017 < 15.0
         : false;
   }
}
