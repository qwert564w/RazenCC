package org.ryzen.utils.render.chams;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import org.ryzen.utils.render.post.PostPipelines;

@Environment(EnvType.CLIENT)
public final class PopChamsRenderTypes {
   private static final Map<PopChamsRenderTypes.Key, class_1921> CACHE = new HashMap<>();

   private PopChamsRenderTypes() {
   }

   public static class_1921 model(class_2960 texture, boolean textured, boolean additive) {
      PopChamsRenderTypes.Kind kind;
      if (additive) {
         kind = textured ? PopChamsRenderTypes.Kind.ADDITIVE : PopChamsRenderTypes.Kind.ADDITIVE_SOLID;
      } else {
         kind = textured ? PopChamsRenderTypes.Kind.TRANSLUCENT : PopChamsRenderTypes.Kind.TRANSLUCENT_SOLID;
      }

      return get(texture, kind);
   }

   public static class_1921 mask(class_2960 texture, boolean textured) {
      return get(texture, textured ? PopChamsRenderTypes.Kind.MASK : PopChamsRenderTypes.Kind.MASK_SOLID);
   }

   private static class_1921 get(class_2960 texture, PopChamsRenderTypes.Kind kind) {
      return CACHE.computeIfAbsent(new PopChamsRenderTypes.Key(texture, kind), key -> {
         class_12247 setup = class_12247.method_75927(kind.pipeline).method_75934("Sampler0", texture).method_75938();
         try {
            var m = class_1921.class.getDeclaredMethod("method_75940", String.class, class_12247.class);
            m.setAccessible(true);
            return (class_1921) m.invoke(null, "blade_popchams_" + kind.name().toLowerCase(), setup);
         } catch (Exception e) { throw new RuntimeException(e); }
      });
   }

   @Environment(EnvType.CLIENT)
   private record Key(class_2960 texture, PopChamsRenderTypes.Kind kind) {
   }

   @Environment(EnvType.CLIENT)
   private enum Kind {
      ADDITIVE(PostPipelines.POPCHAMS_ADDITIVE),
      ADDITIVE_SOLID(PostPipelines.POPCHAMS_ADDITIVE_SOLID),
      TRANSLUCENT(PostPipelines.POPCHAMS_TRANSLUCENT),
      TRANSLUCENT_SOLID(PostPipelines.POPCHAMS_TRANSLUCENT_SOLID),
      MASK(PostPipelines.POPCHAMS_MASK),
      MASK_SOLID(PostPipelines.POPCHAMS_MASK_SOLID);

      private final RenderPipeline pipeline;

      Kind(RenderPipeline pipeline) {
         this.pipeline = pipeline;
      }
   }
}
