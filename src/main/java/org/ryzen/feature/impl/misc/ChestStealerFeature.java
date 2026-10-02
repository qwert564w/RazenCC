package org.ryzen.feature.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1703;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.utils.inventory.ContainerLootService;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class ChestStealerFeature extends Feature {
   public final NumberSetting delay = this.register(new NumberSetting("Delay", 80.0, 0.0, 1000.0, 10.0, "ms"));
   private long lastMoveAt;

   public ChestStealerFeature() {
      super("ChestStealer", "Automatically takes items from containers", FeatureCategory.MISC, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (!PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.INVENTORY) && InventoryUtil.isContainerScreenOpen()) {
         class_1703 menu = InventoryUtil.getOpenMenu();
         if (menu != null && menu != event.getClient().field_1724.field_7498) {
            long now = System.currentTimeMillis();
            if (now - this.lastMoveAt >= this.delay.getValue().longValue()) {
               if (ContainerLootService.quickMoveFirst(menu, stack -> !stack.method_7960())) {
                  this.lastMoveAt = now;
               }
            }
         }
      }
   }
}
