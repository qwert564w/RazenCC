package ru.fiw.proxyserver;

import java.util.ArrayList;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_4286;
import net.minecraft.class_437;

@Environment(EnvType.CLIENT)
public final class GuiProxy extends class_437 {
   private static final int WIDTH = 200;
   private static final int HEIGHT = 20;
   private final class_437 parent;
   private final TestPing testPing = new TestPing();
   private boolean socks4;
   private boolean enabled;
   private String savedIp;
   private String savedUser;
   private String savedPassword;
   private String message = "";
   private class_342 ipPort;
   private class_342 username;
   private class_342 password;
   private class_342 nameInput;
   private class_4286 enabledCheck;
   private int startY;
   private int centerX;

   public GuiProxy(class_437 parent) {
      super(class_2561.method_43470("Proxy Settings"));
      this.parent = parent;
      Config.loadConfig();
      Proxy current = ProxyServer.proxy.copy();
      this.socks4 = current.type == Proxy.ProxyType.SOCKS4;
      this.enabled = ProxyServer.proxyEnabled;
      this.savedIp = current.ipPort;
      this.savedUser = current.username;
      this.savedPassword = current.password;
   }

   protected void method_25426() {
      this.centerX = this.field_22789 / 2;
      this.startY = Math.max(28, this.field_22790 / 2 - 90);
      int x = this.centerX - 100;
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Type: " + (this.socks4 ? "Socks 4" : "Socks 5")), button -> {
         this.captureFields();
         this.socks4 = !this.socks4;
         this.method_41843();
      }).method_46434(x, this.startY, 200, 20).method_46431());
      this.ipPort = this.field(x, this.startY + 24, "e.g. 125.1.34.1:2555", this.savedIp);
      this.username = this.field(x, this.startY + 48, this.socks4 ? "e.g. UserID123" : "e.g. username123", this.savedUser);
      if (!this.socks4) {
         this.password = this.field(x, this.startY + 72, "e.g. myPassword123", this.savedPassword);
      } else {
         this.password = null;
      }

      int enabledY = this.startY + (this.socks4 ? 76 : 100);
      this.enabledCheck = class_4286.method_54787(class_2561.method_43470("Enable Proxy"), this.field_22793)
         .method_54789(x, enabledY)
         .method_54794(this.enabled)
         .method_54791((checkbox, checked) -> this.enabled = checked)
         .method_54788();
      this.method_37063(this.enabledCheck);
      int actionY = this.startY + (this.socks4 ? 105 : 129);
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Apply"), button -> this.apply()).method_46434(x, actionY, 64, 20).method_46431());
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Test"), button -> this.test()).method_46434(x + 68, actionY, 64, 20).method_46431());
      this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Cancel"), button -> this.method_25419()).method_46434(x + 136, actionY, 64, 20).method_46431()
      );
      int saveY = this.startY + (this.socks4 ? 132 : 156);
      this.nameInput = this.field(x, saveY, "Name", "", 120);
      this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Save"), button -> this.savePreset()).method_46434(x + 124, saveY, 76, 20).method_46431()
      );
      int presetY = this.startY + (this.socks4 ? 160 : 184);

      for (Entry<String, Proxy> entry : new ArrayList<>(Config.accounts.entrySet())) {
         String name = entry.getKey();
         Proxy preset = entry.getValue().copy();
         this.method_37063(
            class_4185.method_46430(class_2561.method_43470(name), button -> this.loadPreset(preset)).method_46434(x, presetY, 175, 20).method_46431()
         );
         this.method_37063(class_4185.method_46430(class_2561.method_43470("X"), button -> {
            Config.removeAccount(name);
            this.method_41843();
         }).method_46434(x + 180, presetY, 20, 20).method_46431());
         presetY += 22;
      }
   }

   private class_342 field(int x, int y, String hint, String value) {
      return this.field(x, y, hint, value, 200);
   }

   private class_342 field(int x, int y, String hint, String value, int width) {
      class_342 field = new class_342(this.field_22793, x, y, width, 20, class_2561.method_43473());
      field.method_1880(256);
      field.method_47404(class_2561.method_43470(hint).method_27692(class_124.field_1063));
      field.method_1852(value == null ? "" : value);
      this.method_37063(field);
      return field;
   }

   private void captureFields() {
      if (this.ipPort != null) {
         this.savedIp = this.ipPort.method_1882().trim();
      }

      if (this.username != null) {
         this.savedUser = this.username.method_1882();
      }

      if (this.password != null) {
         this.savedPassword = this.password.method_1882();
      }

      if (this.enabledCheck != null) {
         this.enabled = this.enabledCheck.method_20372();
      }
   }

   private Proxy editedProxy() {
      this.captureFields();
      return new Proxy(this.socks4, this.savedIp, this.savedUser, this.savedPassword);
   }

   private void apply() {
      Proxy proxy = this.editedProxy();
      if (this.enabled && !proxy.isUsable()) {
         this.message = class_124.field_1061 + "Use host:port";
      } else {
         ProxyServer.proxy = proxy;
         ProxyServer.proxyEnabled = this.enabled;
         Config.saveConfig();
         this.field_22787.method_1507(this.parent);
      }
   }

   private void test() {
      Proxy proxy = this.editedProxy();
      if (!proxy.isUsable()) {
         this.message = class_124.field_1061 + "Use host:port";
      } else {
         this.message = "";
         this.testPing.run("mc.hypixel.net", 25565, proxy);
      }
   }

   private void savePreset() {
      Proxy proxy = this.editedProxy();
      if (!proxy.isUsable()) {
         this.message = class_124.field_1061 + "Use host:port";
      } else {
         String name = this.nameInput.method_1882().trim();
         if (name.isEmpty()) {
            name = "Proxy_" + System.currentTimeMillis();
         }

         Config.putAccount(name, proxy);
         this.message = class_124.field_1060 + "Saved " + name;
         this.method_41843();
      }
   }

   private void loadPreset(Proxy preset) {
      this.captureFields();
      this.socks4 = preset.type == Proxy.ProxyType.SOCKS4;
      this.savedIp = preset.ipPort;
      this.savedUser = preset.username;
      this.savedPassword = preset.password;
      this.message = "";
      this.method_41843();
   }

   public void method_25419() {
      this.field_22787.method_1507(this.parent);
   }

   public void method_25394(class_332 graphics, int mouseX, int mouseY, float partialTick) {
      super.method_25394(graphics, mouseX, mouseY, partialTick);
      graphics.method_27534(this.field_22793, this.field_22785, this.centerX, this.startY - 20, -1);
      String status = this.message.isEmpty() ? this.testPing.state : this.message;
      if (status != null && !status.isEmpty()) {
         graphics.method_27534(this.field_22793, class_2561.method_43470(status), this.centerX, this.startY - 9, -1);
      }
   }
}
