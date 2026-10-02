package org.ryzen.feature.impl.pve;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.DisconnectEvent;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.economy.CommandCooldown;
import org.ryzen.pve.economy.EconomyCommands;
import org.ryzen.pve.economy.EconomyTextParser;
import org.ryzen.pve.economy.LocalPaymentLedger;
import org.ryzen.pve.economy.LocalSyncProtocol;
import org.ryzen.pve.economy.LocalSyncTokenStore;
import org.ryzen.pve.economy.LocalSyncTransport;
import org.ryzen.pve.economy.ScoreboardBalance;
import org.ryzen.pve.server.ServerAdapter;
import org.ryzen.pve.server.ServerAdapters;
import org.ryzen.pve.server.ServerProfile;

@Environment(EnvType.CLIENT)
public final class SynchronizationFeature extends PveFeature {
   public static final String MODE_OFF = "Off";
   public static final String MODE_REQUEST_ONLY = "Request Only";
   public static final String MODE_SEND_ONLY = "Send Only";
   public static final String MODE_REQUEST_AND_SEND = "Request & Send";
   private static final long HEARTBEAT_TICKS = 40L;
   private static final long PAYMENT_COOLDOWN_TICKS = 100L;
   private static final long REQUEST_COOLDOWN_TICKS = 200L;
   private static final long REQUEST_EXPIRY_TICKS = 300L;
   private static volatile SynchronizationFeature active;
   public final ModeSetting moneyMode = this.register(new ModeSetting("Money Mode", "Request & Send", "Off", "Request Only", "Send Only", "Request & Send"));
   private final ConcurrentLinkedQueue<LocalSyncProtocol.MoneyRequest> received = new ConcurrentLinkedQueue<>();
   private final ArrayDeque<SynchronizationFeature.QueuedRequest> pending = new ArrayDeque<>();
   private final CommandCooldown paymentCooldown = new CommandCooldown();
   private final CommandCooldown requestCooldown = new CommandCooldown();
   private UUID instanceId;
   private LocalSyncTransport transport;
   private LocalPaymentLedger paymentLedger;
   private Path configDirectory;
   private long lastTick;
   private long nextHeartbeatTick;
   private boolean busyPaying;

