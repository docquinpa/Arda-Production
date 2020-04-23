package productionzones;

import com.google.gson.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.lang.Exception.*;


/**
 * Class that handles production zones stored in a file and allows to dialogue with it. 
 * It allows to create, modify, view, delete zones and to call them to obtain a certain amount of items to be produced when pinged.
 * This amount of items changes according to the time that has elapsed between two of these "pings" and the time that is needed to produce an item.
 * 
 * This data base is based on the ids of the production zones. The structure of theses ids is chosen by the user, it has however to be Strings.
 * 
 * There is also the possibility to put a limit to the production of a zone according to the elapsed time if this time would be bigger as the limit.
 * The default to this time limit is 30 days.
 * 
 * Note: You need to explicitly call the save method to save all the changes into said file.
 * 
 * @author GandalfTheSecond
 */
public class ProductionZonesDB{
    
    private JsonObject db;
    private String     path;
    private boolean    saved;
    
    private static final String MSG_ZONE_NOT_FOUND        = "This production zone %s does not exist";
    private static final String MSG_ZONE_ALREADY_EXISTS   = "This production zone %s already exists";
    private static final String MSG_KEY_NOT_FOUND         = "Map object doesn't contains this key";
    private static final String MSG_INVALID_MAX_PROD_TIME = "The maximum produtction time is not valid";
    private static final String MSG_ITEM_NOT_FOUND        = "This item %s has not been found";
    
    private static final String STR_LAST_PING             = "last-ping";
    private static final String STR_ITEMS                 = "items";
    private static final String STR_ITEM_NEEDED_MILLIS    = "needed";
    private static final String STR_ITEM_PROGRESSION      = "progression";
    private static final String STR_ADDITIONAL_DATA       = "@@DATA$";
    private static final String STR_MAX_PRODUCTION_TIME   = "max-production-time-lapse";
    
    private static final int    DEFAULT_LAST_PROGRESSION  = 0;
    
    private static final long   DAY_MILLISECONDS          = 86_400_000;
    private static final long   MONTH_MILLISECONDS        = 30 * DAY_MILLISECONDS;
    private static final long   DEFAULT_MAX_PROD_TIME     = MONTH_MILLISECONDS;
    
    
    /**
     * Default constructor for the ProductionZonesDB class.
     * 
     * @param path is the path to the file where the data about the production zones are supposed to be.
     * If the file does not exist, it creates an empty data base. (the file would be created in the save() method)
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    public ProductionZonesDB(String path) throws Exception{ // throws Exception -> it is expected that exceptions could be thrown
        // initiation
        File dataFile;
        BufferedReader bufferedReader;
        //code
        this.path = path;
        dataFile = new File(path);
        saved = true;
        if (!dataFile.exists()) { // checks if the data files exists
            createDB();
        } else { // reading the info in the file:
            bufferedReader = new BufferedReader(new FileReader(path));
            db = new Gson().fromJson(bufferedReader, JsonObject.class);
            bufferedReader.close();
            // making some verifications
            if (db == null || db.toString().trim().isEmpty()){
                createDB();
            }
        }
    }
    
    private void createDB(){
        db = new JsonObject();
        addDefaultAdditionalData(); // adding the default things for the data
    }
    
    /**
     * Creates a production zone on said id with following syntax:
     * 
     * id:{
     *    STR_LAST_PING: current time in milliseconds since 1970,
     *    STR_ITEMS: {}
     * }
     * 
     * @param id is a string with with the production zone will be identified. Do note that it must be unique,
     * it will override an existing production zone.
     */
    public void createZone(String zoneId) {
        JsonObject zone;
        if (db.has(zoneId)){ // the zone already exists, to avoid it to be overread
            throw new IllegalArgumentException(String.format(MSG_ZONE_ALREADY_EXISTS, zoneId));
        }
        zone = new JsonObject();
        zone.addProperty(STR_LAST_PING, System.currentTimeMillis()); // adds the current timestamp in milliseconds as the last ping
        zone.add(STR_ITEMS, new JsonObject()); // adding an empty item list
        db.add(zoneId, zone);
        saved = false;
    }
    
    /**
     * Removes a production zone from the db
     * 
     * @param id : the id of the production zone to remove
     * 
     * @throws IllegalArgumentException if it is not in the production zone data base
     */
    public void removeZone(String zoneId) {
        if (db.has(zoneId)){
            db.remove(zoneId);
            saved = false;
        } else { // means there is nothing to remove
            throw new IllegalArgumentException(String.format(MSG_ZONE_ALREADY_EXISTS, zoneId));
        }
    }
    
