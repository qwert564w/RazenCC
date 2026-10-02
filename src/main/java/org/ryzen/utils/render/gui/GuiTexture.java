package org.ryzen.utils.render.gui;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.FloatSize;
import com.github.weisj.jsvg.view.ViewBox;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_11231;
import net.minecraft.class_2960;
import net.minecraft.class_1011.class_1012;
import org.ryzen.context.MinecraftContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class GuiTexture {
   private static final Logger LOGGER = LoggerFactory.getLogger(GuiTexture.class);
   private static final Map<class_2960, GuiTexture> CACHE = new ConcurrentHashMap<>();
   private static final int MAX_VARIANTS = 8;
   private static final int SUPERSAMPLE = 2;
   private static final int MAX_TEXTURE_SIZE = 2048;
   private static final int OVERSAMPLE = 3;
   private static final int OVERSAMPLE_LIMIT = 256;
   private final class_2960 textureId;
   private final boolean svg;
   private volatile SVGDocument document;
   private class_11231 staticSetup;
   private final LinkedHashMap<Long, class_11231> svgVariants = new LinkedHashMap<Long, class_11231>(8, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<Long, class_11231> eldest) {
         if (this.size() <= 8) {
            return false;
         }

         MinecraftContext.mc.method_1531().method_4615(GuiTexture.variantId(GuiTexture.this.textureId, eldest.getKey()));
         return true;
      }
   };

   private GuiTexture(class_2960 textureId) {
      this.textureId = textureId;
      this.svg = textureId.method_12832().endsWith(".svg");
   }

   public static GuiTexture load(class_2960 textureId) {
      return CACHE.computeIfAbsent(textureId, GuiTexture::new);
   }

   public static void prewarm(Collection<class_2960> ids) {
      Thread thread = new Thread(() -> {
         for (class_2960 id : ids) {
            try {
               GuiTexture texture = load(id);
               if (texture.svg) {
                  texture.document();
               }
            } catch (Throwable throwable) {
               LOGGER.warn("Failed to prewarm GUI texture {}", id, throwable);
            }
         }
      }, "Ryzen SVG Prewarm");
      thread.setDaemon(true);
      thread.start();
   }

   public class_11231 textureSetup(float deviceWidth, float deviceHeight) {
      if (!this.svg) {
         return this.staticSetup();
      }

      long key = packSize(quantize(deviceWidth), quantize(deviceHeight));
      class_11231 cached = this.svgVariants.get(key);
      if (cached != null) {
         return cached;
      }

      class_11231 created = this.createSvgSetup(key);
      this.svgVariants.put(key, created);
      return created;
   }

   public class_11231 textureSetup() {
      if (!this.svg) {
         return this.staticSetup();
      }

      FloatSize size = this.documentSize();
      return this.textureSetup((float)size.getWidth(), (float)size.getHeight());
   }

   private class_11231 staticSetup() {
      if (this.staticSetup == null) {
         class_1043 texture = new class_1043(() -> this.textureId.toString(), readPng(this.textureId));
         texture.method_4524();
         MinecraftContext.mc.method_1531().method_4616(this.textureId, texture);
         this.staticSetup = class_11231.method_70900(texture.method_71659(), RenderSystem.getSamplerCache().method_76520(FilterMode.LINEAR, false));
      }

      return this.staticSetup;
   }

   private class_11231 createSvgSetup(long key) {
      int width = Math.min(2048, unpackWidth(key) * 2);
      int height = Math.min(2048, unpackHeight(key) * 2);
      int oversample = oversample(width, height);
      class_2960 variantId = variantId(this.textureId, key);
      class_1043 texture = new class_1043(variantId::toString, rasterizeSvg(this.textureId, this.document(), width, height, oversample));
      texture.method_4524();
      MinecraftContext.mc.method_1531().method_4616(variantId, texture);
      return class_11231.method_70900(texture.method_71659(), RenderSystem.getSamplerCache().method_76520(FilterMode.LINEAR, false));
   }

   private static int oversample(int width, int height) {
      if (width <= 256 && height <= 256) {
         int factor = 3;

         while (factor > 1 && (long)Math.max(width, height) * factor > 2048L) {
            factor--;
         }

         return factor;
      } else {
         return 1;
      }
   }

   private SVGDocument document() {
      SVGDocument loaded = this.document;
      if (loaded == null) {
         loaded = loadDocument(this.textureId);
         this.document = loaded;
      }

      return loaded;
   }

   private FloatSize documentSize() {
      return this.document().size();
   }

   private static int quantize(float devicePx) {
      return Math.max(1, (int)Math.ceil(devicePx));
   }

   private static long packSize(int width, int height) {
      return (long)width << 32 | height & 4294967295L;
   }

   private static int unpackWidth(long key) {
      return (int)(key >>> 32);
   }

   private static int unpackHeight(long key) {
      return (int)key;
   }

   private static class_2960 variantId(class_2960 base, long key) {
      return base.method_45134(path -> path + "/" + unpackWidth(key) + "x" + unpackHeight(key));
   }

   private static class_1011 readPng(class_2960 textureId) {
      try (InputStream inputStream = MinecraftContext.mc.method_1478().open(textureId)) {
         return class_1011.method_4309(inputStream);
      } catch (IOException exception) {
         throw new IllegalStateException("Failed to read GUI texture: " + textureId, exception);
      }
   }

   private static SVGDocument loadDocument(class_2960 textureId) {
      try (InputStream inputStream = MinecraftContext.mc.method_1478().open(textureId)) {
         URI documentUri = URI.create("resource://" + textureId.method_12836() + "/" + textureId.method_12832());
         SVGDocument document = new SVGLoader().load(inputStream, documentUri, LoaderContext.createDefault());
         if (document == null) {
            throw new IOException("Failed to parse SVG document: " + textureId);
         } else {
            return document;
         }
      } catch (IOException exception) {
         throw new IllegalStateException("Failed to read GUI texture: " + textureId, exception);
      }
   }

   private static class_1011 rasterizeSvg(class_2960 textureId, SVGDocument document, int width, int height, int oversample) {
      int rasterWidth = width * oversample;
      int rasterHeight = height * oversample;
      BufferedImage bufferedImage = new BufferedImage(rasterWidth, rasterHeight, 3);
      Graphics2D graphics = bufferedImage.createGraphics();

      try {
         graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
         graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
         document.render(null, graphics, new ViewBox(0.0F, 0.0F, rasterWidth, rasterHeight));
      } finally {
         graphics.dispose();
      }

      int[] premultiplied = ((DataBufferInt)bufferedImage.getRaster().getDataBuffer()).getData();
      int clearBorder = textureId.method_12832().contains("logo_boot") ? Math.max(1, Math.round(4.0F * width / 512.0F)) : 0;
      class_1011 nativeImage = new class_1011(class_1012.field_4997, width, height, false);

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            if (clearBorder <= 0 || x >= clearBorder && x < width - clearBorder && y >= clearBorder && y < height - clearBorder) {
               nativeImage.method_61941(x, y, boxFilter(premultiplied, rasterWidth, x * oversample, y * oversample, oversample));
            } else {
               nativeImage.method_61941(x, y, 16777215);
            }
         }
      }

      return nativeImage;
   }

   private static int boxFilter(int[] premultiplied, int stride, int startX, int startY, int size) {
      int alpha = 0;
      int red = 0;
      int green = 0;
      int blue = 0;

      for (int y = startY; y < startY + size; y++) {
         int row = y * stride;

         for (int x = startX; x < startX + size; x++) {
            int argb = premultiplied[row + x];
            alpha += argb >>> 24 & 0xFF;
            red += argb >>> 16 & 0xFF;
            green += argb >>> 8 & 0xFF;
            blue += argb & 0xFF;
         }
      }

      int samples = size * size;
      int averageAlpha = alpha / samples;
      return averageAlpha == 0
         ? 16777215
         : averageAlpha << 24
            | unpremultiply(red, samples, averageAlpha) << 16
            | unpremultiply(green, samples, averageAlpha) << 8
            | unpremultiply(blue, samples, averageAlpha);
   }

   private static int unpremultiply(int channelSum, int samples, int averageAlpha) {
      int value = Math.round((float)channelSum / samples * 255.0F / averageAlpha);
      return Math.min(255, Math.max(0, value));
   }
}
