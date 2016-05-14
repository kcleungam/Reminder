package hk.ust.cse.comp4521.reminder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.ParseException;

public class TimeReminderActivity extends AppCompatActivity {

    private ReminderData reminderData;
    private TextView editTitle;
    private TextView editTime;
    private CheckBox[] wkdayBox;
    private Button selectAllButton;
    private TextView locationText;
    private TextView editDescription;
    private ImageView imageView;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReminderDAO reminderDAO = new ReminderDAO(getApplicationContext());
        long reminderId = getIntent().getLongExtra("ReminderDataId", -1);
        if(reminderId!=-1)
            reminderData = reminderDAO.get(reminderId);
        else
            reminderData = new ReminderData();
        setContentView(R.layout.edit_time_container);
        RelativeLayout layout = (RelativeLayout) findViewById( R.id.editTimeLayout );
        editTitle = (TextView) layout.findViewById(R.id.editTitle);
        editTime = (TextView) layout.findViewById(R.id.editTime);
        wkdayBox = new CheckBox[7];
        wkdayBox[1] = (CheckBox) layout.findViewById(R.id.monBox);
        wkdayBox[2] = (CheckBox) layout.findViewById(R.id.tueBox);
        wkdayBox[3] = (CheckBox) layout.findViewById(R.id.wedBox);
        wkdayBox[4] = (CheckBox) layout.findViewById(R.id.thurBox);
        wkdayBox[5] = (CheckBox) layout.findViewById(R.id.friBox);
        wkdayBox[6] = (CheckBox) layout.findViewById(R.id.satBox);
        wkdayBox[0] = (CheckBox) layout.findViewById(R.id.sunBox);
        selectAllButton = (Button) layout.findViewById(R.id.selectAll);
        locationText = (TextView) layout.findViewById(R.id.editLocation);
        View.OnClickListener selectAllListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.isEnabled()){
                    for(CheckBox box:wkdayBox){
                        box.setChecked(true);
                    }
                }else{
                    for(CheckBox box:wkdayBox){
                        box.setChecked(false);
                    }
                }
            }
        };
        selectAllButton.setOnClickListener(selectAllListener);
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
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        };
        imageView.setOnClickListener(imageViewListener);
        if(reminderData!=null){
            editTitle.setText(reminderData.title);
             editTime.setText(DateTimeParser.toString(reminderData.time, DateTimeParser.Format.SHORT));
            for(int i=0; i<wkdayBox.length; i++){
                wkdayBox[i].setChecked(reminderData.repeat[i]);
            }
            editDescription.setText(reminderData.description);
            locationText.setText(reminderData.location);
            //imageView
        }
    }

    @Override
    protected void onPause() {
        try {
            reminderData.setReminderType("Time");
            reminderData.setTitle(editTitle.getText().toString());
            reminderData.setTime(DateTimeParser.toTime(editTime.getText().toString(), DateTimeParser.Format.SHORT));
            boolean[] repeat = new boolean[wkdayBox.length];
            for(int i=0; i<wkdayBox.length; i++){
                repeat[i] = wkdayBox[i].isChecked();
            }
            reminderData.setRepeat(repeat);
            reminderData.setLocation(locationText.getText().toString());
            reminderData.setDescription(editDescription.getText().toString());
            ReminderDAO reminderDAO = new ReminderDAO(getApplicationContext());
            if(reminderData.getId()<0)
                reminderDAO.insert(reminderData);
            else
                reminderDAO.update(reminderData);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }
}
