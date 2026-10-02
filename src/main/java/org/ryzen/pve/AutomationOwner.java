package org.ryzen.pve;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface AutomationOwner {
   default String automationId() {
      return this.getClass().getSimpleName();
   }

   default void onAutomationRevoked(PveAutomationCoordinator.RevocationReason reason) {
   }
}
