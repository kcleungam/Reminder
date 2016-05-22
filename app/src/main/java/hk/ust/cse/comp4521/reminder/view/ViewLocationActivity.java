//      Comp 4521
//      Leung Ka Chun       20125844        kcleungam@ust.hk
//      To Wun Yin            20112524        wytoaa@ust.hk
//      Leung Chun Fai      20113619        cfleungac@ust.hk

package hk.ust.cse.comp4521.reminder.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.FileNotFoundException;
import java.io.InputStream;

import hk.ust.cse.comp4521.reminder.R;
import hk.ust.cse.comp4521.reminder.data.DataController;
import hk.ust.cse.comp4521.reminder.data.ReminderData;

public class ViewLocationActivity extends AppCompatActivity {

    private DataController dataController;

    //UI component controls
    private MaterialEditText title,date,time,location,description;
    private ImageView image;

    private ReminderData reminderData;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            dataController = DataController.getInstance(getApplication());

            long reminderId = getIntent().getLongExtra("ReminderId", -1);
            if (reminderId != -1)
                reminderData = dataController.getReminder(reminderId);
            else {
                finish();
                return;
            }

            /* map UI components */
            setContentView(R.layout.edit_location_container);
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.setLocationReminderLayout);
            title = (MaterialEditText) layout.findViewById(R.id.title);
            title.setClickable(false);
            date=(MaterialEditText) layout.findViewById(R.id.date);
            date.setClickable(false);
            time = (MaterialEditText) layout.findViewById(R.id.time);
            time.setClickable(false);
            location=(MaterialEditText)layout.findViewById(R.id.location);
            location.setClickable(false);
            description=(MaterialEditText)layout.findViewById(R.id.description);
            description.setClickable(false);
            image=(ImageView)layout.findViewById(R.id.image);
            image.setClickable(false);

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

}

