package productionzones;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.json.*;


/**
 * Class that handles the production zones stored in a file and allows to dialogue with it
 * 
 * Note: You need to explicitly call the save method to save all the changes into said file
 */
public class ProductionZonesDB{
    
    private JsonObject db;
    private String     path;
    
    private static final String MSG_KEY_NOT_FOUND        = "Map object doesn't contains this key";
    
    private static final String STR_LAST_PING            = "last-ping";
    private static final String STR_ITEMS                = "items";
    
    private static final int    DEFAULT_LAST_PROGRESSION = 0;
    
    
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
            dataFile.createNewFile(); // if this files does not exists, creates new file
        } else {
            fis = new FileInputStream(path); // we're sure the file exists now
            try {
                reader = Json.createReader(fis);
                db = reader.readObject(); // puts the json data in the private JsonObject db
                reader.close();
            } catch (javax.json.JsonException e) {
                db = Json.createObjectBuilder().build();
            }
            fis.close();
        }
    }
    
    
    /**
     * Creates a production zone on said id with following syntax:
     * 
     * dim-x,y,z:{
     *    STR_LAST_PING: current time in milliseconds since 1970,
     *    STR_ITEMS: {}
     * }
     * 
     * TODO: check the validity of {@param id}
     * 
     * @param id is a string with format "dimension-x,y,z" with x, y and z the coordinates of the production zone
     */
    public void createZone(String id) {
        JsonObjectBuilder temp;
        JsonObjectBuilder productionZone = Json.createObjectBuilder()
                .add(STR_LAST_PING, System.currentTimeMillis()) // puts the current time for the last ping
                .add(STR_ITEMS, Json.createObjectBuilder().build()); // creates an empty map at key STR_ITEMS
        temp = (JsonObjectBuilder) db; // saving the current state of the db
        temp = temp.add(id, productionZone); // adding productionZone
        db = temp.build(); // saving the db with the modification
    }
    
    /**
     * Removes a production zone from the db
     * 
     * @param id : the id of the production zone to remove
     * 
     * @throws IllegalArgumentException id is not in the production zone db
     */
    public void removeZone(String id) {
        checkContainsKey(db, id); // checks if id is a key of the db, throws an IllegalArgumentException if it doesn't
        db.remove(id); // removing what's associated with the key known as id
    }
    
    public void addItem(String itemId, int millis, String zoneId) {
        // initiating
        JsonObject productionZone;
        JsonObject items;
        JsonArrayBuilder item;
        JsonObjectBuilder temp;
        // info storage
        checkContainsKey(db, zoneId); // checks if zoneId is a key of the db, throws an IllegalArgumentException if it doesn't
        productionZone = db.getJsonObject(itemId); // stores the production zone
        checkContainsKey(productionZone, STR_ITEMS); // checks if there is a key where to store items
        items = productionZone.getJsonObject(STR_ITEMS); // stores the data about the items in this production zone
        // Applying changes
            // saving items:
        if (items.containsKey(itemId)) {
            items.remove(itemId); // removing so we don't have problems when we're adding the changes
        }
        item = Json.createArrayBuilder();
        item = item.add(millis)
                   .add(DEFAULT_LAST_PROGRESSION);
        temp = (JsonObjectBuilder) items; // allowing to add something into items
        temp = temp.add(itemId, item);
        items = temp.build();
            // saving the production zone
        productionZone.remove(STR_ITEMS); // removing to avoid collision while adding
        temp = (JsonObjectBuilder) productionZone;
        temp = temp.add(STR_ITEMS, items);
        productionZone = temp.build();
            // saving the db
        temp = (JsonObjectBuilder) db;
        
    }
    
    public void removeItem() {}
    
    public void ping(String id) {}
    
    /**
     * Saves the current state of the private JsonObject db into the data file
     */
    public void save() {}
    
    /**
     * Checks if a given key is part of the JsonObject and throws an IllegalArgumentException if it doesn't
     * 
     * @param object is the object where we want to search for the key
     * @param key is the key spoken above
     */
    private static void checkContainsKey(JsonObject object, String key) { 
        if (!object.containsKey(key)) { // checks if this key is a production zone in db
         throw new IllegalArgumentException(MSG_KEY_NOT_FOUND);
        }
    }
    /* //testing
    public static void main() {
        JsonObject a;
        JsonObject b;
        JsonObjectBuilder c;
        c = Json.createObjectBuilder().add("a", 1);
        a = c.build();
        c = (JsonObjectBuilder) a;
        b = c.build();
        System.out.print(a.equals(b));
    }*/
}
