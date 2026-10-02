package org.ryzen.pve.economy;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class LocalSyncTransport implements Closeable {
   private static final int FIRST_PORT = 43170;
   private static final int PORT_COUNT = 12;
   private static final long PEER_EXPIRY_SECONDS = 7L;
   private final UUID instanceId;
   private final byte[] token;
   private final Consumer<LocalSyncProtocol.MoneyRequest> requestListener;
   private final SecureRandom random = new SecureRandom();
   private final AtomicBoolean running = new AtomicBoolean();
   private final Map<UUID, LocalSyncTransport.Peer> peers = new ConcurrentHashMap<>();
   private final Map<LocalSyncTransport.ReplayKey, Long> replayCache = new ConcurrentHashMap<>();
   private InetAddress loopback;
   private DatagramSocket socket;
   private Thread receiver;

   public LocalSyncTransport(UUID instanceId, byte[] token, Consumer<LocalSyncProtocol.MoneyRequest> requestListener) {
      if (token != null && token.length == 32) {
         this.instanceId = Objects.requireNonNull(instanceId, "instanceId");
         this.token = Arrays.copyOf(token, token.length);
         this.requestListener = Objects.requireNonNull(requestListener, "requestListener");
      } else {
         throw new IllegalArgumentException("Invalid local synchronization token");
      }
   }

   public synchronized void start() throws IOException {
      if (!this.running.get()) {
         this.loopback = InetAddress.getByName("127.0.0.1");
         this.socket = bind(this.loopback);
         this.running.set(true);
         this.receiver = new Thread(this::receiveLoop, "blade-local-economy-sync");
         this.receiver.setDaemon(true);
         this.receiver.start();
      }
   }

   public boolean isRunning() {
      return this.running.get();
   }

   public void publish(LocalSyncProtocol.Status status) {
      if (status != null && this.instanceId.equals(status.sender())) {
         this.send(status);
      }
   }

   public void request(LocalSyncProtocol.MoneyRequest request) {
      if (request != null && this.instanceId.equals(request.sender())) {
         this.send(request);
      }
   }

   public List<LocalSyncTransport.Peer> peers(String profile) {
      long now = LocalSyncProtocol.nowSeconds();
      this.prune(now);
      ArrayList<LocalSyncTransport.Peer> result = new ArrayList<>();

      for (LocalSyncTransport.Peer peer : this.peers.values()) {
         if (peer.status().profile().equals(profile)) {
            result.add(peer);
         }
      }

      return List.copyOf(result);
   }

   @Override
   public synchronized void close() {
      this.running.set(false);
      if (this.socket != null) {
         this.socket.close();
      }

      this.socket = null;
      this.receiver = null;
      this.peers.clear();
      this.replayCache.clear();
      Arrays.fill(this.token, (byte)0);
   }

   private void send(LocalSyncProtocol.Payload payload) {
      DatagramSocket activeSocket = this.socket;
      if (this.running.get() && activeSocket != null && !activeSocket.isClosed()) {
         try {
            byte[] encoded = LocalSyncProtocol.encode(payload, LocalSyncProtocol.nowSeconds(), this.random.nextLong(), this.token);

            for (int port = 43170; port < 43182; port++) {
               DatagramPacket packet = new DatagramPacket(encoded, encoded.length, this.loopback, port);
               activeSocket.send(packet);
            }
         } catch (GeneralSecurityException | IOException | RuntimeException var6) {
         }
      }
   }

   private void receiveLoop() {
      byte[] buffer = new byte[768];

      while (this.running.get()) {
         DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);

         try {
            this.socket.receive(datagram);
            if (datagram.getAddress().isLoopbackAddress()) {
               byte[] packet = Arrays.copyOfRange(datagram.getData(), datagram.getOffset(), datagram.getOffset() + datagram.getLength());
               this.handle(packet);
            }
         } catch (SocketException exception) {
            if (this.running.get()) {
               this.running.set(false);
            }
         } catch (IOException | RuntimeException var5) {
         }
      }
   }

   private void handle(byte[] packet) {
      long now = LocalSyncProtocol.nowSeconds();
      LocalSyncProtocol.decode(packet, this.token, now).ifPresent(envelope -> {
         LocalSyncProtocol.Payload payload = envelope.payload();
         if (!this.instanceId.equals(payload.sender())) {
            LocalSyncTransport.ReplayKey replay = new LocalSyncTransport.ReplayKey(payload.sender(), envelope.nonce());
            if (this.replayCache.putIfAbsent(replay, now) == null) {
               this.prune(now);
               if (payload instanceof LocalSyncProtocol.Status status) {
                  this.peers.put(status.sender(), new LocalSyncTransport.Peer(status, now));
               } else if (payload instanceof LocalSyncProtocol.MoneyRequest request) {
                  this.requestListener.accept(request);
               }
            }
         }
      });
   }

   private void prune(long now) {
      this.peers.entrySet().removeIf(entry -> now - entry.getValue().lastSeenSeconds() > 7L);
      this.replayCache.entrySet().removeIf(entry -> now - entry.getValue() > 40L);
   }

   private static DatagramSocket bind(InetAddress loopback) throws IOException {
      IOException lastFailure = null;

      for (int port = 43170; port < 43182; port++) {
         DatagramSocket candidate = new DatagramSocket(null);

         try {
            candidate.setReuseAddress(false);
            candidate.bind(new InetSocketAddress(loopback, port));
            return candidate;
         } catch (IOException exception) {
            lastFailure = exception;
            candidate.close();
         }
      }

      throw new IOException("No local synchronization port is available", lastFailure);
   }

   @Environment(EnvType.CLIENT)
   public record Peer(LocalSyncProtocol.Status status, long lastSeenSeconds) {
   }

   @Environment(EnvType.CLIENT)
   private record ReplayKey(UUID sender, long nonce) {
   }
}
