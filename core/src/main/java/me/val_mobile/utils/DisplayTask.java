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
package me.val_mobile.utils;

import me.val_mobile.data.RSVModule;
import me.val_mobile.data.RSVPlayer;
import me.val_mobile.iceandfire.IceFireModule;
import me.val_mobile.realisticsurvival.RealisticSurvivalPlugin;
import me.val_mobile.tan.TanModule;
import me.val_mobile.tan.TemperatureCalculateTask;
import me.val_mobile.tan.ThirstCalculateTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DisplayTask extends BukkitRunnable implements RSVTask {

    private static final Map<UUID, DisplayTask> tasks = new HashMap<>();
    private final UUID id;
    private final FileConfiguration tanConfig;
    private final FileConfiguration ifConfig;
    private final RealisticSurvivalPlugin plugin;
    private final RSVPlayer player;
    private final CharacterValues characterValues;
    private boolean underSirenEffect = false;
    private boolean parasitesActive = false;
    private final boolean tempEnabled;
    private final boolean thirstEnabled;
    private final Collection<String> tanAllowedWorlds;
    private final Collection<String> ifAllowedWorlds;

    public DisplayTask(RealisticSurvivalPlugin plugin, RSVPlayer player) {
        this.plugin = plugin;

        RSVModule tanModule = RSVModule.getModule(TanModule.NAME);
        RSVModule ifModule = RSVModule.getModule(IceFireModule.NAME);

        this.tanConfig = tanModule.getUserConfig().getConfig();
        this.ifConfig = ifModule.getUserConfig().getConfig();
        this.player = player;
        this.characterValues = new CharacterValues();
        this.tempEnabled = tanConfig.getBoolean("Temperature.Enabled");
        this.thirstEnabled = tanConfig.getBoolean("Thirst.Enabled");
        this.tanAllowedWorlds = tanModule.getAllowedWorlds();
        this.ifAllowedWorlds = ifModule.getAllowedWorlds();
        this.id = player.getPlayer().getUniqueId();
        tasks.put(id, this);
    }

    @Override
    public void run() {
        Player player = this.player.getPlayer();

        if (globalConditionsMet(player)) {
            String actionbarText = "";
            String titleText = "";

            if (ifAllowedWorlds.contains(player.getWorld().getName())) {
                if (!player.hasPermission("realisticsurvival.iceandfire.resistance.*")) {
                    if (!player.hasPermission("realisticsurvival.iceandfire.resistance.sirenvisual")) {
                        if (underSirenEffect) {
                            if (ifConfig.getBoolean("Sirens.ChangeScreen.Enabled")) {
                                titleText += characterValues.getSirenView();
                            }
                        }
                    }
                }
            }

            if (tanAllowedWorlds.contains(player.getWorld().getName())) {
                double temperature = tanConfig.getDouble("Temperature.DefaultTemperature");
                double thirst = tanConfig.getDouble("Temperature.DefaultThirst");

                if (TemperatureCalculateTask.hasTask(id)) {
                    temperature = TemperatureCalculateTask.getTasks().get(id).getTemp();
                }
                if (ThirstCalculateTask.hasTask(id)) {
                    thirst = ThirstCalculateTask.getTasks().get(id).getThirstLvl();
                }

                boolean isUnderwater = player.getRemainingAir() < 300;

                if (tempEnabled && thirstEnabled) {
                    actionbarText += characterValues.getTemperatureThirstActionbar((int) Math.round(temperature), (int) Math.round(thirst), isUnderwater, parasitesActive);
                }
                else {
                    // only temperature is enabled
                    if (tempEnabled) {
                        actionbarText += characterValues.getTemperatureOnlyActionbar((int) Math.round(temperature));
                    }
                    // only thirst is enabled
                    else {
                        actionbarText += characterValues.getThirstOnlyActionbar((int) Math.round(thirst), isUnderwater, parasitesActive);
                    }
                }

                if (!player.hasPermission("realisticsurvival.toughasnails.resistance.*")) {
                    if (temperature < 6) {
                        if (tanConfig.getBoolean("Temperature.Hypothermia.ScreenTinting.Enabled")) {
                            if (!player.hasPermission("realisticsurvival.toughasnails.resistance.coldvisual")) {
                                if (tanConfig.getBoolean("Temperature.Hypothermia.ScreenTinting.UseVanillaFreezeEffect")) {
                                    Utils.setFreezingView(player, tanConfig.getInt("VisualTickPeriod") + 5);
                                }
                                else {
                                    titleText += characterValues.getIceVignette((int) Math.round(temperature));
                                }
                            }
                        }
                    }
                    if (temperature > 19) {
                        if (tanConfig.getBoolean("Temperature.Hyperthermia.ScreenTinting")) {
                            if (!player.hasPermission("realisticsurvival.toughasnails.resistance.hotvisual")) {
                                titleText += characterValues.getFireVignette((int) Math.round(temperature));
                            }
                        }
                    }

                    if (thirst < 5) {
                        if (tanConfig.getBoolean("Thirst.Dehydration.ScreenTinting")) {
                            if (!player.hasPermission("realisticsurvival.toughasnails.resistance.thirstvisual")) {
                                titleText += characterValues.getThirstVignette((int) Math.round(thirst));
                            }
                        }
                    }
                }
            }

            if (!actionbarText.isEmpty()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', actionbarText)));
            }

            if (!titleText.isEmpty()) {
                player.sendTitle(titleText, "", 0, 70, 0);
            }
        }
        else {
            stop();
        }
    }

    @Override
    public boolean conditionsMet(@Nullable Player player) {
        return globalConditionsMet(player);
    }

    @Override
    public void start() {
        int tickPeriod = tanConfig.getInt("VisualTickPeriod"); // get the tick period
        this.runTaskTimer(plugin, 0L, tickPeriod);
    }

    @Override
    public void stop() {
        tasks.remove(id);
        cancel();
    }

    public void setUnderSirenEffect(boolean underSirenEffect) {
        this.underSirenEffect = underSirenEffect;
    }

    public void setParasitesActive(boolean parasitesActive) {
        this.parasitesActive = parasitesActive;
    }

    public static boolean hasTask(UUID id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id) != null;
        }
        return false;
    }

    public static Map<UUID, DisplayTask> getTasks() {
        return tasks;
    }
}
