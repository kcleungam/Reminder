package hk.ust.cse.comp4521.reminder;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import java.util.Calendar;

public class LocationReminderActivity extends AppCompatActivity {


    private ReminderData reminderData;
    private TextView editTitle;

    private TextView editTime;
    private TextView editDate;

    private TextView locationText;
    private ImageButton locationButton;
    private TextView editDescription;
    private ImageView imageView;

    private static final String[] REQUIRED_PERMISSION = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int PICK_IMAGE = 1;
    public static final int RETURN_LOCATION = 2;
    private static int hour = 0;
    private static int minute = 0;
    private TimePickerDialog timePickerDialog;

    private LocationData locationData;      // TODO Please store this


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

        //TODO
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

        ReminderDAO reminderDAO = new ReminderDAO(getApplicationContext());
        long reminderId = getIntent().getLongExtra("ReminderDataId", -1);
        if (reminderId != -1)
            reminderData = reminderDAO.get(reminderId);
        else
            reminderData = new ReminderData();
        if (reminderData != null) {
            editTitle.setText(reminderData.getTitle());


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
                reminderData.setReminderType("Location");
                reminderData.setTitle(editTitle.getText().toString());

                //TODO

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
                locationData = new LocationData(locationName, latitude, longitude);
                locationText.setText(locationData.getName());
            } catch (Exception f){
                f.printStackTrace();
            }
        }
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case R.id.timeButton:
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

