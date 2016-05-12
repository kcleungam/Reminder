package hk.ust.cse.comp4521.reminder;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    ListView reminderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        reminderList = (ListView)findViewById(R.id.reminder_list);
        ReminderDataAdapter adapter = new ReminderDataAdapter(getApplicationContext(), R.layout.row_layout);
        ArrayList<ReminderData> sampleList = new ArrayList<ReminderData>();
        for(int i=0; i<25; i++){
            ReminderData data = new ReminderData();
            data.title = "sample"+i;
            data.location = "location"+i;
            data.enabled = i%2==0;
            data.reminderType = ReminderData.ReminderType.Time;
            data.description = "general description";
            Calendar time = Calendar.getInstance();
            time.set(Calendar.DATE, i);
            data.time = new Time(time.getTimeInMillis());
        }
        for(ReminderData sample:sampleList){
            adapter.addItem(sample);
        }
        reminderList.setAdapter(adapter);

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

    /**
     *
     *      https://www.youtube.com/watch?v=cyk_ht8z6IA
     *      https://www.youtube.com/watch?v=DzpwvZ4S27g
     *
     */
}
