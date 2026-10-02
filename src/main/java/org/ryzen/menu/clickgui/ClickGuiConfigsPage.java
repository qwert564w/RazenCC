package org.ryzen.menu.clickgui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_156;
import org.ryzen.feature.FeatureManager;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.ConfigIO;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class ClickGuiConfigsPage {
   private static final float PANEL_X = 94.0F;
   private static final float PANEL_Y = 67.0F;
   private static final float PANEL_WIDTH = 821.0F;
   private static final float PANEL_HEIGHT = 546.0F;
   private static final float ROW_X = 115.0F;
   private static final float ROW_Y = 126.0F;
   private static final float ROW_WIDTH = 778.0F;
   private static final float ROW_HEIGHT = 66.0F;
   private static final float ROW_STEP = 80.0F;
   private static final float ROW_VIEW_HEIGHT = 466.0F;
   private static final float ACTION_HIT_Y = 16.0F;
   private static final float ACTION_HIT_SIZE = 34.0F;
   private static final float FOLDER_HIT_X = 658.0F;
   private static final float LOAD_HIT_X = 694.0F;
   private static final float DELETE_HIT_X = 730.0F;
   private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy");
   private List<ClickGuiConfigsPage.ConfigEntry> entries = List.of();
   private String activeName;
   private float scrollOffset;
   private float maxScroll;

   public void layout(ClickGuiCanvas canvas) {
      List<ClickGuiConfigsPage.ConfigEntry> next = new ArrayList<>();

      for (String name : FeatureManager.INSTANCE.configNames()) {
         next.add(new ClickGuiConfigsPage.ConfigEntry(name, configDate(name)));
      }

      if (this.activeName != null && next.stream().noneMatch(e -> e.name().equals(this.activeName))) {
         this.activeName = null;
      }

      this.entries = List.copyOf(next);
      float contentHeight = this.entries.isEmpty() ? 0.0F : (this.entries.size() - 1) * 80.0F + 66.0F;
      this.maxScroll = Math.max(0.0F, contentHeight - 466.0F);
      this.scrollOffset = clamp(this.scrollOffset, 0.0F, this.maxScroll);
   }

   public void render(ClickGuiCanvas canvas) {
      canvas.outlinedRect(94.0F, 67.0F, 821.0F, 546.0F, ClickGuiPalette.SURFACE, 17.0F, 0.5F, ClickGuiPalette.STROKE);
      canvas.texture(115.0F, 87.0F, 15.0F, 11.382F, Textures.Icons.FOLDER, ClickGuiPalette.accent());
      canvas.text(140.0F, 85.0F, 15.0F, MenuText.ui("Configs"), -1, UiFontStyle.MEDIUM);
      if (this.entries.isEmpty()) {
         canvas.text(504.5F, 300.0F, 14.0F, MenuText.ui("No saved configs"), ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM, TextAlign.CENTER);
         canvas.text(504.5F, 322.0F, 12.0F, MenuText.ui("Save one with .config save <name>"), ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR, TextAlign.CENTER);
      } else {
         canvas.pushScissor(94.0F, 118.0F, 821.0F, 486.0F);

         for (int index = 0; index < this.entries.size(); index++) {
            float y = 126.0F + index * 80.0F - this.scrollOffset;
            if (rowVisible(y)) {
               this.renderRow(canvas, this.entries.get(index), index, y);
            }
         }

         canvas.popScissor();
      }
   }

   public boolean mousePressed(ClickGuiCanvas canvas, int button) {
      if (button != 0) {
         return canvas.hit(94.0F, 67.0F, 821.0F, 546.0F);
      }

      for (int index = 0; index < this.entries.size(); index++) {
         float y = 126.0F + index * 80.0F - this.scrollOffset;
         if (rowVisible(y)) {
            ClickGuiConfigsPage.ConfigEntry entry = this.entries.get(index);
            if (canvas.hit(115.0F, y, 778.0F, 66.0F)) {
               if (canvas.hit(773.0F, y + 16.0F, 34.0F, 34.0F)) {
                  this.openConfigFolder();
                  return true;
               }

               if (canvas.hit(809.0F, y + 16.0F, 34.0F, 34.0F)) {
                  this.load(entry);
                  return true;
               }

               if (canvas.hit(845.0F, y + 16.0F, 34.0F, 34.0F)) {
                  this.delete(entry, canvas);
                  return true;
               }

               this.load(entry);
               return true;
            }
         }
      }

      return canvas.hit(94.0F, 67.0F, 821.0F, 546.0F);
   }

   private void openConfigFolder() {
      try {
         Path folder = ConfigIO.resolve("configs");
         Files.createDirectories(folder);
         class_156.method_668().method_60932(folder);
      } catch (Exception var2) {
      }
   }

   private void load(ClickGuiConfigsPage.ConfigEntry entry) {
      if (FeatureManager.INSTANCE.loadConfig(entry.name())) {
         this.activeName = entry.name();
      }
   }

   private void delete(ClickGuiConfigsPage.ConfigEntry entry, ClickGuiCanvas canvas) {
      if (FeatureManager.INSTANCE.deleteConfig(entry.name())) {
         if (entry.name().equals(this.activeName)) {
            this.activeName = null;
         }

         this.layout(canvas);
      }
   }

   public void scroll(double amount) {
      this.scrollOffset = clamp(this.scrollOffset - (float)amount * 32.0F, 0.0F, this.maxScroll);
   }

   public String headerTitle() {
      return MenuText.ui("Configs");
   }

   public String headerDescription() {
      return MenuText.ui("Saved client configurations.");
   }

   private void renderRow(ClickGuiCanvas canvas, ClickGuiConfigsPage.ConfigEntry entry, int index, float y) {
      boolean active = entry.name().equals(this.activeName);
      boolean hovered = canvas.hit(115.0F, y, 778.0F, 66.0F);
      int fill = active ? ClickGuiPalette.accent() : (hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.SURFACE);
      int stroke = active ? 0 : ClickGuiPalette.CARD_STROKE;
      canvas.outlinedRect(115.0F, y, 778.0F, 66.0F, fill, 15.0F, 0.5F, stroke);
      int label = active ? -1 : ClickGuiPalette.TEXT_MUTED;
      int circle = active ? -1 : ClickGuiPalette.CONTROL;
      int icon = active ? ClickGuiPalette.accent() : label;
      canvas.rect(130.0F, y + 13.0F, 39.0F, 39.0F, circle, 19.5F);
      canvas.texture(142.0F, y + 27.0F, 15.0F, 11.382F, Textures.Icons.FOLDER, icon);
      canvas.text(179.0F, y + 14.0F, 15.0F, fit(canvas, entry.name(), 580.0F, 15.0F, UiFontStyle.MEDIUM), label, UiFontStyle.MEDIUM);
      canvas.text(179.0F, y + 37.0F, 12.0F, MenuText.ui("Created") + " " + entry.date(), label, UiFontStyle.REGULAR);
      canvas.texture(782.0F, y + 27.0F, 15.0F, 11.382F, Textures.Icons.FOLDER, label);
      canvas.texture(818.0F, y + 25.0F, 13.0F, 15.864F, Textures.Icons.UPLOAD, label);
      canvas.texture(852.0F, y + 27.0F, 15.15F, 12.0F, Textures.Icons.DELETE_LEFT, label);
   }

   private static String configDate(String name) {
      try {
         Path path = ConfigIO.resolve("configs").resolve(name + ".ryz");
         Instant instant = Files.getLastModifiedTime(path).toInstant();
         return DATE_FORMAT.format(instant.atZone(ZoneId.systemDefault()));
      } catch (Exception ignored) {
         return "24.06.26";
      }
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

   private static boolean rowVisible(float y) {
      return y + 66.0F >= 118.0F && y <= 604.0F;
   }

   private static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   @Environment(EnvType.CLIENT)
   private record ConfigEntry(String name, String date) {
   }
}
