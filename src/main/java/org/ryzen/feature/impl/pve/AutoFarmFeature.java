package org.ryzen.feature.impl.pve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2846;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_2828.class_2831;
import net.minecraft.class_2846.class_2847;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveFeature;

@Environment(EnvType.CLIENT)
public final class AutoFarmFeature extends PveFeature {
   private static final String ROTATE_NONE = "None";
   private static final String ROTATE_CLIENT = "Client";
   private static final String ROTATE_PACKET = "Packet";
   private static final String CROP_PUMPKIN = "Pumpkins";
   private static final String CROP_MELON = "Melons";
   private static final String CROP_CARROT = "Carrots";
   private static final String CROP_POTATO = "Potatoes";
   private static final String CROP_BEETROOT = "Beetroots";
   private static final String CROP_WHEAT = "Wheat";
   private static final String CROP_SAPLINGS = "Saplings";
   private static final String CROP_BAMBOO = "Bamboo";
   private static final String CROP_COCOA = "Cocoa";
   private static final String CROP_SUGAR_CANE = "Sugar Cane";
   private static final String CROP_CACTUS = "Cactus";
   private static final String CROP_BERRIES = "Sweet Berries";
   private static final String CROP_NETHER_WART = "Nether Wart";
   private static final int STUCK_TICKS = 5;
   private static final double STUCK_EPSILON = 0.01;
   private static final double ARRIVE_DISTANCE = 2.0;
   public final NumberSetting range = this.register(new NumberSetting("Range", 4.5, 1.0, 6.0, 0.1, " blocks"));
   public final NumberSetting delay = this.register(new NumberSetting("Delay", 50.0, 0.0, 500.0, 10.0, " ms"));
   public final NumberSetting blocksPerTick = this.register(new NumberSetting("Blocks Per Tick", 10.0, 1.0, 32.0, 1.0, ""));
   public final BooleanSetting autoWalk = this.register(new BooleanSetting("Auto Walk", true));
   public final NumberSetting walkRange = this.register(new NumberSetting("Walk Range", 15.0, 5.0, 50.0, 1.0, " blocks").visibleWhen(this.autoWalk::getValue));
   public final BooleanSetting pauseWhileHarvesting = this.register(new BooleanSetting("Pause While Harvesting", true).visibleWhen(this.autoWalk::getValue));
   public final ModeSetting rotate = this.register(new ModeSetting("Rotate", "None", "None", "Client", "Packet"));
   public final MultiSelectSetting crops = this.register(
      new MultiSelectSetting(
         "Crops",
         List.of(
            "Pumpkins",
            "Melons",
            "Carrots",
            "Potatoes",
            "Beetroots",
            "Wheat",
            "Saplings",
            "Bamboo",
            "Cocoa",
            "Sugar Cane",
            "Cactus",
            "Sweet Berries",
            "Nether Wart"
         ),
         "Pumpkins",
         "Melons",
         "Carrots",
         "Potatoes",
         "Beetroots",
         "Wheat",
         "Saplings",
         "Bamboo",
         "Cocoa",
         "Sugar Cane",
         "Cactus",
         "Sweet Berries",
         "Nether Wart"
      )
   );
   private long nextHarvestAt;
   private class_243 lastPosition;
   private int stuckTicks;

   public AutoFarmFeature() {
      super(
         "AutoFarm",
         "Harvests ripe crops around you and walks between patches",
         -1,
         AutomationPriority.FEATURE,
         AutomationResource.MOVEMENT,
         AutomationResource.ROTATION
      );
   }

   @Override
   protected void onPveEnable() {
      this.nextHarvestAt = 0L;
      this.lastPosition = null;
      this.stuckTicks = 0;
   }

   @Override
   protected void onPveDisable() {
      this.releaseMovement();
      this.lastPosition = null;
      this.stuckTicks = 0;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      class_638 level = client.field_1687;
      if (player != null && level != null) {
         List<class_2338> inReach = this.findCrops(player, level, this.range.getValue());
         List<class_2338> walkTargets = this.autoWalk.getValue() ? this.findCrops(player, level, this.walkRange.getValue()) : List.of();
         boolean harvestReady = !inReach.isEmpty() && System.currentTimeMillis() >= this.nextHarvestAt;
         boolean holdStill = this.pauseWhileHarvesting.getValue() && harvestReady;
         if (this.autoWalk.getValue() && !walkTargets.isEmpty() && !holdStill) {
            this.walkToward(client, player, level, (class_2338)walkTargets.getFirst());
         } else if (this.autoWalk.getValue()) {
            this.releaseMovement();
         }

         if (harvestReady) {
            if (holdStill) {
               this.releaseMovement();
            }

            this.harvest(client, player, inReach);
         }
      }
   }

   private void harvest(class_310 client, class_746 player, List<class_2338> targets) {
      double rangeSqr = this.range.getValue() * this.range.getValue();
      int limit = this.blocksPerTick.getValue().intValue();
      int broken = 0;

      for (class_2338 pos : targets) {
         if (broken >= limit) {
            break;
         }

         if (!(player.method_5707(class_243.method_24953(pos)) > rangeSqr)) {
            this.aimAt(client, player, pos);
            if (player.field_3944 != null) {
               player.field_3944.method_52787(new class_2846(class_2847.field_12968, pos, class_2350.field_11036));
               player.field_3944.method_52787(new class_2846(class_2847.field_12973, pos, class_2350.field_11036));
            }

            broken++;
         }
      }

      if (broken > 0) {
         this.nextHarvestAt = System.currentTimeMillis() + this.delay.getValue().longValue();
      }
   }

