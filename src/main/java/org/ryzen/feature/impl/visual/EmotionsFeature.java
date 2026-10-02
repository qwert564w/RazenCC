package org.ryzen.feature.impl.visual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.menu.screens.EmotionWheelScreen;
import org.ryzen.utils.FriendManager;
import org.ryzen.utils.render.EmotionAnimator;

@Environment(EnvType.CLIENT)
public final class EmotionsFeature extends Feature implements MinecraftContext {
   private static EmotionsFeature instance;
   public final InputBindSetting wheelKey = this.register(new InputBindSetting("Wheel Key", -1));
   public final BooleanSetting onSelf = this.register(new BooleanSetting("On Self", true));
   public final BooleanSetting onFriends = this.register(new BooleanSetting("On Friends", false));
   public final BooleanSetting onEveryone = this.register(new BooleanSetting("On Everyone", false));
   private String selectedEmotion;
   private String previewEmotion;

   public EmotionsFeature() {
      super("Emotions", "Emote wheel and player poses", FeatureCategory.VISUAL, -1);
      instance = this;
   }

   public static EmotionsFeature getInstance() {
      return instance;
   }

   public static EmotionsFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(EmotionsFeature.class);
   }

   public String getSelectedEmotion() {
      return this.selectedEmotion;
   }

   public String getPreviewEmotion() {
      return this.previewEmotion;
   }

   public boolean appliesTo(class_1657 player) {
      if (player == null || mc.field_1724 == null) {
         return false;
      } else if (player == mc.field_1724) {
         return this.onSelf.getValue();
      } else {
         return this.onEveryone.getValue() ? true : this.onFriends.getValue() && FriendManager.INSTANCE.isFriend(player.method_7334().name());
      }
   }

   @Override
   protected void onDisable() {
      this.clear();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.clear();
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (this.wheelKey.matches(event.getKey())) {
         this.handleWheelInput(event.getAction());
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      if (this.wheelKey.matchesMouse(event.getButton())) {
         this.handleWheelInput(event.getAction());
      }
   }

   private void handleWheelInput(int action) {
      if (this.wheelKey.isBound() && mc.field_1724 != null) {
         if (mc.field_1755 == null || mc.field_1755 instanceof EmotionWheelScreen) {
            if (action == 1) {
               if (!(mc.field_1755 instanceof EmotionWheelScreen)) {
                  mc.method_1507(new EmotionWheelScreen());
               }
            } else if (action == 0 && mc.field_1755 instanceof EmotionWheelScreen wheel) {
               this.commit(wheel.getHoveredSlot());
               this.previewEmotion = null;
               mc.method_1507(null);
            }
         }
      }
   }

   private void commit(int slot) {
      String[] emotions = EmotionAnimator.EMOTIONS;
      if (slot >= 0 && slot < emotions.length) {
         this.selectedEmotion = emotions[slot];
      } else {
         this.selectedEmotion = null;
      }
   }

   public void updatePreview() {
      if (this.isEnabled() && mc.field_1755 instanceof EmotionWheelScreen wheel) {
         int var4 = wheel.getHoveredSlot();
         String[] emotions = EmotionAnimator.EMOTIONS;
         this.previewEmotion = var4 >= 0 && var4 < emotions.length ? emotions[var4] : null;
      } else {
         this.previewEmotion = null;
      }
   }

   private void clear() {
      this.selectedEmotion = null;
      this.previewEmotion = null;
   }
}
