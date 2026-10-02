package org.ryzen.menu.clickgui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.ryzen.feature.impl.misc.autobuy.AutoBuyFeature;
import org.ryzen.feature.impl.misc.autobuy.AutoBuyItem;
import org.ryzen.feature.impl.misc.autobuy.AutoBuyItemCategory;
import org.ryzen.feature.impl.misc.autobuy.AutoBuyManager;
import org.ryzen.feature.impl.misc.autobuy.AutoBuyServer;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.controls.MenuClipboard;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class ClickGuiAutoBuyPage {
   private static final float PANEL_X = 94.0F;
   private static final float PANEL_Y = 65.0F;
   private static final float PANEL_WIDTH = 821.0F;
   private static final float PANEL_HEIGHT = 562.0F;
   private static final float CONTENT_X = 110.0F;
   private static final float CONTENT_WIDTH = 789.0F;
   private static final float TOPBAR_Y = 81.0F;
   private static final float TOPBAR_HEIGHT = 52.0F;
   private static final float COLUMN_Y = 147.0F;
   private static final float COLUMN_HEIGHT = 464.0F;
   private static final float COLUMN_GAP = 14.0F;
   private static final float HEADER_HEIGHT = 30.0F;
   private static final float TRACK_WIDTH = 3.0F;
   private static final float CELL = 40.0F;
   private static final float CELL_GAP = 6.0F;
   private static final float GRID_INSET = 12.0F;
   private static final float BAR_RESERVE = 10.0F;
   private static final float RULE_HEIGHT = 58.0F;
   private static final float RULE_GAP = 6.0F;
   private static final float ROW_HEIGHT = 44.0F;
   private static final float ROW_GAP = 6.0F;
   private static final float BUTTON_HEIGHT = 30.0F;
   private static final int PRICE_MAX_DIGITS = 10;
   private static final String[] SERVERS = AutoBuyServer.ids();
   private static final String[] SERVER_SHORT = AutoBuyServer.shortLabels();
   private static final AutoBuyItemCategory[] TABS_FUNTIME = new AutoBuyItemCategory[]{
      null, AutoBuyItemCategory.KRUSH, AutoBuyItemCategory.SPHERES, AutoBuyItemCategory.TALISMANS, AutoBuyItemCategory.POTIONS, AutoBuyItemCategory.MISC
   };
   private static final String[] TAB_LABELS_FUNTIME = new String[]{"All", "Crusher", "Spheres", "Talismans", "Potions", "Misc"};
   private static final AutoBuyItemCategory[] TABS_HOLYWORLD = new AutoBuyItemCategory[]{null, AutoBuyItemCategory.HOLYWORLD};
   private static final String[] TAB_LABELS_HOLYWORLD = new String[]{"All", "HolyWorld"};
   private final List<ClickGuiAutoBuyPage.Hit> serverHits = new ArrayList<>();
   private final List<ClickGuiAutoBuyPage.Hit> tabHits = new ArrayList<>();
   private final List<ClickGuiAutoBuyPage.ItemHit> catalogHits = new ArrayList<>();
   private final List<ClickGuiAutoBuyPage.RuleHit> ruleHits = new ArrayList<>();
   private final List<ClickGuiAutoBuyPage.SettingHit> settingHits = new ArrayList<>();
   private final List<ClickGuiAutoBuyPage.StepperHit> stepperHits = new ArrayList<>();
   private final ClickGuiAutoBuyPage.Column catalog = new ClickGuiAutoBuyPage.Column();
   private final ClickGuiAutoBuyPage.Column rules = new ClickGuiAutoBuyPage.Column();
   private final ClickGuiAutoBuyPage.Column options = new ClickGuiAutoBuyPage.Column();
   private ClickGuiAutoBuyPage.Bounds parseNowHit;
   private ClickGuiAutoBuyPage.Bounds autoParseHit;
   private ClickGuiAutoBuyPage.Bounds clearHit;
   private int selectedTab;
   private String lastServer = "";
   private AutoBuyItem editingItem;
   private String priceDraft = "";
   private ClickGuiAutoBuyPage.Column draggedColumn;
   private float dragGrabOffset;
   private List<AutoBuyItem> cachedCatalog = List.of();
   private String cachedCatalogKey = "";

   public void layout(ClickGuiCanvas canvas) {
      AutoBuyManager.get().ensureLoaded();
      float column1 = 264.315F;
      float column3 = 216.975F;
      float column2 = 789.0F - column1 - column3 - 28.0F;
      this.catalog.place(110.0F, column1);
      this.rules.place(110.0F + column1 + 14.0F, column2);
      this.options.place(110.0F + column1 + 14.0F + column2 + 14.0F, column3);
      String server = serverMode();
      if (!server.equalsIgnoreCase(this.lastServer)) {
         if (!this.lastServer.isEmpty() && isHolyWorld(this.lastServer) != isHolyWorld(server)) {
            this.selectedTab = 0;
            this.catalog.reset();
            this.rules.reset();
         }

         this.cancelPriceEdit();
         this.lastServer = server;
      }

      if (this.selectedTab >= tabsFor(server).length) {
         this.selectedTab = 0;
      }
   }

   public void render(ClickGuiCanvas canvas, class_332 graphics) {
      AutoBuyFeature feature = AutoBuyFeature.get();
      String server = serverMode();
      canvas.outlinedRect(94.0F, 65.0F, 821.0F, 562.0F, ClickGuiPalette.SURFACE, 17.0F, 0.5F, ClickGuiPalette.STROKE);
      this.renderTopBar(canvas, feature, server);
      this.renderCatalog(canvas, server);
      this.renderRules(canvas, server);
      this.renderOptions(canvas, feature, server);
      if (graphics != null) {
         Render2DUtil.flush();
         graphics.method_71048();
         this.renderCatalogIcons(canvas, graphics);
         this.renderRuleIcons(canvas, graphics);
      }
   }

   private void renderTopBar(ClickGuiCanvas canvas, AutoBuyFeature feature, String server) {
      canvas.outlinedRect(110.0F, 81.0F, 789.0F, 52.0F, ClickGuiPalette.CONTROL, 14.0F, 0.5F, ClickGuiPalette.CARD_STROKE);
      float badge = 34.0F;
      float badgeY = 81.0F + (52.0F - badge) / 2.0F;
      canvas.rect(119.0F, badgeY, badge, badge, ClickGuiPalette.accent(), 11.0F);
      canvas.texture(125.0F, badgeY + 6.0F, 22.0F, 22.0F, Textures.Logos.AUTOBUY, -1);
      float textX = 119.0F + badge + 11.0F;
      float wordY = 92.0F;
      canvas.text(textX, wordY, 18.0F, "Auto", -1, UiFontStyle.SEMIBOLD);
      float autoWidth = canvas.textWidth("Auto", 18.0F, UiFontStyle.SEMIBOLD);
      canvas.text(textX + autoWidth, wordY, 18.0F, "Buy", ClickGuiPalette.accent(), UiFontStyle.SEMIBOLD);
      canvas.text(textX, 112.0F, 11.0F, MenuText.ui("Auction sniper"), ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR);
      float wordWidth = autoWidth + canvas.textWidth("Buy", 18.0F, UiFontStyle.SEMIBOLD);
      float pillHeight = 24.0F;
      float pillY = 81.0F + (52.0F - pillHeight) / 2.0F;
      float pillX = Math.max(textX + wordWidth + 22.0F, textX + 128.0F);
      this.serverHits.clear();

      for (int index = 0; index < SERVERS.length; index++) {
         float width = canvas.textWidth(SERVER_SHORT[index], 12.0F, UiFontStyle.MEDIUM) + 22.0F;
         boolean selected = SERVERS[index].equalsIgnoreCase(server);
         boolean hovered = canvas.hit(pillX, pillY, width, pillHeight);
         canvas.outlinedRect(
            pillX,
            pillY,
            width,
            pillHeight,
            selected ? ClickGuiPalette.accent() : (hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.CONTROL),
            pillHeight / 2.0F,
            0.5F,
            selected ? 0 : ClickGuiPalette.CONTROL_STROKE
         );
         canvas.text(
            pillX + width / 2.0F,
            pillY + 6.0F,
            12.0F,
            SERVER_SHORT[index],
            selected ? -1 : (hovered ? -1 : ClickGuiPalette.TEXT_MUTED),
            UiFontStyle.MEDIUM,
            TextAlign.CENTER
         );
         this.serverHits.add(new ClickGuiAutoBuyPage.Hit(pillX, pillY, width, pillHeight, index));
         pillX += width + 6.0F;
      }

      this.renderStatus(canvas, feature, pillY, pillHeight);
   }

   private void renderStatus(ClickGuiCanvas canvas, AutoBuyFeature feature, float pillY, float pillHeight) {
      boolean parsing = feature != null && feature.isParseRunning();
      boolean armed = feature != null && feature.isAutoParseEnabled();
      boolean running = feature != null && feature.isEnabled();
      String label;
      int dot;
      if (parsing) {
         label = MenuText.ui("Parsing") + " " + (feature.getParseIndex() + 1) + "/" + Math.max(1, feature.getParseQueueSize());
         dot = ColorUtil.rgb(240, 200, 90);
      } else if (armed) {
         label = MenuText.ui("AutoParse");
         dot = ColorUtil.rgb(100, 220, 140);
      } else if (running) {
         label = MenuText.ui("Running");
         dot = ClickGuiPalette.accent();
      } else {
         label = MenuText.ui("Idle");
         dot = ColorUtil.rgba(255, 255, 255, 90);
      }

      float width = canvas.textWidth(label, 11.0F, UiFontStyle.MEDIUM) + 32.0F;
      float x = 899.0F - width - 10.0F;
      canvas.rect(x, pillY, width, pillHeight, ClickGuiPalette.CONTROL, pillHeight / 2.0F);
      canvas.rect(x + 11.0F, pillY + pillHeight / 2.0F - 2.5F, 5.0F, 5.0F, dot, 2.5F);
      canvas.text(x + 22.0F, pillY + 6.5F, 11.0F, label, !parsing && !armed ? ClickGuiPalette.TEXT_SECONDARY : -1, UiFontStyle.MEDIUM);
   }

   private void renderCatalog(ClickGuiCanvas canvas, String server) {
      ClickGuiAutoBuyPage.Column column = this.catalog;
      this.renderColumnCard(canvas, column);
      this.renderColumnHeader(canvas, column, MenuText.ui("Catalog"), shortServer(server));
      String[] labels = tabLabelsFor(server);
      float tabY = 183.0F;
      float tabX = column.x + 12.0F;
      this.tabHits.clear();

      for (int index = 0; index < labels.length; index++) {
         String label = MenuText.ui(labels[index]);
         float width = canvas.textWidth(label, 11.0F, UiFontStyle.MEDIUM) + 16.0F;
         if (tabX + width > column.x + column.width - 12.0F) {
            break;
         }

         boolean selected = this.selectedTab == index;
         boolean hovered = canvas.hit(tabX, tabY, width, 22.0F);
         canvas.rect(
            tabX, tabY, width, 22.0F, selected ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.22F) : (hovered ? ClickGuiPalette.CONTROL_HOVER : 0), 8.0F
         );
         canvas.text(
            tabX + width / 2.0F,
            tabY + 6.0F,
            11.0F,
            label,
            selected ? ClickGuiPalette.accent() : (hovered ? ClickGuiPalette.TEXT_SECONDARY : ClickGuiPalette.TEXT_MUTED),
            UiFontStyle.MEDIUM,
            TextAlign.CENTER
         );
         this.tabHits.add(new ClickGuiAutoBuyPage.Hit(tabX, tabY, width, 22.0F, index));
         tabX += width + 4.0F;
      }

      float viewY = tabY + 22.0F + 8.0F;
      float viewHeight = 611.0F - viewY - 10.0F;
      column.view(viewY, viewHeight);
      List<AutoBuyItem> items = this.filteredCatalog(server);
      ClickGuiAutoBuyPage.Grid grid = this.grid(column);
      int rows = (items.size() + grid.columns() - 1) / grid.columns();
      column.content(rows <= 0 ? 0.0F : rows * grid.cell() + (rows - 1) * 6.0F + 4.0F);
      this.catalogHits.clear();
      if (items.isEmpty()) {
         canvas.text(
            column.x + column.width / 2.0F,
            viewY + viewHeight / 2.0F - 10.0F,
            13.0F,
            MenuText.ui("Nothing here"),
            ClickGuiPalette.TEXT_SECONDARY,
            UiFontStyle.MEDIUM,
            TextAlign.CENTER
         );
         this.renderScrollbar(canvas, column);
      } else {
         canvas.pushScissor(column.x, viewY, column.width, viewHeight);

         for (int index = 0; index < items.size(); index++) {
            AutoBuyItem item = items.get(index);
            float cellX = column.x + 12.0F + index % grid.columns() * (grid.cell() + 6.0F);
            float cellY = viewY + 2.0F - column.scroll + index / grid.columns() * (grid.cell() + 6.0F);
            if (!(cellY + grid.cell() < viewY) && !(cellY > viewY + viewHeight)) {
               boolean enabled = item.isEnabled();
               boolean hovered = canvas.hit(cellX, cellY, grid.cell(), grid.cell());
               int fill = enabled
                  ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), hovered ? 0.42F : 0.3F)
                  : (hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.CONTROL);
               canvas.outlinedRect(
                  cellX,
                  cellY,
                  grid.cell(),
                  grid.cell(),
                  fill,
                  9.0F,
                  0.5F,
                  enabled ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.85F) : ClickGuiPalette.CONTROL_STROKE
               );
               if (enabled) {
                  float tick = 13.0F;
                  canvas.rect(cellX + grid.cell() - tick - 3.0F, cellY + 3.0F, tick, tick, ClickGuiPalette.accent(), tick / 2.0F);
                  canvas.texture(cellX + grid.cell() - tick + 0.5F, cellY + 7.5F, 6.0F, 4.5F, Textures.Icons.CHECK, -1);
               }

               this.catalogHits.add(new ClickGuiAutoBuyPage.ItemHit(cellX, cellY, grid.cell(), grid.cell(), item));
            }
         }

         canvas.popScissor();
         this.renderScrollbar(canvas, column);
      }
   }

   private void renderCatalogIcons(ClickGuiCanvas canvas, class_332 graphics) {
      if (!this.catalogHits.isEmpty()) {
         ClickGuiAutoBuyPage.Column column = this.catalog;
         float size = this.grid(column).cell() * 0.55F;
         this.scissor(canvas, graphics, column.x, column.viewY, column.width, column.viewHeight);

         try {
            for (ClickGuiAutoBuyPage.ItemHit hit : this.catalogHits) {
               this.drawItem(canvas, graphics, hit.item().icon(), hit.x() + (hit.width() - size) / 2.0F, hit.y() + (hit.height() - size) / 2.0F, size);
            }
         } finally {
            graphics.method_44380();
         }
      }
   }

   private void renderRules(ClickGuiCanvas canvas, String server) {
      ClickGuiAutoBuyPage.Column column = this.rules;
      List<AutoBuyItem> enabled = AutoBuyManager.get().enabledItems(server);
      this.renderColumnCard(canvas, column);
      this.renderColumnHeader(canvas, column, MenuText.ui("List"), String.valueOf(enabled.size()));
      float viewY = 183.0F;
      float viewHeight = 611.0F - viewY - 10.0F;
      column.view(viewY, viewHeight);
      column.content(enabled.isEmpty() ? 0.0F : enabled.size() * 58.0F + (enabled.size() - 1) * 6.0F + 4.0F);
      this.ruleHits.clear();
      if (enabled.isEmpty()) {
         canvas.text(
            column.x + column.width / 2.0F,
            viewY + viewHeight / 2.0F - 16.0F,
            13.0F,
            MenuText.ui("List is empty"),
            ClickGuiPalette.TEXT_SECONDARY,
            UiFontStyle.MEDIUM,
            TextAlign.CENTER
         );
         canvas.text(
            column.x + column.width / 2.0F,
            viewY + viewHeight / 2.0F + 3.0F,
            11.0F,
            MenuText.ui("Left click an icon to add it, right click to remove."),
            ClickGuiPalette.TEXT_MUTED,
            UiFontStyle.REGULAR,
            TextAlign.CENTER
         );
         this.renderScrollbar(canvas, column);
      } else {
         float rowX = column.x + 10.0F;
         float rowWidth = column.width - 20.0F - 10.0F;
         canvas.pushScissor(column.x, viewY, column.width, viewHeight);
         float rowY = viewY + 2.0F - column.scroll;

         for (AutoBuyItem item : enabled) {
            if (rowY + 58.0F >= viewY && rowY <= viewY + viewHeight) {
               this.renderRule(canvas, item, rowX, rowY, rowWidth);
            }

            rowY += 64.0F;
         }

         canvas.popScissor();
         this.renderScrollbar(canvas, column);
      }
   }

   private void renderRule(ClickGuiCanvas canvas, AutoBuyItem item, float x, float y, float width) {
      boolean editing = this.editingItem == item;
      boolean hovered = canvas.hit(x, y, width, 58.0F);
      canvas.outlinedRect(
         x,
         y,
         width,
         58.0F,
         !hovered && !editing ? ClickGuiPalette.CONTROL : ClickGuiPalette.CONTROL_HOVER,
         12.0F,
         0.5F,
         editing ? ClickGuiPalette.accent() : ClickGuiPalette.CARD_STROKE
      );
      canvas.rect(x + 3.0F, y + 12.0F, 2.5F, 34.0F, ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), editing ? 1.0F : 0.65F), 1.25F);
      float iconBox = 38.0F;
      float iconX = x + 11.0F;
      float iconY = y + (58.0F - iconBox) / 2.0F;
      canvas.rect(iconX, iconY, iconBox, iconBox, ClickGuiPalette.CONTROL, 9.0F);
      float textX = iconX + iconBox + 10.0F;
      canvas.text(textX, y + 9.0F, 13.0F, fit(canvas, item.getName(), x + width - textX - 12.0F, 13.0F, UiFontStyle.MEDIUM), -1, UiFontStyle.MEDIUM);
      String label = MenuText.ui("Max");
      float fieldY = y + 28.0F;
      float fieldHeight = 20.0F;
      canvas.text(textX, fieldY + 5.0F, 11.0F, label, ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR);
      float fieldX = textX + canvas.textWidth(label, 11.0F, UiFontStyle.REGULAR) + 8.0F;
      float fieldWidth = Math.max(56.0F, x + width - fieldX - 12.0F);
      canvas.outlinedRect(
         fieldX, fieldY, fieldWidth, fieldHeight, ClickGuiPalette.CONTROL, 6.0F, 0.5F, editing ? ClickGuiPalette.accent() : ClickGuiPalette.CONTROL_STROKE
      );
      String shown = editing ? this.priceDraft : formatPrice(item.getBuyPrice());
      canvas.text(fieldX + 7.0F, fieldY + 4.5F, 12.0F, shown, editing ? -1 : ClickGuiPalette.accent(), UiFontStyle.MEDIUM);
      if (editing && System.currentTimeMillis() / 500L % 2L == 0L) {
         float caret = fieldX + 8.0F + canvas.textWidth(shown, 12.0F, UiFontStyle.MEDIUM);
         canvas.rect(Math.min(caret, fieldX + fieldWidth - 4.0F), fieldY + 4.0F, 1.0F, fieldHeight - 8.0F, ClickGuiPalette.accent(), 0.0F);
      }

      this.ruleHits.add(new ClickGuiAutoBuyPage.RuleHit(x, y, width, 58.0F, fieldX, fieldY, fieldWidth, fieldHeight, iconX, iconY, iconBox, item));
   }

   private void renderRuleIcons(ClickGuiCanvas canvas, class_332 graphics) {
      if (!this.ruleHits.isEmpty()) {
         ClickGuiAutoBuyPage.Column column = this.rules;
         this.scissor(canvas, graphics, column.x, column.viewY, column.width, column.viewHeight);

         try {
            for (ClickGuiAutoBuyPage.RuleHit hit : this.ruleHits) {
               float size = hit.iconBox() * 0.58F;
               this.drawItem(
                  canvas, graphics, hit.item().icon(), hit.iconX() + (hit.iconBox() - size) / 2.0F, hit.iconY() + (hit.iconBox() - size) / 2.0F, size
               );
            }
         } finally {
            graphics.method_44380();
         }
      }
   }

   private void renderOptions(ClickGuiCanvas canvas, AutoBuyFeature feature, String server) {
      ClickGuiAutoBuyPage.Column column = this.options;
      this.renderColumnCard(canvas, column);
      this.renderColumnHeader(canvas, column, MenuText.ui("Settings"), null);
      float viewY = 183.0F;
      float viewHeight = 611.0F - viewY - 10.0F;
      column.view(viewY, viewHeight);
      this.settingHits.clear();
      this.stepperHits.clear();
      this.parseNowHit = null;
      this.autoParseHit = null;
      this.clearHit = null;
      float x = column.x + 10.0F;
      float width = column.width - 20.0F - 10.0F;
      float y = viewY + 2.0F - column.scroll;
      canvas.pushScissor(column.x, viewY, column.width, viewHeight);
      boolean parsing = feature != null && feature.isParseRunning();
      boolean hovered = canvas.hit(x, y, width, 30.0F) && inside(canvas, column);
      canvas.rect(
         x,
         y,
         width,
         30.0F,
         parsing ? ColorUtil.rgb(176, 134, 42) : (hovered ? ColorUtil.multiplyRgb(ClickGuiPalette.accent(), 1.1F) : ClickGuiPalette.accent()),
         9.0F
      );
      canvas.text(x + width / 2.0F, y + 8.0F, 12.0F, MenuText.ui(parsing ? "Parsing" : "Parse now"), -1, UiFontStyle.MEDIUM, TextAlign.CENTER);
      this.parseNowHit = clip(column, x, y, width, 30.0F);
      y += 36.0F;
      boolean armed = feature != null && feature.isAutoParseEnabled();
      hovered = canvas.hit(x, y, width, 30.0F) && inside(canvas, column);
      canvas.outlinedRect(x, y, width, 30.0F, hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.CONTROL, 9.0F, 0.5F, ClickGuiPalette.CONTROL_STROKE);
      canvas.text(x + 12.0F, y + 8.0F, 12.0F, MenuText.ui("AutoParse"), armed ? -1 : ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM);
      renderToggle(canvas, x + width - 41.0F, y + 5.0F, armed);
      this.autoParseHit = clip(column, x, y, width, 30.0F);
      y += 36.0F;
      hovered = canvas.hit(x, y, width, 30.0F) && inside(canvas, column);
      canvas.outlinedRect(x, y, width, 30.0F, hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.CONTROL, 9.0F, 0.5F, ClickGuiPalette.CONTROL_STROKE);
      String clearLabel = MenuText.ui("Clear list");
      float clearWidth = canvas.textWidth(clearLabel, 12.0F, UiFontStyle.MEDIUM);
      float clearIconX = x + (width - clearWidth - 18.0F) / 2.0F;
      canvas.texture(clearIconX, y + 9.5F, 11.0F, 11.0F, Textures.Icons.DELETE, hovered ? ColorUtil.rgb(240, 120, 120) : ClickGuiPalette.TEXT_MUTED);
      canvas.text(clearIconX + 18.0F, y + 8.0F, 12.0F, clearLabel, hovered ? -1 : ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM);
      this.clearHit = clip(column, x, y, width, 30.0F);
      y += 42.0F;
      canvas.rect(x, y, width, 1.0F, ClickGuiPalette.DIVIDER, 0.0F);
      y += 10.0F;
      if (feature != null) {
         y = this.renderSettingRow(
            canvas,
            column,
            x,
            y,
            width,
            MenuText.ui("Refresh"),
            feature.updateDelay.getValue().intValue() + " " + MenuText.ui("ms"),
            ClickGuiAutoBuyPage.SettingId.UPDATE_DELAY,
            progress(feature.updateDelay)
         );
         y = this.renderSettingRow(
            canvas,
            column,
            x,
            y,
            width,
            MenuText.ui("Parse discount"),
            feature.parseDiscount.getValue().intValue() + "%",
            ClickGuiAutoBuyPage.SettingId.PARSE_DISCOUNT,
            progress(feature.parseDiscount)
         );
         y = this.renderSettingRow(
            canvas,
            column,
            x,
            y,
            width,
            MenuText.ui("Buy cooldown"),
            feature.buyDelay.getValue().intValue() + " " + MenuText.ui("ms"),
            ClickGuiAutoBuyPage.SettingId.BUY_DELAY,
            progress(feature.buyDelay)
         );
         if (isFunTime(server)) {
            y = this.renderSettingRow(
               canvas,
               column,
               x,
               y,
               width,
               MenuText.ui("Anarchy swap"),
               feature.anarchyMinSec.getValue().intValue() + "-" + feature.anarchyMaxSec.getValue().intValue() + " " + MenuText.ui("s"),
               ClickGuiAutoBuyPage.SettingId.ANARCHY_RANGE,
               progress(feature.anarchyMinSec)
            );
         }

         if (!isHolyWorld(server)) {
            y = this.renderSettingRow(
               canvas, column, x, y, width, MenuText.ui("Swap /an"), onOff(feature.anarchySwap.getValue()), ClickGuiAutoBuyPage.SettingId.ANARCHY_SWAP, -1.0F
            );
         }

         if (isSpookyTime(server)) {
            y = this.renderSettingRow(
               canvas, column, x, y, width, MenuText.ui("Walk away"), onOff(feature.spWalk.getValue()), ClickGuiAutoBuyPage.SettingId.SPOOKY_WALK, -1.0F
            );
            y = this.renderSettingRow(
               canvas,
               column,
               x,
               y,
               width,
               MenuText.ui("Walk distance"),
               trimZero(feature.spWalkBlocks.getValue()) + " " + MenuText.ui("b"),
               ClickGuiAutoBuyPage.SettingId.SPOOKY_DISTANCE,
               progress(feature.spWalkBlocks)
            );
            y = this.renderSettingRow(
               canvas,
               column,
               x,
               y,
               width,
               MenuText.ui("Walk interval"),
               feature.spWalkIntervalSec.getValue().intValue() + " " + MenuText.ui("s"),
               ClickGuiAutoBuyPage.SettingId.SPOOKY_INTERVAL,
               progress(feature.spWalkIntervalSec)
            );
         }

         y = this.renderSettingRow(
            canvas, column, x, y, width, MenuText.ui("Auto /ah"), onOff(feature.autoOpenAh.getValue()), ClickGuiAutoBuyPage.SettingId.AUTO_AH, -1.0F
         );
         y = this.renderSettingRow(
            canvas,
            column,
            x,
            y,
            width,
            MenuText.ui("ReParse"),
            feature.reparseMinutes.getValue().intValue() + " " + MenuText.ui("min"),
            ClickGuiAutoBuyPage.SettingId.REPARSE,
            progress(feature.reparseMinutes)
         );
         if (parsing) {
            y += 4.0F;
            canvas.text(
               x + width / 2.0F,
               y,
               11.0F,
               fit(canvas, feature.getParseCurrentName(), width, 11.0F, UiFontStyle.REGULAR),
               ClickGuiPalette.TEXT_MUTED,
               UiFontStyle.REGULAR,
               TextAlign.CENTER
            );
            y += 16.0F;
         }
      }

      canvas.popScissor();
      column.content(y + column.scroll - viewY + 6.0F);
      this.renderScrollbar(canvas, column);
   }

   private float renderSettingRow(
      ClickGuiCanvas canvas,
      ClickGuiAutoBuyPage.Column column,
      float x,
      float y,
      float width,
      String label,
      String value,
      ClickGuiAutoBuyPage.SettingId id,
      float fraction
   ) {
      boolean hovered = canvas.hit(x, y, width, 44.0F) && inside(canvas, column);
      canvas.outlinedRect(
         x,
         y,
         width,
         44.0F,
         hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.CONTROL,
         9.0F,
         0.5F,
         hovered ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.45F) : ClickGuiPalette.CONTROL_STROKE
      );
      canvas.text(x + 10.0F, y + 8.0F, 11.0F, fit(canvas, label, width - 118.0F, 11.0F, UiFontStyle.REGULAR), ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR);
      float valueWidth = canvas.textWidth(value, 12.0F, UiFontStyle.MEDIUM);
      canvas.text(x + width - 10.0F, y + 7.0F, 12.0F, value, hovered ? ClickGuiPalette.accent() : -1, UiFontStyle.MEDIUM, TextAlign.RIGHT);
      if (fraction >= 0.0F) {
         canvas.rect(x + 10.0F, y + 44.0F - 13.0F, width - 20.0F, 3.0F, ClickGuiPalette.CONTROL_ACTIVE, 1.5F);
         canvas.rect(x + 10.0F, y + 44.0F - 13.0F, (width - 20.0F) * fraction, 3.0F, ClickGuiPalette.accent(), 1.5F);
      }

      if (hovered) {
         float chip = 15.0F;
         float chipY = y + 5.0F;
         float minusX = x + width - 10.0F - valueWidth - chip * 2.0F - 10.0F;
         this.renderChip(canvas, minusX, chipY, chip, "-");
         this.renderChip(canvas, minusX + chip + 4.0F, chipY, chip, "+");
         this.stepperHits.add(new ClickGuiAutoBuyPage.StepperHit(minusX, chipY, chip, chip, id, -1));
         this.stepperHits.add(new ClickGuiAutoBuyPage.StepperHit(minusX + chip + 4.0F, chipY, chip, chip, id, 1));
      }

      ClickGuiAutoBuyPage.Bounds bounds = clip(column, x, y, width, 44.0F);
      if (bounds != null) {
         this.settingHits.add(new ClickGuiAutoBuyPage.SettingHit(bounds, id));
      }

      return y + 44.0F + 6.0F;
   }

   private void renderChip(ClickGuiCanvas canvas, float x, float y, float size, String glyph) {
      boolean hovered = canvas.hit(x, y, size, size);
      canvas.rect(x, y, size, size, hovered ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.55F) : ClickGuiPalette.CONTROL_ACTIVE, size / 2.0F);
      canvas.text(x + size / 2.0F, y + 2.5F, 11.0F, glyph, hovered ? -1 : ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM, TextAlign.CENTER);
   }

   private void renderColumnCard(ClickGuiCanvas canvas, ClickGuiAutoBuyPage.Column column) {
      canvas.outlinedRect(column.x, 147.0F, column.width, 464.0F, ClickGuiPalette.SURFACE, 14.0F, 0.5F, ClickGuiPalette.STROKE);
   }

   private void renderColumnHeader(ClickGuiCanvas canvas, ClickGuiAutoBuyPage.Column column, String title, String badge) {
      canvas.rect(column.x + 12.0F, 157.0F, 2.5F, 12.0F, ClickGuiPalette.accent(), 1.25F);
      canvas.text(column.x + 21.0F, 156.0F, 13.0F, title, -1, UiFontStyle.MEDIUM);
      if (badge != null && !badge.isEmpty()) {
         float textWidth = canvas.textWidth(badge, 11.0F, UiFontStyle.MEDIUM);
         float width = textWidth + 16.0F;
         float x = column.x + column.width - width - 12.0F;
         canvas.outlinedRect(
            x,
            155.0F,
            width,
            17.0F,
            ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.16F),
            8.5F,
            0.5F,
            ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.42F)
         );
         canvas.text(x + width / 2.0F, 158.0F, 11.0F, badge, ClickGuiPalette.accent(), UiFontStyle.MEDIUM, TextAlign.CENTER);
      }

      canvas.rect(column.x + 12.0F, 177.0F, column.width - 24.0F, 1.0F, ClickGuiPalette.DIVIDER, 0.0F);
   }

   private void renderScrollbar(ClickGuiCanvas canvas, ClickGuiAutoBuyPage.Column column) {
      column.clampScroll();
      if (column.content <= column.viewHeight + 0.5F) {
         column.thumbHeight = 0.0F;
      } else {
         float trackX = column.x + column.width - 8.0F;
         float thumbHeight = Math.max(28.0F, column.viewHeight * column.viewHeight / column.content);
         float travel = column.viewHeight - thumbHeight;
         float thumbY = column.viewY + travel * (column.scroll / Math.max(1.0F, column.maxScroll()));
         canvas.rect(trackX, column.viewY, 3.0F, column.viewHeight, ClickGuiPalette.CONTROL, 1.5F);
         canvas.rect(trackX, thumbY, 3.0F, thumbHeight, ClickGuiPalette.accent(), 1.5F);
         column.thumbX = trackX;
         column.thumbY = thumbY;
         column.thumbHeight = thumbHeight;
      }
   }

   private static void renderToggle(ClickGuiCanvas canvas, float x, float y, boolean enabled) {
      canvas.rect(x, y, 33.0F, 20.0F, enabled ? ClickGuiPalette.accent() : ClickGuiPalette.OFF_TRACK, 10.0F);
      float knobX = x + (enabled ? 16.0F : 3.0F);
      canvas.rect(knobX, y + 2.5F, 15.0F, 15.0F, enabled ? -1 : ClickGuiPalette.OFF_KNOB, 7.5F);
      if (enabled) {
         canvas.texture(knobX + 5.0F, y + 5.5F, 6.0F, 4.493F, Textures.Icons.CHECK, ClickGuiPalette.accent());
      } else {
         canvas.texture(knobX + 4.5F, y + 4.5F, 6.0F, 6.0F, Textures.Icons.X, ClickGuiPalette.OFF_TRACK);
      }
   }

   public boolean mousePressed(ClickGuiCanvas canvas, int button) {
      if (!canvas.hit(94.0F, 65.0F, 821.0F, 562.0F)) {
         this.commitPriceEdit();
         return false;
      }

      boolean left = button == 0;
      boolean right = button == 1;
      if (!left && !right) {
         return true;
      }

      if (!left || !this.grabThumb(canvas, this.catalog) && !this.grabThumb(canvas, this.rules) && !this.grabThumb(canvas, this.options)) {
         AutoBuyFeature feature = AutoBuyFeature.get();
         String server = serverMode();
         if (left) {
            for (ClickGuiAutoBuyPage.Hit hit : this.serverHits) {
               if (hit.contains(canvas)) {
                  String next = SERVERS[hit.index()];
                  if (feature != null && !next.equalsIgnoreCase(server)) {
                     feature.serverMode.setValue(next);
                     if (isHolyWorld(server) != isHolyWorld(next)) {
                        this.selectedTab = 0;
                        this.catalog.reset();
                        this.rules.reset();
                     }

                     this.cancelPriceEdit();
                     this.lastServer = next;
                  }

                  return true;
               }
            }

            for (ClickGuiAutoBuyPage.Hit hit : this.tabHits) {
               if (hit.contains(canvas)) {
                  this.selectedTab = hit.index();
                  this.catalog.reset();
                  return true;
               }
            }
         }

         if (inside(canvas, this.options) && this.pressOptions(canvas, feature, left)) {
            return true;
         }

         if (inside(canvas, this.catalog) && this.pressCatalog(canvas, left)) {
            return true;
         }

         if (inside(canvas, this.rules) && this.pressRules(canvas, left)) {
            return true;
         }

         this.commitPriceEdit();
         return true;
      } else {
         return true;
      }
   }

   private boolean pressOptions(ClickGuiCanvas canvas, AutoBuyFeature feature, boolean left) {
      if (left && this.parseNowHit != null && this.parseNowHit.contains(canvas)) {
         if (feature != null) {
            feature.startParseNow();
         }

         return true;
      } else if (left && this.autoParseHit != null && this.autoParseHit.contains(canvas)) {
         if (feature != null) {
            feature.toggleAutoParse();
         }

         return true;
      } else {
         if (left && this.clearHit != null && this.clearHit.contains(canvas)) {
            AutoBuyManager.get().disableAll(serverMode());
            this.cancelPriceEdit();
            return true;
         }

         if (feature == null) {
            return false;
         }

         for (ClickGuiAutoBuyPage.StepperHit hit : this.stepperHits) {
            if (left && hit.contains(canvas)) {
               step(feature, hit.id(), hit.direction());
               return true;
            }
         }

         for (ClickGuiAutoBuyPage.SettingHit hit : this.settingHits) {
            if (hit.bounds().contains(canvas)) {
               step(feature, hit.id(), left ? 1 : -1);
               return true;
            }
         }

         return false;
      }
   }

   private boolean pressCatalog(ClickGuiCanvas canvas, boolean left) {
      for (ClickGuiAutoBuyPage.ItemHit hit : this.catalogHits) {
         if (hit.contains(canvas)) {
            AutoBuyItem item = hit.item();
            if (left) {
               if (!item.isEnabled()) {
                  AutoBuyManager.get().setEnabled(item, true);
               }
            } else if (item.isEnabled()) {
               AutoBuyManager.get().setEnabled(item, false);
               if (this.editingItem == item) {
                  this.cancelPriceEdit();
               }
            }

            return true;
         }
      }

      return false;
   }

   private boolean pressRules(ClickGuiCanvas canvas, boolean left) {
      for (ClickGuiAutoBuyPage.RuleHit hit : this.ruleHits) {
         AutoBuyItem item = hit.item();
         if (hit.field().contains(canvas)) {
            if (left) {
               this.beginPriceEdit(item);
            }

            return true;
         }

         if (hit.contains(canvas)) {
            if (left) {
               this.beginPriceEdit(item);
            } else {
               AutoBuyManager.get().setEnabled(item, false);
               if (this.editingItem == item) {
                  this.cancelPriceEdit();
               }
            }

            return true;
         }
      }

      return false;
   }

   public void drag(ClickGuiCanvas canvas) {
      ClickGuiAutoBuyPage.Column column = this.draggedColumn;
      if (column != null && !(column.thumbHeight <= 0.0F)) {
         float travel = Math.max(1.0F, column.viewHeight - column.thumbHeight);
         float progress = clamp((canvas.mouseDesignY() - this.dragGrabOffset - column.viewY) / travel, 0.0F, 1.0F);
         column.setScroll(progress * column.maxScroll());
      }
   }

   public void release() {
      this.draggedColumn = null;
   }

   public void scroll(ClickGuiCanvas canvas, double vertical) {
      ClickGuiAutoBuyPage.Column target = this.columnAt(canvas);
      if (target != null) {
         target.setScroll(target.scroll - (float)vertical * 42.0F);
      }
   }

   public boolean keyPressed(int key) {
      if (this.editingItem == null) {
         return false;
      }

      switch (key) {
         case 86:
            if (MenuClipboard.shortcutDown()) {
               this.appendDigits(MenuClipboard.get());
            }
            break;
         case 256:
            this.cancelPriceEdit();
            break;
         case 257:
         case 335:
            this.commitPriceEdit();
            break;
         case 259:
         case 261:
            if (!this.priceDraft.isEmpty()) {
               this.priceDraft = this.priceDraft.substring(0, this.priceDraft.length() - 1);
            }
      }

      return true;
   }

   public boolean charTyped(int codePoint) {
      if (this.editingItem == null) {
         return false;
      }

      if (codePoint >= 48 && codePoint <= 57 && this.priceDraft.length() < 10) {
         this.priceDraft = this.priceDraft + (char)codePoint;
      }

      return true;
   }

   private void appendDigits(String source) {
      if (source != null) {
         for (int index = 0; index < source.length() && this.priceDraft.length() < 10; index++) {
            char character = source.charAt(index);
            if (character >= '0' && character <= '9') {
               this.priceDraft = this.priceDraft + character;
            }
         }
      }
   }

   public boolean isEditing() {
      return this.editingItem != null;
   }

   public String headerTitle() {
      return "AutoBuy";
   }

   public String headerDescription() {
      return MenuText.ui("Buys auction lots below your price limits.");
   }

   private void beginPriceEdit(AutoBuyItem item) {
      this.commitPriceEdit();
      this.editingItem = item;
      this.priceDraft = String.valueOf(item.getBuyPrice());
   }

   private void commitPriceEdit() {
      AutoBuyItem item = this.editingItem;
      if (item != null) {
         String draft = this.priceDraft.trim();
         if (!draft.isEmpty()) {
            try {
               long parsed = Long.parseLong(draft);
               AutoBuyManager.get().setBuyPrice(item, (int)Math.max(1L, Math.min(2147483647L, parsed)));
            } catch (NumberFormatException var5) {
            }
         }

         this.cancelPriceEdit();
      }
   }

   private void cancelPriceEdit() {
      this.editingItem = null;
      this.priceDraft = "";
   }

   private boolean grabThumb(ClickGuiCanvas canvas, ClickGuiAutoBuyPage.Column column) {
      if (!(column.thumbHeight <= 0.0F) && canvas.hit(column.thumbX - 4.0F, column.thumbY, 11.0F, column.thumbHeight)) {
         this.draggedColumn = column;
         this.dragGrabOffset = canvas.mouseDesignY() - column.thumbY;
         return true;
      } else {
         return false;
      }
   }

   private ClickGuiAutoBuyPage.Column columnAt(ClickGuiCanvas canvas) {
      for (ClickGuiAutoBuyPage.Column column : new ClickGuiAutoBuyPage.Column[]{this.catalog, this.rules, this.options}) {
         if (canvas.hit(column.x, 147.0F, column.width, 464.0F)) {
            return column;
         }
      }

      return null;
   }

   private static boolean inside(ClickGuiCanvas canvas, ClickGuiAutoBuyPage.Column column) {
      return canvas.hit(column.x, column.viewY, column.width, column.viewHeight);
   }

   private static ClickGuiAutoBuyPage.Bounds clip(ClickGuiAutoBuyPage.Column column, float x, float y, float width, float height) {
      float top = Math.max(y, column.viewY);
      float bottom = Math.min(y + height, column.viewY + column.viewHeight);
      return bottom - top <= 1.0F ? null : new ClickGuiAutoBuyPage.Bounds(x, top, width, bottom - top);
   }

   private ClickGuiAutoBuyPage.Grid grid(ClickGuiAutoBuyPage.Column column) {
      float available = column.width - 24.0F - 10.0F;
      int columns = Math.max(1, (int)((available + 6.0F) / 46.0F));
      float cell = Math.max(18.0F, (available - 6.0F * (columns - 1)) / columns);
      return new ClickGuiAutoBuyPage.Grid(columns, cell);
   }

   private List<AutoBuyItem> filteredCatalog(String server) {
      AutoBuyItemCategory[] tabs = tabsFor(server);
      int index = Math.max(0, Math.min(this.selectedTab, tabs.length - 1));
      String key = server + "#" + index;
      if (!key.equals(this.cachedCatalogKey)) {
         this.cachedCatalog = tabs[index] == null ? AutoBuyManager.get().itemsForServer(server) : AutoBuyManager.get().byCategoryForServer(tabs[index], server);
         this.cachedCatalogKey = key;
      }

      return this.cachedCatalog;
   }

   private void scissor(ClickGuiCanvas canvas, class_332 graphics, float x, float y, float width, float height) {
      graphics.method_44379(Math.round(canvas.x(x)), Math.round(canvas.y(y)), Math.round(canvas.x(x + width)), Math.round(canvas.y(y + height)));
   }

   private void drawItem(ClickGuiCanvas canvas, class_332 graphics, class_1799 stack, float left, float top, float size) {
      if (stack != null && !stack.method_7960()) {
         Matrix3x2fStack pose = graphics.method_51448();
         pose.pushMatrix();
         pose.translate(canvas.x(left), canvas.y(top));
         pose.scale(canvas.p(size) / 16.0F);

         try {
            graphics.method_51427(stack, 0, 0);
         } catch (Exception var12) {
         } finally {
            pose.popMatrix();
         }
      }
   }

   private static void step(AutoBuyFeature feature, ClickGuiAutoBuyPage.SettingId id, int direction) {
      switch (id) {
         case UPDATE_DELAY:
            nudge(feature.updateDelay, direction * 50.0);
            break;
         case PARSE_DISCOUNT:
            nudge(feature.parseDiscount, direction);
            break;
         case BUY_DELAY:
            nudge(feature.buyDelay, direction * 10.0);
            break;
         case ANARCHY_RANGE:
            nudge(feature.anarchyMinSec, direction * 5.0);
            double min = feature.anarchyMinSec.getValue();
            feature.anarchyMaxSec.setValue(Math.max(min + 5.0, feature.anarchyMaxSec.getValue() + direction * 5.0));
            break;
         case ANARCHY_SWAP:
            feature.anarchySwap.setValue(!feature.anarchySwap.getValue());
            break;
         case SPOOKY_WALK:
            feature.spWalk.setValue(!feature.spWalk.getValue());
            break;
         case SPOOKY_DISTANCE:
            nudge(feature.spWalkBlocks, direction * 0.5);
            break;
         case SPOOKY_INTERVAL:
            nudge(feature.spWalkIntervalSec, direction * 5.0);
            break;
         case AUTO_AH:
            feature.autoOpenAh.setValue(!feature.autoOpenAh.getValue());
            break;
         case REPARSE:
            nudge(feature.reparseMinutes, direction * 5.0);
      }
   }

   private static void nudge(NumberSetting setting, double delta) {
      setting.setValue(setting.getValue() + delta);
   }

   private static float progress(NumberSetting setting) {
      return setting == null ? -1.0F : clamp((float)setting.getProgress(), 0.0F, 1.0F);
   }

   private static String serverMode() {
      AutoBuyFeature feature = AutoBuyFeature.get();
      return feature == null ? "FunTime" : feature.serverMode.getValue();
   }

   private static AutoBuyItemCategory[] tabsFor(String server) {
      return isHolyWorld(server) ? TABS_HOLYWORLD : TABS_FUNTIME;
   }

   private static String[] tabLabelsFor(String server) {
      return isHolyWorld(server) ? TAB_LABELS_HOLYWORLD : TAB_LABELS_FUNTIME;
   }

   private static boolean isHolyWorld(String server) {
      return AutoBuyServer.isHolyFamily(server);
   }

   private static boolean isFunTime(String server) {
      return server != null && server.equalsIgnoreCase("FunTime");
   }

   private static boolean isSpookyTime(String server) {
      return server != null && server.equalsIgnoreCase("SpookyTime");
   }

   private static String shortServer(String server) {
      return AutoBuyServer.shortLabelOf(server);
   }

   private static String onOff(boolean value) {
      return MenuText.ui(value ? "ON" : "OFF");
   }

   private static String trimZero(double value) {
      return value == Math.rint(value) ? String.valueOf((int)value) : String.format(Locale.ROOT, "%.1f", value);
   }

   private static String formatPrice(int price) {
      if (price >= 1000000) {
         return String.format(Locale.ROOT, "%.1fM", price / 1000000.0);
      } else {
         return price >= 10000 ? String.format(Locale.ROOT, "%.1fK", price / 1000.0) : String.valueOf(price);
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

      return value.substring(0, end) + "...";
   }

   private static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   @Environment(EnvType.CLIENT)
   private record Bounds(float x, float y, float width, float height) {
      private boolean contains(ClickGuiCanvas canvas) {
         return canvas.hit(this.x, this.y, this.width, this.height);
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class Column {
      private float x;
      private float width;
      private float viewY;
      private float viewHeight;
      private float content;
      private float scroll;
      private float thumbX;
      private float thumbY;
      private float thumbHeight;

      private void place(float x, float width) {
         this.x = x;
         this.width = width;
      }

      private void view(float viewY, float viewHeight) {
         this.viewY = viewY;
         this.viewHeight = viewHeight;
      }

      private void content(float content) {
         this.content = Math.max(0.0F, content);
         this.clampScroll();
      }

      private float maxScroll() {
         return Math.max(0.0F, this.content - this.viewHeight);
      }

      private void setScroll(float scroll) {
         this.scroll = ClickGuiAutoBuyPage.clamp(scroll, 0.0F, this.maxScroll());
      }

      private void clampScroll() {
         this.scroll = ClickGuiAutoBuyPage.clamp(this.scroll, 0.0F, this.maxScroll());
      }

      private void reset() {
         this.scroll = 0.0F;
      }
   }

   @Environment(EnvType.CLIENT)
   private record Grid(int columns, float cell) {
   }

   @Environment(EnvType.CLIENT)
   private record Hit(float x, float y, float width, float height, int index) {
      private boolean contains(ClickGuiCanvas canvas) {
         return canvas.hit(this.x, this.y, this.width, this.height);
      }
   }

   @Environment(EnvType.CLIENT)
   private record ItemHit(float x, float y, float width, float height, AutoBuyItem item) {
      private boolean contains(ClickGuiCanvas canvas) {
         return canvas.hit(this.x, this.y, this.width, this.height);
      }
   }

   @Environment(EnvType.CLIENT)
   private record RuleHit(
      float x,
      float y,
      float width,
      float height,
      float fieldX,
      float fieldY,
      float fieldWidth,
      float fieldHeight,
      float iconX,
      float iconY,
      float iconBox,
      AutoBuyItem item
   ) {
      private boolean contains(ClickGuiCanvas canvas) {
         return canvas.hit(this.x, this.y, this.width, this.height);
      }

      private ClickGuiAutoBuyPage.Bounds field() {
         return new ClickGuiAutoBuyPage.Bounds(this.fieldX, this.fieldY, this.fieldWidth, this.fieldHeight);
      }
   }

   @Environment(EnvType.CLIENT)
   private record SettingHit(ClickGuiAutoBuyPage.Bounds bounds, ClickGuiAutoBuyPage.SettingId id) {
   }

   @Environment(EnvType.CLIENT)
   private enum SettingId {
      UPDATE_DELAY,
      PARSE_DISCOUNT,
      BUY_DELAY,
      ANARCHY_RANGE,
      ANARCHY_SWAP,
      SPOOKY_WALK,
      SPOOKY_DISTANCE,
      SPOOKY_INTERVAL,
      AUTO_AH,
      REPARSE;
   }

   @Environment(EnvType.CLIENT)
   private record StepperHit(float x, float y, float width, float height, ClickGuiAutoBuyPage.SettingId id, int direction) {
      private boolean contains(ClickGuiCanvas canvas) {
         return canvas.hit(this.x, this.y, this.width, this.height);
      }
   }
}
