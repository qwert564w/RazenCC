package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.AttackEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.utils.FriendManager;

@Environment(EnvType.CLIENT)
public final class NoFriendDamageFeature extends Feature {
   public NoFriendDamageFeature() {
      super("NoFriendDamage", "Blocks manual hits on players in your friend list", FeatureCategory.COMBAT, -1);
   }

   @EventTarget
   public void onAttack(AttackEvent event) {
      class_1297 target = event.getClient().field_1692;
      if (target instanceof class_1657 player) {
         if (FriendManager.INSTANCE.isFriend(player.method_7334().name())) {
            AuraFeature aura = FeatureManager.INSTANCE.getEnabled(AuraFeature.class);
            if (aura == null || aura.getCurrentTarget() != target) {
               event.cancel();
            }
         }
      }
   }
}
