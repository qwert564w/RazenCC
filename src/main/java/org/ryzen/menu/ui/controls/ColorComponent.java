package org.ryzen.menu.ui.controls;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.ui.Component;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;

@Environment(EnvType.CLIENT)
public final class ColorComponent extends Component {
   private static final long ANIMATION_MS = 150L;
   private static final long COLOR_ANIMATION_MS = 180L;
   private static final int[] PALETTE = new int[]{
      -1,
      -1250068,
      -2763307,
      -4079167,
      -5395027,
      -6710887,
      -8026747,
      -9342607,
      -10658467,
      -12105913,
      -13421773,
      -16777216,
      -16762804,
      -16769446,
      -15465156,
      -13892294,
      -12777446,
      -10680317,
      -10937344,
      -10996992,
      -10994175,
      -10067456,
      -11513086,
      -14271475,
      -16626074,
      -16699013,
      -15005103,
      -12186789,
      -11268570,
      -8318976,
      -8640256,
      -8697087,
      -8890112,
      -7437054,
      -9538293,
      -13019365,
      -16683377,
      -16629334,
      -13694598,
      -10413699,
      -8840646,
      -4974336,
      -5489152,
      -5674493,
      -5931772,
      -3884544,
      -6445555,
      -11699932,
      -16610377,
      -16558374,
      -12904556,
      -8773730,
      -6740911,
      -2022656,
      -2469376,
      -2915323,
      -2843390,
      -463872,
      -4009960,
      -10183370,
      -16736041,
      -16621314,
      -11722318,
      -6739010,
      -4706467,
      -49133,
      -104190,
      -21247,
      -145661,
      -1217,
      -2495175,
      -8996032,
      -16529668,
      -12810497,
      -10604310,
      -4179726,
      -1688453,
      -40367,
      -96694,
      -19904,
      -13507,
      -67987,
      -1773466,
      -6696094,
      -11479303,
      -9000705,
      -7974658,
      -2992129,
      -1019492,
      -95103,
      -22914,
      -14217,
      -9863,
      -198254,
      -1314415,
      -5120632,
      -7085313,
      -5716225,
      -5141508,
      -1993474,
      -744257,
      -19024,
      -146004,
      -9814,
      -138330,
      -840,
      -854343,
      -3348297,
      -3411713,
      -2825217,
      -2307585,
      -1062659,
      -470305,
      -140584,
      -73001,
      -201517,
      -3371,
      -1315,
      -590885,
      -2036011
   };
   private final IntSupplier colorSupplier;
   private final IntConsumer colorConsumer;
   private final Animation openAnimation = new Animation(150L, Animation.Easing.EASE_OUT_QUAD);
   private final Animation colorAnimation = new Animation(180L, Animation.Easing.EASE_OUT_QUAD);
   private float alpha = 1.0F;
   private int mouseX;
   private int mouseY;
   private boolean open;
   private int displayedColor;
   private int colorFrom;
   private int colorTo;

   public ColorComponent(IntSupplier colorSupplier, IntConsumer colorConsumer) {
      this.colorSupplier = colorSupplier;
      this.colorConsumer = colorConsumer;
      int initial = colorSupplier.getAsInt();
      this.displayedColor = initial;
      this.colorFrom = initial;
      this.colorTo = initial;
      this.openAnimation.animate(0.0F, 0.0F, 0L, Animation.Easing.EASE_OUT_QUAD);
      this.colorAnimation.animate(1.0F, 1.0F, 0L, Animation.Easing.EASE_OUT_QUAD);
   }

