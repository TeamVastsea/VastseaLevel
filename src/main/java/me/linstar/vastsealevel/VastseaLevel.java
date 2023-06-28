package me.linstar.vastsealevel;

import me.linstar.vastsealevel.commands.LevelCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

public final class VastseaLevel extends JavaPlugin implements Listener {

    public static final String NAME = "vastsealevel";
    public static VastseaLevel INSTANCE;
    public static DataBase DATABASE;
    public static Logger LOGGER;

    YamlConfiguration config;

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = getLogger();

        saveDefaultConfig();
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));

        DATABASE = new DataBase();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new LevelExpansion().register();
        }

        LevelCommandExecutor executor = new LevelCommandExecutor();

        Bukkit.getPluginCommand("level").setExecutor(executor);
        Bukkit.getPluginCommand("experience").setExecutor(executor);

        Bukkit.getPluginManager().registerEvents(this, this);

        //尝试连接数据库 为了不乱传Config就不写Database里了
        DataBridge.runTask(()->{
            if (!DATABASE.connect(config.getString("url"))){
                LOGGER.severe("Unable to connect database server, check your config!");
                Bukkit.getPluginManager().disablePlugin(VastseaLevel.INSTANCE);
            }
        });

        //如果热重载就重新记录所有玩家的数据
        if (!Bukkit.getOnlinePlayers().isEmpty()){
            for (Player player: Bukkit.getOnlinePlayers()){
                DataBridge.genData(player);
            }
        }

        //开启定时数据同步
        DataBridge.start();
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        DATABASE.disConnect();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        DataBridge.genData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeft(PlayerKickEvent event){
        DataBridge.removeData(event.getPlayer().getUniqueId().toString());
    }
}
