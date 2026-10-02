package org.ryzen.utils.inventory;

import java.util.ArrayDeque;
import java.util.Deque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1723;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_4185;
import net.minecraft.class_490;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.screen.ScreenCloseEvent;
import org.ryzen.feature.impl.movement.InventoryMoveFeature;

@Environment(EnvType.CLIENT)
public final class DropAllInventoryController implements MinecraftContext {
   public static final DropAllInventoryController INSTANCE = new DropAllInventoryController();
   private static final class_2561 DROP_ALL = class_2561.method_43470("Выбросить всё");
   private static final class_2561 STOP = class_2561.method_43470("Остановить");
   private final Deque<Integer> slots = new ArrayDeque<>();
   private class_490 screen;
   private class_1723 menu;
   private class_4185 button;
   private boolean running;

   private DropAllInventoryController() {
   }

   public static void bindButton(class_4185 button) {
      INSTANCE.button = button;
      INSTANCE.updateButton();
   }

   public static void toggle(class_490 screen) {
      if (INSTANCE.running) {
         INSTANCE.cancel();
      } else {
         INSTANCE.start(screen);
      }
   }

   public static boolean blocksInventoryOperations() {
      return INSTANCE.running || InventoryMoveFeature.isClickPipelineBusy();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (this.running) {
         class_310 client = event.getClient();
         if (client != null
            && client.field_1724 != null
            && client.field_1755 == this.screen
            && client.field_1724.field_7512 == this.menu
            && !InventoryUtil.hasCarriedItem()) {
            if (!InventorySwap.isBusy()) {
               while (!this.slots.isEmpty()) {
                  int slotId = this.slots.removeFirst();
                  if (this.menu.method_40442(slotId) && this.menu.method_7611(slotId).method_7681()) {
                     InventoryUtil.dropStack(slotId);
                     return;
                  }
               }

               this.finish();
            }
         } else {
            this.cancel();
         }
      }
   }

   @EventTarget
   public void onScreenClose(ScreenCloseEvent event) {
      if (event.getScreen() == this.screen) {
         this.cancel();
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.cancel();
   }

   private void start(class_490 screen) {
      class_310 client = class_310.method_1551();
      if (client != null
         && client.field_1724 != null
         && screen != null
         && client.field_1724.field_7512 == screen.method_17577()
         && !InventoryUtil.hasCarriedItem()
         && !InventorySwap.isBusy()) {
         this.screen = screen;
         this.menu = (class_1723)screen.method_17577();
         this.slots.clear();

         for (int slotId = 0; slotId < this.menu.field_7761.size(); slotId++) {
            if (this.menu.method_7611(slotId).method_7681()) {
               this.slots.addLast(slotId);
            }
         }

         this.running = !this.slots.isEmpty();
         this.updateButton();
      }
   }

   private void finish() {
      this.running = false;
      this.slots.clear();
      this.updateButton();
   }

   private void cancel() {
      this.finish();
      this.screen = null;
      this.menu = null;
      this.button = null;
   }

   private void updateButton() {
      if (this.button != null) {
         this.button.method_25355(this.running ? STOP : DROP_ALL);
      }
   }
}
