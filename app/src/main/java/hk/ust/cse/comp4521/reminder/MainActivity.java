package hk.ust.cse.comp4521.reminder;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    /* View component */
    ListView reminderList;
    //public static ReminderDataAdapter reminderAdaptor;
    //ReminderDataAdapter.RowHandler rowOnSelected;
    public static RecyclerView recyclerView;
    public static RecyclerAdapter recyclerAdapter;

    /* FAB animation */
    private boolean fabMenuShown = false;
    private int[] fabMenuItems = {R.id.timeReminderFab, R.id.fab_2};
    private double[][] fabMenuOffsetRatio = {{1.7, 0.25}, {1.5, 1.5}};
    private int[] fabMenuShowAnimation = {R.anim.fab1_show, R.anim.fab2_show};
    private int[] fabMenuHideAnimation = {R.anim.fab1_hide, R.anim.fab2_hide};

    /* Location */
    protected LocationRequest mLocationRequest;
    private final static int UPDATE_INTERVAL_IN_MILLISECONDS = 200000;   //20 second update once
    private final static int FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 100000;
    private final static float GEOFENCE_RADIUS=500;//radius in meters
    protected GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList;
    private boolean mGeofencesAdded = false;
    private PendingIntent mGeofencePendingIntent;

    /* Database */
    private ReminderDataController dataController;

    /* Others */
    public static final String TAG="Main Activity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataController = ReminderDataController.getInstance(getApplication()); // 建立資料庫物件

       //TODO: remove the following tow lines after your first run
       /*dataController.clear();
       dataController.sample();*/

        /*setContentView(R.layout.new_activity_main);
        reminderList = (ListView)findViewById(R.id.reminder_list);*/
        setContentView(R.layout.main_activity);
        recyclerView=(RecyclerView)findViewById(R.id.reminder_recycler_view);

        recyclerAdapter=new RecyclerAdapter(this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton menuFab = (FloatingActionButton)findViewById(R.id.menuFab);
        menuFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fabMenuShown)
                    hideFabMenu();
                else
                    showFabMenu();
            }
        });
        FloatingActionButton timeReminderFab = (FloatingActionButton)findViewById(R.id.timeReminderFab);
        timeReminderFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TimeReminderActivity.class);
                startActivity(intent);
                hideFabMenu();
            }
        });
        FloatingActionButton locationReminderFab=(FloatingActionButton)findViewById(R.id.fab_2);
        locationReminderFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), LocationReminderActivity.class);
                startActivity(intent);
                hideFabMenu();
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id = ( (ReminderDataAdapter.RowHandler) v.getTag() ).reminderId;
                ReminderData.ReminderType reminderType = ( (ReminderDataAdapter.RowHandler) v.getTag() ).reminderType;
                Intent intent = null;
                switch(reminderType){
                    case Location:
                        intent = new Intent(getApplicationContext(), LocationReminderActivity.class);
                        break;
                    case Time:
                        intent = new Intent(getApplicationContext(), TimeReminderActivity.class);
                        break;
                }
                intent.putExtra("ReminderId", id);
                startActivity(intent);
            }
        };
        View.OnLongClickListener onLongClickListener = new AdapterView.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //rowOnSelected = (ReminderDataAdapter.RowHandler) v.getTag();
                openContextMenu(recyclerView);
                return true;
            }
        };
        //reminderAdaptor = new ReminderDataAdapter(getApplication(), R.layout.row_layout, onClickListener, onLongClickListener);

        // 取得所有記事資料
        //TODO
        /*ArrayList<ReminderData> reminders = dataController.getAll();
        for(ReminderData sample:reminders){
            reminderAdaptor.addItem(sample);
        }*/
        //reminderList.setAdapter(reminderAdaptor);

//        for(int i = 0; i < reminderList.getChildCount(); i++){
//            ReminderDataAdapter.RowHandler rowHandler = ((ReminderDataAdapter.RowHandler) reminderList.getChildAt(i).getTag());
//            ReminderData reminderData = reminderDAO.get(rowHandler.reminderId);
//            String id = Long.toString( reminderData.getId());
//            double latitude = reminderData.getLatitude();
//            double longitude = reminderData.getLongitude();
//        }

        /*
         *  initialisation for location-triggered event
         */
        mGeofenceList = new ArrayList<Geofence>();//empty list for storing geo-fences
        mGeofencePendingIntent=null;//Initially set the PendingIntent used in addGeofences() and removeGeofences() to null
        buildGoogleApiClient();//kick off the request to build GooogleApiClient


        //registerForContextMenu(reminderList);

