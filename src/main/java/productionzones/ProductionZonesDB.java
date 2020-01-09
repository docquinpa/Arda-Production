package productionzones;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.json.*;

public class ProductionZonesDB{
	
	private JsonObject db;
	
	private static final String MSG_KEY_NOT_FOUND = "Map object doesn't contains this key";
	
	private static final String STR_LAST_PING = "last-ping";
	private static final String STR_ITEMS 	  = "items";
	
	
	/**
	 * Default constructor for the ProductionZonesDB class.
	 * it creates a data file if it doesn't exist
	 * 
	 * @throws IOException if something has occurred with I/O
	 */
	public ProductionZonesDB(String path) throws IOException {
		File dataFile = new File(path);
		if (dataFile.exists()) { // checks if the data files exists
			dataFile.createNewFile(); // if this files does not exists, creates new file
		}
		InputStream fis = new FileInputStream(path); // we're sure the file exists now
	    JsonReader reader = Json.createReader(fis);
	    db = reader.readObject(); // puts the json data in the private JsonObject db
	    }
	
	/**
	 * Creates a production zone on said id with following synatx:
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
				.add(STR_ITEMS, (JsonObject) Json.createObjectBuilder()); // creates an empty map at key STR_ITEMS
		temp = (JsonObjectBuilder) db; // saving the current state of the db
		db = (JsonObject) temp.add(id, productionZone); // adding productionZone to the db
	}
	
	/**
	 * Removes a production zone from the db
	 * 
	 * @param id : the id of the production zone to remove
	 * 
	 * @throws IllegalArgumentException id is not in the production zone db
	 */
	public void removeZone(String id) {
		if (!db.containsKey(id)) { // checks if this key is a production zone in db
			throw new IllegalArgumentException(MSG_KEY_NOT_FOUND);
		}
		db.remove(id); // removing what's associated with the key known as id
	}
	
	public void addItems() {}
	
	public void removeItems() {}
	
	public void ping(String id) {}
	
	/**
	 * Saves the current state of the private JsonObject db into the data file
	 */
	public void save() {}
}
