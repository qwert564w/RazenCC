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
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4081;
import net.minecraft.class_5321;
import org.joml.Matrix3x2fStack;
import org.ryzen.context.RenderContext;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class CooldownsElement extends HudElement {
   private static final float DESIGN_WIDTH = 216.0F;
   private static final float ROW_HEIGHT = 31.0F;
   private static final float TEXT_SIZE = 12.0F;
   private static final int SHOWCASE_ROWS = 3;
   private static final float MAX_SCREEN_FRACTION = 0.8F;
   private static final double TWO_PI = Math.PI * 2;
   private static final int DANGER = ColorUtil.rgb(255, 50, 32);
   private static final class_2960 TITLE_ICON = figma("cooldowns_title_0_963.svg");
   private static final class_2960 CLOSE_ICON = figma("cooldowns_close_0_961.svg");
   private final List<CooldownsElement.Row> rows = new ArrayList<>(3);
   private final Map<CooldownsElement.EffectKey, Integer> effectMaxDurations = new HashMap<>();

   public CooldownsElement() {
      super("cooldowns", "Cooldowns");
   }

   @Override
   protected float defaultX(float unit) {
      return 277.0F * unit;
   }

   @Override
   protected float defaultY(float unit) {
      return 488.0F * unit;
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
         this.height = (49.0F + this.rows.size() * 31.0F) * unit;
      }
   }

   @Override
   protected void draw(class_310 mc, float unit) {
      if (!this.rows.isEmpty()) {
         float alpha = this.appearAlpha();
         float[] inner = this.drawCard(unit, alpha, 38.0F, null, null, 6.0F, 6.0F);
         this.drawHeader(unit, alpha);
         float innerX = inner[0];
         float nameX = this.x + 18.0F * unit;
         float effectIconX = this.x + 136.0F * unit;
         float dividerX = this.x + 156.0F * unit;
         float timerCenterX = this.x + 169.5F * unit;
         float timeX = this.x + 178.0F * unit;
         MsdfFont font = UiFonts.sfProDisplay();

         for (int index = 0; index < this.rows.size(); index++) {
            CooldownsElement.Row row = this.rows.get(index);
            float rowY = this.y + (50.0F + index * 31.0F) * unit;
            float centerY = rowY + 7.0F * unit;
            float textSize = 12.0F * unit;
            String name = fit(font, row.name(), 112.0F * unit, textSize);
            Render2DUtil.text(nameX, font.centeredTextY(centerY, textSize), textSize, name)
               .style(UiFontStyle.MEDIUM)
               .color(ColorUtil.multiplyAlpha(row.color(), alpha))
               .draw();
            float iconSize = 12.0F * unit;
            Render2DUtil.TextureBuilder icon = Render2DUtil.texture(effectIconX, centerY - iconSize / 2.0F, iconSize, iconSize, row.icon());
            if (row.managedIcon()) {
               icon.managed();
               icon.color(ColorUtil.multiplyAlpha(-1, alpha));
            } else {
               icon.color(ColorUtil.multiplyAlpha(HudPalette.accent(), alpha));
            }

            icon.draw();
            float timerSize = 9.0F * unit;
            Render2DUtil.rect(dividerX, rowY + 2.0F * unit, 1.0F * unit, 9.0F * unit)
               .color(ColorUtil.multiplyAlpha(DIVIDER_COLOR, alpha))
               .radius(2.0F * unit)
               .draw();
            drawDurationRing(timerCenterX, centerY, timerSize, row.durationFraction(), ColorUtil.multiplyAlpha(HudPalette.accent(), alpha));
            Render2DUtil.text(timeX, font.centeredTextY(centerY, textSize), textSize, row.time())
               .style(UiFontStyle.MEDIUM)
               .color(ColorUtil.multiplyAlpha(Theme.Colors.TEXT_TEXT, alpha))
               .draw();
            if (index < this.rows.size() - 1) {
               Render2DUtil.rect(this.x + 18.0F * unit, rowY + 22.0F * unit, 179.0F * unit, unit).color(ColorUtil.multiplyAlpha(DIVIDER_COLOR, alpha)).draw();
            }
         }
      }
   }

   private void drawHeader(float unit, float alpha) {
      float iconSize = 12.0F * unit;
      float centerY = this.y + 38.0F * unit / 2.0F;
      float iconX = this.x + 18.0F * unit;
      Render2DUtil.texture(iconX, centerY - iconSize / 2.0F, iconSize, iconSize, TITLE_ICON).color(ColorUtil.multiplyAlpha(HudPalette.accent(), alpha)).draw();
      float titleSize = 12.0F * unit;
      Render2DUtil.text(iconX + iconSize + 9.0F * unit, UiFonts.sfProDisplay().centeredTextY(centerY, titleSize), titleSize, "Cooldowns")
         .style(UiFontStyle.MEDIUM)
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .draw();
      float closeSize = 10.0F * unit;
      Render2DUtil.texture(this.x + this.width - 29.0F * unit, centerY - closeSize / 2.0F, closeSize, closeSize, CLOSE_ICON)
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .draw();
   }

   private void collectRows(class_310 mc, float unit) {
      this.rows.clear();
      int maxRows = maxRows(mc, unit);
      Set<CooldownsElement.EffectKey> activeEffectKeys = new HashSet<>();
      if (mc.field_1724 != null) {
         for (class_1293 effect : mc.field_1724.method_6026()) {
            if (this.rows.size() >= maxRows) {
               break;
            }

            class_2960 effectId = ((class_5321)effect.method_5579().method_40230().orElseThrow()).method_29177();
            CooldownsElement.EffectKey effectKey = new CooldownsElement.EffectKey(effectId, effect.method_5578());
            activeEffectKeys.add(effectKey);
            class_2960 icon = effectId.method_45134(path -> "textures/mob_effect/" + path + ".png");
            boolean harmful = ((class_1291)effect.method_5579().comp_349()).method_18792() == class_4081.field_18272;
            this.rows
               .add(
                  new CooldownsElement.Row(
                     ((class_1291)effect.method_5579().comp_349()).method_5560().getString(),
                     icon,
                     true,
                     effectTime(effect),
                     harmful ? DANGER : Theme.Colors.TEXT_TEXT,
                     this.effectProgress(effectKey, effect)
                  )
               );
         }

         this.collectItemCooldowns(mc, maxRows);
      }

      this.effectMaxDurations.keySet().retainAll(activeEffectKeys);
      if (this.rows.isEmpty() && showcase(mc)) {
         this.rows.add(showcaseEffect("speed", "Speed", "1:31"));
         this.rows.add(showcaseEffect("fire_resistance", "Fire Resistance", "0:14"));
         this.rows.add(showcaseEffect("strength", "Strength", "2:45"));
      }
   }

   private static int maxRows(class_310 mc, float unit) {
      if (unit <= 0.0F) {
         return 3;
      }

      float available = mc.method_22683().method_4502() * 0.8F - 49.0F * unit;
      return Math.max(1, (int)(available / (31.0F * unit)));
   }

   private void collectItemCooldowns(class_310 mc, int maxRows) {
      if (this.rows.size() < maxRows && mc.field_1724 != null) {
         Set<class_2960> seenGroups = new HashSet<>();

         for (int slot = 0; slot < 36 && this.rows.size() < maxRows; slot++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
            if (!stack.method_7960() && mc.field_1724.method_7357().method_7904(stack)) {
               class_2960 group = mc.field_1724.method_7357().method_62836(stack);
               if (seenGroups.add(group)) {
                  float fraction = Math.clamp(mc.field_1724.method_7357().method_7905(stack, 0.0F), 0.0F, 1.0F);
                  this.rows
                     .add(
                        new CooldownsElement.Row(
                           stack.method_7964().getString(),
                           class_2960.method_60654("ryzen:textures/menu/icons/dices.svg"),
                           false,
                           cooldownTime(group, fraction),
                           Theme.Colors.TEXT_TEXT,
                           fraction
                        )
                     );
               }
            }
         }
      }
   }

   private static CooldownsElement.Row showcaseEffect(String id, String name, String time) {
      float seconds = parseSeconds(time);
      return new CooldownsElement.Row(
         name, class_2960.method_60656("textures/mob_effect/" + id + ".png"), true, time, Theme.Colors.TEXT_TEXT, Math.clamp(seconds / 180.0F, 0.0F, 1.0F)
      );
   }

   private static float parseSeconds(String time) {
      String[] parts = time.split(":", 2);
      return parts.length == 2 ? Integer.parseInt(parts[0]) * 60.0F + Integer.parseInt(parts[1]) : 0.0F;
   }

   private float effectProgress(CooldownsElement.EffectKey key, class_1293 effect) {
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

   private static String cooldownTime(class_2960 group, float fraction) {
      int totalTicks = switch (group.method_12832()) {
         case "ender_pearl", "chorus_fruit" -> 20;
         case "wind_charge" -> 10;
         case "shield" -> 100;
         default -> -1;
      };
      if (totalTicks < 0) {
         return Math.max(1, Math.round(fraction * 100.0F)) + "%";
      }

      int ticks = Math.max(0, Math.round(totalTicks * fraction));
      int seconds = (ticks + 19) / 20;
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
   private record Row(String name, class_2960 icon, boolean managedIcon, String time, int color, float durationFraction) {
   }
}
