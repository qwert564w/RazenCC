package org.ryzen.pve.economy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class LocalSyncProtocol {
   public static final int TOKEN_BYTES = 32;
   public static final int MAX_PACKET_BYTES = 768;
   public static final long MAX_CLOCK_SKEW_SECONDS = 20L;
   private static final int MAGIC = 1112298329;
   private static final int VERSION = 1;
   private static final int MAC_BYTES = 32;

   private LocalSyncProtocol() {
   }

   public static byte[] encode(LocalSyncProtocol.Payload payload, long timestampSeconds, long nonce, byte[] token) throws GeneralSecurityException, IOException {
      requireToken(token);
      ByteArrayOutputStream unsignedBytes = new ByteArrayOutputStream();
      DataOutputStream output = new DataOutputStream(unsignedBytes);

      try {
         output.writeInt(1112298329);
         output.writeByte(1);
         output.writeByte(payload.type().wireId);
         output.writeLong(timestampSeconds);
         output.writeLong(nonce);
         writeUuid(output, payload.sender());
         if (payload instanceof LocalSyncProtocol.Status status) {
            writeProfile(output, status.profile());
            writePlayerName(output, status.playerName());
            int capabilities = (status.canRequest() ? 1 : 0) | (status.canSend() ? 2 : 0);
            output.writeByte(capabilities);
         } else if (payload instanceof LocalSyncProtocol.MoneyRequest request) {
            writeUuid(output, request.requestId());
            writeProfile(output, request.profile());
            writePlayerName(output, request.recipient());
            output.writeInt(request.amount());
         }
      } catch (Throwable var12) {
         try {
            output.close();
         } catch (Throwable var11) {
            var12.addSuppressed(var11);
         }

         throw var12;
      }

      output.close();
      byte[] unsigned = unsignedBytes.toByteArray();
      byte[] var14 = sign(unsigned, token);
      if (unsigned.length + var14.length > 768) {
         throw new IOException("Synchronization packet is too large");
      }

      ByteArrayOutputStream packet = new ByteArrayOutputStream(unsigned.length + var14.length);
      packet.write(unsigned);
      packet.write(var14);
      return packet.toByteArray();
   }

   public static Optional<LocalSyncProtocol.Envelope> decode(byte[] packet, byte[] token, long nowSeconds) {
      if (packet != null && packet.length > 32 && packet.length <= 768 && token != null && token.length == 32) {
         int unsignedLength = packet.length - 32;
         byte[] unsigned = new byte[unsignedLength];
         byte[] receivedMac = new byte[32];
         System.arraycopy(packet, 0, unsigned, 0, unsignedLength);
         System.arraycopy(packet, unsignedLength, receivedMac, 0, 32);

         try {
            if (!MessageDigest.isEqual(receivedMac, sign(unsigned, token))) {
               return Optional.empty();
            }

            try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(unsigned))) {
               if (input.readInt() != 1112298329 || input.readUnsignedByte() != 1) {
                  return Optional.empty();
               }

               LocalSyncProtocol.Type type = LocalSyncProtocol.Type.fromWire(input.readUnsignedByte());
               if (type == null) {
                  return Optional.empty();
               }

               long timestamp = input.readLong();
               if (timestamp < nowSeconds - 20L || timestamp > nowSeconds + 20L) {
                  return Optional.empty();
               }

               long nonce = input.readLong();
               UUID sender = readUuid(input);
               LocalSyncProtocol.Payload payload;
               if (type == LocalSyncProtocol.Type.STATUS) {
                  String profile = readProfile(input);
                  String playerName = input.readUTF();
                  int capabilities = input.readUnsignedByte();
                  if (!EconomyTextParser.isSafePlayerName(playerName) || (capabilities & -4) != 0) {
                     return Optional.empty();
                  }

                  payload = new LocalSyncProtocol.Status(sender, profile, playerName, (capabilities & 1) != 0, (capabilities & 2) != 0);
               } else {
                  UUID requestId = readUuid(input);
                  String profile = readProfile(input);
                  String recipient = input.readUTF();
                  int amount = input.readInt();
                  if (!EconomyTextParser.isSafePlayerName(recipient) || amount <= 0) {
                     return Optional.empty();
                  }

                  payload = new LocalSyncProtocol.MoneyRequest(sender, requestId, profile, recipient, amount);
               }

               return input.available() != 0 ? Optional.empty() : Optional.of(new LocalSyncProtocol.Envelope(timestamp, nonce, payload));
            }
         } catch (GeneralSecurityException | IOException | RuntimeException ignored) {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   public static long nowSeconds() {
      return Instant.now().getEpochSecond();
   }

   private static byte[] sign(byte[] data, byte[] token) throws GeneralSecurityException {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(token, "HmacSHA256"));
      return mac.doFinal(data);
   }

   private static void requireToken(byte[] token) {
      if (token == null || token.length != 32) {
         throw new IllegalArgumentException("Synchronization token must contain 32 bytes");
      }
   }

   private static void writeUuid(DataOutputStream output, UUID value) throws IOException {
      output.writeLong(value.getMostSignificantBits());
      output.writeLong(value.getLeastSignificantBits());
   }

   private static UUID readUuid(DataInputStream input) throws IOException {
      return new UUID(input.readLong(), input.readLong());
   }

   private static void writeProfile(DataOutputStream output, String value) throws IOException {
      String profile = value == null ? "" : value;
      if (!profile.matches("[A-Z_]{1,24}")) {
         throw new IOException("Invalid server profile");
      }

      output.writeUTF(profile);
   }

   private static String readProfile(DataInputStream input) throws IOException {
      String profile = input.readUTF();
      if (!profile.matches("[A-Z_]{1,24}")) {
         throw new IOException("Invalid server profile");
      } else {
         return profile;
      }
   }

   private static void writePlayerName(DataOutputStream output, String value) throws IOException {
      if (!EconomyTextParser.isSafePlayerName(value)) {
         throw new IOException("Invalid player name");
      }

      output.writeUTF(value);
   }

   @Environment(EnvType.CLIENT)
   public record Envelope(long timestampSeconds, long nonce, LocalSyncProtocol.Payload payload) {
   }

   @Environment(EnvType.CLIENT)
   public record MoneyRequest(UUID sender, UUID requestId, String profile, String recipient, int amount) implements LocalSyncProtocol.Payload {
      @Override
      public LocalSyncProtocol.Type type() {
         return LocalSyncProtocol.Type.MONEY_REQUEST;
      }
   }

   @Environment(EnvType.CLIENT)
   public sealed interface Payload permits LocalSyncProtocol.Status, LocalSyncProtocol.MoneyRequest {
      UUID sender();

      LocalSyncProtocol.Type type();
   }

   @Environment(EnvType.CLIENT)
   public record Status(UUID sender, String profile, String playerName, boolean canRequest, boolean canSend) implements LocalSyncProtocol.Payload {
      @Override
      public LocalSyncProtocol.Type type() {
         return LocalSyncProtocol.Type.STATUS;
      }
   }

   @Environment(EnvType.CLIENT)
   public enum Type {
      STATUS(1),
      MONEY_REQUEST(2);

      private final int wireId;

      Type(int wireId) {
         this.wireId = wireId;
      }

      private static LocalSyncProtocol.Type fromWire(int wireId) {
         for (LocalSyncProtocol.Type value : values()) {
            if (value.wireId == wireId) {
               return value;
            }
         }

         return null;
      }
   }
}
