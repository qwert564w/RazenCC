package org.ryzen.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_408;
import net.minecraft.class_490;
import org.ryzen.menu.core.MenuConfigStore;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.math.MathUtil;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public abstract class HudElement {
   private static final long APPEAR_MS = 200L;
   private final String id;
   private final String displayName;
   private final String configKeyX;
   private final String configKeyY;
   protected float x;
   protected float y;
   protected float width;
   protected float height;
   private boolean dragging;
   private float dragOffsetX;
   private float dragOffsetY;
   private boolean wasHidden = true;
   private long appearStart;
   private boolean positionInitialized;
   private int lastScreenWidth = -1;
   private int lastScreenHeight = -1;
   private static boolean showcaseForced;
   protected static final int DIVIDER_COLOR = HudPalette.DIVIDER;
   protected static final float CARD_RADIUS = 13.0F;
   protected static final float CARD_HEADER = 38.0F;
   protected static final float CARD_INSET = 5.0F;
   protected static final float CARD_FOOTER = 5.0F;

   protected HudElement(String id, String displayName) {
      this.id = id;
      this.displayName = displayName;
      this.configKeyX = "hud." + id + ".x";
      this.configKeyY = "hud." + id + ".y";
   }

   public final String id() {
      return this.id;
   }

   public final String displayName() {
      return this.displayName;
   }

   public final void render(class_310 mc, float unit) {
      this.layout(mc, unit);
      if (!(this.width <= 0.5F) && !(this.height <= 0.5F)) {
         if (this.wasHidden) {
            this.wasHidden = false;
            this.appearStart = System.currentTimeMillis();
         }

         int screenWidth = mc.method_22683().method_4486();
         int screenHeight = mc.method_22683().method_4502();
         float freeWidth = Math.max(0.0F, screenWidth - this.width);
         float freeHeight = Math.max(0.0F, screenHeight - this.height);
         boolean contentResizeOnly = this.preservePositionOnContentResize()
            && this.positionInitialized
            && screenWidth == this.lastScreenWidth
            && screenHeight == this.lastScreenHeight;
         if (!this.dragging && !contentResizeOnly) {
            this.x = MenuConfigStore.getFloat(this.configKeyX, this.defaultX(unit) / Math.max(1.0F, freeWidth)) * freeWidth;
            this.y = MenuConfigStore.getFloat(this.configKeyY, this.defaultY(unit) / Math.max(1.0F, freeHeight)) * freeHeight;
         }

         this.positionInitialized = true;
         this.lastScreenWidth = screenWidth;
         this.lastScreenHeight = screenHeight;
         this.x = contentResizeOnly && !this.dragging ? Math.max(0.0F, this.x) : MathUtil.clamp(this.x, 0.0F, freeWidth);
         this.y = MathUtil.clamp(this.y, 0.0F, freeHeight);
         if (this.centerHorizontally()) {
            this.x = freeWidth / 2.0F;
         }

         this.draw(mc, unit);
      } else {
         this.wasHidden = true;
      }
   }

   protected boolean centerHorizontally() {
      return false;
   }

   protected boolean preservePositionOnContentResize() {
      return false;
   }

   protected final float appearAlpha() {
      float progress = MathUtil.clamp01((float)(System.currentTimeMillis() - this.appearStart) / 200.0F);
      return progress * progress * (3.0F - 2.0F * progress);
   }

   protected final float[] drawCard(float unit, float alpha, float headerUnits, class_2960 headerIcon, String headerTitle) {
      return this.drawCard(unit, alpha, headerUnits, headerIcon, headerTitle, 5.0F, 5.0F);
   }

   protected final float[] drawCard(float unit, float alpha, float headerUnits, class_2960 headerIcon, String headerTitle, float insetUnits, float footerUnits) {
      float radius = 13.0F * unit;
      Render2DUtil.rect(this.x, this.y, this.width, this.height)
         .color(ColorUtil.multiplyAlpha(HudPalette.PANEL, alpha))
         .radius(radius)
         .blur(50.0F * unit, alpha)
         .draw();
      float headerH = headerUnits * unit;
      if (headerH > 0.5F && headerIcon != null) {
         float iconSize = 16.0F * unit;
         float iconX = this.x + 12.0F * unit;
         float iconY = this.y + (headerH - iconSize) / 2.0F;
         Render2DUtil.texture(iconX, iconY, iconSize, iconSize, headerIcon).color(ColorUtil.multiplyAlpha(HudPalette.accent(), alpha)).draw();
         float titleSize = 12.0F * unit;
         Render2DUtil.text(iconX + iconSize + 8.0F * unit, UiFonts.sfProDisplay().centeredTextY(this.y + headerH / 2.0F, titleSize), titleSize, headerTitle)
            .style(UiFontStyle.MEDIUM)
            .color(ColorUtil.multiplyAlpha(-1, alpha))
            .draw();
      }

      float innerX = this.x + insetUnits * unit;
      float innerY = this.y + headerH;
      float innerW = this.width - insetUnits * 2.0F * unit;
      float innerH = this.height - headerH - (headerH > 0.5F ? footerUnits : insetUnits) * unit;
      if (headerH > 0.5F) {
         Render2DUtil.rect(innerX, innerY, innerW, innerH)
            .color(ColorUtil.multiplyAlpha(HudPalette.SURFACE, alpha))
            .radius(13.0F * unit)
            .border(0.5F * unit, ColorUtil.multiplyAlpha(HudPalette.SURFACE_BORDER, alpha))
            .draw();
      }

      return new float[]{innerX, innerY, innerW, innerH};
   }

   protected final void drawPanel(float radius, float unit, float alpha) {
      drawPanel(this.x, this.y, this.width, this.height, radius, unit, alpha);
   }

   protected static void drawPanel(float x, float y, float width, float height, float radius, float unit, float alpha) {
      Render2DUtil.rect(x, y, width, height).color(ColorUtil.multiplyAlpha(HudPalette.PANEL, alpha)).radius(radius).blur(50.0F * unit, alpha).draw();
   }

   protected final float divider(float cursor, float centerY, float heightUnits, float gapUnits, float unit, float alpha) {
      cursor += gapUnits * unit;
      Render2DUtil.rect(cursor, centerY - heightUnits * unit / 2.0F, 1.0F * unit, heightUnits * unit)
         .color(ColorUtil.multiplyAlpha(DIVIDER_COLOR, alpha))
         .draw();
      return cursor + (1.0F + gapUnits) * unit;
   }

   protected abstract void layout(class_310 var1, float var2);

   protected float defaultX(float unit) {
      return 20.0F * unit;
   }

   protected float defaultY(float unit) {
      return 20.0F * unit;
   }

   protected abstract void draw(class_310 var1, float var2);

   protected boolean hasHeaderCloseButton() {
      return false;
   }

   public final boolean headerCloseHit(float mouseX, float mouseY, float unit) {
      if (!this.hasHeaderCloseButton()) {
         return false;
      }

      float hitSize = 24.0F * unit;
      float hitX = this.x + this.width - 30.0F * unit;
      float hitY = this.y + 6.0F * unit;
      return mouseX >= hitX && mouseX <= hitX + hitSize && mouseY >= hitY && mouseY <= hitY + hitSize;
   }

   protected static boolean showcase(class_310 mc) {
      return showcaseForced || mc.field_1755 instanceof class_408 || mc.field_1755 instanceof class_490;
   }

   public static void setShowcaseForced(boolean forced) {
      showcaseForced = forced;
   }

   public final void renderPreview(class_310 mc, float posX, float posY, float unit) {
      this.layout(mc, unit);
      if (!(this.width <= 0.5F) && !(this.height <= 0.5F)) {
         this.x = posX;
         this.y = posY;
         this.wasHidden = false;
         this.appearStart = 0L;
         this.draw(mc, unit);
      }
   }

   public final float[] measurePreview(class_310 mc, float unit) {
      this.layout(mc, unit);
      return new float[]{this.width, this.height};
   }

   public final boolean startDrag(float mouseX, float mouseY) {
      if (!(mouseX < this.x) && !(mouseX > this.x + this.width) && !(mouseY < this.y) && !(mouseY > this.y + this.height)) {
         this.dragging = true;
         this.dragOffsetX = mouseX - this.x;
         this.dragOffsetY = mouseY - this.y;
         return true;
      } else {
         return false;
      }
   }

   public final void dragTo(float mouseX, float mouseY) {
      if (!this.centerHorizontally()) {
         this.x = mouseX - this.dragOffsetX;
      }

      this.y = mouseY - this.dragOffsetY;
   }

   public final boolean isDragging() {
      return this.dragging;
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

   public final boolean isHorizontallyCentered() {
      return this.centerHorizontally();
   }

   public final void snapTo(float x, float y) {
      if (this.dragging) {
         if (!this.centerHorizontally()) {
            this.x = x;
         }

         this.y = y;
      }
   }

   public final boolean overlaps(float x, float y, float width, float height) {
      return this.width > 0.5F && this.height > 0.5F && x < this.x + this.width && x + width > this.x && y < this.y + this.height && y + height > this.y;
   }

   public final void resetPosition() {
      this.dragging = false;
      this.positionInitialized = false;
      this.lastScreenWidth = -1;
      this.lastScreenHeight = -1;
   }

   public final void stopDrag(class_310 mc) {
      if (this.dragging) {
         this.dragging = false;
         float freeWidth = Math.max(1.0F, mc.method_22683().method_4486() - this.width);
         float freeHeight = Math.max(1.0F, mc.method_22683().method_4502() - this.height);
         float fractionX = MathUtil.clamp01(this.x / freeWidth);
         float fractionY = MathUtil.clamp01(this.y / freeHeight);
         MenuConfigStore.save(data -> {
            data.addProperty(this.configKeyX, fractionX);
            data.addProperty(this.configKeyY, fractionY);
         });
      }
   }
}
