package org.ryzen.feature.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MultiSelectSetting extends Setting<Set<String>> {
   private final List<String> options;

   public MultiSelectSetting(String name, Collection<String> defaultValue, String... options) {
      super(name, new LinkedHashSet<>(defaultValue));
      if (options != null && options.length != 0) {
         this.options = List.of(options);

         for (String selected : defaultValue) {
            this.ensureOptionExists(selected);
         }
      } else {
         throw new IllegalArgumentException("MultiSelect setting requires at least one option");
      }
   }

   public boolean isSelected(String option) {
      return this.getValue().contains(this.resolveOption(option));
   }

   public void select(String option) {
      LinkedHashSet<String> selected = new LinkedHashSet<>(this.getValue());
      selected.add(this.resolveOption(option));
      this.setValue(selected);
   }

   public void deselect(String option) {
      LinkedHashSet<String> selected = new LinkedHashSet<>(this.getValue());
      selected.remove(this.resolveOption(option));
      this.setValue(selected);
   }

   public void toggle(String option) {
      if (this.isSelected(option)) {
         this.deselect(option);
      } else {
         this.select(option);
      }
   }

   protected Set<String> normalize(Set<String> value) {
      Objects.requireNonNull(value, "selectedOptions");
      LinkedHashSet<String> normalized = new LinkedHashSet<>();

      for (String option : value) {
         normalized.add(this.resolveOption(option));
      }

      return Set.copyOf(normalized);
   }

   @Override
   protected String normalizeWarningOption(String option) {
      return this.resolveOption(option);
   }

   protected JsonElement writeValue(Set<String> value) {
      JsonArray array = new JsonArray();

      for (String option : value) {
         array.add(option);
      }

      return array;
   }

   protected Set<String> readValue(JsonElement element) {
      LinkedHashSet<String> values = new LinkedHashSet<>();

      for (JsonElement entry : element.getAsJsonArray()) {
         values.add(entry.getAsString());
      }

      return values;
   }

   private String resolveOption(String option) {
      this.ensureOptionExists(option);

      for (String candidate : this.options) {
         if (candidate.equalsIgnoreCase(option)) {
            return candidate;
         }
      }

      throw new IllegalArgumentException("Unknown option '" + option + "' for setting " + this.getName());
   }

   private void ensureOptionExists(String option) {
      for (String candidate : this.options) {
         if (candidate.equalsIgnoreCase(option)) {
            return;
         }
      }

      throw new IllegalArgumentException("Unknown option '" + option + "' for setting " + this.getName());
   }

   @Generated
   public List<String> getOptions() {
      return this.options;
   }
}
