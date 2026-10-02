package org.ryzen.utils.render.post;

import com.mojang.blaze3d.textures.TextureFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_6367;

@Environment(EnvType.CLIENT)
public final class PostTarget {
   private final String label;
   private final TextureFormat format;
   private final boolean useDepth;
   private class_6367 target;

   public PostTarget(String label, TextureFormat format, boolean useDepth) {
      this.label = label;
      this.format = format;
      this.useDepth = useDepth;
   }

   public class_6367 ensure(int width, int height) {
      if (this.target == null || this.target.field_1482 != width || this.target.field_1481 != height) {
         if (this.target != null) {
            this.target.method_1238();
         }

         this.target = new class_6367(this.label, Math.max(1, width), Math.max(1, height), this.useDepth);
      }

      return this.target;
   }

   public class_6367 get() {
      return this.target;
   }

   public void release() {
      if (this.target != null) {
         this.target.method_1238();
         this.target = null;
      }
   }
}
