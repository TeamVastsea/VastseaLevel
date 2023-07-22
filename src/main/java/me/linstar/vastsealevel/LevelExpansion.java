package me.linstar.vastsealevel;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LevelExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "vastsea";
    }

    @Override
    public @NotNull String getAuthor() {
        return VastseaLevel.INSTANCE.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return VastseaLevel.INSTANCE.getDescription().getVersion();
    }
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String lowerIdentifier = params.toLowerCase();

        if (player.isOnline()){
            String uuid = player.getUniqueId().toString();
            int level = DataBridge.getLevel(uuid);
            int xp = DataBridge.getExperience(uuid);

            switch (lowerIdentifier) {
                case "level":
                    return String.format("%d", level);
                case "experience":
                    return String.format("%d", xp);
                case "rank":
                    return String.format("%d", DataBridge.getRank(uuid));
                case "exp_needed":
                    int exp_needed = DataBridge.levelToExp(level + 1) - xp;
                    String result = "";
                    if (exp_needed <= 1000 -1){
                        result += exp_needed;
                    }else if (exp_needed <= 100000 - 1){
                        result = String.format("%.2f", (double) exp_needed / 1000) + "k";
                    }else {
                        result = String.format("%.2f", (double) exp_needed / 1000000) + "M";
                    }
                    return String.format(result);
                case "exp_process":

                    int process = Math.round((float) (xp - DataBridge.levelToExp(level)) / (DataBridge.levelToExp(level + 1) - DataBridge.levelToExp(level)) * 10);
                    StringBuilder stringBuilder = new StringBuilder("§f[§e");
                    int i = 0;
                    while (i < process){
                        stringBuilder.append("▬");
                        i ++;
                    }
                    stringBuilder.append("§7");
                    i = 0;
                    while (i < 10 -process){
                        stringBuilder.append("▬");
                        i ++;
                    }

                    stringBuilder.append("§f]");

                    return stringBuilder.toString();
                case "exp_percent":
                    return String.format("%.2f",(float) (xp - DataBridge.levelToExp(level)) / (DataBridge.levelToExp(level + 1) - DataBridge.levelToExp(level)) * 100);
            }
        }

        String[] split = lowerIdentifier.split("_");
        if (split[0].equals("rank") && split.length == 2){
                return DataBridge.getTextByRank(Integer.parseInt(split[1]));
        }

        return "";
    }
}
