package org.ryzen.utils.render.particles;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import com.mojang.blaze3d.vertex.VertexFormatElement.Type;
import com.mojang.blaze3d.vertex.VertexFormatElement.Usage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_12137;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_6367;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.post.PostFx;

@Environment(EnvType.CLIENT)
public final class ProceduralParticleRenderer {
   private static final int VERTEX_STRIDE = 64;
   private static final int UNIFORM_SIZE = new Std140SizeCalculator().putVec4().putVec4().get();
   private static final VertexFormatElement POS_SIZE = allocateFloat4Element(3);
   private static final VertexFormatElement DYNAMICS = allocateFloat4Element(4);
   private static final VertexFormatElement PARTICLE_COLOR = allocateFloat4Element(5);
   private static final VertexFormatElement PARAMS = allocateFloat4Element(6);
   private static final VertexFormat PARTICLE_FORMAT = VertexFormat.builder()
      .add("PosSize", POS_SIZE)
      .add("Dynamics", DYNAMICS)
      .add("Color", PARTICLE_COLOR)
      .add("Params", PARAMS)
      .build();
   private static final RenderPipeline PIPELINE = RenderPipeline.builder(new Snippet[0])
      .withLocation(class_2960.method_60654("ryzen:pipeline/world/procedural_particles"))
      .withVertexShader(class_2960.method_60654("ryzen:core/procedural_particle"))
      .withFragmentShader(class_2960.method_60654("ryzen:core/procedural_particle"))
      .withUniform("Projection", class_10789.field_60031)
      .withSampler("DepthSampler")
      .withUniform("ProceduralParticleUniforms", class_10789.field_60031)
      .withBlend(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA)
      .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
      .withDepthWrite(false)
      .withVertexFormat(PARTICLE_FORMAT, class_5596.field_27379)
      .withCull(false)
      .build();
   private class_6367 depthCopy;

   private static synchronized VertexFormatElement allocateFloat4Element(int uvIndex) {
      for (int id = 31; id >= 7; id--) {
         if (VertexFormatElement.byId(id) == null) {
            return VertexFormatElement.register(id, uvIndex, Type.FLOAT, Usage.UV, 4);
         }
      }

      throw new IllegalStateException("No free vertex attributes for Ryzen procedural particles");
   }

   public static RenderPipeline pipeline() {
      return PIPELINE;
   }

   public void render(List<ProceduralParticleRenderer.Sprite> sprites, float glow, boolean natural) {
      class_310 minecraft = class_310.method_1551();
      if (!sprites.isEmpty() && minecraft.field_1687 != null) {
         class_276 target = minecraft.method_1522();
         if (target != null
            && target.method_71639() != null
            && target.method_30278() != null
            && target.method_71640() != null
            && target.field_1482 > 0
            && target.field_1481 > 0) {
            this.ensureDepthCopy(target.field_1482, target.field_1481);
            if (this.depthCopy != null && this.depthCopy.method_30278() != null && this.depthCopy.method_71640() != null) {
               RenderSystem.getDevice()
                  .createCommandEncoder()
                  .copyTextureToTexture(target.method_30278(), this.depthCopy.method_30278(), 0, 0, 0, 0, 0, target.field_1482, target.field_1481);
               ByteBuffer vertices = this.buildVertices(minecraft, sprites);
               GpuBuffer vertexBuffer = null;
               GpuBuffer uniforms = null;

               try {
                  vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Ryzen Procedural Particle Vertices", 40, vertices);
                  uniforms = this.uploadUniforms(target.field_1482, target.field_1481, Math.clamp(glow, 0.0F, 1.0F), natural);
                  class_12137 depthSampler = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
                  RenderPass pass = RenderSystem.getDevice()
                     .createCommandEncoder()
                     .createRenderPass(
                        () -> "Ryzen Procedural Particles", target.method_71639(), OptionalInt.empty(), target.method_71640(), OptionalDouble.empty()
                     );

                  try {
                     pass.setPipeline(PIPELINE);
                     pass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
                     pass.setUniform("ProceduralParticleUniforms", uniforms);
                     pass.bindTexture("DepthSampler", this.depthCopy.method_71640(), depthSampler);
                     pass.setVertexBuffer(0, vertexBuffer);
                     pass.draw(0, sprites.size() * 6);
                  } catch (Throwable var18) {
                     if (pass != null) {
                        try {
                           pass.close();
                        } catch (Throwable var17) {
                           var18.addSuppressed(var17);
                        }
                     }

                     throw var18;
                  }

                  if (pass != null) {
                     pass.close();
                  }
               } finally {
                  MemoryUtil.memFree(vertices);
                  if (uniforms != null) {
                     uniforms.close();
                  }

                  if (vertexBuffer != null) {
                     vertexBuffer.close();
                  }
               }
            }
         }
      }
   }

