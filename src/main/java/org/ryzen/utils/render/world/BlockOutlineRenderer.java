package org.ryzen.utils.render.world;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12075;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2680;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import net.minecraft.class_239.class_240;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.system.MemoryStack;
import org.ryzen.feature.impl.visual.BlockOutlineFeature;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.post.PostFx;
import org.ryzen.utils.render.post.PostPipelines;

@Environment(EnvType.CLIENT)
public final class BlockOutlineRenderer {
   private static final float BOX_EPSILON = 0.0025F;
   private static final int TRANSFORM_SIZE = new Std140SizeCalculator().putMat4f().get();
   private static final int STYLE_SIZE = new Std140SizeCalculator().putVec4().putVec4().get();
   private final GpuBuffer transformUniforms = uniformBuffer("Ryzen Block Outline Transform UBO", TRANSFORM_SIZE);
   private final GpuBuffer styleUniforms = uniformBuffer("Ryzen Block Outline Style UBO", STYLE_SIZE);
   private class_2338 selectedPos;
   private class_2680 selectedState;
   private long lastFrameNanos;
   private float transition;
   private GpuBuffer cachedVertexBuffer;
   private int cachedVertexCount;

   public void render(BlockOutlineFeature feature, class_12075 cameraState) {
      class_310 minecraft = class_310.method_1551();
      if (minecraft.field_1687 != null
         && minecraft.field_1773 != null
         && cameraState != null
         && cameraState.field_63079
         && minecraft.field_1765 instanceof class_3965 hit
         && hit.method_17783() == class_240.field_1332) {
         class_2338 blockPos = hit.method_17777();
         class_2680 state = minecraft.field_1687.method_8320(blockPos);
         if (state.method_26215()) {
            this.resetSelection();
         } else {
            boolean selectionChanged = !blockPos.equals(this.selectedPos);
            if (selectionChanged) {
               this.selectedPos = blockPos.method_10062();
               this.transition = 0.0F;
               this.lastFrameNanos = System.nanoTime();
            }

            this.advanceTransition(feature.animationSpeed.getValue().floatValue());
            if (selectionChanged || state != this.selectedState || this.cachedVertexBuffer == null) {
               this.selectedState = state;
               this.rebuildMesh(minecraft, blockPos, state);
            }

            if (this.cachedVertexBuffer != null && this.cachedVertexCount != 0) {
               class_276 target = minecraft.method_1522();
               if (target != null && target.method_71639() != null && target.method_71640() != null) {
                  float eased = 1.0F - (float)Math.pow(1.0F - this.transition, 3.0);
                  float scale = 0.92F + eased * 0.08F;
                  Matrix4f projection = Render3DUtil.levelProjectionCopy();
                  if (projection != null) {
                     Matrix4f modelViewProjection = projection.mul(new Matrix4f().rotation(new Quaternionf(cameraState.field_63081).conjugate()))
                        .translate(
                           (float)(blockPos.method_10263() - cameraState.field_63078.method_10216() + 0.5),
                           (float)(blockPos.method_10264() - cameraState.field_63078.method_10214() + 0.5),
                           (float)(blockPos.method_10260() - cameraState.field_63078.method_10215() + 0.5)
                        )
                        .scale(scale)
                        .translate(-0.5F, -0.5F, -0.5F);
                     this.writeUniforms(feature, modelViewProjection, target.field_1482, target.field_1481, eased);
                     RenderPass pass = RenderSystem.getDevice()
                        .createCommandEncoder()
                        .createRenderPass(
                           () -> "Ryzen Block Outline", target.method_71639(), OptionalInt.empty(), target.method_71640(), OptionalDouble.empty()
                        );

                     try {
                        pass.setPipeline(this.pipeline(feature));
                        RenderSystem.bindDefaultUniforms(pass);
                        pass.setUniform("BlockOutlineTransform", this.transformUniforms);
                        pass.setUniform("BlockOutlineStyle", this.styleUniforms);
                        pass.setVertexBuffer(0, this.cachedVertexBuffer);
                        pass.draw(0, this.cachedVertexCount);
                     } finally {
                        pass.close();
                     }
                  }
               }
            }
         }
      } else {
         this.resetSelection();
      }
   }