    /**
     * Adds or modify the informations concerning an item in a production zone.
     * If the item doesn't exist in this production zone, the method adds it, otherwise it changes the corresponding value.
     * 
     * @param zoneId is the id of the production zone where this item has to be changed or added
     * @param itemId is the id of the item to be added or modified
     * @param millis is the time in milliseconds need to generate one exemplar of this item
     */
    public void touchItem(String zoneId, String itemId, long millis) {
        JsonObject zone;
        JsonObject items;
        JsonObject item;
        JsonObject itemData;
        if (!db.has(zoneId)) { throw new IllegalArgumentException(String.format(MSG_ZONE_ALREADY_EXISTS, zoneId));}
        // getting the info on the production zone
        zone = db.getAsJsonObject(zoneId);
        items = zone.getAsJsonObject(STR_ITEMS);
        // creating the data on the item
        itemData = new JsonObject();
        itemData.addProperty(STR_ITEM_NEEDED_MILLIS, millis);
        itemData.addProperty(STR_ITEM_PROGRESSION, DEFAULT_LAST_PROGRESSION);
        // saving the data
        items.add(itemId, itemData); // adding the item to the other items of the zone
        zone.add(STR_ITEMS, items); // updating the production zone
        db.add(zoneId, zone); // updating the data base
        saved = false;
    }
    
    /**
     * Removes an item from a production zone.
     * 
     * @param zoneId is the id of the production zone
     * @param itemId id the id of the item
     */
    public void removeItem(String zoneId, String itemId) {
        JsonObject zone;
        JsonObject items;
        // getting the info on the production zone
        if (!db.has(zoneId)) { throw new IllegalArgumentException(String.format(MSG_ZONE_ALREADY_EXISTS, zoneId));}
        zone = db.getAsJsonObject(zoneId);
        items = zone.getAsJsonObject(STR_ITEMS);
        if (!items.has(itemId)) { throw new IllegalArgumentException(String.format(MSG_ITEM_NOT_FOUND, itemId));}
        items.remove(itemId);
        zone.add(STR_ITEMS, items); // updating the production zone
        db.add(zoneId, zone); // updating the data base
        saved = false;
    }
    
    /**
     * Ping a production zone : get all items to drop and their amount.
     * 
     * @zone Id is the id of the productionZone to ping
     */
    public Map<String, Long> ping(String zoneId) {
        // initiating
        Map<String, Long> result = new HashMap<String, Long>();
        JsonObject zone;
        JsonObject items;
        JsonObject item;
        long now = System.currentTimeMillis();
        long lastPing;
        long maxProdTime = getMaxProdTime(); // getting the maximimum production time that is allowed in the db
        Set<String> itemsKeySet;
        // code
        if (!db.has(zoneId)) { throw new IllegalArgumentException(String.format(MSG_ZONE_ALREADY_EXISTS, zoneId));} // checking
        zone        = db.getAsJsonObject(zoneId);
        lastPing    = (zone.has(STR_LAST_PING)) ? zone.get(STR_LAST_PING).getAsLong() : now ; // if the case is no, then it will be changed later
        items       = zone.getAsJsonObject(STR_ITEMS);
        itemsKeySet = getKeySet(items);
        for (String itemId : itemsKeySet){
            // calculating
            item = items.getAsJsonObject(itemId);
            long neededMillis     = item.get(STR_ITEM_NEEDED_MILLIS).getAsLong();
            long lastProgression  = item.get(STR_ITEM_PROGRESSION).getAsLong();
            long totalProgression = Math.min(now + lastProgression - lastPing, maxProdTime); // if the total progrssion is greater than what is authorized, it is changed to the maximum that is authorized
            long amount           = totalProgression / neededMillis; // entire division as all variables are long
            lastProgression       = totalProgression % neededMillis; // new last progression
            // storing results
            item.addProperty(STR_ITEM_PROGRESSION, lastProgression);
            items.add(itemId, item); // storing in the items list
            result.put(itemId, amount); // saving the amount for this particular item
        }
        zone.add(STR_ITEMS, items);
        zone.addProperty(STR_LAST_PING, now);
        db.add(zoneId, zone);
        saved = false;
        return result;
    }
    
