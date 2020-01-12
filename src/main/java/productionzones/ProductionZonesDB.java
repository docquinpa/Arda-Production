package productionzones;

import javax.json.*;
import java.io.*;
import java.util.HashMap;
import java.util.Set;


/**
 * Class that handles the production zones stored in a file and allows to dialogue with it
 * 
 * Note: You need to explicitly call the save method to save all the changes into said file
 */
public class ProductionZonesDB{
    
    private JsonObject db;
    private String     path;
    
    private static final String MSG_ZONE_NOT_FOUND       = "This production zone does not exist";
    private static final String MSG_KEY_NOT_FOUND        = "Map object doesn't contains this key";
    
    private static final String STR_LAST_PING            = "last-ping";
    private static final String STR_ITEMS                = "items";
    
    private static final int    DEFAULT_LAST_PROGRESSION = 0;
    private static final int    INDEX_ITEM_NEEDED_MILLIS = 0;
    private static final int    INDEX_ITEM_PROGESSION    = 1;
    
    
    /**
     * Default constructor for the ProductionZonesDB class.
     * it creates a data file if it doesn't exist
     * 
     * @param path is the path to the file where the data about the production zones are supposed to be
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    public ProductionZonesDB(String path) throws Exception{ // throws Exception -> it is expected that exceptions could be thrown
        // initiation
        File dataFile;
        InputStream fis;
        JsonReader reader;
        // code
        this.path = path;
        dataFile = new File(path);
        if (!dataFile.exists()) { // checks if the data files exists
            db = Json.createObjectBuilder().build(); // if it does not exist, then create an empty JsonObject for the db
        } else { // reading the info in the file:
            fis = new FileInputStream(path); // we're sure the file exists now
            try {
                reader = Json.createReader(fis);
                db = reader.readObject(); // puts the json data in the private JsonObject db
                reader.close();
            } catch (javax.json.JsonException e) { // usually if the file is empty
                db = Json.createObjectBuilder().build();
            }
            fis.close();
        }
    }
    
    /**
     * Creates a production zone on said id with following syntax:
     * 
     * id:{
     *    STR_LAST_PING: current time in milliseconds since 1970,
     *    STR_ITEMS: {}
     * }
     * 
     * @param id is a string with format "dimension-x,y,z" with x, y and z the coordinates of the production zone
     */
    public void createZone(String id) {
        JsonObjectBuilder temp;
        JsonObjectBuilder productionZone = Json.createObjectBuilder()
                .add(STR_LAST_PING, System.currentTimeMillis()) // puts the current time for the last ping
                .add(STR_ITEMS, Json.createObjectBuilder().build()); // creates an empty map at key STR_ITEMS
        temp = jsonObjectToBuilder(db); // saving the current state of the db
        temp = temp.add(id, productionZone); // adding productionZone
        db = temp.build(); // saving the db with the modification
    }
    
    /**
     * Removes a production zone from the db
     * 
     * @param id : the id of the production zone to remove
     * 
     * @throws IllegalArgumentException if it is not in the production zone data base
     */
    public void removeZone(String id) {
        db = removeFromJsonObject(db, id);
    }
    
    /**
     * Adds or modify the informations concerning an item in a production zone.
     * If the item doesn't exist in this production zone, the method adds it, otherwise it changes the corresponding value.
     * 
     * @param itemId is the id of the item to be added or modified
     * @param millis is the time in milliseconds need to generate one exemplar of this item
     * @param zoneId is the id of the production zone where this item has to be changed or added
     */
    public void touchItem(String itemId, long millis, String zoneId) {
        // initiating
        JsonObject productionZone;
        JsonObject items;
        JsonArrayBuilder item;
        // info storage
        checkContainsKey(db, zoneId, MSG_ZONE_NOT_FOUND); // checks if zoneId is a key of the db, throws an IllegalArgumentException if it doesn't
        productionZone = db.getJsonObject(zoneId); // stores the production zone
        checkContainsKey(productionZone, STR_ITEMS, MSG_KEY_NOT_FOUND); // checks if there is a key where to store items
        items = productionZone.getJsonObject(STR_ITEMS); // stores the data about the items in this production zone
        // Applying changes
            // saving items:
        item = Json.createArrayBuilder();
        item = item.add(millis)
                   .add(DEFAULT_LAST_PROGRESSION);
        items = touchJsonObject(items, itemId, item.build());
            // saving the production zone
        productionZone = touchJsonObject(productionZone, STR_ITEMS, items);
            // saving the db
        db = touchJsonObject(db, zoneId, productionZone);
    }
    
