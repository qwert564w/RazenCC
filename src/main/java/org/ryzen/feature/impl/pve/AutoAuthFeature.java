package org.ryzen.feature.impl.pve;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.Optional;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_642;
import net.minecraft.class_7438;
import net.minecraft.class_7439;
import net.minecraft.class_7827;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.DisconnectEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;

@Environment(EnvType.CLIENT)
public final class AutoAuthFeature extends PveFeature {
   public static final String MODE_GENERATED = "Generated";
   public static final String MODE_CUSTOM = "Custom Password";
   private static final long RESOURCE_RETRY_MILLIS = 250L;
   private static final long CREDENTIAL_RETRY_MILLIS = 5000L;
   private static final Pattern SAFE_PASSWORD = Pattern.compile("[A-Za-z0-9!@#$%^&*()_+\\-=.,:?~]{4,64}");
   public final ModeSetting passwordMode = this.register(new ModeSetting("Mode", "Generated", "Generated", "Custom Password"));
   public final TextSetting customPassword = this.register(this.customPasswordSetting());
   public final NumberSetting cooldown = this.register(new NumberSetting("Cooldown", 3.0, 1.0, 15.0, 0.5, " s"));
   private final AutoAuthCredentialStore credentialStore;
   private final ArrayDeque<AutoAuthPromptParser.Prompt> pendingPrompts = new ArrayDeque<>();
   private final EnumSet<AutoAuthPromptParser.Prompt> handledPrompts = EnumSet.noneOf(AutoAuthPromptParser.Prompt.class);
   private class_634 connection;
   private long nextActionAt;

   public AutoAuthFeature() {
      this(new AutoAuthCredentialStore());
   }

   AutoAuthFeature(AutoAuthCredentialStore credentialStore) {
      super("AutoAuth", "Automatically responds to server login and registration prompts", -1, AutomationPriority.FEATURE);
      this.credentialStore = credentialStore;
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE) {
         String text = incomingText(event.getPacket());
         Optional<AutoAuthPromptParser.Prompt> prompt = AutoAuthPromptParser.parse(text);
         if (!prompt.isEmpty()) {
            class_310 client = class_310.method_1551();
            client.execute(() -> {
               if (this.isEnabled()) {
                  this.enqueue(client, prompt.get());
               }
            });
         }
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      this.syncConnection(client);
      if (client.field_1724 != null && client.field_1687 != null && client.method_1558() != null && this.connection != null) {
         AutoAuthPromptParser.Prompt prompt = this.nextPrompt();
         if (prompt != null) {
            long now = System.currentTimeMillis();
            if (now >= this.nextActionAt) {
               AutoAuthCredentialStore.Scope scope = scope(client);

               Optional<String> password;
               try {
                  password = this.resolvePassword(scope);
               } catch (IOException | GeneralSecurityException exception) {
                  this.nextActionAt = now + 5000L;
                  return;
               }

               if (password.isEmpty()) {
                  this.nextActionAt = now + 5000L;
               } else if (!this.claim(AutomationResource.CHAT)) {
                  this.nextActionAt = now + 250L;
               } else {
                  String command = command(prompt, password.get());
                  boolean sent = false;

                  try {
                     client.field_1724.field_3944.method_45730(command);
                     sent = true;
                  } catch (RuntimeException ignored) {
                     this.nextActionAt = now + 250L;
                  } finally {
                     PveAutomationCoordinator.INSTANCE.release(this);
                  }

                  if (sent) {
                     this.pendingPrompts.removeFirstOccurrence(prompt);
                     this.handledPrompts.add(prompt);
                     this.nextActionAt = now + Math.round(this.cooldown.getValue() * 1000.0);
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onDisconnect(DisconnectEvent event) {
      this.resetRuntimeState();
   }

   @Override
   protected void onPveEnable() {
      this.resetRuntimeState();
   }

   @Override
   protected void onPveDisable() {
      this.resetRuntimeState();
      this.customPassword.setValue("");
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.resetRuntimeState();
      this.customPassword.setValue("");
   }

   static boolean isValidPassword(String password) {
      return password != null && SAFE_PASSWORD.matcher(password).matches();
   }

   static String incomingText(class_2596<?> packet) {
      if (packet instanceof class_7439 systemChat) {
         return systemChat.comp_763().getString();
      } else if (packet instanceof class_7827 disguisedChat) {
         return disguisedChat.comp_1097().getString();
      } else if (packet instanceof class_7438 playerChat) {
         class_2561 unsigned = playerChat.comp_1103();
         return unsigned != null ? unsigned.getString() : playerChat.comp_1102().comp_1090();
      } else {
         return null;
      }
   }

   private void enqueue(class_310 client, AutoAuthPromptParser.Prompt prompt) {
      this.syncConnection(client);
      if (this.connection != null && !this.handledPrompts.contains(prompt) && !this.pendingPrompts.contains(prompt)) {
         this.pendingPrompts.addLast(prompt);
      }
   }

   private AutoAuthPromptParser.Prompt nextPrompt() {
      while (!this.pendingPrompts.isEmpty() && this.handledPrompts.contains(this.pendingPrompts.peekFirst())) {
         this.pendingPrompts.removeFirst();
      }

      return this.pendingPrompts.peekFirst();
   }

   private Optional<String> resolvePassword(AutoAuthCredentialStore.Scope scope) throws IOException, GeneralSecurityException {
      if (this.passwordMode.is("Generated")) {
         return Optional.of(this.credentialStore.generatedPassword(scope));
      }

      String entered = this.customPassword.getValue();
      if (!entered.isEmpty()) {
         if (!isValidPassword(entered)) {
            return Optional.empty();
         }

         this.credentialStore.saveCustomPassword(scope, entered);
         this.customPassword.setValue("");
         return Optional.of(entered);
      } else {
         return this.credentialStore.loadCustomPassword(scope).filter(AutoAuthFeature::isValidPassword);
      }
   }

   private void syncConnection(class_310 client) {
      class_634 current = client.method_1562();
      if (current != this.connection) {
         this.connection = current;
         this.pendingPrompts.clear();
         this.handledPrompts.clear();
         this.nextActionAt = 0L;
      }
   }

   private void resetRuntimeState() {
      this.connection = null;
      this.pendingPrompts.clear();
      this.handledPrompts.clear();
      this.nextActionAt = 0L;
      PveAutomationCoordinator.INSTANCE.release(this);
   }

   private static AutoAuthCredentialStore.Scope scope(class_310 client) {
      class_642 server = client.method_1558();
      String account = client.field_1724 != null ? client.field_1724.method_7334().name() : client.method_1548().method_1676();
      return new AutoAuthCredentialStore.Scope(server.field_3761, account);
   }

   private static String command(AutoAuthPromptParser.Prompt prompt, String password) {
      return switch (prompt) {
         case LOGIN -> "login " + password;
         case REGISTER -> "register " + password + " " + password;
      };
   }

   private TextSetting customPasswordSetting() {
      TextSetting setting = new TextSetting("Password", "", 64).secret();
      setting.nonPersistent();
      setting.visibleWhen(() -> this.passwordMode.is("Custom Password"));
      return setting;
   }
}
