package me.linstar.vastsealevel.commands;

import me.linstar.vastsealevel.DataBridge;
import me.linstar.vastsealevel.VastseaLevel;
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
        if (Objects.equals(args[0], "reload")){
            if (!sender.hasPermission("vastsea.level.reload")){
                sender.sendMessage("§4你没有权限!");
                return true;
            }
            VastseaLevel.INSTANCE.reload();
            return true;
        }

        if (args.length != 3){
            sender.sendMessage("§4无效参数");
            return true;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if (player == null){
            sender.sendMessage("§4找不到玩家");
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
                    sender.sendMessage(String.format("已将%d级等级添加给%s", value ,player.getName()));
            }, ()->{
                    DataBridge.setExperience(uuid, DataBridge.getExperience(uuid) + value);
                    sender.sendMessage(String.format("已将%d点经验添加给%s", value ,player.getName()));
                });
            case "remove":
                return execution(sender, command, "remove", ()->{
                    DataBridge.setLevel(uuid, DataBridge.getLevel(uuid) - value);
                    sender.sendMessage(String.format("已将%d级等级去除给%s", value ,player.getName()));
                }, ()->{
                    DataBridge.setExperience(uuid, DataBridge.getExperience(uuid) - value);
                    sender.sendMessage(String.format("已将%d点经验去除给%s", value ,player.getName()));
                });
            case "set":
                return execution(sender, command, "set", ()->{
                    DataBridge.setLevel(uuid, value);
                    sender.sendMessage(String.format("已将%s的等级设为%d级", player.getName(), value ));
                }, ()->{
                    DataBridge.setExperience(uuid, value);
                    sender.sendMessage(String.format("已将%s的经验设为%d点", player.getName(), value));
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
