package org.ryzen.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.event.EventManager;
import org.ryzen.event.events.lifecycle.FeatureToggleEvent;
import org.ryzen.feature.setting.BindSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.Setting;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public abstract class Feature {
   private final String name;
   private final String description;
   private final FeatureCategory category;
   protected final BindSetting bind;
   protected final ModeSetting bindMode = new ModeSetting("Bind Mode", BindMode.TOGGLE.name(), BindMode.HOLD.name(), BindMode.TOGGLE.name());
   private final List<Setting<?>> settings = new ArrayList<>();
   private final Map<String, Setting<?>> settingsByName = new HashMap<>();
   private boolean enabled;
   private boolean visible = true;
   private Runnable stateListener = () -> {};
   private String legacyName;

   protected Feature(String name, String description, FeatureCategory category, int bind) {
      this.name = name;
      this.description = description;
      this.category = category;
      this.bind = new BindSetting("Bind", bind);
   }

   protected final void renamedFrom(String previousName) {
      this.legacyName = previousName;
   }

   public boolean isToggleable() {
      return true;
   }

   public boolean supportsBinds() {
      return true;
   }

   public final void setEnabled(boolean enabled) {
      if (this.isToggleable()) {
         if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
               EventManager.subscribe(this);

               try {
                  this.onEnable();
               } catch (Throwable throwable) {
                  if (throwable instanceof FeatureEnableRejectedException) {
                     LoggerFactory.getLogger(this.getClass()).warn("{} was not enabled: {}", this.name, throwable.getMessage());
                  } else {
                     LoggerFactory.getLogger(this.getClass()).error("{} failed to enable", this.name, throwable);
                  }

                  this.enabled = false;
                  EventManager.unsubscribe(this);
                  return;
               }
            } else {
               try {
                  this.onDisable();
               } catch (Throwable throwable) {
                  LoggerFactory.getLogger(this.getClass()).error("{} failed to disable cleanly", this.name, throwable);
               }

               EventManager.unsubscribe(this);
            }

            EventManager.call(new FeatureToggleEvent(this, enabled));
            this.stateListener.run();
         }
      }
   }

   public final void toggle() {
      this.setEnabled(!this.enabled);
   }

   public final void setVisible(boolean visible) {
      if (this.visible != visible) {
         this.visible = visible;
         this.onStateChanged();
      }
   }

   protected final <S extends Setting<?>> S register(S setting) {
      Objects.requireNonNull(setting, "setting");
      setting.setChangeListener(this::onStateChanged);
      this.settings.add(setting);
      this.settingsByName.put(setting.getName(), setting);
      return setting;
   }

   public final List<Setting<?>> getSettings() {
      return Collections.unmodifiableList(this.settings);
   }

   public final Setting<?> getSetting(String name) {
      return this.settingsByName.get(name);
   }

   public final void setStateListener(Runnable stateListener) {
      this.stateListener = stateListener == null ? () -> {} : stateListener;
   }

   public final void setBind(int key) {
      if (this.supportsBinds()) {
         this.bind.setSingle(key);
         this.onStateChanged();
      }
   }

   public final void setKeyBind(int key) {
      if (this.supportsBinds()) {
         this.bind.setSingle(BindSetting.key(key));
         this.onStateChanged();
      }
   }

   public final void setMouseBind(int button) {
      if (this.supportsBinds()) {
         this.bind.setSingle(BindSetting.mouse(button));
         this.onStateChanged();
      }
   }

   public final void clearBind() {
      if (this.supportsBinds()) {
         this.bind.clear();
         this.onStateChanged();
      }
   }

   public final int addBind(int bindCode) {
      if (!this.supportsBinds()) {
         return -1;
      }

      int index = this.bind.add(bindCode);
      this.onStateChanged();
      return index;
   }

   public final int addKeyBind(int key) {
      return this.addBind(BindSetting.key(key));
   }

   public final int addMouseBind(int button) {
      return this.addBind(BindSetting.mouse(button));
   }

   public final int setBindAt(int index, int bindCode) {
      if (!this.supportsBinds()) {
         return -1;
      }

      int resolvedIndex = this.bind.setAt(index, bindCode);
      this.onStateChanged();
      return resolvedIndex;
   }

   public final int setKeyBindAt(int index, int key) {
      return this.setBindAt(index, BindSetting.key(key));
   }

   public final int setMouseBindAt(int index, int button) {
      return this.setBindAt(index, BindSetting.mouse(button));
   }

   public final void removeBindAt(int index) {
      if (this.supportsBinds()) {
         this.bind.removeAt(index);
         this.onStateChanged();
      }
   }

   public final boolean hasBindAt(int index) {
      return this.supportsBinds() && this.bind.hasIndex(index);
   }

   public final List<Integer> getBinds() {
      return this.supportsBinds() ? this.bind.getValue() : List.of();
   }

   public final void setBindMode(BindMode mode) {
      if (this.supportsBinds()) {
         this.bindMode.setValue(mode.name());
         this.bind.setAllModes(mode);
         this.onStateChanged();
      }
   }

   public final void setBindModeAt(int index, BindMode mode) {
      if (this.supportsBinds()) {
         this.bind.setMode(index, mode);
         this.onStateChanged();
      }
   }

   public final BindMode getResolvedBindMode() {
      return BindMode.valueOf(this.bindMode.getValue());
   }

   public final BindMode getBindModeAt(int index) {
      return this.bind.getMode(index, this.getResolvedBindMode());
   }

   public final BindMode getBindModeForCode(int bindCode) {
      return this.bind.getModeForCode(bindCode, this.getResolvedBindMode());
   }

   public final boolean isBindVisibleAt(int index) {
      return this.supportsBinds() && this.bind.isVisibleAt(index);
   }

   public final void setBindVisibleAt(int index, boolean visible) {
      if (this.supportsBinds()) {
         this.bind.setVisibleAt(index, visible);
         this.onStateChanged();
      }
   }

   protected void onEnable() {
   }

   protected void onDisable() {
   }

   protected void onStateChanged() {
      this.stateListener.run();
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public FeatureCategory getCategory() {
      return this.category;
   }

   @Generated
   public BindSetting getBind() {
      return this.bind;
   }

   @Generated
   public ModeSetting getBindMode() {
      return this.bindMode;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public boolean isVisible() {
      return this.visible;
   }

   @Generated
   public Runnable getStateListener() {
      return this.stateListener;
   }

   @Generated
   public String getLegacyName() {
      return this.legacyName;
   }
}
