package com.antartida.ventisca.command;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antartida.ventisca.manager.EventManager;
import com.antartida.ventisca.manager.StatsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class AntartidaCommandTest {

    private final EventManager eventManager = mock(EventManager.class);
    private final StatsManager statsManager = mock(StatsManager.class);
    private final AntartidaCommand command = new AntartidaCommand(eventManager, statsManager);

    @Test
    void nonPlayerSenderIsRejectedWithMessage() {
        CommandSender console = mock(CommandSender.class);

        boolean handled = command.onCommand(console, null, "antartida", new String[0]);

        assertTrue(handled);
        verify(console).sendMessage(any(Component.class));
    }

    @Test
    void explicitAdminRequestWithoutPermissionIsDenied() {
        Player player = mock(Player.class);
        when(player.hasPermission(AntartidaCommand.ADMIN_PERMISSION)).thenReturn(false);

        boolean handled = command.onCommand(player, null, "antartida", new String[]{"admin"});

        assertTrue(handled);
        verify(player).sendMessage(any(Component.class));
        // A denied non-admin never reaches the GUI helpers.
        verify(eventManager, org.mockito.Mockito.never()).isDebugEnabled();
    }

    @Test
    void playerWithoutAnyPermissionIsRejected() {
        Player player = mock(Player.class);
        when(player.hasPermission(AntartidaCommand.ADMIN_PERMISSION)).thenReturn(false);
        when(player.hasPermission(AntartidaCommand.PLAYER_PERMISSION)).thenReturn(false);

        boolean handled = command.onCommand(player, null, "antartida", new String[0]);

        assertTrue(handled);
        verify(player).sendMessage(any(Component.class));
    }
}
