package org.ryzen.feature.impl.misc;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1542;
import net.minecraft.class_1675;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.mixin.accessor.MinecraftAccessor;

@Environment(EnvType.CLIENT)
public final class CrystalOptimizerFeature extends Feature implements PlayerContext {
   private static final Set<class_1792> VALUABLES = Set.of(
      class_1802.field_22027,
      class_1802.field_22028,
      class_1802.field_22029,
      class_1802.field_22030,
      class_1802.field_8805,
      class_1802.field_8058,
      class_1802.field_8348,
      class_1802.field_8285,
      class_1802.field_22022,
      class_1802.field_8802,
      class_1802.field_22024,
      class_1802.field_8377,
      class_1802.field_22023,
      class_1802.field_8250,
      class_1802.field_22025,
      class_1802.field_8556,
      class_1802.field_8288,
      class_1802.field_8301,
      class_1802.field_8367,
      class_1802.field_8463,
      class_1802.field_8634,
      class_1802.field_8547,
      class_1802.field_8399,
      class_1802.field_8833
   );
   public final BooleanSetting protectDrops = this.register(new BooleanSetting("Protect Drops", true));
   public final NumberSetting range = this.register(new NumberSetting("Range", 3.0, 1.0, 6.0, 0.1, " blocks"));
   private class_1511 current;

   public CrystalOptimizerFeature() {
      super("CrystalOptimizer", "Breaks crystals without the use cooldown while right click is held", FeatureCategory.MISC, -1);
   }

   @Override
   protected void onDisable() {
      this.current = null;
   }

   public class_1511 getCurrent() {
      return this.isEnabled() ? this.current : null;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null && client.field_1761 != null) {
         this.current = this.findCrystal(client, player, client.field_1687);
         if (this.current != null && client.field_1690.field_1904.method_1434()) {
            ((MinecraftAccessor)client).setRightClickDelay(0);
            client.field_1761.method_2918(player, this.current);
            player.method_6104(class_1268.field_5808);
         }
      } else {
         this.current = null;
      }
   }

   private class_1511 findCrystal(class_310 client, class_746 player, class_638 level) {
      double reach = this.range.getValue();

      for (class_1297 entity : level.method_18112()) {
         if (entity instanceof class_1511 crystal
            && !(player.method_5739(crystal) > reach)
            && isAimedAt(client, player, level, crystal, reach)
            && (!this.protectDrops.getValue() || !hasValuablesNearby(level, crystal.method_24515()))) {
            return crystal;
         }
      }

      return null;
   }

   private static boolean isAimedAt(class_310 client, class_746 player, class_638 level, class_1511 crystal, double reach) {
      class_243 eye = player.method_33571();
      class_243 end = eye.method_1019(player.method_5828(1.0F).method_1021(reach));
      class_3966 entityHit = class_1675.method_37226(level, player, eye, end, new class_238(eye, end).method_1014(1.0), candidate -> candidate == crystal, 0.0F);
      if (entityHit != null) {
         return true;
      }

      class_3965 blockHit = level.method_17742(new class_3959(eye, end, class_3960.field_17559, class_242.field_1348, player));
      return blockHit.method_17783() == class_240.field_1332 && blockHit.method_17777().equals(crystal.method_24515().method_10074());
   }

   private static boolean hasValuablesNearby(class_638 level, class_2338 pos) {
      class_238 box = new class_238(
         pos.method_10263() - 3.0,
         pos.method_10264() - 3.0,
         pos.method_10260() - 3.0,
         pos.method_10263() + 4.0,
         pos.method_10264() + 5.0,
         pos.method_10260() + 4.0
      );

      for (class_1542 item : level.method_18467(class_1542.class, box)) {
         if (VALUABLES.contains(item.method_6983().method_7909())) {
            return true;
         }
      }

      return false;
   }
}
