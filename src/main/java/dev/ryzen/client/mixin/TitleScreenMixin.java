package dev.ryzen.client.mixin;

import dev.ryzen.client.gui.AltManagerScreen;
import java.net.URI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11876;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_526;
import org.ryzen.context.RenderContext;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_442.class)
public abstract class TitleScreenMixin extends class_437 {
   private static final class_2960 MAIN_MENU = class_2960.method_60655("ryzen", "textures/gui/main_menu.png");
   private static final URI RYZEN_TELEGRAM_URL = URI.create("https://t.me/RyzenClient1");
   private static final URI RYZEN_DISCORD_URL = URI.create("https://discord.gg/9TSnRcBvvb");
   private static final int DESIGN_WIDTH = 1920;
   private static final int DESIGN_HEIGHT = 1080;
   private static final int TEXTURE_WIDTH = 1920;
   private static final int TEXTURE_HEIGHT = 1080;
   private static final int DARK_PANEL = -1038213602;
   private static final int DARK_CARD_OVERLAY = -1474421218;
   private static final int DARK_ROW = 639507998;
   private static final int DIVIDER = 869915097;
   private static final int WHITE = -1;
   private static final int WHITE_50 = -2130706433;
   private static final int WHITE_45 = 1946157055;
   private static final int WHITE_55 = -1929379841;
   private static final int WHITE_25 = 1090519039;
   private static final String PROFILE_NAME = "fikus";
   private static final int PROFILE_CHROME_WIDTH = 82;
   private static final int PROFILE_DEFAULT_WIDTH = 178;
   private static final int PROFILE_MAX_WIDTH = 240;
   private static final int PROFILE_AVATAR_SIZE = 32;
   private static final int PROFILE_AVATAR_RIGHT_PADDING = 8;
   private static final int PROFILE_TEXT_AVATAR_GAP = 12;
   private static final TitleScreenMixin.HitArea SINGLEPLAYER = new TitleScreenMixin.HitArea(1368, 379, 505, 139);
   private static final TitleScreenMixin.HitArea MULTIPLAYER = new TitleScreenMixin.HitArea(1368, 534, 505, 50);
   private static final TitleScreenMixin.HitArea ACCOUNTS = new TitleScreenMixin.HitArea(1368, 600, 505, 50);
   private static final TitleScreenMixin.HitArea OPTIONS = new TitleScreenMixin.HitArea(1368, 666, 505, 50);
   private static final TitleScreenMixin.HitArea QUIT = new TitleScreenMixin.HitArea(1368, 775, 505, 50);
   private static final TitleScreenMixin.HitArea DISCORD = new TitleScreenMixin.HitArea(1358, 875, 42, 35);
   private static final TitleScreenMixin.HitArea TELEGRAM = new TitleScreenMixin.HitArea(1401, 874, 43, 37);
   private float uiScale;
   private float uiOffsetX;
   private float uiOffsetY;

   private static int accent() {
      return ColorUtil.withAlpha(Theme.getAccent(), 255);
   }

   private static int accent60() {
      return ColorUtil.withAlpha(Theme.getAccent(), 153);
   }

   private static int accentGlow() {
      return ColorUtil.withAlpha(Theme.getAccent(), 31);
   }

   protected TitleScreenMixin(class_2561 title) {
      super(title);
   }

   @Inject(method = "method_25426()V", at = @At("HEAD"), cancellable = true)
   private void ryzen$removeVanillaWidgets(CallbackInfo callback) {
      callback.cancel();
   }

