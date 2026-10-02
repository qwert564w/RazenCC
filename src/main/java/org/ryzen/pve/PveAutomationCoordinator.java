package org.ryzen.pve;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.DisconnectEvent;
import org.ryzen.event.events.lifecycle.ShutdownEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;

@Environment(EnvType.CLIENT)
public final class PveAutomationCoordinator {
   public static final PveAutomationCoordinator INSTANCE = new PveAutomationCoordinator();
   private final EnumMap<AutomationResource, PveAutomationCoordinator.Claim> claims = new EnumMap<>(AutomationResource.class);
   private final IdentityHashMap<AutomationOwner, EnumSet<AutomationResource>> owned = new IdentityHashMap<>();

   private PveAutomationCoordinator() {
   }

   public boolean acquire(AutomationOwner owner, AutomationPriority priority, Set<AutomationResource> resources) {
      return this.acquire(owner, priority, resources, false);
   }

   public boolean acquire(AutomationOwner owner, AutomationPriority priority, Set<AutomationResource> resources, boolean preemptEqualPriority) {
      Objects.requireNonNull(owner, "owner");
      Objects.requireNonNull(priority, "priority");
      Objects.requireNonNull(resources, "resources");
      if (resources.isEmpty()) {
         return true;
      }

      Set<AutomationOwner> displaced = Collections.newSetFromMap(new IdentityHashMap<>());
      synchronized (this) {
         Iterator var7 = resources.iterator();

         label53:
         while (true) {
            if (var7.hasNext()) {
               AutomationResource resource = (AutomationResource)var7.next();
               PveAutomationCoordinator.Claim existing = this.claims.get(resource);
               if (existing == null
                  || existing.owner == owner
                  || existing.priority.weight() <= priority.weight() && (existing.priority.weight() != priority.weight() || preemptEqualPriority)) {
                  continue;
               }

               return false;
            }

            var7 = resources.iterator();

            while (true) {
               if (!var7.hasNext()) {
                  break label53;
               }

               AutomationResource resource = (AutomationResource)var7.next();
               PveAutomationCoordinator.Claim existing = this.claims.put(resource, new PveAutomationCoordinator.Claim(owner, priority));
               if (existing != null && existing.owner != owner) {
                  this.removeOwned(existing.owner, resource);
                  displaced.add(existing.owner);
               }

               this.owned.computeIfAbsent(owner, ignored -> EnumSet.noneOf(AutomationResource.class)).add(resource);
            }
         }
      }

      displaced.forEach(displacedOwner -> displacedOwner.onAutomationRevoked(PveAutomationCoordinator.RevocationReason.PREEMPTED));
      return true;
   }

   public boolean acquire(AutomationOwner owner, AutomationPriority priority, AutomationResource first, AutomationResource... rest) {
      EnumSet<AutomationResource> resources = EnumSet.of(first, rest);
      return this.acquire(owner, priority, resources);
   }

   public synchronized void release(AutomationOwner owner) {
      EnumSet<AutomationResource> resources = this.owned.remove(owner);
      if (resources != null) {
         for (AutomationResource resource : resources) {
            PveAutomationCoordinator.Claim claim = this.claims.get(resource);
            if (claim != null && claim.owner == owner) {
               this.claims.remove(resource);
            }
         }
      }
   }

   public synchronized void release(AutomationOwner owner, Set<AutomationResource> resources) {
      EnumSet<AutomationResource> ownedResources = this.owned.get(owner);
      if (ownedResources != null && !resources.isEmpty()) {
         for (AutomationResource resource : resources) {
            PveAutomationCoordinator.Claim claim = this.claims.get(resource);
            if (claim != null && claim.owner == owner) {
               this.claims.remove(resource);
               ownedResources.remove(resource);
            }
         }

         if (ownedResources.isEmpty()) {
            this.owned.remove(owner);
         }
      }
   }

   public synchronized boolean owns(AutomationOwner owner, AutomationResource resource) {
      PveAutomationCoordinator.Claim claim = this.claims.get(resource);
      return claim != null && claim.owner == owner;
   }

   public synchronized boolean isClaimed(AutomationResource resource) {
      return this.claims.containsKey(resource);
   }

   public synchronized boolean isClaimedByOther(AutomationOwner owner, AutomationResource resource) {
      PveAutomationCoordinator.Claim claim = this.claims.get(resource);
      return claim != null && claim.owner != owner;
   }

   public synchronized String ownerId(AutomationResource resource) {
      PveAutomationCoordinator.Claim claim = this.claims.get(resource);
      return claim == null ? null : claim.owner.automationId();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.revokeAll(PveAutomationCoordinator.RevocationReason.WORLD_CHANGE);
   }

   @EventTarget
   public void onDisconnect(DisconnectEvent event) {
      this.revokeAll(PveAutomationCoordinator.RevocationReason.DISCONNECT);
   }

   @EventTarget
   public void onShutdown(ShutdownEvent event) {
      this.revokeAll(PveAutomationCoordinator.RevocationReason.SHUTDOWN);
   }

   public void revokeAll(PveAutomationCoordinator.RevocationReason reason) {
      Set<AutomationOwner> owners = Collections.newSetFromMap(new IdentityHashMap<>());
      synchronized (this) {
         owners.addAll(this.owned.keySet());
         this.claims.clear();
         this.owned.clear();
      }

      owners.forEach(owner -> owner.onAutomationRevoked(reason));
   }

   private void removeOwned(AutomationOwner owner, AutomationResource resource) {
      EnumSet<AutomationResource> resources = this.owned.get(owner);
      if (resources != null) {
         resources.remove(resource);
         if (resources.isEmpty()) {
            this.owned.remove(owner);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private record Claim(AutomationOwner owner, AutomationPriority priority) {
   }

   @Environment(EnvType.CLIENT)
   public enum RevocationReason {
      PREEMPTED,
      WORLD_CHANGE,
      DISCONNECT,
      SHUTDOWN;
   }
}
