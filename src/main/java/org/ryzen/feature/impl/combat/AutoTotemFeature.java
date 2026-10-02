package org.ryzen.feature.impl.combat;

import java.util.EnumSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1294;
import net.minecraft.class_1511;
import net.minecraft.class_1541;
import net.minecraft.class_1548;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3489;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_9334;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.pve.AutomationOwner;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.utils.inventory.InventorySwap;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class AutoTotemFeature extends Feature implements MinecraftContext, AutomationOwner {
   private static final double DANGER_RADIUS = 6.0;
   private static final int ANCHOR_RADIUS_XZ = 4;
   private static final int ANCHOR_RADIUS_Y = 2;
   private static final double FALL_CLIP_DEPTH = 128.0;
   private static final double SAFE_FALL_DISTANCE = 3.0;
   public final NumberSetting health = this.register(new NumberSetting("Health", 16.0, 1.0, 20.0, 0.5, ""));
   public final BooleanSetting skipTalismans = this.register(new BooleanSetting("Skip Talismans", true));
   public final BooleanSetting notWhileEating = this.register(new BooleanSetting("Not While Eating", true));
   public final BooleanSetting notWithHead = this.register(new BooleanSetting("Not With Head", true));
   public final MultiSelectSetting dangers = this.register(
      new MultiSelectSetting(
         "Dangers",
         Set.of("Fall", "Crystal", "Explosion", "Obsidian", "Anchor", "Mace", "Spear"),
         "Fall",
         "Crystal",
         "Explosion",
         "Obsidian",
         "Anchor",
         "Mace",
         "Spear"
      )
   );
   private boolean releasePending;

   public AutoTotemFeature() {
      super("AutoTotem", "Keeps a totem of undying in your offhand", FeatureCategory.COMBAT, -1);
   }

   @Override
   protected void onDisable() {
      this.releasePending = false;
      PveAutomationCoordinator.INSTANCE.release(this);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (this.releasePending && !InventorySwap.isBusy()) {
         this.releasePending = false;
         PveAutomationCoordinator.INSTANCE.release(this);
      }

      class_746 player = this.player();
      if (player != null && !player.method_6079().method_31574(class_1802.field_8288) && !InventorySwap.isBusy()) {
         if (!this.notWhileEating.getValue() || !isEating(player)) {
            if (!this.notWithHead.getValue() || !player.method_6047().method_31574(class_1802.field_8575)) {
               if (!(player.method_6032() > this.health.getValue()) || this.dangerNearby(player)) {
                  int containerSlot = this.findTotemSlot(player);
                  if (containerSlot != -1
                     && PveAutomationCoordinator.INSTANCE.acquire(this, AutomationPriority.EMERGENCY, EnumSet.of(AutomationResource.INVENTORY))) {
                     InventorySwap.equip(containerSlot);
                     this.releasePending = true;
                  }
               }
            }
         }
      }
   }

   private static boolean isEating(class_746 player) {
      return player.method_6115() && player.method_6030().method_57826(class_9334.field_50075);
   }

   private boolean dangerNearby(class_746 player) {
      class_238 box = player.method_5829().method_1014(6.0);
      return this.dangers.isSelected("Fall") && this.lethalFall(player)
         || this.dangers.isSelected("Crystal") && !mc.field_1687.method_18467(class_1511.class, box).isEmpty()
         || this.dangers.isSelected("Explosion") && this.explosionNearby(box)
         || this.dangers.isSelected("Anchor") && this.anchorNearby(player)
         || this.armedPlayerNearby(player, box);
   }

   private boolean lethalFall(class_746 player) {
      if (!player.method_24828()
         && !player.method_5799()
         && !player.method_31549().field_7479
         && !player.method_6128()
         && !player.method_6059(class_1294.field_5906)
         && !(player.method_18798().field_1351 >= 0.0)) {
         class_243 from = player.method_73189();
         class_3965 hit = mc.field_1687
            .method_17742(new class_3959(from, from.method_1031(0.0, -128.0, 0.0), class_3960.field_17558, class_242.field_1347, player));
         if (hit.method_17783() != class_240.field_1333 && !mc.field_1687.method_8316(hit.method_17777()).method_15769()) {
            return false;
         }

         double groundY = hit.method_17783() == class_240.field_1333 ? from.field_1351 - 128.0 : hit.method_17784().field_1351;
         double predictedDamage = player.field_6017 + (from.field_1351 - groundY) - 3.0;
         return predictedDamage >= player.method_6032();
      } else {
         return false;
      }
   }

   private boolean explosionNearby(class_238 box) {
      return !mc.field_1687.method_18467(class_1541.class, box).isEmpty()
         ? true
         : !mc.field_1687.method_8390(class_1548.class, box, creeper -> creeper.method_7000() || creeper.method_7007() > 0).isEmpty();
   }

   private boolean anchorNearby(class_746 player) {
      class_2338 center = player.method_24515();

      for (class_2338 pos : class_2338.method_10097(center.method_10069(-4, -2, -4), center.method_10069(4, 2, 4))) {
         if (mc.field_1687.method_8320(pos).method_27852(class_2246.field_23152)) {
            return true;
         }
      }

      return false;
   }

   private boolean armedPlayerNearby(class_746 player, class_238 box) {
      boolean obsidian = this.dangers.isSelected("Obsidian");
      boolean mace = this.dangers.isSelected("Mace");
      boolean spear = this.dangers.isSelected("Spear");
      if (!obsidian && !mace && !spear) {
         return false;
      }

      for (class_1657 other : mc.field_1687.method_8390(class_1657.class, box, p -> p != player)) {
         if (isThreatItem(other.method_6047(), obsidian, mace, spear) || isThreatItem(other.method_6079(), obsidian, mace, spear)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isThreatItem(class_1799 held, boolean obsidian, boolean mace, boolean spear) {
      return obsidian && (held.method_31574(class_1802.field_8281) || held.method_31574(class_1802.field_22421))
         || mace && held.method_31574(class_1802.field_49814)
         || spear && held.method_31573(class_3489.field_63257);
   }

   private int findTotemSlot(class_746 player) {
      return InventoryUtil.findPlayerMenuSlot(player, this::isUsableTotem);
   }

   private boolean isUsableTotem(class_1799 stack) {
      return stack.method_31574(class_1802.field_8288) && (!this.skipTalismans.getValue() || !stack.method_7942());
   }
}
