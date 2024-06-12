package me.dunescifye.sharedenderchest;

import org.bukkit.plugin.java.JavaPlugin;

public final class SharedEnderChest extends JavaPlugin {
    private static SharedEnderChest plugin;

    public static SharedEnderChest getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        new EnderChestOpen().enderChestOpenHandler(this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
