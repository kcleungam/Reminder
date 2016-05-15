package hk.ust.cse.comp4521.reminder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView reminderList;
    ReminderDataAdapter reminderAdaptor;
    private boolean fabMenuShown = false;
    private int[] fabMenuItems = {R.id.timeReminderFab, R.id.fab_2, R.id.fab_3};
    private double[][] fabMenuOffsetRatio = {{1.7, 0.25}, {1.5, 1.5}, {0.25, 1.7}};
    private int[] fabMenuShowAnimation = {R.anim.fab1_show, R.anim.fab2_show, R.anim.fab3_show};
    private int[] fabMenuHideAnimation = {R.anim.fab1_hide, R.anim.fab2_hide, R.anim.fab3_hide};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        reminderList = (ListView)findViewById(R.id.reminder_list);

        FloatingActionButton menuFab = (FloatingActionButton)findViewById(R.id.menuFab);
        menuFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Fab clicked", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), TimeReminderActivity.class);
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

//        CoordinatorLayout.LayoutParams params =
//                (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
//        params.setBehavior(new FabHideOnScroll());
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

    /**
     *
     *      https://www.youtube.com/watch?v=cyk_ht8z6IA
     *      https://www.youtube.com/watch?v=DzpwvZ4S27g
     *
     */
}
