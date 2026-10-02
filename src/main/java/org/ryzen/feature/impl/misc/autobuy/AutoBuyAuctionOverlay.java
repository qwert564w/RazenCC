package org.ryzen.feature.impl.misc.autobuy;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_465;
import org.ryzen.context.MinecraftContext;
import org.ryzen.context.RenderContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.render.FinalGuiRenderEvent;
import org.ryzen.event.events.screen.ScreenMouseButtonEvent;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.menu.core.MenuOverlay;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.mixin.accessor.AbstractContainerScreenAccessor;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class AutoBuyAuctionOverlay implements MinecraftContext {
   public static final AutoBuyAuctionOverlay INSTANCE = new AutoBuyAuctionOverlay();
   private static final float WIDTH = 138.0F;
   private static final float PADDING = 8.0F;
   private static final float HEADER_HEIGHT = 30.0F;
   private static final float ROW_HEIGHT = 24.0F;
   private static final float ROW_GAP = 4.0F;
   private static final float SEGMENT_HEIGHT = 22.0F;
   private static final float GAP_TO_SCREEN = 6.0F;
   private static final int PANEL = ColorUtil.rgba(18, 18, 20, 240);
   private static final int PANEL_BORDER = ColorUtil.rgba(255, 255, 255, 20);
   private static final int CONTROL = ColorUtil.rgba(255, 255, 255, 12);
   private static final int CONTROL_HOVER = ColorUtil.rgba(255, 255, 255, 26);
   private static final int CONTROL_BORDER = ColorUtil.rgba(255, 255, 255, 18);
   private static final int TEXT = -1;
   private static final int TEXT_MUTED = ColorUtil.rgba(255, 255, 255, 110);
   private static final int OFF_TRACK = ColorUtil.rgb(45, 44, 45);
   private static final int OFF_KNOB = ColorUtil.rgb(75, 74, 75);
   private static final String[] SERVERS = AutoBuyServer.ids();
   private static final String[] SERVER_SHORT = AutoBuyServer.shortLabels();
   private final List<AutoBuyAuctionOverlay.Hit> hits = new ArrayList<>();
   private float panelX;
   private float panelY;
   private float panelHeight;
   private boolean visible;

   private AutoBuyAuctionOverlay() {
   }

   @EventTarget
   public void onFinalGuiRender(FinalGuiRenderEvent event) {
      this.visible = false;
      this.hits.clear();
      if (event.isRenderScreen()) {
         AutoBuyFeature feature = AutoBuyFeature.get();
         class_465<?> screen = this.auctionScreen();
         if (feature != null && screen != null) {
            class_310 client = event.getClient();
            RenderContext.enter2D(event.getGui(), event.getGuiGraphicsExtractor(), event.getDeltaTracker());

            try {
               Render2DUtil.beginFrame();
               int mouseX = MenuOverlay.isOpen() ? -536870912 : event.getMouseX();
               int mouseY = MenuOverlay.isOpen() ? -536870912 : event.getMouseY();
               this.draw(client, screen, feature, mouseX, mouseY);
               Render2DUtil.flush();
               this.visible = true;
            } finally {
               RenderContext.exit2D();
            }
         }
      }
   }

   @EventTarget
   public void onScreenMouseButton(ScreenMouseButtonEvent event) {
      if (this.visible && event.getAction() == ScreenMouseButtonEvent.Action.CLICK && !MenuOverlay.isOpen()) {
         AutoBuyFeature feature = AutoBuyFeature.get();
         if (feature != null) {
            int button = event.getMouseButtonEvent().method_74245();
            if (button == 0 || button == 1) {
               double mouseX = event.getMouseButtonEvent().comp_4798();
               double mouseY = event.getMouseButtonEvent().comp_4799();

               for (AutoBuyAuctionOverlay.Hit hit : this.hits) {
                  if (hit.contains(mouseX, mouseY)) {
                     boolean left = button == 0;
                     apply(feature, hit.action(), !hit.fixed() && !left ? -hit.value() : hit.value());
                     event.cancel();
                     return;
                  }
               }

               if (mouseX >= this.panelX && mouseX <= this.panelX + 138.0F && mouseY >= this.panelY && mouseY <= this.panelY + this.panelHeight) {
                  event.cancel();
               }
            }
         }
      }
   }

   private void draw(class_310 client, class_465<?> screen, AutoBuyFeature feature, int mouseX, int mouseY) {
      AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor)screen;
      boolean extraRow = feature.serverMode.is("FunTime") || feature.serverMode.is("SpookyTime");
      int rows = extraRow ? 6 : 5;
      this.panelHeight = 64.0F + rows * 28.0F + 14.0F;
      float left = accessor.getLeftPos() - 138.0F - 6.0F;
      if (left < 4.0F) {
         left = accessor.getLeftPos() + accessor.getImageWidth() + 6.0F;
      }

      float maxLeft = client.method_22683().method_4486() - 138.0F - 4.0F;
      this.panelX = Math.max(4.0F, Math.min(left, maxLeft));
      this.panelY = Math.max(4.0F, Math.min(accessor.getTopPos(), client.method_22683().method_4502() - this.panelHeight - 4.0F));
      rect(this.panelX, this.panelY, 138.0F, this.panelHeight, PANEL, 12.0F, PANEL_BORDER);
      this.drawHeader(feature);
      float x = this.panelX + 8.0F;
      float width = 122.0F;
      float y = this.panelY + 30.0F + 8.0F;
      y = this.drawServerSegments(feature, x, y, width, mouseX, mouseY);
      y = this.drawPrimary(feature, x, y, width, mouseX, mouseY);
      y = this.drawToggleRow(feature, "AutoParse", feature.isAutoParseEnabled(), AutoBuyAuctionOverlay.Action.TOGGLE_AUTO_PARSE, x, y, width, mouseX, mouseY);
      y = this.drawStepper(
         MenuText.ui("Parse discount"), feature.parseDiscount.getValue().intValue() + "%", AutoBuyAuctionOverlay.Action.DISCOUNT, x, y, width, mouseX, mouseY
      );
      y = this.drawStepper(
         MenuText.ui("Refresh"), feature.updateDelay.getValue().intValue() + " ms", AutoBuyAuctionOverlay.Action.REFRESH, x, y, width, mouseX, mouseY
      );
      if (feature.serverMode.is("FunTime")) {
         y = this.drawStepper(
            MenuText.ui("Anarchy swap"),
            feature.anarchyMinSec.getValue().intValue() + "-" + feature.anarchyMaxSec.getValue().intValue() + " s",
            AutoBuyAuctionOverlay.Action.ANARCHY,
            x,
            y,
            width,
            mouseX,
            mouseY
         );
      } else if (feature.serverMode.is("SpookyTime")) {
         y = this.drawToggleRow(
            feature, MenuText.ui("Walk away"), feature.spWalk.getValue(), AutoBuyAuctionOverlay.Action.TOGGLE_WALK, x, y, width, mouseX, mouseY
         );
      }

      this.drawFooter(feature, x, y, width, mouseX, mouseY);
   }

   private void drawHeader(AutoBuyFeature feature) {
      float badge = 18.0F;
      float badgeY = this.panelY + (30.0F - badge) / 2.0F;
      rect(this.panelX + 8.0F, badgeY, badge, badge, Theme.getAccent(), 6.0F, 0);
      Render2DUtil.texture(this.panelX + 8.0F + 3.0F, badgeY + 3.0F, 12.0F, 12.0F, Textures.Logos.AUTOBUY).color(-1).draw();
      float textX = this.panelX + 8.0F + badge + 7.0F;
      text(textX, this.panelY + 10.0F, 11.0F, "Auto", -1, UiFontStyle.SEMIBOLD, TextAlign.LEFT);
      text(textX + measure("Auto", 11.0F, UiFontStyle.SEMIBOLD), this.panelY + 10.0F, 11.0F, "Buy", Theme.getAccent(), UiFontStyle.SEMIBOLD, TextAlign.LEFT);
      int count = AutoBuyManager.get().enabledCount(feature.serverMode.getValue());
      text(this.panelX + 138.0F - 8.0F, this.panelY + 11.0F, 10.0F, String.valueOf(count), TEXT_MUTED, UiFontStyle.MEDIUM, TextAlign.RIGHT);
      Render2DUtil.rect(this.panelX + 8.0F, this.panelY + 30.0F - 1.0F, 122.0F, 1.0F).color(ColorUtil.rgba(255, 255, 255, 22)).draw();
   }

   private float drawServerSegments(AutoBuyFeature feature, float x, float y, float width, int mouseX, int mouseY) {
      rect(x, y, width, 22.0F, CONTROL, 7.0F, CONTROL_BORDER);
      float segment = width / SERVERS.length;

      for (int index = 0; index < SERVERS.length; index++) {
         float segmentX = x + segment * index;
         boolean selected = feature.serverMode.is(SERVERS[index]);
         boolean hovered = inside(mouseX, mouseY, segmentX, y, segment, 22.0F);
         if (selected) {
            rect(segmentX + 2.0F, y + 2.0F, segment - 4.0F, 18.0F, Theme.getAccent(), 5.0F, 0);
         } else if (hovered) {
            rect(segmentX + 2.0F, y + 2.0F, segment - 4.0F, 18.0F, CONTROL_HOVER, 5.0F, 0);
         }

         text(segmentX + segment / 2.0F, y + 6.5F, 10.0F, SERVER_SHORT[index], selected ? -1 : TEXT_MUTED, UiFontStyle.MEDIUM, TextAlign.CENTER);
         this.hits.add(new AutoBuyAuctionOverlay.Hit(segmentX, y, segment, 22.0F, AutoBuyAuctionOverlay.Action.SERVER, index));
      }

      return y + 22.0F + 4.0F;
   }

   private float drawPrimary(AutoBuyFeature feature, float x, float y, float width, int mouseX, int mouseY) {
      boolean enabled = feature.isEnabled();
      boolean hovered = inside(mouseX, mouseY, x, y, width, 24.0F);
      int fill = enabled ? (hovered ? ColorUtil.multiplyRgb(Theme.getAccent(), 1.1F) : Theme.getAccent()) : (hovered ? CONTROL_HOVER : CONTROL);
      rect(x, y, width, 24.0F, fill, 7.0F, enabled ? 0 : CONTROL_BORDER);
      text(
         x + width / 2.0F,
         y + 7.0F,
         11.0F,
         enabled ? MenuText.ui("Running") : MenuText.ui("Idle"),
         enabled ? -1 : TEXT_MUTED,
         UiFontStyle.MEDIUM,
         TextAlign.CENTER
      );
      this.hits.add(new AutoBuyAuctionOverlay.Hit(x, y, width, 24.0F, AutoBuyAuctionOverlay.Action.TOGGLE_MODULE, 0));
      return y + 24.0F + 4.0F;
   }

   private float drawToggleRow(
      AutoBuyFeature feature, String label, boolean on, AutoBuyAuctionOverlay.Action action, float x, float y, float width, int mouseX, int mouseY
   ) {
      boolean hovered = inside(mouseX, mouseY, x, y, width, 24.0F);
      rect(x, y, width, 24.0F, hovered ? CONTROL_HOVER : CONTROL, 7.0F, CONTROL_BORDER);
      text(x + 8.0F, y + 7.0F, 10.0F, fit(label, width - 44.0F, 10.0F, UiFontStyle.MEDIUM), on ? -1 : TEXT_MUTED, UiFontStyle.MEDIUM, TextAlign.LEFT);
      float track = 24.0F;
      float trackHeight = 13.0F;
      float trackX = x + width - track - 7.0F;
      float trackY = y + (24.0F - trackHeight) / 2.0F;
      rect(trackX, trackY, track, trackHeight, on ? Theme.getAccent() : OFF_TRACK, trackHeight / 2.0F, 0);
      rect(trackX + (on ? track - 11.0F : 2.0F), trackY + 2.0F, 9.0F, 9.0F, on ? -1 : OFF_KNOB, 4.5F, 0);
      this.hits.add(new AutoBuyAuctionOverlay.Hit(x, y, width, 24.0F, action, 0));
      return y + 24.0F + 4.0F;
   }

   private float drawStepper(String label, String value, AutoBuyAuctionOverlay.Action action, float x, float y, float width, int mouseX, int mouseY) {
      boolean hovered = inside(mouseX, mouseY, x, y, width, 24.0F);
      rect(x, y, width, 24.0F, hovered ? CONTROL_HOVER : CONTROL, 7.0F, CONTROL_BORDER);
      float chip = 14.0F;
      float chipY = y + (24.0F - chip) / 2.0F;
      float plusX = x + width - chip - 5.0F;
      float minusX = plusX - chip - 3.0F;
      text(x + 8.0F, y + 3.0F, 9.0F, fit(label, width - 66.0F, 9.0F, UiFontStyle.REGULAR), TEXT_MUTED, UiFontStyle.REGULAR, TextAlign.LEFT);
      text(x + 8.0F, y + 13.0F, 10.0F, value, -1, UiFontStyle.MEDIUM, TextAlign.LEFT);
      this.drawChip(minusX, chipY, chip, "-", mouseX, mouseY);
      this.drawChip(plusX, chipY, chip, "+", mouseX, mouseY);
      this.hits.add(new AutoBuyAuctionOverlay.Hit(minusX, chipY, chip, chip, action, -1, true));
      this.hits.add(new AutoBuyAuctionOverlay.Hit(plusX, chipY, chip, chip, action, 1, true));
      this.hits.add(new AutoBuyAuctionOverlay.Hit(x, y, width, 24.0F, action, 1, false));
      return y + 24.0F + 4.0F;
   }

   private void drawChip(float x, float y, float size, String glyph, int mouseX, int mouseY) {
      boolean hovered = inside(mouseX, mouseY, x, y, size, size);
      rect(x, y, size, size, hovered ? ColorUtil.multiplyAlpha(Theme.getAccent(), 0.65F) : CONTROL_HOVER, size / 2.0F, 0);
      text(x + size / 2.0F, y + 2.5F, 10.0F, glyph, hovered ? -1 : -1, UiFontStyle.MEDIUM, TextAlign.CENTER);
   }

   private void drawFooter(AutoBuyFeature feature, float x, float y, float width, int mouseX, int mouseY) {
      boolean parsing = feature.isParseRunning();
      String label = parsing
         ? MenuText.ui("Parsing") + " " + (feature.getParseIndex() + 1) + "/" + Math.max(1, feature.getParseQueueSize())
         : MenuText.ui("Parse now");
      boolean hovered = !parsing && inside(mouseX, mouseY, x, y, width, 24.0F);
      rect(x, y, width, 24.0F, parsing ? ColorUtil.rgb(176, 134, 42) : (hovered ? ColorUtil.multiplyRgb(Theme.getAccent(), 1.1F) : Theme.getAccent()), 7.0F, 0);
      text(x + width / 2.0F, y + 7.0F, 10.0F, label, -1, UiFontStyle.MEDIUM, TextAlign.CENTER);
      if (!parsing) {
         this.hits.add(new AutoBuyAuctionOverlay.Hit(x, y, width, 24.0F, AutoBuyAuctionOverlay.Action.PARSE_NOW, 0));
      }
   }

   private static void apply(AutoBuyFeature feature, AutoBuyAuctionOverlay.Action action, int value) {
      int direction = value;
      switch (action) {
         case SERVER:
            feature.serverMode.setValue(SERVERS[value]);
            break;
         case TOGGLE_MODULE:
            feature.toggle();
            break;
         case TOGGLE_AUTO_PARSE:
            feature.toggleAutoParse();
            break;
         case TOGGLE_WALK:
            feature.spWalk.setValue(!feature.spWalk.getValue());
            break;
         case PARSE_NOW:
            feature.startParseNow();
            break;
         case DISCOUNT:
            nudge(feature.parseDiscount, direction);
            break;
         case REFRESH:
            nudge(feature.updateDelay, direction * 50.0);
            break;
         case ANARCHY:
            nudge(feature.anarchyMinSec, direction * 5.0);
            feature.anarchyMaxSec.setValue(Math.max(feature.anarchyMinSec.getValue() + 5.0, feature.anarchyMaxSec.getValue() + direction * 5.0));
      }
   }

   private static void nudge(NumberSetting setting, double delta) {
      setting.setValue(setting.getValue() + delta);
   }

   private class_465<?> auctionScreen() {
      if (this.screen() instanceof class_465<?> container && this.player() != null) {
         String title = container.method_25440().getString();
         return !AuctionUtils.isAuctionTitle(title) && !AuctionUtils.isSearchTitle(title) ? null : container;
      } else {
         return null;
      }
   }

   private static boolean inside(double mouseX, double mouseY, float x, float y, float width, float height) {
      return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
   }

   private static void rect(float x, float y, float width, float height, int color, float radius, int border) {
      Render2DUtil.RectBuilder builder = Render2DUtil.rect(x, y, width, height).color(color).radius(radius);
      if (border != 0) {
         builder.border(0.5F, border);
      }

      builder.draw();
   }

   private static void text(float x, float y, float size, String value, int color, UiFontStyle style, TextAlign align) {
      Render2DUtil.text(x, y, size, value).family(UiFonts.suisse()).weight(style.weight()).style(UiFontStyle.REGULAR).align(align).color(color).draw();
   }

   private static float measure(String value, float size, UiFontStyle style) {
      MsdfFont font = UiFonts.suisse().resolve(style.weight());
      return font.measureWidth(value == null ? "" : value, size, 0.0F);
   }

   private static String fit(String raw, float maxWidth, float size, UiFontStyle style) {
      String value = raw == null ? "" : raw;
      if (measure(value, size, style) <= maxWidth) {
         return value;
      }

      int end = value.length();

      while (end > 0 && measure(value.substring(0, end) + "...", size, style) > maxWidth) {
         end--;
      }

      return value.substring(0, end) + "...";
   }

   @Environment(EnvType.CLIENT)
   private enum Action {
      SERVER,
      TOGGLE_MODULE,
      TOGGLE_AUTO_PARSE,
      TOGGLE_WALK,
      PARSE_NOW,
      DISCOUNT,
      REFRESH,
      ANARCHY;
   }

   @Environment(EnvType.CLIENT)
   private record Hit(float x, float y, float width, float height, AutoBuyAuctionOverlay.Action action, int value, boolean fixed) {
      private Hit(float x, float y, float width, float height, AutoBuyAuctionOverlay.Action action, int value) {
         this(x, y, width, height, action, value, true);
      }

      private boolean contains(double mouseX, double mouseY) {
         return AutoBuyAuctionOverlay.inside(mouseX, mouseY, this.x, this.y, this.width, this.height);
      }
   }
}
