package me.linstar.vastsealevel;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;

public class DataBase {
    MongoClient client;
    MongoDatabase database;
    MongoCollection<Document> collection;
    public boolean connect(String url){
        try{
            client = MongoClients.create(url);
            database = client.getDatabase(VastseaLevel.NAME);
            collection = database.getCollection("players");

            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public void disConnect() {
        try {
            client.close();
        }catch (Exception ignored){

        }
    }

    public int getExperience(String uuid){
        Document document = collection.find(eq("uuid", uuid)).first();
        if (document == null){
            InsertOneResult result = collection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("uuid", uuid)
                    .append("name", "")
                    .append("xp", 0));
            if (result.getInsertedId() == null){
                VastseaLevel.LOGGER.info("Fail to create player form, is the database server alive?");
            }
            return 0;
        }

        return document.getInteger("xp");
    }

    public void setExperience(String uuid, int value) {
        try{
            collection.updateOne(eq("uuid", uuid), Updates.set("xp", value));
        }catch (MongoException e){
            e.printStackTrace();
        }
    }

    public List<Document> getRank(){
        List<Document> data = new ArrayList<>();

        BasicDBObject object = new BasicDBObject();
        object.put("xp", -1);

        if (collection == null){
            return data;
        }

        try(MongoCursor<Document> document = collection.find().sort(object).limit(10).iterator()){
            while (document.hasNext()){
                Document doc = document.tryNext();
                if (doc == null){
                    continue;
                }
                data.add(doc);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return data;
    }

    public Map<String, Integer> getRanks(List<String> uuids){

        Map<String, Integer> result = new HashMap<>();
        BasicDBObject object = new BasicDBObject();
        object.put("xp", -1);

        for (String uuid: uuids){
            int rank = 0;
            try(MongoCursor<Document> cursor = collection.find(gte("xp", getExperience(uuid))).sort(object).iterator()) {
                while (cursor.hasNext()) {
                    cursor.tryNext();
                    rank ++;
                }
            }

            result.put(uuid, rank);
        }

        return result;
    }

    public void updateName(String uuid, String name){
        Document document = collection.find(eq("uuid", uuid)).first();
        if (document == null){
            return;
        }

        if (!Objects.equals(document.getString("name"), name)){
            collection.updateOne(document, Updates.set("name", name));
        }
    }
}
