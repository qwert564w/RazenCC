package org.ryzen.event.events.lifecycle;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.event.Event;
import org.ryzen.feature.Feature;

@Environment(EnvType.CLIENT)
public final class FeatureToggleEvent extends Event {
   private final Feature feature;
   private final boolean enabled;

   public FeatureToggleEvent(Feature feature, boolean enabled) {
      this.feature = feature;
      this.enabled = enabled;
   }

   public boolean isDisabled() {
      return !this.enabled;
   }

   @Generated
   public Feature getFeature() {
      return this.feature;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }
}
