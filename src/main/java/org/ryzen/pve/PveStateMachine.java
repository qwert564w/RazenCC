package org.ryzen.pve;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class PveStateMachine<S extends Enum<S>> {
   private final S initialState;
   private S state;
   private long enteredTick;
   private long transitionCount;

   public PveStateMachine(S initialState) {
      this.initialState = Objects.requireNonNull(initialState, "initialState");
      this.state = initialState;
   }

   public S state() {
      return this.state;
   }

   public long enteredTick() {
      return this.enteredTick;
   }

   public long transitionCount() {
      return this.transitionCount;
   }

   public long ticksInState(long currentTick) {
      return Math.max(0L, currentTick - this.enteredTick);
   }

   public boolean is(S expected) {
      return this.state == expected;
   }

   public boolean transition(S next, long currentTick) {
      Objects.requireNonNull(next, "next");
      if (this.state == next) {
         return false;
      }

      this.state = next;
      this.enteredTick = currentTick;
      this.transitionCount++;
      return true;
   }

   public void reset(long currentTick) {
      this.state = this.initialState;
      this.enteredTick = currentTick;
      this.transitionCount = 0L;
   }
}
