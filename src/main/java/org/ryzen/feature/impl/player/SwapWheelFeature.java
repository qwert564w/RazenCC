package org.ryzen.feature.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_490;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.event.events.screen.ScreenCloseEvent;
import org.ryzen.event.events.screen.ScreenMouseButtonEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.menu.core.MenuConfigStore;
import org.ryzen.menu.core.MenuOverlay;
import org.ryzen.mixin.accessor.AbstractContainerScreenAccessor;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.inventory.InventorySwap;
import org.ryzen.utils.inventory.InventoryUtil;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;

@Environment(EnvType.CLIENT)
public final class SwapWheelFeature extends Feature implements MinecraftContext {
   private static final class_2960 HOTBAR_SPRITE = class_2960.method_60656("hud/hotbar");
   private static final class_2960 HOTBAR_SELECTION_SPRITE = class_2960.method_60656("hud/hotbar_selection");
   private static final int HOTBAR_TEXTURE_WIDTH = 182;
   private static final int HOTBAR_TEXTURE_HEIGHT = 22;
   private static final int SLOT_SIZE = 22;
   private static final int SLOT_GAP = 10;
   private static final int INVENTORY_SLOTS_START = 9;
   private static final int INVENTORY_SLOTS_END = 45;
   private static final float DIRECTION_DEAD_ZONE = 10.0F;
   private static final int MAX_SLOTS = 8;
   private static final long TAP_MS = 250L;
   private static final int PENDING_MAX_TICKS = 40;
   private static final String CONFIG_KEY_PREFIX = "autoswap.item.";
   public final InputBindSetting key = this.register(new InputBindSetting("Key", 82));
   public final NumberSetting slots = this.register(new NumberSetting("Slots", 4.0, 2.0, 8.0, 1.0, ""));
   public final BooleanSetting strict = this.register(new BooleanSetting("Strict", false));
   private final class_1792[] assignedItems = new class_1792[8];
   private boolean assignedLoaded;
   private boolean spaceOpen;
   private boolean editMode;
   private long pressTimeMs;
   private int hoveredSlot = -1;
   private int pickTargetSlot = -1;
   private int pendingContainerSlot = -1;
   private int pendingTicks;

   public SwapWheelFeature() {
      super("SwapWheel", "Pick a hotbar item on screen while holding a key", FeatureCategory.PLAYER, -1);
      this.renamedFrom("AutoSwap");
   }

