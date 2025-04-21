package me.orineko.thirstbar.api;

import me.orineko.pluginspigottools.MethodDefault;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPI {

    public double getValuePlaceholder(@NotNull Player player, @NotNull String text){
        String value = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        return MethodDefault.formatNumber(value, 0);
    }

}
