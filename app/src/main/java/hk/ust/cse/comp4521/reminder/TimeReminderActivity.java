package hk.ust.cse.comp4521.reminder;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.util.Calendar;

public class TimeReminderActivity extends AppCompatActivity {

    private ReminderData reminderData;
    private TextView editTitle;
    private TextView editTime;
    private ImageButton timeButton;
    private CheckBox[] wkdayBox;
    private Button selectAllButton;
    private TextView locationText;
    private ImageButton locationButton;
    private TextView editDescription;
    private ImageView imageView;

    private static final int PICK_IMAGE = 1;
    private static final int dialogID = 999;
    private static int hour = 0;
    private static int minute = 0;
    private TimePickerDialog timePickerDialog;


    // try this to make time picker
    // https://www.youtube.com/watch?v=OdcYLOIScOI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReminderDAO reminderDAO = new ReminderDAO(getApplicationContext());
        long reminderId = getIntent().getLongExtra("ReminderDataId", -1);
        if (reminderId != -1)
            reminderData = reminderDAO.get(reminderId);
        else
            reminderData = new ReminderData();
        setContentView(R.layout.edit_time_container);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.editTimeLayout);
        editTitle = (TextView) layout.findViewById(R.id.editTitle);
        editTime = (TextView) layout.findViewById(R.id.editTime);
        timeButton = (ImageButton) layout.findViewById(R.id.timeButton);

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(dialogID);
            }
        });

        wkdayBox = new CheckBox[7];
        wkdayBox[1] = (CheckBox) layout.findViewById(R.id.monBox);
        wkdayBox[2] = (CheckBox) layout.findViewById(R.id.tueBox);
        wkdayBox[3] = (CheckBox) layout.findViewById(R.id.wedBox);
        wkdayBox[4] = (CheckBox) layout.findViewById(R.id.thurBox);
        wkdayBox[5] = (CheckBox) layout.findViewById(R.id.friBox);
        wkdayBox[6] = (CheckBox) layout.findViewById(R.id.satBox);
        wkdayBox[0] = (CheckBox) layout.findViewById(R.id.sunBox);
        selectAllButton = (Button) layout.findViewById(R.id.selectAll);
        View.OnClickListener selectAllListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isEnabled()) {
                    for (CheckBox box : wkdayBox) {
                        box.setChecked(true);
                    }
                } else {
                    for (CheckBox box : wkdayBox) {
                        box.setChecked(false);
                    }
                }
            }
        };
        selectAllButton.setOnClickListener(selectAllListener);
        locationText = (TextView) layout.findViewById(R.id.editLocation);
        locationButton = (ImageButton) layout.findViewById(R.id.locationButton);
        editDescription = (TextView) layout.findViewById(R.id.editDescription);
        imageView = (ImageView) layout.findViewById(R.id.imageView);
        ImageView.OnClickListener imageViewListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        };
        imageView.setOnClickListener(imageViewListener);
        if (reminderData != null) {
            editTitle.setText(reminderData.getTitle());
            editTime.setText(reminderData.getTime());
            for (int i = 0; i < wkdayBox.length; i++) {
                wkdayBox[i].setChecked(reminderData.getRepeat()[i]);
            }
            editDescription.setText(reminderData.getDescription());
            locationText.setText(reminderData.getLocation());
            //imageView
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();

        switch (item_id) {
            case R.id.action_save:
                reminderData.setReminderType("Time");
                reminderData.setTitle(editTitle.getText().toString());
                reminderData.setTime(editTime.getText().toString());
                boolean[] repeat = new boolean[wkdayBox.length];
                for (int i = 0; i < wkdayBox.length; i++) {
                    repeat[i] = wkdayBox[i].isChecked();
                }
                reminderData.setRepeat(repeat);
                reminderData.setLocation(locationText.getText().toString());
                reminderData.setDescription(editDescription.getText().toString());
                ReminderDAO reminderDAO = new ReminderDAO(getApplicationContext());
                if (reminderData.getId() < 0)
                    reminderDAO.insert(reminderData);
                else
                    reminderDAO.update(reminderData);
                finish();
                break;
            case R.id.action_cancel:
                finish();
                break;
            default:
                return false;
        }
        return true;
    }

    // display current time
    public void setCurrentTimeOnView() {
        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        // set current time into textview
        editTime.setText(
                new StringBuilder().append(Integer.toString(hour))
                        .append(":").append(Integer.toString(minute)));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case dialogID:
                // set time picker as current time
                timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
                return timePickerDialog;

        }
        return null;
    }

    private TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int min) {
            hour = hourOfDay;
            minute = min;
            editTime.setText(
                    new StringBuilder().append(Integer.toString(hour))
                            .append(":").append(Integer.toString(minute)));
        }
    };
}
