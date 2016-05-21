package hk.ust.cse.comp4521.reminder.data;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.util.Calendar;

import hk.ust.cse.comp4521.reminder.util.DateTimeParser;

/**
 * Created by Krauser on 9/5/2016.
 */
public class ReminderData implements Serializable{

    public enum ReminderType{Time, Location}

    public final static int REPEAT_ARRAY_LENGTH=7;

    private long id = -1;
    private ReminderType reminderType = null;
    private String title = null;
    private String description = null;
    private String imagePath = null;
    private Time time = null;    //time only, no date
    private boolean[] repeat = new boolean[REPEAT_ARRAY_LENGTH];       // Store the day for repetition, Mon to Fri
    private String location = null;
    private Double longitude = null;
    private Double latitude = null;
    private Date validUntilDate = null;                     // location event may valid until a certain time
    private Time validUntilTime = null;
    private long lastModify = 0L;                           //I should change all time related attribute to long in the beginning :(
    private boolean enabled = false;

    public ReminderData(){

    }

    public void setId(long id){
        this.id = id;
    }

    public long getId(){
        return this.id;
    }

    public boolean hasId(){
        return this.id!=-1;
    }

    public ReminderType getReminderType(){
        return reminderType;
    }

    public void setReminderType(ReminderType reminderType){
        this.reminderType = reminderType;
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

    public int getTime(int field){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time.getTime());
        return c.get(field);
    }

    public long getTimeInMillis(){
        return time.getTime();
    }

    public String getValidUntil(){
        return getValidUntilDate()+" "+getValidUntilTime();
    }

    public long getValidUntilInMillis(){
        return validUntilTime.getTime() + validUntilDate.getTime();
    }

    public void setValidUntil(String time){
        String[] times = time.split(" ");
        setValidUntilDate(times[0]);
        setValidUntilTime(times[1]);
    }

    public String getValidUntilDate(){
        return DateTimeParser.toString(validUntilDate, DateTimeParser.Format.DATE);
    }

    public int getValidUntilDate(int field){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(validUntilDate.getTime());
        return c.get(field);
    }

    public String getValidUntilTime(){
        return DateTimeParser.toString(validUntilTime, DateTimeParser.Format.SHORT);
    }

    public int getValidUntilTime(int field){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(validUntilTime.getTime());
        return c.get(field);
    }

    public void setValidUntilTime(String time){
        try {
            long t = DateTimeParser.toLong(time, DateTimeParser.Format.SHORT);
            validUntilTime = new Time(DateTimeParser.toHour(t), DateTimeParser.toMin(t), 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setValidUntilDate(String date){
        try {
            long t = DateTimeParser.toLong(date, DateTimeParser.Format.DATE);
            validUntilDate = new Date(DateTimeParser.toYear(t)-1900, DateTimeParser.toMonth(t), DateTimeParser.toDay(t));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //TODO: no more object reference
    public void setRepeat(boolean[] repeat){
        this.repeat = repeat;
    }

    //TODO: no more object reference
    public boolean[] getRepeat(){
        return repeat;
    }

    public boolean noRepeat(){
        for(int i=0; i<repeat.length; i++)
            if(repeat[i])
                return false;
        return true;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public boolean isEnabled(){
        return enabled;
    }

    public String getImageUri() {
        return imagePath;
    }

    public void setImageUri(String imageUri) {
        this.imagePath = imageUri;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public long getLastModify() {
        return lastModify;
    }

    public void setLastModify(long lastModify) {
        this.lastModify = lastModify;
    }
}

