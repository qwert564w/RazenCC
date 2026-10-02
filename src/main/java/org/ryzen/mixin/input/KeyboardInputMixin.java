package org.ryzen.mixin.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import net.minecraft.class_241;
import net.minecraft.class_743;
import net.minecraft.class_744;
import org.ryzen.event.EventManager;
import org.ryzen.event.events.input.PlayerInputEvent;
import org.ryzen.feature.impl.movement.InventoryMoveFeature;
import org.ryzen.feature.impl.movement.SprintFeature;
import org.ryzen.utils.combat.SprintManager;
import org.ryzen.utils.inventory.InventorySwap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_743.class)
public abstract class KeyboardInputMixin extends class_744 {
   @Inject(method = "method_3129", at = @At("TAIL"))
   private void onPlayerInputTick(CallbackInfo ci) {
      class_10185 screenInput = InventoryMoveFeature.screenInput();
      if (screenInput != null) {
         this.field_54155 = screenInput;
         this.field_55868 = new class_241(impulse(screenInput.comp_3161(), screenInput.comp_3162()), impulse(screenInput.comp_3159(), screenInput.comp_3160()))
            .method_35581();
      }

      if (!this.field_54155.comp_3165() && SprintFeature.shouldForceSprintKey()) {
         this.field_54155 = new class_10185(
            this.field_54155.comp_3159(),
            this.field_54155.comp_3160(),
            this.field_54155.comp_3161(),
            this.field_54155.comp_3162(),
            this.field_54155.comp_3163(),
            this.field_54155.comp_3164(),
            true
         );
      }

      if (SprintManager.shouldFreezeMovementInput()) {
         this.field_55868 = class_241.field_1340;
         this.field_54155 = new class_10185(false, false, false, false, this.field_54155.comp_3163(), this.field_54155.comp_3164(), false);
      }

      if (InventorySwap.shouldStopMovement() || InventoryMoveFeature.shouldStopMovement()) {
         this.field_55868 = class_241.field_1340;
         this.field_54155 = new class_10185(false, false, false, false, false, this.field_54155.comp_3164(), false);
      }

      if (EventManager.hasListeners(PlayerInputEvent.class)) {
         PlayerInputEvent event = EventManager.call(new PlayerInputEvent((class_743)(Object)this, this.field_54155, this.method_3128()));
         this.field_54155 = event.getKeyPresses();
         this.field_55868 = event.getMoveVector();
      }
   }

   private static float impulse(boolean positive, boolean negative) {
      if (positive == negative) {
         return 0.0F;
      } else {
         return positive ? 1.0F : -1.0F;
      }
   }
}
