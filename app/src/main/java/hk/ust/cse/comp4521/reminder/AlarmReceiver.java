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

    public static final String TAG = "AlarmReceiver";

    ReminderDataController dataController;

    @Override
    public void onReceive(Context context, Intent intent) {
        dataController = ReminderDataController.getInstance(context);
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
