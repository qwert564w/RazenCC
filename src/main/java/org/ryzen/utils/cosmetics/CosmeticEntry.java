package org.ryzen.utils.cosmetics;

import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;

@Environment(EnvType.CLIENT)
public final class CosmeticEntry {
   private final Path folder;
   private final String id;
   private final String displayName;
   private final CosmeticEntry.Kind kind;
   private class_2960 preview;
   private boolean previewLoaded;
   private int previewWidth;
   private int previewHeight;

   CosmeticEntry(Path folder, String id, String displayName, CosmeticEntry.Kind kind) {
      this.folder = folder;
      this.id = id;
      this.displayName = displayName;
      this.kind = kind;
   }

   public Path folder() {
      return this.folder;
   }

   public String id() {
      return this.id;
   }

   public String displayName() {
      return this.displayName;
   }

   public CosmeticEntry.Kind kind() {
      return this.kind;
   }

   public class_2960 preview() {
      return this.preview;
   }

   public int previewWidth() {
      return this.previewWidth;
   }

   public int previewHeight() {
      return this.previewHeight;
   }

   boolean previewLoaded() {
      return this.previewLoaded;
   }

   void setPreview(class_2960 preview, int width, int height) {
      this.preview = preview;
      this.previewWidth = width;
      this.previewHeight = height;
      this.previewLoaded = true;
   }

   @Environment(EnvType.CLIENT)
   public enum Kind {
      MODEL("Models"),
      HEAD("Head"),
      WEAPON("Weapons");

      private final String tabName;

      Kind(String tabName) {
         this.tabName = tabName;
      }

      public String tabName() {
         return this.tabName;
      }
   }
}
