package org.ryzen.menu.clickgui;

import java.nio.file.Files;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_156;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.cosmetics.CosmeticEntry;
import org.ryzen.utils.cosmetics.CosmeticsRepository;
import org.ryzen.utils.cosmetics.FiguraBridge;
import org.ryzen.utils.math.MathUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class ClickGuiCosmeticsPage {
   private static final float PANEL_X = 94.0F;
   private static final float PANEL_Y = 67.0F;
   private static final float PANEL_WIDTH = 821.0F;
   private static final float PANEL_HEIGHT = 546.0F;
   private static final float CONTENT_X = 115.0F;
   private static final float CONTENT_RIGHT = 893.0F;
   private static final float TAB_Y = 118.0F;
   private static final float TAB_HEIGHT = 30.0F;
   private static final float TAB_GAP = 8.0F;
   private static final float ACTION_SIZE = 30.0F;
   private static final float BODY_TOP = 164.0F;
   private static final float BODY_BOTTOM = 594.0F;
   private static final float BODY_HEIGHT = 430.0F;
   private static final float PREVIEW_WIDTH = 320.0F;
   private static final float PREVIEW_X = 573.0F;
   private static final float PREVIEW_GAP = 18.0F;
   private static final int COLUMNS = 3;
   private static final float GRID_WIDTH = 440.0F;
   private static final float CARD_GAP = 12.0F;
   private static final float CARD_WIDTH = 138.66667F;
   private static final float CARD_HEIGHT = 134.0F;
   private static final float CARD_STEP = 146.0F;
   private static final float THUMB_INSET = 8.0F;
   private static final float THUMB_HEIGHT = 96.0F;
   private static final float THUMB_WIDTH = 122.66667F;
   private static final float MODEL_SIZE = 250.0F;
   private static final float MODEL_X = 608.0F;
   private static final float MODEL_Y = 188.0F;
   private static final float CAPTION_CENTER = 733.0F;
   private static final float BUTTON_WIDTH = 264.0F;
   private static final float BUTTON_X = 601.0F;
   private static final float BUTTON_Y = 536.0F;
   private static final float BUTTON_HEIGHT = 40.0F;
   private static final float RESET_SIZE = 24.0F;
   private static final float RESET_X = 834.0F;
   private static final float RESET_Y = 188.0F;
   private static final long STATUS_DURATION_MS = 2600L;
   private static final float MAX_ICON_UPSCALE = 48.0F;
   private static final float EASE_PER_SECOND = 12.0F;
   private static final CosmeticEntry.Kind[] TABS = CosmeticEntry.Kind.values();
   private static final class_2960[] TAB_ICONS = new class_2960[]{Textures.Icons.PERSON_STANDING, Textures.Icons.USER_ROUND, Textures.Icons.SWORDS};
   private final CosmeticPreviewModel preview = new CosmeticPreviewModel();
   private CosmeticEntry.Kind tab = CosmeticEntry.Kind.MODEL;
   private List<CosmeticEntry> entries = List.of();
   private CosmeticEntry selected;
   private CosmeticEntry hovered;
   private float scrollOffset;
   private float maxScroll;
   private final float[] tabWidths = new float[TABS.length];
   private final float[] tabPositions = new float[TABS.length];
   private float unequipX;
   private float unequipWidth;
   private float[] cardHover = new float[0];
   private float tabSlideX;
   private float tabSlideWidth;
   private float captionFade = 1.0F;
   private CosmeticEntry fadingFrom;
   private float equipPulse;
   private float resetFade;
   private long lastFrameNanos;
   private String status = "";
   private boolean statusOk;
   private long statusUntil;
   private boolean scanned;
   private boolean entriesDirty = true;

   public void layout(ClickGuiCanvas canvas) {
      if (!this.scanned) {
         CosmeticsRepository.rescan();
         this.scanned = true;
         this.entriesDirty = true;
      }

      if (this.entriesDirty) {
         this.entriesDirty = false;
         this.entries = CosmeticsRepository.of(this.tab);
         if (this.selected != null && !this.entries.contains(this.selected)) {
            this.selected = null;
         }

         this.cardHover = new float[this.entries.size()];
         int rows = (this.entries.size() + 3 - 1) / 3;
         float contentHeight = rows == 0 ? 0.0F : rows * 134.0F + (rows - 1) * 12.0F;
         this.maxScroll = Math.max(0.0F, contentHeight - 430.0F);
      }

      this.scrollOffset = clamp(this.scrollOffset, 0.0F, this.maxScroll);
      this.measureTabs(canvas);
      this.advance(canvas);
   }

   private void advance(ClickGuiCanvas canvas) {
      long now = System.nanoTime();
      float delta = this.lastFrameNanos == 0L ? 0.016666668F : (float)((now - this.lastFrameNanos) / 1.0E9);
      this.lastFrameNanos = now;
      delta = MathUtil.clamp(delta, 0.0F, 0.1F);
      float ease = 1.0F - (float)Math.exp(-12.0F * delta);

      for (int index = 0; index < this.cardHover.length; index++) {
         float cardY = this.cardY(index);
         boolean visible = cardY + 134.0F >= 164.0F && cardY <= 594.0F;
         float target = visible && canvas.hit(cardX(index), cardY, 138.66667F, 134.0F) ? 1.0F : 0.0F;
         this.cardHover[index] = this.cardHover[index] + (target - this.cardHover[index]) * ease;
      }

      int active = this.tab.ordinal();
      if (this.tabSlideWidth <= 0.0F) {
         this.tabSlideX = this.tabPositions[active];
         this.tabSlideWidth = this.tabWidths[active];
      } else {
         this.tabSlideX = this.tabSlideX + (this.tabPositions[active] - this.tabSlideX) * ease;
         this.tabSlideWidth = this.tabSlideWidth + (this.tabWidths[active] - this.tabSlideWidth) * ease;
      }

      this.captionFade = Math.min(1.0F, this.captionFade + delta * 5.5F);
      this.equipPulse = Math.max(0.0F, this.equipPulse - delta * 1.6F);
      float resetTarget = this.preview.isTouched() ? 1.0F : 0.0F;
      this.resetFade = this.resetFade + (resetTarget - this.resetFade) * ease;
      this.preview.tick(delta);
   }

   public void render(ClickGuiCanvas canvas, class_332 graphics) {
      canvas.outlinedRect(94.0F, 67.0F, 821.0F, 546.0F, ClickGuiPalette.SURFACE, 17.0F, 0.5F, ClickGuiPalette.STROKE);
      this.renderHeader(canvas);
      this.renderTabs(canvas);
      this.hovered = null;
      this.renderGrid(canvas);
      this.renderPreview(canvas, graphics);
   }

   private void renderHeader(ClickGuiCanvas canvas) {
      canvas.texture(115.0F, 86.0F, 14.0F, 14.0F, Textures.Icons.SHIRT, ClickGuiPalette.accent());
      canvas.text(140.0F, 85.0F, 15.0F, MenuText.ui("Cosmetics"), -1, UiFontStyle.MEDIUM);
      boolean figura = FiguraBridge.isAvailable();
      String hint = figura ? MenuText.ui("Avatars from config/ryzen/cosmetics") : MenuText.ui("Figura is not installed - avatars will not apply");
      canvas.text(893.0F, 89.0F, 11.0F, hint, figura ? ClickGuiPalette.TEXT_MUTED : ColorUtil.rgba(255, 168, 120, 217), UiFontStyle.REGULAR, TextAlign.RIGHT);
   }

   private void renderTabs(ClickGuiCanvas canvas) {
      canvas.outlinedRect(
         this.tabSlideX,
         118.0F,
         this.tabSlideWidth,
         30.0F,
         ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.16F),
         15.0F,
         0.5F,
         ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.55F)
      );

      for (int index = 0; index < TABS.length; index++) {
         float left = this.tabPositions[index];
         boolean active = TABS[index] == this.tab;
         boolean hover = canvas.hit(left, 118.0F, this.tabWidths[index], 30.0F);
         if (!active && hover) {
            canvas.rect(left, 118.0F, this.tabWidths[index], 30.0F, ClickGuiPalette.CONTROL_HOVER, 15.0F);
         }

         canvas.texture(left + 13.0F, 127.0F, 11.0F, 12.0F, TAB_ICONS[index], active ? ClickGuiPalette.accent() : ClickGuiPalette.ICON_INACTIVE);
         canvas.text(left + 30.0F, 127.0F, 12.0F, tabLabel(TABS[index]), active ? -1 : ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM);
      }

      int applied = FiguraBridge.appliedId().isEmpty() ? ClickGuiPalette.TEXT_SECONDARY : -1;
      boolean unequipHover = canvas.hit(this.unequipX, 118.0F, this.unequipWidth, 30.0F);
      canvas.outlinedRect(
         this.unequipX,
         118.0F,
         this.unequipWidth,
         30.0F,
         unequipHover ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.CONTROL,
         15.0F,
         0.5F,
         ClickGuiPalette.CONTROL_STROKE
      );
      canvas.text(this.unequipX + this.unequipWidth / 2.0F, 127.0F, 12.0F, MenuText.ui("Unequip"), applied, UiFontStyle.MEDIUM, TextAlign.CENTER);
      this.drawActionButton(canvas, folderX(), Textures.Icons.FOLDER, 15.0F, 11.382F);
      this.drawActionButton(canvas, refreshX(), Textures.Icons.REFRESH_CCW, 13.0F, 13.0F);
   }

   private void drawActionButton(ClickGuiCanvas canvas, float left, class_2960 icon, float iconWidth, float iconHeight) {
      boolean hover = canvas.hit(left, 118.0F, 30.0F, 30.0F);
      canvas.outlinedRect(
         left, 118.0F, 30.0F, 30.0F, hover ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.CONTROL, 15.0F, 0.5F, ClickGuiPalette.CONTROL_STROKE
      );
      canvas.texture(
         left + (30.0F - iconWidth) / 2.0F, 118.0F + (30.0F - iconHeight) / 2.0F, iconWidth, iconHeight, icon, hover ? -1 : ClickGuiPalette.ICON_INACTIVE
      );
   }

   private void renderGrid(ClickGuiCanvas canvas) {
      if (this.entries.isEmpty()) {
         this.renderEmptyState(canvas);
      } else {
         canvas.pushScissor(115.0F, 164.0F, 440.0F, 430.0F);

         for (int index = 0; index < this.entries.size(); index++) {
            float cardY = this.cardY(index);
            if (cardY + 134.0F >= 164.0F && cardY <= 594.0F) {
               this.renderCard(canvas, this.entries.get(index), index, cardX(index), cardY);
            }
         }

         canvas.popScissor();
      }
   }

   private void renderCard(ClickGuiCanvas canvas, CosmeticEntry entry, int index, float left, float top) {
      float hover = this.cardHover[index];
      if (canvas.hit(left, top, 138.66667F, 134.0F)) {
         this.hovered = entry;
      }

      boolean applied = FiguraBridge.isApplied(entry);
      boolean marked = applied || entry == this.selected;
      int fill = ColorUtil.lerp(ClickGuiPalette.SURFACE, ClickGuiPalette.CONTROL_HOVER, hover);
      int stroke = applied
         ? ClickGuiPalette.accent()
         : (
            marked
               ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.5F)
               : ColorUtil.lerp(ClickGuiPalette.CARD_STROKE, ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.35F), hover)
         );
      canvas.outlinedRect(left, top, 138.66667F, 134.0F, fill, 14.0F, 0.5F, stroke);
      if (applied && this.equipPulse > 0.001F) {
         float spread = (1.0F - this.equipPulse) * 7.0F;
         canvas.outlinedRect(
            left - spread,
            top - spread,
            138.66667F + spread * 2.0F,
            134.0F + spread * 2.0F,
            0,
            14.0F + spread,
            1.0F,
            ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), this.equipPulse * 0.8F)
         );
      }

      float grow = hover * 0.05F;
      float thumbWidth = 122.66667F * (1.0F + grow);
      float thumbHeight = 96.0F * (1.0F + grow);
      this.drawThumb(
         canvas, entry, left + 8.0F - (thumbWidth - 122.66667F) / 2.0F, top + 8.0F - (thumbHeight - 96.0F) / 2.0F, thumbWidth, thumbHeight, 10.0F, 1.0F
      );
      if (applied) {
         float badge = left + 138.66667F - 8.0F - 22.0F;
         canvas.rect(badge, top + 8.0F + 6.0F, 16.0F, 16.0F, ClickGuiPalette.accent(), 8.0F);
         canvas.texture(badge + 4.8F, top + 8.0F + 11.6F, 6.343F, 4.75F, Textures.Icons.CHECK, -1);
      }

      canvas.text(
         left + 69.333336F,
         top + 134.0F - 22.0F,
         11.0F,
         fit(canvas, entry.displayName(), 122.66667F, 11.0F, UiFontStyle.MEDIUM),
         applied ? ClickGuiPalette.accent() : ColorUtil.lerp(ClickGuiPalette.TEXT_STRONG, -1, hover),
         UiFontStyle.MEDIUM,
         TextAlign.CENTER
      );
   }

   private void drawThumb(ClickGuiCanvas canvas, CosmeticEntry entry, float left, float top, float width, float height, float radius, float alpha) {
      class_2960 preview = CosmeticsRepository.preview(entry);
      if (preview != null && entry.previewWidth() > 0 && entry.previewHeight() > 0) {
         canvas.rect(left, top, width, height, ColorUtil.multiplyAlpha(ClickGuiPalette.CONTROL, alpha * 0.7F), radius);
         float scale = Math.min(width / entry.previewWidth(), height / entry.previewHeight());
         float longestSide = Math.max(entry.previewWidth(), entry.previewHeight());
         scale = Math.min(scale, Math.max(1.0F, 48.0F / longestSide));
         float drawWidth = entry.previewWidth() * scale;
         float drawHeight = entry.previewHeight() * scale;
         canvas.managedTexture(
            left + (width - drawWidth) / 2.0F,
            top + (height - drawHeight) / 2.0F,
            drawWidth,
            drawHeight,
            preview,
            ColorUtil.multiplyAlpha(-1, alpha),
            Math.min(radius, 6.0F)
         );
      } else {
         canvas.rect(left, top, width, height, ColorUtil.multiplyAlpha(ClickGuiPalette.CONTROL, alpha), radius);
         if (entry != null) {
            class_2960 glyph = TAB_ICONS[entry.kind().ordinal()];
            float size = Math.min(28.0F, height * 0.32F);
            canvas.texture(left + (width - size) / 2.0F, top + (height - size) / 2.0F, size, size, glyph, ColorUtil.rgba(255, 255, 255, (int)(38.0F * alpha)));
         }
      }
   }

   private void renderEmptyState(ClickGuiCanvas canvas) {
      float centerX = 335.0F;
      float centerY = 379.0F;
      canvas.rect(centerX - 27.0F, centerY - 74.0F, 54.0F, 54.0F, ClickGuiPalette.CONTROL, 27.0F);
      canvas.texture(centerX - 11.0F, centerY - 58.0F, 22.0F, 22.0F, Textures.Icons.SHIRT, ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.75F));
      canvas.text(
         centerX,
         centerY - 6.0F,
         14.0F,
         Files.isDirectory(CosmeticsRepository.directory()) ? MenuText.ui("No avatars in this tab") : MenuText.ui("The cosmetics folder was not found"),
         ClickGuiPalette.TEXT_SECONDARY,
         UiFontStyle.MEDIUM,
         TextAlign.CENTER
      );
      canvas.text(
         centerX,
         centerY + 16.0F,
         12.0F,
         MenuText.ui("Drop Figura avatar folders into config/ryzen/cosmetics"),
         ClickGuiPalette.TEXT_MUTED,
         UiFontStyle.REGULAR,
         TextAlign.CENTER
      );
   }

   private void renderPreview(ClickGuiCanvas canvas, class_332 graphics) {
      canvas.outlinedRect(573.0F, 164.0F, 320.0F, 430.0F, ClickGuiPalette.CONTROL, 16.0F, 0.5F, ClickGuiPalette.STROKE);
      CosmeticEntry applied = CosmeticsRepository.byId(FiguraBridge.appliedId());
      CosmeticEntry shown = this.hovered != null ? this.hovered : (this.selected != null ? this.selected : applied);
      if (shown != this.fadingFrom) {
         this.fadingFrom = shown;
         this.captionFade = 0.0F;
      }

      boolean model = this.renderPlayerModel(canvas, graphics);
      if (!model) {
         this.drawThumb(canvas, shown, 608.0F, 188.0F, 250.0F, 250.0F, 12.0F, 1.0F);
      } else if (shown != null && shown != applied) {
         float inset = 75.0F;
         float insetX = 858.0F - inset;
         canvas.outlinedRect(
            insetX - 4.0F,
            188.0F,
            inset + 4.0F,
            inset + 4.0F,
            ColorUtil.multiplyAlpha(ClickGuiPalette.CONTROL_ACTIVE, this.captionFade),
            10.0F,
            0.5F,
            ColorUtil.multiplyAlpha(ClickGuiPalette.CARD_STROKE, this.captionFade)
         );
         this.drawThumb(canvas, shown, insetX - 2.0F, 190.0F, inset, inset, 8.0F, this.captionFade);
      }

      if (model) {
         this.renderTurntableHints(canvas);
      }

      if (shown == null) {
         canvas.text(
            733.0F,
            458.0F,
            14.0F,
            applied == null ? MenuText.ui("Nothing equipped") : MenuText.ui("Hover an avatar"),
            ClickGuiPalette.TEXT_SECONDARY,
            UiFontStyle.MEDIUM,
            TextAlign.CENTER
         );
         this.renderStatus(canvas);
      } else {
         float rise = (1.0F - this.captionFade) * 5.0F;
         canvas.text(
            733.0F,
            458.0F + rise,
            15.0F,
            fit(canvas, shown.displayName(), 280.0F, 15.0F, UiFontStyle.MEDIUM),
            ColorUtil.multiplyAlpha(-1, this.captionFade),
            UiFontStyle.MEDIUM,
            TextAlign.CENTER
         );
         boolean equipped = FiguraBridge.isApplied(shown);
         canvas.text(
            733.0F,
            480.0F + rise,
            12.0F,
            equipped ? MenuText.ui("Equipped") : tabLabel(shown.kind()),
            ColorUtil.multiplyAlpha(equipped ? ClickGuiPalette.accent() : ClickGuiPalette.TEXT_MUTED, this.captionFade),
            UiFontStyle.REGULAR,
            TextAlign.CENTER
         );
         this.renderStatus(canvas);
         this.renderActionButton(canvas, equipped);
      }
   }

   private void renderTurntableHints(ClickGuiCanvas canvas) {
      if (this.resetFade > 0.01F) {
         boolean hover = canvas.hit(834.0F, 188.0F, 24.0F, 24.0F);
         canvas.rect(
            834.0F, 188.0F, 24.0F, 24.0F, ColorUtil.multiplyAlpha(hover ? ClickGuiPalette.CONTROL_ACTIVE : ClickGuiPalette.CONTROL, this.resetFade), 12.0F
         );
         canvas.texture(
            839.5F, 193.5F, 13.0F, 13.0F, Textures.Icons.REFRESH_CCW, ColorUtil.multiplyAlpha(hover ? -1 : ClickGuiPalette.ICON_INACTIVE, this.resetFade)
         );
      }

      float hint = 1.0F - this.resetFade * 0.75F;
      canvas.text(
         733.0F,
         426.0F,
         10.0F,
         MenuText.ui("Drag to rotate, scroll to zoom"),
         ColorUtil.multiplyAlpha(ClickGuiPalette.TEXT_FAINT, hint),
         UiFontStyle.REGULAR,
         TextAlign.CENTER
      );
   }

   private void renderActionButton(ClickGuiCanvas canvas, boolean equipped) {
      boolean hover = canvas.hit(601.0F, 536.0F, 264.0F, 40.0F);
      int fill = equipped
         ? (hover ? ClickGuiPalette.CONTROL_ACTIVE : ClickGuiPalette.CONTROL)
         : ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), hover ? 1.0F : 0.88F);
      canvas.outlinedRect(601.0F, 536.0F, 264.0F, 40.0F, fill, 13.0F, 0.5F, equipped ? ClickGuiPalette.CONTROL_STROKE : 0);
      String label = equipped ? MenuText.ui("Unequip") : MenuText.ui("Equip");
      float labelWidth = canvas.textWidth(label, 13.0F, UiFontStyle.MEDIUM);
      float groupX = 601.0F + (264.0F - (23.0F + labelWidth)) / 2.0F;
      canvas.texture(groupX, 549.0F, 14.0F, 14.0F, equipped ? Textures.Icons.X : Textures.Icons.SHIRT, -1);
      canvas.text(groupX + 23.0F, 549.0F, 13.0F, label, -1, UiFontStyle.MEDIUM);
   }

   private void renderStatus(ClickGuiCanvas canvas) {
      long remaining = this.statusUntil - System.currentTimeMillis();
      if (!this.status.isEmpty() && remaining > 0L) {
         float alpha = Math.min(1.0F, (float)remaining / 500.0F);
         int color = this.statusOk ? ColorUtil.rgba(150, 214, 168, 255) : ColorUtil.rgba(228, 138, 138, 255);
         canvas.text(
            733.0F,
            510.0F,
            11.0F,
            fit(canvas, this.status, 280.0F, 11.0F, UiFontStyle.REGULAR),
            ColorUtil.multiplyAlpha(color, alpha),
            UiFontStyle.REGULAR,
            TextAlign.CENTER
         );
      }
   }

   private boolean renderPlayerModel(ClickGuiCanvas canvas, class_332 graphics) {
      class_746 player = MinecraftContext.mc.field_1724;
      if (graphics != null && player != null) {
         int left = Math.round(canvas.x(608.0F));
         int top = Math.round(canvas.y(188.0F));
         int right = Math.round(canvas.x(858.0F));
         int bottom = Math.round(canvas.y(438.0F));
         Render2DUtil.flush();
         graphics.method_71048();
         graphics.method_44379(left, top, right, bottom);

         try {
            return this.preview.render(graphics, player, left, top, right, bottom);
         } finally {
            graphics.method_44380();
         }
      } else {
         return false;
      }
   }

   public boolean mousePressed(ClickGuiCanvas canvas, int button) {
      if (button != 0) {
         return canvas.hit(94.0F, 67.0F, 821.0F, 546.0F);
      }

      for (int index = 0; index < TABS.length; index++) {
         if (canvas.hit(this.tabPositions[index], 118.0F, this.tabWidths[index], 30.0F)) {
            this.selectTab(canvas, TABS[index]);
            return true;
         }
      }

      if (canvas.hit(this.unequipX, 118.0F, this.unequipWidth, 30.0F)) {
         this.unequip();
         return true;
      }

      if (canvas.hit(folderX(), 118.0F, 30.0F, 30.0F)) {
         this.openFolder();
         return true;
      }

      if (canvas.hit(refreshX(), 118.0F, 30.0F, 30.0F)) {
         this.refresh(canvas);
         return true;
      }

      for (int index = 0; index < this.entries.size(); index++) {
         float cardY = this.cardY(index);
         if (!(cardY + 134.0F < 164.0F) && !(cardY > 594.0F) && canvas.hit(cardX(index), cardY, 138.66667F, 134.0F)) {
            CosmeticEntry entry = this.entries.get(index);
            this.selected = entry;
            this.toggle(entry);
            return true;
         }
      }

      CosmeticEntry shown = this.selected != null ? this.selected : CosmeticsRepository.byId(FiguraBridge.appliedId());
      if (shown != null && canvas.hit(601.0F, 536.0F, 264.0F, 40.0F)) {
         this.toggle(shown);
         return true;
      } else if (this.resetFade > 0.01F && canvas.hit(834.0F, 188.0F, 24.0F, 24.0F)) {
         this.preview.reset();
         return true;
      } else if (canvas.hit(608.0F, 188.0F, 250.0F, 250.0F)) {
         this.preview.beginDrag(canvas.mouseX(), canvas.mouseY());
         return true;
      } else {
         return canvas.hit(94.0F, 67.0F, 821.0F, 546.0F);
      }
   }

   public void drag(ClickGuiCanvas canvas) {
      this.preview.drag(canvas.mouseX(), canvas.mouseY());
   }

   public void release() {
      this.preview.release();
   }

   public void scroll(ClickGuiCanvas canvas, double vertical) {
      if (canvas.hit(573.0F, 164.0F, 320.0F, 430.0F)) {
         this.preview.zoom(vertical);
      } else {
         this.scrollOffset = clamp(this.scrollOffset - (float)vertical * 42.0F, 0.0F, this.maxScroll);
      }
   }

   private void selectTab(ClickGuiCanvas canvas, CosmeticEntry.Kind next) {
      if (this.tab != next) {
         this.tab = next;
         this.scrollOffset = 0.0F;
         this.selected = null;
         this.entriesDirty = true;
         this.layout(canvas);
      }
   }

   private void toggle(CosmeticEntry entry) {
      if (FiguraBridge.isApplied(entry)) {
         this.unequip();
      } else {
         boolean ok = FiguraBridge.apply(entry);
         if (ok) {
            this.equipPulse = 1.0F;
         }

         this.setStatus(ok ? entry.displayName() + " " + MenuText.ui("equipped") : MenuText.ui("Figura is unavailable"), ok);
      }
   }

   private void unequip() {
      boolean ok = FiguraBridge.clear();
      this.setStatus(ok ? MenuText.ui("Avatar removed") : MenuText.ui("Figura is unavailable"), ok);
   }

   private void refresh(ClickGuiCanvas canvas) {
      CosmeticsRepository.rescan();
      this.selected = null;
      this.scrollOffset = 0.0F;
      this.entriesDirty = true;
      this.layout(canvas);
      this.setStatus(MenuText.ui("Found") + " " + CosmeticsRepository.all().size(), true);
   }

   private void openFolder() {
      try {
         Files.createDirectories(CosmeticsRepository.directory());
         class_156.method_668().method_60932(CosmeticsRepository.directory());
      } catch (Exception var2) {
      }
   }

   public String headerTitle() {
      return MenuText.ui("Cosmetics");
   }

   public String headerDescription() {
      return MenuText.ui("Figura avatars, heads and weapon skins.");
   }

   private void measureTabs(ClickGuiCanvas canvas) {
      float cursor = 115.0F;

      for (int index = 0; index < TABS.length; index++) {
         this.tabWidths[index] = canvas.textWidth(tabLabel(TABS[index]), 12.0F, UiFontStyle.MEDIUM) + 43.0F;
         this.tabPositions[index] = cursor;
         cursor += this.tabWidths[index] + 8.0F;
      }

      this.unequipWidth = canvas.textWidth(MenuText.ui("Unequip"), 12.0F, UiFontStyle.MEDIUM) + 30.0F;
      this.unequipX = folderX() - 8.0F - this.unequipWidth;
   }

   private static float refreshX() {
      return 863.0F;
   }

   private static float folderX() {
      return refreshX() - 8.0F - 30.0F;
   }

   private static float cardX(int index) {
      return 115.0F + index % 3 * 150.66667F;
   }

   private float cardY(int index) {
      return 164.0F + index / 3 * 146.0F - this.scrollOffset;
   }

   private static String tabLabel(CosmeticEntry.Kind kind) {
      return MenuText.ui(kind.tabName());
   }

   private void setStatus(String message, boolean ok) {
      this.status = message == null ? "" : message;
      this.statusOk = ok;
      this.statusUntil = System.currentTimeMillis() + 2600L;
   }

   private static String fit(ClickGuiCanvas canvas, String raw, float maxWidth, float size, UiFontStyle style) {
      String value = raw == null ? "" : raw;
      if (canvas.textWidth(value, size, style) <= maxWidth) {
         return value;
      }

      int end = value.length();

      while (end > 0 && canvas.textWidth(value.substring(0, end) + "...", size, style) > maxWidth) {
         end--;
      }

      return value.substring(0, end).stripTrailing() + "...";
   }

   private static float clamp(float value, float min, float max) {
      return MathUtil.clamp(value, min, max);
   }
}
