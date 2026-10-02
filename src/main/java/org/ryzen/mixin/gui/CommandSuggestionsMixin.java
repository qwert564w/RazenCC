package org.ryzen.mixin.gui;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_342;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_4717;
import net.minecraft.class_5481;
import org.ryzen.command.CommandManager;
import org.ryzen.utils.text.SensitiveChatMask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_4717.class)
public abstract class CommandSuggestionsMixin {
   @Shadow
   @Final
   private class_437 field_21598;
   @Shadow
   @Final
   private class_342 field_21599;
   @Shadow
   @Final
   private List<class_5481> field_21607;
   @Shadow
   private CompletableFuture<Suggestions> field_21611;
   @Shadow
   private boolean field_21614;
   @Shadow
   private boolean field_21613;

   @Shadow
   public abstract void method_23920(boolean var1);

   @Shadow
   public abstract void method_44931();

   @Inject(method = "method_23934", at = @At("HEAD"), cancellable = true)
   private void suggestClientCommands(CallbackInfo ci) {
      if (this.field_21598 instanceof class_408) {
         String text = this.field_21599.method_1882();
         if (SensitiveChatMask.hasSecret(text)) {
            ci.cancel();
            this.field_21599.method_1887(null);
            this.method_44931();
            this.field_21607.clear();
            this.field_21611 = null;
         } else {
            String prefix = CommandManager.INSTANCE.getPrefix();
            if (!prefix.equals("/") && text.startsWith(prefix)) {
               ci.cancel();
               if (!this.field_21614) {
                  this.field_21599.method_1887(null);
                  this.method_44931();
                  this.field_21607.clear();
                  StringReader reader = new StringReader(text);
                  reader.setCursor(prefix.length());
                  CommandDispatcher<Object> dispatcher = CommandManager.INSTANCE.getDispatcher();
                  ParseResults<Object> parse = dispatcher.parse(reader, new Object());
                  int cursor = Math.max(this.field_21599.method_1881(), prefix.length());
                  CompletableFuture<Suggestions> future = dispatcher.getCompletionSuggestions(parse, cursor);
                  this.field_21611 = future;
                  future.thenRun(() -> {
                     if (this.field_21611 == future && future.isDone() && !future.join().isEmpty() && this.field_21613) {
                        this.method_23920(false);
                     }
                  });
               }
            }
         }
      }
   }
}
