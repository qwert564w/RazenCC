package org.ryzen.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1934;
import net.minecraft.class_2561;
import net.minecraft.class_268;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_640;
import org.ryzen.feature.impl.pve.ModeratorDetector;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.StaffManager;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class StaffListElement extends HudElement {
   private static final float DESIGN_WIDTH = 216.0F;
   private static final float ROW_HEIGHT = 31.0F;
   private static final float TEXT_SIZE = 12.0F;
   private static final float STATUS_TEXT_SIZE = 12.0F;
   private static final int MAX_ROWS = 4;
   private static final long VANISH_RETENTION_MS = 600000L;
   private static final int DANGER = ColorUtil.rgb(255, 50, 32);
   private static final int PLAYING = ColorUtil.rgb(84, 255, 32);
   private static final int VANISH_COLOR = ColorUtil.rgb(255, 151, 32);
   private static final class_2960 TITLE_ICON = figma("stafflist_title_0_859.svg");
   private static final class_2960 CLOSE_ICON = figma("stafflist_close_0_857.svg");
   private static final class_2960 SPEC_ICON = figma("stafflist_spec_0_865.svg");
   private static final class_2960 PLAYING_ICON = figma("stafflist_playing_0_872.svg");
   private static final class_2960 VANISH_ICON = figma("stafflist_vanish_row3_0_879.svg");
   private final Map<UUID, StaffListElement.TrackedStaff> tracked = new LinkedHashMap<>();
   private final List<StaffListElement.Row> rows = new ArrayList<>(4);
   private Object connectionIdentity;

   public StaffListElement() {
      super("staff_list", "Stafflist");
   }

   @Override
   protected float defaultX(float unit) {
      return 316.0F * unit;
   }

   @Override
   protected float defaultY(float unit) {
      return 194.0F * unit;
   }

   @Override
   protected boolean hasHeaderCloseButton() {
      return true;
   }

   @Override
   protected void layout(class_310 mc, float unit) {
      this.collectRows(mc);
      if (this.rows.isEmpty()) {
         this.width = 0.0F;
         this.height = 0.0F;
      } else {
         this.width = 216.0F * unit;
         this.height = (47.0F + this.rows.size() * 31.0F) * unit;
      }
   }

   @Override
   protected void draw(class_310 mc, float unit) {
      if (!this.rows.isEmpty()) {
         float alpha = this.appearAlpha();
         float[] inner = this.drawCard(unit, alpha, 38.0F, null, null, 6.0F, 6.0F);
         this.drawHeader(unit, alpha);
         float nameX = this.x + 18.0F * unit;
         float timeRight = this.x + 197.0F * unit;
         MsdfFont font = UiFonts.sfProDisplay();

         for (int index = 0; index < this.rows.size(); index++) {
            StaffListElement.Row row = this.rows.get(index);
            float rowY = this.y + (50.0F + index * 31.0F) * unit;
            float centerY = rowY + 7.0F * unit;
            float nameSize = 12.0F * unit;
            float statusSize = 12.0F * unit;
            float spacing = statusSize * UiFontStyle.MEDIUM.letterSpacingEm();
            float timeWidth = font.measureWidth(row.elapsed(), statusSize, spacing);
            float timeLeft = timeRight - timeWidth;
            float dividerX = timeLeft - 8.0F * unit;
            float statusWidth = font.measureWidth(row.status().label, statusSize, spacing);
            float statusX = dividerX - 8.0F * unit - statusWidth;
            float iconWidth = statusIconWidth(row.status(), unit);
            float iconX = statusX - 4.0F * unit - iconWidth;
            String name = fit(font, row.name(), Math.max(20.0F * unit, iconX - nameX - 6.0F * unit), nameSize);
            Render2DUtil.text(nameX, font.centeredTextY(centerY, nameSize), nameSize, name)
               .style(UiFontStyle.MEDIUM)
               .color(ColorUtil.multiplyAlpha(Theme.Colors.TEXT_TEXT, alpha))
               .draw();
            drawStatusIcon(row.status(), iconX, centerY, 10.0F * unit, alpha);
            Render2DUtil.text(statusX, font.centeredTextY(centerY, statusSize), statusSize, row.status().label)
               .style(UiFontStyle.MEDIUM)
               .color(ColorUtil.multiplyAlpha(row.status().color(), alpha))
               .draw();
            Render2DUtil.text(timeRight, font.centeredTextY(centerY, statusSize), statusSize, row.elapsed())
               .style(UiFontStyle.MEDIUM)
               .color(ColorUtil.multiplyAlpha(Theme.Colors.TEXT_TEXT, alpha))
               .align(TextAlign.RIGHT)
               .draw();
            Render2DUtil.rect(dividerX, rowY + 2.0F * unit, 1.0F * unit, 9.0F * unit)
               .color(ColorUtil.multiplyAlpha(DIVIDER_COLOR, alpha))
               .radius(2.0F * unit)
               .draw();
            if (index < this.rows.size() - 1) {
               Render2DUtil.rect(this.x + 18.0F * unit, rowY + 22.0F * unit, 179.0F * unit, unit).color(ColorUtil.multiplyAlpha(DIVIDER_COLOR, alpha)).draw();
            }
         }
      }
   }

   private void drawHeader(float unit, float alpha) {
      float iconWidth = 11.0F * unit;
      float iconHeight = 12.0F * unit;
      float centerY = this.y + 38.0F * unit / 2.0F;
      float iconX = this.x + 18.0F * unit;
      Render2DUtil.texture(iconX, centerY - iconHeight / 2.0F, iconWidth, iconHeight, TITLE_ICON)
         .color(ColorUtil.multiplyAlpha(HudPalette.accent(), alpha))
         .draw();
      float titleSize = 12.0F * unit;
      Render2DUtil.text(iconX + iconWidth + 9.0F * unit, UiFonts.sfProDisplay().centeredTextY(centerY, titleSize), titleSize, "Stafflist")
         .style(UiFontStyle.MEDIUM)
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .draw();
      float closeSize = 10.0F * unit;
      Render2DUtil.texture(this.x + this.width - 29.0F * unit, centerY - closeSize / 2.0F, closeSize, closeSize, CLOSE_ICON)
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .draw();
   }

   private static void drawStatusIcon(StaffListElement.Status status, float x, float centerY, float size, float alpha) {
      class_2960 icon = switch (status) {
         case SPEC -> SPEC_ICON;
         case PLAYING -> PLAYING_ICON;
         case VANISH -> VANISH_ICON;
      };
      float width = statusIconWidth(status, size / 10.0F);
      Render2DUtil.texture(x, centerY - size / 2.0F, width, size, icon).color(ColorUtil.multiplyAlpha(-1, alpha)).draw();
   }

   private static float statusIconWidth(StaffListElement.Status status, float unit) {
      return switch (status) {
         case SPEC -> 12.0F * unit;
         case PLAYING -> 10.0F * unit;
         case VANISH -> 11.0F * unit;
      };
   }

   private void collectRows(class_310 mc) {
      this.rows.clear();
      Object connection = mc.method_1562();
      if (connection != this.connectionIdentity) {
         this.connectionIdentity = connection;
         this.tracked.clear();
      }

      if (connection == null) {
         this.addShowcaseRows(mc);
      } else {
         long now = System.currentTimeMillis();
         Map<UUID, class_640> online = new HashMap<>();

         for (class_640 info : mc.method_1562().method_2880()) {
            UUID id = info.method_2966().id();
            if (mc.field_1724 == null || !mc.field_1724.method_5667().equals(id)) {
               online.put(id, info);
               String staffName = info.method_2966().name();
               StaffListElement.TrackedStaff known = this.tracked.get(id);
               if (known != null || ModeratorDetector.containsRole(roleText(info)) || StaffManager.INSTANCE.isStaff(staffName)) {
                  StaffListElement.Status status = info.method_2958() == class_1934.field_9219 ? StaffListElement.Status.SPEC : StaffListElement.Status.PLAYING;
                  if (staffName == null || staffName.isBlank()) {
                     staffName = "Unknown";
                  }

                  if (known == null) {
                     this.tracked.put(id, new StaffListElement.TrackedStaff(staffName, status, now));
                  } else {
                     known.seen(staffName, status, now);
                  }
               }
            }
         }

         Iterator<Entry<UUID, StaffListElement.TrackedStaff>> iterator = this.tracked.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<UUID, StaffListElement.TrackedStaff> entry = iterator.next();
            StaffListElement.TrackedStaff staff = entry.getValue();
            if (!online.containsKey(entry.getKey())) {
               if (now - staff.lastSeen > 600000L) {
                  iterator.remove();
               } else {
                  staff.changeStatus(StaffListElement.Status.VANISH, now);
               }
            }
         }

         this.tracked
            .values()
            .stream()
            .sorted(Comparator.comparing(staffx -> staffx.name, String.CASE_INSENSITIVE_ORDER))
            .limit(4L)
            .map(staffx -> new StaffListElement.Row(staffx.name, staffx.status, elapsed(now - staffx.statusSince)))
            .forEach(this.rows::add);
         this.addShowcaseRows(mc);
      }
   }

   private void addShowcaseRows(class_310 mc) {
      if (this.rows.isEmpty() && showcase(mc)) {
         this.rows.add(new StaffListElement.Row("Zelemxan", StaffListElement.Status.SPEC, "1:31"));
         this.rows.add(new StaffListElement.Row("Sirenhead", StaffListElement.Status.PLAYING, "0:31"));
         this.rows.add(new StaffListElement.Row("piska333", StaffListElement.Status.VANISH, "5:31"));
         this.rows.add(new StaffListElement.Row("ZadiraBob", StaffListElement.Status.VANISH, "2:11"));
      }
   }

   private static String roleText(class_640 info) {
      StringBuilder result = new StringBuilder();
      append(result, info.method_2971());
      class_268 team = info.method_2955();
      if (team != null) {
         append(result, team.method_1144());
         append(result, team.method_1136());
         result.append(' ').append(team.method_1197());
      }

      return result.toString();
   }

   private static void append(StringBuilder target, class_2561 component) {
      if (component != null) {
         target.append(' ').append(component.getString());
      }
   }

   private static String elapsed(long millis) {
      long totalSeconds = Math.max(0L, millis / 1000L);
      long hours = totalSeconds / 3600L;
      long minutes = totalSeconds % 3600L / 60L;
      long seconds = totalSeconds % 60L;
      return hours > 0L ? String.format("%d:%02d:%02d", hours, minutes, seconds) : String.format("%d:%02d", minutes, seconds);
   }

   private static String fit(MsdfFont font, String value, float maxWidth, float size) {
      float spacing = size * UiFontStyle.MEDIUM.letterSpacingEm();
      if (font.measureWidth(value, size, spacing) <= maxWidth) {
         return value;
      }

      for (int length = value.length() - 1; length > 0; length--) {
         String candidate = value.substring(0, length) + "...";
         if (font.measureWidth(candidate, size, spacing) <= maxWidth) {
            return candidate;
         }
      }

      return "...";
   }

   private static class_2960 figma(String file) {
      return class_2960.method_60654("ryzen:textures/hud/figma/" + file);
   }

   @Environment(EnvType.CLIENT)
   private record Row(String name, StaffListElement.Status status, String elapsed) {
   }

   @Environment(EnvType.CLIENT)
   private enum Status {
      SPEC("SPEC", StaffListElement.DANGER),
      PLAYING("PLAYING", StaffListElement.PLAYING),
      VANISH("VANISH", StaffListElement.VANISH_COLOR);

      private final String label;
      private final int color;

      Status(String label, int color) {
         this.label = label;
         this.color = color;
      }

      int color() {
         return this.color;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class TrackedStaff {
      private String name;
      private StaffListElement.Status status;
      private long statusSince;
      private long lastSeen;

      private TrackedStaff(String name, StaffListElement.Status status, long now) {
         this.name = name;
         this.status = status;
         this.statusSince = now;
         this.lastSeen = now;
      }

      private void seen(String currentName, StaffListElement.Status currentStatus, long now) {
         this.name = currentName;
         this.lastSeen = now;
         this.changeStatus(currentStatus, now);
      }

      private void changeStatus(StaffListElement.Status next, long now) {
         if (this.status != next) {
            this.status = next;
            this.statusSince = now;
         }
      }
   }
}
