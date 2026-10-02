package org.ryzen.menu.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.menu.clickgui.ClickGuiPalette;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class HudLayoutToolbar {
   private static final float WIDTH = 360.0F;
   private static final float HEIGHT = 56.0F;
   private static final float MARGIN_BOTTOM = 28.0F;
   private static final float CLOSE_WIDTH = 92.0F;
   private static final float CLOSE_HEIGHT = 32.0F;
   private float x;
   private float y;

   public void render(class_310 minecraft) {
      if (minecraft != null) {
         float screenWidth = minecraft.method_22683().method_4486();
         float screenHeight = minecraft.method_22683().method_4502();
         this.x = (screenWidth - 360.0F) / 2.0F;
         this.y = screenHeight - 56.0F - 28.0F;
         Render2DUtil.rect(this.x, this.y, 360.0F, 56.0F).color(ClickGuiPalette.WINDOW).radius(18.0F).border(0.5F, ClickGuiPalette.STROKE).draw();
         Render2DUtil.texture(this.x + 20.0F, this.y + 14.0F, 14.0F, 14.0F, Textures.Header.HUD).color(ClickGuiPalette.accent()).draw();
         Render2DUtil.text(this.x + 44.0F, this.y + 11.0F, 13.0F, MenuText.ui("HUD layout")).style(UiFontStyle.MEDIUM).color(-1).draw();
         Render2DUtil.text(this.x + 44.0F, this.y + 30.0F, 10.0F, MenuText.ui("ESC")).color(ClickGuiPalette.TEXT_MUTED).draw();
         float closeX = this.x + 360.0F - 92.0F - 16.0F;
         float closeY = this.y + 12.0F;
         Render2DUtil.rect(closeX, closeY, 92.0F, 32.0F).color(ClickGuiPalette.accent()).radius(10.0F).draw();
         Render2DUtil.text(closeX + 46.0F, closeY + 9.0F, 12.0F, MenuText.ui("Close")).style(UiFontStyle.MEDIUM).align(TextAlign.CENTER).color(-1).draw();
      }
   }

   public boolean closeHit(float mouseX, float mouseY) {
      float closeX = this.x + 360.0F - 92.0F - 16.0F;
      float closeY = this.y + 12.0F;
      return mouseX >= closeX && mouseX <= closeX + 92.0F && mouseY >= closeY && mouseY <= closeY + 32.0F;
   }

   public boolean contains(float mouseX, float mouseY) {
      return mouseX >= this.x && mouseX <= this.x + 360.0F && mouseY >= this.y && mouseY <= this.y + 56.0F;
   }
}
