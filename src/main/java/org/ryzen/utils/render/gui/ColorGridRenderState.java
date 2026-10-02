package org.ryzen.utils.render.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_11231;
import net.minecraft.class_2960;
import net.minecraft.class_4588;
import net.minecraft.class_8030;
import net.minecraft.class_1011.class_1012;
import org.joml.Matrix3x2fc;
import org.ryzen.context.MinecraftContext;

@Environment(EnvType.CLIENT)
public final class ColorGridRenderState extends UiElementRenderState {
   private final ColorGridRenderState.ColorGridTexture texture;
   private final float x;
   private final float y;
   private final float cellSize;
   private final int columns;
   private final int rows;
   private final float radius;
   private final int color;
   private final int selectedIndex;
   private final float selectedThickness;
   private final int hoveredIndex;
   private final float hoveredThickness;

   public ColorGridRenderState(
      Matrix3x2fc pose,
      float x,
      float y,
      float cellSize,
      int columns,
      float radius,
      int color,
      int selectedIndex,
      float selectedThickness,
      int hoveredIndex,
      float hoveredThickness,
      int[] colors,
      class_8030 scissor
   ) {
      super(pose, scissor, x, y, Math.max(1.0F, cellSize) * Math.max(1, columns), Math.max(1.0F, cellSize) * Math.max(1, colors.length / Math.max(1, columns)));
      this.texture = ColorGridRenderState.ColorGridTexture.load(columns, colors);
      this.x = x;
      this.y = y;
      this.cellSize = Math.max(1.0F, cellSize);
      this.columns = Math.max(1, columns);
      this.rows = Math.max(1, colors.length / this.columns);
      this.radius = Math.max(0.0F, radius);
      this.color = color;
      this.selectedIndex = selectedIndex;
      this.selectedThickness = Math.max(0.0F, selectedThickness);
      this.hoveredIndex = hoveredIndex;
      this.hoveredThickness = Math.max(0.0F, hoveredThickness);
   }

   public void method_70917(class_4588 vertexConsumer) {
      float width = this.cellSize * this.columns;
      float height = this.cellSize * this.rows;
      float packedIndices = UiVertexPacking.packDual12Raw(this.selectedIndex + 1, this.hoveredIndex + 1);
      int packedThickness = UiVertexPacking.packU8Pair(Math.round(this.selectedThickness * 8.0F), Math.round(this.hoveredThickness * 8.0F));
      int packedGrid = UiVertexPacking.packU8Pair(this.columns, this.rows);
      int packedSizeX = UiVertexPacking.packSize(width);
      int packedSizeY = UiVertexPacking.packSize(height);
      this.addVertex(vertexConsumer, this.x, this.y, 0.0F, 0.0F, packedIndices, packedThickness, packedGrid, packedSizeX, packedSizeY);
      this.addVertex(vertexConsumer, this.x, this.y + height, 0.0F, height, packedIndices, packedThickness, packedGrid, packedSizeX, packedSizeY);
      this.addVertex(vertexConsumer, this.x + width, this.y + height, width, height, packedIndices, packedThickness, packedGrid, packedSizeX, packedSizeY);
      this.addVertex(vertexConsumer, this.x + width, this.y, width, 0.0F, packedIndices, packedThickness, packedGrid, packedSizeX, packedSizeY);
   }

   private void addVertex(
      class_4588 vertexConsumer,
      float x,
      float y,
      float localX,
      float localY,
      float packedIndices,
      int packedThickness,
      int packedGrid,
      int packedSizeX,
      int packedSizeY
   ) {
      vertexConsumer.method_22912(this.transformX(x, y), this.transformY(x, y), packedIndices)
         .method_39415(this.color)
         .method_22913(localX, localY)
         .method_60796(packedSizeX, packedSizeY)
         .method_22921(packedThickness, packedGrid)
         .method_22914(0.0F, 0.0F, 1.0F)
         .method_75298(this.radius);
   }

   public RenderPipeline comp_4055() {
      return GuiPipelines.COLOR_GRID;
   }

   public class_11231 comp_4056() {
      return this.texture.textureSetup();
   }

   @Environment(EnvType.CLIENT)
   private static final class ColorGridTexture {
      private static final Map<ColorGridRenderState.ColorGridTexture.Key, ColorGridRenderState.ColorGridTexture> CACHE = new ConcurrentHashMap<>();
      private final ColorGridRenderState.ColorGridTexture.Key key;
      private class_11231 textureSetup;

      private ColorGridTexture(ColorGridRenderState.ColorGridTexture.Key key) {
         this.key = key;
      }

      private static ColorGridRenderState.ColorGridTexture load(int columns, int[] colors) {
         return CACHE.computeIfAbsent(new ColorGridRenderState.ColorGridTexture.Key(columns, colors), ColorGridRenderState.ColorGridTexture::new);
      }

      private class_11231 textureSetup() {
         if (this.textureSetup == null) {
            class_2960 textureId = class_2960.method_60654("ryzen:generated/color_grid/" + Integer.toHexString(this.key.hashCode()));
            class_1043 texture = new class_1043(() -> textureId.toString(), createImage(this.key.columns, this.key.colors));
            MinecraftContext.mc.method_1531().method_4616(textureId, texture);
            this.textureSetup = class_11231.method_70900(texture.method_71659(), RenderSystem.getSamplerCache().method_76520(FilterMode.NEAREST, false));
         }

         return this.textureSetup;
      }

      private static class_1011 createImage(int columns, int[] colors) {
         int safeColumns = Math.max(1, columns);
         int rows = Math.max(1, colors.length / safeColumns);
         class_1011 image = new class_1011(class_1012.field_4997, safeColumns, rows, false);

         for (int index = 0; index < colors.length; index++) {
            image.method_61941(index % safeColumns, index / safeColumns, colors[index]);
         }

         return image;
      }

      @Environment(EnvType.CLIENT)
      private record Key(int columns, int[] colors) {
         private Key {
            columns = Math.max(1, columns);
            colors = Arrays.copyOf(colors, colors.length);
         }

         @Override
         public boolean equals(Object object) {
            return object instanceof ColorGridRenderState.ColorGridTexture.Key other
               && this.columns == other.columns
               && Arrays.equals(this.colors, other.colors);
         }

         @Override
         public int hashCode() {
            return 31 * this.columns + Arrays.hashCode(this.colors);
         }
      }
   }
}
