package hk.ust.cse.comp4521.reminder.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import hk.ust.cse.comp4521.reminder.R;
import hk.ust.cse.comp4521.reminder.data.DataController;
import hk.ust.cse.comp4521.reminder.data.ReminderData;
import hk.ust.cse.comp4521.reminder.view.MainActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GeofenceTransitionIntentService extends IntentService {
    protected static final String TAG = "GeofenceTransitionIntentService";

    DataController dataController;

    public GeofenceTransitionIntentService() {
        super("GeofenceTransitionIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataController = DataController.getInstance(getApplication());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Toast.makeText(getApplicationContext(), "Geofence has some error", Toast.LENGTH_SHORT).show();
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            //TODO: this may be buggy
            for(Geofence geofence:triggeringGeofences) {
                dataController = DataController.getInstance(getApplication());
                ReminderData reminderData = dataController.getReminder(intent.getLongExtra("ReminderId", -1));
                long notificationId = intent.getLongExtra("NotificationId", -1);

                if(reminderData==null)
                    return;

                //取得通知管理器
                NotificationManager mNotificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
                //執行通知
                Notification notification = NotificationProvider.getNotifiction(getApplication(), reminderData);
                //TODO: unsafe long to int conversion
                mNotificationManager.notify((int) notificationId, notification);

                if(reminderData.noRepeat()) {
                    reminderData.setEnabled(false);
                    dataController.putReminder(reminderData);
                }
            }
        }
    }

    @Deprecated
    private void sendNotification(ReminderData reminderData){
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        Notification.Builder builder = new Notification.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.pink_stick_man)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.pink_stick_man))
                .setContentTitle("Sofa King Good")
                .setContentText("HIHI")
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());

//        //TODO: this may be buggy and not efficient
//        //as Android M doesn't support lambda expression but will support in Android N
//        DataController mController=DataController.getInstance();
//        for(ReminderData reminderData:MainActivity.reminderAdaptor.reminderList){
//            if(String.valueOf(reminderData.getId()).equals(geofenceID)){
//                mController.setGeoAlarm(reminderData);
//            }
//        }
    }

    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }


    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Exit";
            default:
                return "Unknown";
        }
    }

}
