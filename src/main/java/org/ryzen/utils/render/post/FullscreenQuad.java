package org.ryzen.utils.render.post;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_9799;
import net.minecraft.class_9801;

@Environment(EnvType.CLIENT)
public final class FullscreenQuad {
   private static GpuBuffer buffer;
   private static int vertexCount;

   private FullscreenQuad() {
   }

   public static GpuBuffer buffer() {
      ensure();
      return buffer;
   }

   public static int vertexCount() {
      ensure();
      return vertexCount;
   }

   private static void ensure() {
      if (buffer == null) {
         class_287 builder = new class_287(class_9799.method_72201(6 * class_290.field_1585.getVertexSize()), class_5596.field_27379, class_290.field_1585);
         builder.method_22912(-1.0F, -1.0F, 0.0F).method_22913(0.0F, 0.0F);
         builder.method_22912(1.0F, -1.0F, 0.0F).method_22913(1.0F, 0.0F);
         builder.method_22912(1.0F, 1.0F, 0.0F).method_22913(1.0F, 1.0F);
         builder.method_22912(1.0F, 1.0F, 0.0F).method_22913(1.0F, 1.0F);
         builder.method_22912(-1.0F, 1.0F, 0.0F).method_22913(0.0F, 1.0F);
         builder.method_22912(-1.0F, -1.0F, 0.0F).method_22913(0.0F, 0.0F);
         class_9801 mesh = builder.method_60800();

         try {
            vertexCount = mesh.method_60822().comp_750();
            buffer = RenderSystem.getDevice().createBuffer(() -> "Ryzen fullscreen quad", 32, mesh.method_60818());
         } catch (Throwable var5) {
            if (mesh != null) {
               try {
                  mesh.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (mesh != null) {
            mesh.close();
         }
      }
   }
}
