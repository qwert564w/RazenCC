package dev.ryzen.client.mixin;

import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_320;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(class_310.class)
public interface MinecraftAccessor {
   @Accessor("field_1726")
   @Mutable
   void setUser(class_320 var1);

   @Accessor("field_45899")
   @Mutable
   void setProfileFuture(CompletableFuture<ProfileResult> var1);
}