    /**
     * Removes an item from a production zone.
     * 
     * @param zoneId is the id of the production zone
     * @param itemId id the id of the item
     */
    public void removeItem(String zoneId, String itemId) {
        // initiating
        JsonObject productionZone;
        JsonObject items;
        // code
            // preparing
        checkContainsKey(db, zoneId, MSG_ZONE_NOT_FOUND); // checks if zoneId is a key of the db, throws an IllegalArgumentException if it doesn't
        productionZone = db.getJsonObject(zoneId); // stores the production zone
        checkContainsKey(productionZone, STR_ITEMS, MSG_KEY_NOT_FOUND); // checks if there is a key where to store items
        items = productionZone.getJsonObject(STR_ITEMS); // stores the data about the items in this production zone
            //changing
        items = removeFromJsonObject(items, itemId); // removes the item's info and key (throws an IllegalArgumentException if there is no correponding key)
        productionZone = touchJsonObject(productionZone, STR_ITEMS, items); // updates the production zone
        db = touchJsonObject(db, zoneId, productionZone); // updates the db
    }
    
    /**
     * Ping a production zone : get all items to drop and their amount.
     * 
     * @zone Id is the id of the productionZone to ping
     */
    public HashMap<String, Long> ping(String zoneId) {
        // checking
        checkContainsKey(db, zoneId, MSG_ZONE_NOT_FOUND); // checks if zoneId is a key of the db, throws an IllegalArgumentException if it doesn't
        // initiating
        HashMap<String, Long> result = new HashMap<String, Long>();
        JsonObject productionZone;
        JsonObject items;
        long now = System.currentTimeMillis();
        long previousPing;
        Set<String> itemsKeySet;
        // code
        productionZone = db.getJsonObject(zoneId);
        items = productionZone.getJsonObject(STR_ITEMS);
        itemsKeySet = items.keySet();
        previousPing = (productionZone.isNull(STR_LAST_PING)) ? now : productionZone.getJsonNumber(STR_LAST_PING).longValue();
        for (String key : itemsKeySet){
            JsonArray item        = items.getJsonArray(key);
            long itemNeededMillis = item.getJsonNumber(INDEX_ITEM_NEEDED_MILLIS).longValue();
            long progression      = item.getJsonNumber(INDEX_ITEM_PROGESSION).longValue();
            long amount           = (now + progression - previousPing) / itemNeededMillis; // entire division as all variables are long
            progression           = (now + progression - previousPing) % itemNeededMillis; // new progression
            item = Json.createArrayBuilder().add(itemNeededMillis)
                                            .add(progression)
                                            .build();
            items = touchJsonObject(items, key, item); // updating the items JsonObject
            // storing result
            result.put(key, amount);
        }
        // updating the db
        productionZone = touchJsonObject(productionZone, STR_LAST_PING, Json.createArrayBuilder().add(now).build().getJsonNumber(0)); // updating the last ping in the production zone
        productionZone = touchJsonObject(productionZone, STR_ITEMS, items); // updating the items informations in the prodution zone
        db = touchJsonObject(db, zoneId ,productionZone); // updating the db
        // returning the map with the items and their amount
        return result;
    }
    
    /**
     * Saves the current state of the private JsonObject db into the data file
     */
    public void save() throws Exception {
        // opnening
        OutputStream out = new FileOutputStream(path);
        JsonWriter writer = Json.createWriter(out);
        // writing
        writer.write(db);
        // closing
        writer.close();
        out.close();
    }
    
    public String toString(){
        return "Storage: " + path + " | Data: " + db.toString();
    }
    
    /**
     * Checks if a given key is part of the JsonObject and throws an IllegalArgumentException if it doesn't
     * 
     * @param object is the object where we want to search for the key
     * @param key is the key spoken above
     */
    private static void checkContainsKey(JsonObject object, String key, String msg) {
        if (!object.containsKey(key)) { // checks if this key is a production zone in db
         throw new IllegalArgumentException(msg);
        }
    }
    
    /**
     * Creates a JsonObjectBuilder object with a JsonObject
     * 
     * @param object is the object with wich the JsonObjectBuilder is created
     */
    private static JsonObjectBuilder jsonObjectToBuilder(JsonObject object){
        JsonObjectBuilder result = Json.createObjectBuilder(); // initiating the result
        for (String key : object.keySet()){
            JsonValue value = object.get(key); // get the corresponding value
            result.add(key, value); // adds it
        }
        return result;
    }
    
    /**
     * Adds or modifies a value  with specified a key to a JsonObject.
     */
    private static JsonObject touchJsonObject(JsonObject object, String key, JsonValue value){
        JsonObjectBuilder temp = jsonObjectToBuilder(object); // stores the object in a changes allowed JsonObjectBuilder
        temp = temp.add(key, value); // adds said value
        return temp.build(); // converts it to a JsonObject
    }
    
    /**
     * Removes a key, value pair in a JsonObject.
     * @throw IllegalArgumentException if the key is not part of the object
     */
    private static JsonObject removeFromJsonObject(JsonObject object, String key){
        checkContainsKey(object, key, MSG_KEY_NOT_FOUND);
        JsonObjectBuilder temp = jsonObjectToBuilder(object); // stores the object in a changes allowed JsonObjectBuilder
        temp.remove(key);
        return temp.build();
    }
}
