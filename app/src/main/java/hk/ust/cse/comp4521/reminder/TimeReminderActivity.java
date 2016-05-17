package hk.ust.cse.comp4521.reminder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;

public class TimeReminderActivity extends AppCompatActivity {

    private ReminderData reminderData;
    private TextView editTitle;
    private TextView editTime;
    private ImageButton timeButton;
    private CheckBox[] checkBoxes;
    private Button selectAllButton;
    private TextView locationText;
    private ImageButton locationButton;
    private TextView editDescription;
    private ImageView imageView;

    private static final String[] REQUIRED_PERMISSION = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int PICK_IMAGE = 1;
    private TimePickerDialog timePickerDialog;


    // try this to make time picker
    // https://www.youtube.com/watch?v=OdcYLOIScOI

    @Override
    @TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: turn this into permission listener
        //source:
        for(String requiredPermission:REQUIRED_PERMISSION) {
            Integer value = checkSelfPermission(requiredPermission);
            synchronized (value) {
                if (value == -1) {
                    requestPermissions(REQUIRED_PERMISSION, 1);
                    value = checkSelfPermission(requiredPermission);
                    if (value == -1) {
                        finish();
                        return;
                    }
                }
            }
        }

        setContentView(R.layout.edit_time_container);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.editTimeLayout);
        editTitle = (TextView) layout.findViewById(R.id.editTitle);
        editTime = (TextView) layout.findViewById(R.id.editTime);
        timeButton = (ImageButton) layout.findViewById(R.id.timeButton);
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(R.id.timeButton);
            }
        });
        checkBoxes = new CheckBox[7];
        checkBoxes[1] = (CheckBox) layout.findViewById(R.id.monBox);
        checkBoxes[2] = (CheckBox) layout.findViewById(R.id.tueBox);
        checkBoxes[3] = (CheckBox) layout.findViewById(R.id.wedBox);
        checkBoxes[4] = (CheckBox) layout.findViewById(R.id.thurBox);
        checkBoxes[5] = (CheckBox) layout.findViewById(R.id.friBox);
        checkBoxes[6] = (CheckBox) layout.findViewById(R.id.satBox);
        checkBoxes[0] = (CheckBox) layout.findViewById(R.id.sunBox);
        CompoundButton.OnClickListener wkdayBoxListener = new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (CheckBox box : checkBoxes) {
                    if(box.isChecked()!=true) {
                        setSelectAll(true);
                        return;
                    }
                }
                setSelectAll(false);
            }
        };
        for(CheckBox checkBox: checkBoxes){
            checkBox.setOnClickListener(wkdayBoxListener);
        }
        selectAllButton = (Button) layout.findViewById(R.id.selectAll);
        View.OnClickListener selectAllListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) { //if all box checked, set button to "unselect all", vice versa
                for (CheckBox box : checkBoxes) {
                    box.setChecked(v.getTag().equals(true));
                }
                setSelectAll(!v.getTag().equals(true));
            }
        };
        selectAllButton.setOnClickListener(selectAllListener);
        selectAllButton.setTag(true);
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

        ReminderDAO reminderDAO = new ReminderDAO(getApplicationContext());
        long reminderId = getIntent().getLongExtra("ReminderDataId", -1);
        if (reminderId != -1)
            reminderData = reminderDAO.get(reminderId);
        else
            reminderData = new ReminderData();
        if (reminderData != null) {
            editTitle.setText(reminderData.getTitle());
            editTime.setText(reminderData.getTime());
            for (int i = 0; i < checkBoxes.length; i++) {
                checkBoxes[i].setChecked(reminderData.getRepeat()[i]);
            }
            setSelectAll(!Arrays.asList(reminderData.getRepeat()).contains(false));
            editDescription.setText(reminderData.getDescription());
            locationText.setText(reminderData.getLocation());
            if(reminderData.getImageUri()!=null) {
                try {
                    Uri imageUri = Uri.parse(reminderData.getImageUri());
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap image = BitmapFactory.decodeStream(imageStream);
                    imageView.setImageBitmap(image);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setSelectAll(boolean selectAll){
        if(selectAll){
            selectAllButton.setText("Select all");
            selectAllButton.setTag(true);
        }else{
            selectAllButton.setText("Unselect all");
            selectAllButton.setTag(false);
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
                if(editTitle.getText().length()==0){
                    Toast.makeText(TimeReminderActivity.this, "Empty title", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if(editTime.getText().length()==0){
                    Toast.makeText(TimeReminderActivity.this, "Empty time", Toast.LENGTH_SHORT).show();
                    return true;
                }
                reminderData.setReminderType("Time");
                reminderData.setTitle(editTitle.getText().toString());
                reminderData.setTime(editTime.getText().toString());
                boolean[] repeat = new boolean[checkBoxes.length];
                for (int i = 0; i < checkBoxes.length; i++) {
                    repeat[i] = checkBoxes[i].isChecked();
                }
                reminderData.setRepeat(repeat);
                reminderData.setLocation(locationText.getText().toString());
                reminderData.setDescription(editDescription.getText().toString());
                ReminderDAO reminderDAO = new ReminderDAO(getApplicationContext());
                if (reminderData.getId() < 0)
                    reminderDAO.insert(reminderData);
                else
                    reminderDAO.update(reminderData);
                setAlarm();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
                reminderData.setImageUri(data.getData().toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }

    // display current time
//    public void setCurrentTimeOnView() {
//        final Calendar c = Calendar.getInstance();
//        hour = c.get(Calendar.HOUR_OF_DAY);
//        minute = c.get(Calendar.MINUTE);
//
//        // set current time into textview
//        editTime.setText(
//                new StringBuilder().append(Integer.toString(hour))
//                        .append(":").append(Integer.toString(minute)));
//    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case R.id.timeButton:
                // set time picker as current time
                if(reminderData!=null) {
                    timePickerDialog = new TimePickerDialog(this, onTimeSetListener, DateTimeParser.toHour(reminderData.getTimeInMillis()), DateTimeParser.toMin(reminderData.getTimeInMillis()), true);
                }else{
                    Calendar now = Calendar.getInstance();
                    timePickerDialog = new TimePickerDialog(this, onTimeSetListener, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                }
                return timePickerDialog;

        }
        return null;
    }

    private TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int min) {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, hourOfDay);
            now.set(Calendar.MINUTE, min);
            editTime.setText(DateTimeParser.toString(now.getTimeInMillis(), DateTimeParser.Format.SHORT));
        }
    };
}
