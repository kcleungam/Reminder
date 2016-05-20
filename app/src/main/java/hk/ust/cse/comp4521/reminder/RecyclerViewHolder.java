package hk.ust.cse.comp4521.reminder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by alex on 19/5/2016.
 */
public class RecyclerViewHolder extends RecyclerView.ViewHolder {

    TextView reminder_title,reminder_location,reminder_time;
    ImageView imageView;

    public RecyclerViewHolder(View itemView) {
        super(itemView);

        reminder_title= (TextView) itemView.findViewById(R.id.reminder_title);
        reminder_location= (TextView) itemView.findViewById(R.id.reminder_location);
        reminder_time=(TextView)itemView.findViewById(R.id.reminder_time);
        imageView= (ImageView) itemView.findViewById(R.id.reminder_icon);
    }
}