   public SynchronizationFeature() {
      super("Synchronization", "Coordinates authenticated money requests between local game instances", -1, AutomationPriority.FEATURE);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null) {
         long tick = client.field_1687.method_75260();
         this.lastTick = tick;
         if (isSupportedProfile(ServerAdapters.current().profile()) && !this.moneyMode.is("Off")) {
            if (this.ensureTransport(client)) {
               this.drainReceived(tick);
               if (!this.moneyMode.is("Off") && tick >= this.nextHeartbeatTick) {
                  this.publishStatus(player);
                  this.nextHeartbeatTick = tick + 40L;
               }

               this.processPayment(client, player, tick);
            }
         } else {
            if (this.transport != null) {
               this.stopTransport();
               this.clearRuntime();
            }
         }
      }
   }

   @EventTarget
   public void onDisconnect(DisconnectEvent event) {
      this.stopTransport();
      this.clearRuntime();
   }

   @Override
   protected void onPveEnable() {
      this.clearRuntime();
      active = this;
      this.ensureTransport(class_310.method_1551());
   }

   @Override
   protected void onPveDisable() {
      if (active == this) {
         active = null;
      }

      this.stopTransport();
      this.clearRuntime();
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      if (active == this) {
         active = null;
      }

      this.stopTransport();
      this.clearRuntime();
   }

   public static boolean requestMoney(int amount) {
      class_310 client = class_310.method_1551();
      String recipient = client.field_1724 == null ? "" : client.field_1724.method_7334().name();
      return requestMoney(recipient, amount, "");
   }

   public static boolean requestMoney(String recipient, int amount, String reason) {
      SynchronizationFeature feature = active;
      return feature != null && feature.sendMoneyRequest(recipient, amount);
   }

   public static boolean isActive() {
      SynchronizationFeature feature = active;
      return feature != null && feature.isEnabled() && !feature.moneyMode.is("Off") && isSupportedProfile(ServerAdapters.current().profile());
   }

   public int peersOnline() {
      LocalSyncTransport current = this.transport;
      return current != null && current.isRunning() ? current.peers(ServerAdapters.current().profile().name()).size() : 0;
   }

   public boolean canRequest() {
      return this.moneyMode.is("Request Only") || this.moneyMode.is("Request & Send");
   }

   public boolean canSend() {
      return this.moneyMode.is("Send Only") || this.moneyMode.is("Request & Send");
   }

   public boolean isBusyPaying() {
      return this.busyPaying;
   }

   private boolean sendMoneyRequest(String recipient, int amount) {
      class_310 client = class_310.method_1551();
      if (this.isEnabled()
         && this.canRequest()
         && client.field_1724 != null
         && client.field_1687 != null
         && amount > 0
         && EconomyTextParser.isSafePlayerName(recipient)
         && recipient.equalsIgnoreCase(client.field_1724.method_7334().name())
         && isSupportedProfile(ServerAdapters.current().profile())
         && this.ensureTransport(client)) {
         long tick = client.field_1687.method_75260();
         if (!this.requestCooldown.tryAcquire(tick, 200L)) {
            return false;
         }

         this.transport
            .request(new LocalSyncProtocol.MoneyRequest(this.instanceId, UUID.randomUUID(), ServerAdapters.current().profile().name(), recipient, amount));
         return true;
      } else {
         return false;
      }
   }

   private void processPayment(class_310 client, class_746 player, long tick) {
      this.busyPaying = false;
      if (this.canSend() && this.paymentCooldown.ready(tick)) {
         while (!this.pending.isEmpty() && tick - this.pending.peekFirst().receivedTick() > 300L) {
            this.pending.removeFirst();
         }

         SynchronizationFeature.QueuedRequest queued = this.pending.peekFirst();
         if (queued != null) {
            LocalSyncProtocol.MoneyRequest request = queued.request();
            String profile = ServerAdapters.current().profile().name();
            LocalSyncTransport.Peer requester = this.peer(request.sender(), profile);
            if (requester == null) {
               this.paymentCooldown.defer(tick, 5L);
            } else if (requester.status().canRequest()
               && requester.status().playerName().equals(request.recipient())
               && profile.equals(request.profile())
               && !request.recipient().equalsIgnoreCase(player.method_7334().name())) {
               OptionalLong balance = ScoreboardBalance.read(player);
               if (balance.isEmpty() || balance.getAsLong() < request.amount()) {
                  this.pending.removeFirst();
               } else if (!this.claim(AutomationResource.CHAT)) {
                  this.paymentCooldown.defer(tick, 5L);
               } else {
                  try {
                     if (this.paymentLedger.claim(request.requestId(), LocalSyncProtocol.nowSeconds())) {
                        EconomyCommands.pay(request.recipient(), request.amount()).ifPresent(command -> {
                           ServerAdapter adapter = ServerAdapters.current();
                           this.busyPaying = true;
                           adapter.sendCommand(player, command);
                           this.paymentCooldown.tryAcquire(tick, 100L);
                        });
                        this.pending.removeFirst();
                        return;
                     }

                     this.pending.removeFirst();
                  } finally {
                     PveAutomationCoordinator.INSTANCE.release(this);
                  }
               }
            } else {
               this.pending.removeFirst();
            }
         }
      }
   }

   private LocalSyncTransport.Peer peer(UUID sender, String profile) {
      if (this.transport == null) {
         return null;
      }

      for (LocalSyncTransport.Peer peer : this.transport.peers(profile)) {
         if (peer.status().sender().equals(sender)) {
            return peer;
         }
      }

      return null;
   }

   private void drainReceived(long tick) {
      LocalSyncProtocol.MoneyRequest request;
      while ((request = this.received.poll()) != null) {
         LocalSyncProtocol.MoneyRequest receivedRequest = request;
         if (this.pending.stream().noneMatch(existing -> existing.request().requestId().equals(receivedRequest.requestId()))) {
            this.pending.addLast(new SynchronizationFeature.QueuedRequest(receivedRequest, tick));
         }
      }
   }

   private void publishStatus(class_746 player) {
      this.transport
         .publish(
            new LocalSyncProtocol.Status(
               this.instanceId, ServerAdapters.current().profile().name(), player.method_7334().name(), this.canRequest(), this.canSend()
            )
         );
   }

   private boolean ensureTransport(class_310 client) {
      if (this.transport != null && this.transport.isRunning()) {
         return true;
      }

      if (client != null && !this.moneyMode.is("Off") && isSupportedProfile(ServerAdapters.current().profile())) {
         byte[] token = null;
         LocalSyncTransport candidate = null;

         try {
            this.configDirectory = client.field_1697.toPath().resolve("config").resolve("ryzen");
            token = LocalSyncTokenStore.loadOrCreate(this.configDirectory);
            this.instanceId = UUID.randomUUID();
            this.paymentLedger = new LocalPaymentLedger(this.configDirectory);
            candidate = new LocalSyncTransport(this.instanceId, token, this.received::add);
            candidate.start();
            this.transport = candidate;
            return true;
         } catch (IOException | RuntimeException ignored) {
            if (candidate != null) {
               candidate.close();
            }

            this.stopTransport();
            return false;
         } finally {
            if (token != null) {
               Arrays.fill(token, (byte)0);
            }
         }
      } else {
         return false;
      }
   }

   private void stopTransport() {
      LocalSyncTransport current = this.transport;
      this.transport = null;
      if (current != null) {
         current.close();
      }

      this.instanceId = null;
      this.paymentLedger = null;
      this.received.clear();
      this.pending.clear();
   }

   private void clearRuntime() {
      this.paymentCooldown.reset();
      this.requestCooldown.reset();
      this.nextHeartbeatTick = 0L;
      this.busyPaying = false;
      this.received.clear();
      this.pending.clear();
      this.lastTick = 0L;
   }

   private static boolean isSupportedProfile(ServerProfile profile) {
      return profile == ServerProfile.FUNTIME || profile == ServerProfile.REALLYWORLD;
   }

   @Environment(EnvType.CLIENT)
   private record QueuedRequest(LocalSyncProtocol.MoneyRequest request, long receivedTick) {
   }
}
