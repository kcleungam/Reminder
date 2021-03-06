//      Comp 4521
//      Leung Ka Chun       20125844        kcleungam@ust.hk
//      To Wun Yin            20112524        wytoaa@ust.hk
//      Leung Chun Fai      20113619        cfleungac@ust.hk

package hk.ust.cse.comp4521.reminder.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import hk.ust.cse.comp4521.reminder.R;
import hk.ust.cse.comp4521.reminder.data.ReminderData;
import hk.ust.cse.comp4521.reminder.view.MainActivity;
import hk.ust.cse.comp4521.reminder.view.ViewLocationActivity;
import hk.ust.cse.comp4521.reminder.view.ViewTimeActivity;

/**
 * Created by Jeffrey on 21/5/2016.
 */
public class NotificationProvider {

    public static final String TAG = "NotificationProvider";

    public static Notification getNotifiction(Context context, ReminderData reminderData){
        //建立通知物件
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
        //指定通知欄位要顯示的圖示
        switch(reminderData.getReminderType()){
            case Time:
                notification.setSmallIcon(R.drawable.cast_ic_notification_0);
                break;
            case Location:
                notification.setSmallIcon(R.drawable.pink_stick_man)
                        // In a real app, you may want to use a library like Volley
                        // to decode the Bitmap.
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.pink_stick_man));
                break;
            default:
                Log.d(TAG,"Reminder type not on the list.");
        }
        //指定通知標題
        notification.setContentTitle(reminderData.getTitle());
        //通知內容
        notification.setContentText(reminderData.getDescription());
        //指定通知出現時要顯示的文字,幾秒後會消失只剩圖示
        notification.setTicker(reminderData.getTitle());
        //何時送出通知,傳入當前時間則立即發出通知
        notification.setWhen(System.currentTimeMillis());
        //會有通知預設的鈴聲、振動、light
        notification.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        notification.setDefaults(Notification.DEFAULT_ALL);
        //非必要,會在通知圖示旁顯示數字
        //notification.setNumber(5);

        //當使用者按下通知欄中的通知時要開啟的 Activity
        Intent notificationIntent = new Intent();
        switch(reminderData.getReminderType()){
            case Time:
                notificationIntent.setClass(context, ViewTimeActivity.class);
                break;
            case Location:
                notificationIntent.setClass(context, ViewLocationActivity.class);
                break;
            default:
                Log.d(TAG,"Reminder type not on the list.");
        }

        //非必要,可以利用intent傳值
        notificationIntent.putExtra("ReminderId", reminderData.getId());

        // Construct a task stack.
        android.app.TaskStackBuilder stackBuilder = android.app.TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        //TODO: unsafe long to int conversion
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent((int) reminderData.getId(), PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setContentIntent(notificationPendingIntent);

        return notification.build();
    }
}
