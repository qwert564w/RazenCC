package org.ryzen.utils.text;

import java.util.Collection;
import java.util.StringJoiner;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class StringUtil {
   public static String abbreviate(String value, int maxLength) {
      if (value == null) {
         return "";
      }

      int length = Math.max(0, maxLength);
      return value.length() <= length ? value : value.substring(0, length);
   }

   public static String joinLimited(Collection<String> values, int maxVisible) {
      if (values != null && !values.isEmpty()) {
         int limit = Math.max(0, maxVisible);
         StringJoiner joiner = new StringJoiner(", ");
         int index = 0;

         for (String value : values) {
            if (index >= limit) {
               break;
            }

            joiner.add(value);
            index++;
         }

         if (values.size() > limit) {
            joiner.add("...");
         }

         return joiner.toString();
      } else {
         return "";
      }
   }

   @Generated
   private StringUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
