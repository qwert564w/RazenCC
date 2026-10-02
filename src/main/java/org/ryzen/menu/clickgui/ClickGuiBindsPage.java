package org.ryzen.menu.clickgui;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_642;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BindSetting;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.feature.setting.Setting;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class ClickGuiBindsPage {
   private static final float PANEL_X = 94.0F;
   private static final float PANEL_Y = 67.0F;
   private static final float PANEL_WIDTH = 821.0F;
   private static final float PANEL_HEIGHT = 496.0F;
   private static final float LEFT_X = 115.0F;
   private static final float RIGHT_X = 515.0F;
   private static final float ROW_Y = 151.0F;
   private static final float ROW_WIDTH = 381.0F;
   private static final float ROW_HEIGHT = 66.0F;
   private static final float ROW_STEP = 80.0F;
   private static final float ROW_VIEW_HEIGHT = 386.0F;
   private static final float KEY_WIDTH = 66.0F;
   private static final float KEY_HEIGHT = 20.0F;
   private List<ClickGuiBindsPage.ItemBindEntry> itemBinds = List.of();
   private List<ClickGuiBindsPage.FunctionBindEntry> functionBinds = List.of();
   private ClickGuiBindsPage.Capture capture;
   private float scrollOffset;
   private float maxScroll;

   public void layout(ClickGuiCanvas canvas) {
      List<ClickGuiBindsPage.ItemBindEntry> nextItems = new ArrayList<>();
      List<ClickGuiBindsPage.FunctionBindEntry> nextFunctions = new ArrayList<>();

      for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
         for (Setting<?> setting : feature.getSettings()) {
            if (setting instanceof InputBindSetting bind && bind.isBound()) {
               nextItems.add(new ClickGuiBindsPage.ItemBindEntry(feature, bind));
            }
         }

         List<Integer> binds = feature.getBinds();

         for (int index = 0; index < binds.size(); index++) {
            if (binds.get(index) != -1) {
               nextFunctions.add(new ClickGuiBindsPage.FunctionBindEntry(feature, index, binds.get(index)));
            }
         }
      }

      this.itemBinds = List.copyOf(nextItems);
      this.functionBinds = List.copyOf(nextFunctions);
      int rows = Math.max(this.itemBinds.size(), this.functionBinds.size());
      float contentHeight = rows == 0 ? 0.0F : (rows - 1) * 80.0F + 66.0F;
      this.maxScroll = Math.max(0.0F, contentHeight - 386.0F);
      this.scrollOffset = clamp(this.scrollOffset, 0.0F, this.maxScroll);
      if (this.capture != null && !this.captureStillExists(this.capture)) {
         this.capture = null;
      }
   }

   public void render(ClickGuiCanvas canvas) {
      canvas.outlinedRect(94.0F, 67.0F, 821.0F, 496.0F, ClickGuiPalette.SURFACE, 17.0F, 0.5F, ClickGuiPalette.STROKE);
      canvas.texture(115.0F, 87.0F, 13.459F, 13.358F, Textures.Icons.KEYBOARD, ClickGuiPalette.accent());
      canvas.text(140.0F, 85.0F, 15.0F, MenuText.ui("Binds items"), -1, UiFontStyle.MEDIUM);
      canvas.texture(115.0F, 122.0F, 10.0F, 10.0F, Textures.Icons.GLOBE, ClickGuiPalette.TEXT_STRONG);
      canvas.text(133.0F, 118.0F, 15.0F, currentServer(), ClickGuiPalette.TEXT_STRONG, UiFontStyle.REGULAR);
      canvas.texture(515.0F, 97.0F, 13.459F, 13.358F, Textures.Icons.KEYBOARD, ClickGuiPalette.accent());
      canvas.text(540.0F, 95.0F, 15.0F, MenuText.ui("Binds Functions"), -1, UiFontStyle.MEDIUM);
      canvas.texture(515.0F, 122.0F, 10.0F, 9.581F, Textures.Icons.SWORDS, ClickGuiPalette.TEXT_STRONG);
      canvas.text(533.0F, 120.0F, 13.0F, MenuText.ui("Combat"), ClickGuiPalette.TEXT_STRONG, UiFontStyle.REGULAR);
      canvas.pushScissor(94.0F, 143.0F, 821.0F, 411.0F);
      if (this.itemBinds.isEmpty()) {
         canvas.text(115.0F, 157.0F, 13.0F, MenuText.ui("No item binds yet"), ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM);
         canvas.text(115.0F, 177.0F, 11.0F, MenuText.ui("Add one from a feature's Bind setting."), ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR);
      }

      for (int index = 0; index < this.itemBinds.size(); index++) {
         float y = 151.0F + index * 80.0F - this.scrollOffset;
         if (rowVisible(y)) {
            this.renderItemRow(canvas, this.itemBinds.get(index), y);
         }
      }

      for (int index = 0; index < this.functionBinds.size(); index++) {
         float y = 151.0F + index * 80.0F - this.scrollOffset;
         if (rowVisible(y)) {
            this.renderFunctionRow(canvas, this.functionBinds.get(index), y);
         }
      }

      canvas.popScissor();
   }

   public boolean mousePressed(ClickGuiCanvas canvas, int button) {
      if (this.capture != null) {
         this.applyMouseBind(button);
         return true;
      }

      if (button != 0) {
         return canvas.hit(94.0F, 67.0F, 821.0F, 496.0F);
      }

      for (int index = 0; index < this.itemBinds.size(); index++) {
         float y = 151.0F + index * 80.0F - this.scrollOffset;
         if (rowVisible(y)) {
            ClickGuiBindsPage.ItemBindEntry entry = this.itemBinds.get(index);
            if (canvas.hit(329.0F, y + 23.0F, 66.0F, 20.0F)) {
               this.capture = ClickGuiBindsPage.Capture.item(entry);
               return true;
            }

            if (canvas.hit(447.0F, y + 16.0F, 34.0F, 34.0F)) {
               entry.setting().clear();
               this.capture = null;
               this.layout(canvas);
               return true;
            }
         }
      }

      for (int index = 0; index < this.functionBinds.size(); index++) {
         float y = 151.0F + index * 80.0F - this.scrollOffset;
         if (rowVisible(y)) {
            ClickGuiBindsPage.FunctionBindEntry entry = this.functionBinds.get(index);
            if (canvas.hit(751.0F, y + 23.0F, 66.0F, 20.0F)) {
               this.capture = ClickGuiBindsPage.Capture.function(entry);
               return true;
            }

            if (canvas.hit(835.0F, y + 16.0F, 43.0F, 34.0F)) {
               boolean visible = entry.feature().isBindVisibleAt(entry.index());
               entry.feature().setBindVisibleAt(entry.index(), !visible);
               this.layout(canvas);
               return true;
            }
         }
      }

      if (this.capture != null) {
         this.capture = null;
         return true;
      } else {
         return canvas.hit(94.0F, 67.0F, 821.0F, 496.0F);
      }
   }

   public String headerTitle() {
      return MenuText.ui("Binds");
   }

   public String headerDescription() {
      return MenuText.ui("Item and function keybinds.");
   }

   public void scroll(double amount) {
      this.scrollOffset = clamp(this.scrollOffset - (float)amount * 32.0F, 0.0F, this.maxScroll);
   }

   public boolean keyPressed(int key) {
      if (this.capture == null) {
         return false;
      } else if (key == 256) {
         this.capture = null;
         return true;
      } else if (key != 259 && key != 261) {
         this.applyKeyBind(key);
         return true;
      } else {
         this.clearCapturedBind();
         this.capture = null;
         return true;
      }
   }

   public boolean isCapturingBind() {
      return this.capture != null;
   }

   private void renderItemRow(ClickGuiCanvas canvas, ClickGuiBindsPage.ItemBindEntry entry, float y) {
      boolean bound = entry.setting().isBound();
      boolean hovered = canvas.hit(115.0F, y, 381.0F, 66.0F);
      int rowColor = bound ? ClickGuiPalette.accent() : (hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.SURFACE);
      int rowStroke = bound ? 0 : ClickGuiPalette.CARD_STROKE;
      canvas.outlinedRect(115.0F, y, 381.0F, 66.0F, rowColor, 15.0F, 0.5F, rowStroke);
      int circleColor = bound ? -1 : ClickGuiPalette.CONTROL;
      int iconColor = bound ? -1 : ClickGuiPalette.TEXT_MUTED;
      canvas.rect(130.0F, y + 13.0F, 39.0F, 39.0F, circleColor, 19.5F);
      canvas.texture(140.0F, y + 23.0F, 18.0F, 19.0F, Textures.ClickGui.ENDER_PEARL, iconColor);
      int mainText = bound ? -1 : ClickGuiPalette.TEXT_MUTED;
      int subText = mainText;
      String name = itemName(entry);
      canvas.text(179.0F, y + 14.0F, 15.0F, fit(canvas, name, 139.0F, 15.0F, UiFontStyle.MEDIUM), mainText, UiFontStyle.MEDIUM);
      canvas.text(179.0F, y + 37.0F, 12.0F, itemBindLabel(entry), subText, UiFontStyle.REGULAR);
      boolean listening = this.isCaptured(entry);
      int keyFill = bound ? -1 : ClickGuiPalette.OFF_TRACK;
      int keyText = bound ? ClickGuiPalette.accent() : ClickGuiPalette.TEXT_FAINT;
      if (listening) {
         keyFill = ClickGuiPalette.CONTROL_ACTIVE;
         keyText = -1;
      }

      canvas.rect(329.0F, y + 23.0F, 66.0F, 20.0F, keyFill, 7.0F);
      this.renderKeyLabel(canvas, 329.0F, y + 23.0F, listening ? "..." : entry.setting().getDisplayValue(), keyText);
      int actionColor = bound ? -1 : ClickGuiPalette.TEXT_MUTED;
      canvas.texture(419.0F, y + 26.0F, 13.0F, 13.0F, bound ? Textures.Icons.CHECK : Textures.Icons.POINTER_CLICK, actionColor);
      canvas.texture(456.0F, y + 27.0F, 15.15F, 12.0F, Textures.Icons.DELETE_LEFT, actionColor);
   }

   private void renderFunctionRow(ClickGuiCanvas canvas, ClickGuiBindsPage.FunctionBindEntry entry, float y) {
      boolean visible = entry.feature().isBindVisibleAt(entry.index());
      boolean hovered = canvas.hit(515.0F, y, 381.0F, 66.0F);
      int rowColor = hovered ? ClickGuiPalette.CONTROL_HOVER : (visible ? ClickGuiPalette.SURFACE : canvas.alpha(ClickGuiPalette.SURFACE, 0.58F));
      canvas.outlinedRect(515.0F, y, 381.0F, 66.0F, rowColor, 15.0F, 0.5F, ClickGuiPalette.CARD_STROKE);
      canvas.rect(530.0F, y + 13.0F, 39.0F, 39.0F, ClickGuiPalette.CONTROL, 19.5F);
      canvas.texture(544.0F, y + 27.0F, 11.0F, 12.0F, iconFor(entry.feature().getCategory()), visible ? ClickGuiPalette.accent() : ClickGuiPalette.TEXT_MUTED);
      canvas.text(
         581.0F,
         y + 24.0F,
         15.0F,
         fit(canvas, entry.feature().getName(), 158.0F, 15.0F, UiFontStyle.MEDIUM),
         visible ? -1 : ClickGuiPalette.TEXT_MUTED,
         UiFontStyle.MEDIUM
      );
      boolean listening = this.isCaptured(entry);
      int keyFill = visible ? ClickGuiPalette.accent() : ClickGuiPalette.CONTROL;
      int keyText = visible ? -1 : ClickGuiPalette.TEXT_MUTED;
      canvas.outlinedRect(
         751.0F, y + 23.0F, 66.0F, 20.0F, listening ? ClickGuiPalette.CONTROL_ACTIVE : keyFill, 7.0F, 0.5F, listening ? -1 : canvas.alpha(-1, 0.0F)
      );
      this.renderKeyLabel(canvas, 751.0F, y + 23.0F, listening ? "..." : BindSetting.describe(entry.bindCode()), listening ? -1 : keyText);
      this.renderToggle(canvas, 840.0F, y + 23.0F, visible);
   }

   private static String itemBindLabel(ClickGuiBindsPage.ItemBindEntry entry) {
      String settingName = entry.setting().getName();
      return "Key".equalsIgnoreCase(settingName) ? MenuText.setting(entry.feature().getName(), settingName) : entry.feature().getName();
   }

   private static String currentServer() {
      class_310 minecraft = class_310.method_1551();
      if (minecraft == null) {
         return "—";
      } else {
         class_642 server = minecraft.method_1558();
         if (server != null && server.field_3761 != null && !server.field_3761.isBlank()) {
            return server.field_3761;
         } else {
            return minecraft.method_47392() ? MenuText.ui("Singleplayer") : MenuText.ui("Not connected");
         }
      }
   }

   private void renderKeyLabel(ClickGuiCanvas canvas, float x, float y, String raw, int color) {
      canvas.texture(x + 7.0F, y + 5.0F, 13.0F, 9.0F, Textures.Icons.KEYBOARD, color);
      String label = MenuText.bind(raw == null ? "None" : raw).toUpperCase();
      label = fit(canvas, label, 37.0F, 12.0F, UiFontStyle.MEDIUM);
      canvas.text(x + 25.0F, y + 3.0F, 12.0F, label, color, UiFontStyle.MEDIUM);
   }

   private void renderToggle(ClickGuiCanvas canvas, float x, float y, boolean enabled) {
      canvas.rect(x, y, 33.0F, 20.0F, enabled ? ClickGuiPalette.accent() : ClickGuiPalette.OFF_TRACK, 10.0F);
      float knobX = enabled ? x + 16.0F : x + 3.0F;
      canvas.rect(knobX, y + 2.5F, 15.0F, 15.0F, enabled ? -1 : ClickGuiPalette.OFF_KNOB, 7.5F);
      if (enabled) {
         canvas.texture(knobX + 5.0F, y + 5.5F, 6.0F, 4.493F, Textures.Icons.CHECK, ClickGuiPalette.accent());
      } else {
         canvas.texture(knobX + 4.5F, y + 4.5F, 6.0F, 6.0F, Textures.Icons.X, ClickGuiPalette.OFF_TRACK);
      }
   }

   private void applyKeyBind(int key) {
      ClickGuiBindsPage.Capture current = this.capture;
      if (current != null) {
         if (current.item() != null) {
            current.item().setting().setKey(key);
         } else if (current.function() != null) {
            ClickGuiBindsPage.FunctionBindEntry entry = current.function();
            entry.feature().setKeyBindAt(entry.index(), key);
         }

         this.capture = null;
      }
   }

   private void applyMouseBind(int button) {
      ClickGuiBindsPage.Capture current = this.capture;
      if (current != null) {
         if (current.item() != null) {
            current.item().setting().setMouse(button);
         } else if (current.function() != null) {
            ClickGuiBindsPage.FunctionBindEntry entry = current.function();
            entry.feature().setMouseBindAt(entry.index(), button);
         }

         this.capture = null;
      }
   }

   private void clearCapturedBind() {
      if (this.capture.item() != null) {
         this.capture.item().setting().clear();
      } else if (this.capture.function() != null) {
         ClickGuiBindsPage.FunctionBindEntry entry = this.capture.function();
         entry.feature().removeBindAt(entry.index());
      }
   }

   private boolean captureStillExists(ClickGuiBindsPage.Capture current) {
      if (current.item() != null) {
         return this.itemBinds.stream().anyMatch(entry -> entry.setting() == current.item().setting());
      }

      if (current.function() == null) {
         return false;
      }

      ClickGuiBindsPage.FunctionBindEntry wanted = current.function();
      return wanted.feature().hasBindAt(wanted.index());
   }

   private boolean isCaptured(ClickGuiBindsPage.ItemBindEntry entry) {
      return this.capture != null && this.capture.item() != null && this.capture.item().setting() == entry.setting();
   }

   private boolean isCaptured(ClickGuiBindsPage.FunctionBindEntry entry) {
      return this.capture != null
         && this.capture.function() != null
         && this.capture.function().feature() == entry.feature()
         && this.capture.function().index() == entry.index();
   }

   private static String itemName(ClickGuiBindsPage.ItemBindEntry entry) {
      String settingName = entry.setting().getName();
      return "Key".equalsIgnoreCase(settingName) ? entry.feature().getName() : settingName;
   }

   private static class_2960 iconFor(FeatureCategory category) {
      return switch (category) {
         case COMBAT -> Textures.Icons.SWORDS;
         case MOVEMENT -> Textures.Icons.PERSON_STANDING;
         case VISUAL -> Textures.Icons.EYE;
         case PLAYER -> Textures.Icons.USER_ROUND;
         case MISC -> Textures.Icons.BOXES;
         case PVE -> Textures.Icons.BRAIN;
      };
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

   private static boolean rowVisible(float y) {
      return y + 66.0F >= 143.0F && y <= 554.0F;
   }

   private static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   @Environment(EnvType.CLIENT)
   private record Capture(ClickGuiBindsPage.ItemBindEntry item, ClickGuiBindsPage.FunctionBindEntry function) {
      static ClickGuiBindsPage.Capture item(ClickGuiBindsPage.ItemBindEntry entry) {
         return new ClickGuiBindsPage.Capture(entry, null);
      }

      static ClickGuiBindsPage.Capture function(ClickGuiBindsPage.FunctionBindEntry entry) {
         return new ClickGuiBindsPage.Capture(null, entry);
      }
   }

   @Environment(EnvType.CLIENT)
   private record FunctionBindEntry(Feature feature, int index, int bindCode) {
   }

   @Environment(EnvType.CLIENT)
   private record ItemBindEntry(Feature feature, InputBindSetting setting) {
   }
}
