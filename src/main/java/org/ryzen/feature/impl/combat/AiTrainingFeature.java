package org.ryzen.feature.impl.combat;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_332;
import net.minecraft.class_3417;
import net.minecraft.class_638;
import net.minecraft.class_745;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.AttackEvent;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.utils.combat.neuro.NeuroRotationData;
import org.ryzen.utils.combat.neuro.NeuroSample;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class AiTrainingFeature extends Feature implements MinecraftContext {
   private static final UUID DUMMY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
   private static final String DUMMY_NAME = "AiTrainingDummy";
   private class_745 dummy;
   private final NeuroRotationData buffer = new NeuroRotationData("recording");
   private int sampleCount;

   public AiTrainingFeature() {
      super("AiTraining", "Records combat samples on a training dummy", FeatureCategory.COMBAT, -1);
   }

   public NeuroRotationData recordingBuffer() {
      return this.buffer;
   }

   public int recordedSamples() {
      return this.sampleCount;
   }

   public void clearRecording() {
      this.buffer.clear();
      this.sampleCount = 0;
   }

   @Override
   protected void onEnable() {
      this.buffer.clear();
      this.sampleCount = 0;
      ChatUtil.info("AiTraining запущен  •  Двигайся и бей манекена — каждое движение записывается");
      this.spawnDummy();
   }

   @Override
   protected void onDisable() {
      this.removeDummy();
      ChatUtil.info("Записало " + this.sampleCount + " семплов  •  Чтобы сохранить и использовать в килке: .ai save <name>");
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.removeDummy();
   }

   @EventTarget
   public void onAttack(AttackEvent event) {
      if (this.dummy != null && mc.field_1724 != null && mc.field_1687 != null) {
         class_1297 target = mc.field_1692;
         if (target == this.dummy) {
            class_746 player = mc.field_1724;
            if (mc.field_1761 != null) {
               mc.field_1761.method_2918(player, this.dummy);
            }

            player.method_6104(class_1268.field_5808);
            mc.field_1687
               .method_8486(
                  this.dummy.method_23317(),
                  this.dummy.method_23318(),
                  this.dummy.method_23321(),
                  class_3417.field_14840,
                  player.method_5634(),
                  1.0F,
                  1.0F,
                  false
               );
            if (this.dummy.field_6235 <= 0) {
               this.dummy.field_6235 = 10;
               this.dummy.field_6254 = 10;
               this.dummy.field_6037 = true;
            }
         }
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      class_638 level = event.getClient().field_1687;
      if (player != null && level != null) {
         if (this.dummy != null && this.dummy.method_5805() && !this.dummy.method_31481()) {
            if (this.isMoving(player)) {
               NeuroSample sample = new NeuroSample(
                  player.method_36454(), player.method_36455(), player.field_6212, player.field_6250, player.method_5624(), !player.method_24828()
               );
               this.buffer.add(sample);
               this.sampleCount++;
            }
         } else {
            this.spawnDummy();
         }
      }
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      if (this.isEnabled() && mc.field_1724 != null) {
         class_332 graphics = event.getGuiGraphicsExtractor();
         float unit = 1.0F / mc.method_22683().method_4495();
         MsdfFont font = UiFonts.sfProDisplay();
         float fontSize = 16.0F * unit;
         float letterSpacing = fontSize * UiFontStyle.MEDIUM.letterSpacingEm();
         String label = "Обученно: " + this.sampleCount;
         float textWidth = font.measureWidth(label, fontSize, letterSpacing);
         float padding = 10.0F * unit;
         float pillWidth = textWidth + padding * 2.0F;
         float pillHeight = 28.0F * unit;
         float screenWidth = mc.method_22683().method_4486();
         float screenHeight = mc.method_22683().method_4502();
         float x = (screenWidth - pillWidth) / 2.0F;
         float y = screenHeight / 2.0F + 14.0F * unit;
         Render2DUtil.rect(x, y, pillWidth, pillHeight)
            .color(Theme.Colors.BACKGROUND_PRIMARY_50)
            .radius(pillHeight / 2.0F)
            .border(Math.max(0.5F, 0.5F * unit), Theme.Colors.OUTLINES_SMALL)
            .blur(8.0F * unit)
            .draw();
         float centerY = y + pillHeight / 2.0F;
         Render2DUtil.text(x + padding, font.centeredTextY(centerY, fontSize), fontSize, label).style(UiFontStyle.MEDIUM).color(Theme.getAccent()).draw();
      }
   }

   private boolean isMoving(class_746 player) {
      return Math.abs(player.field_6212) > 0.01F || Math.abs(player.field_6250) > 0.01F || player.method_5624() || !player.method_24828();
   }

   private void spawnDummy() {
      class_746 player = mc.field_1724;
      class_638 level = mc.field_1687;
      if (player != null && level != null) {
         this.removeDummy();
         GameProfile profile = new GameProfile(DUMMY_UUID, "AiTrainingDummy");
         this.dummy = new class_745(level, profile);
         this.dummy.method_5814(player.method_23317(), player.method_23318(), player.method_23321());
         this.dummy.method_36456(player.method_36454() + 180.0F);
         this.dummy.field_6241 = player.method_36454() + 180.0F;

         try {
            level.method_53875(this.dummy);
         } catch (Throwable throwable) {
            this.dummy = null;
         }
      }
   }

   private void removeDummy() {
      if (this.dummy != null) {
         try {
            this.dummy.method_31472();
         } catch (Throwable var2) {
         }

         this.dummy = null;
      }
   }

   public class_1297 getDummy() {
      return this.dummy;
   }
}
