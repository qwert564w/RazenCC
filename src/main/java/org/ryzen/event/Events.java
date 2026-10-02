package org.ryzen.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.event.events.game.AttackEvent;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.game.PlayerJumpEvent;
import org.ryzen.event.events.game.PlayerTickEvent;
import org.ryzen.event.events.lifecycle.ClientStartEvent;
import org.ryzen.event.events.lifecycle.DisconnectEvent;
import org.ryzen.event.events.lifecycle.ResourceReloadEvent;
import org.ryzen.event.events.lifecycle.ShutdownEvent;
import org.ryzen.event.events.lifecycle.WorldJoinEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.render.FinalGuiRenderEvent;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.event.events.render.Render3DEvent;
import org.ryzen.event.events.screen.ScreenCloseEvent;
import org.ryzen.event.events.screen.ScreenKeyEvent;
import org.ryzen.event.events.screen.ScreenMouseButtonEvent;
import org.ryzen.event.events.screen.ScreenOpenEvent;
import org.ryzen.event.events.screen.ScreenRenderEvent;

@Environment(EnvType.CLIENT)
public class Events {
   public static final ClientStartEvent CLIENT_START = new ClientStartEvent();
   public static final GameTickEvent GAME_TICK = new GameTickEvent();
   public static final PlayerTickEvent PLAYER_TICK = new PlayerTickEvent();
   public static final AttackEvent ATTACK = new AttackEvent();
   public static final PlayerJumpEvent PLAYER_JUMP = new PlayerJumpEvent();
   public static final WorldJoinEvent WORLD_JOIN = new WorldJoinEvent();
   public static final WorldLeaveEvent WORLD_LEAVE = new WorldLeaveEvent();
   public static final DisconnectEvent DISCONNECT = new DisconnectEvent();
   public static final ShutdownEvent SHUTDOWN = new ShutdownEvent();
   public static final ResourceReloadEvent RESOURCE_RELOAD = new ResourceReloadEvent();
   public static final ScreenOpenEvent SCREEN_OPEN = new ScreenOpenEvent();
   public static final ScreenCloseEvent SCREEN_CLOSE = new ScreenCloseEvent();
   public static final ScreenRenderEvent SCREEN_RENDER = new ScreenRenderEvent();
   public static final ScreenKeyEvent SCREEN_KEY = new ScreenKeyEvent();
   public static final ScreenMouseButtonEvent SCREEN_MOUSE_BUTTON = new ScreenMouseButtonEvent();
   public static final FinalGuiRenderEvent FINAL_GUI_RENDER = new FinalGuiRenderEvent();
   public static final Render2DEvent RENDER_2D = new Render2DEvent();
   public static final Render3DEvent RENDER_3D = new Render3DEvent();
}
