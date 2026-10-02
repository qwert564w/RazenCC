package org.ryzen.feature.impl.misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1688;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2636;
import net.minecraft.class_2818;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class KeyFinderFeature extends Feature {
   private static final int SCAN_INTERVAL_TICKS = 20;
   private static final double CHEST_SEARCH_RADIUS = 8.0;
   private static final float TAG_HEIGHT = 12.0F;
   private static final float TEXT_SIZE = 7.5F;
   private static final int LOOTED_COLOR = 11184810;
   public final NumberSetting range = this.register(new NumberSetting("Range", 64.0, 16.0, 256.0, 8.0, " blocks"));
   public final BooleanSetting spawners = this.register(new BooleanSetting("Spawners", true));
   public final BooleanSetting minecarts = this.register(new BooleanSetting("Minecarts", true));
   public final BooleanSetting showDistance = this.register(new BooleanSetting("Show Distance", true));
   public final ColorSetting color = this.register(new ColorSetting("Color", 5636095));
   private final List<KeyFinderFeature.Marker> markers = new ArrayList<>();
   private int ticksUntilScan;

   public KeyFinderFeature() {
      super("KeyFinder", "Marks spawners and loot minecarts through walls", FeatureCategory.MISC, -1);
   }

   @Override
   protected void onDisable() {
      this.markers.clear();
      this.ticksUntilScan = 0;
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.markers.clear();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      class_638 level = client.field_1687;
      if (player != null && level != null) {
         if (--this.ticksUntilScan <= 0) {
            this.ticksUntilScan = 20;
            this.rescan(player, level);
         }
      } else {
         this.markers.clear();
      }
   }

   private void rescan(class_746 player, class_638 level) {
      this.markers.clear();
      double rangeSqr = this.range.getValue() * this.range.getValue();
      if (this.spawners.getValue()) {
         Set<class_2338> chests = new HashSet<>();
         List<class_2338> spawnerPositions = new ArrayList<>();
         int viewDistance = class_310.method_1551().field_1690.method_38521();
         int chunkX = player.method_31476().field_9181;
         int chunkZ = player.method_31476().field_9180;

         for (int x = chunkX - viewDistance; x <= chunkX + viewDistance; x++) {
            for (int z = chunkZ - viewDistance; z <= chunkZ + viewDistance; z++) {
               class_2818 chunk = level.method_2935().method_12126(x, z, false);
               if (chunk != null) {
                  for (class_2586 blockEntity : chunk.method_12214().values()) {
                     class_2338 pos = blockEntity.method_11016();
                     if (!(player.method_24515().method_10262(pos) > rangeSqr)) {
                        if (blockEntity instanceof class_2636) {
                           spawnerPositions.add(pos);
                        } else if (blockEntity instanceof class_2595) {
                           chests.add(pos);
                        }
                     }
                  }
               }
            }
         }

         for (class_2338 spawner : spawnerPositions) {
            int chestCount = 0;

            for (class_2338 chest : chests) {
               if (chest.method_10262(spawner) <= 64.0) {
                  chestCount++;
               }
            }

            this.markers
               .add(
                  new KeyFinderFeature.Marker(
                     class_243.method_24953(spawner), chestCount > 0 ? "Спавнер (" + chestCount + ")" : "Спавнер (залутан)", chestCount > 0
                  )
               );
         }
      }

      if (this.minecarts.getValue()) {
         for (class_1297 entity : level.method_18112()) {
            if (entity instanceof class_1688 minecart && player.method_5858(minecart) <= rangeSqr) {
               this.markers.add(new KeyFinderFeature.Marker(minecart.method_73189(), "Вагонетка", true));
            }
         }
      }
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      class_310 mc = event.getClient();
      if (mc.field_1724 != null && !this.markers.isEmpty()) {
         MsdfFont font = UiFonts.sfProDisplay();
         float unit = 1.0F / mc.method_22683().method_4495();
         float textSize = 7.5F * unit;
         float letterSpacing = textSize * UiFontStyle.MEDIUM.letterSpacingEm();
         float height = 12.0F * unit;

         for (KeyFinderFeature.Marker marker : List.copyOf(this.markers)) {
            Render3DUtil.ScreenPoint anchor = Render3DUtil.projectToScreen(mc, marker.position());
            if (anchor != null) {
               String label = marker.label();
               if (this.showDistance.getValue()) {
                  label = label + String.format(Locale.ROOT, "  %.0fм", mc.field_1724.method_73189().method_1022(marker.position()));
               }

               float width = font.measureWidth(label, textSize, letterSpacing) + 8.0F * unit;
               float x = anchor.x() - width / 2.0F;
               float y = anchor.y() - height / 2.0F;
               int tint = marker.active() ? this.color.getValue() : 11184810;
               Render2DUtil.rect(x, y, width, height).color(ColorUtil.withAlpha(0, 150)).radius(height / 2.0F).draw();
               Render2DUtil.text(x + 4.0F * unit, y + (height - textSize) / 2.0F, textSize, label).font(font).color(ColorUtil.withAlpha(tint, 255)).draw();
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private record Marker(class_243 position, String label, boolean active) {
   }
}
