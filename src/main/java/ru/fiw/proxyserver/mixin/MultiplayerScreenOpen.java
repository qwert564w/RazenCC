package ru.fiw.proxyserver.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_500;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fiw.proxyserver.Config;
import ru.fiw.proxyserver.GuiProxy;
import ru.fiw.proxyserver.ProxyServer;

@Environment(EnvType.CLIENT)
@Mixin(class_500.class)
public abstract class MultiplayerScreenOpen extends class_437 {
   private static final int BUTTON_WIDTH = 120;
   private static final int BUTTON_HEIGHT = 20;
   private static final int RIGHT_MARGIN = 2;
   private static final int VIAFABRICPLUS_WIDTH = 106;

   protected MultiplayerScreenOpen(class_2561 title) {
      super(title);
   }

   @Inject(method = "method_25426", at = @At("TAIL"))
   private void proxyserver$multiplayerGuiOpen(CallbackInfo callbackInfo) {
      Config.activateForPlayer(this.field_22787.method_1548().method_1676());
      this.method_37063(
         class_4185.method_46430(
               class_2561.method_43470("Proxy: " + ProxyServer.getLastUsedProxyIp()), button -> this.field_22787.method_1507(new GuiProxy(this))
            )
            .method_46434(this.field_22789 - 120 - 106 - 2, 5, 120, 20)
            .method_46431()
      );
   }
}
