package hk.ust.cse.comp4521.reminder.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import hk.ust.cse.comp4521.reminder.R;
import hk.ust.cse.comp4521.reminder.data.DataController;
import hk.ust.cse.comp4521.reminder.data.ReminderData;

public class ViewLocationActivity extends AppCompatActivity {

    private DataController dataController;

    private ReminderData reminderData;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            dataController = DataController.getInstance(getApplication());

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
            setContentView(R.layout.view_location_container);
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.viewLocationLayout);

            long reminderId = getIntent().getLongExtra("ReminderId", -1);
            if (reminderId != -1)
                reminderData = dataController.getReminder(reminderId);
            else
                reminderData = new ReminderData();

            ( (TextView) layout.findViewById(R.id.showTitle) ).setText(reminderData.getTitle());
            ( (TextView) layout.findViewById(R.id.showTime) ).setText(reminderData.getTime());
            ( (TextView) layout.findViewById(R.id.showDescription) ).setText(reminderData.getDescription());
            ( (TextView) layout.findViewById(R.id.showLocation) ).setText(reminderData.getLocation());

            //TODO: Enable image view
            if(reminderData.getImageUri()!=null) {
//                try {
//                    Uri imageUri = Uri.parse(reminderData.getImageUri());
//                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                    Bitmap image = BitmapFactory.decodeStream(imageStream);
//                    ((ImageView) layout.findViewById(R.id.imageView)).setImageBitmap(image);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
            }
        }

}

