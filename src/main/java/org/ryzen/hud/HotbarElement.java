package org.ryzen.hud;

import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5251;
import net.minecraft.class_746;
import net.minecraft.class_9779;
import org.joml.Matrix3x2fStack;
import org.ryzen.context.RenderContext;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.math.MathUtil;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class HotbarElement extends HudElement {
   private static final int SLOT_COUNT = 9;
   private static final float DESIGN_WIDTH = 295.0F;
   private static final float DESIGN_HEIGHT = 45.0F;
   private static final float SLOT_LEFT = 9.0F;
   private static final float SLOT_TOP = 7.0F;
   private static final float SLOT_PITCH = 31.0F;
   private static final float SELECTION_SIZE = 32.0F;
   private static final float ITEM_SIZE = 18.0F;
   private static final float ITEM_INSET = 7.0F;
   private static final float SELECTION_RADIUS = 9.0F;
   private static final float OFFHAND_GAP = 6.0F;
   private static final float OFFHAND_SIZE = 32.0F;
   private static final float OFFHAND_RADIUS = 9.0F;
   private static final float BOTTOM_MARGIN = 38.0F;
   private static final float NAME_TEXT_SIZE = 10.0F;
   private static final Pattern FORMATTING_CODE = Pattern.compile("(?i)§[0-9A-FK-ORX]");
   private static final float ITEM_COUNT_TEXT_SIZE = 8.0F;
   private static final float ITEM_COUNT_RIGHT = 17.0F;
   private static final float ITEM_COUNT_BOTTOM = 17.0F;
   private static final long NAME_FADE_MS = 500L;
   private static final float XP_TEXT_SIZE = 10.0F;
   private static final int XP_COLOR = ColorUtil.rgb(128, 255, 32);
   private static final float VANILLA_HOTBAR_HEIGHT = 22.0F;
   private static final float VANILLA_STATS_HALF_WIDTH = 91.0F;
   private static final float VANILLA_STATS_ROW_CENTER = 12.5F;
   private static final float VANILLA_NAME_OFFSET = 49.0F;
   private static final float VANILLA_NAME_OFFSET_CREATIVE = 23.0F;
   private static final float STATS_ROW_DROP = 5.0F;
   private float screenHeight;
   private float smoothedSlot;
   private boolean slotInitialized;
   private long lastFrameTime;
   private class_1799 lastHighlight = class_1799.field_8037;
   private long highlightEnd;
   private float renderedTop = Float.NaN;
   private float statsHalfWidth;

   public HotbarElement() {
      super("hotbar", "Hotbar");
   }

   @Override
   protected boolean centerHorizontally() {
      return false;
   }

   @Override
   protected float defaultX(float unit) {
      return 871.0F * unit;
   }

   @Override
   protected void layout(class_310 mc, float unit) {
      this.screenHeight = mc.method_22683().method_4502();
      class_746 player = mc.field_1724;
      if (player != null && !player.method_7325()) {
         this.width = 295.0F * unit;
         this.height = 45.0F * unit;
         this.statsHalfWidth = this.width / 2.0F - 9.0F * unit;
      } else {
         this.width = 0.0F;
         this.height = 0.0F;
         this.statsHalfWidth = 0.0F;
      }
   }

   @Override
   protected float defaultY(float unit) {
      return this.screenHeight - this.height - 38.0F * unit;
   }

   @Override
   protected void draw(class_310 mc, float unit) {
      class_746 player = mc.field_1724;
      if (player != null) {
         this.renderedTop = this.y;
         float alpha = this.appearAlpha();
         Render2DUtil.rect(this.x, this.y, this.width, this.height)
            .color(ColorUtil.multiplyAlpha(HudPalette.PANEL, alpha))
            .radius(13.0F * unit)
            .blur(50.0F * unit, alpha)
            .draw();
         float cellsX = this.x + 9.0F * unit;
         float cellsY = this.y + 7.0F * unit;
         float selection = this.smoothSelection(player.method_31548().method_67532());
         float selectionX = cellsX + selection * 31.0F * unit;
         Render2DUtil.rect(selectionX, cellsY, 32.0F * unit, 32.0F * unit)
            .color(ColorUtil.multiplyAlpha(HudPalette.accent(), alpha))
            .radius(9.0F * unit)
            .draw();
         this.drawXpLevel(mc, player, unit, alpha);
         this.drawSelectedItemName(mc, player, unit, alpha);
         class_1799 offhand = player.method_6079();
         float offhandX = Float.NaN;
         if (!offhand.method_7960()) {
            float capsuleSize = 32.0F * unit;
            float capsuleY = this.y + (this.height - capsuleSize) / 2.0F;
            boolean left = player.method_6068().method_5928() == class_1306.field_6182;
            float capsuleX = left ? this.x - 6.0F * unit - capsuleSize : this.x + this.width + 6.0F * unit;
            Render2DUtil.rect(capsuleX, capsuleY, capsuleSize, capsuleSize)
               .color(ColorUtil.multiplyAlpha(HudPalette.PANEL, alpha))
               .radius(9.0F * unit)
               .blur(50.0F * unit, alpha)
               .draw();
            offhandX = capsuleX + (capsuleSize - 18.0F * unit) / 2.0F;
         }

         if (!(alpha < 0.75F)) {
            class_332 extractor = RenderContext.currentGuiGraphicsExtractor();
            if (extractor != null) {
               Render2DUtil.flush();
               float itemY = this.y + 14.0F * unit;
               int seed = 1;

               for (int slot = 0; slot < 9; slot++) {
                  this.drawItem(mc, extractor, player, player.method_31548().method_5438(slot), cellsX + slot * 31.0F * unit + 7.0F * unit, itemY, unit, seed++);
               }

               if (!offhand.method_7960()) {
                  this.drawItem(mc, extractor, player, offhand, offhandX, this.y + (this.height - 18.0F * unit) / 2.0F, unit, seed);
               }
            }
         }
      }
   }

   public float decorationOffsetX(class_310 mc) {
      if (Float.isNaN(this.renderedTop)) {
         return 0.0F;
      }

      float panelCenter = this.x + this.width / 2.0F;
      float vanillaCenter = mc.method_22683().method_4486() / 2.0F;
      return panelCenter - vanillaCenter;
   }

   public float decorationOffsetY(class_310 mc) {
      return Float.isNaN(this.renderedTop) ? 0.0F : this.y - (mc.method_22683().method_4502() - 22.0F) + bandDrop(mc.field_1724) * this.decorationScale(mc);
   }

   public float decorationScale(class_310 mc) {
      return this.statsHalfWidth > 0.5F ? this.statsHalfWidth / 91.0F : 1.0F;
   }

   private static float bandDrop(class_746 player) {
      boolean bandFree = player != null && player.method_45773() == null && !player.field_3944.method_70936().method_70952();
      return bandFree ? 5.0F : 0.0F;
   }

   public float statsHalfWidth() {
      return this.statsHalfWidth;
   }

   private void drawXpLevel(class_310 mc, class_746 player, float unit, float alpha) {
      if (mc.field_1761.method_2913() && player.field_7520 > 0) {
         MsdfFont font = UiFonts.sfProDisplay();
         String level = Integer.toString(player.field_7520);
         float textSize = 10.0F * unit;
         float centerX = this.x + this.width / 2.0F;
         float scale = this.decorationScale(mc);
         float centerY = this.y - 12.5F * scale + bandDrop(player) * scale;
         Render2DUtil.text(centerX, font.centeredTextY(centerY, textSize), textSize, level)
            .style(UiFontStyle.MEDIUM)
            .color(ColorUtil.multiplyAlpha(XP_COLOR, alpha))
            .align(TextAlign.CENTER)
            .draw();
      }
   }

   private void drawSelectedItemName(class_310 mc, class_746 player, float unit, float panelAlpha) {
      float alpha = this.highlightAlpha(mc, player) * panelAlpha;
      if (!(alpha <= 0.01F) && !this.lastHighlight.method_7960()) {
         String name = FORMATTING_CODE.matcher(this.lastHighlight.method_7964().getString()).replaceAll("").trim();
         if (!name.isEmpty()) {
            MsdfFont font = UiFonts.sfProDisplay();
            float textSize = 10.0F * unit;
            float centerX = this.x + this.width / 2.0F;
            float nameOffset = mc.field_1761.method_2908() ? 49.0F : 23.0F;
            float scale = this.decorationScale(mc);
            float centerY = this.y - nameOffset * scale + bandDrop(player) * scale + textSize / 2.0F;
            class_5251 rarityColor = class_5251.method_27718(this.lastHighlight.method_7932().method_58413());
            int color = rarityColor != null ? 0xFF000000 | rarityColor.method_27716() : -1;
            Render2DUtil.text(centerX, font.centeredTextY(centerY, textSize), textSize, name)
               .style(UiFontStyle.MEDIUM)
               .color(ColorUtil.multiplyAlpha(color, alpha))
               .align(TextAlign.CENTER)
               .draw();
         }
      }
   }

   private float highlightAlpha(class_310 mc, class_746 player) {
      class_1799 selected = player.method_31548().method_7391();
      long now = System.currentTimeMillis();
      if (selected.method_7960()) {
         this.highlightEnd = 0L;
      } else if (this.lastHighlight.method_7960()
         || !selected.method_31574(this.lastHighlight.method_7909())
         || !selected.method_7964().equals(this.lastHighlight.method_7964())) {
         this.highlightEnd = now + (long)(2000.0 * (Double)mc.field_1690.method_48191().method_41753());
      }

      this.lastHighlight = selected;
      return MathUtil.clamp01((float)(this.highlightEnd - now) / 500.0F);
   }

   private void drawItem(class_310 mc, class_332 extractor, class_746 player, class_1799 stack, float x, float y, float unit, int seed) {
      if (!stack.method_7960()) {
         Matrix3x2fStack pose = extractor.method_51448();
         float guiScale = mc.method_22683().method_4495();
         float intendedSize = 18.0F * unit;
         float snappedSize;
         if (intendedSize < 15.99F) {
            snappedSize = intendedSize;
         } else {
            float texelPixels = Math.max(1.0F, Math.round(intendedSize * guiScale / 16.0F));
            snappedSize = texelPixels * 16.0F / guiScale;
         }

         float scale = snappedSize / 16.0F;
         float centerOffset = (intendedSize - snappedSize) / 2.0F;
         float itemX = Math.round((x + centerOffset) * guiScale) / guiScale;
         float itemY = Math.round((y + centerOffset) * guiScale) / guiScale;
         pose.pushMatrix();
         pose.translate(itemX, itemY);
         pose.scale(scale);
         float pop = popProgress(stack);
         if (pop > 0.0F) {
            float squeeze = 1.0F + pop / 5.0F;
            pose.pushMatrix();
            pose.translate(8.0F, 12.0F);
            pose.scale(1.0F / squeeze, (squeeze + 1.0F) / 2.0F);
            pose.translate(-8.0F, -12.0F);
         }

         extractor.method_51423(player, stack, 0, 0, seed);
         if (pop > 0.0F) {
            pose.popMatrix();
         }

         extractor.method_51432(mc.field_1772, stack, 0, 0, "");
         drawItemCount(stack);
         pose.popMatrix();
      }
   }

   private static void drawItemCount(class_1799 stack) {
      if (stack.method_7947() != 1) {
         String amount = Integer.toString(stack.method_7947());
         MsdfFont font = UiFonts.sfProDisplay();
         float textY = 17.0F - font.textHeight(8.0F);
         Render2DUtil.text(17.0F, textY, 8.0F, amount).font(font).style(UiFontStyle.SEMIBOLD).color(-1).align(TextAlign.RIGHT).draw();
      }
   }

   private static float popProgress(class_1799 stack) {
      if (stack.method_7965() <= 0) {
         return 0.0F;
      }

      class_9779 deltaTracker = RenderContext.currentDeltaTracker();
      float partial = deltaTracker != null ? deltaTracker.method_60637(false) : 0.0F;
      return stack.method_7965() - partial;
   }

   private float smoothSelection(int selectedSlot) {
      long now = System.currentTimeMillis();
      float delta = this.lastFrameTime == 0L ? 0.016F : (float)Math.min(100L, now - this.lastFrameTime) / 1000.0F;
      this.lastFrameTime = now;
      if (!this.slotInitialized) {
         this.slotInitialized = true;
         this.smoothedSlot = selectedSlot;
      }

      float step = MathUtil.clamp01(delta * 20.0F);
      this.smoothedSlot = this.smoothedSlot + (selectedSlot - this.smoothedSlot) * step;
      if (Math.abs(this.smoothedSlot - selectedSlot) < 0.005F) {
         this.smoothedSlot = selectedSlot;
      }

      return this.smoothedSlot;
   }
}
