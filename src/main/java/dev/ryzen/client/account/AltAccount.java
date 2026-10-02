package dev.ryzen.client.account;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record AltAccount(UUID id, String nickname, Instant createdAt, boolean favorite) {
   public AltAccount {
      Objects.requireNonNull(id, "id");
      Objects.requireNonNull(nickname, "nickname");
      Objects.requireNonNull(createdAt, "createdAt");
   }

   public AltAccount withFavorite(boolean favorite) {
      return new AltAccount(this.id, this.nickname, this.createdAt, favorite);
   }
}
