package me.orineko.thirstbar.manager;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ThirstBarMethod {

    private static int idDelayMessage = 0;
    private static int idDelayMainTitle = 0;
    private static int idDelaySubTitle = 0;

    /**
     * Send item to inventory player
     *
     * @param player    is player to take
     * @param itemStack is item to give
     * @return true (false if full inventory)
     */
    public static boolean sendItemToInv(@NotNull Player player, @NotNull ItemStack itemStack) {
        PlayerInventory inventory = player.getInventory();
        if (inventory.firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation().add(0, 0.5, 0), itemStack);
            return false;
        }
        inventory.addItem(itemStack);
        return true;
    }

    public static PotionEffect getPotionEffect(@NotNull String text) {
        String[] arr = text.split(":");
        if (arr.length == 0) return null;
        String effString = arr[0].trim();
        int power = (arr.length > 1) ? (int) MethodDefault.formatNumber(arr[1].trim(), 1) : 1;
        XPotion.Effect effect = XPotion.parseEffect(effString);
        if (effect == null) return null;
        return new PotionEffect(effect.getEffect().getType(), Integer.MAX_VALUE, power - 1);
    }

    public static String changeDoubleToInt(double value) {
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(value);
    }

    public static void disableGameMode(@NotNull Player player){
        GameMode gameMode = player.getGameMode();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().getData(player.getName());
        if(playerData == null) return;
        List<String> gamemodeList = ConfigData.DISABLED_GAMEMODE;
        try {
            playerData.setDisableAll(gamemodeList.stream()
                    .anyMatch(g -> gameMode.equals(GameMode.valueOf(g.toUpperCase()))));
        } catch (IllegalArgumentException ignore){

        }
    }

    public static void executeAction(@NotNull Player player, @NotNull List<String> textList, boolean stageConfig) {
        List<String> titleMain = new ArrayList<>();
        List<String> titleSub = new ArrayList<>();
        textList.forEach(text -> {
            int index1 = text.indexOf("[");
            int index2 = text.indexOf("]");
            if (index1 == -1 || index2 == -1) return;
            String key = text.substring(index1 + 1, index2).toLowerCase();
            String value = MethodDefault.formatColor(text.substring(index2 + 1)).trim();
            switch (key) {
                case "title":
                    titleMain.add(value);
                    if (!titleSub.isEmpty()) {
                        String main = titleMain.get(0);
                        String sub = titleSub.get(0);
                        if (main == null || sub == null) return;

                        int versionNumber = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);
                        if (versionNumber <= 19) {
                            if(idDelayMainTitle == 0) Titles.sendTitle(player, main, sub);
                        } else {
                            if(idDelayMainTitle == 0) {
                                player.sendTitle(main, sub, 10, 20, 10);
                            }
                        }
                        if(stageConfig && idDelayMainTitle == 0) idDelayMainTitle =
                                Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                        () -> idDelayMainTitle = 0, 100);
                        titleMain.remove(0);
                        titleSub.remove(0);
                    }
                    break;
                case "subtitle":
                    titleSub.add(value);
                    if (!titleMain.isEmpty()) {
                        String main = titleMain.get(0);
                        String sub = titleSub.get(0);
                        if (main == null || sub == null) return;
                        int versionNumber = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);
                        if (versionNumber <= 19) {
                            if(idDelaySubTitle == 0) Titles.sendTitle(player, main, sub);
                        } else {
                            if(idDelaySubTitle == 0) player.sendTitle(main, sub, 10, 20, 10);
                        }
                        if(stageConfig && idDelaySubTitle == 0) idDelaySubTitle =
                                Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                        () -> idDelaySubTitle = 0, 100);
                        titleMain.remove(0);
                        titleSub.remove(0);
                    }
                    break;
                case "message":
                    if(idDelayMessage == 0) player.sendMessage(value);
                    if(stageConfig && idDelayMessage == 0) idDelayMessage =
                            Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                    () -> idDelayMessage = 0, 100);
                    break;
                case "sound":
                    Optional<XSound> xSound = XSound.matchXSound(value);
                    if (!xSound.isPresent()) break;
                    xSound.get().play(player);
                    break;
                case "player":
                    Bukkit.dispatchCommand(player, value);
                    break;
                case "console":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value.replace("<player>", player.getName()));
                    break;
            }
            String titleMainRemain = (!titleMain.isEmpty()) ? titleMain.get(0) : null;
            String titleSubRemain = (!titleSub.isEmpty()) ? titleSub.get(0) : null;
            int versionNumber = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);
            if(versionNumber <= 19) {
                if (titleMainRemain != null) Titles.sendTitle(player, titleMainRemain, "");
                if (titleSubRemain != null) Titles.sendTitle(player, "", titleSubRemain);
            } else {
                player.sendTitle(titleMainRemain, titleSubRemain, 10, 20, 10);
            }
        });
    }

    public static boolean checkSightIsWater(@NotNull Player player, boolean checkIfClean){
        Location locOrigin = player.getLocation();
        Location loc = player.getLocation().clone().add(0, 1.5, 0);
        Vector vector = player.getLocation().getDirection();
        double x = vector.getX()/5;
        double y = vector.getY()/5;
        double z = vector.getZ()/5;
        //player.sendMessage("test: "+loc+", "+vector);
        do {
            loc = loc.add(x, y, z);
            Block block = loc.getBlock();
            BoundingBox box = block.getBoundingBox();
            //if (box.contains(loc.getX(),loc.getY(),loc.getZ()))
            //    player.sendMessage(block.getType().name() + "intersects at" + loc.toString());

            // skip if current block is not blocking line of sight and is not water
            if (!box.contains(loc.getX(),loc.getY(),loc.getZ()) && !checkIsWaterBlock(block))
                continue;
            // if hit solid (line of sight break) or water block
            if (checkIfClean)
                return checkBlockIsCleanWater(block);
            else
                return checkIsWaterBlock(block);
        } while (locOrigin.distance(loc) < 4);
        return false;
    }

    public static boolean checkBlockIsCleanWater(Block block) {
        if (checkIsWaterBlock(block))
        {
            /*if (block.getType().equals(Material.BUBBLE_COLUMN))
                return true;*/
            Location belowBlock = block.getLocation();
            belowBlock.subtract(0, 1, 0);
            Block blockBelow = belowBlock.getBlock();
            return (blockBelow.getType().name().contains("FIRE") && !blockBelow.getType().name().contains("FIRE_CORAL")) || blockBelow.getType().name().contains("LAVA");
        }
        return false;
    }

    public static boolean checkIsWaterBlock(Block block) {
        return block.getType().name().contains("WATER") || checkIsWaterlogged(block);
    }

    public static boolean checkIsWaterlogged(Block block) {
        if (block.getBlockData() instanceof Waterlogged)
        {
            Waterlogged wlb = (Waterlogged) block.getBlockData();
            return wlb.isWaterlogged();
        }
        return false;
    }

}
