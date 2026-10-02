package org.ryzen.feature.impl.visual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_3283;
import org.ryzen.event.EventManager;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.ClientStartEvent;
import org.ryzen.event.events.lifecycle.ResourceReloadEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.menu.core.MenuOverlay;

@Environment(EnvType.CLIENT)
public final class HoldMyItemsFeature extends Feature {
   private static final String RESOURCE_PACK_ID = "holdmyitems:pack_test";
   private static final HoldMyItemsFeature.ReloadCompletionListener RELOAD_COMPLETION = new HoldMyItemsFeature.ReloadCompletionListener();

   public HoldMyItemsFeature() {
      super("HoldMyItems", "Animated hands and held items in first person", FeatureCategory.VISUAL, -1);
   }

   public static boolean isActive() {
      return FeatureManager.INSTANCE.getEnabled(HoldMyItemsFeature.class) != null;
   }

   @Override
   protected void onEnable() {
      syncResourcePack(true);
   }

   @Override
   protected void onDisable() {
      syncResourcePack(false);
   }

   @EventTarget
   public void onClientStart(ClientStartEvent event) {
      syncResourcePack(true);
   }

   private static void syncResourcePack(boolean enabled) {
      class_310 client = class_310.method_1551();
      if (client != null) {
         client.execute(
            () -> {
               class_3283 repository = client.method_1520();
               repository.method_14445();
               boolean selected = repository.method_29210().contains("holdmyitems:pack_test");
               boolean changed = enabled
                  ? !selected && repository.method_49427("holdmyitems:pack_test")
                  : selected && repository.method_49428("holdmyitems:pack_test");
               if (changed) {
                  boolean restoreMenu = MenuOverlay.suspendForReload(client);
                  if (restoreMenu) {
                     RELOAD_COMPLETION.arm();
                  }

                  try {
                     client.field_1690.method_49598(repository);
                  } catch (RuntimeException exception) {
                     if (restoreMenu) {
                        RELOAD_COMPLETION.complete(client);
                     }

                     throw exception;
                  }
               }
            }
         );
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class ReloadCompletionListener {
      private boolean armed;

      private void arm() {
         if (!this.armed) {
            this.armed = true;
            EventManager.subscribe(this);
         }
      }

      private void complete(class_310 client) {
         if (this.armed) {
            this.armed = false;
            EventManager.unsubscribe(this);
            MenuOverlay.resumeAfterReload(client);
         }
      }

      @EventTarget
      public void onResourceReload(ResourceReloadEvent event) {
         this.complete(event.getClient());
      }
   }
}
