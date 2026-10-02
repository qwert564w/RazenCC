package org.ryzen.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class RegisteredListener {
   final Object owner;
   final EventInvoker invoker;
   final int priority;
   final String methodName;

   RegisteredListener(Object owner, EventInvoker invoker, int priority, String methodName) {
      this.owner = owner;
      this.invoker = invoker;
      this.priority = priority;
      this.methodName = methodName;
   }
}