   @Inject(method = "method_25394(Lnet/minecraft/class_332;IIF)V", at = @At("HEAD"), cancellable = true)
   private void ryzen$renderMainMenu(class_332 graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
      double scale = this.coverScale();
      double offsetX = (this.field_22789 - 1920.0 * scale) / 2.0;
      double offsetY = (this.field_22790 - 1080.0 * scale) / 2.0;
      this.uiScale = (float)scale;
      this.uiOffsetX = (float)offsetX;
      this.uiOffsetY = (float)offsetY;
      RenderContext.enter2D(null, graphics, null);
      Render2DUtil.beginFrame();

      try {
         this.drawBackgroundTexture();
         this.drawPanel();
         this.drawDivider();
         this.drawLogo();
         this.drawProfileButton();
         this.drawWelcomeText();
         this.drawSinglePlayerCard();
         this.drawRow(MULTIPLAYER, Textures.Title.MULTIPLAYER, 16.0F, 12.0F, "MultiPlayer");
         this.drawRow(ACCOUNTS, Textures.Title.ACCOUNTS, 15.0F, 13.0F, "Accounts");
         this.drawRow(OPTIONS, Textures.Title.OPTIONS, 14.05F, 14.0F, "Options");
         this.drawQuitButton();
         this.drawSocialIcons();
         this.drawBottomLeftText();
         Render2DUtil.flush();
      } finally {
         RenderContext.exit2D();
      }

      double designX = this.toDesignX(mouseX);
      double designY = this.toDesignY(mouseY);
      if (this.isInteractive(designX, designY)) {
         graphics.method_74037(class_11876.field_62455);
      }

      callback.cancel();
   }

   private void drawBackgroundTexture() {
      TitleScreenMixin.SourceCrop crop = this.sourceCrop();
      float u0 = crop.x / 1920.0F;
      float v0 = crop.y / 1080.0F;
      float u1 = (crop.x + crop.width) / 1920.0F;
      float v1 = (crop.y + crop.height) / 1080.0F;
      Render2DUtil.texture(0.0F, 0.0F, this.field_22789, this.field_22790, MAIN_MENU).uv(u0, v0, u1, v1).draw();
   }

   private void drawPanel() {
      Render2DUtil.rect(this.sx(1322.0F), this.sy(0.0F), this.sp(598.0F), this.sp(1080.0F)).color(-1038213602).blur(this.sp(90.0F)).draw();
   }

   private void drawDivider() {
      Render2DUtil.rect(this.sx(1321.0F), this.sy(0.0F), this.sp(1.0F), this.sp(1080.0F)).color(869915097).draw();
   }

   private void drawLogo() {
      this.drawIcon(1370.0F, 177.0F, 32.84F, 32.49F, Textures.Title.LOGO, -1);
   }

   private void drawProfileButton() {
      MsdfFont font = this.fontFor(UiFontStyle.MEDIUM);
      float size = this.sp(13.0F);
      float spacing = size * UiFontStyle.MEDIUM.letterSpacingEm();
      float textWidth = font.measureWidth("https://t.me/crackdelay", size, spacing);
      int desiredWidth = 82 + (int)Math.ceil(textWidth / Math.max(this.uiScale, 1.0E-4F));
      int designWidth = Math.min(240, Math.max(178, desiredWidth));
      Render2DUtil.rect(this.sx(1443.0F), this.sy(174.0F), this.sp(designWidth), this.sp(38.0F))
         .color(accent())
         .radius(this.sp(19.0F))
         .shadow(accentGlow(), this.sp(16.15F))
         .draw();
      this.drawIcon(1456.0F, 187.0F, 11.64F, 12.0F, Textures.Title.PROFILE, -1);
      float centerY = this.sy(174.0F) + this.sp(38.0F) / 2.0F;
      float textY = font.centeredTextY(centerY, size);
      float textX = this.sx(1473.0F);
      float avatarSize = this.sp(32.0F);
      float avatarX = this.sx(1443.0F) + this.sp(designWidth) - this.sp(8.0F) - avatarSize;
      float availableTextWidth = Math.max(0.0F, avatarX - this.sp(12.0F) - textX);
      String shownName = font.ellipsize("https://t.me/crackdelay", size, spacing, availableTextWidth);
      Render2DUtil.pushScissor(textX, this.sy(174.0F), availableTextWidth, this.sp(38.0F));
      Render2DUtil.text(textX, textY, size, shownName).font(font).style(UiFontStyle.MEDIUM).color(-1).draw();
      Render2DUtil.popScissor();
      Render2DUtil.texture(avatarX, this.sy(177.0F), avatarSize, avatarSize, Textures.Title.AVATAR).radius(avatarSize / 2.0F).draw();
   }

