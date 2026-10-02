package org.ryzen.event.events.screen;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11909;
import net.minecraft.class_437;
import org.ryzen.event.CancellableEvent;

@Environment(EnvType.CLIENT)
public final class ScreenMouseButtonEvent extends CancellableEvent {
   private class_437 screen;
   private class_11909 mouseButtonEvent;
   private ScreenMouseButtonEvent.Action action;
   private double dragX;
   private double dragY;

   public ScreenMouseButtonEvent set(class_437 screen, class_11909 mouseButtonEvent, ScreenMouseButtonEvent.Action action, double dragX, double dragY) {
      this.screen = screen;
      this.mouseButtonEvent = mouseButtonEvent;
      this.action = action;
      this.dragX = dragX;
      this.dragY = dragY;
      return this;
   }

   @Generated
   public class_437 getScreen() {
      return this.screen;
   }

   @Generated
   public class_11909 getMouseButtonEvent() {
      return this.mouseButtonEvent;
   }

   @Generated
   public ScreenMouseButtonEvent.Action getAction() {
      return this.action;
   }

   @Generated
   public double getDragX() {
      return this.dragX;
   }

   @Generated
   public double getDragY() {
      return this.dragY;
   }

   @Environment(EnvType.CLIENT)
   public enum Action {
      CLICK,
      RELEASE,
      DRAG;
   }
}