   @Override
   protected void onDisable() {
      this.closeSpace();
      this.pickTargetSlot = -1;
      this.pendingContainerSlot = -1;
      this.pendingTicks = 0;
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (this.key.matches(event.getKey()) && this.handleBindAction(event.getAction())) {
         event.cancel();
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      if (this.key.matchesMouse(event.getButton()) && this.handleBindAction(event.getAction())) {
         event.cancel();
      } else if (this.spaceOpen) {
         if (this.editMode && event.getAction() == 1 && this.hoveredSlot >= 0) {
            if (event.getButton() == 0) {
               this.beginAssign(this.hoveredSlot);
            } else if (event.getButton() == 1) {
               this.setAssignedItem(this.hoveredSlot, null);
            }
         }

         event.cancel();
      }
   }

   private boolean handleBindAction(int action) {
      if (action == 1) {
         if (this.spaceOpen && this.editMode) {
            this.closeSpace();
            return true;
         } else {
            this.pressTimeMs = System.currentTimeMillis();
            return this.openSpace();
         }
      } else if (action == 0 && this.spaceOpen) {
         if (this.editMode) {
            return true;
         } else if (System.currentTimeMillis() - this.pressTimeMs < 250L) {
            this.editMode = true;
            return true;
         } else {
            int hovered = this.hoveredSlot;
            this.closeSpace();
            this.applyHoveredSlot(hovered);
            return true;
         }
      } else {
         return action == 2 && this.spaceOpen;
      }
   }

   @EventTarget
   public void onScreenMouseButton(ScreenMouseButtonEvent event) {
      if (this.pickTargetSlot >= 0 && event.getAction() == ScreenMouseButtonEvent.Action.CLICK && event.getScreen() instanceof class_490 screen) {
         class_1735 hovered = ((AbstractContainerScreenAccessor)screen).getHoveredSlot();
         if (hovered != null && hovered.method_7681() && hovered.field_7874 >= 9 && hovered.field_7874 < 45) {
            this.setAssignedItem(this.pickTargetSlot, hovered.method_7677().method_7909());
            this.pickTargetSlot = -1;
            event.cancel();
            mc.method_1507(null);
         }
      }
   }

   @EventTarget
   public void onScreenClose(ScreenCloseEvent event) {
      if (event.getScreen() instanceof class_490) {
         this.pickTargetSlot = -1;
      }
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      if (this.spaceOpen) {
         class_310 mc = event.getClient();
         class_746 player = mc.field_1724;
         if (player != null && mc.field_1755 == null && !MenuOverlay.isOpen()) {
            class_332 extractor = event.getGuiGraphicsExtractor();
            int count = this.slotCount();
            float mouseX = (float)mc.field_1729.method_68879(mc.method_22683());
            float mouseY = (float)mc.field_1729.method_68883(mc.method_22683());
            this.hoveredSlot = -1;
            int[][] offsets = slotOffsets(count);
            int centerX = extractor.method_51421() / 2;
            int centerY = extractor.method_51443() / 2;
            this.hoveredSlot = this.findHoveredSlot(offsets, count, centerX, centerY, mouseX, mouseY);

            for (int index = 0; index < count; index++) {
               int x = centerX + offsets[index][0] - 11;
               int y = centerY + offsets[index][1] - 11;
               this.drawSlot(extractor, player, index, x, y, this.hoveredSlot == index);
            }
         } else {
            this.closeSpace();
         }
      }
   }

   private int findHoveredSlot(int[][] offsets, int count, int centerX, int centerY, float mouseX, float mouseY) {
      for (int index = 0; index < count; index++) {
         int x = centerX + offsets[index][0] - 11;
         int y = centerY + offsets[index][1] - 11;
         if (mouseX >= x && mouseX < x + 22 && mouseY >= y && mouseY < y + 22) {
            return index;
         }
      }

      if (!this.strict.getValue() && !this.editMode) {
         float dragX = mouseX - centerX;
         float dragY = mouseY - centerY;
         if (dragX * dragX + dragY * dragY < 100.0F) {
            return -1;
         }

         int best = -1;
         double bestAlignment = -Double.MAX_VALUE;
         double dragLength = Math.sqrt(dragX * dragX + dragY * dragY);

         for (int index = 0; index < count; index++) {
            double slotLength = Math.sqrt((double)offsets[index][0] * offsets[index][0] + (double)offsets[index][1] * offsets[index][1]);
            if (!(slotLength < 0.001)) {
               double alignment = (dragX * offsets[index][0] + dragY * offsets[index][1]) / (dragLength * slotLength);
               if (alignment > bestAlignment) {
                  bestAlignment = alignment;
                  best = index;
               }
            }
         }

         return best;
      } else {
         return -1;
      }
   }

   private static int[][] slotOffsets(int count) {
      int step = 32;

      return switch (count) {
         case 2 -> new int[][]{{-step, 0}, {step, 0}};
         case 3 -> ring(3, Math.round(step * 1.3F), -90.0F);
         case 4 -> new int[][]{{-step, -step}, {step, -step}, {-step, step}, {step, step}};
         case 5, 6 -> ring(count, Math.round(step * 1.55F), -90.0F);
         case 7 -> ring(7, Math.round(step * 1.85F), -90.0F);
         default -> new int[][]{
            {0, -step * 2 - 8},
            {step + 4, -step - 4},
            {step * 2 + 8, 0},
            {step + 4, step + 4},
            {0, step * 2 + 8},
            {-step - 4, step + 4},
            {-step * 2 - 8, 0},
            {-step - 4, -step - 4}
         };
      };
   }

   private static int[][] ring(int count, int radius, float startAngleDeg) {
      int[][] offsets = new int[count][2];

      for (int index = 0; index < count; index++) {
         double angle = Math.toRadians(startAngleDeg + index * 360.0 / count);
         offsets[index][0] = (int)Math.round(Math.cos(angle) * radius);
         offsets[index][1] = (int)Math.round(Math.sin(angle) * radius);
      }

      return offsets;
   }

   private void drawSlot(class_332 extractor, class_746 player, int slot, int x, int y, boolean hovered) {
      int half = 11;
      extractor.method_70846(class_10799.field_56883, HOTBAR_SPRITE, 182, 22, 0, 0, x, y, half, 22);
      extractor.method_70846(class_10799.field_56883, HOTBAR_SPRITE, 182, 22, 182 - half, 0, x + half, y, half, 22);
      if (hovered) {
         int accent = Theme.getAccent();
         Render2DUtil.rect(x + 1, y + 1, 20.0F, 20.0F).color(ColorUtil.withAlpha(accent, 90)).draw();
         Render2DUtil.flush();
         extractor.method_52707(class_10799.field_56883, HOTBAR_SELECTION_SPRITE, x - 1, y - 1, 24, 23, accent);
      }

      class_1792 item = this.assignedItem(slot);
      if (item != null) {
         class_1799 stack = new class_1799(item);
         extractor.method_51427(stack, x + 3, y + 3);
         extractor.method_51431(mc.field_1772, stack, x + 3, y + 3);
      }
   }

   private boolean openSpace() {
      if (!this.spaceOpen && this.inGame() && this.screen() == null && !MenuOverlay.isOpen() && mc.field_1729.method_1613()) {
         mc.field_1729.method_1610();
         this.spaceOpen = true;
         this.hoveredSlot = -1;
         this.pickTargetSlot = -1;
         return true;
      } else {
         return false;
      }
   }

   private void applyHoveredSlot(int hovered) {
      class_746 player = this.player();
      if (player != null && hovered >= 0 && hovered < this.slotCount() && !PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.INVENTORY)) {
         class_1792 item = this.assignedItem(hovered);
         if (item == null) {
            this.beginAssign(hovered);
         } else if (!player.method_6079().method_31574(item)) {
            int containerSlot = findItemSlot(player, item);
            if (containerSlot >= 0) {
               if (InventorySwap.isBusy()) {
                  this.pendingContainerSlot = containerSlot;
               } else {
                  InventorySwap.equip(containerSlot);
               }
            }
         }
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (this.pendingContainerSlot >= 0) {
         if (InventorySwap.isBusy()) {
            if (++this.pendingTicks > 40) {
               this.pendingContainerSlot = -1;
               this.pendingTicks = 0;
            }
         } else {
            class_746 player = event.getClient().field_1724;
            if (player != null && player.field_7512 == player.field_7498) {
               InventorySwap.equip(this.pendingContainerSlot);
            }

            this.pendingContainerSlot = -1;
            this.pendingTicks = 0;
         }
      }
   }

   private static int findItemSlot(class_746 player, class_1792 item) {
      return InventoryUtil.findPlayerMenuSlot(player, stack -> stack.method_31574(item));
   }

   private void beginAssign(int slot) {
      class_746 player = this.player();
      if (player != null) {
         this.pickTargetSlot = slot;
         this.closeSpace();
         mc.method_1507(new class_490(player));
      }
   }

   private void closeSpace() {
      if (this.spaceOpen) {
         this.spaceOpen = false;
         this.editMode = false;
         this.hoveredSlot = -1;
         if (this.inGame() && this.screen() == null && !MenuOverlay.isOpen()) {
            mc.field_1729.method_1612();
         }
      }
   }

   private int slotCount() {
      return (int)Math.round(this.slots.getValue());
   }

   private class_1792 assignedItem(int slot) {
      this.ensureAssignedLoaded();
      return slot >= 0 && slot < 8 ? this.assignedItems[slot] : null;
   }

   private void setAssignedItem(int slot, class_1792 item) {
      this.ensureAssignedLoaded();
      if (slot >= 0 && slot < 8) {
         this.assignedItems[slot] = item;
         String id = item == null ? "" : class_7923.field_41178.method_10221(item).toString();
         MenuConfigStore.save(data -> data.addProperty("autoswap.item." + slot, id));
      }
   }

   private void ensureAssignedLoaded() {
      if (!this.assignedLoaded) {
         this.assignedLoaded = true;

         for (int slot = 0; slot < 8; slot++) {
            String id = MenuConfigStore.getString("autoswap.item." + slot, "");
            if (!id.isEmpty()) {
               class_1792 item = (class_1792)class_7923.field_41178.method_63535(class_2960.method_60654(id));
               this.assignedItems[slot] = item == class_1802.field_8162 ? null : item;
            }
         }
      }
   }
}
