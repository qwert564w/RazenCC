package org.ryzen.feature.impl.pve;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_310;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.DisconnectEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.PveStateMachine;
import org.ryzen.pve.economy.CommandCooldown;
import org.ryzen.pve.economy.EconomyChat;
import org.ryzen.pve.economy.EconomyCommands;
import org.ryzen.pve.economy.EconomyItemText;
import org.ryzen.pve.economy.EconomyMenus;
import org.ryzen.pve.economy.EconomyTextParser;
import org.ryzen.pve.server.ServerAdapter;
import org.ryzen.pve.server.ServerAdapters;
import org.ryzen.pve.server.ServerProfile;

@Environment(EnvType.CLIENT)
public final class AuctionRelistFeature extends PveFeature {
   private static final long CYCLE_TICKS = 1200L;
   private static final long MENU_TIMEOUT_TICKS = 80L;
   private final PveStateMachine<AuctionRelistFeature.State> machine = new PveStateMachine<>(AuctionRelistFeature.State.WAIT);
   private final CommandCooldown commandCooldown = new CommandCooldown();
   private long lastTick;
   private long nextCycleTick;
   private int ownedContainerId = -1;
   private boolean resourcesClaimed;

   public AuctionRelistFeature() {
      super("AuctionRelist", "Periodically relists expired auction items", -1, AutomationPriority.FEATURE);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      if (client.field_1724 != null && client.field_1687 != null) {
         long tick = client.field_1687.method_75260();
         this.lastTick = tick;
         if (ServerAdapters.current().profile() == ServerProfile.FUNTIME) {
            switch ((AuctionRelistFeature.State)this.machine.state()) {
               case WAIT:
                  if (tick >= this.nextCycleTick && !isPlayerMoving(client)) {
                     this.beginCycle(tick);
                  }
                  break;
               case OPEN_AUCTION:
                  this.openAuction(client, tick);
                  break;
               case WAIT_AUCTION_MENU:
                  this.waitForAuctionMenu(client, tick);
                  break;
               case WAIT_STORAGE_MENU:
                  this.waitForStorageMenu(client, tick);
                  break;
               case CLOSING:
                  if (this.machine.ticksInState(tick) >= 6L) {
                     this.finishCycle(client, tick);
                  }
            }
         } else {
            if (this.resourcesClaimed || !this.machine.is(AuctionRelistFeature.State.WAIT)) {
               this.finishCycle(client, tick);
            }
         }
      }
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE) {
         String text = EconomyChat.incomingText(event.getPacket());
         if (text != null && EconomyTextParser.containsAny(text, "аукцион недоступен", "auction is unavailable", "не удалось открыть аукцион", "слишком часто")
            )
          {
            class_310.method_1551().execute(() -> {
               if (this.isEnabled()) {
                  this.finishCycle(class_310.method_1551(), this.lastTick);
               }
            });
         }
      }
   }

   @EventTarget
   public void onDisconnect(DisconnectEvent event) {
      this.resetRuntime(false);
   }

   @Override
   protected void onPveEnable() {
      this.resetRuntime(false);
      this.nextCycleTick = this.lastTick + 1200L;
   }

   @Override
   protected void onPveDisable() {
      this.resetRuntime(true);
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.resetRuntime(true);
   }

   private void beginCycle(long tick) {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null && client.field_1755 == null && client.field_1724.field_7512 == client.field_1724.field_7498) {
         if (!this.claim(AutomationResource.INVENTORY, AutomationResource.SCREEN, AutomationResource.CHAT)) {
            this.nextCycleTick = tick + 10L;
         } else {
            this.resourcesClaimed = true;
            this.machine.transition(AuctionRelistFeature.State.OPEN_AUCTION, tick);
         }
      } else {
         this.nextCycleTick = tick + 20L;
      }
   }

   private void openAuction(class_310 client, long tick) {
      if (this.commandCooldown.ready(tick)) {
         ServerAdapter adapter = ServerAdapters.current();
         adapter.auctionCommand().flatMap(EconomyCommands::auctionRoot).ifPresentOrElse(command -> {
            adapter.sendCommand(client.field_1724, command);
            this.commandCooldown.tryAcquire(tick, 40L);
            this.machine.transition(AuctionRelistFeature.State.WAIT_AUCTION_MENU, tick);
         }, () -> this.finishCycle(client, tick));
      }
   }

   private void waitForAuctionMenu(class_310 client, long tick) {
      if (this.machine.ticksInState(tick) > 80L) {
         this.finishCycle(client, tick);
      } else if (EconomyMenus.titleContains(client, "аукцион", "auction")) {
         class_1703 menu = client.field_1724.field_7512;
         int storage = EconomyMenus.findContainerSlot(
            menu, stack -> EconomyItemText.containsAny(stack, "хранилище", "storage", "истекшие", "expired", "снятые товары")
         );
         if (storage >= 0) {
            this.ownedContainerId = menu.field_7763;
            if (EconomyMenus.click(client, menu, storage, 0, class_1713.field_7790)) {
               this.machine.transition(AuctionRelistFeature.State.WAIT_STORAGE_MENU, tick);
            }
         }
      }
   }

   private void waitForStorageMenu(class_310 client, long tick) {
      if (this.machine.ticksInState(tick) > 80L) {
         this.finishCycle(client, tick);
      } else if (EconomyMenus.titleContains(client, "хранилище", "storage")) {
         class_1703 menu = client.field_1724.field_7512;
         int relist = EconomyMenus.findContainerSlot(
            menu, stack -> EconomyItemText.containsAny(stack, "перевыставить", "перевыстав", "выставить снова", "relist")
         );
         if (relist < 0) {
            if (this.machine.ticksInState(tick) >= 10L) {
               this.ownedContainerId = menu.field_7763;
               this.machine.transition(AuctionRelistFeature.State.CLOSING, tick);
            }
         } else {
            this.ownedContainerId = menu.field_7763;
            if (EconomyMenus.click(client, menu, relist, 0, class_1713.field_7790)) {
               this.machine.transition(AuctionRelistFeature.State.CLOSING, tick);
            }
         }
      }
   }

   private void finishCycle(class_310 client, long tick) {
      if (this.isRecognizedMenu(client)) {
         EconomyMenus.closeOwned(client, this.ownedContainerId);
      }

      this.ownedContainerId = -1;
      this.nextCycleTick = tick + 1200L;
      this.machine.transition(AuctionRelistFeature.State.WAIT, tick);
      if (this.resourcesClaimed) {
         this.resourcesClaimed = false;
         PveAutomationCoordinator.INSTANCE.release(this);
      }
   }

   private boolean isRecognizedMenu(class_310 client) {
      return EconomyMenus.currentContainerId(client) == this.ownedContainerId
         && EconomyMenus.titleContains(client, "аукцион", "auction", "хранилище", "storage");
   }

   private static boolean isPlayerMoving(class_310 client) {
      return client.field_1724 != null && client.field_1724.method_18798().method_37268() > 0.0025;
   }

   private void resetRuntime(boolean closeScreen) {
      class_310 client = class_310.method_1551();
      if (closeScreen && this.isRecognizedMenu(client)) {
         EconomyMenus.closeOwned(client, this.ownedContainerId);
      }

      this.machine.reset(0L);
      this.commandCooldown.reset();
      this.ownedContainerId = -1;
      if (this.resourcesClaimed) {
         this.resourcesClaimed = false;
         PveAutomationCoordinator.INSTANCE.release(this);
      }

      this.nextCycleTick = 1200L;
      this.lastTick = 0L;
   }

   @Environment(EnvType.CLIENT)
   enum State {
      WAIT,
      OPEN_AUCTION,
      WAIT_AUCTION_MENU,
      WAIT_STORAGE_MENU,
      CLOSING;
   }
}
