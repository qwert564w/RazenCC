package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_408;
import org.lwjgl.glfw.GLFW;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.pve.MineHelperFeature;
import org.ryzen.feature.impl.pve.PveManagerFeature;
import org.ryzen.feature.setting.ButtonSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.hud.AnarchyElement;
import org.ryzen.hud.ArmorHudElement;
import org.ryzen.hud.CooldownsElement;
import org.ryzen.hud.CoordsElement;
import org.ryzen.hud.HotbarElement;
import org.ryzen.hud.HudElement;
import org.ryzen.hud.KeybindsElement;
import org.ryzen.hud.MineTimerElement;
import org.ryzen.hud.NotificationsElement;
import org.ryzen.hud.PotionsElement;
import org.ryzen.hud.PveStatusElement;
import org.ryzen.hud.QuickUseElement;
import org.ryzen.hud.StaffListElement;
import org.ryzen.hud.TargetElement;
import org.ryzen.hud.WatermarkElement;
import org.ryzen.menu.core.MenuConfigStore;
import org.ryzen.menu.core.MenuOverlay;
import org.ryzen.menu.core.MenuPage;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class HudFeature extends Feature {
   private static final float HUD_SCALE = 1.0F;
   private static final String SIZE_MIGRATION_KEY = "hudScale100Migrated";
   private static final double LEGACY_DEFAULT_SIZE = 70.0;
   private static final class_2960 RESET_ICON = class_2960.method_60654("ryzen:textures/menu/icons/refresh_ccw.svg");
   private static final int RESET_DIVIDER_COLOR = ColorUtil.rgba(255, 255, 255, 25);
   private static final String RESET_LABEL = "Сбросить расположение";
   private static final float SNAP_MARGIN = 12.0F;
   private static final float SNAP_GAP = 6.0F;
   private static final float SNAP_THRESHOLD = 8.0F;
   private static final String WATERMARK = "Watermark";
   private static final String STAFFLIST = "Stafflist";
   private static final String COOLDOWNS = "Cooldowns";
   private static final String EFFECTS = "Effects";
   private static final String COORDINATES = "Coordinates";
   private static final String KEYBINDS = "Keybinds";
   private static final String LEGACY_POTIONS = "Potions";
   private static final String ANARCHY = "Anarchy";
   private static final String TARGET = "Target";
   private static final String NOTIFICATIONS = "Notifications";
   private static final String HOTBAR = "Hotbar";
   private static final String ARMOR_HUD = "ArmorHud";
   private static final String QUICK_USE = "QuickUse";
   public final MultiSelectSetting elements = this.register(
      new MultiSelectSetting(
            "Elements",
            List.of("Watermark", "Stafflist", "Cooldowns", "Effects", "Keybinds", "Anarchy", "Target", "Notifications", "Hotbar", "QuickUse"),
            "Watermark",
            "Stafflist",
            "Cooldowns",
            "Effects",
            "Keybinds",
            "Anarchy",
            "Target",
            "Notifications",
            "Hotbar",
            "QuickUse",
            "Coordinates",
            "ArmorHud",
            "Potions"
         )
         .visibleWhen(() -> false)
   );
   public final NumberSetting size = this.register(new NumberSetting("Size", 100.0, 50.0, 200.0, 1.0, "%").visibleWhen(() -> false));
   public final ButtonSetting openEditor = this.register(new ButtonSetting("Open Editor", "Open", () -> {
      class_310 mc = MinecraftContext.mc;
      if (mc != null && MenuOverlay.isOpen()) {
         MenuOverlay.state().openPage(MenuPage.HUD);
      }
   }));
   private static HudFeature instance;
   private final WatermarkElement watermarkElement = new WatermarkElement();
   private final StaffListElement staffListElement = new StaffListElement();
   private final CooldownsElement cooldownsElement = new CooldownsElement();
   private final CoordsElement coordsElement = new CoordsElement();
   private final KeybindsElement keybindsElement = new KeybindsElement();
   private final PotionsElement potionsElement = new PotionsElement();
   private final AnarchyElement anarchyElement = new AnarchyElement();
   private final TargetElement targetElement = new TargetElement();
   private final NotificationsElement notificationsElement = new NotificationsElement();
   private final HotbarElement hotbarElement = new HotbarElement();
   private final ArmorHudElement armorHudElement = new ArmorHudElement();
   private final QuickUseElement quickUseElement = new QuickUseElement();
   private final PveStatusElement pveStatusElement = new PveStatusElement();
   private final MineTimerElement mineTimerElement = new MineTimerElement();
   private final List<HudElement> allElements = List.of(
      this.watermarkElement,
      this.staffListElement,
      this.cooldownsElement,
      this.coordsElement,
      this.keybindsElement,
      this.potionsElement,
      this.anarchyElement,
      this.targetElement,
      this.notificationsElement,
      this.hotbarElement,
      this.armorHudElement,
      this.quickUseElement,
      this.pveStatusElement,
      this.mineTimerElement
   );
   private HudElement draggedElement;
   private boolean sizeMigrationChecked;
   private boolean figmaLayoutMigrationChecked;
   private boolean restartLayoutMigrationChecked;
   private float resetButtonX;
   private float resetButtonY;
   private float resetButtonWidth;
   private float resetButtonHeight;
   private float activeGuideX = Float.NaN;
   private float activeGuideY = Float.NaN;
   private static final String FIGMA_LAYOUT_MIGRATION_KEY = "hudFigmaLayoutV1";
   private static final String RESTART_LAYOUT_MIGRATION_KEY = "hudRestartLayoutV2";
   private static final String HUD_AUTO_ENABLE_KEY = "hudAutoEnabledV2";

   public HudFeature() {
      super("HUD", "HUD customization settings", FeatureCategory.VISUAL, -1);
      instance = this;
   }

   public static boolean customHotbarActive() {
      return instance != null && instance.isEnabled() && instance.elements.isSelected("Hotbar");
   }

   public static boolean layoutModeActive() {
      return MenuOverlay.isOpen() && MenuOverlay.state().isHudLayoutMode();
   }

   public static float hotbarDecorationOffsetX(class_310 mc) {
      return customHotbarActive() && mc.field_1724 != null && !mc.field_1724.method_7325() ? instance.hotbarElement.decorationOffsetX(mc) : 0.0F;
   }

   public static float hotbarDecorationOffsetY(class_310 mc) {
      return customHotbarActive() && mc.field_1724 != null && !mc.field_1724.method_7325() ? instance.hotbarElement.decorationOffsetY(mc) : 0.0F;
   }

   public static float hotbarDecorationScale(class_310 mc) {
      return customHotbarActive() && mc.field_1724 != null && !mc.field_1724.method_7325() ? instance.hotbarElement.decorationScale(mc) : 1.0F;
   }

   @EventTarget(priority = -100)
   public void onRender2D(Render2DEvent event) {
      class_310 mc = event.getClient();
      if (mc != null && mc.field_1724 != null) {
         this.migrateLegacySize();
         this.migrateFigmaLayout();
         this.migrateRestartLayout();
         float unit = (float)(1.0F / mc.method_22683().method_4495() * this.size.getValue() / 100.0);
         List<HudElement> visible = this.visibleElements();
         boolean editable = mc.field_1755 instanceof class_408 || layoutModeActive();
         boolean snapMode = editable && controlDown(mc);
         if (this.draggedElement != null) {
            if (editable) {
               this.draggedElement.dragTo(mouseX(mc), mouseY(mc));
               if (snapMode) {
                  this.applySnap(mc, visible);
               } else {
                  this.clearSnapGuides();
               }
            } else {
               this.stopDrag(mc);
            }
         } else {
            this.clearSnapGuides();
         }

         if (snapMode) {
            this.drawSnapGrid(mc);
         }

         for (HudElement element : visible) {
            element.render(mc, unit);
         }

         if (snapMode) {
            this.drawResetButton(mc, unit, visible);
         } else {
            this.resetButtonWidth = 0.0F;
            this.resetButtonHeight = 0.0F;
         }
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      class_310 mc = MinecraftContext.mc;
      if (mc != null && event.getButton() == 0) {
         if (event.getAction() == 0) {
            if (this.draggedElement != null) {
               this.stopDrag(mc);
               event.cancel();
            }
         } else if (event.getAction() == 1 && (mc.field_1755 instanceof class_408 || layoutModeActive())) {
            float mouseX = mouseX(mc);
            float mouseY = mouseY(mc);
            if (controlDown(mc) && inside(mouseX, mouseY, this.resetButtonX, this.resetButtonY, this.resetButtonWidth, this.resetButtonHeight)) {
               this.resetPositions();
               event.cancel();
            } else if (!inside(mouseX, mouseY, mc.method_22683().method_4486() - 150.0F, mc.method_22683().method_4502() - 40.0F, 146.0F, 20.0F)) {
               float unit = (float)(1.0F / mc.method_22683().method_4495() * this.size.getValue() / 100.0);

               for (HudElement element : this.visibleElements()) {
                  if (element.headerCloseHit(mouseX, mouseY, unit)) {
                     this.hideElement(element);
                     event.cancel();
                     return;
                  }

                  if (element.startDrag(mouseX, mouseY)) {
                     this.draggedElement = element;
                     event.cancel();
                     return;
                  }
               }
            }
         }
      }
   }

   private void hideElement(HudElement element) {
      if (element == this.staffListElement) {
         this.elements.deselect("Stafflist");
      } else if (element == this.cooldownsElement) {
         this.elements.deselect("Cooldowns");
      } else if (element == this.potionsElement) {
         if (this.elements.isSelected("Effects")) {
            this.elements.deselect("Effects");
         }

         if (this.elements.isSelected("Potions")) {
            this.elements.deselect("Potions");
         }
      } else if (element == this.keybindsElement) {
         this.elements.deselect("Keybinds");
      } else if (element == this.anarchyElement) {
         this.elements.deselect("Anarchy");
      }
   }

   @Override
   protected void onDisable() {
      if (this.draggedElement != null && MinecraftContext.mc != null) {
         this.stopDrag(MinecraftContext.mc);
      }
   }

   public void resetPositions() {
      this.draggedElement = null;
      this.clearSnapGuides();
      MenuConfigStore.resetHudPositions();
      this.allElements.forEach(HudElement::resetPosition);
   }

   private void applySnap(class_310 mc, List<HudElement> visible) {
      HudElement dragged = this.draggedElement;
      if (dragged == null) {
         this.clearSnapGuides();
      } else {
         float screenWidth = mc.method_22683().method_4486();
         float screenHeight = mc.method_22683().method_4502();
         float maxX = Math.max(0.0F, screenWidth - dragged.width());
         float maxY = Math.max(0.0F, screenHeight - dragged.height());
         List<HudFeature.SnapCandidate> xCandidates = new ArrayList<>(20);
         List<HudFeature.SnapCandidate> yCandidates = new ArrayList<>(20);
         addCandidate(xCandidates, Math.min(12.0F, maxX), 12.0F, maxX);
         addCandidate(xCandidates, maxX / 2.0F, screenWidth / 2.0F, maxX);
         addCandidate(xCandidates, Math.max(0.0F, maxX - 12.0F), screenWidth - 12.0F, maxX);
         addCandidate(yCandidates, Math.min(12.0F, maxY), 12.0F, maxY);
         addCandidate(yCandidates, maxY / 2.0F, screenHeight / 2.0F, maxY);
         addCandidate(yCandidates, Math.max(0.0F, maxY - 12.0F), screenHeight - 12.0F, maxY);

         for (HudElement other : visible) {
            if (other != dragged && !(other.width() <= 0.5F) && !(other.height() <= 0.5F)) {
               float otherRight = other.x() + other.width();
               float otherBottom = other.y() + other.height();
               float otherCenterX = other.x() + other.width() / 2.0F;
               float otherCenterY = other.y() + other.height() / 2.0F;
               addCandidate(xCandidates, other.x(), other.x(), maxX);
               addCandidate(xCandidates, otherRight - dragged.width(), otherRight, maxX);
               addCandidate(xCandidates, otherCenterX - dragged.width() / 2.0F, otherCenterX, maxX);
               addCandidate(xCandidates, other.x() - dragged.width() - 6.0F, other.x() - 3.0F, maxX);
               addCandidate(xCandidates, otherRight + 6.0F, otherRight + 3.0F, maxX);
               addCandidate(yCandidates, other.y(), other.y(), maxY);
               addCandidate(yCandidates, otherBottom - dragged.height(), otherBottom, maxY);
               addCandidate(yCandidates, otherCenterY - dragged.height() / 2.0F, otherCenterY, maxY);
               addCandidate(yCandidates, other.y() - dragged.height() - 6.0F, other.y() - 3.0F, maxY);
               addCandidate(yCandidates, otherBottom + 6.0F, otherBottom + 3.0F, maxY);
            }
         }

         float x = Math.clamp(dragged.x(), 0.0F, maxX);
         float y = Math.clamp(dragged.y(), 0.0F, maxY);
         this.activeGuideX = Float.NaN;
         this.activeGuideY = Float.NaN;
         if (!dragged.isHorizontallyCentered()) {
            HudFeature.SnapCandidate snappedX = nearestCandidate(x, xCandidates);
            if (snappedX != null) {
               x = snappedX.position();
               this.activeGuideX = snappedX.guide();
            }
         }

         HudFeature.SnapCandidate snappedY = nearestCandidate(y, yCandidates);
         if (snappedY != null) {
            y = snappedY.position();
            this.activeGuideY = snappedY.guide();
         }

         dragged.snapTo(x, y);
      }
   }

   private static void addCandidate(List<HudFeature.SnapCandidate> candidates, float position, float guide, float maximum) {
      if (Float.isFinite(position) && position >= 0.0F && position <= maximum) {
         candidates.add(new HudFeature.SnapCandidate(position, guide));
      }
   }

   private static HudFeature.SnapCandidate nearestCandidate(float position, List<HudFeature.SnapCandidate> candidates) {
      HudFeature.SnapCandidate nearest = null;
      float nearestDistance = 8.0F;

      for (HudFeature.SnapCandidate candidate : candidates) {
         float distance = Math.abs(position - candidate.position());
         if (distance <= nearestDistance) {
            nearest = candidate;
            nearestDistance = distance;
         }
      }

      return nearest;
   }

   private void drawSnapGrid(class_310 mc) {
      float width = mc.method_22683().method_4486();
      float height = mc.method_22683().method_4502();
      float line = (float)Math.max(0.5, 1.0 / Math.max(1.0, mc.method_22683().method_4495()));
      int thirdsColor = ColorUtil.rgba(190, 195, 205, 36);
      int centerColor = ColorUtil.rgba(205, 209, 216, 58);
      int cornerColor = ColorUtil.rgba(210, 214, 220, 78);
      float left = 12.0F;
      float top = 12.0F;
      float right = width - 12.0F;
      float bottom = height - 12.0F;
      float cornerLength = 10.0F;
      drawDashedVertical(width / 3.0F, top, bottom, 1.5F, 6.0F, line, thirdsColor);
      drawDashedVertical(width * 2.0F / 3.0F, top, bottom, 1.5F, 6.0F, line, thirdsColor);
      drawDashedHorizontal(height / 3.0F, left, right, 1.5F, 6.0F, line, thirdsColor);
      drawDashedHorizontal(height * 2.0F / 3.0F, left, right, 1.5F, 6.0F, line, thirdsColor);
      drawDashedVertical(width / 2.0F, top, bottom, 4.0F, 6.0F, line, centerColor);
      drawDashedHorizontal(height / 2.0F, left, right, 4.0F, 6.0F, line, centerColor);
      Render2DUtil.rect(left, top, cornerLength, line).color(cornerColor).draw();
      Render2DUtil.rect(left, top, line, cornerLength).color(cornerColor).draw();
      Render2DUtil.rect(right - cornerLength, top, cornerLength, line).color(cornerColor).draw();
      Render2DUtil.rect(right - line, top, line, cornerLength).color(cornerColor).draw();
      Render2DUtil.rect(left, bottom - line, cornerLength, line).color(cornerColor).draw();
      Render2DUtil.rect(left, bottom - cornerLength, line, cornerLength).color(cornerColor).draw();
      Render2DUtil.rect(right - cornerLength, bottom - line, cornerLength, line).color(cornerColor).draw();
      Render2DUtil.rect(right - line, bottom - cornerLength, line, cornerLength).color(cornerColor).draw();
      if (Float.isFinite(this.activeGuideX)) {
         Render2DUtil.rect(this.activeGuideX - line * 2.0F, 0.0F, line * 4.0F, height).color(ColorUtil.rgba(205, 210, 218, 30)).draw();
         Render2DUtil.rect(this.activeGuideX - line / 2.0F, 0.0F, line, height).color(ColorUtil.rgba(220, 224, 230, 175)).draw();
      }

      if (Float.isFinite(this.activeGuideY)) {
         Render2DUtil.rect(0.0F, this.activeGuideY - line * 2.0F, width, line * 4.0F).color(ColorUtil.rgba(205, 210, 218, 30)).draw();
         Render2DUtil.rect(0.0F, this.activeGuideY - line / 2.0F, width, line).color(ColorUtil.rgba(220, 224, 230, 175)).draw();
      }
   }

   private static void drawDashedVertical(float x, float top, float bottom, float dash, float gap, float width, int color) {
      float y = top;

      while (y < bottom) {
         Render2DUtil.rect(x - width / 2.0F, y, width, Math.min(dash, bottom - y)).color(color).draw();
         y += dash + gap;
      }
   }

   private static void drawDashedHorizontal(float y, float left, float right, float dash, float gap, float height, int color) {
      float x = left;

      while (x < right) {
         Render2DUtil.rect(x, y - height / 2.0F, Math.min(dash, right - x), height).color(color).draw();
         x += dash + gap;
      }
   }

   private void clearSnapGuides() {
      this.activeGuideX = Float.NaN;
      this.activeGuideY = Float.NaN;
   }

   private void drawResetButton(class_310 mc, float unit, List<HudElement> visible) {
      float height = 28.0F * unit;
      float radius = 8.0F * unit;
      float padding = 8.0F * unit;
      float gap = 4.0F * unit;
      float iconSize = 12.0F * unit;
      float dividerWidth = Math.max(0.5F, 0.5F * unit);
      float textSize = 10.0F * unit;
      MsdfFont font = UiFonts.sfProDisplay();
      float letterSpacing = textSize * UiFontStyle.MEDIUM.letterSpacingEm();
      float textWidth = font.measureWidth("Сбросить расположение", textSize, letterSpacing);
      this.resetButtonWidth = padding * 2.0F + iconSize + gap * 2.0F + dividerWidth + textWidth;
      this.resetButtonHeight = height;
      this.resetButtonX = (mc.method_22683().method_4486() - this.resetButtonWidth) / 2.0F;
      this.resetButtonY = this.resolveResetButtonY(mc, visible);
      float mouseX = mouseX(mc);
      float mouseY = mouseY(mc);
      boolean hovered = inside(mouseX, mouseY, this.resetButtonX, this.resetButtonY, this.resetButtonWidth, this.resetButtonHeight);
      int foreground = hovered ? Theme.Colors.PRIMARY_BRIGHT : Theme.Colors.TEXT_TEXT;
      int iconColor = Theme.getAccent();
      float centerY = this.resetButtonY + height / 2.0F;
      Render2DUtil.rect(this.resetButtonX, this.resetButtonY, this.resetButtonWidth, height)
         .color(Theme.Colors.BACKGROUND_PRIMARY_50)
         .radius(radius)
         .border(Math.max(0.5F, 0.5F * unit), Theme.Colors.OUTLINES_MEDIUM)
         .blur(8.0F * unit)
         .draw();
      float cursor = this.resetButtonX + padding;
      Render2DUtil.texture(cursor, centerY - iconSize / 2.0F, iconSize, iconSize, RESET_ICON).color(iconColor).draw();
      cursor += iconSize + gap;
      Render2DUtil.rect(cursor, centerY - iconSize / 2.0F, dividerWidth, iconSize).color(RESET_DIVIDER_COLOR).draw();
      cursor += dividerWidth + gap;
      Render2DUtil.text(cursor, font.centeredTextY(centerY, textSize), textSize, "Сбросить расположение").style(UiFontStyle.MEDIUM).color(foreground).draw();
   }

   private float resolveResetButtonY(class_310 mc, List<HudElement> visible) {
      float preferredY = 18.0F;
      float minY = 4.0F;
      float maxY = Math.max(minY, mc.method_22683().method_4502() - this.resetButtonHeight - minY);

      for (float offset = 0.0F; offset <= maxY + preferredY; offset += 2.0F) {
         float above = preferredY - offset;
         if (above >= minY && this.resetButtonFits(visible, above)) {
            return above;
         }

         float below = preferredY + offset;
         if (offset > 0.0F && below <= maxY && this.resetButtonFits(visible, below)) {
            return below;
         }
      }

      return Math.min(preferredY, maxY);
   }

   private boolean resetButtonFits(List<HudElement> visible, float y) {
      float gap = 4.0F;

      for (HudElement element : visible) {
         if (element.overlaps(this.resetButtonX - gap, y - gap, this.resetButtonWidth + gap * 2.0F, this.resetButtonHeight + gap * 2.0F)) {
            return false;
         }
      }

      return true;
   }

   private List<HudElement> visibleElements() {
      List<HudElement> visible = new ArrayList<>(13);
      if (this.elements.isSelected("Watermark")) {
         visible.add(this.watermarkElement);
      }

      if (this.elements.isSelected("Stafflist")) {
         visible.add(this.staffListElement);
      }

      if (this.elements.isSelected("Cooldowns")) {
         visible.add(this.cooldownsElement);
      }

      if (this.elements.isSelected("Coordinates")) {
         visible.add(this.coordsElement);
      }

      if (this.elements.isSelected("Keybinds")) {
         visible.add(this.keybindsElement);
      }

      if (this.elements.isSelected("Effects") || this.elements.isSelected("Potions")) {
         visible.add(this.potionsElement);
      }

      if (this.elements.isSelected("Anarchy")) {
         visible.add(this.anarchyElement);
      }

      if (this.elements.isSelected("Target")) {
         visible.add(this.targetElement);
      }

      if (this.elements.isSelected("Notifications")) {
         visible.add(this.notificationsElement);
      }

      if (this.elements.isSelected("Hotbar")) {
         visible.add(this.hotbarElement);
      }

      if (this.elements.isSelected("ArmorHud")) {
         visible.add(this.armorHudElement);
      }

      if (this.elements.isSelected("QuickUse")) {
         visible.add(this.quickUseElement);
      }

      if (PveManagerFeature.INSTANCE.currentState.getValue()) {
         visible.add(this.pveStatusElement);
      }

      MineHelperFeature mineHelper = FeatureManager.INSTANCE.getFeature(MineHelperFeature.class);
      if (mineHelper != null && mineHelper.isMineTimerSelected()) {
         visible.add(this.mineTimerElement);
      }

      return visible;
   }

   private void stopDrag(class_310 mc) {
      this.draggedElement.stopDrag(mc);
      this.draggedElement = null;
      this.clearSnapGuides();
   }

   private static boolean controlDown(class_310 mc) {
      long window = mc.method_22683().method_4490();
      return GLFW.glfwGetKey(window, 341) == 1 || GLFW.glfwGetKey(window, 345) == 1;
   }

   private static float mouseX(class_310 mc) {
      return (float)mc.field_1729.method_68879(mc.method_22683());
   }

   private static float mouseY(class_310 mc) {
      return (float)mc.field_1729.method_68883(mc.method_22683());
   }

   private static boolean inside(float mouseX, float mouseY, float x, float y, float width, float height) {
      return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
   }

   private void migrateLegacySize() {
      if (!this.sizeMigrationChecked) {
         this.sizeMigrationChecked = true;
         if (!MenuConfigStore.getBoolean("hudScale100Migrated", false)) {
            if (this.size.getValue() == 70.0) {
               this.size.setValue(100.0);
            }

            MenuConfigStore.save(data -> data.addProperty("hudScale100Migrated", true));
         }
      }
   }

   private void migrateFigmaLayout() {
      if (!this.figmaLayoutMigrationChecked) {
         this.figmaLayoutMigrationChecked = true;
         if (!MenuConfigStore.getBoolean("hudFigmaLayoutV1", false)) {
            this.size.setValue(100.0);
            this.elements
               .setValue(
                  new LinkedHashSet<>(
                     List.of("Watermark", "Stafflist", "Cooldowns", "Effects", "Keybinds", "Anarchy", "Target", "Notifications", "Hotbar", "QuickUse")
                  )
               );
            this.resetPositions();
            MenuConfigStore.save(data -> data.addProperty("hudFigmaLayoutV1", true));
         }
      }
   }

   private void migrateRestartLayout() {
      if (!this.restartLayoutMigrationChecked) {
         this.restartLayoutMigrationChecked = true;
         if (!MenuConfigStore.getBoolean("hudRestartLayoutV2", false)) {
            MenuConfigStore.save(data -> {
               data.addProperty("hud.watermark.x", 0.0F);
               data.addProperty("hud.watermark.y", 0.0F);
               data.addProperty("hud.staff_list.x", 0.82858187F);
               data.addProperty("hud.staff_list.y", 0.025517695F);
               data.addProperty("hud.keybinds.x", 0.19468462F);
               data.addProperty("hud.keybinds.y", 0.16013743F);
               data.addProperty("hud.potions.x", 0.0F);
               data.addProperty("hud.potions.y", 0.3166936F);
               data.addProperty("hud.anarchy.x", 1.0F);
               data.addProperty("hud.anarchy.y", 0.43665725F);
               data.addProperty("hud.target.x", 0.35572535F);
               data.addProperty("hud.target.y", 0.5107142F);
               data.addProperty("hud.notifications.x", 1.0F);
               data.addProperty("hud.notifications.y", 1.0F);
               data.addProperty("hud.hotbar.x", 0.4808188F);
               data.addProperty("hud.hotbar.y", 1.0F);
               data.addProperty("hud.quick_use.x", 0.30206758F);
               data.addProperty("hud.quick_use.y", 0.88670355F);
               data.addProperty("hud.coords.x", 0.0F);
               data.addProperty("hud.coords.y", 1.0F);
               data.addProperty("hud.armor_hud.x", 0.60420966F);
               data.addProperty("hud.armor_hud.y", 1.0F);
               data.addProperty("hud.mine_timer.x", 0.50977F);
               data.addProperty("hud.mine_timer.y", 0.7515891F);
               data.addProperty("hudRestartLayoutV2", true);
            });
            this.allElements.forEach(HudElement::resetPosition);
         }
      }
   }

   public void enableByDefaultOnce() {
      if (!MenuConfigStore.getBoolean("hudAutoEnabledV2", false)) {
         if (!this.isEnabled()) {
            this.setEnabled(true);
         }

         MenuConfigStore.save(data -> data.addProperty("hudAutoEnabledV2", true));
      }
   }

   @Environment(EnvType.CLIENT)
   private record SnapCandidate(float position, float guide) {
   }
}
