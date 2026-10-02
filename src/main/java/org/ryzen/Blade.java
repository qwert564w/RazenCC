package org.ryzen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import org.ryzen.command.CommandManager;
import org.ryzen.event.EventManager;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.ClientStartEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.misc.autobuy.AutoBuyAuctionOverlay;
import org.ryzen.hud.NotificationsElement;
import org.ryzen.menu.MenuKeyHandler;
import org.ryzen.menu.MenuOverlayRenderHandler;
import org.ryzen.menu.i18n.UiLanguage;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PvpStateTracker;
import org.ryzen.utils.AccountSwitcher;
import org.ryzen.utils.FriendManager;
import org.ryzen.utils.ScreenNbtParser;
import org.ryzen.utils.StaffManager;
import org.ryzen.utils.combat.ServerSprintTracker;
import org.ryzen.utils.combat.ServerTickSync;
import org.ryzen.utils.cosmetics.FiguraBridge;
import org.ryzen.utils.inventory.DropAllInventoryController;
import org.ryzen.utils.inventory.InventorySwap;
import org.ryzen.utils.irc.IrcService;
import org.ryzen.utils.render.EntityEspStateCache;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.gui.GuiTexture;
import org.ryzen.utils.render.world.WorldEffects;

@Environment(EnvType.CLIENT)
public class Blade implements ModInitializer {
   private final MenuKeyHandler menuKeyHandler = new MenuKeyHandler();
   private final MenuOverlayRenderHandler menuOverlayRenderHandler = new MenuOverlayRenderHandler();

   public void onInitialize() {
      UiLanguage.loadSaved();
      FriendManager.INSTANCE.initialize();
      StaffManager.INSTANCE.initialize();
      FeatureManager.INSTANCE.initialize();
      CommandManager.INSTANCE.initialize();
      WorldEffects.bootstrap();
      EventManager.subscribe(FeatureManager.INSTANCE);
      EventManager.subscribe(CommandManager.INSTANCE);
      EventManager.subscribe(FriendManager.INSTANCE);
      EventManager.subscribe(new NotificationsElement.ToggleListener());
      EventManager.subscribe(this.menuKeyHandler);
      EventManager.subscribe(AutoBuyAuctionOverlay.INSTANCE);
      EventManager.subscribe(this.menuOverlayRenderHandler);
      EventManager.subscribe(InventorySwap.INSTANCE);
      EventManager.subscribe(DropAllInventoryController.INSTANCE);
      EventManager.subscribe(ServerTickSync.INSTANCE);
      EventManager.subscribe(ServerSprintTracker.INSTANCE);
      EventManager.subscribe(PveAutomationCoordinator.INSTANCE);
      EventManager.subscribe(PvpStateTracker.INSTANCE);
      EventManager.subscribe(ScreenNbtParser.INSTANCE);
      EventManager.subscribe(this);
   }

   @EventTarget
   public void onClientStart(ClientStartEvent event) {
      AccountSwitcher.applyStartupAccount();
      FiguraBridge.disablePopupMenu();
      GuiTexture.prewarm(Textures.all());
      IrcService.INSTANCE.initialize();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      EntityEspStateCache.clear();
   }
}
