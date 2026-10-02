package org.ryzen.feature.setting;

import com.google.gson.JsonElement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class Setting<T> {
   private final String name;
   private final T defaultValue;
   private String configKey;
   private boolean persistent = true;
   private T value;
   private Runnable changeListener = () -> {};
   private BooleanSupplier visibility = () -> true;
   private Double warningRiskValue;
   private Double warningExtraRiskValue;
   private Setting.WarningLevel staticWarningLevel = Setting.WarningLevel.NONE;
   private final Map<String, Setting.WarningLevel> optionWarningLevels = new LinkedHashMap<>();

   protected Setting(String name, T defaultValue) {
      this.name = name;
      this.defaultValue = defaultValue;
      this.configKey = name;
      this.value = defaultValue;
   }

   public final void setValue(T value) {
      T normalized = this.normalize(value);
      if (this.value == null || !this.value.equals(normalized)) {
         this.value = normalized;
         this.changeListener.run();
      }
   }

   public final void reset() {
      this.setValue(this.defaultValue);
   }

   public final void setChangeListener(Runnable changeListener) {
      this.changeListener = changeListener == null ? () -> {} : changeListener;
   }

   public final <S extends Setting<T>> S configKey(String configKey) {
      if (configKey != null && !configKey.isBlank()) {
         this.configKey = configKey;
         return this.self();
      } else {
         throw new IllegalArgumentException("Setting config key cannot be blank");
      }
   }

   public final <S extends Setting<T>> S visibleWhen(BooleanSupplier visibility) {
      this.visibility = visibility == null ? () -> true : visibility;
      return this.self();
   }

   public final <S extends Setting<T>> S nonPersistent() {
      this.persistent = false;
      return this.self();
   }

   public final boolean isVisible() {
      return this.visibility.getAsBoolean();
   }

   public final <S extends Setting<T>> S warning(double riskValue) {
      return this.warning(riskValue, null);
   }

   public final <S extends Setting<T>> S warning(double riskValue, double extraRiskValue) {
      return this.warning(riskValue, Double.valueOf(extraRiskValue));
   }

   public final <S extends Setting<T>> S risk() {
      this.staticWarningLevel = Setting.WarningLevel.RISK;
      return this.self();
   }

   public final <S extends Setting<T>> S risk(String option) {
      this.optionWarningLevels.put(this.normalizeWarningOption(option), Setting.WarningLevel.RISK);
      return this.self();
   }

   public final <S extends Setting<T>> S extraRisk() {
      this.staticWarningLevel = Setting.WarningLevel.EXTRA_RISK;
      return this.self();
   }

   public final <S extends Setting<T>> S extraRisk(String option) {
      this.optionWarningLevels.put(this.normalizeWarningOption(option), Setting.WarningLevel.EXTRA_RISK);
      return this.self();
   }

   public final <S extends Setting<T>> S extrarisk() {
      return this.extraRisk();
   }

   public final <S extends Setting<T>> S extrarisk(String option) {
      return this.extraRisk(option);
   }

   public final Setting.WarningLevel warningLevel() {
      Object current = this.getValue();
      if (current instanceof Number number && this.warningRiskValue != null) {
         double value = number.doubleValue();
         if (this.warningExtraRiskValue != null && value >= this.warningExtraRiskValue) {
            return Setting.WarningLevel.EXTRA_RISK;
         }

         if (value >= this.warningRiskValue) {
            return Setting.WarningLevel.RISK;
         }
      }

      Setting.WarningLevel optionWarningLevel = this.optionWarningLevel(current);
      if (optionWarningLevel != Setting.WarningLevel.NONE) {
         return optionWarningLevel;
      } else {
         return this.staticWarningLevel != Setting.WarningLevel.NONE && this.isStaticWarningActive(current)
            ? this.staticWarningLevel
            : Setting.WarningLevel.NONE;
      }
   }

   public final Setting.WarningLevel warningLevel(String option) {
      return this.optionWarningLevels.getOrDefault(this.normalizeWarningOption(option), Setting.WarningLevel.NONE);
   }

   public final JsonElement write() {
      return this.writeValue(this.value);
   }

   public final void read(JsonElement element) {
      this.setValue(this.readValue(element));
   }

   protected T normalize(T value) {
      return value;
   }

   protected String normalizeWarningOption(String option) {
      return Objects.requireNonNull(option, "option");
   }

   protected abstract JsonElement writeValue(T var1);

   protected abstract T readValue(JsonElement var1);

   private <S extends Setting<T>> S warning(double riskValue, Double extraRiskValue) {
      this.warningRiskValue = riskValue;
      this.warningExtraRiskValue = extraRiskValue;
      return this.self();
   }

   private boolean isStaticWarningActive(Object value) {
      return value instanceof Boolean enabled ? enabled : false;
   }

   private Setting.WarningLevel optionWarningLevel(Object value) {
      if (!this.optionWarningLevels.isEmpty() && value != null) {
         if (value instanceof Collection<?> collection) {
            Setting.WarningLevel highest = Setting.WarningLevel.NONE;

            for (Object option : collection) {
               Setting.WarningLevel warningLevel = this.optionWarningLevel(String.valueOf(option));
               if (warningLevel.ordinal() > highest.ordinal()) {
                  highest = warningLevel;
               }
            }

            return highest;
         } else {
            return this.optionWarningLevel(String.valueOf(value));
         }
      } else {
         return Setting.WarningLevel.NONE;
      }
   }

   private Setting.WarningLevel optionWarningLevel(String option) {
      return this.optionWarningLevels.getOrDefault(this.normalizeWarningOption(option), Setting.WarningLevel.NONE);
   }

   private <S extends Setting<T>> S self() {
      return (S)this;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public T getDefaultValue() {
      return this.defaultValue;
   }

   @Generated
   public String getConfigKey() {
      return this.configKey;
   }

   @Generated
   public boolean isPersistent() {
      return this.persistent;
   }

   @Generated
   public T getValue() {
      return this.value;
   }

   @Generated
   public Runnable getChangeListener() {
      return this.changeListener;
   }

   @Generated
   public BooleanSupplier getVisibility() {
      return this.visibility;
   }

   @Generated
   public Double getWarningRiskValue() {
      return this.warningRiskValue;
   }

   @Generated
   public Double getWarningExtraRiskValue() {
      return this.warningExtraRiskValue;
   }

   @Generated
   public Setting.WarningLevel getStaticWarningLevel() {
      return this.staticWarningLevel;
   }

   @Generated
   public Map<String, Setting.WarningLevel> getOptionWarningLevels() {
      return this.optionWarningLevels;
   }

   @Environment(EnvType.CLIENT)
   public enum WarningLevel {
      NONE,
      RISK,
      EXTRA_RISK;
   }
}
