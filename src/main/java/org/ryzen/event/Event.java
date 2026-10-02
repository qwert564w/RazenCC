package org.ryzen.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class Event {
   private boolean completed;

   public final boolean isCompleted() {
      return this.completed;
   }

   public boolean isCancelled() {
      return false;
   }

   protected void reset() {
      this.completed = false;
   }

   final void complete() {
      this.completed = true;
   }
}
