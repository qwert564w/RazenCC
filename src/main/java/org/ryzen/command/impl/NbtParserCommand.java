package org.ryzen.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.command.ClientCommand;
import org.ryzen.utils.ScreenNbtParser;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class NbtParserCommand extends ClientCommand {
   private final ScreenNbtParser parser = ScreenNbtParser.INSTANCE;

   public NbtParserCommand() {
      super("nbtparser", "Dumps container layouts and complete item NBT", ":mag:");
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> this.showStatus());
      builder.then(LiteralArgumentBuilder.literal("start").executes(context -> this.start()));
      builder.then(LiteralArgumentBuilder.literal("stop").executes(context -> this.stop()));
      builder.then(LiteralArgumentBuilder.literal("now").executes(context -> this.captureNow()));
      builder.then(LiteralArgumentBuilder.literal("clear").executes(context -> this.clear()));
      builder.then(LiteralArgumentBuilder.literal("status").executes(context -> this.showStatus()));
   }

   private int start() {
      class_310 client = class_310.method_1551();
      if (!this.parser.start(client)) {
         ChatUtil.error(this.parser.getLastError());
         return 0;
      } else {
         ChatUtil.success("NBT parser started  •  Open the required auction screens");
         ChatUtil.info("Log: " + this.displayPath(client));
         return 1;
      }
   }

   private int stop() {
      class_310 client = class_310.method_1551();
      if (!this.parser.stop(client)) {
         ChatUtil.error(this.parser.getLastError());
         return 0;
      } else {
         ChatUtil.success("NBT parser stopped  •  Snapshots: " + this.parser.getSnapshotCount());
         ChatUtil.info("Send this file: " + this.displayPath(client));
         return 1;
      }
   }

   private int captureNow() {
      class_310 client = class_310.method_1551();
      if (!this.parser.captureNow(client)) {
         ChatUtil.error(this.parser.getLastError());
         return 0;
      } else {
         ChatUtil.success("Current container captured  •  Snapshot " + this.parser.getSnapshotCount());
         return 1;
      }
   }

   private int clear() {
      class_310 client = class_310.method_1551();
      if (!this.parser.clear(client)) {
         ChatUtil.error(this.parser.getLastError());
         return 0;
      } else {
         ChatUtil.success("NBT parser log cleared");
         return 1;
      }
   }

   private int showStatus() {
      class_310 client = class_310.method_1551();
      ChatUtil.header("NBT parser");
      ChatUtil.info("State: " + (this.parser.isCapturing() ? "recording" : "stopped") + "  •  Snapshots: " + this.parser.getSnapshotCount());
      ChatUtil.info("Log: " + this.displayPath(client));
      ChatUtil.usage("nbtparser start  •  stop  •  now  •  clear  •  status");
      return 1;
   }

   private String displayPath(class_310 client) {
      Path gameDirectory = client.field_1697.toPath().toAbsolutePath().normalize();
      Path output = this.parser.getOutputPath(client).toAbsolutePath().normalize();

      try {
         return gameDirectory.relativize(output).toString();
      } catch (IllegalArgumentException ignored) {
         return output.toString();
      }
   }
}
