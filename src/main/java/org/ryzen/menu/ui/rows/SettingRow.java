package org.ryzen.menu.ui.rows;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.feature.setting.Setting;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.MarqueeText;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public abstract class SettingRow extends Component {
   public static final int ROW_HEIGHT = 40;
   protected static final int PADDING = 16;
   protected static final int WARNING_ICON_SIZE = 12;
   protected static final int WARNING_ICON_GAP = 5;
   protected final Setting<?> setting;
   private final MarqueeText label;
   protected String featureName;
   protected RowHost host;
   protected int rowX;
   protected int rowY;
   protected int rowWidth;
   protected float rowAlpha = 1.0F;
   protected int mouseX;
   protected int mouseY;

   protected SettingRow(Setting<?> setting) {
      this.setting = setting;
      this.label = new MarqueeText(() -> MenuText.setting(this.featureName, this.setting.getName()));
   }

   final SettingRow context(String featureName) {
      this.featureName = featureName;
      return this;
   }

   public final Setting<?> setting() {
      return this.setting;
   }

   public final boolean visible() {
      return this.setting.isVisible();
   }

   public final void place(Component owner, RowHost host, int x, int y, int width, float alpha, int mouseX, int mouseY) {
      this.host = host;
      this.rowX = x;
      this.rowY = y;
      this.rowWidth = width;
      this.rowAlpha = alpha;
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(width), owner.px(40.0F));
      this.placeControl(owner);
      int labelX = this.rowX + 16;
      float labelWidth = Math.max(0.0F, this.labelRight() - labelX);
      this.label.place(owner, labelX, this.rowY, labelWidth, 40.0F, mouseX, mouseY);
   }

   protected abstract void placeControl(Component var1);

   public abstract boolean click(int var1, int var2);

   public boolean key(int key) {
      return false;
   }

   public boolean character(int codePoint) {
      return false;
   }

   public boolean captureMouse(int button) {
      return false;
   }

   public void drag(int mouseX) {
   }

   public boolean scroll(int mouseX, int mouseY, double vertical) {
      return false;
   }

   public void releasePointer() {
   }

   public boolean isCapturingBind() {
      return false;
   }

   public void closeTransient() {
   }

   public void closeTransientImmediately() {
      this.closeTransient();
   }

   public boolean hasOpenPopup() {
      return false;
   }

   public boolean popupClick(int mouseX, int mouseY) {
      return false;
   }

   public void renderPopupOverlay(class_310 minecraft, class_332 guiGraphicsExtractor) {
   }

   @Override
   public final void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.label.style(12.0F, UiFontStyle.MEDIUM, this.labelColor(), this.rowAlpha).render(minecraft, guiGraphicsExtractor);
      Integer warning = warningColor(this.setting);
      if (warning != null && !this.controlRendersWarning()) {
         this.texture(this.warningIconX(), this.rowY + 14, 12.0F, Textures.Icons.TRIANGLE_ALERT, warning, this.rowAlpha);
      }

      this.renderExtras(minecraft, guiGraphicsExtractor);
      this.renderControl(minecraft, guiGraphicsExtractor);
   }

   protected abstract void renderControl(class_310 var1, class_332 var2);

   protected void renderExtras(class_310 minecraft, class_332 guiGraphicsExtractor) {
   }

   protected abstract int controlWidth();

   protected abstract int controlHeight();

   protected final int controlX() {
      return this.rowX + this.rowWidth - 16 - this.controlWidth();
   }

   protected final int controlY() {
      return this.rowY + (40 - this.controlHeight()) / 2;
   }

   protected final int warningIconX() {
      return this.controlX() - 5 - 12;
   }

   protected float labelRight() {
      Integer warning = warningColor(this.setting);
      return warning != null && !this.controlRendersWarning() ? this.warningIconX() - 5 : this.controlX() - 5;
   }

   protected boolean controlRendersWarning() {
      return false;
   }

   protected int labelColor() {
      return Theme.Colors.PRIMARY;
   }

   protected static int controlAccent(Setting<?> setting) {
      Integer warning = warningColor(setting);
      return warning == null ? Theme.getAccent() : warning;
   }

   public static Integer warningColor(Setting<?> setting) {
      return warningColor(setting.warningLevel());
   }

   public static Integer warningColor(Setting.WarningLevel warningLevel) {
      return switch (warningLevel) {
         case NONE -> null;
         case RISK -> Theme.Colors.RISK;
         case EXTRA_RISK -> Theme.Colors.EXTRA_RISK;
      };
   }
}
