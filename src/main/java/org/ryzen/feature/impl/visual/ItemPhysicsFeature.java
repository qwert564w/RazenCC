package org.ryzen.feature.impl.visual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;

@Environment(EnvType.CLIENT)
public final class ItemPhysicsFeature extends Feature {
   public ItemPhysicsFeature() {
      super("ItemPhysics", "Dropped items lie on the ground", FeatureCategory.VISUAL, -1);
   }
}
