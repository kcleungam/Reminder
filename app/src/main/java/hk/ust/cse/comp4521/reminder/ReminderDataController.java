package hk.ust.cse.comp4521.reminder;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Jeffrey on 17/5/2016.
 */
public class ReminderDataController implements ResultCallback<Status> {

    public final static String TAG = "ReminderDataController";

    private static ReminderDataController instance;
    private Context context;
    private ReminderDAO reminderDAO;
    public static final int NOTIFICATION_DELAY=10;
    public static final int TIME_ALARM_NOTIFY_BEFORE = 10; //seconds

    protected GoogleApiClient mGoogleApiClient;
    private final static float GEOFENCE_RADIUS=500;//radius in meters

    //singleton pattern, no public constructor
    private ReminderDataController(){

    }

    public static ReminderDataController getInstance(Context application){
        if(instance==null) {
            if(application==null)
                throw new IllegalArgumentException("context is null");
            instance = new ReminderDataController();
            instance.reminderDAO = new ReminderDAO(application);
            instance.context = application;
            // 如果資料庫是空的，就建立一些範例資料
            // 這是為了方便測試用的，完成應用程式以後可以拿掉
        }
        return instance;
    }

    public ReminderData getReminder(long reminderId){
        return reminderDAO.get(reminderId);
    }

    public ArrayList<ReminderData> getAll(){
        return reminderDAO.getAll();
    }

    public void clear(){
        reminderDAO.clear();
    }

    public int getCount(){
        return reminderDAO.getCount();
    }

    public ReminderData addReminder(ReminderData reminderData){
        reminderDAO.insert(reminderData);
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
        return reminderData;
    }

    public boolean putReminder(ReminderData reminderData){
        if(reminderDAO.update(reminderData)) {
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
        if(reminderDAO.delete(id)){
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
        if(reminderDAO.delete(reminderData.getId())){
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
        if(enable) {
            ReminderData reminderData = getReminder(reminderId);
            removeAlarm(reminderId);
            addAlarm(reminderData);
        }else{
            removeAlarm(reminderId);
        }
    }

    private boolean removeAlarm(long reminderId){
        //取得AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        // find the old PendingIntent
        boolean hasPendingIntent = false;
        for(int i=0; i<7; i++) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) reminderId*7+i, intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                // Now cancel the alarm that matches the old PendingIntent
                alarmManager.cancel(pendingIntent);
                hasPendingIntent = true;
            }
        }
        return hasPendingIntent;
    }

    @SuppressLint("NewApi")
    private void addAlarm(ReminderData reminderData){
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
    @SuppressLint("NewApi")
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
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,(int) reminderData.getId()*7,intent,0);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        else//legacy support for sdk_api<19
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    /**
     * Add a new geofence or update a new geofence
     */
    public void addGeofence(ReminderData reminderData) {
        //TODO: Could someone prove this is working?
        //check whether this id already exists
//        boolean exist=false;
//        for(Geofence geofence:mGeofenceList){
//            if(geofence.getRequestId().equals(id)){
//                exist=true;
//                break;
//            }
//        }
//        if(exist) {
//            //update the existing one
//            removeGeofence(id);
//        }
        //add/update a new one
        Geofence geoFence = new Geofence.Builder()
                .setRequestId(""+reminderData.getId())
                .setCircularRegion(reminderData.getLatitude(), reminderData.getLongitude(), GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();


        if (!mGoogleApiClient.isConnected()) {
            Log.i(TAG, "GOOGLE API Client Not connected");
            return;
        }

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
            intent.putExtra("ReminderId", reminderData.getId());
            intent.putExtra("ReminderType", reminderData.getReminderType().ordinal());
            intent.putExtra("NotificationId", reminderData.getId()*7);
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofences().
            PendingIntent pendingIntent = PendingIntent.getService(context, (int)reminderData.getId()*7, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    geofencingRequest,
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    pendingIntent
            ).setResultCallback(this); // Result processed in onResult().

            Log.i(TAG, "Geofence added");
        } catch (SecurityException securityException) {

        }
    }

    /**
     * remove only one geofence
     * @param id
     */
    public boolean removeGeofence(long id){
        if(!mGoogleApiClient.isConnected()){
            Log.i(TAG,"Cannot connect to Google Service");
            return false;
        }
        //remove the geo-fence
        //TODO: Could anyone prove this is working?
//        for(Geofence geofence:mGeofenceList){
//            if(geofence.getRequestId().equals(id)){
//                mGeofenceList.remove(geofence);
//                break;
//            }
//        }
        //TODO: Or I will try using my own way to implement.
        Intent intent = new Intent(context, GeofenceTransitionIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, (int) id*7, intent, PendingIntent.FLAG_NO_CREATE);
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                // This is the same pending intent that was used in addGeofences().
                pendingIntent
        ).setResultCallback(this); // Result processed in onResult().
        return true;
    }

    public void populateGeofenceList(){
        //Get all the reminder from the database
        for(ReminderData reminderData:getAll()){
            //validate the location
            if(reminderData.getReminderType()== ReminderData.ReminderType.Location
                    && reminderData.getLocation()!=null && !reminderData.getLocation().equals("")
                    && reminderData.getLatitude()!=null && reminderData.getLongitude()!=null
                    && reminderData.isEnabled()){
                addGeofence(reminderData);
            }
        }
    }

    @Override
    public void onResult(Status status) {

    }

    public void sample(){
        reminderDAO.sample();
    }
}
