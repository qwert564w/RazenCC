package org.ryzen.menu.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class CardGrid {
   public static final int COLUMNS = 3;
   private static final int GUTTER = 16;
   private final SmoothScroll scroll = new SmoothScroll();
   private final int listTop;
   private final int cardWidth;
   private final int rowStep;
   private final int viewportBottom;
   private float offset;

   public CardGrid(int listTop, int cardWidth, int rowStep, int viewportBottom) {
      this.listTop = listTop;
      this.cardWidth = cardWidth;
      this.rowStep = rowStep;
      this.viewportBottom = viewportBottom;
   }

   public void update(int cardCount) {
      this.offset = this.scroll.update(this.maxScroll(cardCount));
   }

   public int x(int index) {
      return 16 + index % 3 * (this.cardWidth + 16);
   }

   public int y(int index) {
      return this.listTop + index / 3 * this.rowStep - Math.round(this.offset);
   }

   public void scroll(double vertical, int cardCount) {
      this.scroll.scroll(vertical, this.maxScroll(cardCount));
   }

   private int maxScroll(int cardCount) {
      int rows = (cardCount + 3 - 1) / 3;
      return Math.max(0, this.listTop + rows * this.rowStep - this.viewportBottom);
   }
}
