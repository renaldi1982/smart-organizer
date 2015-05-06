package edu.uco.rnolastname.program6.mapreminder;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.dbutilities.Task;

public class MapReminderTaskList extends DialogFragment implements AdapterView.OnItemClickListener{
	OnTaskListItemClickListener mCallback;
	
	//Views
	private static Activity activity;
	private static View v;
	
	private static List<Task> tasks = new ArrayList<Task>();
	private static ArrayAdapter<Task> taskListAdapter = null;
//	private static List<String> taskNames = new ArrayList<String>();	
	private static ListView taskList;
	private static Dialog dialog;
	
	public interface OnTaskListItemClickListener{
		public void onTaskClick(Task task, Dialog dialog);
	}
	
	public void onAttach(Activity activity){
		super.onAttach(activity);
		this.activity = activity;
		
		try{
			mCallback = (OnTaskListItemClickListener) activity;
		}catch(ClassCastException e){
			throw new ClassCastException(activity.toString() + " must implement OnTaskListItemClickListener");
		}
	}
	
	static MapReminderTaskList newInstance(ArrayList<Task> tasks){
		MapReminderTaskList f = new MapReminderTaskList();	
		
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList("tasks", tasks);
		f.setArguments(bundle);
		return f;	
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		tasks = getArguments().getParcelableArrayList("tasks");		
		
	}		
	
	@Override 
	public Dialog onCreateDialog(Bundle savedStateInstance){		
		taskListAdapter = MapReminder.taskListAdapter;
		
		v = activity.getLayoutInflater().inflate(R.layout.mapreminder_tasklistfragment, null);							
				
		taskList = (ListView) v.findViewById(R.id.mapreminder_tasklist);		
						
		taskList.setAdapter(taskListAdapter);		
		taskList.setOnItemClickListener(this);
		
		AlertDialog.Builder locationList = new AlertDialog.Builder(v.getContext());
		locationList.setTitle("Task");		
		if(MapReminder.taskListAdapter.getCount() != 0){
			locationList.setView(v);
		}else{
			locationList.setMessage("You don't have any tasks");
		}
		
		locationList.setPositiveButton("Close", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();				
			}
		});
		dialog = locationList.create();
		return dialog;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {		
		// TODO what to do when an item is clicked??
		OnTaskListItemClickListener act = (OnTaskListItemClickListener) getActivity();
		
		act.onTaskClick(tasks.get(position),dialog);		
	}
}
