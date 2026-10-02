package org.ryzen.feature.impl.player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1684;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class PearlTrackerFeature extends Feature implements PlayerContext {
   private static final int MAX_SIMULATION_TICKS = 300;
   private static final double AIR_INERTIA = 0.99;
   private static final double WATER_INERTIA = 0.8;
   private static final double THROWABLE_GRAVITY = 0.03;
   public final BooleanSetting ownPearls = this.register(new BooleanSetting("Track Own Pearls", false));
   public final BooleanSetting markAuraTarget = this.register(new BooleanSetting("Mark Aura Target", false));
   private final Set<UUID> reported = new HashSet<>();
   private class_243 auraTargetLanding;

   public PearlTrackerFeature() {
      super("PearlTracker", "Reports where a thrown ender pearl will land", FeatureCategory.PLAYER, -1);
   }

   public static PearlTrackerFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(PearlTrackerFeature.class);
   }

   public class_243 getAuraTargetLanding() {
      return this.markAuraTarget.getValue() ? this.auraTargetLanding : null;
   }

   @Override
   protected void onEnable() {
      this.reported.clear();
      this.auraTargetLanding = null;
   }

   @Override
   protected void onDisable() {
      this.reported.clear();
      this.auraTargetLanding = null;
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.reported.clear();
      this.auraTargetLanding = null;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      class_638 level = client.field_1687;
      if (player != null && level != null) {
         for (class_1297 entity : level.method_18112()) {
            if (entity instanceof class_1684 pearl && this.reported.add(pearl.method_5667())) {
               class_1657 owner = resolveOwner(level, pearl);
               if (owner != null && (owner != player || this.ownPearls.getValue())) {
                  class_243 landing = simulate(level, player, pearl);
                  this.report(owner, landing);
                  this.rememberAuraTarget(owner, landing);
               }
            }
         }

         if (this.reported.size() > 256) {
            this.reported.clear();
         }
      }
   }

   private void report(class_1657 owner, class_243 landing) {
      class_2561 name = owner.method_5476();
      String coords = (int)landing.field_1352 + " " + (int)landing.field_1351 + " " + (int)landing.field_1350;
      ChatUtil.info(name.getString() + class_124.field_1068 + " перлится на " + class_124.field_1060 + coords);
   }

   private void rememberAuraTarget(class_1657 owner, class_243 landing) {
      if (this.markAuraTarget.getValue()) {
         AuraFeature aura = FeatureManager.INSTANCE.getEnabled(AuraFeature.class);
         class_1309 target = aura == null ? null : aura.getCurrentTarget();
         if (target == owner) {
            this.auraTargetLanding = landing;
         }
      }
   }

   private static class_243 simulate(class_638 level, class_746 player, class_1684 pearl) {
      class_243 position = pearl.method_73189();
      class_243 velocity = pearl.method_18798();
      class_243 previous = position;

      for (int step = 0; step <= 300; step++) {
         previous = position;
         position = position.method_1019(velocity);
         boolean inWater = pearl.method_5799() || level.method_8316(class_2338.method_49638(position)).method_15767(class_3486.field_15517);
         velocity = velocity.method_1021(inWater ? 0.8 : 0.99);
         if (!pearl.method_5740()) {
            velocity = new class_243(velocity.field_1352, velocity.field_1351 - 0.03, velocity.field_1350);
         }

         class_3965 hit = level.method_17742(new class_3959(previous, position, class_3960.field_17558, class_242.field_1348, player));
         if (hit.method_17783() == class_240.field_1332) {
            return hit.method_17784();
         }

         if (position.field_1351 <= level.method_31607()) {
            return position;
         }
      }

      return previous;
   }

   private static class_1657 resolveOwner(class_638 level, class_1684 pearl) {
      if (pearl.method_24921() instanceof class_1657 owner) {
         return owner;
      } else {
         class_1657 nearest = null;
         double nearestDistance = Double.MAX_VALUE;

         for (class_1657 candidate : level.method_18456()) {
            double distance = candidate.method_5858(pearl);
            if (distance < nearestDistance) {
               nearestDistance = distance;
               nearest = candidate;
            }
         }

         return nearest;
      }
   }
}
