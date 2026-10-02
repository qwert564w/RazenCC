package org.ryzen.menu.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.event.events.screen.ScreenMouseButtonEvent;

@Environment(EnvType.CLIENT)
public final class MenuOverlay {
   private static final MenuOverlayState STATE = new MenuOverlayState();
   private static final MenuDragController DRAG = new MenuDragController();
   private static final MenuOverlayRenderer RENDERER = new MenuOverlayRenderer();

   private MenuOverlay() {
   }

   public static boolean toggle(class_310 minecraft) {
      if (STATE.isInteractive()) {
         close(minecraft);
         return true;
      } else {
         open(minecraft);
         return true;
      }
   }

   public static void close(class_310 minecraft) {
      if (STATE.isOpen() && !STATE.isClosing()) {
         boolean restoreMouse = shouldReturnMouseToGame(minecraft);
         STATE.beginClose();
         DRAG.reset();
         RENDERER.releasePointer();
         if (restoreMouse) {
            minecraft.field_1729.method_1612();
         }
      }
   }

   public static boolean suspendForReload(class_310 minecraft) {
      if (!STATE.isInteractive()) {
         return false;
      }

      boolean restoreMouse = STATE.grabbedMouseBeforeOpen() && shouldReturnMouseToGame(minecraft);
      STATE.suspend();
      DRAG.reset();
      RENDERER.releasePointer();
      if (restoreMouse) {
         minecraft.field_1729.method_1612();
      }

      return true;
   }

   public static void resumeAfterReload(class_310 minecraft) {
      if (!STATE.isOpen()) {
         STATE.resume();
         DRAG.reset();
         class_304.method_1437();
         if (minecraft.field_1729.method_1613()) {
            minecraft.field_1729.method_1610();
         }
      }
   }

   public static boolean isOpen() {
      return STATE.isInteractive();
   }

   public static MenuOverlayState state() {
      return STATE;
   }

   public static boolean isVisible() {
      return STATE.isOpen();
   }

   public static boolean blocksInput() {
      return STATE.isInteractive();
   }

   public static void focusNextHeaderAction(int direction) {
      STATE.focusNextHeaderAction(direction);
   }

   public static void activateFocusedHeaderAction() {
      STATE.activateFocusedHeaderAction();
   }

   public static void closePageOrOverlay(class_310 minecraft) {
      if (STATE.isHudLayoutMode()) {
         STATE.setHudLayoutMode(false);
      } else if (STATE.page() != MenuPage.NONE) {
         STATE.openPage(MenuPage.NONE);
      } else {
         close(minecraft);
      }
   }

   public static void openSearch() {
      if (STATE.isInteractive() && STATE.page() != MenuPage.SEARCH) {
         STATE.openPage(MenuPage.SEARCH);
      }
   }

   public static boolean isSearchOpen() {
      return STATE.isInteractive() && RENDERER.isSearchOpen();
   }

   public static boolean isSearchFocused() {
      return STATE.isInteractive() && RENDERER.isSearchFocused();
   }

   public static boolean isCapturingBind() {
      return STATE.isInteractive() && RENDERER.isCapturingBind();
   }

   public static void backspaceSearch() {
      RENDERER.backspaceSearch();
   }

   public static boolean handleKey(int key) {
      return STATE.isInteractive() && RENDERER.handleKey(key);
   }

   public static boolean handleCharacter(int codePoint) {
      if (!STATE.isInteractive()) {
         return false;
      }

      if (RENDERER.handleCharacter(codePoint)) {
         return true;
      }

      if (!isSearchFocused()) {
         return false;
      }

      RENDERER.appendSearchCodePoint(codePoint);
      return true;
   }

   public static void handleScroll(double vertical) {
      if (STATE.isInteractive()) {
         RENDERER.handleScroll(mouseX(class_310.method_1551()), mouseY(class_310.method_1551()), vertical);
      }
   }

   public static boolean handleMouseButton(class_310 minecraft, int button, int action) {
      if (!STATE.isInteractive()) {
         return false;
      }

      int screenWidth = minecraft.method_22683().method_4486();
      int screenHeight = minecraft.method_22683().method_4502();
      int mouseX = mouseX(minecraft);
      int mouseY = mouseY(minecraft);
      RENDERER.layout(minecraft, STATE, screenWidth, screenHeight, mouseX, mouseY);
      if (action == 1) {
         if (RENDERER.handleMouseButton(mouseX, mouseY, button, STATE)) {
            DRAG.onRelease();
            return true;
         }

         if (button == 0) {
            DRAG.onPress(mouseX, mouseY, STATE, RENDERER);
         }
      } else if (action == 0) {
         RENDERER.releasePointer();
         if (button == 0) {
            DRAG.onRelease();
         }
      }

      return true;
   }

   public static boolean handleScreenMouseButton(class_310 minecraft, int button, ScreenMouseButtonEvent.Action action) {
      return action == ScreenMouseButtonEvent.Action.DRAG
         ? STATE.isInteractive()
         : handleMouseButton(minecraft, button, action == ScreenMouseButtonEvent.Action.RELEASE ? 0 : 1);
   }

   public static void render(class_310 minecraft, class_332 guiGraphicsExtractor, int screenWidth, int screenHeight) {
      if (STATE.isOpen()) {
         if (STATE.isClosing() && STATE.openProgress() <= 0.001F) {
            STATE.close();
         } else {
            MenuDimensions dimensions = MenuDimensions.resolve(minecraft, STATE);
            int mouseX = mouseX(minecraft);
            int mouseY = mouseY(minecraft);
            DRAG.update(mouseX, mouseY, STATE, dimensions, screenWidth, screenHeight);
            RENDERER.layout(minecraft, STATE, screenWidth, screenHeight, mouseX, mouseY);
            RENDERER.drag(mouseX, mouseY);
            RENDERER.render(minecraft, guiGraphicsExtractor);
         }
      }
   }

   private static void open(class_310 minecraft) {
      boolean grabbedMouseBeforeOpen = minecraft.field_1729.method_1613();
      STATE.open(grabbedMouseBeforeOpen);
      DRAG.reset();
      class_304.method_1437();
      if (grabbedMouseBeforeOpen) {
         minecraft.field_1729.method_1610();
      }
   }

   private static int mouseX(class_310 minecraft) {
      return (int)Math.round(minecraft.field_1729.method_68879(minecraft.method_22683()));
   }

   private static int mouseY(class_310 minecraft) {
      return (int)Math.round(minecraft.field_1729.method_68883(minecraft.method_22683()));
   }

   private static boolean shouldReturnMouseToGame(class_310 minecraft) {
      return minecraft.field_1687 != null && minecraft.field_1724 != null && minecraft.field_1755 == null;
   }
}
