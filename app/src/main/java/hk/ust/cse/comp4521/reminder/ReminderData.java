package hk.ust.cse.comp4521.reminder;

import java.io.Serializable;
import java.sql.Time;
import java.text.ParseException;

/**
 * Created by Krauser on 9/5/2016.
 */
public class ReminderData implements Serializable{

    public enum ReminderType{Time, Location}

    private long id = -1;
    private ReminderType reminderType = null;
    private String title = null;
    private Time time = null;    //time only, no date
    private String location = null;
    private Time validUntil = null;                     // location event may valid until a certain time
    private boolean[] repeat = new boolean[7];       // Store the day for repetition, Mon to Fri
    private String description = null;
    private boolean enabled = false;
    // Location
    // Image

    public ReminderData(){

    }

    /**
     *         Location event constructor, should inculde:      location, title, validUntil, description, image
     */
    public ReminderData(String title, Time validUntil, String description){  // need to add location and image
        this.reminderType = ReminderType.Location;
        this.setTitle(title);
        this.validUntil = validUntil;
        this.setDescription(description);
    }

    /**
     *          TIme event constructor, should include:     time, title, repeat, description,
     */
    public ReminderData(String title, Time time, boolean[] repeat, String description){     // need to add location and image
        this.reminderType = ReminderType.Time;
        this.setTitle(title);
        this.time = time;
        this.setRepeat(repeat);
        this.setDescription(description);
    }

    public void setId(long id){
        this.id = id;
    }

    public long getId(){
        return this.id;
    }

    public String getReminderType(){
        return this.reminderType.name();
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

    public String getTitle(){
        return this.title;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription(){
        return this.description;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public String getLocation(){
        return this.location;
    }

    public void setTime(String time){
        try {
            this.time = DateTimeParser.toTime(time, DateTimeParser.Format.SHORT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getTime(){
        return DateTimeParser.toString(time, DateTimeParser.Format.SHORT);
    }

    public void setValidUntil(String time){
        try {
            validUntil = DateTimeParser.toTime(time, DateTimeParser.Format.ISO8601);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getValidUntil() {
        return DateTimeParser.toString(validUntil, DateTimeParser.Format.ISO8601);
    }

    public String getValidUntilDate(){
        return DateTimeParser.toString(validUntil, DateTimeParser.Format.DATE);
    }

    public String getValidUntilTime(){
        return DateTimeParser.toString(validUntil, DateTimeParser.Format.SHORT);
    }

    //TODO: no more object reference
    public void setRepeat(boolean[] repeat){
        this.repeat = repeat;
    }

    //TODO: no more object reference
    public boolean[] getRepeat(){
        return repeat;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public boolean isEnabled(){
        return enabled;
    }

}

