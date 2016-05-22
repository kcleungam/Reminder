package hk.ust.cse.comp4521.reminder.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Calendar;

import hk.ust.cse.comp4521.reminder.service.AlarmReceiver;
import hk.ust.cse.comp4521.reminder.service.GeofenceTransitionIntentService;
import hk.ust.cse.comp4521.reminder.service.GoogleApiClientProvider;

/**
 * Created by Jeffrey on 17/5/2016.
 */
public class DataController implements ResultCallback<Status> {

    public final static String TAG = "DataController";

    private static DataController instance;
    private Context context;
    private DataAccessObject dataAccessObject;

    public static final int NOTIFICATION_DELAY=10;  //no runtime use at the moment
    public static final int TIME_ALARM_NOTIFY_BEFORE = 10; //seconds
    public final static float GEOFENCE_RADIUS=500;//radius in meters

    private GoogleApiClient googleApiClient;
    private ArrayList<PendingGeofenceAction> pendingGeofenceActions;
    public class PendingGeofenceAction{
        Action action;
        ReminderData reminder;
        long reminderId;
        public PendingGeofenceAction(Action action, ReminderData reminder){
            this.action = action;
            this.reminder = reminder;
        }
        public PendingGeofenceAction(Action action, long reminderId){
            this.action = action;
            this.reminderId = reminderId;
        }
    }
    enum Action{
        ADD, REMOVE
    }

    //singleton pattern, no public constructor
    private DataController(){

    }

    public static DataController getInstance(Context application){
        if(instance==null) {
            if(application==null)
                throw new IllegalArgumentException("context is null");
            instance = new DataController();
            instance.dataAccessObject = new DataAccessObject(application);
            instance.context = application;
            instance.googleApiClient = GoogleApiClientProvider.getInstance(application, instance);
            instance.pendingGeofenceActions = new ArrayList<>();
            // 如果資料庫是空的，就建立一些範例資料
            // 這是為了方便測試用的，完成應用程式以後可以拿掉
        }
        return instance;
    }

    public ReminderData getReminder(long reminderId){
        return dataAccessObject.get(reminderId);
    }

    public ArrayList<ReminderData> getAll(){
        return dataAccessObject.getAll();
    }

    public void clear(){
        dataAccessObject.clear();
    }

    public int getCount(){
        return dataAccessObject.getCount();
    }

    public boolean addReminder(ReminderData reminderData){
        dataAccessObject.insert(reminderData);
        if(reminderData.isEnabled()) {
            switch (reminderData.getReminderType()) {
                case Location:
                    addGeofence(reminderData);
                    break;
                case Time:
                    addAlarm(reminderData);
                    break;
            }
        }
        return reminderData.getId()!=-1;
    }

    public boolean putReminder(ReminderData reminderData){
        if(dataAccessObject.update(reminderData)) {
            switch (reminderData.getReminderType()) {
                case Location:
                    removeGeofence(reminderData.getId());
                    break;
                case Time:
                    removeAlarm(reminderData.getId());
                    break;
            }
            if(reminderData.isEnabled()) {
                switch (reminderData.getReminderType()) {
                    case Location:
                        addGeofence(reminderData);
                        break;
                    case Time:
                        addAlarm(reminderData);
                        break;
                }
            }
            return true;
        }else
            return false;
    }

    public boolean deleteReminder(long id){
        ReminderData reminderData = getReminder(id);
        if(dataAccessObject.delete(id)){
            switch (reminderData.getReminderType()) {
                case Location:
                    removeGeofence(reminderData.getId());
                    break;
                case Time:
                    removeAlarm(reminderData.getId());
                    break;
            }
            return true;
        }else
            return false;
    }

    public boolean deleteReminder(ReminderData reminderData){
        if(dataAccessObject.delete(reminderData.getId())){
            switch (reminderData.getReminderType()) {
                case Location:
                    removeGeofence(reminderData.getId());
                    break;
                case Time:
                    removeAlarm(reminderData.getId());
                    break;
            }
            return true;
        }else
            return false;
    }

