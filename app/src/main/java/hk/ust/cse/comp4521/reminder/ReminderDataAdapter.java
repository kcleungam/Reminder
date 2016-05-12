package hk.ust.cse.comp4521.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Jeffrey on 12/5/2016.
 */
public class ReminderDataAdapter extends ArrayAdapter<ReminderData>{

    ArrayList<ReminderData> reminderList = new ArrayList<>();

    public ReminderDataAdapter(Context context, int resource){
        super(context, resource);
    }

    public void addItem(ReminderData data){
        reminderList.add(data);
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
            row = inflater.inflate(R.layout.row_layout, parent);
            handler.enabledSwitch = (Switch) row.findViewById(R.id.switch1);
            handler.timeView = (TextView) row.findViewById(R.id.timeText);
            handler.titleView = (TextView) row.findViewById( R.id.titleText);
            handler.locationView = (TextView) row.findViewById(R.id.locationText);
        }else{
            handler = (RowHandler) row.getTag();
        }
        ReminderData data = getItem(position);
        handler.enabledSwitch.setChecked(data.enabled);
        handler.timeView.setText(data.time.toString());
        handler.titleView.setText(data.title);
        handler.locationView.setText(data.location);
        return super.getView(position, convertView, parent);
    }

    public static class RowHandler{
        TextView titleView;
        TextView locationView;
        TextView timeView;
        Switch enabledSwitch;
    }
}
