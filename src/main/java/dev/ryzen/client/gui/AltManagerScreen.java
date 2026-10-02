package dev.ryzen.client.gui;

import dev.ryzen.client.account.AccountStore;
import dev.ryzen.client.account.AccountSwitcher;
import dev.ryzen.client.account.AltAccount;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_11876;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_437;
import org.ryzen.context.RenderContext;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class AltManagerScreen extends class_437 {
   private static final int DESIGN_WIDTH = 1920;
   private static final int DESIGN_HEIGHT = 1080;
   private static final int ACCENT = -33504;
   private static final int ACCENT_HOVER = -28354;
   private static final int DARK = -14803426;
   private static final int WHITE = -1;
   private static final int ORANGE_GLOW = ColorUtil.withAlpha(-33504, 50);
   private static final float GLASS_FILL = 1.0F;
   private static final float GLASS_TOP_UP = 0.7F;
   private static final int GRID_X = 423;
   private static final int GRID_Y = 519;
   private static final int GRID_RIGHT = 1497;
   private static final int GRID_BOTTOM = 835;
   private static final int CARD_WIDTH = 257;
   private static final int CARD_HEIGHT = 96;
   private static final int CARD_X_STEP = 272;
   private static final int CARD_Y_STEP = 110;
   private static final int VISIBLE_CARDS = 12;
   private static final int SCROLLBAR_X = 1510;
   private static final int SCROLLBAR_WIDTH = 6;
   private static final int SCROLLBAR_HEIGHT = 316;
   private static final DateTimeFormatter CREATED_DATE = DateTimeFormatter.ofPattern("dd.MM.yy");
   private static final class_2960 BACKGROUND = class_2960.method_60654("ryzen:textures/gui/alt_manager/background.png");
   private static final class_2960 STEVE_BODY = class_2960.method_60654("ryzen:textures/gui/alt_manager/steve_body.png");
   private static final class_2960 STEVE_AVATAR = class_2960.method_60654("ryzen:textures/gui/alt_manager/steve_avatar.png");
   private static final AltManagerScreen.Rect BACK_BUTTON = new AltManagerScreen.Rect(31, 26, 156, 43);
   private static final AltManagerScreen.Rect DELETE_BUTTON = new AltManagerScreen.Rect(472, 437, 190, 36);
   private static final AltManagerScreen.Rect LARGE_STAR = new AltManagerScreen.Rect(674, 434, 42, 42);
   private static final AltManagerScreen.Rect SEARCH_FIELD = new AltManagerScreen.Rect(423, 849, 566, 64);
   private static final AltManagerScreen.Rect NICKNAME_FIELD = new AltManagerScreen.Rect(999, 849, 422, 64);
   private static final AltManagerScreen.Rect ADD_BUTTON = new AltManagerScreen.Rect(1433, 849, 63, 64);
   private static final AltManagerScreen.Rect RANDOM_BUTTON = new AltManagerScreen.Rect(1364, 861, 40, 40);
   private static final AltManagerScreen.Rect GRID_VIEWPORT = new AltManagerScreen.Rect(423, 519, 1074, 316);
   private static final AltManagerScreen.Rect SCROLLBAR_TRACK = new AltManagerScreen.Rect(1510, 519, 6, 316);
   private static final String[] NICK_PREFIXES = new String[]{
      "Shadow",
      "Frost",
      "Void",
      "Pixel",
      "Aqua",
      "Night",
      "Storm",
      "Ember",
      "Ghost",
      "Nova",
      "Cyber",
      "Lunar",
      "Rapid",
      "Toxic",
      "Magma",
      "Blaze",
      "Astro",
      "Neon",
      "Grim",
      "Hyper",
      "Iron",
      "Zero",
      "Drako",
      "Mystic",
      "Silent",
      "Wicked",
      "Prime",
      "Retro",
      "Sour",
      "Vex"
   };
   private static final String[] NICK_SUFFIXES = new String[]{
      "Byte",
      "Craft",
      "Rush",
      "Fox",
      "Ryzen",
      "Wing",
      "Strike",
      "Core",
      "Drift",
      "Zap",
      "Wolf",
      "Hawk",
      "Reign",
      "Spark",
      "Flux",
      "Dash",
      "Rage",
      "Snipe",
      "King",
      "Lord",
      "Punch",
      "Shot",
      "Fang",
      "Peak",
      "Vibe",
      "Loop",
      "Crypt",
      "Gaze",
      "Husk",
      "Riot"
   };
   private final class_437 parent;
   private final Map<UUID, Float> cardHover = new HashMap<>();
   private final Map<String, Float> buttonHover = new HashMap<>();
   private AccountStore store;
   private MsdfFont font;
   private String searchText = "";
   private String nicknameText = "";
   private boolean searchFocused;
   private boolean nicknameFocused;
   private long openedAtNanos = System.nanoTime();
   private long lastFrameNanos = this.openedAtNanos;
   private float scroll;
   private float targetScroll;
   private float selectedPulse = 1.0F;
   private UUID renderedSelectedId;
   private boolean draggingScrollbar;
   private float scrollbarGrabOffset;
   private float lastDesignMouseX;
   private float lastDesignMouseY;
   private float uiScale;
   private float uiOffsetX;
   private float uiOffsetY;

   public AltManagerScreen(class_437 parent) {
      super(class_2561.method_43470("Ryzen Alt Manager"));
      this.parent = parent;
   }

   protected void method_25426() {
      if (this.store == null) {
         this.store = AccountStore.load(this.field_22787.method_1548().method_1676());
         this.renderedSelectedId = this.store.selectedId();
      }

      this.font = UiFonts.sfProDisplay();
      this.method_25395(null);
   }

   protected void method_56131() {
   }

   public void method_25419() {
      this.field_22787.method_1507(this.parent);
   }

   public void method_25394(class_332 graphics, int mouseX, int mouseY, float partialTick) {
      long now = System.nanoTime();
      float delta = Math.min(0.05F, Math.max(0.0F, (float)(now - this.lastFrameNanos) / 1.0E9F));
      this.lastFrameNanos = now;
      this.updateTransform();
      this.lastDesignMouseX = this.toDesignX(mouseX);
      this.lastDesignMouseY = this.toDesignY(mouseY);
      this.updateAnimations(delta);
      float entrance = this.entranceProgress(now);
      this.drawBackground(graphics);
      RenderContext.enter2D(null, graphics, null);
      Render2DUtil.beginFrame();

      try {
         this.drawBackButton(entrance);
         this.drawBreadcrumb(entrance);
         this.drawHero(entrance, now);
         this.drawGrid(entrance);
         this.drawBottomControls(now, entrance);
         Render2DUtil.flush();
      } finally {
         RenderContext.exit2D();
      }

      this.requestCursor(graphics, this.lastDesignMouseX, this.lastDesignMouseY);
   }

   private void updateTransform() {
      this.uiScale = Math.min(this.field_22789 / 1920.0F, this.field_22790 / 1080.0F);
      this.uiOffsetX = (this.field_22789 - 1920.0F * this.uiScale) * 0.5F;
      this.uiOffsetY = (this.field_22790 - 1080.0F * this.uiScale) * 0.5F;
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

   private float toDesignX(double screenX) {
      return (float)((screenX - this.uiOffsetX) / this.uiScale);
   }

   private float toDesignY(double screenY) {
      return (float)((screenY - this.uiOffsetY) / this.uiScale);
   }

   private int alpha(int color, float factor) {
      return ColorUtil.multiplyAlpha(color, factor);
   }

   private void drawGlass(float x, float y, float width, float height, float radius, float blurRadius, float entrance) {
      Render2DUtil.rect(x, y, width, height).color(this.alpha(-14803426, 1.0F * entrance)).radius(radius).blur(blurRadius).draw();
      Render2DUtil.rect(x, y, width, height).color(this.alpha(-14803426, 0.7F * entrance)).radius(radius).draw();
   }

   private int lerpColor(int from, int to, float delta) {
      return ColorUtil.lerp(from, to, delta);
   }

   private float buttonHover(String key) {
      return this.buttonHover.getOrDefault(key, 0.0F);
   }

   private float centeredTop(float designCenterY, float designSize) {
      return this.font.centeredTextY(this.sy(designCenterY), this.sp(designSize));
   }

   private void drawBackground(class_332 graphics) {
      double destAspect = (double)this.field_22789 / Math.max(1, this.field_22790);
      double texAspect = 1.7777777777777777;
      int cropX;
      int cropY;
      int cropW;
      int cropH;
      if (destAspect > texAspect) {
         cropH = Math.max(1, (int)Math.round(1920.0 / destAspect));
         cropX = 0;
         cropY = (1080 - cropH) / 2;
         cropW = 1920;
      } else {
         cropW = Math.max(1, (int)Math.round(1080.0 * destAspect));
         cropX = (1920 - cropW) / 2;
         cropY = 0;
         cropH = 1080;
      }

      graphics.method_25302(class_10799.field_56883, BACKGROUND, 0, 0, cropX, cropY, this.field_22789, this.field_22790, cropW, cropH, 1920, 1080);
   }

   private void drawBackButton(float entrance) {
      float hover = this.buttonHover("back");
      int bg = this.lerpColor(-33504, -28354, hover);
      Render2DUtil.rect(this.sx(31.0F), this.sy(26.0F), this.sp(156.0F), this.sp(43.0F))
         .color(this.alpha(bg, entrance))
         .radius(this.sp(22.0F))
         .shadow(this.alpha(ORANGE_GLOW, entrance * hover), this.sp(16.0F))
         .draw();
      Render2DUtil.texture(this.sx(53.0F), this.sy(41.0F), this.sp(18.0F), this.sp(13.0F), Textures.AltManager.BACK).color(this.alpha(-1, entrance)).draw();
      float textY = this.centeredTop(47.5F, 16.0F);
      Render2DUtil.text(this.sx(82.0F), textY, this.sp(16.0F), "Вернуться").style(UiFontStyle.MEDIUM).color(this.alpha(-1, entrance)).draw();
   }

   private void drawBreadcrumb(float entrance) {
      float size = this.sp(19.0F);
      float spacing = size * UiFontStyle.MEDIUM.letterSpacingEm();
      float topY = this.sy(133.0F);
      String first = "Главное меню / ";
      float firstWidth = this.font.measureWidth(first, size, spacing);
      Render2DUtil.text(this.sx(423.0F), topY, size, first).style(UiFontStyle.MEDIUM).color(this.alpha(-1, entrance)).draw();
      Render2DUtil.text(this.sx(423.0F) + firstWidth, topY, size, "Аккаунт менеджер").style(UiFontStyle.MEDIUM).color(this.alpha(-33504, entrance)).draw();
   }

   private void drawHero(float entrance, long now) {
      this.drawGlass(this.sx(423.0F), this.sy(179.0F), this.sp(1074.0F), this.sp(326.0F), this.sp(30.0F), this.sp(50.0F), entrance);
      Render2DUtil.texture(this.sx(952.0F), this.sy(179.0F), this.sp(545.0F), this.sp(326.0F), Textures.AltManager.ACCENT_SHAPE)
         .color(this.alpha(-1, entrance))
         .draw();
      Render2DUtil.pushScissor(this.sx(423.0F), this.sy(113.0F), this.sp(1074.0F), this.sp(392.0F));
      Render2DUtil.texture(this.sx(1075.0F), this.sy(67.0F), this.sp(375.3542F), this.sp(750.7083F), STEVE_BODY).color(this.alpha(-1, entrance)).draw();
      Render2DUtil.popScissor();
      AltAccount selected = this.store.selected().orElse(null);
      if (selected != null) {
         Render2DUtil.texture(this.sx(472.0F), this.sy(211.0F), this.sp(26.0F), this.sp(19.0F), Textures.AltManager.LOGO)
            .color(this.alpha(-1, entrance))
            .draw();
         float brandY = this.sy(216.0F);
         Render2DUtil.text(this.sx(512.0F), brandY, this.sp(16.0F), "Ryzen").style(UiFontStyle.REGULAR).color(this.alpha(-33504, entrance)).draw();
         Render2DUtil.texture(this.sx(472.0F), this.sy(317.0F), this.sp(21.0F), this.sp(22.0F), Textures.AltManager.PROFILE)
            .color(this.alpha(-1, entrance))
            .draw();
         String nickname = selected.nickname();
         float nickSize = this.sp(32.0F);
         float nickSpacing = nickSize * UiFontStyle.MEDIUM.letterSpacingEm();
         String nickShown = this.font.ellipsize(nickname, nickSize, nickSpacing, this.sp(420.0F));
         float nickY = this.sy(309.0F);
         Render2DUtil.text(this.sx(508.0F), nickY, nickSize, nickShown).style(UiFontStyle.MEDIUM).color(this.alpha(-1, entrance)).draw();
         Render2DUtil.texture(this.sx(473.0F), this.sy(363.0F), this.sp(11.0F), this.sp(12.0F), Textures.AltManager.DATE)
            .color(this.alpha(-1, 0.4F * entrance))
            .draw();
         String created = "Создан: " + CREATED_DATE.format(selected.createdAt().atZone(ZoneId.systemDefault()));
         float createdY = this.sy(360.0F);
         Render2DUtil.text(this.sx(493.0F), createdY, this.sp(15.0F), created).style(UiFontStyle.REGULAR).color(this.alpha(-1, 0.4F * entrance)).draw();
         float deleteHover = this.buttonHover("delete");
         int deleteBg = this.lerpColor(-33504, -28354, deleteHover);
         Render2DUtil.rect(this.sx(472.0F), this.sy(437.0F), this.sp(190.0F), this.sp(36.0F))
            .color(this.alpha(deleteBg, entrance))
            .radius(this.sp(13.0F))
            .shadow(this.alpha(ORANGE_GLOW, entrance * deleteHover), this.sp(14.0F))
            .draw();
         Render2DUtil.texture(this.sx(529.0F), this.sy(450.0F), this.sp(13.0F), this.sp(10.0F), Textures.AltManager.DELETE)
            .color(this.alpha(-1, entrance))
            .draw();
         float deleteTextY = this.centeredTop(455.0F, 14.0F);
         Render2DUtil.text(this.sx(548.0F), deleteTextY, this.sp(14.0F), "Удалить").style(UiFontStyle.REGULAR).color(this.alpha(-1, entrance)).draw();
         float starHover = this.buttonHover("star");
         float starScale = 1.0F + 0.14F * starHover;
         float starEdge = this.sp(20.0F) * starScale;
         float starCx = this.sx(695.0F);
         float starCy = this.sy(455.0F);
         class_2960 starTex = selected.favorite() ? Textures.AltManager.FAVORITE_LARGE : Textures.AltManager.CARD_STAR_OUTLINE;
         float starAlpha = selected.favorite() ? entrance : (0.59F + 0.41F * starHover) * entrance;
         Render2DUtil.texture(starCx - starEdge * 0.5F, starCy - starEdge * 0.5F, starEdge, starEdge, starTex).color(this.alpha(-1, starAlpha)).draw();
      }
   }

   private void drawGrid(float entrance) {
      List<AltAccount> accounts = this.filteredAccounts();
      Render2DUtil.pushScissor(this.sx(423.0F), this.sy(519.0F), this.sp(1074.0F), this.sp(316.0F));

      for (int index = 0; index < accounts.size(); index++) {
         AltAccount account = accounts.get(index);
         int column = index % 4;
         int row = index / 4;
         float cardX = 423 + column * 272;
         float baseY = 519 + row * 110 - this.scroll;
         if (!(baseY + 96.0F < 511.0F) && !(baseY > 843.0F)) {
            float hover = this.cardHover.getOrDefault(account.id(), 0.0F);
            float delay = Math.min(0.34F, index * 0.018F);
            float cardEntrance = easeOutCubic(clamp01((entrance - delay) / Math.max(0.01F, 1.0F - delay)));
            float cardY = baseY + (1.0F - cardEntrance) * 20.0F - hover * 3.0F;
            this.drawCard(account, cardX, cardY, hover, cardEntrance);
         }
      }

      Render2DUtil.popScissor();
      if (accounts.size() > 12) {
         this.drawScrollbar(entrance);
      }

      if (accounts.isEmpty()) {
         float size = this.sp(19.0F);
         float topY = this.centeredTop(677.0F, 19.0F);
         Render2DUtil.text(this.sx(960.0F), topY, size, "Аккаунты не найдены")
            .style(UiFontStyle.MEDIUM)
            .color(this.alpha(-1, 0.5F * entrance))
            .align(TextAlign.CENTER)
            .draw();
      }
   }

   private void drawCard(AltAccount account, float designX, float designY, float hover, float entrance) {
      boolean selected = account.id().equals(this.store.selectedId());
      float x = this.sx(designX);
      float y = this.sy(designY);
      float w = this.sp(257.0F);
      float h = this.sp(96.0F);
      if (selected && this.selectedPulse < 1.0F) {
         float glow = (1.0F - this.selectedPulse) * 0.55F * entrance;
         Render2DUtil.rect(x - this.sp(3.0F), y - this.sp(3.0F), w + this.sp(6.0F), h + this.sp(6.0F))
            .color(this.alpha(-33504, glow))
            .radius(this.sp(23.0F))
            .draw();
      }

      if (selected) {
         Render2DUtil.rect(x, y, w, h).color(this.alpha(-33504, 0.9F * entrance)).radius(this.sp(20.0F)).draw();
      } else {
         this.drawGlass(x, y, w, h, this.sp(20.0F), this.sp(28.0F), entrance);
      }

      if (!selected && hover > 0.01F) {
         Render2DUtil.rect(x, y, w, h).color(this.alpha(-1, 0.07F * hover * entrance)).radius(this.sp(20.0F)).draw();
      }

      Render2DUtil.texture(this.sx(designX + 17.0F), this.sy(designY + 14.0F), this.sp(68.0F), this.sp(68.0F), STEVE_AVATAR)
         .color(this.alpha(-1, entrance))
         .radius(this.sp(20.0F))
         .draw();
      float nickSize = this.sp(17.0F);
      float nickSpacing = nickSize * UiFontStyle.REGULAR.letterSpacingEm();
      String nickShown = this.font.ellipsize(account.nickname(), nickSize, nickSpacing, this.sp(116.0F));
      float nickY = this.sy(designY + 22.0F);
      Render2DUtil.text(this.sx(designX + 100.0F), nickY, nickSize, nickShown).style(UiFontStyle.REGULAR).color(this.alpha(-1, entrance)).draw();
      class_2960 starTex;
      float starAlpha;
      if (account.favorite()) {
         starTex = selected ? Textures.AltManager.CARD_STAR_WHITE : Textures.AltManager.CARD_STAR_ORANGE;
         starAlpha = entrance;
      } else {
         starTex = Textures.AltManager.CARD_STAR_OUTLINE;
         starAlpha = (selected ? 0.85F : 0.59F) * entrance;
      }

      Render2DUtil.texture(this.sx(designX + 226.0F), this.sy(designY + 14.0F), this.sp(15.0F), this.sp(15.0F), starTex)
         .color(this.alpha(-1, starAlpha))
         .draw();
      if (selected) {
         Render2DUtil.rect(this.sx(designX + 100.0F), this.sy(designY + 51.0F), this.sp(103.0F), this.sp(26.0F))
            .color(this.alpha(-1, entrance))
            .radius(this.sp(13.0F))
            .draw();
         Render2DUtil.texture(this.sx(designX + 107.0F), this.sy(designY + 55.0F), this.sp(17.0F), this.sp(17.0F), Textures.AltManager.SELECTED_CHECK)
            .color(this.alpha(-1, entrance))
            .draw();
         float pillY = this.centeredTop(designY + 51.0F + 13.0F, 12.0F);
         Render2DUtil.text(this.sx(designX + 134.0F), pillY, this.sp(12.0F), "Выбрано").style(UiFontStyle.REGULAR).color(this.alpha(-33504, entrance)).draw();
      } else {
         int pillBg = this.lerpColor(-33504, -28354, hover);
         Render2DUtil.rect(this.sx(designX + 100.0F), this.sy(designY + 51.0F), this.sp(90.0F), this.sp(26.0F))
            .color(this.alpha(pillBg, entrance))
            .radius(this.sp(12.0F))
            .shadow(this.alpha(ORANGE_GLOW, entrance * hover), this.sp(12.0F))
            .draw();
         Render2DUtil.texture(this.sx(designX + 109.0F), this.sy(designY + 59.0F), this.sp(9.0F), this.sp(9.0F), Textures.AltManager.SELECT)
            .color(this.alpha(-1, entrance))
            .draw();
         float pillY = this.centeredTop(designY + 51.0F + 13.0F, 12.0F);
         Render2DUtil.text(this.sx(designX + 127.0F), pillY, this.sp(12.0F), "Выбрать").style(UiFontStyle.REGULAR).color(this.alpha(-1, entrance)).draw();
      }
   }

   private void drawScrollbar(float entrance) {
      float maximum = this.maxScroll();
      float contentH = contentHeight(this.filteredAccounts().size());
      float thumbHeight = Math.max(42.0F, 316.0F * (316.0F / Math.max(1.0F, contentH)));
      float travel = 316.0F - thumbHeight;
      float thumbY = 519.0F + (maximum <= 0.0F ? 0.0F : this.scroll / maximum * travel);
      Render2DUtil.rect(this.sx(1510.0F), this.sy(519.0F), this.sp(6.0F), this.sp(316.0F))
         .color(this.alpha(-14803426, 0.6F * entrance))
         .radius(this.sp(3.0F))
         .draw();
      Render2DUtil.rect(this.sx(1510.0F), this.sy(thumbY), this.sp(6.0F), this.sp(thumbHeight))
         .color(this.alpha(-33504, entrance))
         .radius(this.sp(3.0F))
         .draw();
   }

   private void drawBottomControls(long now, float entrance) {
      this.drawField(
         SEARCH_FIELD,
         this.searchFocused,
         this.searchText,
         "Поиск..",
         Textures.AltManager.SEARCH,
         446.0F,
         872.0F,
         18.0F,
         18.0F,
         0.3F,
         482.0F,
         450.0F,
         entrance,
         now
      );
      this.drawField(
         NICKNAME_FIELD,
         this.nicknameFocused,
         this.nicknameText,
         "Ваш никнейм..",
         Textures.AltManager.USER_INPUT,
         1020.0F,
         872.0F,
         18.0F,
         18.0F,
         0.4F,
         1054.0F,
         294.0F,
         entrance,
         now
      );
      float addHover = this.buttonHover("add");
      float addScale = 1.0F + 0.06F * addHover;
      float addW = this.sp(63.0F) * addScale;
      float addH = this.sp(64.0F) * addScale;
      float addCx = this.sx(1433.0F) + this.sp(63.0F) * 0.5F;
      float addCy = this.sy(849.0F) + this.sp(64.0F) * 0.5F;
      int addBg = this.lerpColor(-33504, -28354, addHover);
      Render2DUtil.rect(addCx - addW * 0.5F, addCy - addH * 0.5F, addW, addH)
         .color(this.alpha(addBg, entrance))
         .radius(this.sp(20.0F))
         .shadow(this.alpha(ORANGE_GLOW, entrance * addHover), this.sp(16.0F))
         .draw();
      Render2DUtil.texture(addCx - this.sp(10.0F), addCy - this.sp(10.0F), this.sp(20.0F), this.sp(20.0F), Textures.AltManager.ADD)
         .color(this.alpha(-1, entrance))
         .draw();
      float randomHover = this.buttonHover("random");
      float randomScale = 1.0F + 0.12F * randomHover;
      float randomEdge = this.sp(18.0F) * randomScale;
      float randomCx = this.sx(1384.0F);
      float randomCy = this.sy(881.0F);
      Render2DUtil.texture(randomCx - randomEdge * 0.5F, randomCy - randomEdge * 0.5F, randomEdge, randomEdge, Textures.AltManager.RANDOM)
         .color(this.alpha(-33504, entrance))
         .draw();
   }

   private void drawField(
      AltManagerScreen.Rect field,
      boolean focused,
      String value,
      String placeholder,
      class_2960 icon,
      float iconX,
      float iconY,
      float iconW,
      float iconH,
      float iconAlpha,
      float textX,
      float textMaxWidth,
      float entrance,
      long now
   ) {
      if (focused) {
         Render2DUtil.rect(
               this.sx(field.x) - this.sp(1.5F), this.sy(field.y) - this.sp(1.5F), this.sp(field.width) + this.sp(3.0F), this.sp(field.height) + this.sp(3.0F)
            )
            .color(this.alpha(-33504, 0.55F * entrance))
            .radius(this.sp(21.0F))
            .draw();
      }

      this.drawGlass(this.sx(field.x), this.sy(field.y), this.sp(field.width), this.sp(field.height), this.sp(20.0F), this.sp(28.0F), entrance);
      Render2DUtil.texture(this.sx(iconX), this.sy(iconY), this.sp(iconW), this.sp(iconH), icon).color(this.alpha(-1, iconAlpha * entrance)).draw();
      float size = this.sp(16.0F);
      float spacing = size * UiFontStyle.REGULAR.letterSpacingEm();
      float centerY = this.sy(field.y) + this.sp(field.height) * 0.5F;
      float topY = this.font.centeredTextY(centerY, size);
      if (value.isEmpty()) {
         Render2DUtil.text(this.sx(textX), topY, size, placeholder).style(UiFontStyle.REGULAR).color(this.alpha(-1, 0.4F * entrance)).draw();
         if (focused && now / 450000000L % 2L == 0L) {
            Render2DUtil.rect(this.sx(textX) + this.sp(2.0F), topY + this.sp(3.0F), this.sp(2.0F), this.sp(17.0F)).color(this.alpha(-33504, entrance)).draw();
         }
      } else {
         float textWidth = this.font.measureWidth(value, size, spacing);
         float maxW = this.sp(textMaxWidth);
         float drawX = this.sx(textX);
         float overflow = textWidth - maxW;
         if (overflow > 0.0F) {
            drawX -= overflow;
         }

         Render2DUtil.pushScissor(this.sx(textX) - this.sp(2.0F), this.sy(field.y), maxW + this.sp(8.0F), this.sp(field.height));
         Render2DUtil.text(drawX, topY, size, value).style(UiFontStyle.REGULAR).color(this.alpha(-1, entrance)).draw();
         if (focused && now / 450000000L % 2L == 0L) {
            float caretX = drawX + textWidth + this.sp(3.0F);
            Render2DUtil.rect(caretX, topY + this.sp(3.0F), this.sp(2.0F), this.sp(17.0F)).color(this.alpha(-33504, entrance)).draw();
         }

         Render2DUtil.popScissor();
      }
   }

   private void updateAnimations(float deltaSeconds) {
      float scrollFactor = 1.0F - (float)Math.exp(-14.0F * deltaSeconds);
      this.targetScroll = clamp(this.targetScroll, 0.0F, this.maxScroll());
      if (!this.draggingScrollbar) {
         this.scroll = this.scroll + (this.targetScroll - this.scroll) * scrollFactor;
      }

      if (this.renderedSelectedId == null || !this.renderedSelectedId.equals(this.store.selectedId())) {
         this.renderedSelectedId = this.store.selectedId();
         this.selectedPulse = 0.0F;
      }

      this.selectedPulse = Math.min(1.0F, this.selectedPulse + deltaSeconds * 4.6F);
      List<AltAccount> accounts = this.filteredAccounts();
      float hoverFactor = 1.0F - (float)Math.exp(-18.0F * deltaSeconds);

      for (int index = 0; index < accounts.size(); index++) {
         AltAccount account = accounts.get(index);
         int column = index % 4;
         int row = index / 4;
         AltManagerScreen.Rect rect = new AltManagerScreen.Rect(423 + column * 272, Math.round(519 + row * 110 - this.scroll), 257, 96);
         float target = GRID_VIEWPORT.contains(this.lastDesignMouseX, this.lastDesignMouseY) && rect.contains(this.lastDesignMouseX, this.lastDesignMouseY)
            ? 1.0F
            : 0.0F;
         float current = this.cardHover.getOrDefault(account.id(), 0.0F);
         this.cardHover.put(account.id(), current + (target - current) * hoverFactor);
      }

      boolean hasSelected = this.store != null && this.store.selected().isPresent();
      float mx = this.lastDesignMouseX;
      float my = this.lastDesignMouseY;
      this.updateButtonHover("back", BACK_BUTTON.contains(mx, my), deltaSeconds);
      this.updateButtonHover("add", ADD_BUTTON.contains(mx, my), deltaSeconds);
      this.updateButtonHover("random", RANDOM_BUTTON.contains(mx, my), deltaSeconds);
      this.updateButtonHover("delete", hasSelected && DELETE_BUTTON.contains(mx, my), deltaSeconds);
      this.updateButtonHover("star", hasSelected && LARGE_STAR.contains(mx, my), deltaSeconds);
   }

   private void updateButtonHover(String key, boolean hovered, float deltaSeconds) {
      float current = this.buttonHover.getOrDefault(key, 0.0F);
      float target = hovered ? 1.0F : 0.0F;
      float factor = 1.0F - (float)Math.exp(-16.0F * deltaSeconds);
      this.buttonHover.put(key, current + (target - current) * factor);
   }

   public boolean method_25404(class_11908 event) {
      if (this.nicknameFocused && event.method_74230()) {
         this.addNickname();
         return true;
      }

      if (this.searchFocused && event.method_74230()) {
         this.searchFocused = false;
         return true;
      }

      if (event.method_74231()) {
         if (!this.searchFocused && !this.nicknameFocused) {
            this.method_25419();
            return true;
         } else {
            this.searchFocused = false;
            this.nicknameFocused = false;
            return true;
         }
      } else {
         int key = event.comp_4795();
         if (key == 259) {
            if (this.searchFocused && !this.searchText.isEmpty()) {
               this.searchText = deleteLastCodePoint(this.searchText);
               this.targetScroll = 0.0F;
               this.scroll = Math.min(this.scroll, this.maxScroll());
               return true;
            }

            if (this.nicknameFocused && !this.nicknameText.isEmpty()) {
               this.nicknameText = deleteLastCodePoint(this.nicknameText);
               return true;
            }
         }

         return super.method_25404(event);
      }
   }

   public boolean method_25400(class_11905 event) {
      if (!this.searchFocused && !this.nicknameFocused) {
         return super.method_25400(event);
      }

      this.method_25415(event.method_74226(), false);
      return true;
   }

   public void method_25415(String text, boolean overwrite) {
      String filtered = text == null ? "" : text.replaceAll("[^A-Za-z0-9_]", "");
      if (!filtered.isEmpty()) {
         if (this.searchFocused) {
            this.searchText = appendFiltered(this.searchText, filtered, 64);
            this.targetScroll = 0.0F;
            this.scroll = Math.min(this.scroll, this.maxScroll());
         } else if (this.nicknameFocused) {
            this.nicknameText = appendFiltered(this.nicknameText, filtered, 16);
         }
      }
   }

   private static String appendFiltered(String base, String addition, int maxLength) {
      String combined = base + addition;
      if (combined.length() > maxLength) {
         combined = combined.substring(0, maxLength);
      }

      return combined;
   }

   private static String deleteLastCodePoint(String value) {
      if (value.isEmpty()) {
         return value;
      }

      int lastCodePoint = value.codePointBefore(value.length());
      return value.substring(0, value.length() - Character.charCount(lastCodePoint));
   }

   private static String randomNickname() {
      ThreadLocalRandom random = ThreadLocalRandom.current();

      for (int attempt = 0; attempt < 20; attempt++) {
         String prefix = NICK_PREFIXES[random.nextInt(NICK_PREFIXES.length)];
         String suffix = NICK_SUFFIXES[random.nextInt(NICK_SUFFIXES.length)];

         String name = switch (random.nextInt(6)) {
            case 0 -> prefix + suffix + random.nextInt(10, 100);
            case 1 -> prefix + "_" + suffix;
            case 2 -> prefix.toLowerCase(Locale.ROOT) + suffix + random.nextInt(100, 1000);
            case 3 -> prefix.toLowerCase(Locale.ROOT) + "_" + suffix.toLowerCase(Locale.ROOT);
            case 4 -> prefix + suffix;
            default -> suffix + prefix + random.nextInt(10, 100);
         };
         if (name.length() <= 16) {
            return name;
         }
      }

      return NICK_PREFIXES[random.nextInt(NICK_PREFIXES.length)] + random.nextInt(1000, 10000);
   }

   public boolean method_25402(class_11909 event, boolean doubleClick) {
      if (event.method_74245() != 0) {
         return super.method_25402(event, doubleClick);
      }

      float x = this.toDesignX(event.comp_4798());
      float y = this.toDesignY(event.comp_4799());
      if (BACK_BUTTON.contains(x, y)) {
         this.clickSound();
         this.method_25419();
         return true;
      }

      if (ADD_BUTTON.contains(x, y)) {
         this.clickSound();
         this.addNickname();
         return true;
      }

      if (RANDOM_BUTTON.contains(x, y)) {
         this.clickSound();
         this.nicknameText = randomNickname();
         this.nicknameFocused = true;
         this.searchFocused = false;
         return true;
      }

      if (DELETE_BUTTON.contains(x, y) && this.store.selected().isPresent()) {
         this.clickSound();
         this.store.delete(this.store.selectedId());
         return true;
      }

      if (LARGE_STAR.contains(x, y) && this.store.selected().isPresent()) {
         this.clickSound();
         this.toggleFavorite(this.store.selectedId());
         return true;
      }

      if (this.filteredAccounts().size() > 12 && SCROLLBAR_TRACK.expanded(10).contains(x, y)) {
         this.clickSound();
         this.beginScrollbarDrag(y);
         return true;
      }

      if (GRID_VIEWPORT.contains(x, y)) {
         List<AltAccount> accounts = this.filteredAccounts();

         for (int index = 0; index < accounts.size(); index++) {
            int column = index % 4;
            int row = index / 4;
            int cardX = 423 + column * 272;
            int cardY = Math.round(519 + row * 110 - this.scroll);
            AltManagerScreen.Rect card = new AltManagerScreen.Rect(cardX, cardY, 257, 96);
            if (card.contains(x, y)) {
               AltAccount account = accounts.get(index);
               this.clickSound();
               if (new AltManagerScreen.Rect(cardX + 214, cardY, 43, 43).contains(x, y)) {
                  this.toggleFavorite(account.id());
               } else {
                  this.store.select(account.id());
                  AccountSwitcher.switchTo(account.nickname());
               }

               this.method_25395(null);
               return true;
            }
         }
      }

      if (SEARCH_FIELD.contains(x, y)) {
         this.clickSound();
         this.searchFocused = true;
         this.nicknameFocused = false;
         return true;
      } else if (NICKNAME_FIELD.contains(x, y) && !RANDOM_BUTTON.contains(x, y)) {
         this.clickSound();
         this.nicknameFocused = true;
         this.searchFocused = false;
         return true;
      } else {
         this.searchFocused = false;
         this.nicknameFocused = false;
         return true;
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      float x = this.toDesignX(mouseX);
      float y = this.toDesignY(mouseY);
      if (GRID_VIEWPORT.expanded(18).contains(x, y) && this.maxScroll() > 0.0F) {
         this.targetScroll = clamp(this.targetScroll - (float)verticalAmount * 110.0F, 0.0F, this.maxScroll());
         return true;
      } else {
         return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   public boolean method_25403(class_11909 event, double dragX, double dragY) {
      if (this.draggingScrollbar && event.method_74245() == 0) {
         this.setScrollFromThumb(this.toDesignY(event.comp_4799()) - this.scrollbarGrabOffset);
         return true;
      } else {
         return super.method_25403(event, dragX, dragY);
      }
   }

   public boolean method_25406(class_11909 event) {
      if (event.method_74245() == 0 && this.draggingScrollbar) {
         this.draggingScrollbar = false;
         return true;
      } else {
         return super.method_25406(event);
      }
   }

   private void beginScrollbarDrag(float mouseY) {
      float maximum = this.maxScroll();
      float contentHeight = contentHeight(this.filteredAccounts().size());
      float thumbHeight = Math.max(42.0F, 316.0F * (316.0F / contentHeight));
      float travel = 316.0F - thumbHeight;
      float thumbY = 519.0F + (maximum <= 0.0F ? 0.0F : this.scroll / maximum * travel);
      if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
         this.scrollbarGrabOffset = mouseY - thumbY;
      } else {
         this.scrollbarGrabOffset = thumbHeight * 0.5F;
         this.setScrollFromThumb(mouseY - this.scrollbarGrabOffset);
      }

      this.draggingScrollbar = true;
   }

   private void setScrollFromThumb(float thumbY) {
      float maximum = this.maxScroll();
      float contentHeight = contentHeight(this.filteredAccounts().size());
      float thumbHeight = Math.max(42.0F, 316.0F * (316.0F / contentHeight));
      float travel = Math.max(1.0F, 316.0F - thumbHeight);
      float ratio = clamp((thumbY - 519.0F) / travel, 0.0F, 1.0F);
      this.scroll = ratio * maximum;
      this.targetScroll = this.scroll;
   }

   private void addNickname() {
      String nickname = this.nicknameText.trim();
      if (AccountStore.isValidNickname(nickname)) {
         this.store.add(nickname);
         this.nicknameText = "";
         this.targetScroll = 0.0F;
      }
   }

   private void toggleFavorite(UUID id) {
      AltAccount before = this.store.find(id).orElse(null);
      if (before != null && this.store.toggleFavorite(id)) {
         this.targetScroll = 0.0F;
      }
   }

   private List<AltAccount> filteredAccounts() {
      if (this.store == null) {
         return List.of();
      }

      String query = this.searchText.trim().toLowerCase(Locale.ROOT);
      return query.isEmpty()
         ? this.store.accountsForUi()
         : this.store.accountsForUi().stream().filter(account -> account.nickname().toLowerCase(Locale.ROOT).contains(query)).toList();
   }

   private float maxScroll() {
      return Math.max(0.0F, contentHeight(this.filteredAccounts().size()) - 316.0F);
   }

   private static float contentHeight(int accountCount) {
      int rows = Math.max(0, (accountCount + 3) / 4);
      return rows == 0 ? 0.0F : rows * 96 + (rows - 1) * 14;
   }

   private void requestCursor(class_332 graphics, float x, float y) {
      if (!SEARCH_FIELD.contains(x, y) && (!NICKNAME_FIELD.contains(x, y) || RANDOM_BUTTON.contains(x, y) || ADD_BUTTON.contains(x, y))) {
         if (BACK_BUTTON.contains(x, y)
            || this.store.selected().isPresent() && DELETE_BUTTON.contains(x, y)
            || this.store.selected().isPresent() && LARGE_STAR.contains(x, y)
            || ADD_BUTTON.contains(x, y)
            || RANDOM_BUTTON.contains(x, y)
            || GRID_VIEWPORT.contains(x, y)
            || this.filteredAccounts().size() > 12 && SCROLLBAR_TRACK.expanded(10).contains(x, y)) {
            graphics.method_74037(class_11876.field_62455);
         }
      } else {
         graphics.method_74037(class_11876.field_62453);
      }
   }

   private void clickSound() {
      class_339.method_62888(this.field_22787.method_1483());
   }

   private float entranceProgress(long now) {
      float elapsed = (float)(now - this.openedAtNanos) / 1.0E9F;
      return easeOutCubic(clamp01(elapsed / 0.62F));
   }

   private static float easeOutCubic(float value) {
      float inverse = 1.0F - clamp01(value);
      return 1.0F - inverse * inverse * inverse;
   }

   private static float clamp01(float value) {
      return clamp(value, 0.0F, 1.0F);
   }

   private static float clamp(float value, float minimum, float maximum) {
      return Math.max(minimum, Math.min(maximum, value));
   }

   @Environment(EnvType.CLIENT)
   private record Rect(int x, int y, int width, int height) {
      private boolean contains(float pointX, float pointY) {
         return pointX >= this.x && pointX < this.x + this.width && pointY >= this.y && pointY < this.y + this.height;
      }

      private AltManagerScreen.Rect expanded(int amount) {
         return new AltManagerScreen.Rect(this.x - amount, this.y - amount, this.width + amount * 2, this.height + amount * 2);
      }
   }
}
