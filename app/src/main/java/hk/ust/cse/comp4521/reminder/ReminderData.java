package hk.ust.cse.comp4521.reminder;

import java.sql.Time;

/**
 * Created by Krauser on 9/5/2016.
 */
public class ReminderData {
    public enum ReminderType{Time, Location}

    public long id;
    public ReminderType reminderType = null;
    public String title = null;
    public Time time = null;    //time only, no date
    public String location = null;
    public Time validUntil = null;                     // location event may valid until a certain time
    public boolean[] repeat = new boolean[7];       // Store the day for repetition, Mon to Fri
    public String description = null;
    public boolean enabled = false;
    // Location
    // Image

    public ReminderData(){

    }

    /**
     *         Location event constructor, should inculde:      location, title, validUntil, description, image
     */
    public ReminderData(String title, Time validUntil, String description){  // need to add location and image
        this.reminderType = ReminderType.Location;
        this.title = title;
        this.validUntil = validUntil;
        this.description = description;
    }

    /**
     *          TIme event constructor, should include:     time, title, repeat, description,
     */
    public ReminderData(String title, Time time, boolean[] repeat, String description){     // need to add location and image
        this.reminderType = ReminderType.Time;
        this.title = title;
        this.time = time;
        this.repeat = repeat;
        this.description = description;
    }

    public void setId(long id){
        this.id = id;
    }

    public long getId(){
        return this.id;
    }

    public void setReminderType(String reminderType){
        try {
            this.reminderType = ReminderType.valueOf(reminderType);
        }catch(Exception e){
            this.reminderType = null;
        }
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public void setTime(Time time){
        this.time = time;
    }

    public void setValidUntil(Time time){
        this.time = time;
    }

    public void setRepeat(boolean[] repeat){
        this.repeat = repeat;
    }

}

