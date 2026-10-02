package org.ryzen.menu.clickgui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class ClickGuiCanvas {
   public static final float DESIGN_WIDTH = 928.0F;
   public static final float DESIGN_HEIGHT = 649.0F;
   private float originX;
   private float originY;
   private float scale = 1.0F;
   private int mouseX;
   private int mouseY;

   public void configure(float x, float y, float width, int mouseX, int mouseY) {
      this.originX = x;
      this.originY = y;
      this.scale = Math.max(0.001F, width / 928.0F);
      this.mouseX = mouseX;
      this.mouseY = mouseY;
   }

   public float x(float designX) {
      return this.originX + designX * this.scale;
   }

   public float y(float designY) {
      return this.originY + designY * this.scale;
   }

   public float p(float designPixels) {
      return designPixels * this.scale;
   }

   public float designX(float screenX) {
      return (screenX - this.originX) / this.scale;
   }

   public float designY(float screenY) {
      return (screenY - this.originY) / this.scale;
   }

   public int mouseX() {
      return this.mouseX;
   }

   public int mouseY() {
      return this.mouseY;
   }

   public float mouseDesignX() {
      return this.designX(this.mouseX);
   }

   public float mouseDesignY() {
      return this.designY(this.mouseY);
   }

   public boolean hit(float left, float top, float width, float height) {
      return this.hit(this.mouseX, this.mouseY, left, top, width, height);
   }

   public boolean hit(float screenX, float screenY, float left, float top, float width, float height) {
      float dx = this.designX(screenX);
      float dy = this.designY(screenY);
      return dx >= left && dx <= left + width && dy >= top && dy <= top + height;
   }

   public void rect(float left, float top, float width, float height, int color, float radius) {
      Render2DUtil.rect(this.x(left), this.y(top), this.p(width), this.p(height)).color(color).radius(this.p(radius)).draw();
   }

   public void rect(float left, float top, float width, float height, int topLeft, int topRight, int bottomRight, int bottomLeft, float radius) {
      Render2DUtil.rect(this.x(left), this.y(top), this.p(width), this.p(height))
         .color(topLeft, topRight, bottomRight, bottomLeft)
         .radius(this.p(radius))
         .draw();
   }

   public void outlinedRect(float left, float top, float width, float height, int color, float radius, float thickness, int stroke) {
      Render2DUtil.rect(this.x(left), this.y(top), this.p(width), this.p(height))
         .color(color)
         .radius(this.p(radius))
         .border(Math.max(0.5F, this.p(thickness)), stroke)
         .draw();
   }

   public void shadowedRect(float left, float top, float width, float height, int color, float radius, int shadowColor, float blur) {
      Render2DUtil.rect(this.x(left), this.y(top), this.p(width), this.p(height)).color(color).radius(this.p(radius)).shadow(shadowColor, this.p(blur)).draw();
   }

   public void blurredRect(float left, float top, float width, float height, int color, float radius, float blur, float opacity, int stroke) {
      Render2DUtil.rect(this.x(left), this.y(top), this.p(width), this.p(height))
         .color(color)
         .radius(this.p(radius))
         .border(Math.max(0.5F, this.p(0.5F)), stroke)
         .blur(this.p(blur), opacity)
         .draw();
   }

   public void text(float left, float top, float size, String value, int color) {
      this.text(left, top, size, value, color, UiFontStyle.REGULAR, TextAlign.LEFT);
   }

   public void text(float left, float top, float size, String value, int color, UiFontStyle style) {
      this.text(left, top, size, value, color, style, TextAlign.LEFT);
   }

   public void text(float left, float top, float size, String value, int color, UiFontStyle style, TextAlign align) {
      Render2DUtil.text(this.x(left), this.y(top), this.p(size), value == null ? "" : value)
         .family(UiFonts.suisse())
         .weight(style.weight())
         .style(UiFontStyle.REGULAR)
         .align(align)
         .color(color)
         .draw();
   }

   public float textWidth(String value, float size, UiFontStyle style) {
      return UiFonts.suisse().resolve(style.weight()).measureWidth(value == null ? "" : value, this.p(size), 0.0F) / this.scale;
   }

   public void stationText(float left, float top, float size, String value, int color, UiFontStyle style) {
      this.stationText(left, top, size, value, color, style, TextAlign.LEFT);
   }

   public void stationText(float left, float top, float size, String value, int color, UiFontStyle style, TextAlign align) {
      Render2DUtil.text(this.x(left), this.y(top), this.p(size), value == null ? "" : value)
         .family(UiFonts.stationText())
         .weight(style.weight())
         .style(UiFontStyle.REGULAR)
         .align(align)
         .color(color)
         .draw();
   }

   public float stationTextWidth(String value, float size, UiFontStyle style) {
      return UiFonts.stationText().resolve(style.weight()).measureWidth(value == null ? "" : value, this.p(size), 0.0F) / this.scale;
   }

   public void stationIcon(float left, float top, float size, String glyph, int color) {
      this.stationIcon(left, top, size, glyph, color, TextAlign.LEFT);
   }

   public void stationIcon(float left, float top, float size, String glyph, int color, TextAlign align) {
      Render2DUtil.text(this.x(left), this.y(top), this.p(size), glyph == null ? "" : glyph)
         .family(UiFonts.stationIcons())
         .weight(400)
         .style(UiFontStyle.REGULAR)
         .align(align)
         .color(color)
         .draw();
   }

   public float stationIconWidth(String glyph, float size) {
      return UiFonts.stationIcons().regular().measureWidth(glyph == null ? "" : glyph, this.p(size), 0.0F) / this.scale;
   }

   public void texture(float left, float top, float width, float height, class_2960 id, int color) {
      Render2DUtil.texture(this.x(left), this.y(top), this.p(width), this.p(height), id).color(color).draw();
   }

   public void texture(float left, float top, float width, float height, class_2960 id, int color, float radius) {
      Render2DUtil.texture(this.x(left), this.y(top), this.p(width), this.p(height), id).radius(this.p(radius)).color(color).draw();
   }

   public void managedTexture(float left, float top, float width, float height, class_2960 id, int color, float radius) {
      Render2DUtil.texture(this.x(left), this.y(top), this.p(width), this.p(height), id).managed().smooth().radius(this.p(radius)).color(color).draw();
   }

   public void pushScissor(float left, float top, float width, float height) {
      Render2DUtil.pushScissor(this.x(left), this.y(top), this.p(width), this.p(height));
   }

   public void popScissor() {
      Render2DUtil.popScissor();
   }

   public int alpha(int color, float alpha) {
      return ColorUtil.multiplyAlpha(color, alpha);
   }
}
