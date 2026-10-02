package org.ryzen.menu.clickgui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ButtonSetting;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.Setting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class ClickGuiModulesPage {
   private static final float PANEL_X = 94.0F;
   private static final float PANEL_Y = 65.0F;
   private static final float PANEL_WIDTH = 821.0F;
   private static final float PANEL_HEIGHT = 562.0F;
   private static final float PANEL_BOTTOM = 627.0F;
   private static final float CONTENT_X = 115.0F;
   private static final float CONTENT_Y = 80.0F;
   private static final float CONTENT_WIDTH = 779.0F;
   private static final float HEADER_HEIGHT = 40.0F;
   private static final float DESCRIPTION_TOP = 24.0F;
   private static final float DESCRIPTION_LINE_HEIGHT = 14.0F;
   private static final float DESCRIPTION_WIDTH = 610.0F;
   private static final float HEADER_GAP = 12.0F;
   private static final float SETTINGS_GAP = 17.0F;
   private static final float ROW_GAP = 12.0F;
   private static final float CONTROL_Y = 26.0F;
   private static final float CONTROL_HEIGHT = 30.0F;
   private static final float COLUMN_GAP = 14.0F;
   private static final float COLUMN_WIDTH = 382.5F;
   private static final float MODE_LIST_TOP = 6.0F;
   private static final float CHIP_ROW_BOTTOM = 6.0F;
   private static final float MODE_LIST_PADDING = 5.0F;
   private static final float MODE_ITEM_HEIGHT = 26.0F;
   private static final float MODE_ITEM_GAP = 4.0F;
   private static final float BIND_X = 754.0F;
   private static final float BIND_WIDTH = 66.0F;
   private static final float TOGGLE_X = 836.0F;
   private static final float TOGGLE_WIDTH = 33.0F;
   private static final float COLOR_BAR_X = 613.0F;
   private static final float COLOR_BAR_WIDTH = 271.0F;
   private FeatureCategory category = FeatureCategory.COMBAT;
   private List<Feature> features = List.of();
   private Feature selectedFeature;
   private final List<ClickGuiModulesPage.ModuleLayout> modules = new ArrayList<>();
   private float scrollOffset;
   private float maxScroll;
   private NumberSetting activeNumber;
   private ColorSetting activeColor;
   private TextSetting focusedText;
   private ModeSetting openMode;
   private Feature pendingReveal;
   private Feature capturedFeature;
   private InputBindSetting capturedInput;

   public void layout(ClickGuiCanvas canvas, FeatureCategory category) {
      FeatureCategory resolved = category == null ? FeatureCategory.COMBAT : category;
      boolean categoryChanged = this.category != resolved;
      this.category = resolved;
      this.features = List.copyOf(FeatureManager.INSTANCE.getFeatures(resolved));
      if (categoryChanged) {
         this.selectedFeature = null;
         this.scrollOffset = 0.0F;
         this.clearTransientState();
      } else if (this.selectedFeature != null && !this.features.contains(this.selectedFeature)) {
         this.selectedFeature = null;
         this.clearTransientState();
      }

      float contentHeight = this.calculateContentHeight(canvas);
      this.maxScroll = Math.max(0.0F, 80.0F + contentHeight - 614.0F);
      this.scrollOffset = clamp(this.scrollOffset, 0.0F, this.maxScroll);
      this.rebuildLayouts(canvas);
      if (this.pendingReveal != null) {
         this.revealPending(canvas);
      }
   }

   public void render(ClickGuiCanvas canvas) {
      canvas.outlinedRect(94.0F, 65.0F, 821.0F, 562.0F, ClickGuiPalette.SURFACE, 17.0F, 0.5F, ClickGuiPalette.STROKE);
      canvas.pushScissor(94.0F, 65.0F, 821.0F, 562.0F);
      if (this.modules.isEmpty()) {
         canvas.text(504.5F, 287.0F, 14.0F, MenuText.ui("No functions in this category"), ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM, TextAlign.CENTER);
      } else {
         for (ClickGuiModulesPage.ModuleLayout module : this.modules) {
            if (visible(module.y, module.height)) {
               this.renderModule(canvas, module);
            }
         }
      }

      canvas.popScissor();
   }

   public boolean mousePressed(ClickGuiCanvas canvas, int button) {
      if (this.capturedFeature == null && this.capturedInput == null) {
         if (!canvas.hit(94.0F, 65.0F, 821.0F, 562.0F)) {
            this.focusedText = null;
            this.openMode = null;
            return false;
         }

         boolean left = button == 0;
         if (!left && button != 1) {
            return true;
         }

         TextSetting previousText = this.focusedText;
         this.focusedText = null;

         for (ClickGuiModulesPage.ModuleLayout module : this.modules) {
            if (visible(module.y, module.height)) {
               if (left && canvas.hit(836.0F, module.y, 33.0F, 20.0F)) {
                  if (module.feature.isToggleable()) {
                     module.feature.toggle();
                  }

                  this.openMode = null;
                  return true;
               }

               if (left && module.feature.supportsBinds() && canvas.hit(754.0F, module.y, 66.0F, 20.0F)) {
                  this.capturedFeature = module.feature;
                  this.capturedInput = null;
                  this.openMode = null;
                  return true;
               }

               if (canvas.hit(115.0F, module.y, 779.0F, this.headerHeight(canvas, module.feature))) {
                  if (hasSettings(module.feature)) {
                     this.selectedFeature = this.selectedFeature == module.feature ? null : module.feature;
                     this.scrollOffset = clamp(this.scrollOffset, 0.0F, this.maxScroll);
                     this.clearTransientState();
                     this.layout(canvas, this.category);
                  }

                  return true;
               }

               if (left && module.feature == this.selectedFeature) {
                  for (ClickGuiModulesPage.RowLayout row : module.rows) {
                     if (visible(row.y, row.height)) {
                        ModeSetting openBefore = this.openMode;
                        if (this.pressRow(canvas, row)) {
                           if (this.openMode == openBefore && row.setting != openBefore) {
                              this.openMode = null;
                           }

                           return true;
                        }
                     }
                  }
               }
            }
         }

         this.focusedText = previousText == null ? null : this.focusedText;
         this.openMode = null;
         return true;
      } else {
         this.applyMouseBind(button);
         return true;
      }
   }

   public void drag(ClickGuiCanvas canvas) {
      if (this.activeNumber != null) {
         ClickGuiModulesPage.RowLayout row = this.findRow(this.activeNumber);
         if (row != null) {
            this.updateNumber(canvas, this.activeNumber);
         }
      }

      if (this.activeColor != null) {
         ClickGuiModulesPage.RowLayout row = this.findRow(this.activeColor);
         if (row != null) {
            this.updateColor(canvas, this.activeColor);
         }
      }
   }

   public void release() {
      this.activeNumber = null;
      this.activeColor = null;
   }

   public void scroll(double vertical) {
      this.scrollOffset = clamp(this.scrollOffset - (float)vertical * 42.0F, 0.0F, this.maxScroll);
   }

   public boolean keyPressed(int key) {
      if (this.capturedFeature == null && this.capturedInput == null) {
         if (this.openMode != null && key == 256) {
            this.openMode = null;
            return true;
         }

         if (this.focusedText == null) {
            return false;
         }

         if (key == 256 || key == 257 || key == 335) {
            this.focusedText = null;
            return true;
         }

         if (key != 259 && key != 261) {
            return false;
         }

         String value = this.focusedText.getValue();
         if (!value.isEmpty()) {
            int codePoint = value.codePointBefore(value.length());
            this.focusedText.setValue(value.substring(0, value.length() - Character.charCount(codePoint)));
         }

         return true;
      } else {
         if (key == 256) {
            this.clearCapture();
            return true;
         }

         if (key != 259 && key != 261) {
            if (this.capturedFeature != null) {
               this.capturedFeature.setKeyBind(key);
            } else {
               this.capturedInput.setKey(key);
            }

            this.clearCapture();
            return true;
         } else {
            if (this.capturedFeature != null) {
               this.capturedFeature.clearBind();
            } else {
               this.capturedInput.clear();
            }

            this.clearCapture();
            return true;
         }
      }
   }

   public boolean charTyped(int codePoint) {
      if (this.focusedText != null && Character.isValidCodePoint(codePoint) && !Character.isISOControl(codePoint)) {
         String value = this.focusedText.getValue();
         if (value.codePointCount(0, value.length()) >= this.focusedText.getMaxLength()) {
            return true;
         }

         this.focusedText.setValue(value + Character.toString(codePoint));
         return true;
      } else {
         return false;
      }
   }

   public boolean isCapturingBind() {
      return this.capturedFeature != null || this.capturedInput != null;
   }

   public void select(Feature feature) {
      if (feature != null) {
         this.selectedFeature = hasSettings(feature) ? feature : null;
         this.pendingReveal = feature;
         this.clearTransientState();
      }
   }

   private void revealPending(ClickGuiCanvas canvas) {
      Feature target = this.pendingReveal;
      this.pendingReveal = null;

      for (ClickGuiModulesPage.ModuleLayout module : this.modules) {
         if (module.feature == target) {
            float absolute = module.y - 80.0F + this.scrollOffset;
            this.scrollOffset = clamp(absolute, 0.0F, this.maxScroll);
            this.rebuildLayouts(canvas);
            return;
         }
      }
   }

   public String headerTitle() {
      return switch (this.category) {
         case COMBAT -> "Бой";
         case MOVEMENT -> "Движение";
         case VISUAL -> "Визуал";
         case PLAYER -> "Игрок";
         case MISC -> "Разное";
         case PVE -> "PVE";
      };
   }

   public String headerDescription() {
      return switch (this.category) {
         case COMBAT -> "Боевые функции и настройки атаки.";
         case MOVEMENT -> "Функции перемещения и скорости.";
         case VISUAL -> "Визуальные функции и оформление игры.";
         case PLAYER -> "Функции игрока и инвентаря.";
         case MISC -> "Дополнительные функции клиента.";
         case PVE -> "Автоматизация PVE и серверных задач.";
      };
   }

   private float calculateContentHeight(ClickGuiCanvas canvas) {
      float cursor = 0.0F;

      for (Feature feature : this.features) {
         float moduleHeight = this.headerHeight(canvas, feature);
         if (feature == this.selectedFeature) {
            float rowCursor = this.headerHeight(canvas, feature) + 17.0F;
            List<Setting<?>> settings = visibleSettings(feature);

            for (int index = 0; index < settings.size(); index++) {
               rowCursor += this.rowHeight(canvas, settings.get(index)) + 12.0F;
               if (pairsWithNext(settings, index)) {
                  index++;
               }
            }

            moduleHeight = Math.max(this.headerHeight(canvas, feature), rowCursor + 3.0F);
         }

         cursor += moduleHeight + 12.0F;
      }

      return Math.max(0.0F, cursor - 12.0F);
   }

   private void rebuildLayouts(ClickGuiCanvas canvas) {
      this.modules.clear();
      float cursor = 0.0F;

      for (Feature feature : this.features) {
         float moduleY = 80.0F + cursor - this.scrollOffset;
         ClickGuiModulesPage.ModuleLayout module = new ClickGuiModulesPage.ModuleLayout(feature, moduleY);
         float moduleHeight = this.headerHeight(canvas, feature);
         if (feature == this.selectedFeature) {
            float rowY = moduleY + this.headerHeight(canvas, feature) + 17.0F;
            List<Setting<?>> settings = visibleSettings(feature);

            for (int index = 0; index < settings.size(); index++) {
               Setting<?> setting = settings.get(index);
               float height = this.rowHeight(canvas, setting);
               boolean paired = pairsWithNext(settings, index);
               float width = setting instanceof InputBindSetting ? 382.5F : 779.0F;
               ClickGuiModulesPage.RowLayout row = new ClickGuiModulesPage.RowLayout(feature, setting, 115.0F, width, rowY, height);
               if (setting instanceof MultiSelectSetting multi) {
                  this.buildOptionHits(canvas, row, multi.getOptions(), multi::isSelected);
               } else if (setting instanceof ModeSetting mode && mode.isChips()) {
                  this.buildOptionHits(canvas, row, mode.getModes(), mode::is);
               }

               module.rows.add(row);
               if (paired) {
                  Setting<?> second = settings.get(++index);
                  module.rows.add(new ClickGuiModulesPage.RowLayout(feature, second, 511.5F, 382.5F, rowY, this.rowHeight(canvas, second)));
               }

               rowY += height + 12.0F;
            }

            moduleHeight = Math.max(this.headerHeight(canvas, feature), rowY - moduleY + 3.0F);
         }

         module.height = moduleHeight;
         this.modules.add(module);
         cursor += moduleHeight + 12.0F;
      }
   }

   private static boolean pairsWithNext(List<Setting<?>> settings, int index) {
      return settings.get(index) instanceof InputBindSetting && index + 1 < settings.size() && settings.get(index + 1) instanceof InputBindSetting;
   }

   private static boolean hasSettings(Feature feature) {
      return !visibleSettings(feature).isEmpty();
   }

   private static boolean hasDescription(Feature feature) {
      String description = feature.getDescription();
      return description != null && !description.isBlank();
   }

   private float headerHeight(ClickGuiCanvas canvas, Feature feature) {
      if (!hasDescription(feature)) {
         return 40.0F;
      }

      int lines = wrapDescription(canvas, MenuText.featureDescription(feature.getDescription()), 610.0F).size();
      return 24.0F + lines * 14.0F + 8.0F;
   }

   private static List<String> wrapDescription(ClickGuiCanvas canvas, String raw, float maxWidth) {
      List<String> lines = new ArrayList<>();
      String value = raw == null ? "" : raw.strip();
      if (value.isEmpty()) {
         return lines;
      }

      StringBuilder line = new StringBuilder();

      for (String word : value.split(" +")) {
         String candidate = line.isEmpty() ? word : line + " " + word;
         if (!line.isEmpty() && canvas.textWidth(candidate, 12.0F, UiFontStyle.REGULAR) > maxWidth) {
            lines.add(line.toString());
            line.setLength(0);
            line.append(word);
         } else {
            line.setLength(0);
            line.append(candidate);
         }
      }

      if (!line.isEmpty()) {
         lines.add(line.toString());
      }

      return lines;
   }

   private void renderModule(ClickGuiCanvas canvas, ClickGuiModulesPage.ModuleLayout module) {
      float y = module.y;
      boolean selected = module.feature == this.selectedFeature;
      float headerHeight = this.headerHeight(canvas, module.feature);
      if (canvas.hit(115.0F, y, 779.0F, headerHeight)) {
         canvas.rect(110.0F, y - 5.0F, 789.0F, headerHeight + 8.0F, ClickGuiPalette.CONTROL, 10.0F);
      }

      canvas.texture(115.0F, y + 4.545F, 11.0F, 12.0F, featureIcon(module.feature.getCategory()), ClickGuiPalette.accent());
      canvas.text(136.0F, y + 2.0F, 15.0F, fit(canvas, module.feature.getName(), 570.0F, 15.0F, UiFontStyle.MEDIUM), -1, UiFontStyle.MEDIUM);
      List<String> lines = wrapDescription(canvas, MenuText.featureDescription(module.feature.getDescription()), 610.0F);

      for (int line = 0; line < lines.size(); line++) {
         canvas.text(115.0F, y + 24.0F + line * 14.0F, 12.0F, lines.get(line), ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.REGULAR);
      }

      if (module.feature.supportsBinds()) {
         boolean listening = this.capturedFeature == module.feature;
         canvas.rect(754.0F, y, 66.0F, 20.0F, listening ? ClickGuiPalette.CONTROL_ACTIVE : ClickGuiPalette.accent(), 7.0F);
         canvas.texture(761.0F, y + 5.0F, 13.0F, 9.0F, Textures.Icons.KEYBOARD, -1);
         String bind = listening ? "..." : module.feature.getBind().getDisplayValue();
         canvas.text(779.0F, y + 3.0F, 12.0F, fit(canvas, bind.toUpperCase(Locale.ROOT), 36.0F, 12.0F, UiFontStyle.MEDIUM), -1, UiFontStyle.MEDIUM);
      }

      renderToggle(canvas, 836.0F, y, module.feature.isEnabled(), module.feature.isToggleable());
      if (hasSettings(module.feature)) {
         canvas.texture(885.0F, y + 7.431F, 9.036F, 5.137F, selected ? Textures.Icons.CHEVRON_UP : Textures.Icons.CHEVRON_DOWN, ClickGuiPalette.TEXT_SECONDARY);
      }

      canvas.rect(115.0F, y + headerHeight - 1.0F, 779.0F, 1.0F, ClickGuiPalette.DIVIDER, 0.0F);
      if (selected) {
         for (int index = 0; index < module.rows.size(); index++) {
            ClickGuiModulesPage.RowLayout row = module.rows.get(index);
            if (visible(row.y, row.height)) {
               this.renderRow(canvas, row);
            }

            boolean lastOnLine = index == module.rows.size() - 1 || module.rows.get(index + 1).y != row.y;
            if (lastOnLine && index < module.rows.size() - 1) {
               float divider = row.y + row.height - 6.0F;
               if (visible(divider, 1.0F)) {
                  canvas.rect(115.0F, divider, 779.0F, 1.0F, ClickGuiPalette.DIVIDER, 0.0F);
               }
            }
         }
      }
   }

   private void renderRow(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row) {
      Setting<?> setting = row.setting;
      if (setting instanceof NumberSetting number) {
         this.renderNumber(canvas, row, number);
      } else if (setting instanceof ModeSetting mode) {
         if (mode.isChips()) {
            this.renderModeChips(canvas, row, mode);
         } else {
            this.renderMode(canvas, row, mode);
         }
      } else if (setting instanceof BooleanSetting bool) {
         this.renderBoolean(canvas, row, bool);
      } else if (setting instanceof MultiSelectSetting multi) {
         this.renderMultiSelect(canvas, row, multi);
      } else if (setting instanceof ColorSetting color) {
         this.renderColor(canvas, row, color);
      } else if (setting instanceof TextSetting text) {
         this.renderText(canvas, row, text);
      } else if (setting instanceof InputBindSetting input) {
         this.renderInputBind(canvas, row, input);
      } else if (setting instanceof ButtonSetting button) {
         this.renderButton(canvas, row, button);
      } else {
         this.renderUnknown(canvas, row);
      }
   }

   private void renderNumber(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, NumberSetting setting) {
      this.renderSettingHeading(canvas, row, setting, Textures.Icons.OPTION);
      canvas.text(894.0F, row.y + 1.5F, 12.0F, MenuText.numberValue(setting), ClickGuiPalette.accent(), UiFontStyle.MEDIUM, TextAlign.RIGHT);
      float progress = clamp((float)setting.getProgress(), 0.0F, 1.0F);
      float trackY = row.y + 26.0F + 4.0F;
      canvas.rect(115.0F, trackY, 779.0F, 3.0F, ClickGuiPalette.CONTROL, 1.5F);
      canvas.rect(115.0F, trackY, 779.0F * progress, 3.0F, ClickGuiPalette.accent(), 1.5F);
      float knob = 121.5F + 766.0F * progress;
      canvas.rect(knob - 6.5F, row.y + 26.0F - 1.0F, 13.0F, 13.0F, -1, 6.5F);
   }

   private void renderMode(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, ModeSetting setting) {
      this.renderSettingHeading(canvas, row, setting, Textures.Icons.CHEVRONS_LEFT_RIGHT);
      int index = Math.max(0, setting.indexOf(setting.getValue())) + 1;
      renderCounter(canvas, row.y + 3.0F, String.valueOf(index), String.valueOf(setting.getModes().size()));
      boolean open = this.openMode == setting;
      boolean hovered = canvas.hit(115.0F, row.y + 26.0F, 779.0F, 30.0F);
      canvas.outlinedRect(
         115.0F,
         row.y + 26.0F,
         779.0F,
         30.0F,
         !open && !hovered ? ClickGuiPalette.CONTROL : ClickGuiPalette.CONTROL_HOVER,
         7.0F,
         0.5F,
         open ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.8F) : ClickGuiPalette.CONTROL_STROKE
      );
      canvas.texture(126.0F, row.y + 26.0F + 11.0F, 12.595F, 8.963F, Textures.Icons.FILTER_LINES, ClickGuiPalette.TEXT_SECONDARY);
      canvas.text(
         148.0F,
         row.y + 26.0F + 8.0F,
         12.0F,
         fit(canvas, MenuText.option(setting.getValue()), 709.0F, 12.0F, UiFontStyle.MEDIUM),
         ClickGuiPalette.TEXT_ACTIVE_VALUE,
         UiFontStyle.MEDIUM
      );
      canvas.texture(883.0F, row.y + 26.0F + 14.0F, 8.0F, 4.0F, open ? Textures.Icons.CHEVRON_UP : Textures.Icons.CHEVRON_DOWN, ClickGuiPalette.TEXT_SECONDARY);
      if (open) {
         this.renderModeList(canvas, row, setting);
      }
   }

   private void renderModeList(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, ModeSetting setting) {
      List<String> modes = setting.getModes();
      float listY = modeListY(row);
      canvas.outlinedRect(115.0F, listY, 779.0F, modeListHeight(modes.size()), ClickGuiPalette.CONTROL, 8.0F, 0.5F, ClickGuiPalette.CONTROL_STROKE);
      float itemX = 120.0F;
      float itemWidth = 769.0F;
      int selected = setting.indexOf(setting.getValue());

      for (int index = 0; index < modes.size(); index++) {
         float itemY = modeItemY(row, index);
         boolean current = index == selected;
         boolean hovered = canvas.hit(itemX, itemY, itemWidth, 26.0F);
         if (current || hovered) {
            canvas.rect(
               itemX, itemY, itemWidth, 26.0F, current ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.18F) : ClickGuiPalette.CONTROL_HOVER, 6.0F
            );
         }

         canvas.text(
            itemX + 11.0F,
            itemY + 7.0F,
            12.0F,
            fit(canvas, MenuText.option(modes.get(index)), itemWidth - 42.0F, 12.0F, UiFontStyle.MEDIUM),
            current ? -1 : (hovered ? ClickGuiPalette.TEXT_SECONDARY : ClickGuiPalette.TEXT_MUTED),
            UiFontStyle.MEDIUM
         );
         if (current) {
            canvas.texture(itemX + itemWidth - 20.0F, itemY + 9.5F, 9.0F, 6.5F, Textures.Icons.CHECK, ClickGuiPalette.accent());
         }
      }
   }

   private static float modeListY(ClickGuiModulesPage.RowLayout row) {
      return row.y + 26.0F + 30.0F + 6.0F;
   }

   private static float modeItemY(ClickGuiModulesPage.RowLayout row, int index) {
      return modeListY(row) + 5.0F + index * 30.0F;
   }

   private static float modeListHeight(int count) {
      return 10.0F + count * 26.0F + Math.max(0, count - 1) * 4.0F;
   }

   private static void renderCounter(ClickGuiCanvas canvas, float y, String current, String total) {
      float right = 894.0F;
      canvas.text(right, y, 12.0F, total, ClickGuiPalette.TEXT_MUTED, UiFontStyle.MEDIUM, TextAlign.RIGHT);
      float slashRight = right - canvas.textWidth(total, 12.0F, UiFontStyle.MEDIUM) - 3.0F;
      canvas.text(slashRight, y, 12.0F, "/", ColorUtil.rgba(255, 255, 255, 125), UiFontStyle.MEDIUM, TextAlign.RIGHT);
      float currentRight = slashRight - canvas.textWidth("/", 12.0F, UiFontStyle.MEDIUM) - 3.0F;
      canvas.text(currentRight, y, 12.0F, current, ClickGuiPalette.accent(), UiFontStyle.MEDIUM, TextAlign.RIGHT);
   }

   private void renderBoolean(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, BooleanSetting setting) {
      this.renderSettingHeading(canvas, row, setting, Textures.Icons.OPTION, 20.0F, 11.0F, 28.0F);
      renderToggle(canvas, 861.0F, row.y - 1.0F, setting.getValue(), true);
   }

   private void renderMultiSelect(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, MultiSelectSetting setting) {
      this.renderSettingHeading(canvas, row, setting, Textures.Icons.BOXES);
      this.renderChips(canvas, row, setting::isSelected);
   }

   private void renderModeChips(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, ModeSetting setting) {
      this.renderSettingHeading(canvas, row, setting, Textures.Icons.CHEVRONS_LEFT_RIGHT);
      int index = Math.max(0, setting.indexOf(setting.getValue())) + 1;
      renderCounter(canvas, row.y + 3.0F, String.valueOf(index), String.valueOf(setting.getModes().size()));
      this.renderChips(canvas, row, setting::is);
   }

   private void renderChips(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, Predicate<String> selectedTest) {
      for (ClickGuiModulesPage.OptionHit option : row.options) {
         boolean selected = selectedTest.test(option.option);
         boolean hovered = canvas.hit(option.x, option.y, option.width, option.height);
         int fill = selected ? ClickGuiPalette.accent() : (hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.CONTROL);
         canvas.rect(option.x, option.y, option.width, option.height, fill, 13.0F);
         if (selected) {
            canvas.rect(option.x + 5.0F, option.y + 5.0F, 16.0F, 16.0F, -1, 8.0F);
            canvas.texture(option.x + 8.5F, option.y + 9.5F, 9.0F, 6.5F, Textures.Icons.CHECK, ClickGuiPalette.accent());
         }

         canvas.text(
            option.x + chipTextX(selected),
            option.y + 6.0F,
            12.0F,
            fit(canvas, MenuText.option(option.option), option.width - chipPadding(selected), 12.0F, UiFontStyle.REGULAR),
            selected ? -1 : ClickGuiPalette.TEXT_FAINT,
            UiFontStyle.REGULAR
         );
      }
   }

   private void renderColor(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, ColorSetting setting) {
      this.renderSettingHeading(canvas, row, setting, Textures.Icons.PALETTE);
      boolean hovered = canvas.hit(115.0F, row.y + 26.0F, 779.0F, 30.0F);
      canvas.outlinedRect(
         115.0F, row.y + 26.0F, 779.0F, 30.0F, hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.CONTROL, 7.0F, 0.5F, ClickGuiPalette.STROKE_SOFT
      );
      canvas.rect(121.0F, row.y + 26.0F + 5.0F, 20.0F, 20.0F, setting.getValue(), 6.0F);
      String hex = ColorUtil.toHex(setting.getValue());
      canvas.text(151.0F, row.y + 26.0F + 9.0F, 12.0F, hex.substring(0, Math.min(7, hex.length())), ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.REGULAR);
      float segment = 45.166668F;

      for (int index = 0; index < 6; index++) {
         int left = ColorUtil.hue(index / 6.0F);
         int right = ColorUtil.hue((index + 1) / 6.0F);
         canvas.rect(613.0F + segment * index, row.y + 26.0F + 8.0F, segment + 0.5F, 14.0F, left, right, right, left, index != 0 && index != 5 ? 0.0F : 4.0F);
      }

      float hue = ColorUtil.hsv(setting.getValue())[0];
      float knobX = 613.0F + hue * 271.0F;
      canvas.outlinedRect(knobX - 4.0F, row.y + 26.0F + 6.0F, 8.0F, 18.0F, 0, 4.0F, 1.5F, -1);
   }

   private void renderText(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, TextSetting setting) {
      this.renderSettingHeading(canvas, row, setting, Textures.Icons.CHEVRONS_LEFT_RIGHT_ELLIPSIS);
      boolean focused = this.focusedText == setting;
      boolean hovered = canvas.hit(115.0F, row.y + 26.0F, 779.0F, 30.0F);
      canvas.outlinedRect(
         115.0F,
         row.y + 26.0F,
         779.0F,
         30.0F,
         hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.CONTROL,
         7.0F,
         0.5F,
         focused ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.8F) : ClickGuiPalette.STROKE_SOFT
      );
      String value = setting.isSecret() ? "*".repeat(setting.getValue().codePointCount(0, setting.getValue().length())) : setting.getValue();
      String shown = fit(canvas, value, 751.0F, 12.0F, UiFontStyle.REGULAR);
      canvas.text(
         127.0F,
         row.y + 26.0F + 9.0F,
         12.0F,
         shown.isEmpty() && !focused ? MenuText.ui("Enter value...") : shown,
         shown.isEmpty() ? ClickGuiPalette.TEXT_MUTED : ClickGuiPalette.TEXT_SECONDARY,
         UiFontStyle.REGULAR
      );
      if (focused && System.currentTimeMillis() / 500L % 2L == 0L) {
         float caretX = 127.0F + canvas.textWidth(shown, 12.0F, UiFontStyle.REGULAR) + 1.0F;
         canvas.rect(Math.min(caretX, 884.0F), row.y + 26.0F + 7.0F, 1.0F, 16.0F, ClickGuiPalette.accent(), 0.0F);
      }
   }

   private void renderInputBind(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, InputBindSetting setting) {
      this.renderSettingHeading(canvas, row, setting, Textures.Icons.COMMAND);
      boolean listening = this.capturedInput == setting;
      String raw = listening ? "..." : setting.getDisplayValue().toUpperCase(Locale.ROOT);
      int color = listening ? -1 : ClickGuiPalette.accent();
      canvas.texture(row.x, row.y + 26.0F + 2.0F, 13.0F, 9.0F, Textures.Icons.KEYBOARD, color);
      canvas.text(row.x + 19.0F, row.y + 26.0F, 12.0F, fit(canvas, raw, row.width - 19.0F, 12.0F, UiFontStyle.MEDIUM), color, UiFontStyle.MEDIUM);
   }

   private void renderButton(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, ButtonSetting setting) {
      this.renderSettingHeading(canvas, row, setting, Textures.Icons.CIRCLE_PLUS);
      boolean hovered = canvas.hit(115.0F, row.y + 26.0F, 779.0F, 30.0F);
      canvas.rect(115.0F, row.y + 26.0F, 779.0F, 30.0F, hovered ? ColorUtil.multiplyRgb(ClickGuiPalette.accent(), 1.08F) : ClickGuiPalette.accent(), 8.0F);
      canvas.text(
         504.5F,
         row.y + 26.0F + 8.0F,
         12.0F,
         fit(canvas, MenuText.ui(setting.getButtonLabel()), 739.0F, 12.0F, UiFontStyle.MEDIUM),
         -1,
         UiFontStyle.MEDIUM,
         TextAlign.CENTER
      );
   }

   private void renderUnknown(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row) {
      this.renderSettingHeading(canvas, row, row.setting, Textures.Icons.OPTION);
      canvas.text(894.0F, row.y + 3.0F, 12.0F, String.valueOf(row.setting.getValue()), ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.REGULAR, TextAlign.RIGHT);
   }

   private void renderSettingHeading(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, Setting<?> setting, class_2960 icon) {
      this.renderSettingHeading(canvas, row, setting, icon, 13.0F, 13.0F, 21.0F);
   }

   private void renderSettingHeading(
      ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, Setting<?> setting, class_2960 icon, float iconWidth, float iconHeight, float titleX
   ) {
      canvas.texture(row.x, row.y + 3.0F, iconWidth, iconHeight, icon, ClickGuiPalette.accent());
      canvas.text(
         row.x + titleX,
         row.y,
         15.0F,
         fit(canvas, MenuText.setting(row.feature.getName(), setting.getName()), Math.min(590.0F, row.width - titleX - 8.0F), 15.0F, UiFontStyle.MEDIUM),
         -1,
         UiFontStyle.MEDIUM
      );
   }

   private boolean pressRow(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row) {
      Setting<?> setting = row.setting;
      if (setting instanceof NumberSetting number && canvas.hit(115.0F, row.y + 26.0F - 4.0F, 779.0F, 26.0F)) {
         this.activeNumber = number;
         this.updateNumber(canvas, number);
         return true;
      } else if (setting instanceof ModeSetting mode && mode.isChips()) {
         for (ClickGuiModulesPage.OptionHit option : row.options) {
            if (canvas.hit(option.x, option.y, option.width, option.height)) {
               mode.setValue(option.option);
               this.layout(canvas, this.category);
               return true;
            }
         }

         return false;
      } else {
         if (setting instanceof ModeSetting dropdown) {
            if (this.openMode == dropdown) {
               List<String> modes = dropdown.getModes();

               for (int index = 0; index < modes.size(); index++) {
                  if (canvas.hit(120.0F, modeItemY(row, index), 769.0F, 26.0F)) {
                     dropdown.setValue(modes.get(index));
                     this.openMode = null;
                     this.layout(canvas, this.category);
                     return true;
                  }
               }
            }

            if (canvas.hit(115.0F, row.y + 26.0F, 779.0F, 30.0F)) {
               this.openMode = this.openMode == dropdown ? null : dropdown;
               this.layout(canvas, this.category);
               return true;
            }

            if (this.openMode == dropdown && canvas.hit(115.0F, modeListY(row), 779.0F, modeListHeight(dropdown.getModes().size()))) {
               return true;
            }
         }

         if (setting instanceof BooleanSetting bool && canvas.hit(115.0F, row.y, 779.0F, 26.0F)) {
            bool.setValue(!bool.getValue());
            this.layout(canvas, this.category);
            return true;
         } else {
            if (setting instanceof MultiSelectSetting multi) {
               for (ClickGuiModulesPage.OptionHit option : row.options) {
                  if (canvas.hit(option.x, option.y, option.width, option.height)) {
                     multi.toggle(option.option);
                     this.layout(canvas, this.category);
                     return true;
                  }
               }
            }

            if (setting instanceof ColorSetting color && canvas.hit(115.0F, row.y + 26.0F, 779.0F, 30.0F)) {
               this.activeColor = color;
               this.updateColor(canvas, color);
               return true;
            } else if (setting instanceof TextSetting text && canvas.hit(115.0F, row.y + 26.0F, 779.0F, 30.0F)) {
               this.focusedText = text;
               return true;
            } else {
               if (setting instanceof InputBindSetting input) {
                  String value = input.getDisplayValue().toUpperCase(Locale.ROOT);
                  float width = 19.0F + canvas.textWidth(value, 12.0F, UiFontStyle.MEDIUM);
                  if (canvas.hit(row.x, row.y + 26.0F - 2.0F, Math.min(row.width, Math.max(width, 72.0F)), 22.0F)) {
                     this.capturedInput = input;
                     this.capturedFeature = null;
                     return true;
                  }
               }

               if (setting instanceof ButtonSetting button && canvas.hit(115.0F, row.y + 26.0F, 779.0F, 30.0F)) {
                  button.press();
                  this.layout(canvas, this.category);
                  return true;
               } else {
                  return false;
               }
            }
         }
      }
   }

   private void updateNumber(ClickGuiCanvas canvas, NumberSetting setting) {
      float progress = clamp((canvas.mouseDesignX() - 115.0F) / 779.0F, 0.0F, 1.0F);
      setting.setValue(setting.getMin() + (setting.getMax() - setting.getMin()) * progress);
   }

   private void updateColor(ClickGuiCanvas canvas, ColorSetting setting) {
      float[] hsv = ColorUtil.hsv(setting.getValue());
      float hue;
      if (canvas.mouseDesignX() >= 613.0F) {
         hue = clamp((canvas.mouseDesignX() - 613.0F) / 271.0F, 0.0F, 1.0F);
      } else {
         hue = (hsv[0] + 0.08F) % 1.0F;
      }

      float saturation = Math.max(0.65F, hsv[1]);
      float brightness = Math.max(0.72F, hsv[2]);
      setting.setValue(ColorUtil.fromHsv(hue, saturation, brightness, 255));
   }

   private void applyMouseBind(int button) {
      if (this.capturedFeature != null) {
         this.capturedFeature.setMouseBind(button);
      } else if (this.capturedInput != null) {
         this.capturedInput.setMouse(button);
      }

      this.clearCapture();
   }

   private void clearCapture() {
      this.capturedFeature = null;
      this.capturedInput = null;
   }

   private void clearTransientState() {
      this.activeNumber = null;
      this.activeColor = null;
      this.focusedText = null;
      this.openMode = null;
      this.clearCapture();
   }

   private ClickGuiModulesPage.RowLayout findRow(Setting<?> setting) {
      for (ClickGuiModulesPage.ModuleLayout module : this.modules) {
         for (ClickGuiModulesPage.RowLayout row : module.rows) {
            if (row.setting == setting) {
               return row;
            }
         }
      }

      return null;
   }

   private static float chipWidth(ClickGuiCanvas canvas, String option, boolean selected) {
      float text = (float)Math.ceil(canvas.textWidth(MenuText.option(option), 12.0F, UiFontStyle.REGULAR));
      return Math.min(779.0F, text + chipPadding(selected));
   }

   private static float chipPadding(boolean selected) {
      return selected ? 37.0F : 24.0F;
   }

   private static float chipTextX(boolean selected) {
      return selected ? 26.0F : 12.0F;
   }

   private void buildOptionHits(ClickGuiCanvas canvas, ClickGuiModulesPage.RowLayout row, List<String> options, Predicate<String> selectedTest) {
      float x = 115.0F;
      float y = row.y + 26.0F;

      for (String option : options) {
         float width = chipWidth(canvas, option, selectedTest.test(option));
         if (x > 115.0F && x + width > 894.0F) {
            x = 115.0F;
            y += 31.0F;
         }

         row.options.add(new ClickGuiModulesPage.OptionHit(option, x, y, width, 26.0F));
         x += width + 5.0F;
      }
   }

   private float chipRowHeight(ClickGuiCanvas canvas, List<String> options, Predicate<String> selectedTest) {
      float x = 115.0F;
      int lines = 1;

      for (String option : options) {
         float width = chipWidth(canvas, option, selectedTest.test(option));
         if (x > 115.0F && x + width > 894.0F) {
            x = 115.0F;
            lines++;
         }

         x += width + 5.0F;
      }

      return 26.0F + lines * 26.0F + Math.max(0, lines - 1) * 5.0F + 6.0F;
   }

   private float rowHeight(ClickGuiCanvas canvas, Setting<?> setting) {
      if (setting instanceof NumberSetting) {
         return 48.0F;
      }

      if (setting instanceof ModeSetting mode) {
         if (mode.isChips()) {
            return this.chipRowHeight(canvas, mode.getModes(), mode::is);
         }

         float height = 64.0F;
         return this.openMode == mode ? height + 6.0F + modeListHeight(mode.getModes().size()) : height;
      } else if (setting instanceof BooleanSetting) {
         return 26.0F;
      } else if (setting instanceof MultiSelectSetting multi) {
         return this.chipRowHeight(canvas, multi.getOptions(), multi::isSelected);
      } else if (setting instanceof ColorSetting || setting instanceof TextSetting) {
         return 64.0F;
      } else if (setting instanceof InputBindSetting) {
         return 46.0F;
      } else {
         return setting instanceof ButtonSetting ? 64.0F : 26.0F;
      }
   }

   private static List<Setting<?>> visibleSettings(Feature feature) {
      List<Setting<?>> result = new ArrayList<>();

      for (Setting<?> setting : feature.getSettings()) {
         if (setting.isVisible()) {
            result.add(setting);
         }
      }

      return result;
   }

   private static class_2960 featureIcon(FeatureCategory category) {
      return switch (category) {
         case COMBAT -> Textures.Icons.SWORDS;
         case MOVEMENT -> Textures.Icons.PERSON_STANDING;
         case VISUAL -> Textures.Icons.EYE;
         case PLAYER -> Textures.Icons.USER_ROUND;
         case MISC -> Textures.Icons.BOXES;
         case PVE -> Textures.Icons.BRAIN;
      };
   }

   private static void renderToggle(ClickGuiCanvas canvas, float x, float y, boolean enabled, boolean interactive) {
      int track = !interactive ? canvas.alpha(ClickGuiPalette.OFF_TRACK, 0.55F) : (enabled ? ClickGuiPalette.accent() : ClickGuiPalette.OFF_TRACK);
      int knob = !interactive ? canvas.alpha(ClickGuiPalette.OFF_KNOB, 0.55F) : (enabled ? -1 : ClickGuiPalette.OFF_KNOB);
      canvas.rect(x, y, 33.0F, 20.0F, track, 10.0F);
      float knobX = x + (enabled ? 16.0F : 3.0F);
      canvas.rect(knobX, y + 2.5F, 15.0F, 15.0F, knob, 7.5F);
      if (enabled) {
         canvas.texture(
            knobX + 5.0F, y + 5.5F, 6.0F, 4.493F, Textures.Icons.CHECK, interactive ? ClickGuiPalette.accent() : canvas.alpha(ClickGuiPalette.accent(), 0.55F)
         );
      } else {
         canvas.texture(
            knobX + 4.5F, y + 4.5F, 6.0F, 6.0F, Textures.Icons.X, interactive ? ClickGuiPalette.OFF_TRACK : canvas.alpha(ClickGuiPalette.OFF_TRACK, 0.55F)
         );
      }
   }

   private static String fit(ClickGuiCanvas canvas, String raw, float maxWidth, float size, UiFontStyle style) {
      String value = raw == null ? "" : raw;
      if (canvas.textWidth(value, size, style) <= maxWidth) {
         return value;
      }

      String suffix = "...";
      int end = value.length();

      while (end > 0 && canvas.textWidth(value.substring(0, end) + suffix, size, style) > maxWidth) {
         end--;
      }

      return value.substring(0, end) + suffix;
   }

   private static boolean visible(float y, float height) {
      return y + height >= 65.0F && y <= 627.0F;
   }

   private static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   @Environment(EnvType.CLIENT)
   private static final class ModuleLayout {
      private final Feature feature;
      private final float y;
      private final List<ClickGuiModulesPage.RowLayout> rows = new ArrayList<>();
      private float height;

      private ModuleLayout(Feature feature, float y) {
         this.feature = feature;
         this.y = y;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class OptionHit {
      private final String option;
      private final float x;
      private final float y;
      private final float width;
      private final float height;

      private OptionHit(String option, float x, float y, float width, float height) {
         this.option = option;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class RowLayout {
      private final Feature feature;
      private final Setting<?> setting;
      private final float x;
      private final float width;
      private final float y;
      private final float height;
      private final List<ClickGuiModulesPage.OptionHit> options = new ArrayList<>();

      private RowLayout(Feature feature, Setting<?> setting, float x, float width, float y, float height) {
         this.feature = feature;
         this.setting = setting;
         this.x = x;
         this.width = width;
         this.y = y;
         this.height = height;
      }
   }
}
