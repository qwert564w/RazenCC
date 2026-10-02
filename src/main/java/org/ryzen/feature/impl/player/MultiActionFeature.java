package org.ryzen.feature.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;

@Environment(EnvType.CLIENT)
public final class MultiActionFeature extends Feature {
   public MultiActionFeature() {
      super("MultiAction", "Attack and mine while using items", FeatureCategory.PLAYER, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null && client.field_1761 != null) {
         if (player.method_6115() && client.field_1690.field_1886.method_1434()) {
            class_239 hit = client.field_1765;
            if (hit instanceof class_3966 entityHit) {
               if (player.method_7261(0.0F) >= 1.0F) {
                  client.field_1761.method_2918(player, entityHit.method_17782());
                  player.method_6104(class_1268.field_5808);
               }
            } else {
               if (hit instanceof class_3965 blockHit && hit.method_17783() == class_240.field_1332) {
                  client.field_1761.method_2902(blockHit.method_17777(), blockHit.method_17780());
               }

               player.method_6104(class_1268.field_5808);
            }
         }
      }
   }
}
