package org.ryzen.menu;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.context.RenderContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.render.FinalGuiRenderEvent;
import org.ryzen.menu.core.MenuAppearance;
import org.ryzen.menu.core.MenuOverlay;
import org.ryzen.utils.render.gui.Render2DUtil;

@Environment(EnvType.CLIENT)
public final class MenuOverlayRenderHandler {
   @EventTarget
   public void onFinalGuiRender(FinalGuiRenderEvent event) {
      class_310 minecraft = event.getClient();
      RenderContext.enter2D(event.getGui(), event.getGuiGraphicsExtractor(), event.getDeltaTracker());

      try {
         int screenWidth = minecraft.method_22683().method_4486();
         int screenHeight = minecraft.method_22683().method_4502();
         Render2DUtil.beginFrame();
         Render2DUtil.setBackdropBlurScale(MenuAppearance.glassBlurScale());
         MenuOverlay.render(minecraft, event.getGuiGraphicsExtractor(), screenWidth, screenHeight);
         Render2DUtil.flush();
      } finally {
         RenderContext.exit2D();
      }
   }
}
