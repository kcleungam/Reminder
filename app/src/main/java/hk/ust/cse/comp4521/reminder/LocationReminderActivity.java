package hk.ust.cse.comp4521.reminder;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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

import java.util.Calendar;

public class LocationReminderActivity extends AppCompatActivity {
    private ReminderData reminderData;
    private TextView editTitle;
    private TextView editTime;
    private TextView editDate;
//    private ImageButton timeButton;
    private TextView locationText;
    private ImageButton locationButton;
    private TextView editDescription;
    private ImageView imageView;

    private static final String[] REQUIRED_PERMISSION = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int PICK_IMAGE = 1;
    public static final int RETURN_LOCATION = 2;

    // try this to make time picker
    // https://www.youtube.com/watch?v=OdcYLOIScOI

    @Override
    //@TargetApi(23)
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

        setContentView(R.layout.edit_location_container);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.editLocationLayout);
        editTitle = (TextView) layout.findViewById(R.id.editTitle);
        editDate=(TextView)layout.findViewById(R.id.editDate);
        editDate.setText(DateTimeParser.toString(Calendar.getInstance().getTimeInMillis(), DateTimeParser.Format.DATE));
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(R.id.editDate);
            }
        });
        editTime = (TextView) layout.findViewById(R.id.editTime);
        editTime.setText(DateTimeParser.toString(Calendar.getInstance().getTimeInMillis(), DateTimeParser.Format.SHORT));
        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(R.id.editTime);
            }
        });
//        timeButton = (ImageButton) layout.findViewById(R.id.timeButton);
        locationText = (TextView) layout.findViewById(R.id.editLocation);
        locationButton = (ImageButton) layout.findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivityForResult(intent, RETURN_LOCATION);
            }
        });
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

        long reminderId = getIntent().getLongExtra("ReminderDataId", -1);
        if (reminderId != -1)
            reminderData = ReminderDataController.getInstance().getReminder(reminderId);
        else
            reminderData = new ReminderData();
        if (reminderData.getId()!=-1) {
            editTitle.setText(reminderData.getTitle());
            editTime.setText(reminderData.getValidUntilTime());
            editDate.setText(reminderData.getValidUntilDate());
            editDescription.setText(reminderData.getDescription());
            locationText.setText(reminderData.getLocation());
            if(reminderData.getImageUri()!=null) {
//                try {
//                    Uri imageUri = Uri.parse(reminderData.getImageUri());
//                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                    Bitmap image = BitmapFactory.decodeStream(imageStream);
//                    imageView.setImageBitmap(image);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
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
                if(editTitle.getText().length()==0){
                    Toast.makeText(LocationReminderActivity.this, "Empty title", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if(editTime.getText().length()==0){
                    Toast.makeText(LocationReminderActivity.this, "Empty time", Toast.LENGTH_SHORT).show();
                    return true;
                }
                reminderData.setReminderType(ReminderData.ReminderType.Location);
                reminderData.setTitle(editTitle.getText().toString());
                reminderData.setValidUntilTime(editTime.getText().toString());
                reminderData.setValidUntilDate(editDate.getText().toString());
                reminderData.setLocation(locationText.getText().toString());
                reminderData.setDescription(editDescription.getText().toString());
                if (reminderData.getId() < 0) {
                    reminderData.setEnabled(true);
                    ReminderDataController.getInstance().addReminder(reminderData);
                }else
                    ReminderDataController.getInstance().putReminder(reminderData);
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
//            try {
//                Uri imageUri = data.getData();
//                InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                imageView.setImageBitmap(selectedImage);
//                reminderData.setImageUri(data.getData().toString());
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
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
                locationText.setText(locationName);
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
            case R.id.editTime:
                // set time picker as current time
                TimePickerDialog timePickerDialog;
                if(reminderData.getId()!=-1) {
                    timePickerDialog = new TimePickerDialog(this, onTimeSetListener, DateTimeParser.toHour(reminderData.getTimeInMillis()), DateTimeParser.toMin(reminderData.getTimeInMillis()), true);
                }else{
                    Calendar now = Calendar.getInstance();
                    timePickerDialog = new TimePickerDialog(this, onTimeSetListener, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                }
                return timePickerDialog;
            case R.id.editDate:
                // set date picker as current time
                DatePickerDialog datePickerDialog;
                if(reminderData.getId()!=-1) {
                    datePickerDialog = new DatePickerDialog(this, onDateSetListener, DateTimeParser.toYear(reminderData.getTimeInMillis()), DateTimeParser.toMonth(reminderData.getTimeInMillis()), DateTimeParser.toMin(reminderData.getTimeInMillis()));
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
            editTime.setText(DateTimeParser.toString(now.getTimeInMillis(), DateTimeParser.Format.SHORT));
        }
    };

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener(){
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.YEAR, year);
            now.set(Calendar.MONTH, monthOfYear);
            now.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            editDate.setText(DateTimeParser.toString(now.getTimeInMillis(), DateTimeParser.Format.DATE));
        }
    };
}

