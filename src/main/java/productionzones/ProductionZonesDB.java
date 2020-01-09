package productionzones;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.json.*;

public class ProductionZonesDB{
	
	private JsonObject db;
	
	private final static String TESTING_DATA_FILE = "production-zones.json";
	private final static String DATA_FILE = TESTING_DATA_FILE;//"production-zones.json";
	
	/**
	 * Default constructor for the ProductionZonesDB class.
	 * it creates a data file if it doesn't exist
	 * 
	 * @throws IOException if something has occurred with I/O
	 */
	public ProductionZonesDB() throws IOException {
		File dataFile = new File(DATA_FILE);
		if (dataFile.exists()) { // checks if the data files exists
			dataFile.createNewFile(); // if this files does not exists, creates new file
		}
		InputStream fis = new FileInputStream(DATA_FILE); // we're sure the file exists now
	    JsonReader reader = Json.createReader(fis);
	    db = reader.readObject(); // puts the json data in the private JsonObject db
	    }
	
	/**
	 * Creates a production zone on said id
	 * 
	 * @param id is a string with format "dimension-x,y,z" with x, y and z the coordinates of the prodution zone
	 */
	public void create(String id) {}
	
	
	public void addItems() {}
	
	/**
	 * Saves the current state of the private JsonObject db into the data file
	 */
	public void save() {}
	
}
