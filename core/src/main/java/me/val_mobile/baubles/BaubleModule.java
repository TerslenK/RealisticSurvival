/*
    Copyright (C) 2022  Val_Mobile

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.val_mobile.baubles;

import me.val_mobile.data.ModuleItems;
import me.val_mobile.data.ModuleRecipes;
import me.val_mobile.data.RSVModule;
import me.val_mobile.data.baubles.ItemConfig;
import me.val_mobile.data.baubles.PlayerDataConfig;
import me.val_mobile.data.baubles.RecipesConfig;
import me.val_mobile.data.baubles.UserConfig;
import me.val_mobile.realisticsurvival.RealisticSurvivalPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class BaubleModule extends RSVModule {

    private final RealisticSurvivalPlugin plugin;

    public static final String NAME = "Baubles";

    private final PlayerDataConfig config;
    private WormholeInventory inv;
    private BaubleEvents events;
    private final Collection<UUID> brokenHeartPlayers = new ArrayList<>();

    public BaubleModule(RealisticSurvivalPlugin plugin) {
        super(NAME, plugin);
        this.plugin = plugin;
        this.config = new PlayerDataConfig(plugin);
    }

    @Override
    public void initialize() {
        setUserConfig(new UserConfig(plugin));
        setItemConfig(new ItemConfig(plugin));
        setRecipeConfig(new RecipesConfig(plugin));
        setModuleItems(new ModuleItems(this));
        setModuleRecipes(new ModuleRecipes(this, plugin));

        FileConfiguration config = getUserConfig().getConfig();
        if (config.getBoolean("Initialize.Enabled")) {
            String message = ChatColor.translateAlternateColorCodes('&', config.getString("Initialize.Message"));
            message = message.replaceAll("%NAME%", NAME);

            plugin.getLogger().info(message);
        }

        events = new BaubleEvents(this, plugin);

        getModuleItems().initialize();
        getModuleRecipes().initialize();
        events.initialize();

        inv = new WormholeInventory(plugin, this);
    }

    @Override
    public void shutdown() {
        FileConfiguration config = getUserConfig().getConfig();
        if (config.getBoolean("Shutdown.Enabled")) {
            String message = ChatColor.translateAlternateColorCodes('&', config.getString("Shutdown.Message"));
            message = message.replaceAll("%NAME%", NAME);

            plugin.getLogger().info(message);
        }
    }

    public PlayerDataConfig getPlayerDataConfig() {
        return config;
    }

    public Collection<UUID> getBrokenHeartPlayers() {
        return brokenHeartPlayers;
    }

    public BaubleEvents getEvents() {
        return events;
    }

    public WormholeInventory getInv() {
        return inv;
    }
}
