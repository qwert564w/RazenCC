package org.ryzen.feature.impl.misc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12180;
import net.minecraft.class_1531;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_746;
import net.minecraft.class_12180.class_12181;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.render.Render3DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.world.ZoneGizmos;

@Environment(EnvType.CLIENT)
public final class MineAssistantFeature extends Feature implements MinecraftContext {
   private static final String DIAMOND = "Diamond";
   private static final String REDSTONE = "Redstone";
   private static final String IRON = "Iron";
   private static final String LAPIS = "Lapis";
   private static final String GOLD = "Gold";
   private static final String ANCIENT = "Ancient Debris";
   private static final String COAL = "Coal";
   private static final int SCAN_INTERVAL = 10;
   private static final int MAX_MARKERS = 1200;
   private static final Map<class_2248, MineAssistantFeature.Ore> ORES = createOres();
   public final MultiSelectSetting ores = this.register(
      new MultiSelectSetting("Ores", List.of("Diamond", "Gold", "Ancient Debris"), "Diamond", "Redstone", "Iron", "Lapis", "Gold", "Ancient Debris", "Coal")
   );
   private final List<MineAssistantFeature.Marker> markers = new ArrayList<>();
   private class_2338 mineCenter;
   private int ticks;

   public MineAssistantFeature() {
      super("Mine Assistant", "Highlights selected resources in FunTime automatic mines", FeatureCategory.MISC, -1);
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
      if (player != null && event.getClient().field_1687 != null && ++this.ticks >= 10) {
         this.ticks = 0;
         this.mineCenter = this.findMineCenter(player);
         this.markers.clear();
         if (this.mineCenter != null) {
            int centerX = this.mineCenter.method_10263();
            int centerY = this.mineCenter.method_10264() - 2;
            int centerZ = this.mineCenter.method_10260();

            for (int y = centerY - 10; y <= centerY + 1 && this.markers.size() < 1200; y++) {
               for (int x = centerX - 18; x <= centerX + 18 && this.markers.size() < 1200; x++) {
                  for (int z = centerZ - 18; z <= centerZ + 18 && this.markers.size() < 1200; z++) {
                     class_2338 pos = new class_2338(x, y, z);
                     if (event.getClient().field_1687.method_22340(pos)) {
                        MineAssistantFeature.Ore ore = ORES.get(event.getClient().field_1687.method_8320(pos).method_26204());
                        if (ore != null && this.ores.isSelected(ore.option())) {
                           this.markers.add(new MineAssistantFeature.Marker(pos, ore.color()));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onRender3D(Render3DEvent event) {
      if (!this.markers.isEmpty() && event.getClient().field_1769 != null) {
         class_12181 ignored = event.getClient().field_1769.method_75414();

         try {
            for (MineAssistantFeature.Marker marker : List.copyOf(this.markers)) {
               int stroke = ColorUtil.withAlpha(marker.color(), 225);
               class_12180.method_75553(ZoneGizmos.cube(new class_238(marker.pos()), stroke, ColorUtil.withAlpha(marker.color(), 42), 1.2F)).method_75533();
            }
         } catch (Throwable var7) {
            if (ignored != null) {
               try {
                  ignored.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (ignored != null) {
            ignored.close();
         }
      }
   }

   private class_2338 findMineCenter(class_746 player) {
      class_238 search = player.method_5829().method_1014(256.0);

      for (class_1531 stand : mc.field_1687.method_8390(class_1531.class, search, entity -> entity.method_5805() && entity.method_16914())) {
         String name = stand.method_5477().getString().toLowerCase(Locale.ROOT);
         if (name.contains("авто-шахта") || name.contains("auto-mine") || name.contains("auto mine")) {
            return stand.method_24515();
         }
      }

      return null;
   }

   private void reset() {
      this.markers.clear();
      this.mineCenter = null;
      this.ticks = 0;
   }

   private static Map<class_2248, MineAssistantFeature.Ore> createOres() {
      Map<class_2248, MineAssistantFeature.Ore> result = new LinkedHashMap<>();
      put(result, "Diamond", 2287359, class_2246.field_10442, class_2246.field_29029);
      put(result, "Redstone", 16726832, class_2246.field_10080, class_2246.field_29030);
      put(result, "Iron", 14211288, class_2246.field_10212, class_2246.field_29027);
      put(result, "Lapis", 2647295, class_2246.field_10090, class_2246.field_29028);
      put(result, "Gold", 16766011, class_2246.field_10571, class_2246.field_29026);
      put(result, "Ancient Debris", 11023136, class_2246.field_22109);
      put(result, "Coal", 5592405, class_2246.field_10418, class_2246.field_29219);
      return Map.copyOf(result);
   }

   private static void put(Map<class_2248, MineAssistantFeature.Ore> target, String option, int color, class_2248... blocks) {
      for (class_2248 block : blocks) {
         target.put(block, new MineAssistantFeature.Ore(option, color));
      }
   }

   @Environment(EnvType.CLIENT)
   private record Marker(class_2338 pos, int color) {
   }

   @Environment(EnvType.CLIENT)
   private record Ore(String option, int color) {
   }
}
