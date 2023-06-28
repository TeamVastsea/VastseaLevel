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

            switch (lowerIdentifier) {
                case "level":
                    return String.format("%d", DataBridge.getLevel(uuid));
                case "experience":
                    return String.format("%d", DataBridge.getExperience(uuid));
                case "rank":
                    return String.format("%d", DataBridge.getRank(uuid));
            }
        }

        String[] split = lowerIdentifier.split("_");
        if (split[0].equals("rank") && split.length == 2){
                return DataBridge.getNameByRank(Integer.parseInt(split[1]));
        }

        return "";
    }
}