   private void rebuildMesh(class_310 minecraft, class_2338 blockPos, class_2680 state) {
      this.releaseMesh();
      List<class_238> boxes = state.method_26218(minecraft.field_1687, blockPos).method_1090();
      if (boxes.isEmpty()) {
         boxes = List.of(new class_238(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
      }

      class_9801 mesh = this.buildMesh(boxes);
      if (mesh != null) {
         class_9801 var6 = mesh;

         try {
            this.cachedVertexCount = mesh.method_60822().comp_750();
            this.cachedVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Ryzen Block Outline Vertices", 32, mesh.method_60818());
         } catch (Throwable var10) {
            if (var6 != null) {
               try {
                  var6.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (var6 != null) {
            var6.close();
         }
      }
   }

   private void releaseMesh() {
      if (this.cachedVertexBuffer != null) {
         this.cachedVertexBuffer.close();
         this.cachedVertexBuffer = null;
      }

      this.cachedVertexCount = 0;
   }

   public void resetSelection() {
      this.selectedPos = null;
      this.selectedState = null;
      this.transition = 0.0F;
      this.lastFrameNanos = 0L;
      this.releaseMesh();
   }

   public void release() {
      this.resetSelection();
      this.transformUniforms.close();
      this.styleUniforms.close();
   }

   private void advanceTransition(float speed) {
      long now = System.nanoTime();
      if (this.lastFrameNanos == 0L) {
         this.lastFrameNanos = now;
      } else {
         float seconds = Math.min(0.1F, (float)(now - this.lastFrameNanos) / 1.0E9F);
         this.lastFrameNanos = now;
         this.transition = Math.min(1.0F, this.transition + seconds * speed * 0.12F);
      }
   }

   private void writeUniforms(BlockOutlineFeature feature, Matrix4f modelViewProjection, int width, int height, float alpha) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         ByteBuffer transform = Std140Builder.onStack(stack, TRANSFORM_SIZE).putMat4f(modelViewProjection).get();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.transformUniforms.slice(), transform);
         int tint = feature.resolvedTint();
         ByteBuffer style = Std140Builder.onStack(stack, STYLE_SIZE)
            .putVec4(ColorUtil.red(tint) / 255.0F, ColorUtil.green(tint) / 255.0F, ColorUtil.blue(tint) / 255.0F, alpha * 0.82F)
            .putVec4(width, height, PostFx.shaderTime() * feature.shaderSpeed.getValue().floatValue(), feature.shaderIntensity.getValue().floatValue())
            .get();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.styleUniforms.slice(), style);
      } catch (Throwable var11) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
         }

         throw var11;
      }

      if (stack != null) {
         stack.close();
      }
   }

   private RenderPipeline pipeline(BlockOutlineFeature feature) {
      boolean through = feature.ignoreDepth.getValue();

      return switch ((String)feature.variant.getValue()) {
         case "Water Caustics" -> through ? PostPipelines.BLOCK_OUTLINE_CAUSTICS_THROUGH : PostPipelines.BLOCK_OUTLINE_CAUSTICS;
         case "Prismatic Flow" -> through ? PostPipelines.BLOCK_OUTLINE_PRISMATIC_THROUGH : PostPipelines.BLOCK_OUTLINE_PRISMATIC;
         case "Glossy Gradients" -> through ? PostPipelines.BLOCK_OUTLINE_GLOSSY_THROUGH : PostPipelines.BLOCK_OUTLINE_GLOSSY;
         case "Deep Space" -> through ? PostPipelines.BLOCK_OUTLINE_DEEP_SPACE_THROUGH : PostPipelines.BLOCK_OUTLINE_DEEP_SPACE;
         case "Nebula" -> through ? PostPipelines.BLOCK_OUTLINE_NEBULA_THROUGH : PostPipelines.BLOCK_OUTLINE_NEBULA;
         default -> through ? PostPipelines.BLOCK_OUTLINE_CLASSIC_THROUGH : PostPipelines.BLOCK_OUTLINE_CLASSIC;
      };
   }

   private class_9801 buildMesh(List<class_238> boxes) {
      int vertexCount = boxes.size() * 36;
      if (vertexCount == 0) {
         return null;
      }

      class_287 builder = new class_287(
         new class_9799(Math.max(256, vertexCount * class_290.field_1592.getVertexSize())), class_5596.field_27379, class_290.field_1592
      );

      for (class_238 box : boxes) {
         float minX = (float)box.field_1323 - 0.0025F;
         float minY = (float)box.field_1322 - 0.0025F;
         float minZ = (float)box.field_1321 - 0.0025F;
         float maxX = (float)box.field_1320 + 0.0025F;
         float maxY = (float)box.field_1325 + 0.0025F;
         float maxZ = (float)box.field_1324 + 0.0025F;
         this.face(builder, minX, minY, minZ, maxX, maxY, minZ);
         this.face(builder, maxX, minY, maxZ, minX, maxY, maxZ);
         this.face(builder, minX, minY, maxZ, minX, maxY, minZ);
         this.face(builder, maxX, minY, minZ, maxX, maxY, maxZ);
         this.face(builder, minX, maxY, minZ, maxX, maxY, maxZ);
         this.face(builder, minX, minY, maxZ, maxX, minY, minZ);
      }

      return builder.method_60800();
   }

   private void face(class_287 builder, float x1, float y1, float z1, float x2, float y2, float z2) {
      boolean constantX = x1 == x2;
      boolean constantY = y1 == y2;
      if (constantX) {
         this.vertex(builder, x1, y1, z1);
         this.vertex(builder, x1, y1, z2);
         this.vertex(builder, x1, y2, z2);
         this.vertex(builder, x1, y2, z2);
         this.vertex(builder, x1, y2, z1);
         this.vertex(builder, x1, y1, z1);
      } else if (constantY) {
         this.vertex(builder, x1, y1, z1);
         this.vertex(builder, x2, y1, z1);
         this.vertex(builder, x2, y1, z2);
         this.vertex(builder, x2, y1, z2);
         this.vertex(builder, x1, y1, z2);
         this.vertex(builder, x1, y1, z1);
      } else {
         this.vertex(builder, x1, y1, z1);
         this.vertex(builder, x2, y1, z1);
         this.vertex(builder, x2, y2, z1);
         this.vertex(builder, x2, y2, z1);
         this.vertex(builder, x1, y2, z1);
         this.vertex(builder, x1, y1, z1);
      }
   }

   private void vertex(class_287 builder, float x, float y, float z) {
      builder.method_22912(x, y, z);
   }

   private static GpuBuffer uniformBuffer(String label, int size) {
      return RenderSystem.getDevice().createBuffer(() -> label, 136, size);
   }
}
