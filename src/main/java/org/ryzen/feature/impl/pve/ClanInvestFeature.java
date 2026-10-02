package org.ryzen.feature.impl.pve;

import java.util.OptionalLong;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.DisconnectEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.PveStateMachine;
import org.ryzen.pve.economy.CommandCooldown;
import org.ryzen.pve.economy.EconomyChat;
import org.ryzen.pve.economy.EconomyCommands;
import org.ryzen.pve.economy.EconomyTextParser;
import org.ryzen.pve.economy.ScoreboardBalance;
import org.ryzen.pve.server.ServerAdapter;
import org.ryzen.pve.server.ServerAdapters;
import org.ryzen.pve.server.ServerProfile;

@Environment(EnvType.CLIENT)
public final class ClanInvestFeature extends PveFeature {
   private static final long CONFIRMATION_TIMEOUT_TICKS = 80L;
   private static final long COMMAND_COOLDOWN_TICKS = 100L;
   public final TextSetting currencyThreshold = this.register(new TextSetting("Currency Threshold", "1000000"));
   public final NumberSetting investPercentage = this.register(new NumberSetting("Invest Percentage", 30.0, 1.0, 100.0, 1.0, "%"));
   private final PveStateMachine<ClanInvestFeature.State> machine = new PveStateMachine<>(ClanInvestFeature.State.ARMED);
   private final CommandCooldown commandCooldown = new CommandCooldown();
   private long lastTick;
   private long pendingAmount;

   public ClanInvestFeature() {
      super("ClanInvest", "Invests a percentage of the displayed balance after a threshold", -1, AutomationPriority.FEATURE);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      if (client.field_1724 != null && client.field_1687 != null) {
         long tick = client.field_1687.method_75260();
         this.lastTick = tick;
         if (ServerAdapters.current().profile() == ServerProfile.FUNTIME) {
            if (this.machine.is(ClanInvestFeature.State.WAITING_CONFIRMATION) && this.machine.ticksInState(tick) >= 80L) {
               this.pendingAmount = 0L;
               this.machine.transition(ClanInvestFeature.State.ARMED, tick);
            }

            int threshold = this.positiveThreshold();
            if (threshold > 0) {
               OptionalLong balanceResult = ScoreboardBalance.read(client.field_1724);
               if (!balanceResult.isEmpty()) {
                  long balance = balanceResult.getAsLong();
                  if (balance < threshold) {
                     this.pendingAmount = 0L;
                     this.machine.transition(ClanInvestFeature.State.ARMED, tick);
                  } else if (this.machine.is(ClanInvestFeature.State.ARMED) && this.commandCooldown.ready(tick)) {
                     long boundedBalance = Math.min(2147483647L, balance);
                     long amount = Math.max(1L, Math.min(2147483647L, boundedBalance * Math.round(this.investPercentage.getValue()) / 100L));
                     EconomyCommands.clanInvest(amount).ifPresent(command -> {
                        if (!this.claim(AutomationResource.CHAT)) {
                           this.commandCooldown.defer(tick, 5L);
                        } else {
                           try {
                              ServerAdapter adapter = ServerAdapters.current();
                              adapter.sendCommand(client.field_1724, command);
                              this.pendingAmount = amount;
                              this.machine.transition(ClanInvestFeature.State.WAITING_CONFIRMATION, tick);
                              this.commandCooldown.tryAcquire(tick, 100L);
                           } finally {
                              PveAutomationCoordinator.INSTANCE.release(this);
                           }
                        }
                     });
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE) {
         String text = EconomyChat.incomingText(event.getPacket());
         if (text != null) {
            String normalized = EconomyTextParser.normalize(text);
            class_310.method_1551().execute(() -> this.handleChat(normalized));
         }
      }
   }

   @EventTarget
   public void onDisconnect(DisconnectEvent event) {
      this.resetRuntime();
   }

   @Override
   protected void onPveEnable() {
      this.resetRuntime();
   }

   @Override
   protected void onPveDisable() {
      this.resetRuntime();
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.resetRuntime();
   }

   private void handleChat(String text) {
      if (this.isEnabled()) {
         if (!EconomyTextParser.containsAny(text, "/clan create - создать клан", "you are not in a clan")
            && !EconomyTextParser.containsAny(text, "вы не можете пополнить баланс клана", "cannot deposit to the clan")) {
            if (this.machine.is(ClanInvestFeature.State.WAITING_CONFIRMATION)
               && EconomyTextParser.containsAny(text, "пополнил баланс казны", "clan treasury", "clan balance")) {
               this.pendingAmount = 0L;
               this.machine.transition(ClanInvestFeature.State.LATCHED, this.lastTick);
            }
         } else {
            this.setEnabled(false);
         }
      }
   }

   private int positiveThreshold() {
      String value = this.currencyThreshold.getValue();
      if (value != null && value.matches("\\d+")) {
         try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : -1;
         } catch (NumberFormatException ignored) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private void resetRuntime() {
      this.machine.reset(0L);
      this.commandCooldown.reset();
      this.pendingAmount = 0L;
      this.lastTick = 0L;
   }

   @Environment(EnvType.CLIENT)
   enum State {
      ARMED,
      WAITING_CONFIRMATION,
      LATCHED;
   }
}
