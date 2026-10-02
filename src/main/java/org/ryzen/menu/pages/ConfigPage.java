package org.ryzen.menu.pages;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.core.MenuPage;
import org.ryzen.menu.ui.PageComponent;

@Environment(EnvType.CLIENT)
public final class ConfigPage extends PageComponent {
   private MenuPage displayedPage = MenuPage.NONE;

   @Override
   protected void onLayout() {
      this.displayedPage = this.state.displayPage();
   }

   public void handleScroll(int mouseX, int mouseY, double vertical) {
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      return this.contentContains(mouseX, mouseY);
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      if (this.displayedPage == MenuPage.CONFIGURATIONS && !(this.progress <= 0.001F)) {
         this.pageHeader("Configurations", "Load, save, and manage your client presets.");
         this.emptyState(468.0F, "There are no configs here yet", "Coming in one of the next updates, maybe.");
      }
   }
}
