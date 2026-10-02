package org.ryzen.menu.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.clickgui.FigmaClickGui;
import org.ryzen.menu.ui.Component;
import org.ryzen.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public final class MenuOverlayRenderer extends Component {
   private final FigmaClickGui clickGui = new FigmaClickGui();
   private final HudLayoutToolbar layoutToolbar = new HudLayoutToolbar();
   private MenuDimensions dimensions;
   private MenuOverlayState state;
   private MenuPage displayPage = MenuPage.NONE;

   public void layout(class_310 minecraft, MenuOverlayState state, int screenWidth, int screenHeight, int mouseX, int mouseY) {
      this.state = state;
      this.dimensions = MenuDimensions.resolve(minecraft, state);
      this.ensurePosition(state, screenWidth, screenHeight);
      float slideY = (1.0F - state.openProgress()) * Math.max(0.0F, screenHeight - state.panelY());
      this.configureFrame(state.panelX(), state.panelY() + slideY, this.dimensions.panelWidth(), this.dimensions.panelHeight());
      this.displayPage = state.displayPage();
      if (!state.isHudLayoutMode()) {
         this.clickGui.layout(this.x(), this.y(), this.width(), state, mouseX, mouseY);
      }
   }

   public boolean handleMouseButton(int mouseX, int mouseY, int button, MenuOverlayState state) {
      if (state.isHudLayoutMode()) {
         if (this.layoutToolbar.closeHit(mouseX, mouseY)) {
            state.setHudLayoutMode(false);
            return true;
         } else {
            return this.layoutToolbar.contains(mouseX, mouseY);
         }
      } else if (this.clickGui.mousePressed(mouseX, mouseY, button)) {
         this.displayPage = state.displayPage();
         return true;
      } else {
         return this.clickGui.contains(mouseX, mouseY) && !this.clickGui.isDragHandle(mouseX, mouseY);
      }
   }

   public void drag(int mouseX, int mouseY) {
      this.clickGui.drag(mouseX, mouseY);
   }

   public void releasePointer() {
      this.clickGui.release();
   }

   public boolean isSearchOpen() {
      return this.clickGui.isSearchOpen();
   }

   public boolean isSearchFocused() {
      return this.clickGui.isSearchFocused();
   }

   public boolean isCapturingBind() {
      return this.clickGui.isCapturingBind();
   }

   public void appendSearchCodePoint(int codePoint) {
      this.clickGui.charTyped(codePoint);
   }

   public void backspaceSearch() {
      this.clickGui.backspaceSearch();
   }

   public boolean handleKey(int key) {
      return this.clickGui.keyPressed(key);
   }

   public boolean handleCharacter(int codePoint) {
      return this.clickGui.charTyped(codePoint);
   }

   public void handleScroll(int mouseX, int mouseY, double vertical) {
      if (this.state == null || !this.state.isHudLayoutMode()) {
         this.clickGui.scroll(vertical);
      }
   }

   @Override
   public boolean isDragHandle(float mouseX, float mouseY) {
      return (this.state == null || !this.state.isHudLayoutMode()) && this.clickGui.isDragHandle(mouseX, mouseY);
   }

   @Override
   public void render(class_310 minecraft, class_332 graphics) {
      if (this.state != null && this.state.isHudLayoutMode()) {
         this.layoutToolbar.render(minecraft);
      } else {
         this.clickGui.render(minecraft, graphics);
      }
   }

   private void ensurePosition(MenuOverlayState state, int screenWidth, int screenHeight) {
      if (!state.hasPosition()) {
         state.setPanelPosition((screenWidth - this.dimensions.panelWidth()) / 2.0F, (screenHeight - this.dimensions.panelHeight()) / 2.0F);
         state.markPositionInitialized();
      }

      float maxX = Math.max(0.0F, screenWidth - this.dimensions.panelWidth());
      float maxY = Math.max(0.0F, screenHeight - this.dimensions.panelHeight());
      state.setPanelPosition(MathUtil.clamp(state.panelX(), 0.0F, maxX), MathUtil.clamp(state.panelY(), 0.0F, maxY));
   }
}
