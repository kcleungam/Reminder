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

public class MainActivity extends AppCompatActivity {
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
    protected GoogleApiClient mGoogleApiClient;

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
        GoogleApiClientProvider.getInstance(getApplication());//kick off the request to build GooogleApiClient

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

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     *
     *      https://www.youtube.com/watch?v=cyk_ht8z6IA
     *      https://www.youtube.com/watch?v=DzpwvZ4S27g
     *
     */

}
