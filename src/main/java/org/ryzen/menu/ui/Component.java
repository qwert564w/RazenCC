package org.ryzen.menu.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public abstract class Component {
   private static final int DESIGN_WIDTH = 1024;
   private Component frame;
   private double designScale = 1.0;
   private float x;
   private float y;
   private float width;
   private float height;

   public final void configureFrame(float x, float y, float width, float height) {
      this.frame = this;
      this.designScale = width / 1024.0;
      this.setBounds(x, y, width, height);
   }

   public final void attach(Component frame, float x, float y, float width, float height) {
      this.frame = frame;
      this.designScale = frame.designScale;
      this.setBounds(x, y, width, height);
   }

   public final float x() {
      return this.x;
   }

   public final float y() {
      return this.y;
   }

   public final float width() {
      return this.width;
   }

   public final float height() {
      return this.height;
   }

   public final float px(float designPixels) {
      return (float)(designPixels * this.designScale);
   }

   protected final Component frame() {
      return this.frame;
   }

   public final float sx(float designX) {
      return this.frame.x + this.px(designX);
   }

   public final float sy(float designY) {
      return this.frame.y + this.px(designY);
   }

   public final float designX(float screenX) {
      return (float)((screenX - this.frame.x) / this.designScale);
   }

   public final float designY(float screenY) {
      return (float)((screenY - this.frame.y) / this.designScale);
   }

   public final boolean hit(float mouseX, float mouseY, float x, float y, float width, float height) {
      return mouseX >= this.sx(x) && mouseX <= this.sx(x + width) && mouseY >= this.sy(y) && mouseY <= this.sy(y + height);
   }

   public boolean contains(float mouseX, float mouseY) {
      return Render2DUtil.isPointScissored(mouseX, mouseY)
         ? false
         : mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
   }

   public boolean isDragHandle(float mouseX, float mouseY) {
      return false;
   }

   public boolean handleClick(int mouseX, int mouseY) {
      return false;
   }

   protected static class_2960 menuTexturePng(String path) {
      return class_2960.method_60654("ryzen:textures/menu/" + path + ".png");
   }

   protected final int alpha(int color, float alpha) {
      return alpha >= 0.999F ? color : ColorUtil.multiplyAlpha(color, alpha);
   }

   protected final void rect(float x, float y, float width, float height, int color, float radius) {
      Render2DUtil.rect(this.sx(x), this.sy(y), this.px(width), this.px(height)).color(color).radius(this.px(radius)).draw();
   }

   protected final void rect(float x, float y, float width, float height, int color, float radius, float alpha) {
      this.rect(x, y, width, height, this.alpha(color, alpha), radius);
   }

   protected final void outline(float x, float y, float width, float height, int color, float radius, float thickness) {
      Render2DUtil.rect(this.sx(x), this.sy(y), this.px(width), this.px(height))
         .color(0)
         .radius(this.px(radius))
         .border(Math.max(0.5F, this.px(thickness)), color)
         .draw();
   }

   protected final void outline(float x, float y, float width, float height, int color, float radius, float thickness, float alpha) {
      this.outline(x, y, width, height, this.alpha(color, alpha), radius, thickness);
   }

   protected final void glassPanel(float x, float y, float width, float height, float radius, float shadowBlur, float alpha) {
      Render2DUtil.rect(x, y, width, height)
         .glass(alpha, this.px(0.5F), this.px(8.0F))
         .radius(radius)
         .shadow(ColorUtil.multiplyAlpha(Theme.Colors.POPUP_SHADOW, alpha), shadowBlur)
         .draw();
   }

   protected final float centeredTextY(float centerY, float size) {
      return UiFonts.sfProDisplay().centeredTextY(centerY, size);
   }

   protected final void text(float x, float y, float size, String value, int color) {
      Render2DUtil.text(this.sx(x), this.sy(y), this.px(size), value).color(color).draw();
   }

   protected final void text(float x, float y, float size, String value, int color, float alpha) {
      this.text(x, y, size, value, this.alpha(color, alpha));
   }

   protected final void text(float x, float y, float size, String value, int color, float alpha, UiFontStyle style) {
      Render2DUtil.text(this.sx(x), this.sy(y), this.px(size), value).style(style).color(this.alpha(color, alpha)).draw();
   }

   protected final void text(float x, float y, float size, String value, int color, UiFontStyle style) {
      this.text(x, y, size, value, color, 1.0F, style);
   }

   protected final void textCentered(float x, float y, float size, String value, int color) {
      Render2DUtil.text(this.sx(x), this.sy(y), this.px(size), value).color(color).align(TextAlign.CENTER).draw();
   }

   protected final void textCentered(float x, float y, float size, String value, int color, float alpha) {
      this.textCentered(x, y, size, value, this.alpha(color, alpha));
   }

   protected final void textCentered(float x, float y, float size, String value, int color, float alpha, UiFontStyle style) {
      Render2DUtil.text(this.sx(x), this.sy(y), this.px(size), value).style(style).color(this.alpha(color, alpha)).align(TextAlign.CENTER).draw();
   }

   protected final void textRight(float x, float y, float size, String value, int color) {
      Render2DUtil.text(this.sx(x), this.sy(y), this.px(size), value).color(color).align(TextAlign.RIGHT).draw();
   }

   protected final void textRight(float x, float y, float size, String value, int color, float alpha) {
      this.textRight(x, y, size, value, this.alpha(color, alpha));
   }

   protected final void textRight(float x, float y, float size, String value, int color, float alpha, UiFontStyle style) {
      Render2DUtil.text(this.sx(x), this.sy(y), this.px(size), value).style(style).color(this.alpha(color, alpha)).align(TextAlign.RIGHT).draw();
   }

   protected final void texture(float x, float y, float width, float height, class_2960 texture, int color) {
      Render2DUtil.texture(this.sx(x), this.sy(y), this.px(width), this.px(height), texture).color(color).draw();
   }

   protected final void texture(float x, float y, float width, float height, class_2960 texture, int color, float alpha) {
      this.texture(x, y, width, height, texture, this.alpha(color, alpha));
   }

   protected final void texture(float x, float y, float size, class_2960 texture, int color) {
      this.texture(x, y, size, size, texture, color);
   }

   protected final void texture(float x, float y, float size, class_2960 texture, int color, float alpha) {
      this.texture(x, y, size, size, texture, color, alpha);
   }

   private void setBounds(float x, float y, float width, float height) {
      this.x = x;
      this.y = y;
      this.width = Math.max(0.0F, width);
      this.height = Math.max(0.0F, height);
   }

   public abstract void render(class_310 var1, class_332 var2);
}
