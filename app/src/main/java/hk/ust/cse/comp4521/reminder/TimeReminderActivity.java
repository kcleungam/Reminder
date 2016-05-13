package hk.ust.cse.comp4521.reminder;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TimeReminderActivity extends AppCompatActivity {

    private ReminderData reminderData;
    private TextView titleText;
    private TextView timeText;
    private CheckBox monBox;
    private CheckBox tueBox;
    private CheckBox wedBox;
    private CheckBox thuBox;
    private CheckBox friBox;
    private CheckBox satBox;
    private CheckBox sunBox;
    private Button selectAllButton;
    private TextView locationText;
    private TextView descriptionText;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_time_container);
        RelativeLayout layout = (RelativeLayout) findViewById( R.id.editTimeLayout );
        titleText = (TextView) layout.findViewById(R.id.titleText);
        timeText = (TextView) layout.findViewById(R.id.timeText);
        monBox = (CheckBox) layout.findViewById(R.id.monBox);
        tueBox = (CheckBox) layout.findViewById(R.id.tueBox);
        wedBox = (CheckBox) layout.findViewById(R.id.wedBox);
        thuBox = (CheckBox) layout.findViewById(R.id.thurBox);
        friBox = (CheckBox) layout.findViewById(R.id.friBox);
        satBox = (CheckBox) layout.findViewById(R.id.satBox);
        sunBox = (CheckBox) layout.findViewById(R.id.sunBox);
        selectAllButton = (Button) layout.findViewById(R.id.selectAll);
        descriptionText = (TextView) layout.findViewById(R.id.descriptionText);
        imageView = (ImageView) layout.findViewById(R.id.imageView);
    }

    public void setReminder(ReminderData reminderData){
        this.reminderData = reminderData;
    }
}
