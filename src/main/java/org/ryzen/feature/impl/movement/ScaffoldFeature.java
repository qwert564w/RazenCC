package org.ryzen.feature.impl.movement;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.input.PlayerInputEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class ScaffoldFeature extends Feature {
   private static final class_2350[] SUPPORT_DIRECTIONS = new class_2350[]{
      class_2350.field_11033, class_2350.field_11043, class_2350.field_11035, class_2350.field_11039, class_2350.field_11034
   };
   private static final Set<class_2248> BLOCK_BLACKLIST = Set.of(
      class_2246.field_10034,
      class_2246.field_10380,
      class_2246.field_10443,
      class_2246.field_9980,
      class_2246.field_10181,
      class_2246.field_10535,
      class_2246.field_10375,
      class_2246.field_10102,
      class_2246.field_10255
   );
   public final BooleanSetting avoidFall = this.register(new BooleanSetting("Avoid Falling", false));
   public final BooleanSetting keepY = this.register(new BooleanSetting("Keep Y", false));
   public final BooleanSetting swing = this.register(new BooleanSetting("Swing", true));
   public final NumberSetting placeDelay = this.register(new NumberSetting("Place Delay", 1.0, 0.0, 5.0, 1.0, " ticks"));
   private int baseY = Integer.MIN_VALUE;
   private int delay;

   public ScaffoldFeature() {
      super("Scaffold", "Automatically places safe blocks beneath you", FeatureCategory.MOVEMENT, -1);
   }

   @Override
   protected void onEnable() {
      class_746 player = class_310.method_1551().field_1724;
      this.baseY = player == null ? Integer.MIN_VALUE : player.method_24515().method_10264() - 1;
      this.delay = 0;
   }

   @Override
   protected void onDisable() {
      this.baseY = Integer.MIN_VALUE;
      this.delay = 0;
   }

   @EventTarget
   public void onInput(PlayerInputEvent event) {
      class_746 player = class_310.method_1551().field_1724;
      if (this.avoidFall.getValue() && player != null && wouldStepIntoVoid(player)) {
         event.setShift(true);
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null && client.field_1761 != null && player.field_3944 != null) {
         if (this.delay > 0) {
            this.delay--;
         } else {
            int blockSlot = findBlockSlot(player);
            if (blockSlot >= 0) {
               int y = this.keepY.getValue() && this.baseY != Integer.MIN_VALUE ? this.baseY : (int)Math.floor(player.method_23318() - 0.25) - 1;
               class_243 velocity = player.method_18798();
               double lead = player.method_24828() ? 0.35 : 0.18;
               class_2338 target = class_2338.method_49637(
                  player.method_23317() + velocity.field_1352 * lead, y, player.method_23321() + velocity.field_1350 * lead
               );
               if (client.field_1687.method_8320(target).method_45474()) {
                  ScaffoldFeature.Placement placement = findSupport(client, target);
                  if (placement != null) {
                     int previous = player.method_31548().method_67532();
                     if (blockSlot != previous) {
                        player.field_3944.method_52787(new class_2868(blockSlot));
                        player.method_31548().method_61496(blockSlot);
                     }

                     client.field_1761
                        .method_2896(player, class_1268.field_5808, new class_3965(placement.hit(), placement.face(), placement.support(), false));
                     if (this.swing.getValue()) {
                        player.method_6104(class_1268.field_5808);
                     }

                     if (blockSlot != previous) {
                        player.field_3944.method_52787(new class_2868(previous));
                        player.method_31548().method_61496(previous);
                     }

                     this.delay = this.placeDelay.getValue().intValue();
                  }
               }
            }
         }
      }
   }

   private static ScaffoldFeature.Placement findSupport(class_310 client, class_2338 target) {
      for (class_2350 direction : SUPPORT_DIRECTIONS) {
         class_2338 support = target.method_10093(direction);
         class_2680 state = client.field_1687.method_8320(support);
         if (!state.method_26215() && !state.method_45474() && !state.method_26220(client.field_1687, support).method_1110()) {
            class_2350 face = direction.method_10153();
            class_243 hit = class_243.method_24953(support).method_1031(face.method_10148() * 0.5, face.method_10164() * 0.5, face.method_10165() * 0.5);
            return new ScaffoldFeature.Placement(support, face, hit);
         }
      }

      return null;
   }

   private static int findBlockSlot(class_746 player) {
      int best = -1;
      int count = -1;

      for (int slot = 0; slot < 9; slot++) {
         class_1799 stack = player.method_31548().method_5438(slot);
         if (stack.method_7909() instanceof class_1747 blockItem && !BLOCK_BLACKLIST.contains(blockItem.method_7711()) && stack.method_7947() > count) {
            best = slot;
            count = stack.method_7947();
         }
      }

      return best;
   }

   private static boolean wouldStepIntoVoid(class_746 player) {
      class_310 client = class_310.method_1551();
      if (client.field_1687 == null) {
         return false;
      }

      class_243 velocity = player.method_18798();
      class_2338 next = class_2338.method_49637(
         player.method_23317() + velocity.field_1352 * 1.6, player.method_23318() - 1.0, player.method_23321() + velocity.field_1350 * 1.6
      );
      return client.field_1687.method_8320(next).method_45474() && client.field_1687.method_8320(next.method_10074()).method_45474();
   }

   @Environment(EnvType.CLIENT)
   private record Placement(class_2338 support, class_2350 face, class_243 hit) {
   }
}
