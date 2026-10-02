package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2487;
import net.minecraft.class_746;
import net.minecraft.class_9279;
import net.minecraft.class_9334;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.utils.inventory.InventorySwap;

@Environment(EnvType.CLIENT)
public final class AutoSwapFeature extends Feature implements MinecraftContext {
   private static final int OFFHAND_SLOT = 45;
   private static final String ITEM_HEAD = "Head";
   private static final String ITEM_TOTEM = "Totem";
   private static final String ITEM_GAPPLE = "Gapple";
   private static final String ITEM_SHIELD = "Shield";
   private static final String CERB_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA5NWE3ZmQ5MGRhYTFiYmU3MDY5MDg5NzQwZTA1ZDBiZmM2NjI5NmVlM2M0MGVlNzFhNGUwYTY2MTZiMmJiYyJ9fX0=";
   public final ModeSetting item = this.register(new ModeSetting("Item", "Totem", "Head", "Totem", "Gapple", "Shield"));
   public final ModeSetting swap = this.register(new ModeSetting("Swap", "Head", "Head", "Totem", "Gapple", "Shield"));
   public final InputBindSetting key = this.register(new InputBindSetting("Key", -1));
   public final InputBindSetting cerbKey = this.register(new InputBindSetting("Cerb Key", -1));
   private int pendingSlot = -1;

   public AutoSwapFeature() {
      super("AutoSwap", "Swap items to your offhand on key press", FeatureCategory.COMBAT, -1);
      this.renamedFrom("OffhandSwap");
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (event.getAction() == 1) {
         if (this.key.matches(event.getKey())) {
            event.cancel();
            this.requestSwap();
         } else if (this.cerbKey.matches(event.getKey())) {
            event.cancel();
            this.requestCerbSwap();
         }
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      if (event.getAction() == 1) {
         if (this.key.matchesMouse(event.getButton())) {
            event.cancel();
            this.requestSwap();
         } else if (this.cerbKey.matchesMouse(event.getButton())) {
            event.cancel();
            this.requestCerbSwap();
         }
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (this.pendingSlot >= 0 && !InventorySwap.isBusy()) {
         class_746 player = event.getClient().field_1724;
         if (player != null && player.field_7512 == player.field_7498) {
            InventorySwap.equip(this.pendingSlot);
         }

         this.pendingSlot = -1;
      }
   }

   private void requestSwap() {
      class_746 player = this.player();
      if (player != null && mc.field_1755 == null && player.field_7512 == player.field_7498) {
         class_1792 primary = itemFor(this.item.getValue());
         class_1792 secondary = itemFor(this.swap.getValue());
         class_1792 offhandItem = player.method_6079().method_7909();
         class_1792 target = offhandItem != primary ? primary : secondary;
         int slot = findBestSlot(player, target);
         if (slot >= 0) {
            if (InventorySwap.isBusy()) {
               this.pendingSlot = slot;
            } else {
               InventorySwap.equip(slot);
            }
         }
      }
   }

   private void requestCerbSwap() {
      class_746 player = this.player();
      if (player != null && mc.field_1755 == null && player.field_7512 == player.field_7498) {
         int slot = findCerbHeadSlot(player);
         if (slot >= 0) {
            if (InventorySwap.isBusy()) {
               this.pendingSlot = slot;
            } else {
               InventorySwap.equip(slot);
            }
         }
      }
   }

   private static int findBestSlot(class_746 player, class_1792 target) {
      if (target == class_1802.field_8162) {
         return -1;
      }

      int fallback = -1;

      for (int slot = 9; slot < 45; slot++) {
         if (slot != 45) {
            class_1799 stack = player.field_7498.method_7611(slot).method_7677();
            if (stack.method_31574(target)) {
               if (!stack.method_7942()) {
                  return slot;
               }

               if (fallback < 0) {
                  fallback = slot;
               }
            }
         }
      }

      return fallback;
   }

   private static int findCerbHeadSlot(class_746 player) {
      for (int slot = 9; slot < 45; slot++) {
         if (slot != 45) {
            class_1799 stack = player.field_7498.method_7611(slot).method_7677();
            if (isCerbHead(stack)) {
               return slot;
            }
         }
      }

      return -1;
   }

   private static class_1792 itemFor(String mode) {
      return switch (mode) {
         case "Head" -> class_1802.field_8575;
         case "Totem" -> class_1802.field_8288;
         case "Gapple" -> class_1802.field_8463;
         case "Shield" -> class_1802.field_8255;
         default -> class_1802.field_8162;
      };
   }

   private static boolean isCerbHead(class_1799 stack) {
      if (!stack.method_31574(class_1802.field_8575)) {
         return false;
      }

      class_9279 customData = (class_9279)stack.method_58694(class_9334.field_49628);
      if (customData == null) {
         return false;
      }

      class_2487 nbt = customData.method_57461();
      return !nbt.method_10545("SkullOwner")
         ? false
         : nbt.method_10580("SkullOwner")
            .toString()
            .contains(
               "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA5NWE3ZmQ5MGRhYTFiYmU3MDY5MDg5NzQwZTA1ZDBiZmM2NjI5NmVlM2M0MGVlNzFhNGUwYTY2MTZiMmJiYyJ9fX0="
            );
   }

   @Override
   protected void onDisable() {
      this.pendingSlot = -1;
   }
}
