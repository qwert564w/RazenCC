package org.ryzen.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.command.ClientCommand;
import org.ryzen.command.CommandManager;
import org.ryzen.command.KeyNames;
import org.ryzen.command.Macro;
import org.ryzen.feature.setting.BindSetting;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class MacroCommand extends ClientCommand {
   private final CommandManager manager;

   public MacroCommand(CommandManager manager) {
      super("macro", "Sends a chat message or command on key press", ":speech_balloon:");
      this.manager = manager;
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> {
         ChatUtil.usage("macro add <name> <key> <text>  •  remove <name>  •  list");
         return 1;
      });
      builder.then(
         LiteralArgumentBuilder.literal("add")
            .then(
               RequiredArgumentBuilder.argument("name", StringArgumentType.word())
                  .then(
                     RequiredArgumentBuilder.argument("key", StringArgumentType.word())
                        .then(RequiredArgumentBuilder.argument("text", StringArgumentType.greedyString()).executes(this::executeAdd))
                  )
            )
      );
      builder.then(
         LiteralArgumentBuilder.literal("remove").then(RequiredArgumentBuilder.argument("name", StringArgumentType.word()).executes(this::executeRemove))
      );
      builder.then(LiteralArgumentBuilder.literal("list").executes(this::executeList));
   }

   private int executeAdd(CommandContext<Object> context) {
      String name = StringArgumentType.getString(context, "name");
      String keyName = StringArgumentType.getString(context, "key");
      String text = StringArgumentType.getString(context, "text");
      int bindCode = KeyNames.parse(keyName);
      if (bindCode == -1) {
         ChatUtil.error("Unknown key  •  " + keyName);
         return 0;
      } else {
         this.manager.putMacro(new Macro(name, bindCode, text));
         ChatUtil.success("Macro " + name + "  •  " + BindSetting.describe(bindCode) + "  •  " + text);
         return 1;
      }
   }

   private int executeRemove(CommandContext<Object> context) {
      String name = StringArgumentType.getString(context, "name");
      if (this.manager.removeMacro(name)) {
         ChatUtil.success("Macro removed  •  " + name);
         return 1;
      } else {
         ChatUtil.error("Macro not found  •  " + name);
         return 0;
      }
   }

   private int executeList(CommandContext<Object> context) {
      if (this.manager.getMacros().isEmpty()) {
         ChatUtil.info("No macros saved");
         return 1;
      }

      for (Macro macro : this.manager.getMacros()) {
         ChatUtil.entry(":speech_balloon:", macro.name(), BindSetting.describe(macro.bindCode()) + "  •  " + macro.text());
      }

      return 1;
   }
}
