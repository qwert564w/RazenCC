package org.ryzen.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.pve.MineHelperFeature;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.pve.mining.MineTimer;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class MineTimerElement extends HudElement {
   private static final float PADDING_X = 9.0F;
   private static final float PADDING_Y = 7.0F;
   private static final float TEXT_SIZE = 9.0F;
   private static final float LINE_HEIGHT = 12.0F;
   private String mineText;
   private String timeText;

   public MineTimerElement() {
      super("mine_timer", "Mine Timer");
   }

   @Override
   protected float defaultX(float unit) {
      return 280.0F * unit;
   }

   @Override
   protected float defaultY(float unit) {
      return 24.0F * unit;
   }

   @Override
   protected boolean preservePositionOnContentResize() {
      return true;
   }

   @Override
   protected void layout(class_310 mc, float unit) {
      MineHelperFeature helper = FeatureManager.INSTANCE.getFeature(MineHelperFeature.class);
      MineTimer timer = helper == null ? null : helper.getCurrentTimer().orElse(null);
      boolean configured = helper != null && helper.isMineTimerSelected();
      if ((!configured || timer == null) && !showcase(mc)) {
         this.width = 0.0F;
         this.height = 0.0F;
      } else {
         this.mineText = MenuText.ui("Next mine") + ": " + (timer == null ? MenuText.ui("Diamond") : timer.nextType());
         this.timeText = MenuText.ui("Time left") + ": " + (timer == null ? "01:30" : timer.formattedTime(System.currentTimeMillis()));
         MsdfFont font = UiFonts.sfProDisplay();
         float textSize = 9.0F * unit;
         float spacing = textSize * UiFontStyle.MEDIUM.letterSpacingEm();
         this.width = Math.max(font.measureWidth(this.mineText, textSize, spacing), font.measureWidth(this.timeText, textSize, spacing)) + 18.0F * unit;
         this.height = 38.0F * unit;
      }
   }

   @Override
   protected void draw(class_310 mc, float unit) {
      float alpha = this.appearAlpha();
      this.drawPanel(13.0F * unit, unit, alpha);
      MsdfFont font = UiFonts.sfProDisplay();
      float textSize = 9.0F * unit;
      float firstCenterY = this.y + 13.0F * unit;
      Render2DUtil.text(this.x + 9.0F * unit, font.centeredTextY(firstCenterY, textSize), textSize, this.mineText)
         .style(UiFontStyle.MEDIUM)
         .color(ColorUtil.multiplyAlpha(Theme.getAccent(), alpha))
         .draw();
      Render2DUtil.text(this.x + 9.0F * unit, font.centeredTextY(firstCenterY + 12.0F * unit, textSize), textSize, this.timeText)
         .style(UiFontStyle.MEDIUM)
         .color(ColorUtil.multiplyAlpha(Theme.Colors.TEXT_TEXT, alpha))
         .draw();
   }
}
