package org.ryzen.feature.impl.combat;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.combat.CombatTargets;
import org.ryzen.utils.combat.TargetFilter;
import org.ryzen.utils.combat.TargetUtil;

@Environment(EnvType.CLIENT)
public final class AimAssistFeature extends Feature implements MinecraftContext {
   private static final float DAMP_CUTOFF_DEGREES = 40.0F;
   public final NumberSetting distance = this.register(new NumberSetting("Distance", 4.0, 0.5, 6.0, 0.1, " blocks"));
   public final MultiSelectSetting targets = this.register(
      new MultiSelectSetting("Targets", List.of("Players"), "Players", "Naked Players", "Invisibles", "Monsters", "Animals", "Villagers")
   );
   public final BooleanSetting assistYaw = this.register(new BooleanSetting("Assist Yaw", true));
   public final NumberSetting yawAcceleration = this.register(
      new NumberSetting("Yaw Acceleration", 1.75, 0.1, 5.0, 0.05, "x").visibleWhen(this.assistYaw::getValue)
   );
   public final NumberSetting yawDamping = this.register(new NumberSetting("Yaw Damping", 0.4, 0.1, 5.0, 0.05, "x").visibleWhen(this.assistYaw::getValue));
   public final NumberSetting yawRange = this.register(new NumberSetting("Yaw Range", 20.0, 0.0, 100.0, 5.0, "°").visibleWhen(this.assistYaw::getValue));
   public final BooleanSetting assistPitch = this.register(new BooleanSetting("Assist Pitch", false));
   public final NumberSetting pitchAcceleration = this.register(
      new NumberSetting("Pitch Acceleration", 1.25, 0.1, 5.0, 0.05, "x").visibleWhen(this.assistPitch::getValue)
   );
   public final NumberSetting pitchDamping = this.register(
      new NumberSetting("Pitch Damping", 0.75, 0.1, 5.0, 0.05, "x").visibleWhen(this.assistPitch::getValue)
   );
   public final NumberSetting pitchRange = this.register(new NumberSetting("Pitch Range", 20.0, 0.0, 100.0, 5.0, "°").visibleWhen(this.assistPitch::getValue));
   private class_1309 target;

   public AimAssistFeature() {
      super("AimAssist", "Speeds up mouse movement towards the target", FeatureCategory.COMBAT, -1);
   }

   public static AimAssistFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(AimAssistFeature.class);
   }

   public class_1309 getCurrentTarget() {
      return this.isEnabled() ? this.target : null;
   }

   @Override
   protected void onDisable() {
      this.target = null;
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.target = null;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      this.target = player != null && event.getClient().field_1687 != null
         ? TargetUtil.getBestTarget(player, event.getClient().field_1687, this.distance.getValue(), this.filter())
         : null;
   }

   public double scaleYaw(class_746 player, double yawDelta) {
      if (!this.assistYaw.getValue()) {
         return yawDelta;
      } else {
         float difference = this.yawDifference(player);
         if (!Float.isNaN(difference) && !(Math.abs(difference) <= this.yawRange.getValue())) {
            double damping = Math.abs(difference) < 40.0F ? this.yawDamping.getValue() : 1.0;
            return scale(yawDelta, difference, this.yawAcceleration.getValue(), damping);
         } else {
            return yawDelta;
         }
      }
   }

   public double scalePitch(class_746 player, double pitchDelta) {
      if (!this.assistPitch.getValue()) {
         return pitchDelta;
      }

      float difference = this.pitchDifference(player);
      return !Float.isNaN(difference) && !(Math.abs(difference) <= this.pitchRange.getValue())
         ? scale(pitchDelta, difference, this.pitchAcceleration.getValue(), this.pitchDamping.getValue())
         : pitchDelta;
   }

   private static double scale(double delta, float difference, double acceleration, double damping) {
      boolean towardsTarget = Math.signum(delta) == Math.signum(difference);
      double factor = towardsTarget ? acceleration : damping;
      double scaled = delta * factor;
      return Double.isFinite(scaled) ? scaled : delta;
   }

   private float yawDifference(class_746 player) {
      class_1309 current = this.target;
      if (current != null && current.method_5805()) {
         class_243 delta = current.method_5829().method_1005().method_1020(player.method_33571());
         float wanted = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
         return class_3532.method_15393(wanted - player.method_36454());
      } else {
         return Float.NaN;
      }
   }

   private float pitchDifference(class_746 player) {
      class_1309 current = this.target;
      if (current != null && current.method_5805()) {
         class_243 delta = current.method_5829().method_1005().method_1020(player.method_33571());
         double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
         float wanted = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
         return class_3532.method_15393(wanted - player.method_36455());
      } else {
         return Float.NaN;
      }
   }

   private TargetFilter filter() {
      return CombatTargets.groups(TargetFilter.builder(), this.targets).build();
   }
}
