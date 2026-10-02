package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;

@Environment(EnvType.CLIENT)
public final class CombatMiscFeature extends Feature {
   public CombatMiscFeature() {
      super("CombatMisc", "Miscellaneous combat tweaks", FeatureCategory.COMBAT, -1);
   }
}
