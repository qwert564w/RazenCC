package org.ryzen.event.events.screen;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_437;
import org.ryzen.event.CancellableEvent;

@Environment(EnvType.CLIENT)
public final class ScreenKeyEvent extends CancellableEvent {
   private class_437 screen;
   private class_11908 keyEvent;
   private ScreenKeyEvent.Action action;

   public ScreenKeyEvent set(class_437 screen, class_11908 keyEvent, ScreenKeyEvent.Action action) {
      this.screen = screen;
      this.keyEvent = keyEvent;
      this.action = action;
      return this;
   }

   @Generated
   public class_437 getScreen() {
      return this.screen;
   }

   @Generated
   public class_11908 getKeyEvent() {
      return this.keyEvent;
   }

   @Generated
   public ScreenKeyEvent.Action getAction() {
      return this.action;
   }

   @Environment(EnvType.CLIENT)
   public enum Action {
      PRESS,
      RELEASE;
   }
}
