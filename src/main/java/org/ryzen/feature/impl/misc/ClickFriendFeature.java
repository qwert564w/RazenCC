package org.ryzen.feature.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.utils.FriendManager;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class ClickFriendFeature extends Feature implements MinecraftContext {
   public final InputBindSetting key = this.register(new InputBindSetting("Key", -1));

   public ClickFriendFeature() {
      super("ClickFriend", "Adds the player you are looking at to your friends with one key", FeatureCategory.MISC, -1);
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (event.getAction() == 1 && this.key.matches(event.getKey())) {
         this.toggleFriend();
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      if (event.getAction() == 1 && this.key.matchesMouse(event.getButton())) {
         this.toggleFriend();
      }
   }

   private void toggleFriend() {
      if (mc.field_1692 instanceof class_1657 player) {
         String name = player.method_7334().name();
         if (FriendManager.INSTANCE.isFriend(name)) {
            FriendManager.INSTANCE.remove(name);
            ChatUtil.info(name + " удалён из списка друзей");
         } else {
            FriendManager.INSTANCE.add(name);
            ChatUtil.success(name + " добавлен в список друзей");
         }
      }
   }
}
