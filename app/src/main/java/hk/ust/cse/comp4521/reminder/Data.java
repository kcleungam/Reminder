package hk.ust.cse.comp4521.reminder;

import java.sql.Time;

/**
 * Created by Krauser on 9/5/2016.
 */
public class Data {
    public static enum ReminderType{Time, Location}

    public ReminderType reminderType = null;
    public String title = null;
    public Time time = null;
    public Time validUntil = null;                     // location event may valid until a certain time
    public boolean[] repeat = new boolean[7];       // Store the day for repetition, Mon to Fri
    public String description = null;
    public String location = null;  // Please change to correct type  when you use it
    // Image

    /**
     *         Location event constructor, should inculde:      location, title, validUntil, description, image
     */
    public Data(String title, Time validUntil, String description){  // need to add location and image
        this.reminderType = ReminderType.Location;
        this.title = title;
        this.validUntil = validUntil;
        this.description = description;
    }

    /**
     *          TIme event constructor, should include:     time, title, repeat, description,
     */
    public Data(String title, Time time, boolean[] repeat, String description){     // need to add location and image
        this.reminderType = ReminderType.Time;
        this.title = title;
        this.time = time;
        this.repeat = repeat;
        this.description = description;
    }

}

