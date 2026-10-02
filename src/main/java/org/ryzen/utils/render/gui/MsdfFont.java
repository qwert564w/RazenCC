package org.ryzen.utils.render.gui;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_11231;
import net.minecraft.class_2960;
import org.ryzen.context.MinecraftContext;

@Environment(EnvType.CLIENT)
public final class MsdfFont {
   private static final Gson GSON = new Gson();
   private static final Map<class_2960, MsdfFont> CACHE = new ConcurrentHashMap<>();
   private static final int SHAPE_CACHE_CAPACITY = 1024;
   private final class_2960 textureId;
   private final float distanceRange;
   private final int atlasWidth;
   private final int atlasHeight;
   private final float atlasSize;
   private final float lineHeightEm;
   private final float ascenderEm;
   private final float descenderEm;
   private final Int2ObjectMap<MsdfFont.Glyph> glyphs;
   private final Long2FloatMap kerning;
   private final MsdfFont.Glyph fallbackGlyph;
   private final Map<String, MsdfFont.Paragraph> shapeCache = new LinkedHashMap<String, MsdfFont.Paragraph>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, MsdfFont.Paragraph> eldest) {
         return this.size() > 1024;
      }
   };
   private class_11231 textureSetup;

   private MsdfFont(class_2960 metadataId, MsdfFont.FontFile file) {
      this.textureId = metadataId.method_45134(path -> path.replace(".json", ".png"));
      this.distanceRange = (float)file.atlas.distanceRange;
      this.atlasWidth = file.atlas.width;
      this.atlasHeight = file.atlas.height;
      this.atlasSize = file.atlas.size;
      this.lineHeightEm = (float)file.metrics.lineHeight;
      this.ascenderEm = (float)file.metrics.ascender;
      this.descenderEm = (float)file.metrics.descender;
      this.glyphs = new Int2ObjectOpenHashMap(file.glyphs.size());

      for (MsdfFont.RawGlyph raw : file.glyphs) {
         this.glyphs.put(raw.unicode, MsdfFont.Glyph.bake(raw, this.atlasWidth, this.atlasHeight));
      }

      this.fallbackGlyph = (MsdfFont.Glyph)this.glyphs.get(63);
      this.kerning = new Long2FloatOpenHashMap();
      this.kerning.defaultReturnValue(0.0F);
      if (file.kerning != null) {
         for (MsdfFont.KerningPair pair : file.kerning) {
            this.kerning.put(packKerningKey(pair.unicode1, pair.unicode2), (float)pair.advance);
         }
      }
   }

   public static MsdfFont load(class_2960 metadataId) {
      Objects.requireNonNull(metadataId, "metadataId");
      return CACHE.computeIfAbsent(metadataId, MsdfFont::readFont);
   }

   public float lineHeight(float size) {
      return this.lineHeightEm * size;
   }

   public float ascender(float size) {
      return this.ascenderEm * size;
   }

   public float centeredTextY(float centerY, float size) {
      return centerY - this.textHeight(size) * 0.5F;
   }

   public float textHeight(float size) {
      return (this.ascenderEm - this.descenderEm) * size;
   }

   public float distanceRange() {
      return this.distanceRange;
   }

   public int atlasWidth() {
      return this.atlasWidth;
   }

   public int atlasHeight() {
      return this.atlasHeight;
   }

   public float atlasSize() {
      return this.atlasSize;
   }

   public float localPxPerSdfUnit(float size) {
      return this.distanceRange * size / this.atlasSize;
   }

   public MsdfFont.Glyph glyph(int codePoint) {
      MsdfFont.Glyph glyph = (MsdfFont.Glyph)this.glyphs.get(codePoint);
      return glyph != null ? glyph : this.fallbackGlyph;
   }

   public boolean canRender(String text) {
      return !this.shape(text).missingGlyphs();
   }

   public float kerning(int leftCodePoint, int rightCodePoint) {
      return this.kerning.get(packKerningKey(leftCodePoint, rightCodePoint));
   }

   public float measureWidth(String text, float size) {
      return this.measureWidth(text, size, 0.0F);
   }

   public float measureWidth(String text, float size, float letterSpacing) {
      return this.shape(text).maxWidth(size, letterSpacing);
   }

   public String ellipsize(String text, float size, float letterSpacing, float maxWidth) {
      MsdfFont.Paragraph paragraph = this.shape(text);
      if (paragraph.maxWidth(size, letterSpacing) <= maxWidth) {
         return text;
      }

      MsdfFont.Line line = paragraph.lines().get(0);
      float budget = maxWidth - this.shape("...").maxWidth(size, letterSpacing);
      int low = 0;
      int high = line.glyphCount();

      while (low < high) {
         int mid = low + high + 1 >>> 1;
         if (line.prefixWidth(mid, size, letterSpacing) > budget) {
            high = mid - 1;
         } else {
            low = mid;
         }
      }

      int count = low;

      while (count > 0 && Character.isWhitespace(line.codePointAt(count - 1))) {
         count--;
      }

      return line.charPrefix(count) + "...";
   }

   public MsdfFont.Paragraph shape(String text) {
      Objects.requireNonNull(text, "text");
      MsdfFont.Paragraph cached = this.shapeCache.get(text);
      if (cached != null) {
         return cached;
      }

      MsdfFont.Paragraph paragraph = this.shapeUncached(text);
      this.shapeCache.put(text, paragraph);
      return paragraph;
   }

   private MsdfFont.Paragraph shapeUncached(String text) {
      List<MsdfFont.Line> lines = new ArrayList<>(1);
      boolean missing = false;
      int lineStart = 0;

      for (int index = 0; index <= text.length(); index++) {
         if (index == text.length() || text.charAt(index) == '\n') {
            MsdfFont.Line line = this.shapeLine(text.substring(lineStart, index).replace("\r", ""));
            missing |= line.missingGlyphs;
            lines.add(line);
            lineStart = index + 1;
         }
      }

      return new MsdfFont.Paragraph(List.copyOf(lines), missing);
   }

   private MsdfFont.Line shapeLine(String text) {
      int capacity = text.length();
      MsdfFont.Glyph[] glyphs = new MsdfFont.Glyph[capacity];
      float[] penEm = new float[capacity];
      int[] charIndex = new int[capacity];
      int[] codePoints = new int[capacity];
      int count = 0;
      float pen = 0.0F;
      boolean missing = false;
      int previousCodePoint = -1;
      int index = 0;

      while (index < text.length()) {
         int codePoint = text.codePointAt(index);
         int charLength = Character.charCount(codePoint);
         MsdfFont.Glyph glyph = (MsdfFont.Glyph)this.glyphs.get(codePoint);
         if (glyph == null) {
            missing = true;
            glyph = this.fallbackGlyph;
         }

         if (glyph != null) {
            if (previousCodePoint != -1) {
               pen += this.kerning(previousCodePoint, codePoint);
            }

            glyphs[count] = glyph;
            penEm[count] = pen;
            charIndex[count] = index;
            codePoints[count] = codePoint;
            pen += glyph.advanceEm();
            count++;
            previousCodePoint = codePoint;
         }

         index += charLength;
      }

      return new MsdfFont.Line(text, glyphs, penEm, charIndex, codePoints, count, pen, missing);
   }

   public class_11231 textureSetup() {
      if (this.textureSetup == null) {
         this.textureSetup = this.createTextureSetup();
      }

      return this.textureSetup;
   }

   private class_11231 createTextureSetup() {
      class_1043 texture = new class_1043(() -> this.textureId.toString(), readAtlasImage(this.textureId));
      MinecraftContext.mc.method_1531().method_4616(this.textureId, texture);
      return class_11231.method_70900(texture.method_71659(), RenderSystem.getSamplerCache().method_76520(FilterMode.LINEAR, false));
   }

   private static MsdfFont readFont(class_2960 metadataId) {
      try (Reader reader = MinecraftContext.mc.method_1478().openAsReader(metadataId)) {
         MsdfFont.FontFile file = (MsdfFont.FontFile)GSON.fromJson(reader, MsdfFont.FontFile.class);
         if (file == null
            || file.atlas == null
            || file.metrics == null
            || file.glyphs == null
            || file.atlas.width <= 0
            || file.atlas.height <= 0
            || file.atlas.size <= 0
            || file.metrics.lineHeight <= 0.0) {
            throw new IllegalStateException("Invalid MSDF font metadata: " + metadataId);
         } else {
            return new MsdfFont(metadataId, file);
         }
      } catch (IOException exception) {
         throw new IllegalStateException("Failed to read MSDF font metadata: " + metadataId, exception);
      }
   }

   private static class_1011 readAtlasImage(class_2960 textureId) {
      try (InputStream inputStream = MinecraftContext.mc.method_1478().open(textureId)) {
         return class_1011.method_4309(inputStream);
      } catch (IOException exception) {
         throw new IllegalStateException("Failed to read MSDF atlas image: " + textureId, exception);
      }
   }

   private static long packKerningKey(int leftCodePoint, int rightCodePoint) {
      return (long)leftCodePoint << 32 | rightCodePoint & 4294967295L;
   }

   @Environment(EnvType.CLIENT)
   private static final class Atlas {
      @SerializedName("distanceRange")
      private double distanceRange;
      private int width;
      private int height;
      private int size;
   }

   @Environment(EnvType.CLIENT)
   public record Bounds(float left, float bottom, float right, float top) {
   }

   @Environment(EnvType.CLIENT)
   private static final class FontFile {
      private MsdfFont.Atlas atlas;
      private MsdfFont.Metrics metrics;
      private List<MsdfFont.RawGlyph> glyphs;
      private List<MsdfFont.KerningPair> kerning;
   }

   @Environment(EnvType.CLIENT)
   public static final class Glyph {
      private final float advanceEm;
      private final MsdfFont.Bounds planeBounds;
      private final float u0;
      private final float v0;
      private final float u1;
      private final float v1;

      private Glyph(float advanceEm, MsdfFont.Bounds planeBounds, float u0, float v0, float u1, float v1) {
         this.advanceEm = advanceEm;
         this.planeBounds = planeBounds;
         this.u0 = u0;
         this.v0 = v0;
         this.u1 = u1;
         this.v1 = v1;
      }

      private static MsdfFont.Glyph bake(MsdfFont.RawGlyph raw, int atlasWidth, int atlasHeight) {
         MsdfFont.Bounds plane = raw.planeBounds != null && raw.atlasBounds != null
            ? new MsdfFont.Bounds((float)raw.planeBounds.left, (float)raw.planeBounds.bottom, (float)raw.planeBounds.right, (float)raw.planeBounds.top)
            : null;
         float u0 = 0.0F;
         float v0 = 0.0F;
         float u1 = 0.0F;
         float v1 = 0.0F;
         if (plane != null) {
            u0 = (float)(raw.atlasBounds.left / atlasWidth);
            v0 = (float)(1.0 - raw.atlasBounds.top / atlasHeight);
            u1 = (float)(raw.atlasBounds.right / atlasWidth);
            v1 = (float)(1.0 - raw.atlasBounds.bottom / atlasHeight);
         }

         return new MsdfFont.Glyph((float)raw.advance, plane, u0, v0, u1, v1);
      }

      public float advance() {
         return this.advanceEm;
      }

      public float advanceEm() {
         return this.advanceEm;
      }

      public MsdfFont.Bounds planeBounds() {
         return this.planeBounds;
      }

      public float u0() {
         return this.u0;
      }

      public float v0() {
         return this.v0;
      }

      public float u1() {
         return this.u1;
      }

      public float v1() {
         return this.v1;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class KerningPair {
      @SerializedName("unicode1")
      private int unicode1;
      @SerializedName("unicode2")
      private int unicode2;
      private double advance;
   }

   @Environment(EnvType.CLIENT)
   public static final class Line {
      private final String text;
      private final MsdfFont.Glyph[] glyphs;
      private final float[] penEm;
      private final int[] charIndex;
      private final int[] codePoints;
      private final int glyphCount;
      private final float widthEm;
      private final boolean missingGlyphs;

      private Line(String text, MsdfFont.Glyph[] glyphs, float[] penEm, int[] charIndex, int[] codePoints, int glyphCount, float widthEm, boolean missingGlyphs) {
         this.text = text;
         this.glyphs = glyphs;
         this.penEm = penEm;
         this.charIndex = charIndex;
         this.codePoints = codePoints;
         this.glyphCount = glyphCount;
         this.widthEm = widthEm;
         this.missingGlyphs = missingGlyphs;
      }

      public String text() {
         return this.text;
      }

      public int glyphCount() {
         return this.glyphCount;
      }

      public MsdfFont.Glyph glyphAt(int index) {
         return this.glyphs[index];
      }

      public float penX(int index, float size, float letterSpacing) {
         return this.penEm[index] * size + letterSpacing * index;
      }

      public float width(float size, float letterSpacing) {
         return this.widthEm * size + letterSpacing * Math.max(0, this.glyphCount - 1);
      }

      private float prefixWidth(int glyphCountPrefix, float size, float letterSpacing) {
         if (glyphCountPrefix <= 0) {
            return 0.0F;
         }

         int last = glyphCountPrefix - 1;
         float em = this.penEm[last] + this.glyphs[last].advanceEm();
         return em * size + letterSpacing * last;
      }

      private int codePointAt(int glyphIndex) {
         return this.codePoints[glyphIndex];
      }

      private String charPrefix(int glyphCountPrefix) {
         if (glyphCountPrefix <= 0) {
            return "";
         }

         int last = glyphCountPrefix - 1;
         int end = this.charIndex[last] + Character.charCount(this.codePoints[last]);
         return this.text.substring(0, end);
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class Metrics {
      private double lineHeight;
      private double ascender;
      private double descender;
   }

   @Environment(EnvType.CLIENT)
   public record Paragraph(List<MsdfFont.Line> lines, boolean missingGlyphs) {
      public float maxWidth(float size, float letterSpacing) {
         float max = 0.0F;

         for (int i = 0; i < this.lines.size(); i++) {
            max = Math.max(max, this.lines.get(i).width(size, letterSpacing));
         }

         return max;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class RawBounds {
      private double left;
      private double bottom;
      private double right;
      private double top;
   }

   @Environment(EnvType.CLIENT)
   private static final class RawGlyph {
      private int unicode;
      private double advance;
      private MsdfFont.RawBounds planeBounds;
      private MsdfFont.RawBounds atlasBounds;
   }
}