   private void drawWelcomeText() {
      MsdfFont font = this.fontFor(UiFontStyle.SEMIBOLD);
      float size = this.sp(27.0F);
      float spacing = size * UiFontStyle.SEMIBOLD.letterSpacingEm();
      float baseY = this.sy(264.0F) - font.ascender(size);
      String first = "C возвращением, ";
      String second = "https://t.me/crackdelay!";
      float firstWidth = font.measureWidth(first, size, spacing);
      Render2DUtil.text(this.sx(1370.0F), baseY, size, first).font(font).color(-1).draw();
      Render2DUtil.text(this.sx(1370.0F) + firstWidth, baseY, size, second).font(font).color(accent()).draw();
      MsdfFont subFont = this.fontFor(UiFontStyle.REGULAR);
      float subSize = this.sp(15.0F);
      float subSpacing = subSize * UiFontStyle.REGULAR.letterSpacingEm();
      float subY = this.sy(322.0F) - subFont.ascender(subSize);
      String s1 = "Добро пожаловать в ";
      String s2 = "Ryzen";
      String s3 = "! Выберите режим и начните своё приключение.";
      float s1w = subFont.measureWidth(s1, subSize, subSpacing);
      float s2w = subFont.measureWidth(s2, subSize, subSpacing);
      Render2DUtil.text(this.sx(1370.0F), subY, subSize, s1).font(subFont).color(1946157055).draw();
      Render2DUtil.text(this.sx(1370.0F) + s1w, subY, subSize, s2).font(subFont).color(accent60()).draw();
      Render2DUtil.text(this.sx(1370.0F) + s1w + s2w, subY, subSize, s3).font(subFont).color(1946157055).draw();
   }

   private void drawSinglePlayerCard() {
      float x = this.sx(1368.0F);
      float y = this.sy(379.0F);
      float w = this.sp(505.0F);
      float h = this.sp(139.0F);
      float r = this.sp(15.0F);
      Render2DUtil.texture(x, y, w, h, Textures.Title.SINGLEPLAYER_BG).uv(0.0F, 0.256F, 1.0F, 0.745F).radius(r).draw();
      Render2DUtil.rect(x, y, w, h).color(-1474421218).radius(r).draw();
      Render2DUtil.rect(x, y, w, h).color(0).radius(r).border(this.sp(0.5F), 1090519039).draw();
      this.drawIconTextGroup(SINGLEPLAYER, Textures.Title.SINGLEPLAYER, 21.02F, 19.0F, "SinglePlayer", 20.0F, -1, -2130706433, UiFontStyle.MEDIUM, 22.0F, true);
   }

   private void drawRow(TitleScreenMixin.HitArea row, class_2960 iconTex, float iconW, float iconH, String label) {
      Render2DUtil.rect(this.sx(row.x), this.sy(row.y), this.sp(row.width), this.sp(row.height))
         .color(639507998)
         .radius(this.sp(15.0F))
         .border(this.sp(0.5F), 1090519039)
         .draw();
      this.drawIconTextGroup(row, iconTex, iconW, iconH, label, 16.0F, -2130706433, -2130706433, UiFontStyle.REGULAR, 12.0F, true);
   }

   private void drawQuitButton() {
      Render2DUtil.rect(this.sx(1368.0F), this.sy(775.0F), this.sp(505.0F), this.sp(50.0F)).color(accent()).radius(this.sp(15.0F)).draw();
      this.drawIconTextGroup(QUIT, Textures.Title.QUIT, 14.0F, 14.0F, "Quit", 16.0F, -1, -1, UiFontStyle.MEDIUM, 12.0F, false);
   }