    public void enableReminder(long reminderId, boolean enable){
        ReminderData reminderData = getReminder(reminderId);
        reminderData.setEnabled(enable);
        putReminder(reminderData);
        if(enable) {
            switch (reminderData.getReminderType()) {
                case Location:
                    removeGeofence(reminderId);
                    addGeofence(reminderData);
                    break;
                case Time:
                    removeAlarm(reminderId);
                    addAlarm(reminderData);
                    break;
            }
        }else{
            switch (reminderData.getReminderType()) {
                case Location:
                    removeGeofence(reminderId);
                    break;
                case Time:
                    removeAlarm(reminderId);
                    break;
            }
        }
    }

    private boolean removeAlarm(long reminderId){
        //取得AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        // find the old PendingIntent
        for(int i=0; i<7; i++) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) reminderId*7+i, intent, PendingIntent.FLAG_NO_CREATE);
            if(pendingIntent==null)
                continue;
            // Now cancel the alarm that matches the old PendingIntent
            alarmManager.cancel(pendingIntent);
        }
        return true;
    }

    private void addAlarm(ReminderData reminderData){
        if(reminderData.getReminderType()!= ReminderData.ReminderType.Time)
            throw new IllegalArgumentException("Invalid reminder type.");

        //使用Calendar指定時間
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, reminderData.getTime(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, reminderData.getTime(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.SECOND, -TIME_ALARM_NOTIFY_BEFORE);

        //取得AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //建立意圖
        //這裡的 this 是指當前的 Activity
        //AlarmReceiver.class 則是負責接收的 BroadcastReceiver

        // Now create and schedule a new Alarm
        Intent intent = new Intent(context, AlarmReceiver.class); // New component for alarm
        intent.putExtra("ReminderId", reminderData.getId());
        intent.putExtra("ReminderType", reminderData.getReminderType().ordinal());

        //設定一個警報
        //參數1,我們選擇一個會在指定時間喚醒裝置的警報類型
        //參數2,將指定的時間以millisecond傳入
        //參數3,傳入待處理意圖
        if(reminderData.noRepeat()) { //if no repeat, alarm time closet future moment that met the input HH;mm
            if(calendar.before(Calendar.getInstance()))
                calendar.add(Calendar.DATE, 1);
            //TODO: unsafe long to int conversion
            intent.putExtra("NotificationId", reminderData.getId()*7);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) reminderData.getId()*7, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            else//legacy support for sdk_api<19
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }else{ //if repeats, alarms every day-of-week
            for (int i = 0; i < reminderData.getRepeat().length; i++) {
                if (!reminderData.getRepeat()[i])
                    continue;
                calendar.set(Calendar.DAY_OF_WEEK, i + 1);
                intent.putExtra("NotificationId", reminderData.getId()*7+i);
                //TODO: unsafe long to int conversion
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) reminderData.getId()*7+i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
            }
        }
    }

    /**
     * I do not recommend calling notification through alarm manager
     * @param reminderData
     */
    @Deprecated
    public void setGeoAlarm(ReminderData reminderData){
        //使用Calendar指定時間
        Calendar calendar = Calendar.getInstance();
        //add delay
        calendar.add(Calendar.SECOND,NOTIFICATION_DELAY);

        //取得AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //建立意圖
        //這裡的 this 是指當前的 Activity
        //AlarmReceiver.class 則是負責接收的 BroadcastReceiver

        // Now create and schedule a new Alarm
        Intent intent = new Intent(context, AlarmReceiver.class); // New component for alarm
        intent.putExtra("ReminderId", reminderData.getId());
        intent.putExtra("ReminderType", reminderData.getReminderType().ordinal());
        intent.putExtra("NotificationId",reminderData.getId()*7);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context, (int) reminderData.getId() * 7, intent, 0);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        else//legacy support for sdk_api<19
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    /**
     * Add a new geofence or update a new geofence
     * return false if pending in queue, return true if it handles the request immediately
     */
    public boolean addGeofence(ReminderData reminder) {
        if(reminder.getReminderType()!= ReminderData.ReminderType.Location)
            throw new IllegalArgumentException("Invalid reminder type.");
        if(!googleApiClient.isConnected()){
            Log.i(TAG, "GOOGLE API Client Not connected");
            pendingGeofenceActions.add(new PendingGeofenceAction(Action.ADD, reminder));
            return false;
        }
        //add/update a new one
        Geofence geoFence = new Geofence.Builder()
                .setRequestId(""+reminder.getId())
                .setCircularRegion(reminder.getLatitude(), reminder.getLongitude(), GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setNotificationResponsiveness(60000)       // set the responsiveness to 1 minute to reduce power consumption
                .build();
        //https://developer.android.com/training/location/geofencing.html
        //https://developers.google.com/android/reference/com/google/android/gms/location/Geofence.Builder.html#public-methods

        try {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            // Add the geofences to be monitored by geofencing service.
            builder.addGeofence(geoFence);
            // Return a GeofencingRequest.
            GeofencingRequest geofencingRequest = builder.build();
            Intent intent = new Intent(context, GeofenceTransitionIntentService.class);
            intent.putExtra("ReminderId", reminder.getId());
            intent.putExtra("ReminderType", reminder.getReminderType().ordinal());
            intent.putExtra("NotificationId", reminder.getId()*7);
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofences().
            PendingIntent pendingIntent = PendingIntent.getService(context, (int)reminder.getId()*7, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    // The GeofenceRequest object.
                    geofencingRequest,
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    pendingIntent
            );
            Log.i(TAG, "Geofence added");
        } catch (SecurityException securityException) {
        }
        return true;
    }

    /**
     * remove only one geofence
     * return false if pending in queue, return true if it handles the request immediately
     * @param id
     */
    public boolean removeGeofence(long id){
        if(!googleApiClient.isConnected()){
            Log.i(TAG, "GOOGLE API Client Not connected");
            pendingGeofenceActions.add(new PendingGeofenceAction(Action.REMOVE, id));
            return false;
        }
        //remove the geo-fence
        Intent intent = new Intent(context, GeofenceTransitionIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, (int) id*7, intent, PendingIntent.FLAG_NO_CREATE);
        if(pendingIntent==null)
            return true;
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                // This is the same pending intent that was used in addGeofences().
                pendingIntent
        );
        return true;
    }

    public void update(){
        //Get all the reminder from the database
        for(ReminderData reminder:getAll()){
            switch (reminder.getReminderType()) {
                case Location:
                    if(reminder.isEnabled() && reminder.getValidUntilInMillis() < System.currentTimeMillis()) {
                        reminder.setEnabled(false);
                        putReminder(reminder);
                    }
                    break;
                case Time:
                    //check if a one time time reminder is a past reminder
                    //note that one time reminder only alarms in the coming 24 hours
                    if(reminder.isEnabled() && reminder.noRepeat() && reminder.getLastModify() < System.currentTimeMillis() + (24*60*60*1000)) {
                        reminder.setEnabled(false);
                        putReminder(reminder);
                    }
                    break;
            }
        }
        Log.i(TAG, "Updated database");
    }

    public void populateAlarms(){
        //Get all the reminder from the database
        for(ReminderData reminder:getAll()){
            if(!reminder.isEnabled())
                continue;
            switch (reminder.getReminderType()) {
                case Location:
                    if(reminder.getLocation()!=null && reminder.getLatitude()!=null && reminder.getLongitude()!=null) {
                        addGeofence(reminder);
                        Log.i(TAG, "Broadcasted geofence for reminder " + reminder.getTitle());
                    }
                    break;
                case Time:
                    if(reminder.getRepeat()!=null && reminder.getTime()!=null) {
                        addAlarm(reminder);
                        Log.i(TAG, "Broadcasted alarm for reminder " + reminder.getTitle());
                    }
                    break;
            }
        }
    }

    @Override
    /**
     * On connected
     */
    public void onResult(@NonNull Status status) {
        for(PendingGeofenceAction pga: pendingGeofenceActions){
            switch(pga.action){
                case ADD:
                    addGeofence(pga.reminder);
                    break;
                case REMOVE:
                    removeGeofence(pga.reminderId);
                    break;
                default:
                    throw new IllegalArgumentException("Missing action");
            }
        }
    }

    public void sample(){
        dataAccessObject.sample();
    }


}
