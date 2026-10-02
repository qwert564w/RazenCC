package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;

@Environment(EnvType.CLIENT)
public final class CombatExtraFeature extends Feature {
   public CombatExtraFeature() {
      super("CombatExtra", "Extra combat options", FeatureCategory.COMBAT, -1);
   }
}