//        CoordinatorLayout.LayoutParams params =
//                (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
//        params.setBehavior(new FabHideOnScroll());

        //make the card view swipe-able
        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(recyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipeLeft(int position) {
                                return true;
                            }

                            @Override
                            public boolean canSwipeRight(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                //remove it
                                recyclerAdapter.removeByPosition(reverseSortedPositions[0]);
                                recyclerAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                //remove it
                                recyclerAdapter.removeByPosition(reverseSortedPositions[0]);
                                recyclerAdapter.notifyDataSetChanged();
                            }
                        });

        recyclerView.addOnItemTouchListener(swipeTouchListener);
    }

    private void showFabMenu(){
        fabMenuShown = true;
        for(int i=0; i<fabMenuItems.length; i++){
            FloatingActionButton fab = (FloatingActionButton) findViewById(fabMenuItems[i]);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fab.getLayoutParams();
            layoutParams.rightMargin += (int) (fab.getWidth() * fabMenuOffsetRatio[i][0]);
            layoutParams.bottomMargin += (int) (fab.getHeight() * fabMenuOffsetRatio[i][1]);
            fab.setLayoutParams(layoutParams);
            Animation show_fab = AnimationUtils.loadAnimation(getApplication(), fabMenuShowAnimation[i]);
            fab.startAnimation(show_fab);
            fab.setClickable(true);
        }
    }

    private void hideFabMenu(){
        fabMenuShown = false;
        for(int i=0; i<fabMenuItems.length; i++){
            FloatingActionButton fab = (FloatingActionButton) findViewById(fabMenuItems[i]);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fab.getLayoutParams();
            layoutParams.rightMargin -= (int) (fab.getWidth() * fabMenuOffsetRatio[i][0]);
            layoutParams.bottomMargin -= (int) (fab.getHeight() * fabMenuOffsetRatio[i][1]);
            fab.setLayoutParams(layoutParams);
            Animation hide_fab = AnimationUtils.loadAnimation(getApplication(), fabMenuHideAnimation[i]);
            fab.startAnimation(hide_fab);
            fab.setClickable(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        recyclerAdapter.notifyDataSetChanged();//this needs other parts in Adapter to work successfully
        //otherwise, it may be no changed or duplicate the cards
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
        if (v instanceof ListView) {
            /*menu.setHeaderTitle(rowOnSelected.titleView.getText());
            menu.add(Menu.NONE, 0, 0, "Delete");*/
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals("Delete")){
            //TODO
            /*dataController.deleteReminder(rowOnSelected.reminderId);
            Toast.makeText(MainActivity.this, "Reminder "+rowOnSelected.titleView.getText()+" deleted.", Toast.LENGTH_SHORT).show();
            reminderAdaptor.clear();
            for(ReminderData reminderData:dataController.getAll())
                reminderAdaptor.add(reminderData);
            reminderAdaptor.notifyDataSetChanged();*/
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

    /**
     * Add a new geofence or update a new geofence
     * @param id
     * @param latitude
     * @param longitude
     */
    public void addGeofence(String id, double latitude, double longitude) {
        //TODO: This may be buggy
        //check whether this id already exists
        boolean exist=false;
        for(Geofence geofence:mGeofenceList){
            if(geofence.getRequestId().equals(id)){
                exist=true;
                break;
            }
        }
        if(exist) {
            //update the existing one
            removeGeofence(id);
        }
        //add/update a new one
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS)
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

    /**
     * remove only one geofence
     * @param id
     */
    public void removeGeofence(String id){
        if(!mGoogleApiClient.isConnected()){
            Toast.makeText(this,"Cannot connect to Google Service",Toast.LENGTH_SHORT).show();
            return;
        }
        //remove the geo-fence
        for(Geofence geofence:mGeofenceList){
            if(geofence.getRequestId().equals(id)){
                mGeofenceList.remove(geofence);
                break;
            }
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
        populateGeofenceList();//get the geo-fences used
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    public void populateGeofenceList(){
        //Get all the reminder from the database
        for(ReminderData reminderData:dataController.getAll()){
            //validate the location
            if(reminderData.getReminderType()== ReminderData.ReminderType.Location && reminderData.getLocation()!=null && !reminderData.getLocation().equals("") && reminderData.getLatitude()!=null && reminderData.getLongitude()!=null){
                addGeofence(String.valueOf(reminderData.getId()), reminderData.getLatitude(), reminderData.getLongitude());
            }
        }
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
