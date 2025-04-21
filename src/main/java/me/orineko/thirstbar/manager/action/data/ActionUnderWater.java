package me.orineko.thirstbar.manager.action.data;

import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.action.ActionRegister;
import me.orineko.thirstbar.manager.action.ActionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionUnderWater extends ActionRegister{

    public ActionUnderWater() {
        super(ActionType.UNDERWATER);
        int idRepeat = Bukkit.getScheduler().scheduleSyncRepeatingTask(ThirstBar.getInstance(), () -> {
            Bukkit.getOnlinePlayers().forEach(v -> {
                if(checkCanExecute(v)){
                    executeAction(v);
                } else if(checkCanNotExecute(v)) {
                    disableAction(v);
                }
            });
        }, 0, 20);
        setIdRepeat(idRepeat);
    }

    @Override
    public boolean checkCondition(@NotNull Player player) {
        Location location = player.getLocation();
        World world = location.getWorld();
        if(world == null) return false;
        int x = location.getBlockX();
        int y = location.getBlockY()+1;
        int z = location.getBlockZ();
        Block block = world.getBlockAt(x, y, z);
        return block.getType().name().contains("WATER") || player.getRemainingAir() < player.getMaximumAir();
    }
}
