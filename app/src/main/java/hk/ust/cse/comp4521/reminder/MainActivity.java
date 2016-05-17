package hk.ust.cse.comp4521.reminder;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    ListView reminderList;
    ReminderDataAdapter reminderAdaptor;

    protected LocationRequest mLocationRequest;
    private int UPDATE_INTERVAL_IN_MILLISECONDS = 200000;   //20 second update once
    private int FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 100000;
    protected GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList;
    private boolean mGeofencesAdded = false;
    private PendingIntent mGeofencePendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        reminderList = (ListView)findViewById(R.id.reminder_list);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
//        CoordinatorLayout.LayoutParams params =
//                (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
//        params.setBehavior(new FabHideOnScroll());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Fab clicked", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), TimeReminderActivity.class);
                startActivity(intent);
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TimeReminderActivity.class);
                long id = ( (ReminderDataAdapter.RowHandler) v.getTag() ).reminderId;
                intent.putExtra("ReminderDataId", id);
                startActivity(intent);
            }
        };
        View.OnLongClickListener onLongClickListener = new AdapterView.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), ( (ReminderDataAdapter.RowHandler)v.getTag() ).titleView.getText() , Toast.LENGTH_SHORT).show();
                registerForContextMenu(v);
                openContextMenu(v);
                unregisterForContextMenu(v);
                return true;
            }
        };
        reminderAdaptor = new ReminderDataAdapter(getApplicationContext(), R.layout.row_layout, onClickListener, onLongClickListener);

        // 建立資料庫物件
        ReminderDAO reminderDAO = new ReminderDAO(getApplicationContext());
        // 如果資料庫是空的，就建立一些範例資料
        // 這是為了方便測試用的，完成應用程式以後可以拿掉
        if (reminderDAO.getCount() == 0) {
            reminderDAO.sample();
        }
        // 取得所有記事資料
        ArrayList<ReminderData> reminders = reminderDAO.getAll();
        for(ReminderData sample:reminders){
            reminderAdaptor.addItem(sample);
        }
        reminderList.setAdapter(reminderAdaptor);
//        for(int i = 0; i < reminderList.getChildCount(); i++){
//            ReminderDataAdapter.RowHandler rowHandler = ((ReminderDataAdapter.RowHandler) reminderList.getChildAt(i).getTag());
//            ReminderData reminderData = reminderDAO.get(rowHandler.reminderId);
//            String id = Long.toString( reminderData.getId());
//            double latitude = reminderData.getLattitude();
//            double longitude = reminderData.getLongitude();
//        }

        mGeofenceList = new ArrayList<Geofence>();

        buildGoogleApiClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ReminderDAO reminderDAO = new ReminderDAO(getApplicationContext());
        reminderAdaptor.clear();
        ArrayList<ReminderData> reminders = reminderDAO.getAll();
        for(ReminderData sample:reminders){
            reminderAdaptor.addItem(sample);
        }
        reminderAdaptor.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getTag() instanceof ReminderDataAdapter.RowHandler) {
            ReminderDataAdapter.RowHandler handler = (ReminderDataAdapter.RowHandler) v.getTag();
            menu.setHeaderTitle(handler.titleView.getText());
            menu.add(Menu.NONE, 0, 0, "Delete");
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals("Delete")){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            ReminderDAO reminderDAO = new ReminderDAO(getApplicationContext());
            ReminderDataAdapter.RowHandler handler = (ReminderDataAdapter.RowHandler) info.targetView.getTag();
            reminderDAO.delete(handler.reminderId);
            Toast.makeText(MainActivity.this, "Reminder "+handler.titleView.getText()+" deleted.", Toast.LENGTH_SHORT).show();
            reminderAdaptor.notifyDataSetChanged();
        }
        return true;
    }




    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    private void addGeoFence(String id, double latitude, double longitude) {
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(latitude, longitude, 50)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(getApplicationContext(), "GOOGLE API Client Not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
            mGeofencesAdded = true;

            Toast.makeText(getApplicationContext(), "Geofence added", Toast.LENGTH_SHORT).show();
        } catch (SecurityException securityException) {

        }
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this,"On connected", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        addGeoFence("123", 22.337398, 114.259114);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        Toast.makeText(this,"create location request", Toast.LENGTH_SHORT).show();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    @Override
    public void onResult(Status status) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     *
     *      https://www.youtube.com/watch?v=cyk_ht8z6IA
     *      https://www.youtube.com/watch?v=DzpwvZ4S27g
     *
     */

}