   private void drawIconTextGroup(
      TitleScreenMixin.HitArea area,
      class_2960 iconTex,
      float iconW,
      float iconH,
      String label,
      float fontSize,
      int textColor,
      int iconColor,
      UiFontStyle style,
      float gap,
      boolean chevron
   ) {
      MsdfFont font = this.fontFor(style);
      float screenIconW = this.sp(iconW);
      float screenIconH = this.sp(iconH);
      float screenGap = this.sp(gap);
      float screenFontSize = this.sp(fontSize);
      float spacing = screenFontSize * style.letterSpacingEm();
      float textWidth = font.measureWidth(label, screenFontSize, spacing);
      float groupWidth = screenIconW + screenGap + textWidth;
      float groupStartX = this.sx(area.x) + (this.sp(area.width) - groupWidth) / 2.0F;
      float centerY = this.sy(area.y) + this.sp(area.height) / 2.0F;
      float iconY = centerY - screenIconH / 2.0F;
      Render2DUtil.texture(groupStartX, iconY, screenIconW, screenIconH, iconTex).color(iconColor).draw();
      float textY = font.centeredTextY(centerY, screenFontSize);
      float textX = groupStartX + screenIconW + screenGap;
      Render2DUtil.text(textX, textY, screenFontSize, label).font(font).color(textColor).draw();
      if (chevron) {
         float chevH = this.sp(8.01F);
         float chevW = this.sp(5.0F);
         float chevX = this.sx(area.x + area.width) - this.sp(25.0F) - chevW;
         float chevY = centerY - chevH / 2.0F;
         Render2DUtil.texture(chevX, chevY, chevW, chevH, Textures.Title.CHEVRON).color(iconColor).draw();
      }
   }

   private void drawSocialIcons() {
      this.drawSocialIcon(DISCORD, Textures.Title.DISCORD, 21.74F, 16.09F);
      this.drawSocialIcon(TELEGRAM, Textures.Title.TELEGRAM, 22.62F, 21.0F);
   }

   private void drawSocialIcon(TitleScreenMixin.HitArea area, class_2960 tex, float iconW, float iconH) {
      float w = this.sp(iconW);
      float h = this.sp(iconH);
      float x = this.sx(area.x) + (this.sp(area.width) - w) / 2.0F;
      float y = this.sy(area.y) + (this.sp(area.height) - h) / 2.0F;
      Render2DUtil.texture(x, y, w, h, tex).color(-2130706433).draw();
   }

   private void drawBottomLeftText() {
      this.textAtBaseline("Новое путешествие", 77.0F, 898.0F, 27.0F, -1, UiFontStyle.SEMIBOLD);
      this.textAtBaseline("начинается здесь", 234.0F, 949.0F, 27.0F, accent(), UiFontStyle.SEMIBOLD);
      MsdfFont regular = this.fontFor(UiFontStyle.REGULAR);
      float subtitleSize = 15.0F;
      float secondBaseline = 984.0F + regular.lineHeight(this.sp(subtitleSize)) / this.uiScale;
      this.textAtBaseline("Собирайте ресурсы, исследуйте неизведанные земли и", 71.0F, 984.0F, subtitleSize, -1929379841, UiFontStyle.REGULAR);
      this.textAtBaseline("станьте героем собственной истории.", 71.0F, secondBaseline, subtitleSize, -1929379841, UiFontStyle.REGULAR);
   }

   private void textAtBaseline(String str, float designX, float designBaselineY, float fontSize, int color, UiFontStyle style) {
      MsdfFont font = this.fontFor(style);
      float screenSize = this.sp(fontSize);
      float screenX = this.sx(designX);
      float screenY = this.sy(designBaselineY) - font.ascender(screenSize);
      Render2DUtil.text(screenX, screenY, screenSize, str).font(font).style(style).color(color).draw();
   }

   private MsdfFont fontFor(UiFontStyle style) {
      return UiFonts.sfPro(style.weight());
   }

   private float sx(float designX) {
      return this.uiOffsetX + designX * this.uiScale;
   }

