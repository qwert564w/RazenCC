package org.ryzen.feature.impl.movement;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12175;
import net.minecraft.class_12177;
import net.minecraft.class_12180;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_7439;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_12180.class_12181;
import net.minecraft.class_2828.class_2829;
import net.minecraft.class_2828.class_5911;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.event.events.render.Render3DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class TpLootFeature extends Feature implements MinecraftContext {
   private static final String MODE_GRIM = "Grim";
   private static final String MODE_MATRIX = "Grim + Matrix";
   private static final long TELEPORT_COOLDOWN_MS = 50L;
   private static final long LOOT_FINISH_DELAY_MS = 100L;
   private static final double BOX_INSET = 0.5;
   private static final double CHASE_SPEED_SCALE = 0.5;
   private static final double MAX_CHASE_SPEED = 0.8;
   private static final double MIN_CHASE_DISTANCE = 0.3;
   private static final float HEALTH_ARMOR_FACTOR = 0.04F;
   private static final int PRIORITY_FALLBACK = Integer.MAX_VALUE;
   private static final int PRIORITY_OTHER = 100;
   private static final List<String> FLIGHT_MARKERS = List.of("режиме полета", "режим полёта");
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Grim", "Grim", "Grim + Matrix"));
   public final BooleanSetting autoLeave = this.register(new BooleanSetting("Auto Leave", true));
   public final TextSetting leaveCommand = this.register(new TextSetting("Leave Command", "hub"));
   public final BooleanSetting autoDisable = this.register(new BooleanSetting("Auto Disable", true));
   public final BooleanSetting tpCommand = this.register(new BooleanSetting("Tp Command", true));
   public final TextSetting command = this.register(new TextSetting("Command", "tp"));
   public final TextSetting targetItems = this.register(new TextSetting("Target Items", ""));
   private class_238 chaseBox;
   private class_238 lootBox;
   private boolean chasingPlayer;
   private boolean teleportActive;
   private boolean tpCommandSent;
   private long lastTeleportMs;
   private long lastLootMs;

   public TpLootFeature() {
      super("TpLoot", "Teleports onto dropped loot and collects it", FeatureCategory.MOVEMENT, -1);
   }

   @Override
   protected void onDisable() {
      this.resetState();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.resetState();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null && mc.field_1687 != null) {
         boolean foundLoot = this.scanBestLootItem(player);
         if (this.mode.is("Grim + Matrix")) {
            this.handleMatrix(player, foundLoot);
         } else {
            this.handleGrim(player, foundLoot);
         }

         if (this.teleportActive && System.currentTimeMillis() - this.lastLootMs > 100L && this.hasTargetItemsInInventory(player)) {
            if (this.autoLeave.getValue() && player.field_3944 != null) {
               player.field_3944.method_45730(stripSlash(this.leaveCommand.getValue()));
            }

            this.teleportActive = false;
            if (this.autoDisable.getValue()) {
               this.setEnabled(false);
               ChatUtil.info("TpLoot finished");
            }
         }
      }
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE && event.getPacket() instanceof class_7439 chat) {
         String var4 = chat.comp_763().getString().toLowerCase(Locale.ROOT);
         if (FLIGHT_MARKERS.stream().anyMatch(var4::contains)) {
            this.sendTpChatCommand();
            this.tpCommandSent = true;
         }
      }
   }

   @EventTarget
   public void onRender3D(Render3DEvent event) {
      if (this.mode.is("Grim + Matrix") && mc.field_1724 != null && mc.field_1687 != null && event.getClient().field_1769 != null) {
         class_12181 ignored = event.getClient().field_1769.method_75414();

         try {
            if (this.chaseBox != null) {
               class_12180.method_75553(new TpLootFeature.LootGizmo(this.chaseBox, 1694498815)).method_75533();
            }

            if (this.lootBox != null) {
               class_12180.method_75553(new TpLootFeature.LootGizmo(this.lootBox, 1694498560)).method_75533();
            }
         } catch (Throwable var6) {
            if (ignored != null) {
               try {
                  ignored.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (ignored != null) {
            ignored.close();
         }
      }
   }

   private void handleGrim(class_746 player, boolean foundLoot) {
      if (foundLoot && this.lootBox != null) {
         if (!player.method_5829().method_994(this.lootBox)) {
            this.teleportToBox(player, this.lootBox);
            this.tpCommandSent = false;
         } else {
            if (this.tpCommand.getValue() && !this.tpCommandSent) {
               this.sendTpChatCommand();
               this.tpCommandSent = true;
            }
         }
      }
   }

   private void handleMatrix(class_746 player, boolean foundLoot) {
      if (foundLoot && this.lootBox != null) {
         this.chasingPlayer = true;
         if (!player.method_5829().method_994(this.lootBox)) {
            this.teleportToBox(player, this.lootBox);
            this.tpCommandSent = false;
         } else {
            this.chasingPlayer = false;
            if (player.method_31549().field_7479 && this.tpCommand.getValue() && !this.tpCommandSent) {
               this.sendTpChatCommand();
               this.tpCommandSent = true;
            }

            this.scanAndTeleportItems(player);
         }
      } else {
         class_1657 target = this.findWeakestPlayer(player);
         if (target != null && !this.chasingPlayer) {
            this.tpCommandSent = false;
            this.chasePlayer(player, target);
         }
      }
   }

   private boolean scanBestLootItem(class_746 player) {
      int bestPriority = Integer.MAX_VALUE;
      class_243 bestPos = null;

      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (entity instanceof class_1542 itemEntity) {
            int priority = this.itemPriority(itemEntity.method_6983().method_7909());
            if (priority < bestPriority) {
               bestPriority = priority;
               bestPos = itemEntity.method_73189();
            }
         }
      }

      if (bestPos == null) {
         this.lootBox = null;
         return false;
      } else {
         this.lootBox = new class_238(
            bestPos.field_1352 - 0.5,
            bestPos.field_1351,
            bestPos.field_1350 - 0.5,
            bestPos.field_1352 + 0.5,
            bestPos.field_1351 + 1.0,
            bestPos.field_1350 + 0.5
         );
         return true;
      }
   }

   private void scanAndTeleportItems(class_746 player) {
      if (System.currentTimeMillis() - this.lastTeleportMs >= 50L) {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1542 itemEntity && this.isTargetItem(itemEntity.method_6983().method_7909())) {
               this.teleportTo(player, itemEntity.method_73189());
               return;
            }
         }
      }
   }

   private void teleportToBox(class_746 player, class_238 box) {
      double x = (box.field_1323 + box.field_1320) / 2.0;
      double y = box.field_1322;
      double z = (box.field_1321 + box.field_1324) / 2.0;
      this.teleportTo(player, new class_243(x, y, z));
      if (this.mode.is("Grim + Matrix")) {
         this.chaseBox = box;
      }
   }

   private void teleportTo(class_746 player, class_243 pos) {
      if (player.field_3944 != null) {
         long now = System.currentTimeMillis();
         if (now - this.lastTeleportMs >= 50L) {
            for (int i = 0; i < 3; i++) {
               player.field_3944.method_52787(new class_5911(player.method_24828(), player.field_5976));
            }

            player.field_3944.method_52787(new class_2829(pos.field_1352, pos.field_1351, pos.field_1350, false, player.field_5976));
            player.method_5814(pos.field_1352, pos.field_1351, pos.field_1350);
            this.lastTeleportMs = now;
            this.teleportActive = true;
            this.lastLootMs = now;
         }
      }
   }

   private class_1657 findWeakestPlayer(class_746 self) {
      class_1657 best = null;
      float bestScore = Float.MAX_VALUE;

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (player != self && player.method_5805() && !player.method_7325() && !player.method_68878()) {
            float score = this.playerHealthScore(player);
            if (score < bestScore) {
               bestScore = score;
               best = player;
            }
         }
      }

      return best;
   }

   private void chasePlayer(class_746 player, class_1657 target) {
      class_243 pos = target.method_73189();
      double tx = pos.field_1352;
      double ty = player.method_23318();
      double tz = pos.field_1350;
      this.chaseBox = new class_238(tx - 0.5, ty - 0.1, tz - 0.5, tx + 0.5, ty + 0.5, tz + 0.5);
      double dx = tx - player.method_23317();
      double dy = ty - player.method_23318();
      double dz = tz - player.method_23321();
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (distance < 0.3) {
         player.method_18800(0.0, 0.0, 0.0);
      } else {
         double speed = Math.min(distance * 0.5, 0.8);
         player.method_18800(dx / distance * speed, dy / distance * speed, dz / distance * speed);
         player.field_6037 = true;
         player.method_36456((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
      }
   }

   private float playerHealthScore(class_1657 player) {
      float health = player.method_6032() + player.method_6067();
      float armorFactor = player.method_6096() * 0.04F;
      return health * (1.0F - armorFactor);
   }

   private boolean hasTargetItemsInInventory(class_746 player) {
      for (int slot = 0; slot < 36; slot++) {
         if (this.isTargetItem(player.method_31548().method_5438(slot).method_7909())) {
            return true;
         }
      }

      return false;
   }

   private boolean isTargetItem(class_1792 item) {
      List<String> filter = this.parsedTargets();
      if (filter.isEmpty()) {
         return this.itemPriority(item) != Integer.MAX_VALUE;
      }

      String id = this.itemId(item);
      return filter.stream().anyMatch(id::contains);
   }

   private List<String> parsedTargets() {
      String raw = this.targetItems.getValue();
      return raw != null && !raw.isBlank()
         ? Arrays.stream(raw.toLowerCase(Locale.ROOT).split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()
         : List.of();
   }

   private int itemPriority(class_1792 item) {
      String name = this.itemId(item);
      if (!this.parsedTargets().isEmpty() && this.parsedTargets().stream().noneMatch(name::contains)) {
         return Integer.MAX_VALUE;
      } else if (name.contains("totem")) {
         return 1;
      } else if (name.contains("elytra")) {
         return 2;
      } else if (name.contains("enchanted_golden_apple") || name.contains("notch_apple")) {
         return 3;
      } else if (name.contains("netherite") || name.contains("ancient_debris")) {
         return 4;
      } else if (name.contains("skull") || name.contains("head")) {
         return 5;
      } else if (name.contains("end_crystal")) {
         return 6;
      } else if (name.contains("pickaxe")
         || name.contains("axe")
         || name.contains("shovel")
         || name.contains("hoe")
         || name.contains("sword")
         || name.contains("bow")
         || name.contains("crossbow")
         || name.contains("trident")) {
         return 7;
      } else if (name.contains("golden_apple")) {
         return 8;
      } else if (name.contains("firework")) {
         return 9;
      } else if (name.contains("obsidian")) {
         return 10;
      } else if (name.contains("potion") || name.contains("splash") || name.contains("lingering")) {
         return 11;
      } else {
         return name.contains("golden_carrot") ? 12 : 100;
      }
   }

   private String itemId(class_1792 item) {
      return class_7923.field_41178.method_10221(item).method_12832().toLowerCase(Locale.ROOT);
   }

   private void sendTpChatCommand() {
      class_746 player = mc.field_1724;
      if (player != null && player.field_3944 != null) {
         player.field_3944.method_45730(stripSlash(this.command.getValue()));
      }
   }

   private static String stripSlash(String command) {
      return command.startsWith("/") ? command.substring(1) : command;
   }

   private void resetState() {
      this.teleportActive = false;
      this.chaseBox = null;
      this.lootBox = null;
      this.chasingPlayer = false;
      this.tpCommandSent = false;
   }

   @Environment(EnvType.CLIENT)
   private record LootGizmo(class_238 box, int color) implements class_12175 {
      public void method_75531(class_12177 primitives, float alpha) {
         class_243 a = new class_243(this.box.field_1323, this.box.field_1322, this.box.field_1321);
         class_243 b = new class_243(this.box.field_1320, this.box.field_1322, this.box.field_1321);
         class_243 c = new class_243(this.box.field_1320, this.box.field_1322, this.box.field_1324);
         class_243 d = new class_243(this.box.field_1323, this.box.field_1322, this.box.field_1324);
         class_243 e = new class_243(this.box.field_1323, this.box.field_1325, this.box.field_1321);
         class_243 f = new class_243(this.box.field_1320, this.box.field_1325, this.box.field_1321);
         class_243 g = new class_243(this.box.field_1320, this.box.field_1325, this.box.field_1324);
         class_243 h = new class_243(this.box.field_1323, this.box.field_1325, this.box.field_1324);
         float w = 1.5F;
         primitives.method_75473(a, b, this.color, w);
         primitives.method_75473(b, c, this.color, w);
         primitives.method_75473(c, d, this.color, w);
         primitives.method_75473(d, a, this.color, w);
         primitives.method_75473(e, f, this.color, w);
         primitives.method_75473(f, g, this.color, w);
         primitives.method_75473(g, h, this.color, w);
         primitives.method_75473(h, e, this.color, w);
         primitives.method_75473(a, e, this.color, w);
         primitives.method_75473(b, f, this.color, w);
         primitives.method_75473(c, g, this.color, w);
         primitives.method_75473(d, h, this.color, w);
      }
   }
}
