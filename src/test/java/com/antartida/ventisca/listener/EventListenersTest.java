package com.antartida.ventisca.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antartida.ventisca.manager.EventManager;
import com.antartida.ventisca.manager.StatsManager;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// NOTE: the "escape command" branch calls player.playSound(..., Sound, ...).
// org.bukkit.Sound is a registry-backed type that fails to initialize outside
// a running server, so only the branches that avoid it are unit-tested here.
class EventListenersTest {

    private final EventManager eventManager = mock(EventManager.class);
    private final StatsManager statsManager = mock(StatsManager.class);
    private EventListeners listeners;

    @BeforeEach
    void setUp() {
        listeners = new EventListeners(eventManager, statsManager);
    }

    private PlayerCommandPreprocessEvent commandEvent(String message, boolean locked) {
        Player player = mock(Player.class);
        UUID id = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(id);
        when(player.getLocation()).thenReturn(mock(Location.class));
        when(eventManager.isCommandLocked(id)).thenReturn(locked);
        PlayerCommandPreprocessEvent event = mock(PlayerCommandPreprocessEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getMessage()).thenReturn(message);
        return event;
    }

    @Test
    void unlockedPlayerCommandsAreLeftUntouched() {
        PlayerCommandPreprocessEvent event = commandEvent("/spawn", false);
        listeners.onCommandPreprocess(event);
        verify(event, never()).setCancelled(true);
    }

    @Test
    void nonEscapeCommandIsCancelledSilently() {
        PlayerCommandPreprocessEvent event = commandEvent("/say hello", true);
        Player player = event.getPlayer();

        listeners.onCommandPreprocess(event);

        verify(event).setCancelled(true);
        verify(player, never()).playSound(any(Location.class), any(Sound.class), anyFloat(), anyFloat());
    }

    @Test
    void namespacedNonEscapeCommandIsCancelledSilently() {
        PlayerCommandPreprocessEvent event = commandEvent("/minecraft:say hi", true);
        Player player = event.getPlayer();

        listeners.onCommandPreprocess(event);

        verify(event).setCancelled(true);
        verify(player, never()).playSound(any(Location.class), any(Sound.class), anyFloat(), anyFloat());
    }
}