   private float sy(float designY) {
      return this.uiOffsetY + designY * this.uiScale;
   }

   private float sp(float designPx) {
      return designPx * this.uiScale;
   }

   private void drawIcon(float designX, float designY, float designW, float designH, class_2960 texture, int color) {
      Render2DUtil.texture(this.sx(designX), this.sy(designY), this.sp(designW), this.sp(designH), texture).color(color).draw();
   }

   @Inject(method = "method_25402(Lnet/minecraft/class_11909;Z)Z", at = @At("HEAD"), cancellable = true)
   private void ryzen$handleMainMenuClick(class_11909 event, boolean doubleClick, CallbackInfoReturnable<Boolean> callback) {
      if (event.method_74245() == 0) {
         double designX = this.toDesignX(event.comp_4798());
         double designY = this.toDesignY(event.comp_4799());
         if (!this.isInteractive(designX, designY)) {
            callback.setReturnValue(false);
         } else {
            class_339.method_62888(this.field_22787.method_1483());
            if (SINGLEPLAYER.contains(designX, designY)) {
               this.field_22787.method_1507(new class_526(this));
            } else if (MULTIPLAYER.contains(designX, designY)) {
               this.field_22787.method_1507(new class_500(this));
            } else if (ACCOUNTS.contains(designX, designY)) {
               this.field_22787.method_1507(new AltManagerScreen(this));
            } else if (OPTIONS.contains(designX, designY)) {
               this.field_22787.method_1507(new class_429(this, this.field_22787.field_1690));
            } else if (QUIT.contains(designX, designY)) {
               this.field_22787.method_1592();
            } else if (TELEGRAM.contains(designX, designY)) {
               class_156.method_668().method_673(RYZEN_TELEGRAM_URL);
            } else if (DISCORD.contains(designX, designY)) {
               class_156.method_668().method_673(RYZEN_DISCORD_URL);
            }

            callback.setReturnValue(true);
         }
      }
   }

   private double toDesignX(double screenX) {
      double scale = this.coverScale();
      double offsetX = (this.field_22789 - 1920.0 * scale) / 2.0;
      return (screenX - offsetX) / scale;
   }

   private double toDesignY(double screenY) {
      double scale = this.coverScale();
      double offsetY = (this.field_22790 - 1080.0 * scale) / 2.0;
      return (screenY - offsetY) / scale;
   }

   private double coverScale() {
      return Math.max(this.field_22789 / 1920.0, this.field_22790 / 1080.0);
   }

   private TitleScreenMixin.SourceCrop sourceCrop() {
      double destinationAspect = (double)this.field_22789 / Math.max(1, this.field_22790);
      double textureAspect = 1.7777777777777777;
      if (destinationAspect > textureAspect) {
         int cropHeight = Math.max(1, (int)Math.round(1920.0 / destinationAspect));
         return new TitleScreenMixin.SourceCrop(0, (1080 - cropHeight) / 2, 1920, cropHeight);
      } else {
         int cropWidth = Math.max(1, (int)Math.round(1080.0 * destinationAspect));
         return new TitleScreenMixin.SourceCrop((1920 - cropWidth) / 2, 0, cropWidth, 1080);
      }
   }

   private boolean isInteractive(double x, double y) {
      return SINGLEPLAYER.contains(x, y)
         || MULTIPLAYER.contains(x, y)
         || ACCOUNTS.contains(x, y)
         || OPTIONS.contains(x, y)
         || QUIT.contains(x, y)
         || DISCORD.contains(x, y)
         || TELEGRAM.contains(x, y);
   }

   @Environment(EnvType.CLIENT)
   private record HitArea(int x, int y, int width, int height) {
      private boolean contains(double pointX, double pointY) {
         return pointX >= this.x && pointX < this.x + this.width && pointY >= this.y && pointY < this.y + this.height;
      }
   }

   @Environment(EnvType.CLIENT)
   private record SourceCrop(int x, int y, int width, int height) {
   }
}
