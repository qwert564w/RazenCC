package org.ryzen.utils.combat.neuro;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class NeuroRotationData {
   private final String name;
   private final List<NeuroSample> samples;

   public NeuroRotationData(String name) {
      this(name, new ArrayList<>());
   }

   public NeuroRotationData(String name, List<NeuroSample> samples) {
      this.name = name;
      this.samples = new ArrayList<>(samples);
   }

   public String name() {
      return this.name;
   }

   public List<NeuroSample> samples() {
      return this.samples;
   }

   public int sampleCount() {
      return this.samples.size();
   }

   public void add(NeuroSample sample) {
      this.samples.add(sample);
   }

   public void clear() {
      this.samples.clear();
   }

   public JsonObject toJson() {
      JsonObject root = new JsonObject();
      root.addProperty("name", this.name);
      root.addProperty("count", this.samples.size());
      JsonArray array = new JsonArray();

      for (NeuroSample sample : this.samples) {
         JsonObject s = new JsonObject();
         s.addProperty("yaw", sample.yaw());
         s.addProperty("pitch", sample.pitch());
         s.addProperty("moveX", sample.moveX());
         s.addProperty("moveZ", sample.moveZ());
         s.addProperty("sprinting", sample.sprinting());
         s.addProperty("jumping", sample.jumping());
         array.add(s);
      }

      root.add("samples", array);
      return root;
   }

   public static NeuroRotationData fromJson(String name, JsonElement root) {
      List<NeuroSample> samples = new ArrayList<>();
      if (root != null && root.isJsonObject()) {
         JsonObject obj = root.getAsJsonObject();
         if (obj.has("samples") && obj.get("samples").isJsonArray()) {
            for (JsonElement element : obj.getAsJsonArray("samples")) {
               if (element.isJsonObject()) {
                  JsonObject s = element.getAsJsonObject();
                  samples.add(
                     new NeuroSample(
                        s.get("yaw").getAsFloat(),
                        s.get("pitch").getAsFloat(),
                        s.has("moveX") ? s.get("moveX").getAsFloat() : 0.0F,
                        s.has("moveZ") ? s.get("moveZ").getAsFloat() : 0.0F,
                        s.has("sprinting") && s.get("sprinting").getAsBoolean(),
                        s.has("jumping") && s.get("jumping").getAsBoolean()
                     )
                  );
               }
            }
         }
      }

      return new NeuroRotationData(name, samples);
   }

   public boolean save(Path file) {
      try {
         Files.createDirectories(file.getParent());
         Path temp = file.resolveSibling(file.getFileName() + ".tmp");
         Files.writeString(
            temp, this.toJson().toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
         );

         try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
         }

         return true;
      } catch (IOException exception) {
         return false;
      }
   }

   public static NeuroRotationData load(Path file) {
      String name = stripExtension(file.getFileName().toString());
      if (!Files.exists(file)) {
         return null;
      }

      try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
         return fromJson(name, JsonParser.parseReader(reader));
      } catch (Exception exception) {
         return null;
      }
   }

   public static String stripExtension(String fileName) {
      int dot = fileName.lastIndexOf(46);
      return dot > 0 ? fileName.substring(0, dot) : fileName;
   }
}
