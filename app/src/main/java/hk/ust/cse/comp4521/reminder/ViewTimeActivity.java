package hk.ust.cse.comp4521.reminder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class ViewTimeActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.view_time_container);

            String title = getIntent().getStringExtra("title");

            TextView showTitle = (TextView) findViewById(R.id.showTitle);
            showTitle.setText(title);
        }

}

