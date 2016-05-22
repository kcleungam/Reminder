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
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import hk.ust.cse.comp4521.reminder.R;
import hk.ust.cse.comp4521.reminder.data.DataController;
import hk.ust.cse.comp4521.reminder.data.ReminderData;

public class ViewTimeActivity extends AppCompatActivity {

    private DataController dataController;

    private ReminderData reminderData;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            dataController = DataController.getInstance(getApplication());

            setContentView(R.layout.view_time_container);
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.viewTimeLayout);

            long reminderId = getIntent().getLongExtra("ReminderId", -1);
            if (reminderId != -1)
                reminderData = dataController.getReminder(reminderId);
            else
                reminderData = new ReminderData();

            ( (TextView) layout.findViewById(R.id.showTitle) ).setText(reminderData.getTitle());
            ( (TextView) layout.findViewById(R.id.showTime) ).setText(reminderData.getTime());
            ( (TextView) layout.findViewById(R.id.showDescription) ).setText(reminderData.getDescription());

            ( (CheckBox) layout.findViewById(R.id.monBox) ).setChecked(reminderData.getRepeat()[1]);
            ( (CheckBox) layout.findViewById(R.id.tueBox) ).setChecked(reminderData.getRepeat()[2]);
            ( (CheckBox) layout.findViewById(R.id.wedBox) ).setChecked(reminderData.getRepeat()[3]);
            ( (CheckBox) layout.findViewById(R.id.thurBox) ).setChecked(reminderData.getRepeat()[4]);
            ( (CheckBox) layout.findViewById(R.id.friBox) ).setChecked(reminderData.getRepeat()[5]);
            ( (CheckBox) layout.findViewById(R.id.satBox) ).setChecked(reminderData.getRepeat()[6]);
            ( (CheckBox) layout.findViewById(R.id.sunBox) ).setChecked(reminderData.getRepeat()[0]);

            if(reminderData.getImageUri()!=null) {
                try {
                    Uri imageUri = Uri.parse(reminderData.getImageUri());
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap image = BitmapFactory.decodeStream(imageStream);
                    ((ImageView) layout.findViewById(R.id.imageView)).setImageBitmap(image);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

}

