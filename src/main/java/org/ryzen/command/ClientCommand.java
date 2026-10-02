package org.ryzen.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class ClientCommand {
   private final String name;
   private final String description;
   private final String emoji;

   protected ClientCommand(String name, String description) {
      this(name, description, ":small_blue_diamond:");
   }

   protected ClientCommand(String name, String description, String emoji) {
      this.name = name;
      this.description = description;
      this.emoji = emoji;
   }

   public final String name() {
      return this.name;
   }

   public final String description() {
      return this.description;
   }

   public final String emoji() {
      return this.emoji;
   }

   public List<String> aliases() {
      return List.of();
   }

   public abstract void build(LiteralArgumentBuilder<Object> var1);
}
