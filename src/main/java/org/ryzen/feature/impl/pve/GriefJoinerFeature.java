package org.ryzen.feature.impl.pve;

import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.DisconnectEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.PveStateMachine;
import org.ryzen.pve.economy.CommandCooldown;
import org.ryzen.pve.economy.EconomyChat;
import org.ryzen.pve.economy.EconomyInventory;
import org.ryzen.pve.economy.EconomyItemText;
import org.ryzen.pve.economy.EconomyMenus;
import org.ryzen.pve.economy.EconomyTextParser;
import org.ryzen.pve.economy.ServerUiText;
import org.ryzen.pve.server.ServerAdapters;
import org.ryzen.pve.server.ServerProfile;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class GriefJoinerFeature extends PveFeature {
   public static final String MODE_REALLYWORLD = "ReallyWorld";
   public static final String MODE_SPOOKYTIME = "SpookyTime";
   private static final long RETRY_TICKS = 400L;
   private static final long MENU_TIMEOUT_TICKS = 100L;
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "ReallyWorld", "ReallyWorld", "SpookyTime"));
   public final BooleanSetting mega = this.register(new BooleanSetting("Mega", false).visibleWhen(() -> this.mode.is("ReallyWorld")));
   public final TextSetting griefNumber = this.register(
      new TextSetting("Grief Number", "1", 3).visibleWhen(() -> this.mode.is("ReallyWorld") && !this.mega.getValue())
   );
   private final PveStateMachine<GriefJoinerFeature.State> machine = new PveStateMachine<>(GriefJoinerFeature.State.OPEN_SELECTOR);
   private final CommandCooldown actionCooldown = new CommandCooldown();
   private long lastTick;
   private long retryAtTick;
   private int ownedContainerId = -1;
   private boolean resourcesClaimed;

   public GriefJoinerFeature() {
      super("GriefJoiner", "Retries the selected grief server through its validated selector menus", -1, AutomationPriority.FEATURE);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null && client.field_1761 != null) {
         long tick = client.field_1687.method_75260();
         this.lastTick = tick;
         if (this.isSupported(client)) {
            if (this.hasJoined(client)) {
               this.setEnabled(false);
            } else if (this.ensureResources(client, tick)) {
               if (tick >= this.retryAtTick) {
                  switch ((GriefJoinerFeature.State)this.machine.state()) {
                     case OPEN_SELECTOR:
                        this.openSelector(client, player, tick);
                        break;
                     case SELECT_CATEGORY:
                     case SELECT_SERVER:
                        this.selectMenuEntry(client, tick);
                        break;
                     case WAIT_JOIN:
                        if (this.machine.ticksInState(tick) > 100L) {
                           this.closeOwned(client);
                           this.machine.transition(GriefJoinerFeature.State.OPEN_SELECTOR, tick);
                        }
                        break;
                     case RETRY_DELAY:
                        if (tick >= this.retryAtTick) {
                           this.machine.transition(GriefJoinerFeature.State.OPEN_SELECTOR, tick);
                        }
                  }
               }
            }
         } else {
            if (this.resourcesClaimed || this.ownedContainerId >= 0 || !this.machine.is(GriefJoinerFeature.State.OPEN_SELECTOR)) {
               this.resetRuntime(true);
            }
         }
      }
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE) {
         String text = EconomyChat.incomingText(event.getPacket());
         if (text != null
            && EconomyTextParser.containsAny(
               text,
               "к сожалению сервер переполнен",
               "подождите 20 секунд",
               "большой поток игроков",
               "imperator",
               "подождите несколько секунд",
               "server is full",
               "too many players"
            )) {
            class_310.method_1551().execute(() -> {
               if (this.isEnabled()) {
                  this.closeOwned(class_310.method_1551());
                  this.retryAtTick = this.lastTick + 400L;
                  this.machine.transition(GriefJoinerFeature.State.RETRY_DELAY, this.lastTick);
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
   }

   @Override
   protected void onPveDisable() {
      this.resetRuntime(true);
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.resetRuntime(true);
   }

   private void openSelector(class_310 client, class_746 player, long tick) {
      if (client.field_1755 == null && this.actionCooldown.ready(tick)) {
         int previous = player.method_31548().method_67532();
         int compassMenuSlot = InventoryUtil.findPlayerMenuSlot(player, stack -> stack.method_31574(class_1802.field_8251));
         boolean swapped = false;
         int compass;
         if (compassMenuSlot >= 36 && compassMenuSlot <= 44) {
            compass = compassMenuSlot - 36;
         } else if (compassMenuSlot >= 9
            && compassMenuSlot < 36
            && player.field_7512 == player.field_7498
            && InventoryUtil.swapWithHotbar(compassMenuSlot, previous)) {
            compass = previous;
            swapped = true;
         } else {
            compass = -1;
         }

         if (compass >= 0 && EconomyInventory.selectHotbar(player, compass)) {
            client.field_1761.method_2919(player, class_1268.field_5808);
            player.method_6104(class_1268.field_5808);
            if (swapped) {
               InventoryUtil.swapWithHotbar(compassMenuSlot, previous);
            }

            EconomyInventory.selectHotbar(player, previous);
            this.actionCooldown.tryAcquire(tick, 40L);
            this.machine.transition(GriefJoinerFeature.State.SELECT_CATEGORY, tick);
         } else {
            this.actionCooldown.defer(tick, 40L);
         }
      }
   }

   private void selectMenuEntry(class_310 client, long tick) {
      if (client.field_1724.field_7512 == client.field_1724.field_7498) {
         if (this.machine.ticksInState(tick) > 100L) {
            this.machine.transition(GriefJoinerFeature.State.OPEN_SELECTOR, tick);
         }
      } else if (this.actionCooldown.ready(tick)) {
         class_1703 menu = client.field_1724.field_7512;
         int specific = EconomyMenus.findContainerSlot(menu, stack -> this.isSpecificTarget(stack));
         if (specific >= 0) {
            this.ownedContainerId = menu.field_7763;
            if (EconomyMenus.click(client, menu, specific, 0, class_1713.field_7790)) {
               this.actionCooldown.tryAcquire(tick, 10L);
               this.machine.transition(GriefJoinerFeature.State.WAIT_JOIN, tick);
            }
         } else {
            if (this.machine.is(GriefJoinerFeature.State.SELECT_CATEGORY)) {
               int category = EconomyMenus.findContainerSlot(
                  menu, stack -> EconomyItemText.containsAny(stack, "гриферское выживание", "grief survival", "spookytime", "reallyworld")
               );
               if (category >= 0) {
                  this.ownedContainerId = menu.field_7763;
                  if (EconomyMenus.click(client, menu, category, 0, class_1713.field_7790)) {
                     this.actionCooldown.tryAcquire(tick, 10L);
                     this.machine.transition(GriefJoinerFeature.State.SELECT_SERVER, tick);
                  }

                  return;
               }
            }

            if (this.machine.ticksInState(tick) > 100L) {
               this.closeOwned(client);
               this.machine.transition(GriefJoinerFeature.State.OPEN_SELECTOR, tick);
            }
         }
      }
   }

   private boolean isSpecificTarget(class_1799 stack) {
      String text = EconomyTextParser.normalize(EconomyItemText.combined(stack));
      if (this.mode.is("SpookyTime")) {
         return EconomyTextParser.containsAny(text, "гриф", "grief") && !EconomyTextParser.containsAny(text, "хаб", "hub", "лобби", "lobby");
      }

      if (this.mega.getValue()) {
         return EconomyTextParser.containsAny(text, "мега", "mega") && EconomyTextParser.containsAny(text, "гриф", "grief", "выживание", "survival");
      }

      int number = EconomyTextParser.positiveInt(this.griefNumber.getValue(), 1, 999);
      Pattern exactNumber = Pattern.compile("(?<!\\d)" + number + "(?!\\d)");
      return exactNumber.matcher(text).find() && EconomyTextParser.containsAny(text, "гриф", "grief", "сервер", "server");
   }

   private boolean hasJoined(class_310 client) {
      String header = ServerUiText.tabHeader(client);
      return !this.mode.is("SpookyTime")
         ? EconomyTextParser.containsAny(header, "гриферское выживание", "grief survival")
         : header.contains("spookytime") && !EconomyTextParser.containsAny(header, "хаб", "hub", "лобби", "lobby");
   }

   private boolean isSupported(class_310 client) {
      if (this.mode.is("ReallyWorld")) {
         return ServerAdapters.current().profile() == ServerProfile.REALLYWORLD;
      }

      String host = ServerUiText.serverHost(client);
      return host.equals("spookytime.net") || host.endsWith(".spookytime.net");
   }

   private boolean ensureResources(class_310 client, long tick) {
      if (this.resourcesClaimed) {
         return true;
      }

      if (client.field_1755 == null && client.field_1724 != null && client.field_1724.field_7512 == client.field_1724.field_7498) {
         if (!this.claim(AutomationResource.INVENTORY, AutomationResource.SCREEN)) {
            this.actionCooldown.defer(tick, 5L);
            return false;
         } else {
            this.resourcesClaimed = true;
            return true;
         }
      } else {
         this.actionCooldown.defer(tick, 5L);
         return false;
      }
   }

   private void closeOwned(class_310 client) {
      if (EconomyMenus.currentContainerId(client) == this.ownedContainerId) {
         EconomyMenus.closeOwned(client, this.ownedContainerId);
      }

      this.ownedContainerId = -1;
   }

   private void resetRuntime(boolean closeScreen) {
      if (closeScreen) {
         this.closeOwned(class_310.method_1551());
      }

      this.machine.reset(0L);
      this.actionCooldown.reset();
      this.retryAtTick = 0L;
      this.ownedContainerId = -1;
      if (this.resourcesClaimed) {
         this.resourcesClaimed = false;
         PveAutomationCoordinator.INSTANCE.release(this);
      }

      this.lastTick = 0L;
   }

   @Environment(EnvType.CLIENT)
   enum State {
      OPEN_SELECTOR,
      SELECT_CATEGORY,
      SELECT_SERVER,
      WAIT_JOIN,
      RETRY_DELAY;
   }
}
