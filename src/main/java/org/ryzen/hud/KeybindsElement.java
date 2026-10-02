package org.ryzen.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.ryzen.event.EventManager;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.FeatureToggleEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BindSetting;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class KeybindsElement extends HudElement {
   private static final float DESIGN_WIDTH = 201.0F;
   private static final float ROW_HEIGHT = 31.0F;
   private static final float ROWS_TOP = 50.0F;
   private static final float ROWS_BOTTOM = 28.0F;
   private static final float TEXT_SIZE = 12.0F;
   private static final class_2960 TITLE_ICON = figma("keybinds_title_0_793.svg");
   private static final class_2960 CLOSE_ICON = figma("keybinds_close_0_791.svg");
   private static final class_2960 KEYBOARD_ICON = figma("keybinds_kill_aura_keyboard_0_799.svg");
   private static final class_2960 STATUS_ICON = figma("keybinds_kill_aura_status_0_801.svg");
   private final List<KeybindsElement.Row> rows = new ArrayList<>();
   private boolean rowsDirty = true;
   private int lastFingerprint;

   public KeybindsElement() {
      super("keybinds", "Keybinds");
      EventManager.subscribe(this);
   }

   @EventTarget
   public void onFeatureToggle(FeatureToggleEvent event) {
      this.rowsDirty = true;
   }

   @Override
   protected float defaultX(float unit) {
      return 1196.0F * unit;
   }

   @Override
   protected float defaultY(float unit) {
      return 512.0F * unit;
   }

   @Override
   protected boolean hasHeaderCloseButton() {
      return true;
   }

   @Override
   protected void layout(class_310 mc, float unit) {
      int fingerprint = this.fingerprint(mc);
      if (this.rowsDirty || fingerprint != this.lastFingerprint) {
         this.rowsDirty = false;
         this.lastFingerprint = fingerprint;
         this.collectRows(mc);
      }

      if (this.rows.isEmpty()) {
         this.width = 0.0F;
         this.height = 0.0F;
      } else {
         this.width = 201.0F * unit;
         this.height = (50.0F + (this.rows.size() - 1) * 31.0F + 28.0F) * unit;
      }
   }

   @Override
   protected void draw(class_310 mc, float unit) {
      if (!this.rows.isEmpty()) {
         float alpha = this.appearAlpha();
         this.drawCard(unit, alpha, 38.0F, null, null, 5.0F, 6.0F);
         this.drawHeader(unit, alpha);
         MsdfFont font = UiFonts.sfProDisplay();
         float textSize = 12.0F * unit;
         float spacing = textSize * UiFontStyle.MEDIUM.letterSpacingEm();
         float rowX = this.x + 18.0F * unit;

         for (int index = 0; index < this.rows.size(); index++) {
            KeybindsElement.Row row = this.rows.get(index);
            float rowY = this.y + (50.0F + index * 31.0F) * unit;
            float centerY = rowY + 7.0F * unit;
            float textY = font.centeredTextY(centerY, textSize);
            String name = fit(font, row.name(), 96.0F * unit, textSize);
            String bind = displayBind(row.bind());
            Render2DUtil.text(rowX, textY, textSize, name).style(UiFontStyle.MEDIUM).color(ColorUtil.multiplyAlpha(-1, alpha)).draw();
            float bindWidth = font.measureWidth(bind, textSize, spacing);
            float bindRight = rowX + 140.0F * unit;
            float bindLeft = bindRight - bindWidth;
            Render2DUtil.texture(bindLeft - 19.0F * unit, rowY + 2.0F * unit, 13.0F * unit, 9.0F * unit, KEYBOARD_ICON)
               .color(ColorUtil.multiplyAlpha(-1, alpha))
               .draw();
            Render2DUtil.text(bindRight, textY, textSize, bind)
               .style(UiFontStyle.MEDIUM)
               .color(ColorUtil.multiplyAlpha(-1, alpha))
               .align(TextAlign.RIGHT)
               .draw();
            Render2DUtil.rect(rowX + 147.0F * unit, rowY + 2.0F * unit, 1.0F * unit, 9.0F * unit)
               .color(ColorUtil.multiplyAlpha(HudPalette.DIVIDER, alpha))
               .radius(2.0F * unit)
               .draw();
            Render2DUtil.texture(rowX + 155.0F * unit, rowY + 2.0F * unit, 9.9278F * unit, 9.5117F * unit, STATUS_ICON)
               .color(ColorUtil.multiplyAlpha(HudPalette.accent(), alpha * (row.enabled() ? 1.0F : 0.45F)))
               .draw();
            if (index < this.rows.size() - 1) {
               Render2DUtil.rect(rowX, rowY + 22.0F * unit, 165.0F * unit, 1.0F * unit)
                  .color(ColorUtil.multiplyAlpha(HudPalette.DIVIDER, alpha))
                  .radius(2.0F * unit)
                  .draw();
            }
         }
      }
   }

   private void drawHeader(float unit, float alpha) {
      Render2DUtil.texture(this.x + 18.0F * unit, this.y + 14.0F * unit, 10.0F * unit, 10.0F * unit, TITLE_ICON)
         .color(ColorUtil.multiplyAlpha(HudPalette.accent(), alpha))
         .draw();
      Render2DUtil.text(this.x + 36.0F * unit, this.y + 12.0F * unit, 12.0F * unit, "Keybinds")
         .style(UiFontStyle.MEDIUM)
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .draw();
      Render2DUtil.texture(this.x + 173.0F * unit, this.y + 14.0F * unit, 10.0F * unit, 10.0F * unit, CLOSE_ICON)
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .draw();
   }

   private int fingerprint(class_310 mc) {
      int result = showcase(mc) ? 1 : 0;

      for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
         result = result * 31 + feature.getName().hashCode();
         List<Integer> binds = feature.getBinds();
         result = result * 31 + binds.hashCode();

         for (int index = 0; index < binds.size(); index++) {
            result = result * 31 + (feature.isBindVisibleAt(index) ? 1 : 0);
         }

         result = result * 31 + (feature.isEnabled() ? 1 : 0);
      }

      return result;
   }

   private void collectRows(class_310 mc) {
      this.rows.clear();

      for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
         StringJoiner binds = new StringJoiner(" + ");
         List<Integer> codes = feature.getBinds();

         for (int index = 0; index < codes.size(); index++) {
            int code = codes.get(index);
            if (code != -1 && feature.isBindVisibleAt(index)) {
               binds.add(BindSetting.describe(code));
            }
         }

         if (binds.length() > 0) {
            this.rows.add(new KeybindsElement.Row(displayName(feature.getName()), binds.toString(), feature.isEnabled()));
         }
      }

      if (this.rows.isEmpty() && showcase(mc)) {
         this.rows.add(new KeybindsElement.Row("Kill Aura", "R", true));
         this.rows.add(new KeybindsElement.Row("Aim Bow", "Z", true));
         this.rows.add(new KeybindsElement.Row("Auto Armor", "SHIFT", true));
         this.rows.add(new KeybindsElement.Row("Criticals", "Q", true));
      }
   }

   private static String displayName(String name) {
      return switch (name) {
         case "Aura", "KillAura", "Kill Aura" -> "Kill Aura";
         case "BowAimbot", "AimBow", "Aim Bow" -> "Aim Bow";
         case "AutoArmor", "Auto Armor" -> "Auto Armor";
         default -> name.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
      };
   }

   private static String displayBind(String bind) {
      String value = MenuText.bindCombination(bind);
      if (!"LShift".equalsIgnoreCase(value) && !"RShift".equalsIgnoreCase(value)) {
         return value.length() <= 6 ? value.toUpperCase(Locale.ROOT) : value;
      } else {
         return "SHIFT";
      }
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
   private record Row(String name, String bind, boolean enabled) {
   }
}
