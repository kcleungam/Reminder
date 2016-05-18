package hk.ust.cse.comp4521.reminder;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Jeffrey on 17/5/2016.
 */
public class ReminderDataController {

    private static ReminderDataController instance;
    private static Context context;
    private ReminderDAO reminderDAO;
    public static final int NOTIFICATION_DELAY=10;

    //singleton pattern, no public constructor
    private ReminderDataController(){

    }

    public static ReminderDataController getInstance(){
        if(instance==null) {
            if(context==null)
                throw new IllegalArgumentException("Call setContext(getApplicationContext) in your onCreate before using getInstance().");
            instance = new ReminderDataController();
            instance.reminderDAO = new ReminderDAO(context);
            // 如果資料庫是空的，就建立一些範例資料
            // 這是為了方便測試用的，完成應用程式以後可以拿掉
        }
        return instance;
    }

    public static void setContext(Context context){
        ReminderDataController.context = context;
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
            if (reminderData.isEnabled()) {
                switch (reminderData.getReminderType()) {
                    case Location:
                        break;
                    case Time:
                        setAlarm(reminderData);
                        break;
                }
            }
        }
        return reminderData;
    }

    public boolean putReminder(ReminderData reminderData){
        if(reminderDAO.update(reminderData)) {
            deleteAlarm(reminderData.getId());
            if(reminderData.isEnabled()) {
                switch (reminderData.getReminderType()) {
                    case Location:
                        break;
                    case Time:
                        setAlarm(reminderData);
                        break;
                }
            }
            return true;
        }else
            return false;
    }

    public boolean deleteReminder(long reminderId){
        if(reminderDAO.delete(reminderId)){
            deleteAlarm(reminderId);
            return true;
        }else
            return false;
    }

    public void enableReminder(long reminderId, boolean enable){
        if(enable) {
            ReminderData reminderData = getReminder(reminderId);
            setAlarm(reminderData);
        }else{
            deleteAlarm(reminderId);
        }
    }

    private boolean deleteAlarm(long reminderId){
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

    @TargetApi(19)
    private void setAlarm(ReminderData reminderData){
        //使用Calendar指定時間
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, reminderData.getTime(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, reminderData.getTime(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);

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
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) reminderData.getId()*7, intent, 0);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }else{ //if repeats, alarms every day-of-week
            for (int i = 0; i < reminderData.getRepeat().length; i++) {
                if (!reminderData.getRepeat()[i])
                    continue;
                calendar.set(Calendar.DAY_OF_WEEK, i + 1);
                //TODO: unsafe long to int conversion
                intent.putExtra("NotificationId", reminderData.getId()*7+i);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) reminderData.getId()*7+i, intent, 0);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
            }
        }
    }

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
        //TODO: are these lines below correct?
        intent.putExtra("NotificationID",reminderData.getId()*7);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,(int) reminderData.getId()*7,intent,0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
    }

    public void sample(){
        reminderDAO.sample();
    }
}
