package hk.ust.cse.comp4521.reminder;

import android.content.Context;
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

    public RecyclerAdapter(Context context){
        this.context=context;
        this.onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        };
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
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        Log.d(TAG,"position = "+position);
        /*holder.reminder_title.setText("TITLE");
        holder.reminder_location.setText("LOCATION");
        holder.reminder_time.setText("TIME");*/
        ReminderData reminder=reminderDataHashMap.get(UItoID.get(position));
        holder.tv1.setText(reminder.getTitle());
        if(position%2==0){
            int id=context.getResources().getIdentifier(context.getPackageName()+":drawable/clock",null,null);
            holder.imageView.setImageResource(id);
        }
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
        //holder.imageView.setOnClickListener(clickListener);
        //holder.imageView.setTag(holder);

        //holder.itemView.setOnClickListener(this.onClickListener);


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
