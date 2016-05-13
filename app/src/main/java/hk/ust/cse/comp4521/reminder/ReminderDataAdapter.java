package hk.ust.cse.comp4521.reminder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Handler;

import static android.support.v4.app.ActivityCompat.startActivity;
import static android.support.v4.content.ContextCompat.startActivities;

/**
 * Created by Jeffrey on 12/5/2016.
 */
public class ReminderDataAdapter extends ArrayAdapter<ReminderData>{

    ArrayList<ReminderData> reminderList = new ArrayList<>();
    View.OnClickListener onClickListener;

    public ReminderDataAdapter(Context context, int resource){
        super(context, resource);
    }

    public ReminderDataAdapter(Context context, int resource, View.OnClickListener onClickListener){
        super(context, resource);
        this.onClickListener = onClickListener;
    }

    public void addItem(ReminderData data){
        reminderList.add(data);
    }

    @Override
    public void add(ReminderData object) {
        super.add(object);
        reminderList.add(object);
    }

    @Override
    public ReminderData getItem(int position){
        return reminderList.get(position);
    }

    @Override
    public int getCount() {
        return reminderList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RowHandler handler = new RowHandler();
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.row_layout, parent, false);
            handler.enabledSwitch = (Switch) row.findViewById(R.id.switch1);
            handler.timeView = (TextView) row.findViewById(R.id.rowTimeText);
            handler.titleView = (TextView) row.findViewById(R.id.rowTitleText);
            handler.locationView = (TextView) row.findViewById(R.id.rowLocationText);

            final int _position = position;
            handler.enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // do something, the isChecked will be
                    // true if the switch is in the On position
                    ReminderData data = reminderList.get(_position);
                    data.enabled = isChecked;
                }
            });
            row.setTag(handler);        // When we get the row, we can retrieve the corresponing handler (tag)
        }else{
            handler = (RowHandler) row.getTag();
        }
        ReminderData data = getItem(position);
        handler.enabledSwitch.setChecked(data.enabled);
        handler.timeView.setText(data.time.toString());
        handler.titleView.setText(data.title);
        handler.locationView.setText(data.location);

        /*
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Toast.makeText(getContext(), ( (RowHandler)v.getTag() ).titleView.getText() , Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), TimeReminderActivity.class);
                startActivity(getContext(),intent,);
                return false;
            }
        };

    */

        row.setOnClickListener(onClickListener);
        return row;
    }

    public static class RowHandler{
        TextView titleView;
        TextView locationView;
        TextView timeView;
        Switch enabledSwitch;
    }
}
