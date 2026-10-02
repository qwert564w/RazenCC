package org.ryzen.feature.impl.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1533;
import net.minecraft.class_1806;
import net.minecraft.class_22;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3620;
import net.minecraft.class_3965;
import net.minecraft.class_2350.class_2351;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class CaptchaSolverFeature extends Feature {
   private static final Pattern SAFE_CODE = Pattern.compile("[A-Za-z0-9]{2,12}");
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
   public final TextSetting solverUrl = this.register(new TextSetting("Solver URL", "", 256));
   public final BooleanSetting autoSend = this.register(new BooleanSetting("Auto Send", true));
   public final BooleanSetting saveImage = this.register(new BooleanSetting("Save Image", true));
   private byte[] lastImage = new byte[0];
   private int tick;
   private volatile boolean solving;

   public CaptchaSolverFeature() {
      super("Captcha Solver", "Captures map captchas and submits them to an OCR service", FeatureCategory.PLAYER, -1);
   }

   @Override
   protected void onDisable() {
      this.lastImage = new byte[0];
      this.solving = false;
      this.tick = 0;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      if (client.field_1687 != null && client.field_1724 != null && !this.solving && ++this.tick >= 10 && client.field_1765 instanceof class_3965 hit) {
         this.tick = 0;
         List<class_1533> frames = client.field_1687
            .method_8390(
               class_1533.class,
               new class_238(hit.method_17777()).method_1014(16.0),
               frame -> frame.method_43273() && class_1806.method_8001(frame.method_6940(), client.field_1687) != null
            );
         if (!frames.isEmpty()) {
            class_1533 origin = frames.stream()
               .min(Comparator.comparingDouble(frame -> frame.method_5707(CaptchaSolverFeature.Vec3Holder.center(hit.method_17777()))))
               .orElse(null);
            if (origin != null) {
               class_2350 facing = origin.method_5735();
               frames = frames.stream().filter(frame -> frame.method_5735() == facing).toList();
               byte[] png = this.render(client, frames, facing);
               if (png.length != 0 && !Arrays.equals(png, this.lastImage)) {
                  this.lastImage = png;
                  if (this.saveImage.getValue()) {
                     save(client, png);
                  }

                  String endpoint = this.solverUrl.getValue().trim();
                  if (endpoint.isEmpty()) {
                     ChatUtil.info("Captcha captured. Set Solver URL to enable automatic recognition.");
                  } else {
                     this.submit(client, endpoint, png);
                  }
               }
            }
         }
      }
   }

   private byte[] render(class_310 client, List<class_1533> frames, class_2350 facing) {
      boolean alongZ = facing.method_10166() == class_2351.field_11051;
      boolean flip = facing == class_2350.field_11043 || facing == class_2350.field_11034;
      int minU = Integer.MAX_VALUE;
      int maxU = Integer.MIN_VALUE;
      int minV = Integer.MAX_VALUE;
      int maxV = Integer.MIN_VALUE;

      for (class_1533 frame : frames) {
         class_2338 pos = frame.method_24515();
         int u = alongZ ? pos.method_10263() : pos.method_10260();
         minU = Math.min(minU, u);
         maxU = Math.max(maxU, u);
         minV = Math.min(minV, pos.method_10264());
         maxV = Math.max(maxV, pos.method_10264());
      }

      if (minU <= maxU && minV <= maxV && maxU - minU <= 8 && maxV - minV <= 8) {
         BufferedImage image = new BufferedImage((maxU - minU + 1) * 128, (maxV - minV + 1) * 128, 2);
         Graphics2D graphics = image.createGraphics();
         graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

         for (class_1533 frame : frames) {
            class_22 data = class_1806.method_8001(frame.method_6940(), client.field_1687);
            if (data != null && data.field_122 != null && data.field_122.length == 16384) {
               BufferedImage tile = new BufferedImage(128, 128, 2);

               for (int index = 0; index < data.field_122.length; index++) {
                  int raw = data.field_122[index] & 255;
                  int color = raw < 4 ? 0 : class_3620.method_38480(raw);
                  tile.setRGB(index % 128, index / 128, color);
               }

               class_2338 pos = frame.method_24515();
               int u = alongZ ? pos.method_10263() : pos.method_10260();
               int x = (flip ? maxU - u : u - minU) * 128;
               int y = (maxV - pos.method_10264()) * 128;
               AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
               transform.rotate(Math.toRadians((frame.method_6934() & 7) * 45.0), 64.0, 64.0);
               graphics.drawImage(tile, transform, null);
            }
         }

         graphics.dispose();

         try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return output.toByteArray();
         } catch (Exception ignored) {
            return new byte[0];
         }
      } else {
         return new byte[0];
      }
   }

   private void submit(class_310 client, String endpoint, byte[] png) {
      this.solving = true;
      JsonObject payload = new JsonObject();
      payload.addProperty("image", Base64.getEncoder().encodeToString(png));

      HttpRequest request;
      try {
         request = HttpRequest.newBuilder(URI.create(endpoint))
            .timeout(Duration.ofSeconds(20L))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
            .build();
      } catch (IllegalArgumentException invalidUrl) {
         this.solving = false;
         ChatUtil.error("Captcha Solver URL is invalid");
         return;
      }

      HTTP.sendAsync(request, BodyHandlers.ofString(StandardCharsets.UTF_8))
         .orTimeout(22L, TimeUnit.SECONDS)
         .whenComplete((response, error) -> client.execute(() -> {
            this.solving = false;
            if (this.isEnabled()) {
               if (error == null && response != null && response.statusCode() / 100 == 2) {
                  String code = extractCode(response.body());
                  if (code == null) {
                     ChatUtil.error("Captcha service returned no code");
                  } else {
                     ChatUtil.success("Captcha recognized: " + code);
                     if (this.autoSend.getValue() && client.field_1724 != null && client.field_1724.field_3944 != null) {
                        client.field_1724.field_3944.method_45729(code);
                     }
                  }
               } else {
                  ChatUtil.error("Captcha recognition failed");
               }
            }
         }));
   }

   private static String extractCode(String body) {
      if (body == null) {
         return null;
      }

      String value = body.trim();

      try {
         JsonObject json = JsonParser.parseString(value).getAsJsonObject();
         if (json.has("code")) {
            value = json.get("code").getAsString().trim();
         } else if (json.has("text")) {
            value = json.get("text").getAsString().trim();
         }
      } catch (Throwable var3) {
      }

      value = value.replaceAll("[^A-Za-z0-9]", "");
      return SAFE_CODE.matcher(value).matches() ? value : null;
   }

   private static void save(class_310 client, byte[] png) {
      CompletableFuture.runAsync(() -> {
         try {
            Path directory = client.field_1697.toPath().resolve("ryzen");
            Files.createDirectories(directory);
            Path temporary = directory.resolve("captcha.png.tmp");
            Files.write(temporary, png);
            Files.move(temporary, directory.resolve("captcha.png"), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (Exception var4) {
         }
      });
   }

   @Environment(EnvType.CLIENT)
   private static final class Vec3Holder {
      private static class_243 center(class_2338 pos) {
         return class_243.method_24953(pos);
      }
   }
}
