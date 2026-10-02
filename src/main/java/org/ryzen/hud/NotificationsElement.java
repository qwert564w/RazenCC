package org.ryzen.hud;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.FeatureToggleEvent;
import org.ryzen.feature.Feature;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class NotificationsElement extends HudElement {
   private static final long LIFETIME_MS = 4000L;
   private static final long APPEAR_MS = 200L;
   private static final long FADE_MS = 300L;
   private static final int MAX_NOTES = 5;
   private static final int EXPIRING_TICKS = 400;
   private static final float DESIGN_WIDTH = 358.0F;
   private static final float TOAST_HEIGHT = 60.0F;
   private static final float WRAPPED_HEIGHT = 72.0F;
   private static final float STACK_GAP = 9.0F;
   private static final float TEXT_SIZE = 12.0F;
   private static final float LINE_HEIGHT = 14.0F;
   private static final float DESCRIPTION_WIDTH = 282.0F;
   private static final float SLIDE_DISTANCE = 24.0F;
   private static final int TOAST_BACKGROUND = ColorUtil.rgba(18, 18, 20, 3);
   private static final class_2960 SUCCESS_ICON = figma("notifications_config_saved_0_929.svg");
   private static final class_2960 ENABLED_ICON = figma("notifications_killaura_enabled_0_935.svg");
   private static final class_2960 DISABLED_ICON = figma("notifications_killaura_disabled_0_938.svg");
   private static final class_2960 ERIDA_ICON = figma("notifications_erida_star_0_945.svg");
   private static final class_2960 DISORIENTATION_ICON = figma("notifications_disorientation_star_0_951.svg");
   private static final class_2960 TOTEM_ICON = figma("notifications_totem_lost_0_957.svg");
   private static final String[] ROMAN = new String[]{"", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"};
   private static final Deque<NotificationsElement.Note> NOTES = new ArrayDeque<>();
   private final Map<class_2960, NotificationsElement.EffectState> trackedEffects = new HashMap<>();
   private final List<NotificationsElement.VisibleNote> visible = new ArrayList<>(5);

   public NotificationsElement() {
      super("notifications", "Notifications");
   }

   @Override
   protected float defaultX(float unit) {
      return 1908.0F * unit - this.width;
   }

   @Override
   protected float defaultY(float unit) {
      return 1066.0F * unit - this.height;
   }

   private static void featureToggled(Feature feature, boolean enabled) {
      class_310 mc = MinecraftContext.mc;
      if (mc != null && mc.field_1687 != null) {
         push(
            new NotificationsElement.Note(
               enabled ? ENABLED_ICON : DISABLED_ICON,
               feature.getName(),
               enabled ? "Модуль был успешно активирован." : "Модуль был успешно деактивирован.",
               System.currentTimeMillis()
            )
         );
      }
   }

   private static void push(NotificationsElement.Note note) {
      NOTES.addLast(note);

      while (NOTES.size() > 5) {
         NOTES.removeFirst();
      }
   }

   @Override
   protected void layout(class_310 mc, float unit) {
      this.trackEffects(mc);
      long now = System.currentTimeMillis();
      NOTES.removeIf(notex -> now - notex.createdAt() >= 4000L);
      List<NotificationsElement.Note> notes = new ArrayList<>(NOTES);
      boolean figmaShowcase = false;
      if (notes.isEmpty() && showcase(mc)) {
         notes.addAll(showcaseNotes(now));
         figmaShowcase = true;
      }

      this.visible.clear();
      if (notes.isEmpty()) {
         this.width = 0.0F;
         this.height = 0.0F;
      } else {
         MsdfFont font = UiFonts.sfProDisplay();
         float stackHeight = 0.0F;

         for (int index = 0; index < notes.size(); index++) {
            NotificationsElement.Note note = notes.get(index);
            List<String> lines = notificationLines(font, note, unit);
            float toastHeight;
            if (figmaShowcase) {
               toastHeight = index == 3 ? 72.0F : (index == notes.size() - 1 ? 58.0F : 60.0F);
            } else {
               toastHeight = lines.size() > 1 ? 72.0F : 60.0F;
            }

            float presence = envelope(note, now);
            this.visible.add(new NotificationsElement.VisibleNote(note, lines, toastHeight));
            stackHeight += (toastHeight + 9.0F) * unit * presence;
         }

         this.width = 358.0F * unit;
         this.height = Math.max(1.0F, stackHeight - 9.0F * unit);
      }
   }

   @Override
   protected void draw(class_310 mc, float unit) {
      long now = System.currentTimeMillis();
      float elementAlpha = this.appearAlpha();
      float toastY = this.y;

      for (NotificationsElement.VisibleNote entry : this.visible) {
         float presence = envelope(entry.note(), now);
         if (presence > 0.01F) {
            float toastX = this.x + (1.0F - presence) * 24.0F * unit;
            drawToast(entry, toastX, toastY, unit, elementAlpha * presence);
         }

         toastY += (entry.height() + 9.0F) * unit * presence;
      }
   }

   private static void drawToast(NotificationsElement.VisibleNote entry, float x, float y, float unit, float alpha) {
      float height = entry.height() * unit;
      Render2DUtil.rect(x, y, 358.0F * unit, height)
         .color(ColorUtil.multiplyAlpha(TOAST_BACKGROUND, alpha))
         .radius(13.0F * unit)
         .blur(25.0F * unit, alpha)
         .draw();
      float iconSize = 25.0F * unit;
      Render2DUtil.texture(x + 19.0F * unit, y + (height - iconSize) / 2.0F, iconSize, iconSize, entry.note().icon())
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .draw();
      Render2DUtil.text(x + 57.0F * unit, y + 12.0F * unit, 12.0F * unit, entry.note().title())
         .style(UiFontStyle.MEDIUM)
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .draw();

      for (int index = 0; index < entry.lines().size(); index++) {
         drawDescriptionLine(x + 57.0F * unit, y + (31.0F + index * 14.0F) * unit, 12.0F * unit, entry.lines().get(index), alpha);
      }
   }

   private static void drawDescriptionLine(float x, float y, float size, String line, float alpha) {
      MsdfFont font = UiFonts.sfProDisplay();
      float spacing = size * UiFontStyle.REGULAR.letterSpacingEm();
      List<NotificationsElement.TextSegment> segments = descriptionSegments(line);
      float cursorX = x;

      for (int index = 0; index < segments.size(); index++) {
         NotificationsElement.TextSegment segment = segments.get(index);
         if (!segment.text().isEmpty()) {
            Render2DUtil.text(cursorX, y, size, segment.text()).style(UiFontStyle.REGULAR).color(ColorUtil.multiplyAlpha(segment.color(), alpha)).draw();
            cursorX += font.measureWidth(segment.text(), size, spacing);
            if (index + 1 < segments.size()) {
               NotificationsElement.TextSegment next = segments.get(index + 1);
               if (!next.text().isEmpty()) {
                  int left = segment.text().codePointBefore(segment.text().length());
                  int right = next.text().codePointAt(0);
                  cursorX += font.kerning(left, right) * size + spacing;
               }
            }
         }
      }
   }

   private static List<NotificationsElement.TextSegment> descriptionSegments(String line) {
      String accent = accentFragment(line);
      if (accent == null) {
         return List.of(new NotificationsElement.TextSegment(line, HudPalette.TEXT_SECONDARY));
      }

      int start = line.indexOf(accent);
      int end = start + accent.length();
      List<NotificationsElement.TextSegment> segments = new ArrayList<>(3);
      if (start > 0) {
         segments.add(new NotificationsElement.TextSegment(line.substring(0, start), HudPalette.TEXT_SECONDARY));
      }

      segments.add(new NotificationsElement.TextSegment(line.substring(start, end), HudPalette.accentAlt()));
      if (end < line.length()) {
         segments.add(new NotificationsElement.TextSegment(line.substring(end), HudPalette.TEXT_SECONDARY));
      }

      return List.copyOf(segments);
   }

   private static String accentFragment(String line) {
      int star = line.indexOf("звезду");
      if (star >= 0) {
         return line.substring(star);
      } else if (line.startsWith("дезориентации")) {
         return line;
      } else {
         return line.contains("1284") ? "1284" : null;
      }
   }

   private static float envelope(NotificationsElement.Note note, long now) {
      long age = now - note.createdAt();
      float appear = Math.clamp((float)age / 200.0F, 0.0F, 1.0F);
      float disappear = Math.clamp((float)(4000L - age) / 300.0F, 0.0F, 1.0F);
      float value = Math.min(appear, disappear);
      return value * value * (3.0F - 2.0F * value);
   }

   private static List<String> wrap(MsdfFont font, String value, float maxWidth, float size) {
      float spacing = size * UiFontStyle.REGULAR.letterSpacingEm();
      if (font.measureWidth(value, size, spacing) <= maxWidth) {
         return List.of(value);
      }

      List<String> lines = new ArrayList<>(2);
      StringBuilder line = new StringBuilder();

      for (String word : value.split(" ")) {
         String candidate = line.isEmpty() ? word : line + " " + word;
         if (!line.isEmpty() && font.measureWidth(candidate, size, spacing) > maxWidth) {
            lines.add(line.toString());
            line.setLength(0);
            if (lines.size() == 2) {
               break;
            }
         }

         if (lines.size() < 2) {
            if (!line.isEmpty()) {
               line.append(' ');
            }

            line.append(word);
         }
      }

      if (!line.isEmpty() && lines.size() < 2) {
         lines.add(line.toString());
      }

      return lines.isEmpty() ? List.of(value) : List.copyOf(lines);
   }

   private static List<String> notificationLines(MsdfFont font, NotificationsElement.Note note, float unit) {
      String description = note.description();
      String forcedTail = "дезориентации.";
      return description.endsWith("звезду " + forcedTail)
         ? List.of(description.substring(0, description.length() - forcedTail.length()).stripTrailing(), forcedTail)
         : wrap(font, description, 282.0F * unit, 12.0F * unit);
   }

   private void trackEffects(class_310 mc) {
      if (mc.field_1724 == null) {
         this.trackedEffects.clear();
      } else {
         Map<class_2960, NotificationsElement.EffectState> current = new HashMap<>();

         for (class_1293 effect : mc.field_1724.method_6026()) {
            class_2960 id = ((class_5321)effect.method_5579().method_40230().orElseThrow()).method_29177();
            int duration = effect.method_5584();
            int amplifier = effect.method_5578();
            NotificationsElement.EffectState previous = this.trackedEffects.get(id);
            boolean applied = previous == null || amplifier != previous.amplifier() || !effect.method_48559() && duration > previous.duration() + 200;
            boolean expiryNotified = previous != null && previous.expiryNotified();
            if (applied) {
               pushEffect(effect, true, duration);
               expiryNotified = false;
            } else if (!expiryNotified && !effect.method_48559() && duration <= 400) {
               pushEffect(effect, false, duration);
               expiryNotified = true;
            }

            current.put(id, new NotificationsElement.EffectState(duration, amplifier, expiryNotified));
         }

         this.trackedEffects.clear();
         this.trackedEffects.putAll(current);
      }
   }

   private static void pushEffect(class_1293 effect, boolean applied, int durationTicks) {
      String title = ((class_1291)effect.method_5579().comp_349()).method_5560().getString() + roman(effect.method_5578());
      String duration = effect.method_48559() ? "∞" : formatTicks(durationTicks);
      push(
         new NotificationsElement.Note(
            SUCCESS_ICON, title, applied ? "Эффект применён на " + duration + "." : "Эффект закончится через " + duration + ".", System.currentTimeMillis()
         )
      );
   }

   private static String roman(int amplifier) {
      if (amplifier <= 0) {
         return "";
      } else {
         return amplifier < ROMAN.length ? ROMAN[amplifier] : " " + (amplifier + 1);
      }
   }

   private static String formatTicks(int ticks) {
      int seconds = Math.max(0, ticks / 20);
      return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
   }

   private static List<NotificationsElement.Note> showcaseNotes(long now) {
      long stableCreatedAt = now - 2000L;
      return List.of(
         new NotificationsElement.Note(SUCCESS_ICON, "Конфигурация успешно сохранена", "Мы успешно сохранили все ваши настройки.", stableCreatedAt),
         new NotificationsElement.Note(ENABLED_ICON, "KillAura", "Модуль был успешно активирован.", stableCreatedAt),
         new NotificationsElement.Note(ERIDA_ICON, "Звезда сферы Эрида поднята", "Вы успешно подняли звезду сферы Эрида.", stableCreatedAt),
         new NotificationsElement.Note(
            DISORIENTATION_ICON, "Звезда дезориентации использована", "Вы успешно использовали звезду дезориентации.", stableCreatedAt
         ),
         new NotificationsElement.Note(TOTEM_ICON, "Тотем бессмертия потерян", "Игрок 1284 лишился тотема бессмертия x1.", stableCreatedAt)
      );
   }

   private static class_2960 figma(String file) {
      return class_2960.method_60654("ryzen:textures/hud/figma/" + file);
   }

   @Environment(EnvType.CLIENT)
   private record EffectState(int duration, int amplifier, boolean expiryNotified) {
   }

   @Environment(EnvType.CLIENT)
   private record Note(class_2960 icon, String title, String description, long createdAt) {
   }

   @Environment(EnvType.CLIENT)
   private record TextSegment(String text, int color) {
   }

   @Environment(EnvType.CLIENT)
   public static final class ToggleListener {
      @EventTarget
      public void onFeatureToggle(FeatureToggleEvent event) {
         NotificationsElement.featureToggled(event.getFeature(), event.isEnabled());
      }
   }

   @Environment(EnvType.CLIENT)
   private record VisibleNote(NotificationsElement.Note note, List<String> lines, float height) {
   }
}
