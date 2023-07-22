package me.linstar.vastsealevel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataBridge {
    private static final Map<String, Integer> DATA_CACHE = new HashMap<>();
    private static final Map<String, Integer> ONLINE_RANK = new ConcurrentHashMap<>();
    private static final List<Document> RANK = new ArrayList<>();

    public static void setExperience(String uuid, int valued){
        int value = Math.max(valued, 0);

        DATA_CACHE.replace(uuid, value);

        VastseaLevel.LOGGER.info(DATA_CACHE.toString());

        //先缓存保障及时性 和数据库同步就丢Task
        runTask(()->{
            VastseaLevel.DATABASE.setExperience(uuid, value);
        });
    }
    public static int getExperience(String uuid){
        if (DATA_CACHE.containsKey(uuid)){
            return DATA_CACHE.get(uuid);
        }

        //VastseaLevel.LOGGER.info("Can not find player in cache, please rejoin.");

        return 0;
    }
    public static void setLevel(String uuid, int level){
        if (level <= 0){
            setExperience(uuid, 0);
            return;
        }

        int experience = levelToExp(level);
        setExperience(uuid, experience);
    }
    public static int getLevel(String uuid){
        return expToLevel(getExperience(uuid));
    }
    public static void genData(Player player){
        String uuid = player.getUniqueId().toString();
        String name = player.getName();

        runTask(()->{
            DATA_CACHE.put(uuid, VastseaLevel.DATABASE.getExperience(uuid));
            VastseaLevel.DATABASE.updateName(uuid, name);
        });
    }

    public static void removeData(String uuid){
        DATA_CACHE.remove(uuid);
    }

    public static String getTextByRank(int rank){
        if (rank > 10  || rank <= 0){
            VastseaLevel.LOGGER.warning("Invalid rank, only first ten provided");
            return "";
        }

        if (RANK.size() < rank){
            return "          ";
        }

        Document document = RANK.get(rank-1);
        String name = document.getString("name");
        if (name.isEmpty()){
            name = "Unknown";
            String uuid = document.getString("uuid");
            Bukkit.getScheduler().runTaskAsynchronously(VastseaLevel.INSTANCE, ()->{
               VastseaLevel.DATABASE.updateName(uuid, getNameByUUID(uuid));
            });
        }
        return VastseaLevel.INSTANCE.config.getString("rank_formatter")
                .replace("{rank}", String.valueOf(rank))
                .replace("{name}", name)
                .replace("{level}", String.valueOf(
                        expToLevel(document.getInteger("xp"))
                ));
    }

    public static int getRank(String uuid){
        if (ONLINE_RANK.containsKey(uuid)){
            return ONLINE_RANK.get(uuid);
        }

        return 0;
    }

    private static String getNameByUUID(String uuid){
        Player player = Bukkit.getPlayer(UUID.fromString(uuid));

        if (player != null){
            return player.getName();
        }

        try {
            URLConnection urlConnection = new URL("https://playerdb.co/api/player/minecraft/" + uuid).openConnection();
            HttpsURLConnection connection = (HttpsURLConnection) urlConnection;

            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)"); //这弱智API没UA直接Forbidden
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Response: " + connection.getResponseMessage() + connection.getResponseCode());
            }

            JSONObject json = JSON.parseObject(connection.getInputStream(), JSONObject.class);

            return json.getJSONObject("data").getJSONObject("player").getString("username");

        }catch (Exception e){
            e.printStackTrace();
            VastseaLevel.LOGGER.warning("Fail to connect to player data api, is it works?");
            return "";
        }
    }

    public static void start(){
        //三十秒更新一次前十排行数据&在线玩家排行数据
        Bukkit.getScheduler().runTaskTimerAsynchronously(VastseaLevel.INSTANCE, ()->{
            List<Document> rank = VastseaLevel.DATABASE.getRank();
            if (rank == null){
                return;
            }

            //同步缓存
            RANK.clear();
            RANK.addAll(rank);
        }, 0, 20*30);

        Bukkit.getScheduler().runTaskTimer(VastseaLevel.INSTANCE, ()->{
            List<String> uuids = new ArrayList<>();
            for (Player player: Bukkit.getOnlinePlayers()){
                uuids.add(player.getUniqueId().toString());
            }

            ONLINE_RANK.clear();
            ONLINE_RANK.putAll(VastseaLevel.DATABASE.getRanks(uuids));
        }, 0, 20*30);

        VastseaLevel.LOGGER .info("Start Data Bridge Tasks.");
    }

    public static void runTask(Runnable task){
        Bukkit.getScheduler().runTask(VastseaLevel.INSTANCE, ()->{
            try {
                task.run();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    public static int levelToExp(int level){
        return  (int) Math.ceil((Math.pow(level + 2.5, 2) -12.25)*1250);
    }

    public static int expToLevel(int exp){
        return (int)(Math.sqrt((double) exp / 1250 + 12.25) -2.5);
    }
}
