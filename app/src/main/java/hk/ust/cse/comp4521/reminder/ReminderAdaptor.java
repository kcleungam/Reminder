package hk.ust.cse.comp4521.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Krauser on 12/5/2016.
 */
public class ReminderAdaptor extends ArrayAdapter {

    List reminderList = new ArrayList<>();

    public ReminderAdaptor(Context context, int resource){      // resource is the id of the xml,     ie R.layout.row_layout

        super(context, resource);

    }

    static class DataHandler{
        TextView titleText;
        TextView timeText;
        TextView locationText;
    }

    @Override
    public void add(Object object){
        super.add(object);
        reminderList.add(object);
    }

    @Override
    public int getCount(){
        return this.reminderList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.reminderList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        row = convertView;
        DataHandler handler;

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //public View inflate (int resource, ViewGroup root, boolean attachToRoot)
            row = inflater.inflate(R.layout.row_layout, parent, false);

            handler = new DataHandler();
            handler.titleText = (TextView) row.findViewById(R.id.rowTitleText);
            handler.timeText = (TextView) row.findViewById(R.id.rowTimeText);
            handler.locationText = (TextView) row.findViewById(R.id.rowLocationText);

            row.setTag(handler);        // When we get the row, we can retrieve the corresponing handler (tag)
        } else {
            handler = (DataHandler) row.getTag();
        }

        Data data = (Data) reminderList.get(position);
        handler.titleText.setText(data.title);
        handler.timeText.setText("Test");
        handler.locationText.setText(data.location);        // Modify this later

        return row;
    }
}
