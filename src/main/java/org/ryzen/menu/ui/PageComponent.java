package org.ryzen.menu.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.menu.core.MenuOverlayState;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public abstract class PageComponent extends Component {
   protected MenuOverlayState state;
   protected int mouseX;
   protected int mouseY;
   protected float progress;

   public final void layout(Component frame, MenuOverlayState state, int mouseX, int mouseY) {
      this.state = state;
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.progress = state.contentProgress();
      float slide = Math.round(36.0F * (1.0F - this.progress));
      this.attach(frame, frame.x() + frame.px(slide), frame.y(), frame.width(), frame.height());
      this.onLayout();
   }

   protected abstract void onLayout();

   protected final boolean contentContains(float mouseX, float mouseY) {
      return this.progress >= 0.75F && this.contains(mouseX, mouseY) && mouseY >= this.y() + this.px(54.0F);
   }

   protected final void pageHeader(String title, String subtitle) {
      this.text(32.0F, 96.0F, 24.0F, MenuText.ui(title), -1, this.progress, UiFontStyle.SEMIBOLD);
      this.text(32.0F, 134.0F, 16.0F, MenuText.ui(subtitle), Theme.Colors.SECONDARY, this.progress, UiFontStyle.MEDIUM);
   }

   protected final void emptyState(float centerY, String title, String subtitle) {
      float titleHeight = UiFonts.sfProDisplay().textHeight(16.0F);
      float descHeight = UiFonts.sfProDisplay().textHeight(12.0F);
      float gap = 8.0F;
      float blockTop = centerY - (titleHeight + gap + descHeight) / 2.0F;
      this.textCentered(512.0F, blockTop, 16.0F, MenuText.ui(title), Theme.Colors.PRIMARY, this.progress, UiFontStyle.MEDIUM);
      this.textCentered(512.0F, blockTop + titleHeight + gap, 12.0F, MenuText.ui(subtitle), Theme.Colors.SECONDARY, this.progress, UiFontStyle.REGULAR);
   }
}
