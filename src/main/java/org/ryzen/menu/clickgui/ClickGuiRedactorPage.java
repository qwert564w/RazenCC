package org.ryzen.menu.clickgui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.ryzen.feature.impl.misc.autobuy.AutoBuyFeature;
import org.ryzen.feature.impl.misc.autobuy.AutoBuyItem;
import org.ryzen.feature.impl.misc.autobuy.AutoBuyManager;
import org.ryzen.feature.impl.misc.collector.CollectorCondition;
import org.ryzen.feature.impl.misc.collector.CollectorItem;
import org.ryzen.feature.impl.misc.collector.CollectorManager;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class ClickGuiRedactorPage {
   private static final float PANEL_WIDTH = 425.0F;
   private static final float PANEL_HEIGHT = 235.0F;
   private static final float PANEL_X = 251.5F;
   private static final float PANEL_Y = 207.0F;
   private static final float HEADER_Y = 215.0F;
   private static final float HEADER_HEIGHT = 16.0F;
   private static final float FRAME_X = 251.5F;
   private static final float FRAME_Y = 239.0F;
   private static final float FRAME_WIDTH = 425.0F;
   private static final float FRAME_HEIGHT = 191.0F;
   private static final float SLOT = 20.0F;
   private static final float SLOT_GAP = 4.0F;
   private static final int GRID_COLUMNS = 10;
   private static final float GRID_X = 259.5F;
   private static final float SETTINGS_X = 503.5F;
   private static final float ROW_X = 511.5F;
   private static final float ROW_WIDTH = 149.0F;
   private static final float GRID_STEP = 24.0F;
   private static final int PRICE_MAX_DIGITS = 10;
   private static volatile ClickGuiRedactorPage.Section requestedSection = ClickGuiRedactorPage.Section.AUTOBUY;
   private final List<ClickGuiRedactorPage.AutoHit> autoHits = new ArrayList<>();
   private final List<ClickGuiRedactorPage.CollectorHit> collectorHits = new ArrayList<>();
   private final List<ClickGuiRedactorPage.ItemIcon> itemIcons = new ArrayList<>();
   private final List<ClickGuiRedactorPage.CountLabel> countLabels = new ArrayList<>();
   private final List<ClickGuiRedactorPage.ConditionHit> conditionHits = new ArrayList<>();
   private ClickGuiRedactorPage.Section section = ClickGuiRedactorPage.Section.AUTOBUY;
   private AutoBuyItem selectedAuto;
   private CollectorItem selectedCollector;
   private float gridScroll;
   private float minimumGridScroll;
   private boolean editingPrice;
   private String priceDraft = "";
   private ClickGuiRedactorPage.Bounds autoToggleHit;
   private ClickGuiRedactorPage.Bounds autoSearchHit;
   private ClickGuiRedactorPage.Bounds priceFieldHit;
   private ClickGuiRedactorPage.Bounds saveHit;
   private ClickGuiRedactorPage.Bounds loadHit;
   private ClickGuiRedactorPage.Bounds collectorToggleHit;
   private ClickGuiRedactorPage.Bounds collectorMinusHit;
   private ClickGuiRedactorPage.Bounds collectorPlusHit;

   public void layout(ClickGuiCanvas canvas) {
      AutoBuyManager.get().ensureLoaded();
      CollectorManager.get().ensureLoaded();
      if (this.section != requestedSection) {
         this.setSection(requestedSection);
      }

      this.ensureSelections();
   }

   public static void requestAutoBuy() {
      requestedSection = ClickGuiRedactorPage.Section.AUTOBUY;
   }

   public static void requestCollector() {
      requestedSection = ClickGuiRedactorPage.Section.COLLECTOR;
   }

   public void render(ClickGuiCanvas canvas, class_332 graphics) {
      this.ensureSelections();
      this.resetFrameHits();
      this.renderStation(canvas);
      if (this.section == ClickGuiRedactorPage.Section.AUTOBUY) {
         this.renderAutoBuy(canvas);
      } else {
         this.renderCollector(canvas);
      }

      this.renderItemIcons(canvas, graphics);
   }

   private void renderStation(ClickGuiCanvas canvas) {
      int background = ColorUtil.withAlpha(ColorUtil.lerp(ColorUtil.rgb(18, 18, 20), ClickGuiPalette.accent(), 0.06F), 220);
      canvas.shadowedRect(251.5F, 207.0F, 425.0F, 235.0F, background, 8.0F, ColorUtil.rgba(0, 0, 0, 105), 16.0F);
      canvas.outlinedRect(251.5F, 207.0F, 425.0F, 235.0F, 0, 8.0F, 0.5F, ColorUtil.rgba(255, 255, 255, 20));
      float tabWidth = 53.0F;
      float tabX = 251.5F + (425.0F - tabWidth) / 2.0F;
      canvas.outlinedRect(tabX, 215.0F, tabWidth, 16.0F, ColorUtil.rgba(255, 255, 255, 4), 6.0F, 0.5F, ColorUtil.rgba(255, 255, 255, 18));
      float iconX = tabX + 12.0F;
      this.renderSectionIcon(canvas, iconX, "S", this.section == ClickGuiRedactorPage.Section.COLLECTOR);
      iconX += 20.5F;
      this.renderSectionIcon(canvas, iconX, "h", this.section == ClickGuiRedactorPage.Section.AUTOBUY);
      float activeCenter = this.section == ClickGuiRedactorPage.Section.COLLECTOR ? tabX + 12.0F + 4.25F : tabX + 12.0F + 8.5F + 12.0F + 4.25F;
      canvas.rect(activeCenter - 3.0F, 231.0F, 6.0F, 0.5F, ClickGuiPalette.accent(), 0.0F);
      canvas.texture(259.5F, 217.0F, 12.0F, 12.0F, Textures.Logos.BOOT, ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.75F));
      float leftSeparator = 279.5F;
      canvas.rect(leftSeparator, 219.0F, 0.75F, 8.0F, ColorUtil.rgba(255, 255, 255, 25), 0.0F);
      canvas.stationText(leftSeparator + 8.0F, 219.0F, 6.75F, "ryzendlc.org", ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR);
      float avatarX = 656.5F;
      canvas.texture(avatarX, 217.0F, 12.0F, 12.0F, Textures.ClickGui.APP_ICON, -1, 5.0F);
      float rightSeparator = avatarX - 8.0F;
      canvas.rect(rightSeparator, 219.0F, 0.75F, 8.0F, ColorUtil.rgba(255, 255, 255, 25), 0.0F);
      String sectionName = this.section == ClickGuiRedactorPage.Section.COLLECTOR ? "collector section" : "auto-buy section";
      canvas.stationText(rightSeparator - 8.0F, 219.0F, 6.75F, sectionName, ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR, TextAlign.RIGHT);
   }

   private void renderSectionIcon(ClickGuiCanvas canvas, float x, String glyph, boolean active) {
      boolean hovered = canvas.hit(x - 6.0F, 215.0F, 20.5F, 16.0F);
      int color = active ? ClickGuiPalette.accent() : (hovered ? ClickGuiPalette.TEXT_SECONDARY : ClickGuiPalette.TEXT_MUTED);
      canvas.stationIcon(x, 218.75F, 8.5F, glyph, color);
   }

   private void renderAutoBuy(ClickGuiCanvas canvas) {
      List<AutoBuyItem> items = this.autoItems();
      if (items.isEmpty()) {
         this.minimumGridScroll = 0.0F;
      } else {
         if (this.selectedAuto == null || !items.contains(this.selectedAuto)) {
            this.selectAuto((AutoBuyItem)items.getFirst());
         }

         this.renderAutoGrid(canvas, items);
         this.renderAutoSettings(canvas, this.selectedAuto);
      }
   }

   private void renderAutoGrid(ClickGuiCanvas canvas, List<AutoBuyItem> items) {
      this.updateGridRange(items.size());
      canvas.pushScissor(251.5F, 239.0F, 425.0F, 191.0F);

      for (int index = 0; index < items.size(); index++) {
         AutoBuyItem item = items.get(index);
         float x = 259.5F + index % 10 * 24.0F;
         float y = 239.0F + index / 10 * 24.0F + this.gridScroll;
         if (visibleSlot(y)) {
            boolean hovered = canvas.hit(x, y, 20.0F, 20.0F) && canvas.mouseDesignY() >= 239.0F && canvas.mouseDesignY() <= 430.0F;
            int fill = item == this.selectedAuto ? ColorUtil.rgba(255, 255, 255, 6) : (hovered ? ColorUtil.rgba(255, 255, 255, 3) : 0);
            canvas.outlinedRect(x, y, 20.0F, 20.0F, fill, 4.0F, 0.5F, ColorUtil.rgba(255, 255, 255, 20));
            this.autoHits.add(new ClickGuiRedactorPage.AutoHit(new ClickGuiRedactorPage.Bounds(x, y, 20.0F, 20.0F), item));
            this.itemIcons.add(new ClickGuiRedactorPage.ItemIcon(item.icon(), x + 2.5F, y + 2.5F, 15.0F));
         }
      }

      canvas.popScissor();
   }

   private void renderAutoSettings(ClickGuiCanvas canvas, AutoBuyItem item) {
      if (item != null) {
         float center = 249.0F;
         float toggleX = 640.5F;
         this.autoToggleHit = new ClickGuiRedactorPage.Bounds(toggleX, center - 4.25F, 14.0F, 8.5F);
         float searchX = toggleX - 14.0F;
         this.autoSearchHit = new ClickGuiRedactorPage.Bounds(searchX, center - 5.0F, 10.0F, 10.0F);
         this.itemIcons.add(new ClickGuiRedactorPage.ItemIcon(item.icon(), 511.5F, center - 6.0F, 12.0F));
         float nameX = 531.5F;
         String name = fit(canvas, item.getName(), searchX - 6.0F - nameX, 7.5F, UiFontStyle.REGULAR);
         canvas.stationText(nameX, center - 3.5F, 7.5F, name, item.isEnabled() ? -1 : ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR);
         canvas.stationIcon(searchX + 1.0F, center - 4.0F, 8.0F, "G", canvas.hit(searchX, center - 5.0F, 10.0F, 10.0F) ? -1 : ClickGuiPalette.TEXT_MUTED);
         renderToggle(canvas, this.autoToggleHit, item.isEnabled());
         float controlsY = 267.0F;
         float loadX = 646.5F;
         float saveX = loadX - 18.0F;
         float fieldWidth = saveX - 4.0F - 511.5F;
         this.priceFieldHit = new ClickGuiRedactorPage.Bounds(511.5F, controlsY, fieldWidth, 14.0F);
         this.saveHit = new ClickGuiRedactorPage.Bounds(saveX, controlsY, 14.0F, 14.0F);
         this.loadHit = new ClickGuiRedactorPage.Bounds(loadX, controlsY, 14.0F, 14.0F);
         canvas.outlinedRect(
            511.5F,
            controlsY,
            fieldWidth,
            14.0F,
            ColorUtil.rgba(255, 255, 255, this.editingPrice ? 8 : 4),
            4.0F,
            0.5F,
            this.editingPrice ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.75F) : ColorUtil.rgba(255, 255, 255, 18)
         );
         String shown = this.editingPrice ? groupDigits(this.priceDraft) : formatPrice(item.getBuyPrice());
         boolean placeholder = shown.isEmpty();
         canvas.stationText(
            516.5F,
            controlsY + 3.5F,
            6.75F,
            placeholder ? "Цена предмета" : shown,
            placeholder ? ClickGuiPalette.TEXT_FAINT : ClickGuiPalette.TEXT_SECONDARY,
            UiFontStyle.REGULAR
         );
         if (this.editingPrice && System.currentTimeMillis() / 500L % 2L == 0L) {
            float caretX = 517.0F + canvas.stationTextWidth(shown, 6.75F, UiFontStyle.REGULAR);
            canvas.rect(Math.min(caretX, 511.5F + fieldWidth - 3.0F), controlsY + 3.0F, 0.75F, 8.0F, ClickGuiPalette.accent(), 0.0F);
         }

         renderSquareButton(canvas, this.saveHit, "N");
         renderSquareButton(canvas, this.loadHit, "A");
      }
   }

   private void renderCollector(ClickGuiCanvas canvas) {
      List<CollectorItem> items = CollectorManager.get().all();
      if (items.isEmpty()) {
         this.minimumGridScroll = 0.0F;
      } else {
         if (this.selectedCollector == null || !items.contains(this.selectedCollector)) {
            this.selectedCollector = (CollectorItem)items.getFirst();
         }

         this.renderCollectorGrid(canvas, items);
         this.renderCollectorSettings(canvas, this.selectedCollector);
      }
   }

   private void renderCollectorGrid(ClickGuiCanvas canvas, List<CollectorItem> items) {
      this.updateGridRange(items.size());
      canvas.pushScissor(251.5F, 239.0F, 425.0F, 191.0F);

      for (int index = 0; index < items.size(); index++) {
         CollectorItem item = items.get(index);
         float x = 259.5F + index % 10 * 24.0F;
         float y = 239.0F + index / 10 * 24.0F + this.gridScroll;
         if (visibleSlot(y)) {
            boolean hovered = canvas.hit(x, y, 20.0F, 20.0F) && canvas.mouseDesignY() >= 239.0F && canvas.mouseDesignY() <= 430.0F;
            int fill = item == this.selectedCollector ? ColorUtil.rgba(255, 255, 255, 6) : (hovered ? ColorUtil.rgba(255, 255, 255, 3) : 0);
            canvas.outlinedRect(x, y, 20.0F, 20.0F, fill, 4.0F, 0.5F, ColorUtil.rgba(255, 255, 255, 20));
            this.collectorHits.add(new ClickGuiRedactorPage.CollectorHit(new ClickGuiRedactorPage.Bounds(x, y, 20.0F, 20.0F), item));
            this.itemIcons.add(new ClickGuiRedactorPage.ItemIcon(item.icon(), x + 2.5F, y + 2.5F, 15.0F));
            if (item.getCount() > 1) {
               this.countLabels
                  .add(new ClickGuiRedactorPage.CountLabel(x + 20.0F - 2.0F, y + 20.0F - 8.0F, Integer.toString(item.getCount()), item.isEnabled()));
            }
         }
      }

      canvas.popScissor();
   }

   private void renderCollectorSettings(ClickGuiCanvas canvas, CollectorItem item) {
      if (item != null) {
         float center = 249.0F;
         float toggleX = 640.5F;
         this.collectorToggleHit = new ClickGuiRedactorPage.Bounds(toggleX, center - 4.25F, 14.0F, 8.5F);
         boolean adjustableCount = item.maxTargetCount() > 1;
         float countX = toggleX - 38.0F;
         this.itemIcons.add(new ClickGuiRedactorPage.ItemIcon(item.icon(), 511.5F, center - 6.0F, 12.0F));
         float nameX = 531.5F;
         float nameLimit = adjustableCount ? countX - 6.0F : toggleX - 6.0F;
         canvas.stationText(
            nameX,
            center - 3.5F,
            7.5F,
            fit(canvas, item.getName(), nameLimit - nameX, 7.5F, UiFontStyle.REGULAR),
            item.isEnabled() ? -1 : ClickGuiPalette.TEXT_MUTED,
            UiFontStyle.REGULAR
         );
         renderToggle(canvas, this.collectorToggleHit, item.isEnabled());
         if (adjustableCount) {
            this.collectorMinusHit = new ClickGuiRedactorPage.Bounds(countX, center - 5.0F, 16.0F, 10.0F);
            this.collectorPlusHit = new ClickGuiRedactorPage.Bounds(countX + 16.0F, center - 5.0F, 16.0F, 10.0F);
            canvas.stationText(countX + 6.0F, center - 5.5F, 11.0F, "-", ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR, TextAlign.CENTER);
            canvas.stationText(
               countX + 16.0F,
               center - 3.5F,
               6.75F,
               Integer.toString(item.getCount()),
               item.isEnabled() ? -1 : ClickGuiPalette.TEXT_MUTED,
               UiFontStyle.REGULAR,
               TextAlign.CENTER
            );
            canvas.stationText(countX + 26.0F, center - 5.5F, 11.0F, "+", ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR, TextAlign.CENTER);
         }

         float rowY = 267.0F;

         for (CollectorCondition condition : item.getConditions()) {
            if (rowY + 10.0F > 430.0F) {
               break;
            }

            this.renderCondition(canvas, condition, rowY, item.isEnabled());
            rowY += 14.0F;
         }
      }
   }

   private void renderCondition(ClickGuiCanvas canvas, CollectorCondition condition, float rowY, boolean itemEnabled) {
      float centerY = rowY + 5.0F;
      float switchX = 641.5F;
      ClickGuiRedactorPage.Bounds toggle = new ClickGuiRedactorPage.Bounds(switchX, centerY - 3.8475F, 13.0F, 7.695F);
      float levelX = switchX - 34.0F;
      float labelLimit = condition.isAdjustable() ? levelX - 4.0F : switchX - 4.0F;
      float opacity = itemEnabled && condition.isEnabled() ? 1.0F : 0.45F;
      canvas.stationText(
         511.5F,
         centerY - 3.5F,
         6.75F,
         fit(canvas, condition.getLabel(), labelLimit - 511.5F, 6.75F, UiFontStyle.REGULAR),
         ColorUtil.multiplyAlpha(-1, opacity),
         UiFontStyle.REGULAR
      );
      ClickGuiRedactorPage.Bounds minus = null;
      ClickGuiRedactorPage.Bounds plus = null;
      if (condition.isAdjustable()) {
         minus = new ClickGuiRedactorPage.Bounds(levelX, centerY - 5.0F, 14.0F, 10.0F);
         plus = new ClickGuiRedactorPage.Bounds(levelX + 14.0F, centerY - 5.0F, 14.0F, 10.0F);
         canvas.stationText(
            levelX + 7.0F, centerY - 5.5F, 11.0F, "-", ColorUtil.multiplyAlpha(ClickGuiPalette.TEXT_MUTED, opacity), UiFontStyle.REGULAR, TextAlign.CENTER
         );
         canvas.rect(levelX + 13.625F, centerY - 2.5F, 0.75F, 5.0F, ColorUtil.multiplyAlpha(ClickGuiPalette.CONTROL_STROKE, opacity), 0.0F);
         canvas.stationText(
            levelX + 21.0F, centerY - 5.5F, 11.0F, "+", ColorUtil.multiplyAlpha(ClickGuiPalette.TEXT_MUTED, opacity), UiFontStyle.REGULAR, TextAlign.CENTER
         );
      }

      renderMiniToggle(canvas, toggle, itemEnabled && condition.isEnabled());
      this.conditionHits.add(new ClickGuiRedactorPage.ConditionHit(toggle, minus, plus, condition));
   }

   private void renderItemIcons(ClickGuiCanvas canvas, class_332 graphics) {
      if (graphics != null && !this.itemIcons.isEmpty()) {
         Render2DUtil.flush();
         graphics.method_71048();
         graphics.method_44379(Math.round(canvas.x(251.5F)), Math.round(canvas.y(239.0F)), Math.round(canvas.x(676.5F)), Math.round(canvas.y(430.0F)));

         try {
            for (ClickGuiRedactorPage.ItemIcon icon : this.itemIcons) {
               drawItem(canvas, graphics, icon.stack(), icon.x(), icon.y(), icon.size());
            }
         } finally {
            graphics.method_44380();
         }

         for (ClickGuiRedactorPage.CountLabel label : this.countLabels) {
            canvas.stationText(
               label.x(), label.y(), 6.5F, label.value(), label.enabled() ? -1 : ClickGuiPalette.TEXT_FAINT, UiFontStyle.REGULAR, TextAlign.RIGHT
            );
         }

         Render2DUtil.flush();
      }
   }

   public boolean mousePressed(ClickGuiCanvas canvas, int button) {
      if (button != 0) {
         return canvas.hit(251.5F, 207.0F, 425.0F, 235.0F);
      } else {
         float tabX = 449.5F;
         if (canvas.hit(tabX - 6.0F, 215.0F, 20.5F, 16.0F)) {
            this.setSection(ClickGuiRedactorPage.Section.COLLECTOR);
            return true;
         } else {
            float autoTabX = tabX + 20.5F;
            if (canvas.hit(autoTabX - 6.0F, 215.0F, 20.5F, 16.0F)) {
               this.setSection(ClickGuiRedactorPage.Section.AUTOBUY);
               return true;
            } else {
               return this.section == ClickGuiRedactorPage.Section.AUTOBUY ? this.pressAutoBuy(canvas) : this.pressCollector(canvas);
            }
         }
      }
   }

   private boolean pressAutoBuy(ClickGuiCanvas canvas) {
      if (contains(this.saveHit, canvas)) {
         this.commitPrice();
         AutoBuyManager.get().saveNow();
         this.editingPrice = false;
         return true;
      }

      if (contains(this.loadHit, canvas)) {
         AutoBuyManager.get().reload();
         this.editingPrice = false;
         if (this.selectedAuto != null) {
            this.priceDraft = Integer.toString(this.selectedAuto.getBuyPrice());
         }

         return true;
      } else {
         if (contains(this.autoSearchHit, canvas)) {
            this.searchSelectedAuto();
            return true;
         }

         if (contains(this.autoToggleHit, canvas) && this.selectedAuto != null) {
            AutoBuyManager.get().setEnabled(this.selectedAuto, !this.selectedAuto.isEnabled());
            return true;
         }

         if (contains(this.priceFieldHit, canvas) && this.selectedAuto != null) {
            this.editingPrice = true;
            this.priceDraft = Integer.toString(this.selectedAuto.getBuyPrice());
            return true;
         }

         for (ClickGuiRedactorPage.AutoHit hit : this.autoHits) {
            if (hit.bounds().contains(canvas)) {
               if (hit.item() != this.selectedAuto) {
                  this.commitPrice();
                  this.selectAuto(hit.item());
               }

               return true;
            }
         }

         if (this.editingPrice) {
            this.commitPrice();
            this.editingPrice = false;
         }

         return canvas.hit(251.5F, 207.0F, 425.0F, 235.0F);
      }
   }

   private boolean pressCollector(ClickGuiCanvas canvas) {
      if (contains(this.collectorToggleHit, canvas) && this.selectedCollector != null) {
         CollectorManager.get().setEnabled(this.selectedCollector, !this.selectedCollector.isEnabled());
         return true;
      }

      if (contains(this.collectorMinusHit, canvas) && this.selectedCollector != null) {
         CollectorManager.get().setCount(this.selectedCollector, this.selectedCollector.getCount() - 1);
         return true;
      }

      if (contains(this.collectorPlusHit, canvas) && this.selectedCollector != null) {
         CollectorManager.get().setCount(this.selectedCollector, this.selectedCollector.getCount() + 1);
         return true;
      }

      for (ClickGuiRedactorPage.ConditionHit hit : this.conditionHits) {
         if (hit.toggle().contains(canvas)) {
            CollectorManager.get().setConditionEnabled(hit.condition(), !hit.condition().isEnabled());
            return true;
         }

         if (contains(hit.minus(), canvas)) {
            CollectorManager.get().setConditionLevel(hit.condition(), hit.condition().getLevel() - 1);
            return true;
         }

         if (contains(hit.plus(), canvas)) {
            CollectorManager.get().setConditionLevel(hit.condition(), hit.condition().getLevel() + 1);
            return true;
         }
      }

      for (ClickGuiRedactorPage.CollectorHit hit : this.collectorHits) {
         if (hit.bounds().contains(canvas)) {
            this.selectedCollector = hit.item();
            return true;
         }
      }

      return canvas.hit(251.5F, 207.0F, 425.0F, 235.0F);
   }

   public void drag(ClickGuiCanvas canvas) {
   }

   public void release() {
   }

   public void scroll(ClickGuiCanvas canvas, double vertical) {
      if (canvas.hit(251.5F, 239.0F, 425.0F, 191.0F)) {
         this.gridScroll = clamp(this.gridScroll + (float)vertical * 15.0F, this.minimumGridScroll, 0.0F);
      }
   }

   public boolean keyPressed(int key) {
      if (!this.editingPrice) {
         return false;
      }

      if (key == 259) {
         if (!this.priceDraft.isEmpty()) {
            this.priceDraft = this.priceDraft.substring(0, this.priceDraft.length() - 1);
            this.commitPrice();
         }

         return true;
      } else {
         if (key != 256 && key != 257 && key != 335) {
            return true;
         }

         this.commitPrice();
         this.editingPrice = false;
         return true;
      }
   }

   public boolean charTyped(int codePoint) {
      if (this.editingPrice && codePoint >= 48 && codePoint <= 57 && this.priceDraft.length() < 10) {
         if (this.priceDraft.equals("0")) {
            this.priceDraft = "";
         }

         this.priceDraft = this.priceDraft + (char)codePoint;
         this.commitPrice();
         return true;
      } else {
         return false;
      }
   }

   public boolean isEditing() {
      return this.editingPrice;
   }

   public String headerTitle() {
      return "Redactor";
   }

   public String headerDescription() {
      return "AutoBuy and Collector editors.";
   }

   private void setSection(ClickGuiRedactorPage.Section next) {
      if (next != null && this.section != next) {
         this.commitPrice();
         this.editingPrice = false;
         this.section = next;
         requestedSection = next;
         this.gridScroll = 0.0F;
         this.minimumGridScroll = 0.0F;
      } else {
         requestedSection = this.section;
      }
   }

   private void ensureSelections() {
      List<AutoBuyItem> autoItems = this.autoItems();
      if (!autoItems.isEmpty() && (this.selectedAuto == null || !autoItems.contains(this.selectedAuto))) {
         this.selectAuto((AutoBuyItem)autoItems.getFirst());
      }

      List<CollectorItem> collectorItems = CollectorManager.get().all();
      if (!collectorItems.isEmpty() && (this.selectedCollector == null || !collectorItems.contains(this.selectedCollector))) {
         this.selectedCollector = (CollectorItem)collectorItems.getFirst();
      }
   }

   private List<AutoBuyItem> autoItems() {
      AutoBuyFeature feature = AutoBuyFeature.get();
      String server = feature == null ? null : feature.serverMode.getValue();
      return server == null ? AutoBuyManager.get().allItems() : AutoBuyManager.get().itemsForServer(server);
   }

   private void selectAuto(AutoBuyItem item) {
      this.selectedAuto = item;
      this.editingPrice = false;
      this.priceDraft = item == null ? "" : Integer.toString(item.getBuyPrice());
   }

   private void commitPrice() {
      if (this.selectedAuto != null && !this.priceDraft.isEmpty()) {
         try {
            long parsed = Long.parseLong(this.priceDraft);
            AutoBuyManager.get().setBuyPrice(this.selectedAuto, (int)Math.min(2147483647L, Math.max(1L, parsed)));
         } catch (NumberFormatException ignored) {
            this.priceDraft = Integer.toString(this.selectedAuto.getBuyPrice());
         }
      }
   }

   private void searchSelectedAuto() {
      if (this.selectedAuto != null) {
         class_310 minecraft = class_310.method_1551();
         if (minecraft.field_1724 != null && minecraft.field_1724.field_3944 != null) {
            String query = this.selectedAuto
               .getName()
               .replaceAll("\\[\\d+x\\d+]", "")
               .replace("⚡", "")
               .replace("★", "")
               .replace("xxx", "")
               .replace("[", "")
               .replace("]", "")
               .trim()
               .replaceAll("\\s+", " ");
            if (!query.isEmpty()) {
               minecraft.field_1724.field_3944.method_45730("ah search " + query);
            }
         }
      }
   }

   private void updateGridRange(int itemCount) {
      int rows = (itemCount + 10 - 1) / 10;
      float contentHeight = rows <= 0 ? 0.0F : rows * 24.0F - 4.0F;
      this.minimumGridScroll = Math.min(0.0F, 191.0F - contentHeight);
      this.gridScroll = clamp(this.gridScroll, this.minimumGridScroll, 0.0F);
   }

   private static boolean visibleSlot(float y) {
      return y + 20.0F >= 239.0F && y <= 430.0F;
   }

   private void resetFrameHits() {
      this.autoHits.clear();
      this.collectorHits.clear();
      this.itemIcons.clear();
      this.countLabels.clear();
      this.conditionHits.clear();
      this.autoToggleHit = null;
      this.autoSearchHit = null;
      this.priceFieldHit = null;
      this.saveHit = null;
      this.loadHit = null;
      this.collectorToggleHit = null;
      this.collectorMinusHit = null;
      this.collectorPlusHit = null;
   }

   private static void renderSquareButton(ClickGuiCanvas canvas, ClickGuiRedactorPage.Bounds bounds, String glyph) {
      boolean hovered = bounds.contains(canvas);
      canvas.outlinedRect(
         bounds.x(),
         bounds.y(),
         bounds.width(),
         bounds.height(),
         ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), hovered ? 0.12F : 0.04F),
         4.0F,
         0.5F,
         ColorUtil.rgba(255, 255, 255, 20)
      );
      canvas.stationIcon(bounds.x() + 3.0F, bounds.y() + 3.0F, 8.0F, glyph, ClickGuiPalette.TEXT_SECONDARY);
   }

   private static void renderToggle(ClickGuiCanvas canvas, ClickGuiRedactorPage.Bounds bounds, boolean enabled) {
      canvas.rect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), enabled ? ClickGuiPalette.accent() : ColorUtil.rgba(45, 44, 45, 255), 3.25F);
      canvas.outlinedRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), 0, 3.25F, 0.3F, ColorUtil.rgba(255, 255, 255, 20));
      canvas.rect(bounds.x() + 1.5F + (enabled ? 5.5F : 0.0F), bounds.y() + 1.5F, 5.5F, 5.5F, enabled ? -1 : ClickGuiPalette.OFF_KNOB, 1.75F);
   }

   private static void renderMiniToggle(ClickGuiCanvas canvas, ClickGuiRedactorPage.Bounds bounds, boolean enabled) {
      canvas.rect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), enabled ? ClickGuiPalette.accent() : ColorUtil.rgba(45, 44, 45, 255), 3.078F);
      canvas.outlinedRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), 0, 3.078F, 0.5F, ColorUtil.rgba(255, 255, 255, 20));
      canvas.rect(bounds.x() + 1.496F + (enabled ? 5.216F : 0.0F), bounds.y() + 1.496F, 4.703F, 4.703F, enabled ? -1 : ClickGuiPalette.OFF_KNOB, 1.539F);
   }

   private static void drawItem(ClickGuiCanvas canvas, class_332 graphics, class_1799 stack, float left, float top, float size) {
      if (stack != null && !stack.method_7960()) {
         Matrix3x2fStack pose = graphics.method_51448();
         pose.pushMatrix();
         pose.translate(canvas.x(left), canvas.y(top));
         pose.scale(canvas.p(size) / 16.0F);

         try {
            graphics.method_51427(stack, 0, 0);
         } finally {
            pose.popMatrix();
         }
      }
   }

   private static String fit(ClickGuiCanvas canvas, String raw, float maxWidth, float size, UiFontStyle style) {
      String value = raw == null ? "" : raw;
      if (maxWidth <= 0.0F) {
         return "";
      }

      if (canvas.stationTextWidth(value, size, style) <= maxWidth) {
         return value;
      }

      int end = value.length();

      while (end > 0 && canvas.stationTextWidth(value.substring(0, end) + "...", size, style) > maxWidth) {
         end--;
      }

      return end == 0 ? "" : value.substring(0, end) + "...";
   }

   private static String formatPrice(int value) {
      return String.format(Locale.US, "%,d", Math.max(1, value));
   }

   private static String groupDigits(String digits) {
      if (digits != null && !digits.isEmpty()) {
         try {
            return String.format(Locale.US, "%,d", Long.parseLong(digits));
         } catch (NumberFormatException ignored) {
            return digits;
         }
      } else {
         return "";
      }
   }

   private static boolean contains(ClickGuiRedactorPage.Bounds bounds, ClickGuiCanvas canvas) {
      return bounds != null && bounds.contains(canvas);
   }

   private static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   @Environment(EnvType.CLIENT)
   private record AutoHit(ClickGuiRedactorPage.Bounds bounds, AutoBuyItem item) {
   }

   @Environment(EnvType.CLIENT)
   private record Bounds(float x, float y, float width, float height) {
      private boolean contains(ClickGuiCanvas canvas) {
         return canvas.hit(this.x, this.y, this.width, this.height);
      }
   }

   @Environment(EnvType.CLIENT)
   private record CollectorHit(ClickGuiRedactorPage.Bounds bounds, CollectorItem item) {
   }

   @Environment(EnvType.CLIENT)
   private record ConditionHit(
      ClickGuiRedactorPage.Bounds toggle, ClickGuiRedactorPage.Bounds minus, ClickGuiRedactorPage.Bounds plus, CollectorCondition condition
   ) {
   }

   @Environment(EnvType.CLIENT)
   private record CountLabel(float x, float y, String value, boolean enabled) {
   }

   @Environment(EnvType.CLIENT)
   private record ItemIcon(class_1799 stack, float x, float y, float size) {
   }

   @Environment(EnvType.CLIENT)
   private enum Section {
      COLLECTOR,
      AUTOBUY;
   }
}
