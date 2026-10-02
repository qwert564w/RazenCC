package org.ryzen.feature.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ModeSetting extends Setting<String> {
   private final List<String> modes;
   private final Map<String, String> renamed = new HashMap<>();
   private boolean chips;

   public ModeSetting(String name, String defaultValue, String... modes) {
      super(name, defaultValue);
      if (modes != null && modes.length != 0) {
         this.modes = List.copyOf(new ArrayList<>(List.of(modes)));
         if (!this.containsMode(defaultValue)) {
            throw new IllegalArgumentException("Default mode must exist in modes: " + defaultValue);
         }
      } else {
         throw new IllegalArgumentException("Mode setting requires at least one mode");
      }
   }

   public ModeSetting chips() {
      this.chips = true;
      return this;
   }

   public boolean is(String mode) {
      return this.getValue().equalsIgnoreCase(mode);
   }

   public void cycle() {
      int nextIndex = (this.indexOf(this.getValue()) + 1) % this.modes.size();
      this.setValue(this.modes.get(nextIndex));
   }

   public int indexOf(String mode) {
      for (int i = 0; i < this.modes.size(); i++) {
         if (this.modes.get(i).equalsIgnoreCase(mode)) {
            return i;
         }
      }

      return -1;
   }

   public boolean containsMode(String mode) {
      return this.indexOf(mode) != -1;
   }

   protected String normalize(String value) {
      Objects.requireNonNull(value, "mode");
      int index = this.indexOf(value);
      if (index == -1) {
         throw new IllegalArgumentException("Unknown mode '" + value + "' for setting " + this.getName());
      } else {
         return this.modes.get(index);
      }
   }

   @Override
   protected String normalizeWarningOption(String option) {
      Objects.requireNonNull(option, "mode");
      int index = this.indexOf(option);
      if (index == -1) {
         throw new IllegalArgumentException("Unknown mode '" + option + "' for setting " + this.getName());
      } else {
         return this.modes.get(index);
      }
   }

   protected JsonElement writeValue(String value) {
      return new JsonPrimitive(value);
   }

   public ModeSetting renamedFrom(String previousName, String currentName) {
      if (!this.containsMode(currentName)) {
         throw new IllegalArgumentException("Unknown mode '" + currentName + "' for setting " + this.getName());
      }

      this.renamed.put(previousName.toLowerCase(Locale.ROOT), currentName);
      return this;
   }

   protected String readValue(JsonElement element) {
      String value = element.getAsString();
      int index = this.indexOf(value);
      if (index == -1) {
         String current = this.renamed.get(value.toLowerCase(Locale.ROOT));
         index = current == null ? -1 : this.indexOf(current);
      }

      return index == -1 ? this.getDefaultValue() : this.modes.get(index);
   }

   @Generated
   public List<String> getModes() {
      return this.modes;
   }

   @Generated
   public Map<String, String> getRenamed() {
      return this.renamed;
   }

   @Generated
   public boolean isChips() {
      return this.chips;
   }
}
