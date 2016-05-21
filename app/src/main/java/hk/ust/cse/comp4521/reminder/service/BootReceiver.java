package hk.ust.cse.comp4521.reminder.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Jeffrey on 21/5/2016.
 */
public class BootReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent("hk.ust.cse.comp4521.reminder.service.DataUpdateIntentService");
        i.setClass(context, DataUpdateIntentService.class);
        context.startService(i);
    }
}
