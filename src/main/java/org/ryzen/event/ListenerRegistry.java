package org.ryzen.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class ListenerRegistry {
   private volatile ListenerSnapshot snapshot = ListenerSnapshot.EMPTY;

   synchronized void add(RegisteredListener listener) {
      RegisteredListener[] current = this.snapshot.metadata;
      int insertIndex = current.length;

      for (int i = 0; i < current.length; i++) {
         if (listener.priority > current[i].priority) {
            insertIndex = i;
            break;
         }
      }

      RegisteredListener[] updated = new RegisteredListener[current.length + 1];
      System.arraycopy(current, 0, updated, 0, insertIndex);
      updated[insertIndex] = listener;
      System.arraycopy(current, insertIndex, updated, insertIndex + 1, current.length - insertIndex);
      this.snapshot = createSnapshot(updated);
   }

   synchronized void remove(RegisteredListener listener) {
      RegisteredListener[] current = this.snapshot.metadata;
      int removeIndex = -1;

      for (int i = 0; i < current.length; i++) {
         if (current[i] == listener) {
            removeIndex = i;
            break;
         }
      }

      if (removeIndex != -1) {
         if (current.length == 1) {
            this.snapshot = ListenerSnapshot.EMPTY;
         } else {
            RegisteredListener[] updated = new RegisteredListener[current.length - 1];
            System.arraycopy(current, 0, updated, 0, removeIndex);
            System.arraycopy(current, removeIndex + 1, updated, removeIndex, current.length - removeIndex - 1);
            this.snapshot = createSnapshot(updated);
         }
      }
   }

   ListenerSnapshot snapshot() {
      return this.snapshot;
   }

   boolean hasListeners() {
      return this.snapshot.metadata.length != 0;
   }

   synchronized void clear() {
      this.snapshot = ListenerSnapshot.EMPTY;
   }

   private static ListenerSnapshot createSnapshot(RegisteredListener[] listeners) {
      int size = listeners.length;
      Object[] owners = new Object[size];
      EventInvoker[] invokers = new EventInvoker[size];

      for (int i = 0; i < size; i++) {
         RegisteredListener listener = listeners[i];
         owners[i] = listener.owner;
         invokers[i] = listener.invoker;
      }

      return new ListenerSnapshot(listeners, owners, invokers);
   }
}
