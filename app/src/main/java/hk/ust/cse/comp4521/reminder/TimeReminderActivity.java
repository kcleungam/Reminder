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
    private CheckBox[] wkdayBox;
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
        wkdayBox = new CheckBox[7];
        wkdayBox[1] = (CheckBox) layout.findViewById(R.id.monBox);
        wkdayBox[2] = (CheckBox) layout.findViewById(R.id.tueBox);
        wkdayBox[3] = (CheckBox) layout.findViewById(R.id.wedBox);
        wkdayBox[4] = (CheckBox) layout.findViewById(R.id.thurBox);
        wkdayBox[5] = (CheckBox) layout.findViewById(R.id.friBox);
        wkdayBox[6] = (CheckBox) layout.findViewById(R.id.satBox);
        wkdayBox[0] = (CheckBox) layout.findViewById(R.id.sunBox);
        selectAllButton = (Button) layout.findViewById(R.id.selectAll);
        descriptionText = (TextView) layout.findViewById(R.id.descriptionText);
        imageView = (ImageView) layout.findViewById(R.id.imageView);
        if(reminderData!=null){
            titleText.setText(reminderData.title);
            timeText.setText(DateTimeParser.toString(reminderData.time, DateTimeParser.Format.SHORT));
            for(int i=0; i<wkdayBox.length; i++){
                wkdayBox[i].setChecked(reminderData.repeat[i]);
            }
        }
    }

    public void setReminder(ReminderData reminderData){
        this.reminderData = reminderData;
    }
}