   private void aimAt(class_310 client, class_746 player, class_2338 pos) {
      if (!this.rotate.is("None")) {
         class_243 delta = class_243.method_24953(pos).method_1020(player.method_33571());
         float yaw = class_3532.method_15393((float)(Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0));
         float pitch = class_3532.method_15363(
            (float)(-Math.toDegrees(Math.atan2(delta.field_1351, Math.hypot(delta.field_1352, delta.field_1350)))), -90.0F, 90.0F
         );
         if (this.rotate.is("Client")) {
            player.method_36456(yaw);
            player.method_36457(pitch);
            player.field_6241 = yaw;
         } else {
            if (player.field_3944 != null) {
               player.field_3944.method_52787(new class_2831(yaw, pitch, player.method_24828(), player.field_5976));
            }
         }
      }
   }

   private void walkToward(class_310 client, class_746 player, class_638 level, class_2338 target) {
      double deltaX = target.method_10263() + 0.5 - player.method_23317();
      double deltaZ = target.method_10260() + 0.5 - player.method_23321();
      double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
      if (horizontal < 2.0) {
         this.releaseMovement();
         this.stuckTicks = 0;
         this.lastPosition = null;
      } else {
         float yaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
         if (this.lastPosition != null) {
            boolean stalled = player.method_73189().method_1025(this.lastPosition) < 0.01 && player.method_24828();
            this.stuckTicks = stalled ? this.stuckTicks + 1 : 0;
         }

         this.lastPosition = player.method_73189();
         if (this.stuckTicks > 5) {
            yaw += this.stuckTicks % 20 < 10 ? 45.0F : -45.0F;
         }

         player.method_36456(yaw);
         client.field_1690.field_1894.method_23481(true);
         class_2338 ahead = player.method_24515();
         class_243 look = player.method_5828(1.0F);
         class_2338 step = ahead.method_10069((int)Math.round(look.field_1352), 0, (int)Math.round(look.field_1350));
         class_2338 stepPrecise = class_2338.method_49637(
            player.method_23317() + look.field_1352, player.method_23318(), player.method_23321() + look.field_1350
         );
         boolean blocked = !level.method_8320(step).method_26215() || !level.method_8320(stepPrecise).method_26215();
         boolean headroom = level.method_8320(step.method_10084()).method_26215() && level.method_8320(step.method_10086(2)).method_26215();
         boolean shouldJump = player.method_24828() && (player.field_5976 || blocked && headroom);
         client.field_1690.field_1903.method_23481(shouldJump);
      }
   }

   private void releaseMovement() {
      class_310 client = class_310.method_1551();
      client.field_1690.field_1894.method_23481(false);
      client.field_1690.field_1903.method_23481(false);
   }

   private List<class_2338> findCrops(class_746 player, class_638 level, double radius) {
      List<class_2338> found = new ArrayList<>();
      class_2338 origin = player.method_24515();
      int limit = (int)Math.ceil(radius);

      for (int x = -limit; x <= limit; x++) {
         for (int y = -limit; y <= limit; y++) {
            for (int z = -limit; z <= limit; z++) {
               class_2338 pos = origin.method_10069(x, y, z);
               if (this.isHarvestable(level, pos)) {
                  found.add(pos);
               }
            }
         }
      }

      found.sort(Comparator.comparingDouble(posx -> player.method_5707(class_243.method_24953(posx))));
      return found;
   }

   private boolean isHarvestable(class_638 level, class_2338 pos) {
      class_2680 state = level.method_8320(pos);
      class_2248 block = state.method_26204();
      if (block == class_2246.field_46282) {
         return this.crops.isSelected("Pumpkins");
      } else if (block == class_2246.field_46283) {
         return this.crops.isSelected("Melons");
      } else if (block == class_2246.field_10609) {
         return this.crops.isSelected("Carrots") && (Integer)state.method_11654(class_2741.field_12550) >= 7;
      } else if (block == class_2246.field_10247) {
         return this.crops.isSelected("Potatoes") && (Integer)state.method_11654(class_2741.field_12550) >= 7;
      } else if (block == class_2246.field_10341) {
         return this.crops.isSelected("Beetroots") && (Integer)state.method_11654(class_2741.field_12497) >= 3;
      } else if (block == class_2246.field_10293) {
         return this.crops.isSelected("Wheat") && (Integer)state.method_11654(class_2741.field_12550) >= 7;
      } else if (isSapling(block)) {
         return this.crops.isSelected("Saplings");
      } else if (block == class_2246.field_10211 || block == class_2246.field_10108) {
         return this.crops.isSelected("Bamboo");
      } else if (block == class_2246.field_10302) {
         return this.crops.isSelected("Cocoa") && (Integer)state.method_11654(class_2741.field_12556) >= 2;
      } else if (block == class_2246.field_10424) {
         return this.crops.isSelected("Sugar Cane") && level.method_8320(pos.method_10074()).method_27852(class_2246.field_10424);
      } else if (block == class_2246.field_10029) {
         return this.crops.isSelected("Cactus") && level.method_8320(pos.method_10074()).method_27852(class_2246.field_10029);
      } else if (block == class_2246.field_16999) {
         return this.crops.isSelected("Sweet Berries") && (Integer)state.method_11654(class_2741.field_12497) >= 2;
      } else {
         return block != class_2246.field_9974 ? false : this.crops.isSelected("Nether Wart") && (Integer)state.method_11654(class_2741.field_12497) >= 3;
      }
   }

   private static boolean isSapling(class_2248 block) {
      return block == class_2246.field_10394
         || block == class_2246.field_10575
         || block == class_2246.field_10217
         || block == class_2246.field_10276
         || block == class_2246.field_10385
         || block == class_2246.field_10160
         || block == class_2246.field_54712
         || block == class_2246.field_42727
         || block == class_2246.field_37544;
   }
}
