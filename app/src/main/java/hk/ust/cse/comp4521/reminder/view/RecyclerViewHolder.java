//      Comp 4521
//      Leung Ka Chun       20125844        kcleungam@ust.hk
//      To Wun Yin            20112524        wytoaa@ust.hk
//      Leung Chun Fai      20113619        cfleungac@ust.hk

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

    TextView reminder_title,reminder_location,reminder_time,reminder_status;
    ImageView imageView;
    public long reminder_id;

    public RecyclerViewHolder(View itemView) {
        super(itemView);

        reminder_title= (TextView) itemView.findViewById(R.id.reminder_title);
        reminder_location= (TextView) itemView.findViewById(R.id.reminder_location);
        reminder_time=(TextView)itemView.findViewById(R.id.reminder_time);
        reminder_status = (TextView) itemView.findViewById(R.id.reminder_status);
        imageView= (ImageView) itemView.findViewById(R.id.reminder_icon);
    }
}