   private ByteBuffer buildVertices(class_310 minecraft, List<ProceduralParticleRenderer.Sprite> sprites) {
      ByteBuffer data = MemoryUtil.memAlloc(sprites.size() * 6 * 64).order(ByteOrder.nativeOrder());
      class_4184 camera = minecraft.field_1773.method_19418();
      class_243 cameraPosition = camera.method_71156();
      Matrix4f viewPose = Render3DUtil.cameraViewPose(camera);

      for (ProceduralParticleRenderer.Sprite sprite : sprites) {
         Vector4f position = Render3DUtil.toViewSpace(sprite.position(), cameraPosition, viewPose);
         ProceduralParticleRenderer.Shape shape = sprite.shape() == ProceduralParticleRenderer.Shape.RANDOM
            ? ProceduralParticleRenderer.Shape.STAR
            : sprite.shape();
         int color = sprite.color();

         for (int vertex = 0; vertex < 6; vertex++) {
            putVec4(data, position.x, position.y, position.z, sprite.halfSize());
            putVec4(data, sprite.rotation(), Math.clamp(sprite.normalizedLife(), 0.0F, 1.0F), sprite.seed(), sprite.phase());
            putVec4(data, ColorUtil.red(color) / 255.0F, ColorUtil.green(color) / 255.0F, ColorUtil.blue(color) / 255.0F, ColorUtil.alpha(color) / 255.0F);
            putVec4(data, shape.shapeId, shape.additivity, shape.glow, 0.0F);
         }
      }

      return data.flip();
   }

   private GpuBuffer uploadUniforms(int width, int height, float glow, boolean natural) {
      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Ryzen Procedural Particle UBO", 136, UNIFORM_SIZE);
      MemoryStack stack = MemoryStack.stackPush();

      try {
         ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE)
            .putVec4(PostFx.shaderTime(), glow, natural ? 1.0F : 0.0F, 0.0025F)
            .putVec4(width, height, 0.0F, 0.0F)
            .get();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), data);
      } catch (Throwable var10) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (stack != null) {
         stack.close();
      }

      return buffer;
   }

   private void ensureDepthCopy(int width, int height) {
      if (this.depthCopy == null || this.depthCopy.field_1482 != width || this.depthCopy.field_1481 != height) {
         if (this.depthCopy != null) {
            this.depthCopy.method_1238();
         }

         this.depthCopy = new class_6367("blade-procedural-particle-depth", width, height, true);
      }
   }

   private static void putVec4(ByteBuffer buffer, float x, float y, float z, float w) {
      buffer.putFloat(x).putFloat(y).putFloat(z).putFloat(w);
   }

   public void release() {
      if (this.depthCopy != null) {
         this.depthCopy.method_1238();
         this.depthCopy = null;
      }
   }

   @Environment(EnvType.CLIENT)
   public enum Shape {
      RANDOM("Random", -1, 0.4F, 0.5F),
      STAR("Stars", 0, 0.5F, 0.65F),
      DOLLAR("Dollars", 1, 0.3F, 0.2F),
      SNOWFLAKE("Snowflakes", 2, 0.25F, 0.15F),
      GLOW("Glow", 3, 1.0F, 0.9F),
      PUMPKIN("Pumpkins", 4, 0.2F, 0.0F),
      HEART("Hearts", 5, 0.35F, 0.2F),
      SPARK("Sparks", 6, 0.8F, 0.85F),
      EMBER("Embers", 7, 0.9F, 0.8F);

      private static final ProceduralParticleRenderer.Shape[] CONCRETE = new ProceduralParticleRenderer.Shape[]{
         STAR, DOLLAR, SNOWFLAKE, GLOW, PUMPKIN, HEART, SPARK, EMBER
      };
      private final String displayName;
      private final int shapeId;
      private final float additivity;
      private final float glow;

      Shape(String displayName, int shapeId, float additivity, float glow) {
         this.displayName = displayName;
         this.shapeId = shapeId;
         this.additivity = additivity;
         this.glow = glow;
      }

      public String displayName() {
         return this.displayName;
      }

      public ProceduralParticleRenderer.Shape resolve(Random random) {
         return this == RANDOM ? CONCRETE[random.nextInt(CONCRETE.length)] : this;
      }

      public static ProceduralParticleRenderer.Shape fromDisplayName(String value) {
         for (ProceduralParticleRenderer.Shape shape : values()) {
            if (shape.displayName.equals(value)) {
               return shape;
            }
         }

         return RANDOM;
      }

      public static String[] displayNames() {
         ProceduralParticleRenderer.Shape[] values = values();
         String[] names = new String[values.length];

         for (int index = 0; index < values.length; index++) {
            names[index] = values[index].displayName;
         }

         return names;
      }
   }

   @Environment(EnvType.CLIENT)
   public record Sprite(
      class_243 position, float halfSize, int color, ProceduralParticleRenderer.Shape shape, float rotation, float normalizedLife, float seed, float phase
   ) {
   }
}
