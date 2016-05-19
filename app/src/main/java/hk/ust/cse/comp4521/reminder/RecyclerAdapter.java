package hk.ust.cse.comp4521.reminder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by alex on 19/5/2016.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
    /* UI controls */
    Context context;
    LayoutInflater layoutInflater;

    /* Data controls */
    ReminderDataController reminderDataController;
    LinkedHashMap<Long,ReminderData> reminderDataHashMap=new LinkedHashMap<>();
    ArrayList<Long> UItoID=new ArrayList<>();

    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;
    String [] name={"Androidwarriors","Stackoverflow","Developer Android","AndroidHive",
            "Slidenerd","TheNewBoston","Truiton","HmkCode","JavaTpoint","Javapeper"};

    /* Others */
    public static final String TAG="RecyclerAdapter";



   /*
    * Constructor
    */
    @Deprecated
    public RecyclerAdapter(Context context,View.OnClickListener onClickListener,View.OnLongClickListener onLongClickListener){
        this.context=context;
        this.onClickListener=onClickListener;
        this.onLongClickListener=onLongClickListener;
        this.reminderDataController=ReminderDataController.getInstance(context);
        Log.d(TAG,"The number of reminders is "+reminderDataController.getCount());
        for(ReminderData reminderData:reminderDataController.getAll()) {
            reminderDataHashMap.put(reminderData.getId(), reminderData);
            UItoID.add(reminderData.getId());
        }

        layoutInflater=LayoutInflater.from(context);
    }

    public RecyclerAdapter(final Context context){
        this.context=context;
        this.onLongClickListener=new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO
                return false;
            }
        };

        this.reminderDataController=ReminderDataController.getInstance(context);
        Log.d(TAG,"The number of reminders is "+reminderDataController.getCount());
        for(ReminderData reminderData:reminderDataController.getAll()) {
            reminderDataHashMap.put(reminderData.getId(), reminderData);
            UItoID.add(reminderData.getId());
        }

        layoutInflater=LayoutInflater.from(context);
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
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
        final ReminderData reminder=reminderDataHashMap.get(UItoID.get(position));

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
                Intent intent=null;
                ReminderData selectedReminder=reminderDataHashMap.get(UItoID.get(holder.getAdapterPosition()));
                switch (selectedReminder.getReminderType()){
                    case Time:
                        intent=new Intent(context,TimeReminderActivity.class);
                        break;
                    case Location:
                        intent=new Intent(context,LocationReminderActivity.class);
                        break;
                }
                intent.putExtra("ReminderId",selectedReminder.getId());
                context.startActivity(intent);
            }
        });
        //enable/disable the reminder
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @SuppressLint("NewApi")
            @Override
            public boolean onLongClick(View v) {
                if(holder.itemView.getElevation()==0.0){//enable it
                    holder.itemView.setElevation(5.0f);
                    holder.itemView.setAlpha(1.0f);
                    //TODO: call related function
                }else{//disable it
                    holder.itemView.setElevation(0.0f);
                    holder.itemView.setAlpha(0.1f);
                    //TODO: call related function
                }

                return true;//consume the long click
            }
        });
        //delete the reminder
    }

    @Override
    public int getItemCount() {
        return reminderDataHashMap.size();
        //return name.length;
    }



    /*
     * Other functions
     */
    public boolean add(ReminderData reminderData) throws RuntimeException{
        //Adapter layer
        if(reminderDataHashMap.put(reminderData.getId(),reminderData)==null){//insertion
            UItoID.add(reminderData.getId());//update the mapping

            //Database layer
            ReminderData newReminder=reminderDataController.addReminder(reminderData);
            if(newReminder==null){//failed to add
                Log.e(TAG,"The given reminder can't be added to the database");
                throw new RuntimeException();
            }
        }else {//modification
            Log.d(TAG, "A reminder is being modified.");
            //Database layer
            if(!reminderDataController.putReminder(reminderData)){
                Log.e(TAG,"The given reminder can't be added to the database");
                throw new RuntimeException();
            }
        }
        return true;
    }

    public boolean remove(long reminderID) throws RuntimeException{
        //Adapter layer
        if(reminderDataHashMap.remove(reminderID)==null) {
            Log.d(TAG, "The given reminder is not on the list");
            return false;
        }else{
            UItoID.remove(Long.valueOf(reminderID));

            //Database layer
            if(!reminderDataController.deleteReminder(reminderID)){
                Log.e(TAG,"The given reminder can't be removed from the database.");
                throw new RuntimeException();
            }
        }
        return true;
    }

    public boolean remove(ReminderData reminderData) throws  RuntimeException{
        return remove(reminderData.getId());
    }
}
