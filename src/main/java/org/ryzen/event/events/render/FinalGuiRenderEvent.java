package org.ryzen.event.events.render;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.ryzen.event.Event;

@Environment(EnvType.CLIENT)
public final class FinalGuiRenderEvent extends Event {
   private class_310 client;
   private class_329 gui;
   private class_332 guiGraphicsExtractor;
   private class_9779 deltaTracker;
   private boolean renderHud;
   private boolean renderScreen;
   private int mouseX;
   private int mouseY;

   public FinalGuiRenderEvent set(
      class_310 client, class_329 gui, class_332 guiGraphicsExtractor, class_9779 deltaTracker, boolean renderHud, boolean renderScreen, int mouseX, int mouseY
   ) {
      this.client = client;
      this.gui = gui;
      this.guiGraphicsExtractor = guiGraphicsExtractor;
      this.deltaTracker = deltaTracker;
      this.renderHud = renderHud;
      this.renderScreen = renderScreen;
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      return this;
   }

   @Generated
   public class_310 getClient() {
      return this.client;
   }

   @Generated
   public class_329 getGui() {
      return this.gui;
   }

   @Generated
   public class_332 getGuiGraphicsExtractor() {
      return this.guiGraphicsExtractor;
   }

   @Generated
   public class_9779 getDeltaTracker() {
      return this.deltaTracker;
   }

   @Generated
   public boolean isRenderHud() {
      return this.renderHud;
   }

   @Generated
   public boolean isRenderScreen() {
      return this.renderScreen;
   }

   @Generated
   public int getMouseX() {
      return this.mouseX;
   }

   @Generated
   public int getMouseY() {
      return this.mouseY;
   }
}
