package hk.ust.cse.comp4521.reminder.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import hk.ust.cse.comp4521.reminder.service.GoogleApiClientProvider;
import hk.ust.cse.comp4521.reminder.R;
import hk.ust.cse.comp4521.reminder.ReminderDataAdapter;
import hk.ust.cse.comp4521.reminder.data.ReminderData;

public class MainActivity extends AppCompatActivity implements ResultCallback<Status>, ActivityCompat.OnRequestPermissionsResultCallback{
    /* View component */
    //ListView reminderList;
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

    /* Others */
    public static final String TAG="Main Activity";

    /* Permission requests */
    private static final String[] PERMISSION_REQUESTS={Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int ACCESS_TIME_REMINDER_ACTIVITY=0,ACCESS_LOCATION_REMINDER_ACTIVITY=1;
    private static boolean canUse=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleApiClientProvider.getInstance(getApplication(), this); //kick off the request to build GooogleApiClient

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
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if(isAllPermissionsGranted()){
                    Intent intent = new Intent(getApplicationContext(), TimeReminderActivity.class);
                    startActivity(intent);
                    hideFabMenu();
                }else{
                    //ask for permissions
                    checkAndRequestPermissions(ACCESS_TIME_REMINDER_ACTIVITY);
                }
            }
        });
        FloatingActionButton locationReminderFab=(FloatingActionButton)findViewById(R.id.fab_2);
        locationReminderFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAllPermissionsGranted()){
                    Intent intent=new Intent(getApplicationContext(), LocationReminderActivity.class);
                    startActivity(intent);
                    hideFabMenu();
                }else{
                    //ask for permissions
                    checkAndRequestPermissions(ACCESS_LOCATION_REMINDER_ACTIVITY);
                }
            }
        });

        isAllPermissionsGranted();

//        View.OnClickListener onClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                long id = ( (ReminderDataAdapter.RowHandler) v.getTag() ).reminderId;
//                ReminderData.ReminderType reminderType = ( (ReminderDataAdapter.RowHandler) v.getTag() ).reminderType;
//                Intent intent = null;
//                switch(reminderType){
//                    case Location:
//                        intent = new Intent(getApplicationContext(), LocationReminderActivity.class);
//                        break;
//                    case Time:
//                        intent = new Intent(getApplicationContext(), TimeReminderActivity.class);
//                        break;
//                }
//                intent.putExtra("ReminderId", id);
//                startActivity(intent);
//            }
//        };
//        View.OnLongClickListener onLongClickListener = new AdapterView.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                //rowOnSelected = (ReminderDataAdapter.RowHandler) v.getTag();
//                openContextMenu(recyclerView);
//                return true;
//            }
//        };
        //reminderAdaptor = new ReminderDataAdapter(getApplication(), R.layout.row_layout, onClickListener, onLongClickListener);

        // 取得所有記事資料
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
                                if(canUse) {
                                    //remove it
                                    recyclerAdapter.remove(reverseSortedPositions[0]);
                                    recyclerAdapter.notifyDataSetChanged();
                                }else{
                                    Toast.makeText(recyclerView.getContext(),"Please grant the permission first.",Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                if(canUse) {
                                    //remove it
                                    recyclerAdapter.remove(reverseSortedPositions[0]);
                                    recyclerAdapter.notifyDataSetChanged();
                                }else{
                                    Toast.makeText(recyclerView.getContext(),"Please grant the permission first.",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

        recyclerView.addOnItemTouchListener(swipeTouchListener);
    }

    public boolean canUse(){return isAllPermissionsGranted();}

    private boolean isAllPermissionsGranted(){
        for(String request:PERMISSION_REQUESTS){
            if(ContextCompat.checkSelfPermission(this,request)!=PackageManager.PERMISSION_GRANTED) {
                canUse=false;
                return false;
            }
        }
        canUse=true;
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermissions(int code) {
        if(!isAllPermissionsGranted()){
            ActivityCompat.requestPermissions(this,PERMISSION_REQUESTS,code);
        }
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
        recyclerAdapter.reset();
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

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        if(item.getTitle().equals("Delete")){
//            dataController.deleteReminder(rowOnSelected.reminderId);
//            Toast.makeText(MainActivity.this, "Reminder "+rowOnSelected.titleView.getText()+" deleted.", Toast.LENGTH_SHORT).show();
//            reminderAdaptor.clear();
//            for(ReminderData reminderData:dataController.getAll())
//                reminderAdaptor.add(reminderData);
//            reminderAdaptor.notifyDataSetChanged();
//        }
//        return true;
//    }

//    @Override
//    protected void onStop() {
//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
//        super.onStop();
//    }

    @Override
    public void onResult(@NonNull Status status) {
        Toast.makeText(MainActivity.this, "Google api online", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        //TODO: maybe add Snackbar or Toast to give information about the permissions
        if(isAllPermissionsGranted()){
            switch (requestCode){
                case 0:
                    //update the UI
                    startActivity(new Intent(getApplicationContext(), TimeReminderActivity.class));
                    hideFabMenu();
                    break;
                case 1:
                    //update the UI
                    startActivity(new Intent(getApplicationContext(), LocationReminderActivity.class));
                    hideFabMenu();
                    break;
                default:
                    Log.e(TAG,"The given request code does not match any case.");
            }
        }
    }

    /**
     *
     *      https://www.youtube.com/watch?v=cyk_ht8z6IA
     *      https://www.youtube.com/watch?v=DzpwvZ4S27g
     *
     */

}
