package hk.ust.cse.comp4521.reminder.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import hk.ust.cse.comp4521.reminder.R;

/**
 * Created by alex on 19/5/2016.
 */
public class RecyclerViewHolder extends RecyclerView.ViewHolder {

    TextView reminder_title,reminder_location,reminder_time;
    ImageView imageView;
    public long reminder_id;

    public RecyclerViewHolder(View itemView) {
        super(itemView);

        reminder_title= (TextView) itemView.findViewById(R.id.reminder_title);
        reminder_location= (TextView) itemView.findViewById(R.id.reminder_location);
        reminder_time=(TextView)itemView.findViewById(R.id.reminder_time);
        imageView= (ImageView) itemView.findViewById(R.id.reminder_icon);
    }
}
