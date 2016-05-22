package hk.ust.cse.comp4521.reminder.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import hk.ust.cse.comp4521.reminder.R;
import hk.ust.cse.comp4521.reminder.data.DataController;
import hk.ust.cse.comp4521.reminder.data.ReminderData;

/**
 * Created by alex on 19/5/2016.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
    /* UI controls */
    Context context;
    LayoutInflater layoutInflater;

    /* Data controls */
    DataController mController;

    public static ArrayList<ReminderData> reminderDatas;

    /* Others */
    public static final String TAG="RecyclerAdapter";



   /*
    * Constructor
    */
    @Deprecated
//    public RecyclerAdapter(Context context,View.OnClickListener onClickListener,View.OnLongClickListener onLongClickListener){
//        this.context=context;
//        this.onClickListener=onClickListener;
//        this.onLongClickListener=onLongClickListener;
//        this.mController=DataController.getInstance(context);
//        Log.d(TAG,"The number of reminders is "+mController.getCount());
//        for(ReminderData reminderData:mController.getAll()) {
//            reminderDataHashMap.put(reminderData.getId(), reminderData);
//            UItoID.add(reminderData.getId());
//        }
//
//        layoutInflater=LayoutInflater.from(context);
//    }

    public RecyclerAdapter(final Context context){
        this.context=context;

        this.mController = DataController.getInstance(context);
        //Log.d(TAG,"The number of reminders is "+mController.getCount());
        if(reminderDatas==null) {
            reminderDatas = new ArrayList<>();
            reminderDatas.addAll(mController.getAll());
        }

        //layoutInflater=LayoutInflater.from(context);
        layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



    /*
     * Methods inherited
     */
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v=layoutInflater.inflate(R.layout.reminder_card, parent, false);

        RecyclerViewHolder viewHolder=new RecyclerViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, final int position) {
        final ReminderData reminder = reminderDatas.get(position);

        /* set info of the card */
        holder.reminder_id = reminder.getId();
        /* set style of the card */
        if(reminder.isEnabled()){
            holder.itemView.setElevation(5.0f);
            holder.itemView.setAlpha(1.0f);
        }else{//disable it
            holder.itemView.setElevation(0.0f);
            holder.itemView.setAlpha(0.1f);
        }
        /* set the content of the card */
        //event title
        holder.reminder_title.setText(reminder.getTitle());
        holder.reminder_title.setTypeface(Typeface.DEFAULT_BOLD);
        //event location
        holder.reminder_location.setText(reminder.getLocation());
        //event time
        holder.reminder_time.setText((reminder.getReminderType()== ReminderData.ReminderType.Location)?reminder.getValidUntilTime():reminder.getTime());
        //set the icon
        int id;
        switch(reminder.getReminderType()){
            case Time:
                id=context.getResources().getIdentifier(context.getPackageName()+":drawable/clock",null,null);
                holder.imageView.setImageResource(id);
                break;
            case Location:
                id=context.getResources().getIdentifier(context.getPackageName()+":drawable/googlemap",null,null);
                holder.imageView.setImageResource(id);
                break;
            default:
        }

        /* set the listener */
        //enter the details of the event
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((MainActivity)context).canUse()) {
                    Intent intent = null;
                    switch (reminder.getReminderType()) {
                        case Time:
                            intent = new Intent(context, TimeReminderActivity.class);
                            break;
                        case Location:
                            intent = new Intent(context, LocationReminderActivity.class);
                            break;
                    }
                    intent.putExtra("ReminderId", holder.reminder_id);
                    context.startActivity(intent);
                }else{
                    Toast.makeText(context,"Please grant the permissions first.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //enable/disable the reminder
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(((MainActivity)context).canUse()) {
                    if (holder.itemView.getElevation() == 0.0) {//enable it
                        holder.itemView.setElevation(5.0f);
                        holder.itemView.setAlpha(1.0f);
                        mController.enableReminder(holder.reminder_id, true);
                        reminder.setEnabled(true);
                        notifyItemChanged(position);
                    } else {//disable it
                        holder.itemView.setElevation(0.0f);
                        holder.itemView.setAlpha(0.1f);
                        mController.enableReminder(holder.reminder_id, false);
                        reminder.setEnabled(false);
                        notifyItemChanged(position);
                    }
                }else
                    Toast.makeText(context,"Please grant the permission first.",Toast.LENGTH_SHORT).show();

                return true;//consume the long click
            }
        });
        //delete the reminder
    }

    @Override
    public int getItemCount() {
        return reminderDatas.size();
        //return name.length;
    }

    public void reset(){
        reminderDatas.clear();
        reminderDatas.addAll(mController.getAll());
    }

    public void remove(int position){
        ReminderData reminder = reminderDatas.remove(position);
        if(reminder!=null)
            mController.deleteReminder(reminder.getId());
    }


    /*
     * Other functions
     */
//    public void add(ReminderData reminderData) throws RuntimeException{
//        //Adapter layer
//        if(!reminderData.hasId()){//insertion
//            //Database layer
//            if(!mController.addReminder(reminderData)){//failed to add
//                Log.e(TAG,"The given reminder can't be added to the database");
//                throw new RuntimeException();
//            }
//            reminderDataHashMap.put(reminderData.getId(),reminderData);
//            UItoID.add(reminderData.getId());//update the mapping
//        }else {//modification
//            Log.d(TAG, "A reminder is being modified.");
//            //Database layer
//            if(!mController.putReminder(reminderData)){
//                Log.e(TAG,"The given reminder can't be added to the database");
//                throw new RuntimeException();
//            }
//        }
//        //  Log.d(TAG,"The time is "+reminderData.getTime());
//        return true;
//    }

//    public void remove(long reminderID) throws RuntimeException{
//        //Adapter layer
//        if(reminderDataHashMap.remove(reminderID)==null) {
//            Log.d(TAG, "The given reminder is not on the list");
//            return false;
//        }else{
//            UItoID.remove(Long.valueOf(reminderID));
//
//            //Database layer
//            if(!mController.deleteReminder(reminderID)){
//                Log.e(TAG,"The given reminder can't be removed from the database.");
//                throw new RuntimeException();
//            }
//        }
//        return true;
//    }
//
//    public boolean remove(ReminderData reminderData) throws  RuntimeException{
//        return remove(reminderData.getId());
//    }

//    public void removeByPosition(int position){
//        reminderDatas.remove(position);
//    }

//    public ReminderData get(int reminderID){
//        return reminderDatas.get(reminderID);
//    }
}
