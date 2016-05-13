package hk.ust.cse.comp4521.reminder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Jeffrey on 12/5/2016.
 */
public class ReminderDataAdapter extends ArrayAdapter<ReminderData>{

    ArrayList<ReminderData> reminderList = new ArrayList<>();
    private View.OnClickListener onClickListener;

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
    public void clear() {
        super.clear();
        reminderList.clear();
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
            handler.enabledSwitch.setTag(handler);
            handler.enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // do something, the isChecked will be
                    // true if the switch is in the On position
                    ReminderDAO reminderDAO = new ReminderDAO(getContext());
                    long id = ( (ReminderDataAdapter.RowHandler) buttonView.getTag() ).reminderId;
                    ReminderData data = reminderDAO.get(id);
                    data.enabled = isChecked;
                    reminderDAO.update(data);
                }
            });
            row.setTag(handler);        // When we get the row, we can retrieve the corresponing handler (tag)
        }else{
            handler = (RowHandler) row.getTag();
        }
        ReminderData data = getItem(position);
        handler.reminderId = data.getId();
        handler.enabledSwitch.setChecked(data.enabled);
        handler.timeView.setText(DateTimeParser.toString(data.time, DateTimeParser.Format.SHORT));
        handler.titleView.setText(data.title);
        handler.locationView.setText(data.location);

        row.setOnLongClickListener(new AdapterView.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub

                Toast.makeText(getContext(), ( (RowHandler)v.getTag() ).titleView.getText() , Toast.LENGTH_SHORT).show();

                return true;
            }
        });

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
        long reminderId;
        TextView titleView;
        TextView locationView;
        TextView timeView;
        Switch enabledSwitch;
    }
}
