package org.ryzen.event.events.input;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import net.minecraft.class_241;
import net.minecraft.class_743;
import org.ryzen.event.Event;

@Environment(EnvType.CLIENT)
public final class PlayerInputEvent extends Event {
   private final class_743 keyboardInput;
   private class_241 moveVector;
   private class_10185 keyPresses;

   public PlayerInputEvent(class_743 keyboardInput, class_10185 keyPresses, class_241 moveVector) {
      this.keyboardInput = keyboardInput;
      this.keyPresses = keyPresses;
      this.moveVector = moveVector;
   }

   public void setShift(boolean shift) {
      class_10185 in = this.keyPresses;
      this.keyPresses = new class_10185(in.comp_3159(), in.comp_3160(), in.comp_3161(), in.comp_3162(), in.comp_3163(), shift, in.comp_3165());
   }

   public void setJump(boolean jump) {
      class_10185 in = this.keyPresses;
      this.keyPresses = new class_10185(in.comp_3159(), in.comp_3160(), in.comp_3161(), in.comp_3162(), jump, in.comp_3164(), in.comp_3165());
   }

   public void setSprint(boolean sprint) {
      class_10185 in = this.keyPresses;
      this.keyPresses = new class_10185(in.comp_3159(), in.comp_3160(), in.comp_3161(), in.comp_3162(), in.comp_3163(), in.comp_3164(), sprint);
   }

   public void setDirections(boolean forward, boolean backward, boolean left, boolean right) {
      class_10185 in = this.keyPresses;
      this.keyPresses = new class_10185(forward, backward, left, right, in.comp_3163(), in.comp_3164(), in.comp_3165());
   }

   public void setMoveVector(class_241 moveVector) {
      this.moveVector = moveVector == null ? class_241.field_1340 : moveVector;
   }

   public void clearMovement(boolean keepJump, boolean keepShift) {
      class_10185 in = this.keyPresses;
      this.keyPresses = new class_10185(false, false, false, false, keepJump && in.comp_3163(), keepShift && in.comp_3164(), false);
      this.moveVector = class_241.field_1340;
   }

   @Generated
   public class_743 getKeyboardInput() {
      return this.keyboardInput;
   }

   @Generated
   public class_241 getMoveVector() {
      return this.moveVector;
   }

   @Generated
   public class_10185 getKeyPresses() {
      return this.keyPresses;
   }
}