    /**
     * Saves the current state of the private JsonObject db into the data file
     */
    public void save() throws Exception {
        if (!saved){ // avoiding unnecessary saves
            try (Writer writer = new FileWriter(this.path)) {
                Gson gson = new GsonBuilder().create();
                gson.toJson(db, writer);
                saved = true;
            } catch (Exception e){
                throw e;
            }
        }
    }
    
    /**
     * Simple toString method, giving the path to the data file and what contains the data base.
     */
    public String toString(){
        return "Storage: " + path + " | Data: " + db.toString();
    }
    
    /**
     * Returns a set composed by all zoneIds in the data base
     */
    public Set<String> allZoneIds(){
        return getKeySet(db);
    }
    
    /**
     * This method gives the last ping as a timestamp (milliseconds format) and all the items and their amount of milliseconds required to create one examplar 
     */
    public Map<String, Long> zoneInfo(String zoneId){
        Map<String, Long> result = new HashMap<String, Long>();
        JsonObject zone;
        JsonObject items;
        // getting the info on the production zone
        if (!db.has(zoneId)) { throw new IllegalArgumentException(String.format(MSG_ZONE_ALREADY_EXISTS, zoneId));}
        zone = db.getAsJsonObject(zoneId);
        items = zone.getAsJsonObject(STR_ITEMS);
        // storing the results
        result.put(STR_LAST_PING, zone.get(STR_LAST_PING).getAsLong()); // adding the last ping timestamp
        Set<String> allItemsIds = getKeySet(items);
        for (String itemId : allItemsIds){
            JsonObject itemInfos = items.getAsJsonObject(itemId); // obtaining the informations stored for this item
            Long timestamp = itemInfos.get(STR_ITEM_NEEDED_MILLIS).getAsLong(); // obtaining the needed timestamp
            result.put(itemId, timestamp); // adding the item and it's needed timestamp
        }
        return result;
    }
    
    /**
     * Change the maximum prodcution time allowed for all prodcution zones.
     * 
     * @param timeLapse is this maximum. It must be expressed in millisceonds ans be greater than 0.
     * 
     * @throws IllegalArgumentException if the timeLapse <= 0
     */
    public void changeMaxProdictionTime(long timeLapse){
        if (db.has(STR_ADDITIONAL_DATA)) { // means the additionnal data is there
            JsonObject additionalData = db.getAsJsonObject(STR_ADDITIONAL_DATA);
            additionalData.addProperty(STR_MAX_PRODUCTION_TIME, timeLapse);
        } else {
            addDefaultAdditionalData();
            changeMaxProdictionTime(timeLapse); // laziness: 100%
        }
        saved = false;
    }
    
    /**
     * Creating a default additionnal data
     */
    private void addDefaultAdditionalData(){
        JsonObject additionalData = new JsonObject();
        additionalData.addProperty(STR_MAX_PRODUCTION_TIME, DEFAULT_MAX_PROD_TIME);
        db.add(STR_ADDITIONAL_DATA, additionalData);
        saved = false;
    }
    
    /**
     * Gives the maximum production time as stated in the db and creates it and gives the default if it isn't in the db
     * 
     * In certain cases, this methos changes things in the DB if some things are missing, so you might want to use the method save afterwards
     */
    private long getMaxProdTime(){
        long maxProdTime = DEFAULT_MAX_PROD_TIME; // default
        if (db.has(STR_ADDITIONAL_DATA)) { // means the additionnal data is there
            JsonObject additionalData = db.getAsJsonObject(STR_ADDITIONAL_DATA);
            if (additionalData.has(STR_MAX_PRODUCTION_TIME)) { // means the maximum production time is there, all in order
                // the real thing happens here
                maxProdTime = additionalData.get(STR_MAX_PRODUCTION_TIME).getAsLong();
                // it's over, all the other things are there to avoid an error
            } else { // no maximum production time in there, needs to be fixed
                changeMaxProdictionTime(DEFAULT_MAX_PROD_TIME);
            }
        } else { // no additional data in there, need to fix it
            addDefaultAdditionalData();
        }
        return maxProdTime;
    }
    
    /**
     * Creates a key set for the JsonObject
     */
    private Set<String> getKeySet(JsonObject o){
        Set<String> result = new HashSet<String>();
        Set<Map.Entry<String, JsonElement>> entries = o.entrySet();//will return members of your object
        for (Map.Entry<String, JsonElement> entry: entries) {
            result.add(entry.getKey());
        }
        return result;
    }
}
