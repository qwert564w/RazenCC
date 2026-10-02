package org.ryzen.menu.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class MenuCard extends Component {
   public static final int WIDTH = 320;
   public static final int HEIGHT = 57;
   protected static final int RADIUS = 8;
   protected static final int PADDING_X = 16;
   protected static final int AVATAR_SIZE = 32;
   protected static final int ACTION_BOX = 24;
   protected static final int ACTION_ICON = 16;
   protected static final int ACTION_GAP = 4;
   protected static final float AVATAR_Y = 12.5F;
   protected int designX;
   protected int designY;
   protected int mouseX;
   protected int mouseY;
   protected float alpha = 1.0F;

   protected final void placeBounds(Component owner, int x, int y, int mouseX, int mouseY, float alpha) {
      this.designX = x;
      this.designY = y;
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.alpha = alpha;
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(320.0F), owner.px(57.0F));
   }

   protected final int actionSlotX(int indexFromRight) {
      return 280 - indexFromRight * 28;
   }

   protected final float actionRowY() {
      return this.designY + 16.5F;
   }

   protected final boolean cardHovered() {
      return this.contains(this.mouseX, this.mouseY);
   }
}
