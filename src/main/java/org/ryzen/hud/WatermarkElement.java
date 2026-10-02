package org.ryzen.hud;

import java.net.InetSocketAddress;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_640;
import net.minecraft.class_642;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class WatermarkElement extends HudElement {
   private static final float DESIGN_WIDTH = 239.0F;
   private static final String PROFILE_NAME = "https://t.me/crackdelay";
   private static final float PROFILE_PANEL_X = 11.0F;
   private static final float PROFILE_PANEL_WIDTH = 108.0F;
   private static final float PROFILE_TEXT_X = 34.0F;
   private static final float PROFILE_TEXT_RIGHT_PADDING = 8.0F;
   private static final float SERVER_PANEL_WIDTH = 105.0F;
   private static final float MAX_SERVER_PANEL = 215.0F;
   private static final float SERVER_TEXT_LEFT = 34.0F;
   private static final float SERVER_TEXT_RIGHT = 8.0F;
   private static final float DESIGN_HEIGHT = 134.0F;
   private static final float VALUE_SUFFIX_GAP = 2.0F;
   private static final class_2960 LOGO_UPPER = figma("brand_logo_upper_0_990.svg");
   private static final class_2960 LOGO_LOWER = figma("brand_logo_lower_0_992.svg");
   private static final class_2960 TELEGRAM = figma("telemetry_telegram_0_998.svg");
   private static final class_2960 FPS = figma("telemetry_fps_0_1002.svg");
   private static final class_2960 PING = figma("telemetry_ping_0_1005.svg");
   private static final class_2960 SERVER = figma("telemetry_server_0_1008.svg");

   public WatermarkElement() {
      super("watermark", "Watermark");
   }

   @Override
   protected void layout(class_310 mc, float unit) {
      this.width = 239.0F * unit;
      this.height = 134.0F * unit;
   }

   @Override
   protected float defaultX(float unit) {
      return 12.0F * unit;
   }

   @Override
   protected float defaultY(float unit) {
      return 9.0F * unit;
   }

   @Override
   protected void draw(class_310 mc, float unit) {
      float alpha = this.appearAlpha();
      this.drawLogo(unit, alpha);
      this.drawTelemetry(mc, unit, alpha);
      this.drawServer(mc, unit, alpha);
   }

   private void drawLogo(float unit, float alpha) {
      float logoX = this.x;
      float logoY = this.y;
      float logoSize = 48.0F * unit;
      Render2DUtil.rect(logoX, logoY, logoSize, logoSize)
         .color(ColorUtil.multiplyAlpha(HudPalette.PANEL, alpha))
         .radius(24.0F * unit)
         .blur(50.0F * unit, alpha)
         .draw();
      Render2DUtil.rect(logoX + 4.0F * unit, logoY + 4.0F * unit, 40.0F * unit, 40.0F * unit)
         .color(ColorUtil.multiplyAlpha(ColorUtil.withAlpha(HudPalette.accent(), 179), alpha))
         .radius(20.0F * unit)
         .draw();
      Render2DUtil.rect(logoX + 5.0F * unit, logoY + 5.0F * unit, 38.0F * unit, 38.0F * unit)
         .color(ColorUtil.multiplyAlpha(HudPalette.PANEL, alpha))
         .radius(19.0F * unit)
         .draw();
      drawFigmaTexture(logoX + 16.0F * unit, logoY + 16.0F * unit, 16.42F * unit, 11.85F * unit, LOGO_UPPER, alpha);
      drawFigmaTexture(logoX + 16.0F * unit, logoY + 24.0F * unit, 13.64F * unit, 8.24F * unit, LOGO_LOWER, alpha);
   }

   private void drawTelemetry(class_310 mc, float unit, float alpha) {
      float panelX = this.x + 4.0F * unit;
      float panelY = this.y + 55.0F * unit;
      drawGlass(panelX, panelY, 235.0F * unit, 36.0F * unit, 10.0F * unit, unit, alpha);
      Render2DUtil.rect(this.x + 11.0F * unit, this.y + 61.0F * unit, 108.0F * unit, 23.0F * unit)
         .color(ColorUtil.multiplyAlpha(HudPalette.accent(), alpha))
         .radius(6.0F * unit)
         .draw();
      drawFigmaTexture(this.x + 18.0F * unit, this.y + 67.0F * unit, 11.0F * unit, 11.0F * unit, TELEGRAM, -1, alpha);
      MsdfFont font = UiFonts.sfProDisplay();
      float valueSize = 11.0F * unit;
      float suffixSize = 9.0F * unit;
      float profileAvailable = 77.0F * unit;
      float profileSize = fitTextSize(font, "https://t.me/crackdelay", valueSize, 8.0F * unit, profileAvailable);
      float profileSpacing = profileSize * UiFontStyle.MEDIUM.letterSpacingEm();
      String shownProfile = font.ellipsize("https://t.me/crackdelay", profileSize, profileSpacing, profileAvailable);
      drawText(font, this.x + 34.0F * unit, this.y + 72.5F * unit, profileSize, shownProfile, -1, alpha, TextAlign.LEFT);
      drawFigmaTexture(this.x + 128.0F * unit, this.y + 68.0F * unit, 12.0F * unit, 10.0F * unit, FPS, alpha);
      this.drawMetric(font, unit, alpha, 144.0F, 161.0F, Integer.toString(mc.method_47599()), "FPS", valueSize, suffixSize);
      drawFigmaTexture(this.x + 183.0F * unit, this.y + 68.0F * unit, 11.0F * unit, 10.01F * unit, PING, alpha);
      this.drawMetric(font, unit, alpha, 198.0F, 209.0F, Integer.toString(latency(mc)), "ms", valueSize, suffixSize);
   }

   private void drawMetric(
      MsdfFont font, float unit, float alpha, float leftLimit, float suffixX, String value, String suffix, float valueSize, float suffixSize
   ) {
      float valueRight = this.x + suffixX * unit - 2.0F * unit;
      float available = Math.max(unit, valueRight - (this.x + leftLimit * unit));
      float size = fitTextSize(font, value, valueSize, 7.0F * unit, available);
      drawText(font, valueRight, this.y + 72.5F * unit, size, value, -1, alpha, TextAlign.RIGHT);
      drawText(font, this.x + suffixX * unit, this.y + 74.0F * unit, suffixSize, suffix, HudPalette.accent(), alpha, TextAlign.LEFT);
   }

   private void drawServer(class_310 mc, float unit, float alpha) {
      float panelX = this.x + 4.0F * unit;
      float panelY = this.y + 98.0F * unit;
      MsdfFont font = UiFonts.sfProDisplay();
      float textSize = 11.0F * unit;
      float letterSpacing = textSize * UiFontStyle.MEDIUM.letterSpacingEm();
      String server = serverAddress(mc);
      float textWidth = font.measureWidth(server, textSize, letterSpacing) / unit;
      float panelWidth = Math.min(215.0F, Math.max(105.0F, 34.0F + textWidth + 8.0F));
      float available = (panelWidth - 34.0F - 8.0F) * unit;
      drawGlass(panelX, panelY, panelWidth * unit, 36.0F * unit, 10.0F * unit, unit, alpha);
      drawFigmaTexture(this.x + 18.0F * unit, this.y + 111.0F * unit, 14.0F * unit, 10.0F * unit, SERVER, alpha);
      drawText(
         font, this.x + 38.0F * unit, this.y + 115.5F * unit, textSize, font.ellipsize(server, textSize, letterSpacing, available), -1, alpha, TextAlign.LEFT
      );
   }

   private static void drawGlass(float x, float y, float width, float height, float radius, float unit, float alpha) {
      Render2DUtil.rect(x, y, width, height).color(ColorUtil.multiplyAlpha(HudPalette.PANEL, alpha)).radius(radius).blur(50.0F * unit, alpha).draw();
   }

   private static void drawFigmaTexture(float x, float y, float width, float height, class_2960 texture, float alpha) {
      drawFigmaTexture(x, y, width, height, texture, HudPalette.accent(), alpha);
   }

   private static void drawFigmaTexture(float x, float y, float width, float height, class_2960 texture, int color, float alpha) {
      Render2DUtil.texture(x, y, width, height, texture).color(ColorUtil.multiplyAlpha(color, alpha)).draw();
   }

   private static void drawText(MsdfFont font, float x, float centerY, float size, String text, int color, float alpha, TextAlign align) {
      Render2DUtil.text(x, font.centeredTextY(centerY, size), size, text)
         .style(UiFontStyle.MEDIUM)
         .color(ColorUtil.multiplyAlpha(color, alpha))
         .align(align)
         .draw();
   }

   private static float fitTextSize(MsdfFont font, String text, float preferredSize, float minimumSize, float maxWidth) {
      float preferredSpacing = preferredSize * UiFontStyle.MEDIUM.letterSpacingEm();
      float width = font.measureWidth(text, preferredSize, preferredSpacing);
      return !(width <= maxWidth) && !(width <= 0.0F) ? Math.max(minimumSize, preferredSize * maxWidth / width) : preferredSize;
   }

   private static class_2960 figma(String fileName) {
      return class_2960.method_60654("ryzen:textures/hud/figma/" + fileName);
   }

   private static String serverAddress(class_310 mc) {
      if (!mc.method_1496() && mc.method_1562() != null) {
         class_642 data = mc.method_1558();
         if (data != null && data.field_3761 != null && !data.field_3761.isBlank()) {
            return data.field_3761;
         } else if (mc.method_1562().method_48296().method_10755() instanceof InetSocketAddress inet) {
            return inet.getPort() == 25565 ? inet.getHostString() : inet.getHostString() + ":" + inet.getPort();
         } else {
            return "Multiplayer";
         }
      } else {
         return "Singleplayer";
      }
   }

   private static int latency(class_310 mc) {
      if (mc.method_1562() != null && mc.field_1724 != null) {
         class_640 info = mc.method_1562().method_2871(mc.field_1724.method_5667());
         return info != null ? Math.max(0, info.method_2959()) : 0;
      } else {
         return 0;
      }
   }
}
