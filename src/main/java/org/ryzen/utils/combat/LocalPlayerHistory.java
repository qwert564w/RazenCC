package org.ryzen.utils.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class LocalPlayerHistory {
   private static final int CAPACITY = 16;
   private static final LocalPlayerHistory.Snapshot[] BUFFER = new LocalPlayerHistory.Snapshot[16];
   private static int head;
   private static int size;

   private LocalPlayerHistory() {
   }

   public static void record(class_746 player) {
      head = (head + 1) % 16;
      BUFFER[head] = new LocalPlayerHistory.Snapshot(
         player.method_73189(), player.method_18798(), player.method_24828(), player.field_5992, player.field_36331, (float)player.field_6017
      );
      size = Math.min(size + 1, 16);
   }

   public static LocalPlayerHistory.Snapshot get(int ticksAgo) {
      return ticksAgo >= 0 && ticksAgo < size ? BUFFER[(head - ticksAgo % 16 + 16) % 16] : null;
   }

   public static boolean verticalCollision(int ticksAgo, boolean fallback) {
      LocalPlayerHistory.Snapshot snapshot = get(ticksAgo);
      return snapshot == null ? fallback : snapshot.verticalCollision();
   }

   public static boolean verticalCollisionBelow(int ticksAgo, boolean fallback) {
      LocalPlayerHistory.Snapshot snapshot = get(ticksAgo);
      return snapshot == null ? fallback : snapshot.verticalCollisionBelow();
   }

   public static void reset() {
      for (int i = 0; i < 16; i++) {
         BUFFER[i] = null;
      }

      head = 0;
      size = 0;
   }

   @Environment(EnvType.CLIENT)
   public record Snapshot(class_243 pos, class_243 motion, boolean onGround, boolean verticalCollision, boolean verticalCollisionBelow, float fallDistance) {
   }
}
