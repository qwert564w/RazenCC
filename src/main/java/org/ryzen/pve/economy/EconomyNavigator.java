package org.ryzen.pve.economy;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;
import net.minecraft.class_746;
import org.ryzen.feature.impl.pve.PveManagerFeature;
import org.ryzen.pve.navigation.NavigationOptions;
import org.ryzen.pve.navigation.Navigator;

@Environment(EnvType.CLIENT)
public final class EconomyNavigator {
   private final Navigator navigator;
   private class_2338 goal;
   private boolean begun;

   public EconomyNavigator(Navigator navigator) {
      this.navigator = Objects.requireNonNull(navigator, "navigator");
   }

   public boolean moveTo(class_746 player, class_2338 target, int radius) {
      if (player != null && target != null) {
         if (arrived(player, target, Math.max(1.5, radius + 0.75))) {
            this.cancel();
            return true;
         }

         if (!this.navigator.isAvailable()) {
            return false;
         }

         if (!this.begun) {
            this.navigator.begin(PveManagerFeature.INSTANCE.configureNavigation(NavigationOptions.walking()));
            this.begun = true;
         }

         class_2338 immutable = target.method_10062();
         if (!immutable.equals(this.goal) || !this.navigator.isPathing()) {
            this.goal = immutable;
            this.navigator.pathTo(immutable, Math.max(1, radius));
         }

         return false;
      } else {
         return false;
      }
   }

   public void cancel() {
      if (this.begun) {
         this.navigator.cancel();
      }

      this.goal = null;
   }

   public void close() {
      if (this.begun) {
         this.navigator.end();
      }

      this.begun = false;
      this.goal = null;
   }

   public static boolean arrived(class_746 player, class_2338 target, double distance) {
      return player != null
         && target != null
         && target.method_10268(player.method_23317(), player.method_23318(), player.method_23321()) <= distance * distance;
   }
}
