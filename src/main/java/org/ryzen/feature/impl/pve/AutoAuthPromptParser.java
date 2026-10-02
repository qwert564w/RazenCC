package org.ryzen.feature.impl.pve;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class AutoAuthPromptParser {
   private static final Pattern FORMATTING_CODE = Pattern.compile("(?i)§[0-9A-FK-ORX]");
   private static final Pattern ZERO_WIDTH = Pattern.compile("[\\u200B-\\u200D\\u2060\\uFEFF]");
   private static final Pattern WHITESPACE = Pattern.compile("\\s+");
   private static final Pattern REGISTER_COMMAND = Pattern.compile("(?<![\\p{L}\\p{N}_])/(?:register|reg)(?=\\s|$)", 66);
   private static final Pattern LOGIN_COMMAND = Pattern.compile("(?<![\\p{L}\\p{N}_])/(?:login|l)(?=\\s|$)", 66);
   private static final Pattern REGISTER_PROMPT = Pattern.compile(
      "(?:\\b(?:please\\s+register|register\\s+(?:now|using|with|by))\\b|\\b(?:must\\s+|(?:need|required|have)\\s+to\\s+)register\\b|\\b(?:choose|create|set|repeat|confirm)\\s+(?:a\\s+|your\\s+)?password\\b|(?:зарегистриру(?:йтесь|йся)|необходимо\\s+зарегистрироваться)|(?:придумайте|создайте|повторите|подтвердите)\\s+(?:свой\\s+)?парол\\p{L}*|регистрац\\p{L}*\\s*[:\\-]?\\s*(?:введите\\s+)?парол\\p{L}*)",
      66
   );
   private static final Pattern LOGIN_PROMPT = Pattern.compile(
      "(?:\\bplease\\s+(?:login|log\\s+in)\\b|\\b(?:must\\s+|(?:need|required|have)\\s+to\\s+)(?:login|log\\s+in)\\b|\\b(?:enter|type|provide)\\s+(?:your\\s+)?password\\b|(?:авторизуйтесь|войдите)(?:\\s+в\\s+аккаунт)?|(?:введите|напишите|укажите)\\s+(?:свой\\s+)?парол\\p{L}*)",
      66
   );

   private AutoAuthPromptParser() {
   }

   public static Optional<AutoAuthPromptParser.Prompt> parse(String message) {
      String normalized = normalize(message);
      if (normalized.isEmpty()) {
         return Optional.empty();
      } else if (REGISTER_COMMAND.matcher(normalized).find() || REGISTER_PROMPT.matcher(normalized).find()) {
         return Optional.of(AutoAuthPromptParser.Prompt.REGISTER);
      } else {
         return !LOGIN_COMMAND.matcher(normalized).find() && !LOGIN_PROMPT.matcher(normalized).find()
            ? Optional.empty()
            : Optional.of(AutoAuthPromptParser.Prompt.LOGIN);
      }
   }

   static String normalize(String message) {
      if (message != null && !message.isBlank()) {
         String normalized = Normalizer.normalize(message, Form.NFKC);
         normalized = FORMATTING_CODE.matcher(normalized).replaceAll("");
         normalized = ZERO_WIDTH.matcher(normalized).replaceAll("");
         normalized = WHITESPACE.matcher(normalized).replaceAll(" ").trim();
         return normalized.toLowerCase(Locale.ROOT);
      } else {
         return "";
      }
   }

   @Environment(EnvType.CLIENT)
   public enum Prompt {
      LOGIN,
      REGISTER;
   }
}
