package org.ryzen.pve.economy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class LocalPaymentLedger {
   private static final long RETENTION_SECONDS = 86400L;
   private static final int MAX_BYTES = 131072;
   private final Path file;

   public LocalPaymentLedger(Path configDirectory) {
      this.file = configDirectory.resolve("local-sync-payments.ledger");
   }

   public synchronized boolean claim(UUID requestId, long nowSeconds) {
      if (requestId == null) {
         return false;
      }

      try {
         Files.createDirectories(this.file.getParent());

         try (
            FileChannel channel = FileChannel.open(this.file, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
            FileLock lock = channel.tryLock();
         ) {
            if (lock == null) {
               return false;
            } else {
               Map<UUID, Long> entries = read(channel);
               entries.entrySet().removeIf(entry -> entry.getValue() < nowSeconds - 86400L);
               if (!entries.containsKey(requestId)) {
                  entries.put(requestId, nowSeconds);
                  write(channel, entries);
                  return true;
               } else {
                  return false;
               }
            }
         }
      } catch (IOException | RuntimeException ignored) {
         return false;
      }
   }

   private static Map<UUID, Long> read(FileChannel channel) throws IOException {
      LinkedHashMap<UUID, Long> entries = new LinkedHashMap<>();
      long size = channel.size();
      if (size > 0L && size <= 131072L) {
         ByteBuffer bytes = ByteBuffer.allocate((int)size);
         channel.position(0L);

         while (bytes.hasRemaining() && channel.read(bytes) >= 0) {
         }

         String content = new String(bytes.array(), StandardCharsets.US_ASCII);

         for (String line : content.split("\\R")) {
            String[] parts = line.trim().split(" ", 2);
            if (parts.length == 2) {
               try {
                  entries.put(UUID.fromString(parts[1]), Long.parseLong(parts[0]));
               } catch (IllegalArgumentException var12) {
               }
            }
         }

         return entries;
      } else {
         return entries;
      }
   }

   private static void write(FileChannel channel, Map<UUID, Long> entries) throws IOException {
      StringBuilder content = new StringBuilder();

      for (Entry<UUID, Long> entry : entries.entrySet()) {
         content.append(entry.getValue()).append(' ').append(entry.getKey()).append('\n');
      }

      byte[] bytes = content.toString().getBytes(StandardCharsets.US_ASCII);
      channel.truncate(0L);
      channel.position(0L);
      ByteBuffer buffer = ByteBuffer.wrap(bytes);

      while (buffer.hasRemaining()) {
         channel.write(buffer);
      }

      channel.force(true);
   }
}
