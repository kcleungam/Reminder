package hk.ust.cse.comp4521.reminder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CheckBox;
import android.widget.TextView;

public class ViewTimeActivity extends AppCompatActivity {

    private ReminderData reminderData;
    private TextView editTitle;
    private TextView editTime;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            //TODO: turn this into permission listener
            //ref: http://stackoverflow.com/questions/34211693/understanding-the-android-6-permission-method
            //        for(String requiredPermission:REQUIRED_PERMISSION) {
            //            Integer value = checkSelfPermission(requiredPermission);
            //            synchronized (value) {
            //                if (value == -1) {
            //                    requestPermissions(REQUIRED_PERMISSION, 1);
            //                    value = checkSelfPermission(requiredPermission);
            //                    if (value == -1) {
            //                        finish();
            //                        return;
            //                    }
            //                }
            //            }
            //        }
            setContentView(R.layout.view_time_container);

            String title = getIntent().getStringExtra("title");
            String time = getIntent().getStringExtra("time");
            boolean[] repeat = getIntent().getBooleanArrayExtra("repeat");
            String description = getIntent().getStringExtra("description");

            ( (TextView) findViewById(R.id.showTitle) ).setText(title);
            ( (TextView) findViewById(R.id.showTime) ).setText(time);
            ( (TextView) findViewById(R.id.showDescription) ).setText(description);

            ( (CheckBox) findViewById(R.id.monBox) ).setChecked(repeat[0]);
            ( (CheckBox) findViewById(R.id.tueBox) ).setChecked(repeat[1]);
            ( (CheckBox) findViewById(R.id.wedBox) ).setChecked(repeat[2]);
            ( (CheckBox) findViewById(R.id.thurBox) ).setChecked(repeat[3]);
            ( (CheckBox) findViewById(R.id.friBox) ).setChecked(repeat[4]);
            ( (CheckBox) findViewById(R.id.satBox) ).setChecked(repeat[5]);
            ( (CheckBox) findViewById(R.id.sunBox) ).setChecked(repeat[6]);
        }

}

