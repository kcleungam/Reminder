package hk.ust.cse.comp4521.reminder;

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

    private ReminderDataController(){

    }

    public static ReminderDataController getInstance(){
        if(instance==null) {
            instance = new ReminderDataController();
            instance.reminderDAO = new ReminderDAO(context);
        }
        return instance;
    }

    public static void setContext(Context context){
        ReminderDataController.context = context;
    }

    public boolean putReminder(ReminderData reminderData){
        if(reminderDAO.update(reminderData)) {
            //deleteAlarm(reminderData.getId());
            setAlarm(reminderData);
            return true;
        }else
            return false;
    }

    public ReminderData getReminder(long reminderId){
        return reminderDAO.get(reminderId);
    }

    public ArrayList<ReminderData> getAll(){
        return reminderDAO.getAll();
    }

    public int getCount(){
        return reminderDAO.getCount();
    }

    public ReminderData addReminder(ReminderData reminderData){
        return reminderDAO.insert(reminderData);
    }

    public boolean deleteReminder(long reminderId){
        return reminderDAO.delete(reminderId);
    }

    private boolean deleteAlarm(long reminderId){
        //取得AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE); // find the old PendingIntent
        if (pendingIntent != null) {
            // Now cancel the alarm that matches the old PendingIntent
            alarmManager.cancel(pendingIntent);
            return true;
        }
        return false;
    }

    private void setAlarm(ReminderData reminderData){
        //使用Calendar指定時間
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, DateTimeParser.toHour(reminderData.getTimeInMillis()));
        calendar.set(Calendar.MINUTE, DateTimeParser.toMin(reminderData.getTimeInMillis()));

        //取得AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //建立意圖
        //這裡的 this 是指當前的 Activity
        //AlarmReceiver.class 則是負責接收的 BroadcastReceiver

        // Now create and schedule a new Alarm
        Intent intent = new Intent(context, AlarmReceiver.class); // New component for alarm
        intent.putExtra("ReminderId", reminderData.getId());
        //TODO: unsafe long to int conversion
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) reminderData.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

        //設定一個警報
        //參數1,我們選擇一個會在指定時間喚醒裝置的警報類型
        //參數2,將指定的時間以millisecond傳入
        //參數3,傳入待處理意圖
        if(reminderData.noRepeat()) { //if no repeat, alarm time closet future moment that met the input HH;mm
            if(calendar.before(Calendar.getInstance()))
                calendar.add(Calendar.DATE, 1);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }else{ //if repeats, alarms every day-of-week
            for (int i = 0; i < reminderData.getRepeat().length; i++) {
                if (!reminderData.getRepeat()[i])
                    continue;
                calendar.set(Calendar.DAY_OF_WEEK, i + 1);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
            }
        }
    }

    public void sample(){
        reminderDAO.sample();
    }
}