   public ColorComponent place(Component owner, int x, int y, int width, int height) {
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(width), owner.px(height));
      return this;
   }

   public ColorComponent alpha(float alpha) {
      this.alpha = alpha;
      return this;
   }

   public ColorComponent mouse(int mouseX, int mouseY) {
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      return this;
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (!this.contains(mouseX, mouseY)) {
         return false;
      }

      if (this.open) {
         this.close();
      } else {
         this.open = true;
         this.openAnimation.animate(0.0F, 1.0F, 150L, Animation.Easing.EASE_OUT_QUAD);
      }

      return true;
   }

   public boolean handlePopupClick(int mouseX, int mouseY) {
      if (!this.open) {
         return false;
      }

      int index = this.colorIndex(mouseX, mouseY);
      if (index >= 0) {
         this.setColor(PALETTE[index]);
         this.close();
         return true;
      }

      if (this.contains(mouseX, mouseY)) {
         return false;
      }

      if (this.containsPopup(mouseX, mouseY)) {
         return true;
      }

      this.close();
      return false;
   }

   public void close() {
      if (this.open) {
         this.openAnimation.animate(this.openAnimation.getValue(), 0.0F, 120L, Animation.Easing.EASE_OUT_QUAD);
      }

      this.open = false;
   }

   public void closeImmediately() {
      this.open = false;
      this.openAnimation.animate(0.0F, 0.0F, 0L, Animation.Easing.EASE_OUT_QUAD);
   }

   public boolean isOpen() {
      return this.open;
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.updateDisplayedColor();
      float padding = this.px(2.0F);
      float outlineRadius = this.px(6.0F);
      float innerRadius = this.px(4.0F);
      Render2DUtil.rect(this.x(), this.y(), this.width(), this.height())
         .color(ColorUtil.multiplyAlpha(Theme.Colors.OUTLINES_SMALL, this.alpha))
         .radius(outlineRadius)
         .draw();
      float innerWidth = Math.max(0.0F, this.width() - padding * 2.0F);
      float innerHeight = Math.max(0.0F, this.height() - padding * 2.0F);
      Render2DUtil.rect(this.x() + padding, this.y() + padding, innerWidth, innerHeight)
         .color(ColorUtil.multiplyAlpha(this.displayedColor, this.alpha))
         .radius(innerRadius)
         .draw();
   }

   public void renderOverlay() {
      float progress = this.openAnimation.getValue();
      if (!(progress <= 0.001F)) {
         float popupAlpha = this.alpha * progress;
         float lift = -6.0F * (1.0F - progress);
         float popupX = this.popupX();
         float popupY = this.popupY() + lift;
         float popupWidth = this.popupWidth();
         float popupHeight = this.popupHeight();
         this.glassPanel(popupX, popupY, popupWidth, popupHeight, this.px(12.0F), this.px(40.0F), popupAlpha);
         float gridX = this.gridX();
         float gridY = this.gridY() + lift;
         float gridWidth = this.gridWidth();
         float gridHeight = this.gridHeight();
         if (!(gridWidth <= 0.0F) && !(gridHeight <= 0.0F)) {
            int selectedIndex = this.selectedIndex();
            int hoveredIndex = this.open ? this.colorIndex(this.mouseX, this.mouseY) : -1;
            if (hoveredIndex == selectedIndex) {
               hoveredIndex = -1;
            }

            Render2DUtil.colorGrid(gridX, gridY, this.cellSize(), 12, PALETTE)
               .radius(Math.min(this.cellSize(), this.px(10.0F)))
               .color(ColorUtil.multiplyAlpha(-1, popupAlpha))
               .draw();
            if (hoveredIndex >= 0) {
               this.renderCellOutline(hoveredIndex, this.px(1.0F), popupAlpha, lift);
            }

            if (selectedIndex >= 0) {
               this.renderCellOutline(selectedIndex, this.px(2.0F), popupAlpha, lift);
            }
         }
      }
   }

   private void setColor(int color) {
      this.colorConsumer.accept(color);
      this.colorFrom = this.displayedColor;
      this.colorTo = color;
      this.colorAnimation.animate(0.0F, 1.0F, 180L, Animation.Easing.EASE_OUT_QUAD);
   }

   private void updateDisplayedColor() {
      int target = this.colorSupplier.getAsInt();
      if (ColorUtil.withAlpha(target, 255) != ColorUtil.withAlpha(this.colorTo, 255)) {
         this.colorFrom = this.displayedColor;
         this.colorTo = target;
         this.colorAnimation.animate(0.0F, 1.0F, 180L, Animation.Easing.EASE_OUT_QUAD);
      }

      this.displayedColor = ColorUtil.lerp(this.colorFrom, this.colorTo, this.colorAnimation.getValue());
   }

   private int selectedIndex() {
      int current = ColorUtil.withAlpha(this.colorSupplier.getAsInt(), 255);

      for (int index = 0; index < PALETTE.length; index++) {
         if (ColorUtil.withAlpha(PALETTE[index], 255) == current) {
            return index;
         }
      }

      return -1;
   }

   private int colorIndex(int mouseX, int mouseY) {
      float gridX = this.gridX();
      float gridY = this.gridY();
      float gridWidth = this.gridWidth();
      float gridHeight = this.gridHeight();
      if (!(mouseX < gridX) && !(mouseX >= gridX + gridWidth) && !(mouseY < gridY) && !(mouseY >= gridY + gridHeight)) {
         float cell = this.cellSize();
         int column = (int)((mouseX - gridX) / cell);
         int row = (int)((mouseY - gridY) / cell);
         int index = row * 12 + column;
         return index >= 0 && index < PALETTE.length ? index : -1;
      } else {
         return -1;
      }
   }

   private boolean containsPopup(int mouseX, int mouseY) {
      float popupX = this.popupX();
      float popupY = this.popupY();
      return mouseX >= popupX && mouseX <= popupX + this.popupWidth() && mouseY >= popupY && mouseY <= popupY + this.popupHeight();
   }

   private float[] cellBounds(int index, float lift) {
      int column = index % 12;
      int row = index / 12;
      float gridX = this.gridX();
      float gridY = this.gridY() + lift;
      float cell = this.cellSize();
      return new float[]{gridX + column * cell, gridY + row * cell, cell, cell};
   }

   private void renderCellOutline(int index, float thickness, float popupAlpha, float lift) {
      int[] outlineColors = new int[PALETTE.length];
      outlineColors[index] = -1;
      Render2DUtil.colorGrid(this.gridX(), this.gridY() + lift, this.cellSize(), 12, outlineColors)
         .radius(Math.min(this.cellSize(), this.px(10.0F)))
         .color(ColorUtil.multiplyAlpha(-1, popupAlpha))
         .draw();
      float[] cell = this.cellBounds(index, lift);
      float innerSize = Math.max(0.0F, cell[2] - thickness * 2.0F);
      if (!(innerSize <= 0.0F)) {
         Render2DUtil.RectBuilder inner = Render2DUtil.rect(cell[0] + thickness, cell[1] + thickness, innerSize, innerSize)
            .color(ColorUtil.multiplyAlpha(PALETTE[index], popupAlpha));
         float[] radii = this.innerCornerRadii(index, thickness, innerSize);
         inner.radius(radii[0], radii[1], radii[2], radii[3]).draw();
      }
   }

   private float[] innerCornerRadii(int index, float thickness, float innerSize) {
      int column = index % 12;
      int row = index / 12;
      int lastColumn = 11;
      int lastRow = PALETTE.length / 12 - 1;
      float radius = Math.min(innerSize, Math.max(0.0F, Math.min(this.cellSize(), this.px(10.0F)) - thickness));
      return new float[]{
         column == 0 && row == 0 ? radius : 0.0F,
         column == lastColumn && row == 0 ? radius : 0.0F,
         column == lastColumn && row == lastRow ? radius : 0.0F,
         column == 0 && row == lastRow ? radius : 0.0F
      };
   }

   private float popupX() {
      return this.x() + this.width() - this.popupWidth();
   }

   private float popupY() {
      return this.y() + this.height() + this.px(4.0F);
   }

   private float popupWidth() {
      return this.gridWidth() + this.px(6.0F) * 2.0F;
   }

   private float popupHeight() {
      return this.gridHeight() + this.px(6.0F) * 2.0F;
   }

   private float gridX() {
      return this.popupX() + this.px(6.0F);
   }

   private float gridY() {
      return this.popupY() + this.px(6.0F);
   }

   private float gridWidth() {
      return this.cellSize() * 12.0F;
   }

   private float gridHeight() {
      return this.cellSize() * (PALETTE.length / 12);
   }

   private float cellSize() {
      return this.px(11.0F);
   }
}
