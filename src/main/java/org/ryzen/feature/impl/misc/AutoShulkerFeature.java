package org.ryzen.feature.impl.misc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2815;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_495;
import net.minecraft.class_746;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.utils.inventory.InventoryUtil;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class AutoShulkerFeature extends Feature implements PlayerContext {
   private static final int WORK_SLOT = 7;
   private static final int SHULKER_SLOTS = 27;
   private static final int FIRST_PLAYER_SLOT = 27;
   private static final int STALL_LIMIT = 25;
   private static final Map<String, class_1792> COLOURS = new LinkedHashMap<>();
   public final MultiSelectSetting colours = this.register(
      new MultiSelectSetting("Boxes", List.of("Magenta", "Red", "Light Blue", "Pink"), COLOURS.keySet().toArray(String[]::new))
   );
   private boolean opening;
   private int colourIndex;
   private int stallTicks;

   public AutoShulkerFeature() {
      super("AutoShulker", "Packs your inventory into shulker boxes", FeatureCategory.MISC, -1);
   }

   @Override
   protected void onEnable() {
      this.opening = false;
      this.colourIndex = 0;
      this.stallTicks = 0;
   }

   @Override
   protected void onDisable() {
      this.opening = false;
      this.closeScreen();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null && client.field_1761 != null) {
         if (!this.opening) {
            this.openNextBox(client, player);
         } else if (client.field_1755 instanceof class_495) {
            if (this.isBoxFull(player)) {
               ChatUtil.info("AutoShulker: шалкер заполнен, ищем следующий");
               this.closeScreen();
               this.opening = false;
               this.colourIndex++;
            } else {
               this.fillBox(client, player);
            }
         }
      }
   }

   private void openNextBox(class_310 client, class_746 player) {
      int slot = this.findBoxSlot(player);
      if (slot == -1) {
         ChatUtil.error("AutoShulker: шалкеров в инвентаре нет");
         this.setEnabled(false);
      } else {
         client.field_1761.method_2906(player.field_7498.field_7763, slot, 7, class_1713.field_7791, player);
         if (player.field_3944 != null) {
            player.field_3944.method_52787(new class_2868(7));
         }

         player.method_31548().method_61496(7);
         client.field_1761.method_2919(player, class_1268.field_5808);
         this.opening = true;
         this.stallTicks = 0;
      }
   }

   private void fillBox(class_310 client, class_746 player) {
      class_1703 menu = player.field_7512;
      class_1799 held = player.method_31548().method_5438(7);
      if (!held.method_7960() && !this.isShulkerBox(held)) {
         class_1735 slot = menu.field_7761.size() > 34 ? (class_1735)menu.field_7761.get(34) : null;
         if (slot != null && slot.method_7681()) {
            InventoryUtil.quickMoveSlot(slot.field_7874);
         }

         this.stallTicks = 0;
      } else {
         this.stallTicks++;
         if (this.stallTicks > 25) {
            this.closeScreen();
            this.setEnabled(false);
         }
      }
   }

   private boolean isBoxFull(class_746 player) {
      class_1703 menu = player.field_7512;
      if (menu.field_7761.size() < 27) {
         return false;
      }

      for (int index = 0; index < 27; index++) {
         if (((class_1735)menu.field_7761.get(index)).method_7677().method_7960()) {
            return false;
         }
      }

      return true;
   }

   private int findBoxSlot(class_746 player) {
      List<String> names = List.copyOf(COLOURS.keySet());

      for (int index = this.colourIndex; index < names.size(); index++) {
         String name = names.get(index);
         if (this.colours.isSelected(name)) {
            class_1792 item = COLOURS.get(name);
            int slot = InventoryUtil.findPlayerMenuSlot(player, stack -> stack.method_31574(item));
            if (slot != -1) {
               this.colourIndex = index;
               return slot;
            }
         }
      }

      return -1;
   }

   private boolean isShulkerBox(class_1799 stack) {
      return COLOURS.values().stream().anyMatch(stack::method_31574);
   }

   private void closeScreen() {
      class_310 client = class_310.method_1551();
      class_746 player = client.field_1724;
      if (player != null && client.field_1755 instanceof class_495) {
         if (player.field_3944 != null) {
            player.field_3944.method_52787(new class_2815(player.field_7512.field_7763));
         }

         player.method_7346();
      }
   }

   static {
      COLOURS.put("Magenta", class_1802.field_8050);
      COLOURS.put("Red", class_1802.field_8676);
      COLOURS.put("Light Blue", class_1802.field_8829);
      COLOURS.put("Pink", class_1802.field_8520);
      COLOURS.put("White", class_1802.field_8722);
      COLOURS.put("Plain", class_1802.field_8545);
   }
}
