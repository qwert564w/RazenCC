package org.ryzen.utils.render.world;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.ryzen.utils.render.Render3DUtil;

@Environment(EnvType.CLIENT)
public final class WorldMeshRenderer {
   private static final float LINE_HALF_WIDTH = 0.012F;
   private static final RenderPipeline THROUGH_WALLS = buildPipeline("line", false);
   private static final RenderPipeline DEPTH_TESTED = buildPipeline("line_depth", true);
   private static GpuBuffer vertexBuffer;

   private WorldMeshRenderer() {
   }

   private static RenderPipeline buildPipeline(String name, boolean depthTest) {
      return RenderPipeline.builder(new Snippet[0])
         .withLocation(class_2960.method_60654("ryzen:pipeline/world/" + name))
         .withVertexShader(class_2960.method_60654("ryzen:core/blade_line"))
         .withFragmentShader(class_2960.method_60654("ryzen:core/blade_line"))
         .withUniform("Projection", class_10789.field_60031)
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(depthTest ? DepthTestFunction.LEQUAL_DEPTH_TEST : DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withVertexFormat(class_290.field_1576, class_5596.field_27379)
         .withCull(false)
         .build();
   }

   public static void render(WorldMeshRenderer.WorldMesh mesh) {
      render(mesh, true);
   }

   public static void render(WorldMeshRenderer.WorldMesh mesh, boolean throughWalls) {
      if (mesh != null && !mesh.isEmpty()) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1687 != null && mc.field_1773 != null) {
            class_276 target = mc.method_1522();
            GpuTextureView colorView = target != null ? target.method_71639() : null;
            if (colorView != null) {
               WorldMeshRenderer.BuiltMesh built = buildMesh(mc, mesh);
               if (built != null) {
                  GpuDevice device = RenderSystem.getDevice();

                  try {
                     ByteBuffer vertexData = built.meshData().method_60818();
                     int byteSize = vertexData.remaining();
                     ensureVertexCapacity(byteSize);
                     device.createCommandEncoder().writeToBuffer(vertexBuffer.slice(0L, byteSize), vertexData);
                     GpuTextureView depthView = target.method_71640();
                     RenderPass pass = depthView != null
                        ? device.createCommandEncoder()
                           .createRenderPass(() -> "Ryzen World Mesh Pass", colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())
                        : device.createCommandEncoder().createRenderPass(() -> "Ryzen World Mesh Pass", colorView, OptionalInt.empty());

                     try {
                        pass.setPipeline(throughWalls ? THROUGH_WALLS : DEPTH_TESTED);
                        pass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
                        pass.setVertexBuffer(0, vertexBuffer);
                        pass.draw(0, built.vertexCount());
                     } finally {
                        pass.close();
                     }
                  } finally {
                     built.meshData().close();
                  }
               }
            }
         }
      }
   }

   private static void ensureVertexCapacity(int byteSize) {
      if (vertexBuffer == null || vertexBuffer.size() < byteSize) {
         if (vertexBuffer != null) {
            vertexBuffer.close();
         }

         int capacity = Math.max(byteSize + byteSize / 2, 16384);
         vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Ryzen World Mesh Vertices", 40, capacity);
      }
   }

   private static WorldMeshRenderer.BuiltMesh buildMesh(class_310 mc, WorldMeshRenderer.WorldMesh mesh) {
      class_4184 camera = mc.field_1773.method_19418();
      class_243 cameraPos = camera.method_71156();
      Matrix4f pose = Render3DUtil.cameraViewPose(camera);
      int estimatedVertices = mesh.lines().size() * 6
         + mesh.rings().stream().mapToInt(WorldMeshRenderer.Ring::segments).sum() * 6
         + mesh.planeRects().size() * 6
         + mesh.tris().size() * 3;
      if (estimatedVertices <= 0) {
         return null;
      }

      int bytes = estimatedVertices * class_290.field_1576.getVertexSize();
      class_287 builder = new class_287(new class_9799(Math.max(bytes, 256)), class_5596.field_27379, class_290.field_1576);
      int vertexCount = 0;

      for (WorldMeshRenderer.Line line : mesh.lines()) {
         addThickLine(
            builder,
            Render3DUtil.toViewSpace(line.start(), cameraPos, pose),
            Render3DUtil.toViewSpace(line.end(), cameraPos, pose),
            line.startColor(),
            line.endColor()
         );
         vertexCount += 6;
      }

      for (WorldMeshRenderer.Ring ring : mesh.rings()) {
         addRing(builder, ring, cameraPos, pose);
         vertexCount += ring.segments() * 6;
      }

      for (WorldMeshRenderer.PlaneRect rect : mesh.planeRects()) {
         addPlaneRect(builder, rect, cameraPos, pose);
         vertexCount += 6;
      }

      for (WorldMeshRenderer.Tri tri : mesh.tris()) {
         addTri(builder, tri, cameraPos, pose);
         vertexCount += 3;
      }

      if (vertexCount == 0) {
         return null;
      }

      class_9801 meshData = builder.method_60800();
      return new WorldMeshRenderer.BuiltMesh(meshData, vertexCount);
   }

   private static void addThickLine(class_287 builder, Vector4f start, Vector4f end, int startColor, int endColor) {
      float dx = end.x - start.x;
      float dy = end.y - start.y;
      float length = (float)Math.sqrt(dx * dx + dy * dy);
      float normalX;
      float normalY;
      if (length > 0.05F) {
         normalX = -dy / length * 0.012F;
         normalY = dx / length * 0.012F;
      } else {
         normalX = 0.012F;
         normalY = 0.0F;
      }

      float sx1 = start.x + normalX;
      float sy1 = start.y + normalY;
      float sx2 = start.x - normalX;
      float sy2 = start.y - normalY;
      float ex1 = end.x + normalX;
      float ey1 = end.y + normalY;
      float ex2 = end.x - normalX;
      float ey2 = end.y - normalY;
      builder.method_22912(sx1, sy1, start.z).method_39415(startColor);
      builder.method_22912(sx2, sy2, start.z).method_39415(startColor);
      builder.method_22912(ex2, ey2, end.z).method_39415(endColor);
      builder.method_22912(sx1, sy1, start.z).method_39415(startColor);
      builder.method_22912(ex2, ey2, end.z).method_39415(endColor);
      builder.method_22912(ex1, ey1, end.z).method_39415(endColor);
   }

   private static void addRing(class_287 builder, WorldMeshRenderer.Ring ring, class_243 cameraPos, Matrix4f pose) {
      double innerRadius = Math.max(0.0, ring.radius() - ring.halfWidth());
      double outerRadius = ring.radius() + ring.halfWidth();

      for (int i = 0; i < ring.segments(); i++) {
         double angle1 = i * (Math.PI * 2) / ring.segments();
         double angle2 = (i + 1) * (Math.PI * 2) / ring.segments();
         class_243 outer1 = ring.center()
            .method_1019(ring.u().method_1021(Math.cos(angle1) * outerRadius))
            .method_1019(ring.v().method_1021(Math.sin(angle1) * outerRadius));
         class_243 inner1 = ring.center()
            .method_1019(ring.u().method_1021(Math.cos(angle1) * innerRadius))
            .method_1019(ring.v().method_1021(Math.sin(angle1) * innerRadius));
         class_243 outer2 = ring.center()
            .method_1019(ring.u().method_1021(Math.cos(angle2) * outerRadius))
            .method_1019(ring.v().method_1021(Math.sin(angle2) * outerRadius));
         class_243 inner2 = ring.center()
            .method_1019(ring.u().method_1021(Math.cos(angle2) * innerRadius))
            .method_1019(ring.v().method_1021(Math.sin(angle2) * innerRadius));
         Vector4f outerView1 = Render3DUtil.toViewSpace(outer1, cameraPos, pose);
         Vector4f innerView1 = Render3DUtil.toViewSpace(inner1, cameraPos, pose);
         Vector4f outerView2 = Render3DUtil.toViewSpace(outer2, cameraPos, pose);
         Vector4f innerView2 = Render3DUtil.toViewSpace(inner2, cameraPos, pose);
         builder.method_22912(outerView1.x, outerView1.y, outerView1.z).method_39415(ring.color());
         builder.method_22912(innerView1.x, innerView1.y, innerView1.z).method_39415(ring.color());
         builder.method_22912(innerView2.x, innerView2.y, innerView2.z).method_39415(ring.color());
         builder.method_22912(outerView1.x, outerView1.y, outerView1.z).method_39415(ring.color());
         builder.method_22912(innerView2.x, innerView2.y, innerView2.z).method_39415(ring.color());
         builder.method_22912(outerView2.x, outerView2.y, outerView2.z).method_39415(ring.color());
      }
   }

   private static void addPlaneRect(class_287 builder, WorldMeshRenderer.PlaneRect rect, class_243 cameraPos, Matrix4f pose) {
      class_243 p1 = rect.center().method_1019(rect.axis().method_1021(rect.halfLength())).method_1019(rect.normal().method_1021(rect.halfWidth()));
      class_243 p2 = rect.center().method_1019(rect.axis().method_1021(rect.halfLength())).method_1019(rect.normal().method_1021(-rect.halfWidth()));
      class_243 p3 = rect.center().method_1019(rect.axis().method_1021(-rect.halfLength())).method_1019(rect.normal().method_1021(-rect.halfWidth()));
      class_243 p4 = rect.center().method_1019(rect.axis().method_1021(-rect.halfLength())).method_1019(rect.normal().method_1021(rect.halfWidth()));
      Vector4f v1 = Render3DUtil.toViewSpace(p1, cameraPos, pose);
      Vector4f v2 = Render3DUtil.toViewSpace(p2, cameraPos, pose);
      Vector4f v3 = Render3DUtil.toViewSpace(p3, cameraPos, pose);
      Vector4f v4 = Render3DUtil.toViewSpace(p4, cameraPos, pose);
      builder.method_22912(v1.x, v1.y, v1.z).method_39415(rect.color());
      builder.method_22912(v2.x, v2.y, v2.z).method_39415(rect.color());
      builder.method_22912(v3.x, v3.y, v3.z).method_39415(rect.color());
      builder.method_22912(v1.x, v1.y, v1.z).method_39415(rect.color());
      builder.method_22912(v3.x, v3.y, v3.z).method_39415(rect.color());
      builder.method_22912(v4.x, v4.y, v4.z).method_39415(rect.color());
   }

   private static void addTri(class_287 builder, WorldMeshRenderer.Tri tri, class_243 cameraPos, Matrix4f pose) {
      Vector4f a = Render3DUtil.toViewSpace(tri.a(), cameraPos, pose);
      Vector4f b = Render3DUtil.toViewSpace(tri.b(), cameraPos, pose);
      Vector4f c = Render3DUtil.toViewSpace(tri.c(), cameraPos, pose);
      builder.method_22912(a.x, a.y, a.z).method_39415(tri.colorA());
      builder.method_22912(b.x, b.y, b.z).method_39415(tri.colorB());
      builder.method_22912(c.x, c.y, c.z).method_39415(tri.colorC());
   }

   @Environment(EnvType.CLIENT)
   private record BuiltMesh(class_9801 meshData, int vertexCount) {
   }

   @Environment(EnvType.CLIENT)
   public record Line(class_243 start, class_243 end, int startColor, int endColor) {
      public Line(class_243 start, class_243 end, int color) {
         this(start, end, color, color);
      }
   }

   @Environment(EnvType.CLIENT)
   public record PlaneRect(class_243 center, class_243 axis, class_243 normal, double halfLength, double halfWidth, int color) {
   }

   @Environment(EnvType.CLIENT)
   public record Ring(class_243 center, class_243 u, class_243 v, double radius, double halfWidth, int color, int segments) {
   }

   @Environment(EnvType.CLIENT)
   public record Tri(class_243 a, class_243 b, class_243 c, int colorA, int colorB, int colorC) {
      public Tri(class_243 a, class_243 b, class_243 c, int color) {
         this(a, b, c, color, color, color);
      }
   }

   @Environment(EnvType.CLIENT)
   public record WorldMesh(
      List<WorldMeshRenderer.Line> lines, List<WorldMeshRenderer.Ring> rings, List<WorldMeshRenderer.PlaneRect> planeRects, List<WorldMeshRenderer.Tri> tris
   ) {
      public WorldMesh(List<WorldMeshRenderer.Line> lines, List<WorldMeshRenderer.Ring> rings, List<WorldMeshRenderer.PlaneRect> planeRects) {
         this(lines, rings, planeRects, List.of());
      }

      public boolean isEmpty() {
         return this.lines.isEmpty() && this.rings.isEmpty() && this.planeRects.isEmpty() && this.tris.isEmpty();
      }
   }
}
