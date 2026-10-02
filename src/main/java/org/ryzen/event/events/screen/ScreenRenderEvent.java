package org.ryzen.event.events.screen;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.ryzen.event.CancellableEvent;

@Environment(EnvType.CLIENT)
public final class ScreenRenderEvent extends CancellableEvent {
   private class_437 screen;
   private class_332 guiGraphicsExtractor;
   private int mouseX;
   private int mouseY;
   private float partialTick;

   public ScreenRenderEvent set(class_437 screen, class_332 guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
      this.screen = screen;
      this.guiGraphicsExtractor = guiGraphicsExtractor;
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.partialTick = partialTick;
      return this;
   }

   @Generated
   public class_437 getScreen() {
      return this.screen;
   }

   @Generated
   public class_332 getGuiGraphicsExtractor() {
      return this.guiGraphicsExtractor;
   }

   @Generated
   public int getMouseX() {
      return this.mouseX;
   }

   @Generated
   public int getMouseY() {
      return this.mouseY;
   }

   @Generated
   public float getPartialTick() {
      return this.partialTick;
   }
}
