package org.ryzen.context;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_757;
import net.minecraft.class_9779;

@Environment(EnvType.CLIENT)
public final class RenderContext {
   public static long overlayStartTime = -1L;
   private static final RenderContext.State2D STATE_2D = new RenderContext.State2D();
   private static final RenderContext.State3D STATE_3D = new RenderContext.State3D();

   private RenderContext() {
   }

   public static void enter2D(class_329 gui, class_332 guiGraphicsExtractor, class_9779 deltaTracker) {
      RenderContext.State2D state = STATE_2D;
      state.active = true;
      state.gui = gui;
      state.guiGraphicsExtractor = guiGraphicsExtractor;
      state.deltaTracker = deltaTracker;
   }

   public static void exit2D() {
      STATE_2D.clear();
   }

   public static boolean isIn2D() {
      return STATE_2D.isActive();
   }

   public static class_332 currentGuiGraphicsExtractor() {
      return STATE_2D.getGuiGraphicsExtractor();
   }

   public static class_329 currentGui() {
      return STATE_2D.getGui();
   }

   public static class_9779 currentDeltaTracker() {
      return STATE_2D.getDeltaTracker();
   }

   public static void enter3D(class_757 gameRenderer, class_9779 deltaTracker) {
      RenderContext.State3D state = STATE_3D;
      state.active = true;
      state.gameRenderer = gameRenderer;
      state.deltaTracker = deltaTracker;
   }

   public static void exit3D() {
      STATE_3D.clear();
   }

   static RenderContext.State2D state2D() {
      return STATE_2D;
   }

   static RenderContext.State3D state3D() {
      return STATE_3D;
   }

   @Environment(EnvType.CLIENT)
   static final class State2D {
      private boolean active;
      private class_329 gui;
      private class_332 guiGraphicsExtractor;
      private class_9779 deltaTracker;

      boolean isActive() {
         return this.active;
      }

      class_329 getGui() {
         return this.gui;
      }

      class_332 getGuiGraphicsExtractor() {
         return this.guiGraphicsExtractor;
      }

      class_9779 getDeltaTracker() {
         return this.deltaTracker;
      }

      private void clear() {
         this.active = false;
         this.gui = null;
         this.guiGraphicsExtractor = null;
         this.deltaTracker = null;
      }
   }

   @Environment(EnvType.CLIENT)
   static final class State3D {
      private boolean active;
      private class_757 gameRenderer;
      private class_9779 deltaTracker;

      boolean isActive() {
         return this.active;
      }

      class_757 getGameRenderer() {
         return this.gameRenderer;
      }

      class_9779 getDeltaTracker() {
         return this.deltaTracker;
      }

      private void clear() {
         this.active = false;
         this.gameRenderer = null;
         this.deltaTracker = null;
      }
   }
}
