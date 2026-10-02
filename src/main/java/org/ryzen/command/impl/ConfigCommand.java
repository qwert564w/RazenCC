package org.ryzen.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.command.ClientCommand;
import org.ryzen.feature.FeatureManager;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class ConfigCommand extends ClientCommand {
   public ConfigCommand() {
      super("config", "Manages named configs: save/load/remove/list", ":gear:");
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> {
         ChatUtil.usage("config save <name>  •  load <name>  •  remove <name>  •  list");
         return 1;
      });
      builder.then(this.named("save", this::executeSave));
      builder.then(this.named("load", this::executeLoad));
      builder.then(this.named("remove", this::executeRemove));
      builder.then(LiteralArgumentBuilder.literal("list").executes(context -> {
         List<String> names = FeatureManager.INSTANCE.configNames();
         if (names.isEmpty()) {
            ChatUtil.info("No configs saved");
         } else {
            ChatUtil.header("Saved configs  •  " + names.size());
            names.forEach(name -> ChatUtil.entry(":floppy_disk:", name, null));
         }

         return 1;
      }));
   }

   private LiteralArgumentBuilder<Object> named(String literal, Function<String, Integer> action) {
      return (LiteralArgumentBuilder<Object>)LiteralArgumentBuilder.literal(literal)
         .then(RequiredArgumentBuilder.argument("name", StringArgumentType.word()).executes(context -> action.apply(this.name(context))));
   }

   private int executeSave(String name) {
      if (FeatureManager.INSTANCE.saveConfigAs(name)) {
         ChatUtil.success("Saved config  •  " + name);
         return 1;
      } else {
         ChatUtil.error("Invalid config name  •  Use letters, digits, - or _  •  " + name);
         return 0;
      }
   }

   private int executeLoad(String name) {
      if (FeatureManager.INSTANCE.loadConfig(name)) {
         ChatUtil.success("Loaded config  •  " + name);
         return 1;
      } else {
         ChatUtil.error("Config not found  •  " + name);
         return 0;
      }
   }

   private int executeRemove(String name) {
      if (FeatureManager.INSTANCE.deleteConfig(name)) {
         ChatUtil.success("Removed config  •  " + name);
         return 1;
      } else {
         ChatUtil.error("Config not found  •  " + name);
         return 0;
      }
   }

   private String name(CommandContext<Object> context) {
      return StringArgumentType.getString(context, "name");
   }
}
