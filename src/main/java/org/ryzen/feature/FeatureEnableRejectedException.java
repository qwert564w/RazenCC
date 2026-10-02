package org.ryzen.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class FeatureEnableRejectedException extends RuntimeException {
   public FeatureEnableRejectedException(String message) {
      super(message, null, false, false);
   }
}
