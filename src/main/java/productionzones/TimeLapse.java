package productionzones;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that translates a user given timelapse
 *
 * @author GandalfTheSecond
 * @version 24/04/2020
 */
public class TimeLapse
{
    private long millis;
    
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60_000;
    private static final int HOUR_MILLIS   = 3_600_000;
    private static final int DAY_MILLIS    = 86_400_000;
    private static final int WEEK_MILLIS   = 604_800_000;
    
    private static final char DEFAULT_UNIT_MILLIS = SECOND_MILLIS;
    
    private static final char BLANK  = ' ';
    private static final char SECOND = 's';
    private static final char MINUTE = 'm';
    private static final char HOUR   = 'h';
    private static final char DAY    = 'd';
    private static final char DAY_FR = 'j';
    private static final char WEEK   = 'w';
    
    private static final Map<Character, Integer> INFO_MAP;
    static {
        Map<Character, Integer> map = new HashMap<Character, Integer>();
        map.put(SECOND, SECOND_MILLIS);
        map.put(MINUTE, MINUTE_MILLIS);
        map.put(HOUR,   HOUR_MILLIS);
        map.put(DAY,    DAY_MILLIS);
        map.put(DAY_FR, DAY_MILLIS);
        map.put(WEEK,   WEEK_MILLIS);
        INFO_MAP = map;
    }
    
    /**
     * Constructor, the input is the user's input inf this format : "9d6h" with letters such as w, d, h, m, s for weeks, days, hours, minutes and seconds
     */
    public TimeLapse(String input)
    {
        millis = constructMillis(input);
        
    }

    private long constructMillis(String input){
        long result = 0;
        String obtained = "";
        for (int i = 0; i < input.length(); i++){
            char c = input.charAt(i);
            if (c != BLANK) {
                if (Character.isDigit(c)){
                    obtained += c;
                } else if (Character.isAlphabetic(c) && !obtained.equals("")){
                    if (INFO_MAP.containsKey(c)){
                        long amount = new Integer(obtained);
                        long unitMillis = INFO_MAP.get(c);
                        result += amount * unitMillis;
                        obtained = "";
                    }
                }
            }
        }
        if (!obtained.trim().isEmpty()){
            int amount = new Integer(obtained);
            result += amount * DEFAULT_UNIT_MILLIS;
        }
        return result;
    }
    
    /**
     * Gives the amount in milliseconds of the timelapse given by the user
     */
    public long getMillis() {
        return this.millis;
    }
    
    /**
     * Gives the the time lapse in human readable amounts of time.
     * Example : "8 days, 9 hours, "
     */
    public String getMessage(){
        long milliseconds = this.millis;
        StringBuilder result = new StringBuilder();
        String[] units = {"weeks", "days", "hours", "minutes", "seconds"};
        //
        for (int i = 0; i < units.length; i++){
            int unitValue = INFO_MAP.get(units[i].charAt(0));
            long amount = milliseconds / unitValue;
            milliseconds = milliseconds % unitValue;
            if (amount > 0){
                result.append(String.format("%s %s ", amount, units[i]));
            }
        }
        if (milliseconds > 0) {
            result.append(String.format("%s milliseconds", milliseconds));
        }
        return result.toString();
    }
    
    public String toString(){
        return String.format("%s milliseconds: %s", getMillis(), getMessage());
    }
}
