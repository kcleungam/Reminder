//      Comp 4521
//      Leung Ka Chun       20125844        kcleungam@ust.hk
//      To Wun Yin            20112524        wytoaa@ust.hk
//      Leung Chun Fai      20113619        cfleungac@ust.hk

package hk.ust.cse.comp4521.reminder.view;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

import hk.ust.cse.comp4521.reminder.R;
import hk.ust.cse.comp4521.reminder.data.DataController;
import hk.ust.cse.comp4521.reminder.data.ReminderData;
import hk.ust.cse.comp4521.reminder.util.DateTimeParser;

public class LocationReminderActivity extends AppCompatActivity {
    /* variables */
    //data controls
    private DataController dataController;
    private ReminderData reminderData;

    //UI component controls
    private MaterialEditText title,date,time,location,description;
    private ImageView image;
    /*
    private TextView editTitle;
    private TextView editTime;
    private TextView editDate;
    private TextView locationText;
    private ImageButton locationButton;
    private TextView editDescription;
    private ImageView imageView;*/

    //other UI related controls
    private static final int PICK_IMAGE = 1;
    public static final int RETURN_LOCATION = 2;

    // try this to make time picker
    // https://www.youtube.com/watch?v=OdcYLOIScOI

    @Override
    //@TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataController = DataController.getInstance(getApplication());

        /* map UI components */
        setContentView(R.layout.edit_location_container);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.setLocationReminderLayout);
        title = (MaterialEditText) layout.findViewById(R.id.title);
        date=(MaterialEditText) layout.findViewById(R.id.date);
        time = (MaterialEditText) layout.findViewById(R.id.time);
        location=(MaterialEditText)layout.findViewById(R.id.location);
        description=(MaterialEditText)layout.findViewById(R.id.description);
        image=(ImageView)layout.findViewById(R.id.image);

        /* set up listeners */
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(R.id.date);
            }
        });
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(R.id.time);
            }
        });
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivityForResult(intent, RETURN_LOCATION);
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
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
        });

        /* initialise the value of UI components */
        date.setText(DateTimeParser.toString(Calendar.getInstance().getTimeInMillis()+86400000, DateTimeParser.Format.DATE));
        time.setText(DateTimeParser.toString(Calendar.getInstance().getTimeInMillis(), DateTimeParser.Format.SHORT));

        long reminderId = getIntent().getLongExtra("ReminderId", -1);
        if (reminderId != -1)
            reminderData = dataController.getReminder(reminderId);
        else
            reminderData = new ReminderData();
        if (reminderData.getId()!=-1) {
            title.setText(reminderData.getTitle());
            time.setText(reminderData.getValidUntilTime());
            date.setText(reminderData.getValidUntilDate());
            description.setText(reminderData.getDescription());
            location.setText(reminderData.getLocation());
            if(reminderData.getImageUri()!=null) {
                try {
                    Uri imageUri = Uri.parse(reminderData.getImageUri());
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap image = BitmapFactory.decodeStream(imageStream);
                    this.image.setImageBitmap(image);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
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
                if(title.getText().length()==0){
                    Toast.makeText(LocationReminderActivity.this, "Empty title", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if(!DateTimeParser.validate(time.getText().toString(), DateTimeParser.Format.SHORT)){
                    Toast.makeText(LocationReminderActivity.this, "Empty time", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if(!DateTimeParser.validate(date.getText().toString(), DateTimeParser.Format.DATE)){
                    Toast.makeText(LocationReminderActivity.this, "Empty date", Toast.LENGTH_SHORT).show();
                    return true;
                }
                reminderData.setReminderType(ReminderData.ReminderType.Location);
                reminderData.setTitle(title.getText().toString());
                reminderData.setValidUntilTime(time.getText().toString());
                reminderData.setValidUntilDate(date.getText().toString());
                reminderData.setLocation(location.getText().toString());
                reminderData.setDescription(description.getText().toString());
                if (reminderData.getId() < 0) {
                    reminderData.setEnabled(true);
                    dataController.addReminder(reminderData);
                }else
                    dataController.putReminder(reminderData);
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
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                this.image.setImageBitmap(selectedImage);
                reminderData.setImageUri(data.getData().toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        } else if( requestCode == RETURN_LOCATION && resultCode == Activity.RESULT_OK){
            if(data == null){
                //Display an error
                return;
            }
            try{
                String locationName = data.getStringExtra("locationName");
                double latitude = data.getDoubleExtra("latitude", -999);
                double longitude = data.getDoubleExtra("longitude", -999);
                location.setText(locationName);
                reminderData.setLatitude(latitude);
                reminderData.setLongitude(longitude);
            } catch (Exception f){
                f.printStackTrace();
            }
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
            case R.id.time:
                // set time picker as current time
                TimePickerDialog timePickerDialog;
                if(reminderData.getId()!=-1) {
                    timePickerDialog = new TimePickerDialog(this, onTimeSetListener, reminderData.getValidUntilTime(Calendar.HOUR_OF_DAY), reminderData.getValidUntilTime(Calendar.MINUTE), true);
                }else{
                    Calendar now = Calendar.getInstance();
                    timePickerDialog = new TimePickerDialog(this, onTimeSetListener, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                }
                return timePickerDialog;
            case R.id.date:
                // set date picker as current time
                DatePickerDialog datePickerDialog;
                if(reminderData.getId()!=-1) {
                    datePickerDialog = new DatePickerDialog(this, onDateSetListener, reminderData.getValidUntilDate(Calendar.YEAR), reminderData.getValidUntilDate(Calendar.MONTH), reminderData.getValidUntilDate(Calendar.DAY_OF_MONTH));
                }else{
                    Calendar now = Calendar.getInstance();
                    datePickerDialog = new DatePickerDialog(this, onDateSetListener, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                }
                return datePickerDialog;
        }
        return null;
    }

    private TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int min) {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, hourOfDay);
            now.set(Calendar.MINUTE, min);
            time.setText(DateTimeParser.toString(now.getTimeInMillis(), DateTimeParser.Format.SHORT));
        }
    };

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener(){
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.YEAR, year);
            now.set(Calendar.MONTH, monthOfYear);
            now.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            date.setText(DateTimeParser.toString(now.getTimeInMillis(), DateTimeParser.Format.DATE));
        }
    };
}

