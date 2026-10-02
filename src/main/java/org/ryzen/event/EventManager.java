package org.ryzen.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class EventManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(EventManager.class);
   private static final ListenerRegistration[] EMPTY_REGISTRATIONS = new ListenerRegistration[0];
   private static final Object OWNER_REGISTRATIONS_LOCK = new Object();
   private static final ClassValue<ListenerDefinition[]> DEFINITION_CACHE = new ClassValue<ListenerDefinition[]>() {
      protected ListenerDefinition[] computeValue(Class<?> type) {
         return EventManager.scanDefinitions(type);
      }
   };
   private static final ConcurrentHashMap<Class<? extends Event>, ListenerRegistry> REGISTRIES = new ConcurrentHashMap<>();
   private static final IdentityHashMap<Object, ListenerRegistration[]> OWNER_REGISTRATIONS = new IdentityHashMap<>();

   public static void subscribe(Object listener) {
      if (listener != null) {
         ListenerDefinition[] definitions = DEFINITION_CACHE.get(listener.getClass());
         synchronized (OWNER_REGISTRATIONS_LOCK) {
            if (!OWNER_REGISTRATIONS.containsKey(listener)) {
               if (definitions.length == 0) {
                  OWNER_REGISTRATIONS.put(listener, EMPTY_REGISTRATIONS);
               } else {
                  ListenerRegistration[] registrations = new ListenerRegistration[definitions.length];

                  for (int i = 0; i < definitions.length; i++) {
                     ListenerDefinition definition = definitions[i];
                     RegisteredListener registered = new RegisteredListener(listener, definition.invoker(), definition.priority(), definition.methodName());
                     REGISTRIES.computeIfAbsent(definition.eventType(), ignored -> new ListenerRegistry()).add(registered);
                     registrations[i] = new ListenerRegistration(definition.eventType(), registered);
                  }

                  OWNER_REGISTRATIONS.put(listener, registrations);
               }
            }
         }
      }
   }

   public static void unsubscribe(Object listener) {
      if (listener != null) {
         ListenerRegistration[] registrations;
         synchronized (OWNER_REGISTRATIONS_LOCK) {
            registrations = OWNER_REGISTRATIONS.remove(listener);
         }

         if (registrations != null) {
            for (ListenerRegistration registration : registrations) {
               ListenerRegistry registry = REGISTRIES.get(registration.eventType());
               if (registry != null) {
                  registry.remove(registration.listener());
               }
            }
         }
      }
   }

   public static void unregisterAll() {
      synchronized (OWNER_REGISTRATIONS_LOCK) {
         OWNER_REGISTRATIONS.clear();
      }

      for (ListenerRegistry registry : REGISTRIES.values()) {
         registry.clear();
      }
   }

   public static boolean hasListeners(Class<? extends Event> eventType) {
      ListenerRegistry registry = REGISTRIES.get(eventType);
      return registry != null && registry.hasListeners();
   }

   public static <T extends Event> T call(T event) {
      if (event == null) {
         return null;
      }

      event.reset();
      ListenerRegistry registry = REGISTRIES.get(event.getClass());
      if (registry != null) {
         ListenerSnapshot snapshot = registry.snapshot();
         if (event instanceof CancellableEvent cancellableEvent) {
            dispatchCancellable(snapshot, event, cancellableEvent);
         } else {
            dispatchNonCancellable(snapshot, event);
         }
      }

      event.complete();
      return event;
   }

   public static <T extends Event> T callEvent(T event) {
      return call(event);
   }

   private static void dispatchNonCancellable(ListenerSnapshot snapshot, Event event) {
      EventInvoker[] invokers = snapshot.invokers;
      Object[] owners = snapshot.owners;
      RegisteredListener[] metadata = snapshot.metadata;

      for (int i = 0; i < invokers.length; i++) {
         invokeListener(invokers[i], owners[i], metadata[i], event);
      }
   }

   private static void dispatchCancellable(ListenerSnapshot snapshot, Event event, CancellableEvent cancellableEvent) {
      EventInvoker[] invokers = snapshot.invokers;
      Object[] owners = snapshot.owners;
      RegisteredListener[] metadata = snapshot.metadata;

      for (int i = 0; i < invokers.length; i++) {
         invokeListener(invokers[i], owners[i], metadata[i], event);
         if (cancellableEvent.isCancelled()) {
            return;
         }
      }
   }

   private static void invokeListener(EventInvoker invoker, Object owner, RegisteredListener metadata, Event event) {
      try {
         invoker.invoke(owner, event);
      } catch (Throwable throwable) {
         LOGGER.error(
            "Failed to dispatch {} to {}::{}", new Object[]{event.getClass().getSimpleName(), owner.getClass().getName(), metadata.methodName, throwable}
         );
      }
   }

   private static ListenerDefinition[] scanDefinitions(Class<?> listenerClass) {
      List<ListenerDefinition> definitions = new ArrayList<>(4);
      Set<String> overriddenSignatures = new HashSet<>();

      for (Class<?> currentClass = listenerClass; currentClass != null && currentClass != Object.class; currentClass = currentClass.getSuperclass()) {
         Method[] methods = currentClass.getDeclaredMethods();

         for (Method method : methods) {
            if (!method.isSynthetic() && !method.isBridge()) {
               String signature = methodSignature(method);
               EventTarget annotation = method.getAnnotation(EventTarget.class);
               boolean overridden = overriddenSignatures.contains(signature);
               if (!overridden && annotation != null) {
                  Class<? extends Event> eventType = resolveEventType(listenerClass, method);
                  definitions.add(new ListenerDefinition(eventType, annotation.priority(), method.getName(), AsmEventInvokerFactory.compile(method)));
               }

               if (canOverride(method)) {
                  overriddenSignatures.add(signature);
               }
            }
         }
      }

      return definitions.toArray(ListenerDefinition[]::new);
   }

   private static Class<? extends Event> resolveEventType(Class<?> listenerClass, Method method) {
      if (Modifier.isAbstract(method.getModifiers())) {
         throw new IllegalArgumentException("Annotated listener method " + listenerClass.getName() + "::" + method.getName() + " cannot be abstract.");
      } else if (method.getReturnType() != void.class) {
         throw new IllegalArgumentException("Annotated listener method " + listenerClass.getName() + "::" + method.getName() + " must return void.");
      } else if (method.getParameterCount() != 1) {
         throw new IllegalArgumentException(
            "Annotated listener method " + listenerClass.getName() + "::" + method.getName() + " must have exactly one event parameter."
         );
      } else {
         Class<?> eventType = method.getParameterTypes()[0];
         if (!Event.class.isAssignableFrom(eventType)) {
            throw new IllegalArgumentException(
               "Annotated listener method " + listenerClass.getName() + "::" + method.getName() + " parameter must extend " + Event.class.getName() + "."
            );
         } else {
            return (Class<? extends Event>)eventType;
         }
      }
   }

   private static boolean canOverride(Method method) {
      int modifiers = method.getModifiers();
      return !Modifier.isPrivate(modifiers) && !Modifier.isStatic(modifiers);
   }

   private static String methodSignature(Method method) {
      StringBuilder builder = new StringBuilder(64);
      builder.append(method.getName()).append('(');
      Class<?>[] parameterTypes = method.getParameterTypes();

      for (int i = 0; i < parameterTypes.length; i++) {
         if (i > 0) {
            builder.append(',');
         }

         builder.append(parameterTypes[i].getName());
      }

      return builder.append(")->").append(method.getReturnType().getName()).toString();
   }
}
