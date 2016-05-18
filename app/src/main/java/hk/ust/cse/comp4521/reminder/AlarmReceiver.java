package hk.ust.cse.comp4521.reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Jeffrey on 16/5/2016.
 */
public class AlarmReceiver extends BroadcastReceiver {

    ReminderDataController dataController;

    @Override
    public void onReceive(Context context, Intent intent) {
        dataController = ReminderDataController.getInstance(context);
        ReminderData reminderData = dataController.getReminder(intent.getLongExtra("ReminderId", -1));
        long notificationId = intent.getLongExtra("NotificationId", -1);
        Log.i("AlarmReceiver", ""+notificationId);

        //建立通知物件
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
        //指定通知欄位要顯示的圖示
        notification.setSmallIcon(R.drawable.cast_ic_notification_0);
        //指定通知標題
        notification.setContentTitle(reminderData.getTitle());
        //通知內容
        notification.setContentText(reminderData.getDescription());
        //指定通知出現時要顯示的文字,幾秒後會消失只剩圖示
        notification.setTicker(reminderData.getTitle());
        //何時送出通知,傳入當前時間則立即發出通知
        notification.setWhen(System.currentTimeMillis());
        //會有通知預設的鈴聲、振動、light
        notification.setDefaults(Notification.DEFAULT_ALL);
        //非必要,會在通知圖示旁顯示數字
        //notification.setNumber(5);

        //當使用者按下通知欄中的通知時要開啟的 Activity
        Intent resultIntent = new Intent();
        switch(reminderData.getReminderType()){
        case Time:
            resultIntent.setClass(context, ViewTimeActivity.class);
            break;
        case Location:
            resultIntent.setClass(context, ViewLocationActivity.class);
            break;
        default:
            Log.d("Alarm","Reminder type not on the list.");
        }
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        //非必要,可以利用intent傳值
        resultIntent.putExtra("ReminderId", reminderData.getId());
        //建立待處理意圖
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        switch(reminderData.getReminderType()){
//            case Time:
//                stackBuilder.addParentStack(MainActivity.class);
//                break;
//            case Location:
//
//        }
//        stackBuilder.addNextIntent(resultIntent);
//        //TODO: unsafe long to int conversion
//        PendingIntent pendingResultIntent = stackBuilder.getPendingIntent((int) reminderData.getId(), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingResultIntent = PendingIntent.getActivity(context, (int) reminderData.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingResultIntent);
        //取得通知管理器
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //執行通知
        //TODO: unsafe long to int conversion
        mNotificationManager.notify(1, notification.build());

        if(reminderData.noRepeat()) {
            reminderData.setEnabled(false);
            dataController.putReminder(reminderData);
        }
    }
}
