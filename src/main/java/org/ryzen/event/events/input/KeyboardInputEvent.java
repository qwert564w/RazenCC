package org.ryzen.event.events.input;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.event.CancellableEvent;

@Environment(EnvType.CLIENT)
public final class KeyboardInputEvent extends CancellableEvent {
   private final long window;
   private final int key;
   private final int scanCode;
   private final int action;
   private final int modifiers;

   public KeyboardInputEvent(long window, int key, int scanCode, int action, int modifiers) {
      this.window = window;
      this.key = key;
      this.scanCode = scanCode;
      this.action = action;
      this.modifiers = modifiers;
   }

   @Generated
   public long getWindow() {
      return this.window;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public int getScanCode() {
      return this.scanCode;
   }

   @Generated
   public int getAction() {
      return this.action;
   }

   @Generated
   public int getModifiers() {
      return this.modifiers;
   }
}
