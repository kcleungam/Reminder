//      Comp 4521
//      Leung Ka Chun       20125844        kcleungam@ust.hk
//      To Wun Yin            20112524        wytoaa@ust.hk
//      Leung Chun Fai      20113619        cfleungac@ust.hk

package hk.ust.cse.comp4521.reminder.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.FileNotFoundException;
import java.io.InputStream;

import hk.ust.cse.comp4521.reminder.R;
import hk.ust.cse.comp4521.reminder.data.DataController;
import hk.ust.cse.comp4521.reminder.data.ReminderData;

public class ViewTimeActivity extends AppCompatActivity {

    private DataController dataController;

    private ReminderData reminderData;

    //UI component controls
    private MaterialEditText title,time,location,description;
    private Switch repeat;
    private CheckBox[] checkBoxes;
    private ImageView image;

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
            setContentView(R.layout.edit_time_container);
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.setTimeReminderLayout);

            title = (MaterialEditText) layout.findViewById(R.id.title);
            title.setClickable(false);
            title.setEnabled(false);
            time = (MaterialEditText) layout.findViewById(R.id.time);
            time.setClickable(false);
            location = (MaterialEditText) layout.findViewById(R.id.location);
            location.setClickable(false);
            description = (MaterialEditText) layout.findViewById(R.id.description);
            description.setClickable(false);
            description.setEnabled(false);
            repeat=(Switch)layout.findViewById(R.id.repeat_switch);
            repeat.setClickable(false);
            checkBoxes = new CheckBox[7];
            checkBoxes[1] = (CheckBox) layout.findViewById(R.id.repeat_mon);
            checkBoxes[1].setClickable(false);
            checkBoxes[2] = (CheckBox) layout.findViewById(R.id.repeat_tue);
            checkBoxes[2].setClickable(false);
            checkBoxes[3] = (CheckBox) layout.findViewById(R.id.repeat_wed);
            checkBoxes[3].setClickable(false);
            checkBoxes[4] = (CheckBox) layout.findViewById(R.id.repeat_thu);
            checkBoxes[4].setClickable(false);
            checkBoxes[5] = (CheckBox) layout.findViewById(R.id.repeat_fri);
            checkBoxes[5].setClickable(false);
            checkBoxes[6] = (CheckBox) layout.findViewById(R.id.repeat_sat);
            checkBoxes[6].setClickable(false);
            checkBoxes[0] = (CheckBox) layout.findViewById(R.id.repeat_sun);
            checkBoxes[0].setClickable(false);
            image=(ImageView)layout.findViewById(R.id.image);

            if (reminderData.getId()!=-1) {
                title.setText(reminderData.getTitle());
                time.setText(reminderData.getTime());
                for (int i = 0; i < checkBoxes.length; i++) {
                    checkBoxes[i].setChecked(reminderData.getRepeat()[i]);
                }
                repeat.setChecked(!reminderData.noRepeat());
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

