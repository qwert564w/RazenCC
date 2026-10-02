package org.ryzen.utils.render.jump;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_6367;

@Environment(EnvType.CLIENT)
final class SceneSnapshot {
   private final String label;
   private class_6367 copy;

   SceneSnapshot(String label) {
      this.label = label;
   }

   class_6367 capture() {
      class_276 mainTarget = class_310.method_1551().method_1522();
      if (mainTarget != null && mainTarget.method_30277() != null && mainTarget.method_71639() != null) {
         if (this.copy == null || this.copy.field_1482 != mainTarget.field_1482 || this.copy.field_1481 != mainTarget.field_1481) {
            if (this.copy != null) {
               this.copy.method_1238();
            }

            this.copy = new class_6367(this.label, mainTarget.field_1482, mainTarget.field_1481, false);
         }

         if (this.copy.method_30277() != null && this.copy.method_71639() != null) {
            RenderSystem.getDevice()
               .createCommandEncoder()
               .copyTextureToTexture(mainTarget.method_30277(), this.copy.method_30277(), 0, 0, 0, 0, 0, mainTarget.field_1482, mainTarget.field_1481);
            return this.copy;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   void release() {
      if (this.copy != null) {
         this.copy.method_1238();
         this.copy = null;
      }
   }
}
