package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12178;
import net.minecraft.class_12180;
import net.minecraft.class_1531;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;
import net.minecraft.class_12180.class_12181;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.event.events.render.Render3DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;
import org.ryzen.utils.render.world.ZoneGizmos;

@Environment(EnvType.CLIENT)
public final class WardenEspFeature extends Feature implements MinecraftContext {
   private static final Pattern TIMER = Pattern.compile("(\\d{1,2}):(\\d{2})");
   private static final int CITY_MIN_X = -2070;
   private static final int CITY_MAX_X = -1921;
   private static final int CITY_MIN_Y = -60;
   private static final int CITY_MAX_Y = -35;
   private static final int CITY_MIN_Z = -2076;
   private static final int CITY_MAX_Z = -1929;
   private static final int SCAN_BUDGET = 8000;
   public final BooleanSetting throughWalls = this.register(new BooleanSetting("Through Walls", true));
   public final BooleanSetting timers = this.register(new BooleanSetting("Timers", true));
   private final List<class_2338> chests = new ArrayList<>();
   private final Map<class_2338, WardenEspFeature.Countdown> countdowns = new HashMap<>();
   private int scanCursor;
   private int armorStandTick;

   public WardenEspFeature() {
      super("Warden ESP", "Shows Warden-city chests and their respawn timers", FeatureCategory.VISUAL, -1);
   }

   @Override
   protected void onDisable() {
      this.reset();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.reset();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null && event.getClient().field_1687 != null) {
         if (this.scanCursor == 0) {
            this.chests.clear();
         }

         this.scanSlice(event.getClient());
         if (++this.armorStandTick >= 5) {
            this.armorStandTick = 0;
            this.updateCountdowns(player);
         }
      }
   }

   @EventTarget
   public void onRender3D(Render3DEvent event) {
      if (!this.chests.isEmpty() && event.getClient().field_1769 != null) {
         class_12181 ignored = event.getClient().field_1769.method_75414();

         try {
            for (class_2338 pos : List.copyOf(this.chests)) {
               long remaining = this.remaining(pos);
               int base = remaining > 0L ? 16737380 : 6619030;
               class_12178 handle = class_12180.method_75553(
                  ZoneGizmos.cube(new class_238(pos), ColorUtil.withAlpha(base, 235), ColorUtil.withAlpha(base, 35), 1.4F)
               );
               if (this.throughWalls.getValue()) {
                  handle.method_75533();
               }
            }
         } catch (Throwable var10) {
            if (ignored != null) {
               try {
                  ignored.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (ignored != null) {
            ignored.close();
         }
      }
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      if (this.timers.getValue() && !this.chests.isEmpty()) {
         float scale = (float)(1.0 / Math.max(1.0, event.getClient().method_22683().method_4495()));
         float size = Math.max(6.5F, 12.0F * scale);

         for (class_2338 pos : List.copyOf(this.chests)) {
            long remaining = this.remaining(pos);
            String text = remaining > 0L ? format(remaining) : "Ready";
            Render3DUtil.ScreenPoint point = Render3DUtil.projectToScreen(event.getClient(), class_243.method_24953(pos).method_1031(0.0, 0.85, 0.0));
            if (point != null) {
               float width = UiFonts.sfProDisplay().measureWidth(text, size, size * UiFontStyle.MEDIUM.letterSpacingEm()) + 10.0F;
               float x = point.x() - width / 2.0F;
               float y = point.y() - 7.0F;
               Render2DUtil.rect(x, y, width, 14.0F).color(-938076391).radius(7.0F).draw();
               Render2DUtil.text(x + 5.0F, y + 3.0F, size, text)
                  .font(UiFonts.sfProDisplay())
                  .color(remaining > 0L ? -34953 : -8847460)
                  .style(UiFontStyle.MEDIUM)
                  .draw();
            }
         }
      }
   }

   private void scanSlice(class_310 client) {
      int width = 150;
      int depth = 148;
      int height = 26;
      int plane = width * depth;
      int total = plane * height;
      int budget = 8000;

      while (this.scanCursor < total && budget-- > 0) {
         int index = this.scanCursor++;
         int y = -60 + index / plane;
         int remainder = index % plane;
         int z = -2076 + remainder / width;
         int x = -2070 + remainder % width;
         class_2338 pos = new class_2338(x, y, z);
         if (client.field_1687.method_22340(pos)) {
            class_2248 block = client.field_1687.method_8320(pos).method_26204();
            if (block == class_2246.field_10034 || block == class_2246.field_10380) {
               this.chests.add(pos);
            }
         }
      }

      if (this.scanCursor >= total) {
         this.scanCursor = 0;
      }
   }

   private void updateCountdowns(class_746 player) {
      if (mc.field_1687 != null) {
         class_238 search = player.method_5829().method_1014(320.0);
         long now = System.currentTimeMillis();

         for (class_1531 stand : mc.field_1687.method_8390(class_1531.class, search, entity -> entity.method_5805() && entity.method_16914())) {
            Matcher matcher = TIMER.matcher(stand.method_5477().getString());
            if (matcher.find()) {
               long duration = (Long.parseLong(matcher.group(1)) * 60L + Long.parseLong(matcher.group(2))) * 1000L;
               class_2338 chest = this.nearestChest(stand.method_24515());
               if (chest != null) {
                  this.countdowns.put(chest, new WardenEspFeature.Countdown(duration, now));
               }
            }
         }

         this.countdowns.entrySet().removeIf(entry -> now - entry.getValue().seenAt() > 2700000L);
      }
   }

   private class_2338 nearestChest(class_2338 stand) {
      class_2338 best = null;
      double bestDistance = 9.0;

      for (class_2338 chest : this.chests) {
         double distance = chest.method_10262(stand);
         if (distance < bestDistance) {
            best = chest;
            bestDistance = distance;
         }
      }

      return best;
   }

   private long remaining(class_2338 pos) {
      WardenEspFeature.Countdown countdown = this.countdowns.get(pos);
      return countdown == null ? 0L : Math.max(0L, countdown.duration() - (System.currentTimeMillis() - countdown.seenAt()));
   }

   private static String format(long millis) {
      long seconds = Math.max(0L, millis / 1000L);
      return String.format(Locale.ROOT, "%02d:%02d", seconds / 60L, seconds % 60L);
   }

   private void reset() {
      this.chests.clear();
      this.countdowns.clear();
      this.scanCursor = 0;
      this.armorStandTick = 0;
   }

   @Environment(EnvType.CLIENT)
   private record Countdown(long duration, long seenAt) {
   }
}
