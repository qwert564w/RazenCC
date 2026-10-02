package org.ryzen.hud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5321;
import org.joml.Matrix3x2fStack;
import org.ryzen.context.RenderContext;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class PotionsElement extends HudElement {
   private static final float DESIGN_WIDTH = 216.0F;
   private static final float ROW_HEIGHT = 31.0F;
   private static final float TEXT_SIZE = 12.0F;
   private static final int SHOWCASE_ROWS = 4;
   private static final float MAX_SCREEN_FRACTION = 0.8F;
   private static final double TWO_PI = Math.PI * 2;
   private static final int LEVEL_ONE = ColorUtil.rgba(255, 255, 255, 128);
   private static final class_2960 TITLE_ICON = figma("effects_title_0_892.svg");
   private static final class_2960 CLOSE_ICON = figma("effects_close_0_890.svg");
   private final List<PotionsElement.Row> rows = new ArrayList<>(4);
   private final Map<PotionsElement.EffectKey, Integer> effectMaxDurations = new HashMap<>();

   public PotionsElement() {
      super("potions", "Effects");
   }

   @Override
   protected float defaultX(float unit) {
      return 1054.0F * unit;
   }

   @Override
   protected float defaultY(float unit) {
      return 118.0F * unit;
   }

   @Override
   protected boolean hasHeaderCloseButton() {
      return true;
   }

   @Override
   protected void layout(class_310 mc, float unit) {
      this.collectRows(mc, unit);
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
         this.drawPanel(13.0F * unit, unit, alpha);
         Render2DUtil.rect(this.x + 6.0F * unit, this.y + 38.0F * unit, 204.0F * unit, this.height - 44.0F * unit)
            .color(ColorUtil.multiplyAlpha(HudPalette.SURFACE, alpha))
            .radius(13.0F * unit)
            .border(0.5F * unit, ColorUtil.multiplyAlpha(HudPalette.SURFACE_BORDER, alpha))
            .draw();
         this.drawHeader(unit, alpha);
         MsdfFont font = UiFonts.sfProDisplay();
         float textSize = 12.0F * unit;
         float spacing = textSize * UiFontStyle.MEDIUM.letterSpacingEm();
         float rowX = this.x + 18.0F * unit;
         float timerCenterX = this.x + 169.5F * unit;

         for (int index = 0; index < this.rows.size(); index++) {
            PotionsElement.Row row = this.rows.get(index);
            float rowY = this.y + (50.0F + index * 31.0F) * unit;
            float centerY = rowY + 7.0F * unit;
            float textY = font.centeredTextY(centerY, textSize);
            String name = fit(font, row.name(), 91.0F * unit, textSize);
            Render2DUtil.text(rowX, textY, textSize, name).style(UiFontStyle.MEDIUM).color(ColorUtil.multiplyAlpha(-1, alpha)).draw();
            float nameWidth = font.measureWidth(name, textSize, spacing);
            Render2DUtil.text(rowX + nameWidth + 4.0F * unit, textY, textSize, Integer.toString(row.level()))
               .style(UiFontStyle.MEDIUM)
               .color(ColorUtil.multiplyAlpha(row.level() > 1 ? HudPalette.accent() : LEVEL_ONE, alpha))
               .draw();
            Render2DUtil.text(this.x + 178.0F * unit, textY, textSize, row.time()).style(UiFontStyle.MEDIUM).color(ColorUtil.multiplyAlpha(-1, alpha)).draw();
            float effectIconSize = 12.0F * unit;
            Render2DUtil.texture(this.x + 136.0F * unit, rowY, effectIconSize, effectIconSize, row.icon())
               .managed()
               .color(ColorUtil.multiplyAlpha(-1, alpha))
               .draw();
            Render2DUtil.rect(this.x + 156.0F * unit, rowY + 2.0F * unit, 1.0F * unit, 9.0F * unit)
               .color(ColorUtil.multiplyAlpha(HudPalette.DIVIDER, alpha))
               .radius(2.0F * unit)
               .draw();
            drawDurationRing(timerCenterX, centerY, 9.0F * unit, row.durationFraction(), ColorUtil.multiplyAlpha(HudPalette.accent(), alpha));
            if (index < this.rows.size() - 1) {
               Render2DUtil.rect(rowX, rowY + 22.0F * unit, 179.0F * unit, 1.0F * unit)
                  .color(ColorUtil.multiplyAlpha(HudPalette.DIVIDER, alpha))
                  .radius(2.0F * unit)
                  .draw();
            }
         }
      }
   }

   private void drawHeader(float unit, float alpha) {
      Render2DUtil.texture(this.x + 18.0F * unit, this.y + 12.0F * unit, 8.0F * unit, 13.0F * unit, TITLE_ICON)
         .color(ColorUtil.multiplyAlpha(HudPalette.accent(), alpha))
         .draw();
      Render2DUtil.text(this.x + 36.0F * unit, this.y + 12.0F * unit, 12.0F * unit, "Effects")
         .style(UiFontStyle.MEDIUM)
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .draw();
      Render2DUtil.texture(this.x + 187.0F * unit, this.y + 14.0F * unit, 10.0F * unit, 10.0F * unit, CLOSE_ICON)
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .draw();
   }

   private void collectRows(class_310 mc, float unit) {
      this.rows.clear();
      int maxRows = maxRows(mc, unit);
      Set<PotionsElement.EffectKey> activeEffectKeys = new HashSet<>();
      if (mc.field_1724 != null) {
         for (class_1293 effect : mc.field_1724.method_6026()) {
            if (this.rows.size() >= maxRows) {
               break;
            }

            class_2960 effectId = ((class_5321)effect.method_5579().method_40230().orElseThrow()).method_29177();
            PotionsElement.EffectKey effectKey = new PotionsElement.EffectKey(effectId, effect.method_5578());
            activeEffectKeys.add(effectKey);
            this.rows
               .add(
                  new PotionsElement.Row(
                     ((class_1291)effect.method_5579().comp_349()).method_5560().getString(),
                     effect.method_5578() + 1,
                     effectId.method_45134(path -> "textures/mob_effect/" + path + ".png"),
                     effectTime(effect),
                     this.effectProgress(effectKey, effect)
                  )
               );
         }
      }

      this.effectMaxDurations.keySet().retainAll(activeEffectKeys);
      if (this.rows.isEmpty() && showcase(mc)) {
         this.rows.add(showcaseRow("speed", "Speed", 2, "1:31"));
         this.rows.add(showcaseRow("fire_resistance", "Fire Resistance", 1, "0:14"));
         this.rows.add(showcaseRow("strength", "Strength", 3, "2:45"));
         this.rows.add(showcaseRow("blindness", "Blindness", 2, "0:42"));
      }
   }

   private static int maxRows(class_310 mc, float unit) {
      if (unit <= 0.0F) {
         return 4;
      }

      float available = mc.method_22683().method_4502() * 0.8F - 47.0F * unit;
      return Math.max(1, (int)(available / (31.0F * unit)));
   }

   private static PotionsElement.Row showcaseRow(String id, String name, int level, String time) {
      return new PotionsElement.Row(
         name, level, class_2960.method_60656("textures/mob_effect/" + id + ".png"), time, Math.clamp(parseSeconds(time) / 180.0F, 0.0F, 1.0F)
      );
   }

   private static float parseSeconds(String time) {
      String[] parts = time.split(":", 2);
      return parts.length == 2 ? Integer.parseInt(parts[0]) * 60.0F + Integer.parseInt(parts[1]) : 0.0F;
   }

   private float effectProgress(PotionsElement.EffectKey key, class_1293 effect) {
      if (effect.method_48559()) {
         return 1.0F;
      }

      int remaining = Math.max(0, effect.method_5584());
      int maximum = this.effectMaxDurations.merge(key, Math.max(1, remaining), Math::max);
      return Math.clamp((float)remaining / maximum, 0.0F, 1.0F);
   }

   private static void drawDurationRing(float centerX, float centerY, float size, float fraction, int color) {
      float clamped = Math.clamp(fraction, 0.0F, 1.0F);
      if (!(clamped <= 1.0E-4F)) {
         float thickness = Math.max(0.75F, size * 0.116F);
         float radius = Math.max(0.0F, (size - thickness) * 0.5F);
         float sweep = (float) (Math.PI * 2) * clamped;
         int segments = Math.max(2, (int)Math.ceil(sweep / Math.toRadians(6.0)));
         float start = (float) (-Math.PI / 2);

         for (int index = 0; index < segments; index++) {
            float angle0 = start - sweep * index / segments;
            float angle1 = start - sweep * (index + 1) / segments;
            float x0 = centerX + (float)Math.cos(angle0) * radius;
            float y0 = centerY + (float)Math.sin(angle0) * radius;
            float x1 = centerX + (float)Math.cos(angle1) * radius;
            float y1 = centerY + (float)Math.sin(angle1) * radius;
            strokeSegment(x0, y0, x1, y1, thickness, color);
         }
      }
   }

   private static void strokeSegment(float x0, float y0, float x1, float y1, float thickness, int color) {
      float dx = x1 - x0;
      float dy = y1 - y0;
      float length = (float)Math.sqrt(dx * dx + dy * dy);
      if (!(length <= 0.001F)) {
         class_332 graphics = RenderContext.currentGuiGraphicsExtractor();
         if (graphics != null) {
            Matrix3x2fStack pose = graphics.method_51448();
            float half = thickness * 0.5F;
            float overlap = Math.min(thickness * 0.3F, length * 0.45F);
            pose.pushMatrix();
            pose.translate(x0, y0);
            pose.rotate((float)Math.atan2(dy, dx));
            Render2DUtil.rect(-overlap, -half, length + overlap * 2.0F, thickness).color(color).radius(half).draw();
            pose.popMatrix();
         }
      }
   }

   private static String effectTime(class_1293 effect) {
      if (effect.method_48559()) {
         return "∞";
      }

      int seconds = Math.max(0, effect.method_5584() / 20);
      return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
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
   private record EffectKey(class_2960 id, int amplifier) {
   }

   @Environment(EnvType.CLIENT)
   private record Row(String name, int level, class_2960 icon, String time, float durationFraction) {
   }
}
