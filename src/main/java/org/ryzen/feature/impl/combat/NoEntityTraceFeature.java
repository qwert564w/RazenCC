package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;

@Environment(EnvType.CLIENT)
public final class NoEntityTraceFeature extends Feature {
   public NoEntityTraceFeature() {
      super("NoEntityTrace", "The crosshair ignores entities and only picks blocks", FeatureCategory.COMBAT, -1);
   }

   public static boolean shouldSkipEntities() {
      return FeatureManager.INSTANCE.getEnabled(NoEntityTraceFeature.class) != null;
   }
}
