package org.ryzen.event.events.input;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.event.CancellableEvent;

@Environment(EnvType.CLIENT)
public final class CharacterInputEvent extends CancellableEvent {
   private final long window;
   private final int codePoint;

   public CharacterInputEvent(long window, int codePoint) {
      this.window = window;
      this.codePoint = codePoint;
   }

   @Generated
   public long getWindow() {
      return this.window;
   }

   @Generated
   public int getCodePoint() {
      return this.codePoint;
   }
}
