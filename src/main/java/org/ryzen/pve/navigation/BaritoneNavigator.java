package org.ryzen.pve.navigation;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2248;
import net.minecraft.class_2338;

@Environment(EnvType.CLIENT)
public final class BaritoneNavigator implements Navigator {
   public static final BaritoneNavigator INSTANCE = new BaritoneNavigator();
   private static final String IMPL = "org.ryzen.pve.navigation.BaritoneNavigatorImpl";
   private static final String PROVIDER = "baritone.BaritoneProvider";
   private final Navigator delegate = loadDelegate();

   private BaritoneNavigator() {
   }

   private static Navigator loadDelegate() {
      ClassLoader loader = BaritoneNavigator.class.getClassLoader();

      try {
         Class.forName("baritone.BaritoneProvider", false, loader);
      } catch (ClassNotFoundException | LinkageError ignored) {
         return null;
      }

      try {
         return (Navigator)Class.forName("org.ryzen.pve.navigation.BaritoneNavigatorImpl", true, loader).getDeclaredConstructor().newInstance();
      } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
         return null;
      }
   }

   @Override
   public boolean isAvailable() {
      return this.delegate != null && this.delegate.isAvailable();
   }

   @Override
   public void begin(NavigationOptions options) {
      if (this.delegate != null) {
         this.delegate.begin(options);
      }
   }

   @Override
   public void pathTo(class_2338 position, int radius) {
      if (this.delegate != null) {
         this.delegate.pathTo(position, radius);
      }
   }

   @Override
   public void mine(int quantity, class_2248... blocks) {
      if (this.delegate != null) {
         this.delegate.mine(quantity, blocks);
      }
   }

   @Override
   public void setMineBounds(class_2338 min, class_2338 max) {
      if (this.delegate != null) {
         this.delegate.setMineBounds(min, max);
      }
   }

   @Override
   public void setMineRenderColor(int argb) {
      if (this.delegate != null) {
         this.delegate.setMineRenderColor(argb);
      }
   }

   @Override
   public void setMineAoeLevel(int level) {
      if (this.delegate != null) {
         this.delegate.setMineAoeLevel(level);
      }
   }

   @Override
   public boolean isPathing() {
      return this.delegate != null && this.delegate.isPathing();
   }

   @Override
   public boolean isMining() {
      return this.delegate != null && this.delegate.isMining();
   }

   @Override
   public List<class_2338> miningTargets() {
      return this.delegate == null ? List.of() : this.delegate.miningTargets();
   }

   @Override
   public Optional<Double> estimatedTicksToGoal() {
      return this.delegate == null ? Optional.empty() : this.delegate.estimatedTicksToGoal();
   }

   @Override
   public Optional<class_2338> currentGoal() {
      return this.delegate == null ? Optional.empty() : this.delegate.currentGoal();
   }

   @Override
   public String diagnostics() {
      return this.delegate == null ? "Baritone is not installed" : this.delegate.diagnostics();
   }

   @Override
   public void cancel() {
      if (this.delegate != null) {
         this.delegate.cancel();
      }
   }

   @Override
   public void end() {
      if (this.delegate != null) {
         this.delegate.end();
      }
   }
}
