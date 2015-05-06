package edu.uco.rnolastname.program6.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.calendar.FragmentCalendar;
import edu.uco.rnolastname.program6.dbutilities.Task;

public class StableArrayAdapterTask extends ArrayAdapter<Task> {
	final int INVALID_ID = -1;
	LayoutInflater inflater;
	Context context;
    HashMap<Task, Integer> mIdMap = new HashMap<Task, Integer>();
    View.OnTouchListener mTouchListener;     
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd HH:mm aa");

    public StableArrayAdapterTask(Context context, int textViewResourceId,
            List<Task> tasks, View.OnTouchListener listener) {
        super(context, textViewResourceId, tasks);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        mTouchListener = listener;
        for (int i = 0; i < tasks.size(); ++i) {
            mIdMap.put(tasks.get(i), i);
        }
    }

    @Override
    public long getItemId(int position) {
    	if(position < 0 || position >= mIdMap.size()){
    		return INVALID_ID;
    	}
        Task item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;//super.getView(position, convertView, parent);
        if(view == null){
        	view = inflater.inflate(R.layout.reminderlist_item, parent,false);    		
        }        
        
        if (view != convertView) {
            // Add touch listener to every new view to track swipe motion
        	view.setOnTouchListener(mTouchListener);
        }
        
        Task item = getItem(position);        
        
        TextView taskName = (TextView) view.findViewById(R.id.task_name);
        TextView taskCategory = (TextView) view.findViewById(R.id.task_category);
        TextView taskTime = (TextView) view.findViewById(R.id.task_time);
        TextView taskNote = (TextView) view.findViewById(R.id.task_note);    
        
        if(item != null){
        	Date parsedDate = null;
            if(item.getDate() != null && !item.getDate().equalsIgnoreCase("")){        	
                try {
        			parsedDate = FragmentCalendar.dateFormatWriter.parse(item.getDate());
        		} catch (ParseException e) {
        			Log.d("DEBUG",e.getMessage());    			
        		}
            }
                    
            if(item.getTaskName() != null && item.getCategory() != null && parsedDate != null 
            		&& (item.getNotes() != null || !item.getNotes().equalsIgnoreCase(""))){        	
            	taskName.setText(item.getTaskName().toUpperCase() + " - ");
            	taskCategory.setText(item.getCategory().toUpperCase() + " - ");    
            	taskTime.setText(FragmentCalendar.dateFormatter.format(parsedDate));
            	
//            	((TextView) view.findViewById(R.id.task_name)).setText(item.getTaskName().toUpperCase() + " - ");
//                ((TextView) view.findViewById(R.id.task_category)).setText(item.getCategory().toUpperCase());
            }
        }                       
        
        return view;
    }

}