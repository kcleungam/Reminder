//      Comp 4521
//      Leung Ka Chun       20125844        kcleungam@ust.hk
//      To Wun Yin            20112524        wytoaa@ust.hk
//      Leung Chun Fai      20113619        cfleungac@ust.hk

package hk.ust.cse.comp4521.reminder.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import hk.ust.cse.comp4521.reminder.data.DataController;
import hk.ust.cse.comp4521.reminder.data.ReminderData;

/**
 * Created by Jeffrey on 16/5/2016.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "AlarmReceiver";

    DataController dataController;

    @Override
    public void onReceive(Context context, Intent intent) {
        dataController = DataController.getInstance(context);
        ReminderData reminderData = dataController.getReminder(intent.getLongExtra("ReminderId", -1));
        long notificationId = intent.getLongExtra("NotificationId", -1);
        Log.i("AlarmReceiver", ""+notificationId);

        if(reminderData==null)
            return;

        //取得通知管理器
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //執行通知
        Notification notification = NotificationProvider.getNotifiction(context, reminderData);
        //TODO: unsafe long to int conversion
        mNotificationManager.notify((int) notificationId, notification);

        if(reminderData.noRepeat()) {
            reminderData.setEnabled(false);
            dataController.putReminder(reminderData);
        }
    }
}
