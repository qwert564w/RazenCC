package org.ryzen.menu.clickgui;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.misc.collector.CollectorCondition;
import org.ryzen.feature.impl.misc.collector.CollectorFeature;
import org.ryzen.feature.impl.misc.collector.CollectorItem;
import org.ryzen.feature.impl.misc.collector.CollectorManager;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class ClickGuiCollectorPage {
   private static final float PANEL_X = 94.0F;
   private static final float PANEL_Y = 65.0F;
   private static final float PANEL_WIDTH = 821.0F;
   private static final float PANEL_HEIGHT = 562.0F;
   private static final float CONTENT_X = 110.0F;
   private static final float CONTENT_WIDTH = 789.0F;
   private static final float TOP_Y = 81.0F;
   private static final float TOP_HEIGHT = 52.0F;
   private static final float BODY_Y = 147.0F;
   private static final float BODY_HEIGHT = 464.0F;
   private static final float LIST_WIDTH = 500.0F;
   private static final float GAP = 14.0F;
   private static final float EDITOR_X = 624.0F;
   private static final float EDITOR_WIDTH = 275.0F;
   private static final float ROW_HEIGHT = 49.0F;
   private static final float ROW_GAP = 6.0F;
   private final List<ClickGuiCollectorPage.RowHit> rowHits = new ArrayList<>();
   private final List<ClickGuiCollectorPage.IconHit> icons = new ArrayList<>();
   private final List<ClickGuiCollectorPage.ConditionHit> conditionHits = new ArrayList<>();
   private CollectorItem selected;
   private float scroll;
   private float maxScroll;
   private float conditionScroll;
   private float maxConditionScroll;
   private ClickGuiCollectorPage.Bounds enabledHit;
   private ClickGuiCollectorPage.Bounds scanHit;
   private ClickGuiCollectorPage.Bounds minusHit;
   private ClickGuiCollectorPage.Bounds plusHit;
   private ClickGuiCollectorPage.Bounds startHit;

   public void layout(ClickGuiCanvas canvas) {
      CollectorManager.get().ensureLoaded();
      if (this.selected == null && !CollectorManager.get().all().isEmpty()) {
         this.selected = (CollectorItem)CollectorManager.get().all().getFirst();
      }
   }

   public void render(ClickGuiCanvas canvas, class_332 graphics) {
      List<CollectorItem> items = CollectorManager.get().all();
      canvas.outlinedRect(94.0F, 65.0F, 821.0F, 562.0F, ClickGuiPalette.SURFACE, 17.0F, 0.5F, ClickGuiPalette.STROKE);
      this.renderHeader(canvas, items);
      this.renderList(canvas, items);
      this.renderEditor(canvas);
      if (graphics != null && !this.icons.isEmpty()) {
         Render2DUtil.flush();
         graphics.method_71048();
         graphics.method_44379(Math.round(canvas.x(110.0F)), Math.round(canvas.y(182.0F)), Math.round(canvas.x(610.0F)), Math.round(canvas.y(611.0F)));

         try {
            for (ClickGuiCollectorPage.IconHit icon : this.icons) {
               drawItem(canvas, graphics, icon.item().icon(), icon.x(), icon.y(), 25.0F);
            }
         } finally {
            graphics.method_44380();
         }
      }
   }

   private void renderHeader(ClickGuiCanvas canvas, List<CollectorItem> items) {
      canvas.outlinedRect(110.0F, 81.0F, 789.0F, 52.0F, ClickGuiPalette.CONTROL, 14.0F, 0.5F, ClickGuiPalette.CARD_STROKE);
      canvas.rect(119.0F, 90.0F, 34.0F, 34.0F, ClickGuiPalette.accent(), 11.0F);
      canvas.texture(125.0F, 96.0F, 22.0F, 22.0F, Textures.Icons.BOXES, -1);
      canvas.text(164.0F, 91.0F, 18.0F, MenuText.ui("Collector"), -1, UiFontStyle.SEMIBOLD);
      canvas.text(164.0F, 112.0F, 11.0F, MenuText.ui("FunTime loadout editor"), ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR);
      String status = CollectorManager.get().enabledCount() + "/" + items.size() + " " + MenuText.ui("enabled");
      canvas.text(881.0F, 101.0F, 12.0F, status, ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM, TextAlign.RIGHT);
   }

   private void renderList(ClickGuiCanvas canvas, List<CollectorItem> items) {
      canvas.outlinedRect(110.0F, 147.0F, 500.0F, 464.0F, ClickGuiPalette.CONTROL, 15.0F, 0.5F, ClickGuiPalette.CARD_STROKE);
      canvas.text(124.0F, 158.0F, 13.0F, MenuText.ui("Loadout"), -1, UiFontStyle.MEDIUM);
      canvas.text(596.0F, 158.0F, 11.0F, MenuText.ui("Left: select   Right: enable"), ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR, TextAlign.RIGHT);
      float viewY = 182.0F;
      float viewHeight = 419.0F;
      float contentHeight = items.isEmpty() ? 0.0F : items.size() * 55.0F - 6.0F;
      this.maxScroll = Math.max(0.0F, contentHeight - viewHeight);
      this.scroll = clamp(this.scroll, 0.0F, this.maxScroll);
      this.rowHits.clear();
      this.icons.clear();
      canvas.pushScissor(110.0F, viewY, 500.0F, viewHeight);
      float y = viewY - this.scroll;

      for (CollectorItem item : items) {
         boolean visible = y + 49.0F >= viewY && y <= viewY + viewHeight;
         if (visible) {
            boolean active = item == this.selected;
            boolean hovered = canvas.hit(120.0F, y, 480.0F, 49.0F) && canvas.mouseDesignY() >= viewY;
            int fill = active ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.16F) : (hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.SURFACE);
            int stroke = active ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.65F) : ClickGuiPalette.CARD_STROKE;
            canvas.outlinedRect(120.0F, y, 480.0F, 49.0F, fill, 11.0F, 0.5F, stroke);
            canvas.rect(128.0F, y + 7.0F, 35.0F, 35.0F, ClickGuiPalette.CONTROL, 9.0F);
            canvas.text(
               173.0F, y + 9.0F, 13.0F, fit(canvas, item.getName(), 300.0F, 13.0F), item.isEnabled() ? -1 : ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM
            );
            canvas.text(
               173.0F,
               y + 28.0F,
               11.0F,
               MenuText.ui("Target") + ": " + item.getCount() + (item.isScanCheapest() ? "  •  " + MenuText.ui("scan pages") : ""),
               ClickGuiPalette.TEXT_MUTED,
               UiFontStyle.REGULAR
            );
            renderToggle(canvas, 554.0F, y + 14.5F, item.isEnabled());
            this.rowHits.add(new ClickGuiCollectorPage.RowHit(120.0F, y, 480.0F, 49.0F, item));
            this.icons.add(new ClickGuiCollectorPage.IconHit(133.0F, y + 12.0F, item));
         }

         y += 55.0F;
      }

      canvas.popScissor();
      this.renderScrollbar(canvas, viewY, viewHeight, contentHeight);
   }

   private void renderEditor(ClickGuiCanvas canvas) {
      canvas.outlinedRect(624.0F, 147.0F, 275.0F, 464.0F, ClickGuiPalette.CONTROL, 15.0F, 0.5F, ClickGuiPalette.CARD_STROKE);
      canvas.text(638.0F, 158.0F, 13.0F, MenuText.ui("Editor"), -1, UiFontStyle.MEDIUM);
      CollectorItem item = this.selected;
      this.enabledHit = null;
      this.scanHit = null;
      this.minusHit = null;
      this.plusHit = null;
      this.startHit = null;
      this.conditionHits.clear();
      if (item != null) {
         float x = 638.0F;
         float width = 247.0F;
         float y = 196.0F;
         canvas.outlinedRect(x, y, width, 74.0F, ClickGuiPalette.SURFACE, 12.0F, 0.5F, ClickGuiPalette.CARD_STROKE);
         canvas.text(x + 12.0F, y + 12.0F, 14.0F, fit(canvas, item.getName(), width - 24.0F, 14.0F), -1, UiFontStyle.SEMIBOLD);
         canvas.text(x + 12.0F, y + 40.0F, 11.0F, MenuText.ui("Desired inventory amount"), ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR);
         y += 88.0F;
         this.renderSettingRow(canvas, x, y, width, MenuText.ui("Enabled"), item.isEnabled());
         this.enabledHit = new ClickGuiCollectorPage.Bounds(x, y, width, 44.0F);
         y += 52.0F;
         this.renderSettingRow(canvas, x, y, width, MenuText.ui("Scan cheapest pages"), item.isScanCheapest());
         this.scanHit = new ClickGuiCollectorPage.Bounds(x, y, width, 44.0F);
         y += 62.0F;
         canvas.text(x, y, 12.0F, MenuText.ui("Amount"), ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM);
         float controlY = y + 24.0F;
         float button = 34.0F;
         canvas.outlinedRect(x, controlY, button, button, ClickGuiPalette.SURFACE, 9.0F, 0.5F, ClickGuiPalette.CONTROL_STROKE);
         canvas.text(x + button / 2.0F, controlY + 8.0F, 16.0F, "−", -1, UiFontStyle.MEDIUM, TextAlign.CENTER);
         canvas.outlinedRect(
            x + button + 7.0F, controlY, width - button * 2.0F - 14.0F, button, ClickGuiPalette.SURFACE, 9.0F, 0.5F, ClickGuiPalette.CONTROL_STROKE
         );
         canvas.text(
            x + width / 2.0F, controlY + 10.0F, 13.0F, String.valueOf(item.getCount()), ClickGuiPalette.accent(), UiFontStyle.SEMIBOLD, TextAlign.CENTER
         );
         canvas.outlinedRect(x + width - button, controlY, button, button, ClickGuiPalette.SURFACE, 9.0F, 0.5F, ClickGuiPalette.CONTROL_STROKE);
         canvas.text(x + width - button / 2.0F, controlY + 8.0F, 16.0F, "+", -1, UiFontStyle.MEDIUM, TextAlign.CENTER);
         this.minusHit = new ClickGuiCollectorPage.Bounds(x, controlY, button, button);
         this.plusHit = new ClickGuiCollectorPage.Bounds(x + width - button, controlY, button, button);
         CollectorFeature feature = FeatureManager.INSTANCE.getFeature(CollectorFeature.class);
         boolean running = feature != null && feature.isRunning();
         float startY = 553.0F;
         this.renderConditions(canvas, item, x, controlY + 46.0F, width, Math.max(0.0F, startY - controlY - 55.0F));
         boolean hovered = canvas.hit(x, startY, width, 38.0F);
         canvas.rect(
            x,
            startY,
            width,
            38.0F,
            running ? ColorUtil.rgb(191, 139, 45) : (hovered ? ColorUtil.multiplyRgb(ClickGuiPalette.accent(), 1.08F) : ClickGuiPalette.accent()),
            10.0F
         );
         String label = running
            ? MenuText.ui("Working") + " " + (feature.progressIndex() + 1) + "/" + Math.max(1, feature.progressTotal())
            : MenuText.ui("Start Work");
         canvas.text(x + width / 2.0F, startY + 11.0F, 13.0F, label, -1, UiFontStyle.SEMIBOLD, TextAlign.CENTER);
         this.startHit = new ClickGuiCollectorPage.Bounds(x, startY, width, 38.0F);
      }
   }

   public boolean mousePressed(ClickGuiCanvas canvas, int button) {
      boolean left = button == 0;
      boolean right = button == 1;
      if (!left && !right) {
         return false;
      }

      for (ClickGuiCollectorPage.RowHit hit : this.rowHits) {
         if (hit.contains(canvas)) {
            if (left) {
               if (this.selected != hit.item()) {
                  this.conditionScroll = 0.0F;
               }

               this.selected = hit.item();
            } else {
               CollectorManager.get().setEnabled(hit.item(), !hit.item().isEnabled());
            }

            return true;
         }
      }

      if (left && this.selected != null) {
         if (contains(this.enabledHit, canvas)) {
            CollectorManager.get().setEnabled(this.selected, !this.selected.isEnabled());
         } else if (contains(this.scanHit, canvas)) {
            CollectorManager.get().setScanCheapest(this.selected, !this.selected.isScanCheapest());
         } else if (contains(this.minusHit, canvas)) {
            CollectorManager.get().setCount(this.selected, this.selected.getCount() - 1);
         } else if (contains(this.plusHit, canvas)) {
            CollectorManager.get().setCount(this.selected, this.selected.getCount() + 1);
         } else {
            if (this.pressCondition(canvas)) {
               return true;
            }

            if (contains(this.startHit, canvas)) {
               CollectorFeature feature = FeatureManager.INSTANCE.getFeature(CollectorFeature.class);
               if (feature != null && !feature.isRunning()) {
                  feature.start();
               }
            }
         }

         return canvas.hit(94.0F, 65.0F, 821.0F, 562.0F);
      } else {
         return canvas.hit(94.0F, 65.0F, 821.0F, 562.0F);
      }
   }

   public void scroll(ClickGuiCanvas canvas, double vertical) {
      if (canvas.hit(110.0F, 147.0F, 500.0F, 464.0F)) {
         this.scroll = clamp(this.scroll - (float)vertical * 42.0F, 0.0F, this.maxScroll);
      } else if (canvas.hit(624.0F, 147.0F, 275.0F, 464.0F)) {
         this.conditionScroll = clamp(this.conditionScroll - (float)vertical * 27.0F, 0.0F, this.maxConditionScroll);
      }
   }

   private void renderConditions(ClickGuiCanvas canvas, CollectorItem item, float x, float y, float width, float height) {
      List<CollectorCondition> conditions = item.getConditions();
      canvas.text(x, y, 11.0F, MenuText.ui("Requirements"), ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM);
      float viewY = y + 18.0F;
      float viewHeight = Math.max(0.0F, height - 18.0F);
      if (conditions.isEmpty()) {
         this.conditionScroll = 0.0F;
         this.maxConditionScroll = 0.0F;
         canvas.text(
            x,
            viewY + 4.0F,
            10.0F,
            item.getProfile() == null && item.getSignature() == null ? MenuText.ui("Base item match") : MenuText.ui("Exact item profile"),
            ClickGuiPalette.TEXT_MUTED,
            UiFontStyle.REGULAR
         );
      } else {
         float rowHeight = 25.0F;
         float contentHeight = conditions.size() * rowHeight;
         this.maxConditionScroll = Math.max(0.0F, contentHeight - viewHeight);
         this.conditionScroll = clamp(this.conditionScroll, 0.0F, this.maxConditionScroll);
         canvas.pushScissor(x, viewY, width, viewHeight);
         float rowY = viewY - this.conditionScroll;

         for (CollectorCondition condition : conditions) {
            if (rowY + 22.0F >= viewY && rowY <= viewY + viewHeight) {
               boolean hovered = canvas.hit(x, rowY, width, 22.0F) && canvas.mouseDesignY() >= viewY && canvas.mouseDesignY() <= viewY + viewHeight;
               canvas.outlinedRect(
                  x, rowY, width, 22.0F, hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.SURFACE, 7.0F, 0.5F, ClickGuiPalette.CARD_STROKE
               );
               float controlsWidth = condition.isAdjustable() ? 89.0F : 39.0F;
               canvas.text(
                  x + 8.0F,
                  rowY + 6.0F,
                  10.0F,
                  fit(canvas, condition.getLabel(), width - controlsWidth - 13.0F, 10.0F),
                  condition.isEnabled() ? ClickGuiPalette.TEXT_SECONDARY : ClickGuiPalette.TEXT_MUTED,
                  UiFontStyle.REGULAR
               );
               ClickGuiCollectorPage.Bounds minus = null;
               ClickGuiCollectorPage.Bounds plus = null;
               if (condition.isAdjustable()) {
                  float levelX = x + width - 84.0F;
                  canvas.text(levelX + 7.0F, rowY + 4.0F, 12.0F, "−", ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM, TextAlign.CENTER);
                  canvas.text(
                     levelX + 28.0F,
                     rowY + 6.0F,
                     10.0F,
                     Integer.toString(condition.getLevel()),
                     ClickGuiPalette.accent(),
                     UiFontStyle.SEMIBOLD,
                     TextAlign.CENTER
                  );
                  canvas.text(levelX + 49.0F, rowY + 4.0F, 12.0F, "+", ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM, TextAlign.CENTER);
                  minus = new ClickGuiCollectorPage.Bounds(levelX, rowY, 15.0F, 22.0F);
                  plus = new ClickGuiCollectorPage.Bounds(levelX + 42.0F, rowY, 15.0F, 22.0F);
               }

               float toggleX = x + width - 31.0F;
               renderMiniToggle(canvas, toggleX, rowY + 5.0F, condition.isEnabled());
               this.conditionHits
                  .add(new ClickGuiCollectorPage.ConditionHit(new ClickGuiCollectorPage.Bounds(toggleX - 3.0F, rowY, 34.0F, 22.0F), minus, plus, condition));
            }

            rowY += rowHeight;
         }

         canvas.popScissor();
         if (contentHeight > viewHeight && this.maxConditionScroll > 0.0F) {
            float thumb = Math.max(14.0F, viewHeight * viewHeight / contentHeight);
            float progress = this.conditionScroll / this.maxConditionScroll;
            canvas.rect(x + width - 2.0F, viewY, 2.0F, viewHeight, ClickGuiPalette.CONTROL_STROKE, 1.0F);
            canvas.rect(x + width - 2.0F, viewY + (viewHeight - thumb) * progress, 2.0F, thumb, ClickGuiPalette.accent(), 1.0F);
         }
      }
   }

   private boolean pressCondition(ClickGuiCanvas canvas) {
      for (ClickGuiCollectorPage.ConditionHit hit : this.conditionHits) {
         CollectorCondition condition = hit.condition();
         if (contains(hit.toggle(), canvas)) {
            CollectorManager.get().setConditionEnabled(condition, !condition.isEnabled());
            return true;
         }

         if (contains(hit.minus(), canvas)) {
            CollectorManager.get().setConditionLevel(condition, condition.getLevel() - 1);
            return true;
         }

         if (contains(hit.plus(), canvas)) {
            CollectorManager.get().setConditionLevel(condition, condition.getLevel() + 1);
            return true;
         }
      }

      return false;
   }

   private void renderSettingRow(ClickGuiCanvas canvas, float x, float y, float width, String label, boolean enabled) {
      boolean hovered = canvas.hit(x, y, width, 44.0F);
      canvas.outlinedRect(x, y, width, 44.0F, hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.SURFACE, 10.0F, 0.5F, ClickGuiPalette.CARD_STROKE);
      canvas.text(x + 11.0F, y + 15.0F, 12.0F, label, ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM);
      renderToggle(canvas, x + width - 44.0F, y + 12.0F, enabled);
   }

   private static void renderToggle(ClickGuiCanvas canvas, float x, float y, boolean enabled) {
      canvas.rect(x, y, 33.0F, 20.0F, enabled ? ClickGuiPalette.accent() : ClickGuiPalette.OFF_TRACK, 10.0F);
      canvas.rect(x + (enabled ? 16.0F : 3.0F), y + 2.5F, 15.0F, 15.0F, enabled ? -1 : ClickGuiPalette.OFF_KNOB, 7.5F);
   }

   private static void renderMiniToggle(ClickGuiCanvas canvas, float x, float y, boolean enabled) {
      canvas.rect(x, y, 25.0F, 13.0F, enabled ? ClickGuiPalette.accent() : ClickGuiPalette.OFF_TRACK, 6.5F);
      canvas.rect(x + (enabled ? 13.0F : 2.0F), y + 1.5F, 10.0F, 10.0F, enabled ? -1 : ClickGuiPalette.OFF_KNOB, 5.0F);
   }

   private void renderScrollbar(ClickGuiCanvas canvas, float y, float height, float content) {
      if (!(content <= height) && !(this.maxScroll <= 0.0F)) {
         float trackX = 604.0F;
         float thumb = Math.max(28.0F, height * height / content);
         float progress = this.scroll / this.maxScroll;
         canvas.rect(trackX, y, 3.0F, height, ClickGuiPalette.CONTROL_STROKE, 1.5F);
         canvas.rect(trackX, y + (height - thumb) * progress, 3.0F, thumb, ClickGuiPalette.accent(), 1.5F);
      }
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

   private static String fit(ClickGuiCanvas canvas, String raw, float maxWidth, float size) {
      if (canvas.textWidth(raw, size, UiFontStyle.MEDIUM) <= maxWidth) {
         return raw;
      }

      int end = raw.length();

      while (end > 0 && canvas.textWidth(raw.substring(0, end) + "...", size, UiFontStyle.MEDIUM) > maxWidth) {
         end--;
      }

      return raw.substring(0, end) + "...";
   }

   private static boolean contains(ClickGuiCollectorPage.Bounds bounds, ClickGuiCanvas canvas) {
      return bounds != null && bounds.contains(canvas);
   }

   private static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   @Environment(EnvType.CLIENT)
   private record Bounds(float x, float y, float width, float height) {
      private boolean contains(ClickGuiCanvas canvas) {
         return canvas.hit(this.x, this.y, this.width, this.height);
      }
   }

   @Environment(EnvType.CLIENT)
   private record ConditionHit(
      ClickGuiCollectorPage.Bounds toggle, ClickGuiCollectorPage.Bounds minus, ClickGuiCollectorPage.Bounds plus, CollectorCondition condition
   ) {
   }

   @Environment(EnvType.CLIENT)
   private record IconHit(float x, float y, CollectorItem item) {
   }

   @Environment(EnvType.CLIENT)
   private record RowHit(float x, float y, float width, float height, CollectorItem item) {
      private boolean contains(ClickGuiCanvas canvas) {
         return canvas.hit(this.x, this.y, this.width, this.height);
      }
   }
}
