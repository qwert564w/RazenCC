package org.ryzen.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class ListenerSnapshot {
   static final ListenerSnapshot EMPTY = new ListenerSnapshot(new RegisteredListener[0], new Object[0], new EventInvoker[0]);
   final RegisteredListener[] metadata;
   final Object[] owners;
   final EventInvoker[] invokers;

   ListenerSnapshot(RegisteredListener[] metadata, Object[] owners, EventInvoker[] invokers) {
      this.metadata = metadata;
      this.owners = owners;
      this.invokers = invokers;
   }
}
