package org.ryzen.feature.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ButtonSetting extends Setting<Boolean> {
   private final String buttonLabel;
   private final Runnable action;

   public ButtonSetting(String name, String buttonLabel, Runnable action) {
      super(name, false);
      this.buttonLabel = buttonLabel;
      this.action = action == null ? () -> {} : action;
      this.nonPersistent();
   }

   public String getButtonLabel() {
      return this.buttonLabel;
   }

   public void press() {
      this.action.run();
   }

   protected JsonElement writeValue(Boolean value) {
      return new JsonPrimitive(false);
   }

   protected Boolean readValue(JsonElement element) {
      return false;
   }
}
