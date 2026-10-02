package org.ryzen.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.command.ClientCommand;
import org.ryzen.feature.impl.combat.AiTrainingFeature;
import org.ryzen.utils.combat.neuro.NeuroManager;
import org.ryzen.utils.combat.neuro.NeuroRotationData;
import org.ryzen.utils.combat.neuro.NeuroSampleStore;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class AiCommand extends ClientCommand {
   public AiCommand() {
      super("ai", "Neuro rotation recordings: save/load/list/remove/select/dir", ":brain:");
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> {
         this.executeHelp();
         return 1;
      });
      builder.then(LiteralArgumentBuilder.literal("help").executes(context -> {
         this.executeHelp();
         return 1;
      }));
      builder.then(this.named("save", this::executeSave));
      builder.then(this.named("load", this::executeLoad));
      builder.then(this.named("remove", this::executeRemove));
      builder.then(this.named("select", this::executeSelect));
      builder.then(LiteralArgumentBuilder.literal("list").executes(context -> this.executeList()));
      builder.then(LiteralArgumentBuilder.literal("dir").executes(context -> this.executeDir()));
   }

   private void executeHelp() {
      ChatUtil.header("Ryzen AI — Neuro Rotations");
      ChatUtil.info(".ai help  •  Показать эту справку");
      ChatUtil.info(".ai save <name>  •  Сохранить записанные семплы в <name>.neuro");
      ChatUtil.info(".ai load <name>  •  Загрузить ротацию <name>.neuro");
      ChatUtil.info(".ai list  •  Список всех сохранённых нейро-ротаций");
      ChatUtil.info(".ai select <name>  •  Выбрать и встроить ротацию в килку");
      ChatUtil.info(".ai remove <name>  •  Удалить ротацию <name>.neuro");
      ChatUtil.info(".ai dir  •  Открыть папку с нейро-ротациями");
      ChatUtil.info("");
      ChatUtil.info("Как пользоваться:");
      ChatUtil.info("1. Включи AiTraining (Combat)  •  Бей манекена двигаясь");
      ChatUtil.info("2. Выключи AiTraining  •  Семплы записаны");
      ChatUtil.info("3. .ai save test  •  Сохраняет ротацию");
      ChatUtil.info("4. В Aura → Rotation выбери Neuro");
      ChatUtil.info("5. .ai select test  •  Встраивает ротацию в килку");
   }

   private LiteralArgumentBuilder<Object> named(String literal, Function<String, Integer> action) {
      return (LiteralArgumentBuilder<Object>)LiteralArgumentBuilder.literal(literal)
         .then(RequiredArgumentBuilder.argument("name", StringArgumentType.word()).executes(context -> action.apply(this.name(context))));
   }

   private int executeSave(String name) {
      if (!NeuroSampleStore.isValidName(name)) {
         ChatUtil.error("Неверное имя  •  Только A-Z, 0-9, _, -  •  " + name);
         return 0;
      }

      AiTrainingFeature training = NeuroManager.training();
      if (training != null && training.recordedSamples() != 0) {
         NeuroRotationData data = training.recordingBuffer();
         NeuroRotationData named = new NeuroRotationData(name);

         for (int i = 0; i < data.sampleCount(); i++) {
            named.add(data.samples().get(i));
         }

         if (NeuroSampleStore.save(named)) {
            ChatUtil.success("Успешно сохранено в " + name + ".neuro (" + named.sampleCount() + " семплов)");
            return 1;
         } else {
            ChatUtil.error("Не удалось сохранить  •  " + name);
            return 0;
         }
      } else {
         ChatUtil.error("Нет записанных семплов  •  Включи AiTraining и побей манекена");
         return 0;
      }
   }

   private int executeLoad(String name) {
      NeuroRotationData data = NeuroSampleStore.load(name);
      if (data == null) {
         ChatUtil.error("Ротация не найдена  •  " + name);
         return 0;
      } else {
         ChatUtil.success("Загружено  •  " + name + "  •  " + data.sampleCount() + " семплов");
         return 1;
      }
   }

   private int executeList() {
      List<String> names = NeuroSampleStore.listNames();
      if (names.isEmpty()) {
         ChatUtil.info("Нет нейро-ротаций  •  Запиши через AiTraining и сохрани .ai save <name>");
         return 1;
      }

      ChatUtil.header("Нейро-ротации  •  " + names.size());

      for (String name : names) {
         int count = NeuroSampleStore.sampleCount(name);
         String selected = name.equals(NeuroManager.selectedName()) ? "  •  выбрано" : "";
         ChatUtil.success(name + "  •  " + count + " семплов" + selected);
      }

      return 1;
   }

   private int executeRemove(String name) {
      if (NeuroSampleStore.delete(name)) {
         if (name.equals(NeuroManager.selectedName())) {
            NeuroManager.clearSelection();
         }

         ChatUtil.success("Удалено  •  " + name);
         return 1;
      } else {
         ChatUtil.error("Ротация не найдена  •  " + name);
         return 0;
      }
   }

   private int executeSelect(String name) {
      if (NeuroManager.select(name)) {
         ChatUtil.success("Выбрано и встроено в килку  •  " + name + "  •  " + NeuroManager.activeRotation().data().sampleCount() + " семплов");
         return 1;
      } else {
         ChatUtil.error("Ротация не найдена или пуста  •  " + name);
         return 0;
      }
   }

   private int executeDir() {
      Path dir = NeuroSampleStore.neuroDir();

      try {
         Files.createDirectories(dir);
      } catch (IOException var5) {
      }

      String path = dir.toAbsolutePath().toString();

      try {
         new ProcessBuilder("explorer.exe", path).start();
         ChatUtil.success("Папка открыта  •  " + path);
      } catch (IOException exception) {
         try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.OPEN)) {
               Desktop.getDesktop().open(dir.toFile());
               ChatUtil.success("Папка открыта  •  " + path);
            } else {
               ChatUtil.info("Папка  •  " + path);
            }
         } catch (IOException e) {
            ChatUtil.error("Не удалось открыть папку  •  " + path);
         }
      }

      return 1;
   }

   private String name(CommandContext<Object> context) {
      return StringArgumentType.getString(context, "name");
   }
}
