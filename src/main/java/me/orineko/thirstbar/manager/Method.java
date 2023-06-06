package me.orineko.thirstbar.manager;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import me.orineko.pluginspigottools.MethodDefault;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Method {

    /**
     * Send item to inventory player
     *
     * @param player    is player to take
     * @param itemStack is item to give
     * @return true (false if full inventory)
     */
    public static boolean sendItemToInv(@Nonnull Player player, @Nonnull ItemStack itemStack) {
        PlayerInventory inventory = player.getInventory();
        if (inventory.firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation().add(0, 0.5, 0), itemStack);
            return false;
        }
        inventory.addItem(itemStack);
        return true;
    }

    public static PotionEffect getPotionEffect(@Nonnull String text) {
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

    public static void executeAction(@Nonnull Player player, @Nonnull List<String> textList) {
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
                    if (titleSub.size() > 0) {
                        String main = titleMain.get(0);
                        String sub = titleSub.get(0);
                        if (main == null || sub == null) return;
                        Titles.sendTitle(player, main, sub);
                        titleMain.remove(0);
                        titleSub.remove(0);
                    }
                    break;
                case "title-sub":
                    titleSub.add(value);
                    if (titleMain.size() > 0) {
                        String main = titleMain.get(0);
                        String sub = titleSub.get(0);
                        if (main == null || sub == null) return;
                        Titles.sendTitle(player, main, sub);
                        titleMain.remove(0);
                        titleSub.remove(0);
                    }
                    break;
                case "message":
                    player.sendMessage(value);
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
            String titleMainRemain = (titleMain.size() > 0) ? titleMain.get(0) : null;
            String titleSubRemain = (titleSub.size() > 0) ? titleSub.get(0) : null;
            if (titleMainRemain != null) Titles.sendTitle(player, titleMainRemain, "");
            if (titleSubRemain != null) Titles.sendTitle(player, "", titleSubRemain);
        });
    }

}
