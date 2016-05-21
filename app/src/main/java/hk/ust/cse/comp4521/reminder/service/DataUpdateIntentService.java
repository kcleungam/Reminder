package hk.ust.cse.comp4521.reminder.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import hk.ust.cse.comp4521.reminder.data.DataController;

/**
 * Created by Jeffrey on 21/5/2016.
 */
public class DataUpdateIntentService extends IntentService{

    DataController dataController;

    public static final String TAG = "DataUpdateIntentService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public DataUpdateIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataController = DataController.getInstance(getApplication());
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        dataController.update();
        dataController.populateAlarms();
    }
}
