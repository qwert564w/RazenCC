package org.ryzen.menu.core;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.math.MathUtil;
import org.ryzen.utils.render.Theme;

@Environment(EnvType.CLIENT)
public final class MenuOverlayState {
   private static final long CONTENT_ANIMATION_MS = 180L;
   private static final long OPEN_ANIMATION_MS = 320L;
   private static final long CLOSE_ANIMATION_MS = 240L;
   public static final float MIN_UI_SCALE = 0.525F;
   public static final float MAX_UI_SCALE = 2.1F;
   public static final float MIN_UI_SCALE_PERCENT = 50.0F;
   public static final float MAX_UI_SCALE_PERCENT = 200.0F;
   public static final float DEFAULT_UI_SCALE_VALUE = 0.30158734F;
   public static final int DEFAULT_PANEL_RADIUS = 20;
   private final Animation contentAnimation = new Animation(180L, Animation.Easing.EASE_OUT_QUAD);
   private final Animation openAnimation = new Animation(0L, Animation.Easing.EASE_OUT_QUAD);
   private boolean open;
   private boolean closing;
   private boolean hudLayoutMode;
   private boolean grabbedMouseBeforeOpen;
   private boolean positionInitialized;
   private float panelX;
   private float panelY;
   private MenuPage page = MenuPage.NONE;
   private MenuPage lastPage = MenuPage.NONE;
   private int focusedHeaderAction = -1;
   private float uiScaleValue = MenuConfigStore.loadUiScaleValue();
   private float panelRadius = Theme.Sizes.PANEL_RADII[MathUtil.clamp(MenuConfigStore.getInt("roundedMode", 2), 0, Theme.Sizes.PANEL_RADII.length - 1)];
   private final List<MenuPage> backHistory = new ArrayList<>();
   private final List<MenuPage> forwardHistory = new ArrayList<>();
   private boolean traversingHistory;

   public boolean isHudLayoutMode() {
      return this.hudLayoutMode;
   }

   public void setHudLayoutMode(boolean hudLayoutMode) {
      this.hudLayoutMode = hudLayoutMode;
   }

   public boolean isOpen() {
      return this.open;
   }

   public void open(boolean grabbedMouseBeforeOpen) {
      float from = this.open ? this.openProgress() : 0.0F;
      this.open = true;
      this.closing = false;
      this.grabbedMouseBeforeOpen = grabbedMouseBeforeOpen;
      this.positionInitialized = false;
      this.openAnimation.animate(from, 1.0F, 320L, Animation.Easing.EASE_OUT_QUAD);
   }

   public void beginClose() {
      if (this.open && !this.closing) {
         this.closing = true;
         this.openAnimation.animate(this.openProgress(), 0.0F, 240L, Animation.Easing.EASE_IN_QUAD);
      }
   }

   public void suspend() {
      this.open = false;
      this.closing = false;
      this.openAnimation.animate(0.0F, 0.0F, 0L, Animation.Easing.EASE_OUT_QUAD);
   }

   public void resume() {
      this.open = true;
      this.closing = false;
      this.openAnimation.animate(0.0F, 1.0F, 320L, Animation.Easing.EASE_OUT_QUAD);
   }

   public void close() {
      this.open = false;
      this.closing = false;
      this.grabbedMouseBeforeOpen = false;
      this.page = MenuPage.NONE;
      this.lastPage = MenuPage.NONE;
      this.contentAnimation.animate(0.0F, 0.0F, 0L, Animation.Easing.EASE_OUT_QUAD);
      this.openAnimation.animate(0.0F, 0.0F, 0L, Animation.Easing.EASE_OUT_QUAD);
      this.focusedHeaderAction = -1;
      this.backHistory.clear();
      this.forwardHistory.clear();
   }

   public boolean isClosing() {
      return this.closing;
   }

   public boolean isInteractive() {
      return this.open && !this.closing;
   }

   public float openProgress() {
      return this.openAnimation.getValue();
   }

   public boolean grabbedMouseBeforeOpen() {
      return this.grabbedMouseBeforeOpen;
   }

   public float panelX() {
      return this.panelX;
   }

   public float panelY() {
      return this.panelY;
   }

   public MenuPage page() {
      return this.page;
   }

   public MenuPage displayPage() {
      return this.page == MenuPage.NONE && !(this.contentProgress() <= 0.001F) ? this.lastPage : this.page;
   }

   public float contentProgress() {
      return this.contentAnimation.getValue();
   }

   public int focusedHeaderAction() {
      return this.focusedHeaderAction;
   }

   public float uiScaleValue() {
      return this.uiScaleValue;
   }

   public void setUiScaleValue(float uiScaleValue) {
      this.uiScaleValue = MathUtil.clamp01(uiScaleValue);
      MenuConfigStore.saveUiScaleValue(this.uiScaleValue);
   }

   public float panelRadius() {
      return this.panelRadius;
   }

   public void setPanelRadius(float panelRadius) {
      this.panelRadius = Math.max(0.0F, panelRadius);
   }

   public void focusNextHeaderAction(int direction) {
      if (this.focusedHeaderAction < 0) {
         this.focusedHeaderAction = direction < 0 ? MenuPage.actionCount() - 1 : 0;
      } else {
         this.focusedHeaderAction = Math.floorMod(this.focusedHeaderAction + direction, MenuPage.actionCount());
      }
   }

   public void activateFocusedHeaderAction() {
      if (this.focusedHeaderAction >= 0) {
         this.openPage(MenuPage.actionAt(this.focusedHeaderAction));
      }
   }

   public void openPage(MenuPage requestedPage) {
      if (requestedPage != null) {
         if (requestedPage != this.page) {
            if (!this.traversingHistory) {
               this.backHistory.add(this.page);
               this.forwardHistory.clear();
            }

            if (requestedPage == MenuPage.NONE) {
               this.page = MenuPage.NONE;
               this.focusedHeaderAction = -1;
               this.contentAnimation.animate(this.contentProgress(), 0.0F, 180L, Animation.Easing.EASE_OUT_QUAD);
            } else {
               this.page = requestedPage;
               this.lastPage = requestedPage;
               this.focusedHeaderAction = requestedPage.actionIndex();
               this.contentAnimation.animate(0.0F, 1.0F, 180L, Animation.Easing.EASE_OUT_QUAD);
            }
         }
      }
   }

   public boolean canGoBack() {
      return !this.backHistory.isEmpty();
   }

   public boolean canGoForward() {
      return !this.forwardHistory.isEmpty();
   }

   public void goBack() {
      if (this.canGoBack()) {
         this.traversingHistory = true;

         try {
            MenuPage target = this.backHistory.remove(this.backHistory.size() - 1);
            this.forwardHistory.add(this.page);
            this.openPage(target);
         } finally {
            this.traversingHistory = false;
         }
      }
   }

   public void goForward() {
      if (this.canGoForward()) {
         this.traversingHistory = true;

         try {
            MenuPage target = this.forwardHistory.remove(this.forwardHistory.size() - 1);
            this.backHistory.add(this.page);
            this.openPage(target);
         } finally {
            this.traversingHistory = false;
         }
      }
   }

   public boolean hasPosition() {
      return this.positionInitialized;
   }

   public void markPositionInitialized() {
      this.positionInitialized = true;
   }

   public void setPanelPosition(float panelX, float panelY) {
      this.panelX = panelX;
      this.panelY = panelY;
   }
}
