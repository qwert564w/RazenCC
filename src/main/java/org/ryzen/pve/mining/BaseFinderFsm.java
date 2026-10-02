package org.ryzen.pve.mining;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class BaseFinderFsm {
   private BaseFinderFsm() {
   }

   public static BaseFinderFsm.State next(BaseFinderFsm.State state, BaseFinderFsm.Signal signal, boolean usesServerTransfer) {
      if (signal == BaseFinderFsm.Signal.STOP) {
         return BaseFinderFsm.State.IDLE;
      }

      if (signal == BaseFinderFsm.Signal.FAIL) {
         return BaseFinderFsm.State.ERROR;
      }

      if (signal == BaseFinderFsm.Signal.CLUSTER_FOUND && state != BaseFinderFsm.State.IDLE && state != BaseFinderFsm.State.ERROR) {
         return BaseFinderFsm.State.FOUND;
      }

      if (signal == BaseFinderFsm.Signal.UNSAFE
         && state != BaseFinderFsm.State.IDLE
         && state != BaseFinderFsm.State.FOUND
         && state != BaseFinderFsm.State.ERROR) {
         return BaseFinderFsm.State.PAUSED;
      }

      return switch (state) {
         case IDLE -> signal == BaseFinderFsm.Signal.START
            ? (usesServerTransfer ? BaseFinderFsm.State.SERVER_TRANSFER : BaseFinderFsm.State.DESCENDING)
            : state;
         case SERVER_TRANSFER -> signal == BaseFinderFsm.Signal.TRANSFER_DONE ? BaseFinderFsm.State.DESCENDING : state;
         case DESCENDING -> signal == BaseFinderFsm.Signal.DESCENT_DONE
            ? BaseFinderFsm.State.SEARCHING
            : (signal == BaseFinderFsm.Signal.PATH_STUCK ? BaseFinderFsm.State.BYPASSING : state);
         case SEARCHING -> signal == BaseFinderFsm.Signal.PATH_STUCK ? BaseFinderFsm.State.BYPASSING : state;
         case BYPASSING -> signal == BaseFinderFsm.Signal.BYPASS_DONE ? BaseFinderFsm.State.SEARCHING : state;
         case FOUND, PAUSED, ERROR -> state;
      };
   }

   public static BaseFinderFsm.State resume(BaseFinderFsm.State desired, boolean usesServerTransfer) {
      if (desired != null
         && desired != BaseFinderFsm.State.IDLE
         && desired != BaseFinderFsm.State.PAUSED
         && desired != BaseFinderFsm.State.ERROR
         && desired != BaseFinderFsm.State.FOUND) {
         return desired;
      } else {
         return usesServerTransfer ? BaseFinderFsm.State.SERVER_TRANSFER : BaseFinderFsm.State.DESCENDING;
      }
   }

   @Environment(EnvType.CLIENT)
   public enum Signal {
      START,
      TRANSFER_DONE,
      DESCENT_DONE,
      PATH_STUCK,
      BYPASS_DONE,
      CLUSTER_FOUND,
      UNSAFE,
      FAIL,
      STOP;
   }

   @Environment(EnvType.CLIENT)
   public enum State {
      IDLE,
      SERVER_TRANSFER,
      DESCENDING,
      SEARCHING,
      BYPASSING,
      FOUND,
      PAUSED,
      ERROR;
   }
}
