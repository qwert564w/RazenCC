package org.ryzen.pve;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureEnableRejectedException;

@Environment(EnvType.CLIENT)
public abstract class PveFeature extends Feature implements AutomationOwner {
   private final AutomationPriority priority;
   private final EnumSet<AutomationResource> baseResources;

   protected PveFeature(String name, String description, int bind, AutomationPriority priority, AutomationResource... resources) {
      super(name, description, FeatureCategory.PVE, bind);
      this.priority = priority;
      this.baseResources = resources.length == 0 ? EnumSet.noneOf(AutomationResource.class) : EnumSet.copyOf(Arrays.asList(resources));
   }

   @Override
   protected final void onEnable() {
      this.validatePveEnable();
      if (!PveAutomationCoordinator.INSTANCE.acquire(this, this.priority, this.baseResources, true)) {
         String owner = this.busyOwner();
         throw new FeatureEnableRejectedException(owner == null ? "automation resources are temporarily busy" : "resource is held by " + owner);
      }

      try {
         this.onPveEnable();
      } catch (RuntimeException | Error throwable) {
         PveAutomationCoordinator.INSTANCE.release(this);
         throw throwable;
      }
   }

   @Override
   protected final void onDisable() {
      try {
         this.onPveDisable();
      } finally {
         PveAutomationCoordinator.INSTANCE.release(this);
      }
   }

   protected final boolean claim(AutomationResource first, AutomationResource... rest) {
      return PveAutomationCoordinator.INSTANCE.acquire(this, this.priority, first, rest);
   }

   protected final boolean owns(AutomationResource resource) {
      return PveAutomationCoordinator.INSTANCE.owns(this, resource);
   }

   protected final void release(AutomationResource first, AutomationResource... rest) {
      PveAutomationCoordinator.INSTANCE.release(this, EnumSet.of(first, rest));
   }

   protected final Set<AutomationResource> baseResources() {
      return Set.copyOf(this.baseResources);
   }

   protected void onPveEnable() {
   }

   protected void validatePveEnable() {
   }

   protected void onPveDisable() {
   }

   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
   }

   protected boolean disableAfterRevocation(PveAutomationCoordinator.RevocationReason reason) {
      return true;
   }

   private String busyOwner() {
      for (AutomationResource resource : this.baseResources) {
         String owner = PveAutomationCoordinator.INSTANCE.ownerId(resource);
         if (owner != null && !owner.equals(this.automationId())) {
            return owner;
         }
      }

      return null;
   }

   @Override
   public final void onAutomationRevoked(PveAutomationCoordinator.RevocationReason reason) {
      class_310 client = class_310.method_1551();
      Runnable revoke = () -> {
         this.onPvePreempted(reason);
         if (this.disableAfterRevocation(reason) && this.isEnabled()) {
            this.setEnabled(false);
         }
      };
      if (client.method_18854()) {
         revoke.run();
      } else {
         client.execute(revoke);
      }
   }
}
