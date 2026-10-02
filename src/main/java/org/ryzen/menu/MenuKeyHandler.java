package org.ryzen.menu;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.input.CharacterInputEvent;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.event.events.screen.ScreenKeyEvent;
import org.ryzen.event.events.screen.ScreenMouseButtonEvent;
import org.ryzen.menu.core.MenuOverlay;
import org.ryzen.menu.ui.controls.MenuClipboard;

@Environment(EnvType.CLIENT)
public final class MenuKeyHandler implements MinecraftContext {
   @EventTarget(priority = 1000)
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (event.getKey() != 300) {
         if (event.getAction() != 1 || event.getKey() != 344 || MenuOverlay.isOpen() && MenuOverlay.isCapturingBind()) {
            if (MenuOverlay.isOpen()) {
               boolean pressOrRepeat = event.getAction() == 1 || event.getAction() == 2;
               if (pressOrRepeat && MenuOverlay.handleKey(event.getKey())) {
                  event.cancel();
               } else if (event.getAction() == 1 && event.getKey() == 344) {
                  event.cancel();
               } else {
                  if (pressOrRepeat && event.getKey() == 256) {
                     MenuOverlay.closePageOrOverlay(mc);
                  } else if (pressOrRepeat && event.getKey() == 259 && MenuOverlay.isSearchFocused()) {
                     MenuOverlay.backspaceSearch();
                  } else if (event.getAction() == 1 && event.getKey() == 70 && MenuClipboard.shortcutDown()) {
                     MenuOverlay.openSearch();
                  } else if (event.getAction() != 1 || event.getKey() != 258 && event.getKey() != 262) {
                     if (event.getAction() == 1 && event.getKey() == 263) {
                        MenuOverlay.focusNextHeaderAction(-1);
                     } else if (event.getAction() == 1 && (event.getKey() == 257 || event.getKey() == 32)) {
                        MenuOverlay.activateFocusedHeaderAction();
                     }
                  } else {
                     MenuOverlay.focusNextHeaderAction(1);
                  }

                  event.cancel();
               }
            }
         } else {
            if (MenuOverlay.toggle(mc)) {
               event.cancel();
            }
         }
      }
   }

   @EventTarget(priority = 1000)
   public void onCharacterInput(CharacterInputEvent event) {
      if (MenuOverlay.handleCharacter(event.getCodePoint())) {
         event.cancel();
      }
   }

   @EventTarget(priority = 1000)
   public void onMouseInput(MouseInputEvent event) {
      if (MenuOverlay.handleMouseButton(mc, event.getButton(), event.getAction())) {
         event.cancel();
      }
   }

   @EventTarget(priority = 1000)
   public void onScreenKey(ScreenKeyEvent event) {
      if (event.getKeyEvent() == null || event.getKeyEvent().comp_4795() != 300) {
         if (MenuOverlay.blocksInput()) {
            event.cancel();
         }
      }
   }

   @EventTarget(priority = 1000)
   public void onScreenMouseButton(ScreenMouseButtonEvent event) {
      if (MenuOverlay.handleScreenMouseButton(mc, event.getMouseButtonEvent().method_74245(), event.getAction())) {
         event.cancel();
      }
   }
}
