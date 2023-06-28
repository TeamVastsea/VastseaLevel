package me.linstar.vastsealevel.commands;

import me.linstar.vastsealevel.DataBridge;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.junit.Test;

import java.util.*;

public class LevelCommandExecutor implements TabExecutor {
    private static final String LEVEL = "level";
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 3){
            sender.sendMessage(Color.RED + "无效参数");
            return true;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if (player == null){
            sender.sendMessage(Color.RED + "找不到玩家");
            return true;
        }

        String uuid = player.getUniqueId().toString();
        int value = Integer.parseInt(args[2]);

        if (value < 0){
            sender.sendMessage("?");
            return true;
        }

        switch (args[0]){
            case "add":
                return execution(sender, command, "add", ()->{
                    DataBridge.setLevel(uuid, DataBridge.getLevel(uuid) + value);
            }, ()->{
                    DataBridge.setExperience(uuid, DataBridge.getExperience(uuid) + value);
                });
            case "remove":
                return execution(sender, command, "remove", ()->{
                    DataBridge.setLevel(uuid, DataBridge.getLevel(uuid) - value);
                }, ()->{
                    DataBridge.setExperience(uuid, DataBridge.getExperience(uuid) - value);
                });
            case "set":
                return execution(sender, command, "set", ()->{
                    DataBridge.setLevel(uuid, value);
                }, ()->{
                    DataBridge.setExperience(uuid, value);
                });
            default:
                sender.sendMessage("§4无效操作");
                return true;
        }
    }

    public boolean execution(CommandSender sender, Command command, String method, Runnable execution1, Runnable execution2){
        if (!sender.hasPermission("vastsea.level" + method + command.getName())){
            sender.sendMessage("§4你没有权限!");
            return true;
        }
        if ((command.getName().equals(LEVEL))) {
            execution1.run();
        } else {
            execution2.run();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        List<String> result = new ArrayList<>();

        if (strings.length == 1){
            result.add("add");
            result.add("remove");
            result.add("set");
        }else if (strings.length == 2){
            for(Player player: Bukkit.getOnlinePlayers()){
                result.add(player.getName());
            }
        }

        return result;
    }
}
