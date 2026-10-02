package org.ryzen.feature.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.text.StringUtil;

@Environment(EnvType.CLIENT)
public final class TextSetting extends Setting<String> {
   private final int maxLength;
   private boolean secret;

   public TextSetting(String name, String defaultValue) {
      this(name, defaultValue, 64);
   }

   public TextSetting(String name, String defaultValue, int maxLength) {
      super(name, StringUtil.abbreviate(defaultValue, Math.max(0, maxLength)));
      this.maxLength = Math.max(0, maxLength);
   }

   public TextSetting secret() {
      this.secret = true;
      return this;
   }

   protected String normalize(String value) {
      return StringUtil.abbreviate(value, this.maxLength);
   }

   protected JsonElement writeValue(String value) {
      return new JsonPrimitive(value);
   }

   protected String readValue(JsonElement element) {
      return element != null && !element.isJsonNull() && element.isJsonPrimitive() ? element.getAsString() : this.getDefaultValue();
   }

   @Generated
   public int getMaxLength() {
      return this.maxLength;
   }

   @Generated
   public boolean isSecret() {
      return this.secret;
   }
}
