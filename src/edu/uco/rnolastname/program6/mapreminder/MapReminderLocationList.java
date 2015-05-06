package edu.uco.rnolastname.program6.mapreminder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.DropBoxManager.Entry;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.app.MainActivity;
import edu.uco.rnolastname.program6.dbutilities.Task;

public class MapReminderLocationList extends DialogFragment implements AdapterView.OnItemClickListener{
	OnLocationListItemClickListener mCallback;
	private static Activity activity;
	private static View v;
	private static ListView locationList;
	
	private static Task task;
	private static String taskName;	
	private static int taskSession;
			
	private static double latitude;
	private static double longitude;
	
	private String marker;
	private String latlong;
	private static List<String> markers = new ArrayList<String>();
	private static List<String> latlongs = new ArrayList<String>();		
	
	public interface OnLocationListItemClickListener{
		public void onLocationClick(int position);
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		this.activity = activity;
		
		try{
			mCallback = (OnLocationListItemClickListener) activity;
		}catch(ClassCastException e){
			throw new ClassCastException(activity.toString() + " must implement OnLocationListItemClickListener");
		}
	}
	
	public static MapReminderLocationList newInstance(Task task){
		MapReminderLocationList f = new MapReminderLocationList();
		
		Bundle args = new Bundle(2);	
				
		args.putParcelable("task", task);		
		f.setArguments(args);
		return f;	
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);		
					
		if(markers.size() != 0){
			markers.clear();
		}
		
		for(Marker mark: MapReminder.markers){
			markers.add(mark.getTitle());			
		}		
		
		// display Task name in the title
		task = getArguments().getParcelable("task");			
		
		//get session
		taskSession = getArguments().getInt("session");
	}
	
	@Override 
	public Dialog onCreateDialog(Bundle savedStateInstance){
		v = activity.getLayoutInflater().inflate(R.layout.mapreminder_locationlistfragment, null);							
	
		locationList = (ListView) v.findViewById(R.id.mapreminder_locationlist);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(v.getContext(),
				R.layout.mapreminder_singlelocationlistitem,R.id.mapreminder_locationlistitem, 
				markers);				
		locationList.setAdapter(adapter);
		locationList.setOnItemClickListener(this);		
		
		AlertDialog.Builder locationListBuilder= new AlertDialog.Builder(v.getContext());
		
		locationListBuilder.setView(v);
					
		if(task == null){
			locationListBuilder.setTitle("Location list");
			locationListBuilder.setMessage("You do not have any active task");
			locationListBuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();					
				}
			});
		}else{
			String title = "";
			if(!MapReminder.addingLocation){
				//previous selected task
				title += "Task: " + task.getTaskName() 
						+ ",  Priority: " + task.getPriority() + ",  Reminder: "  
						+ MapReminder.taskReminderIds.get(task.getReminderId()-1);
				locationListBuilder.setTitle(title);	
				locationListBuilder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();					
					}
				});
			}else if(MapReminder.addingLocation && (task.getReminderId() == -1 || task.getPriority() == -1)){
				title += "Task: " + task.getTaskName() + "  Priority and Reminder type has not been set";
				locationListBuilder.setTitle(title);					
				locationListBuilder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();					
					}
				});			
			}else{
				title += "Task: " + task.getTaskName() 
						+ ",  Priority: " + task.getPriority()
						+ ",  Reminder: " + MapReminder.taskReminderIds.get(task.getReminderId()-1);				
				
				//in the middle of adding task or just done adding task
				locationListBuilder.setTitle(title);
				locationListBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						//one task has many markers and locations
						String locationJSON = "";
						String markerJSON = "";
						if(task != null){
							locationJSON = task.convertMarkerLatlongToJson(MapReminder.markersLatlong);
							markerJSON = task.convertMarkerNameToJson(MapReminder.markersLatlong);
							
							if(!locationJSON.equals("") && !markerJSON.equals("")){
								task.setMapLatlongName(markerJSON);
								task.setMapLatlong(locationJSON);
							}else{
								Log.d("DEBUG","Error - MapReminderLocationList - location and marker convert to JSON is empty");
							}															
						}				
						
						if(task.getDate() == null){
							Toast.makeText(v.getContext(), "Please select task date", Toast.LENGTH_SHORT).show();
							dialog.dismiss();
						}
						else{
							MapReminder.taskDataLoader.insert(task);
							MapReminder.taskDataLoader.commitContentChanged();					
							MapReminder.mActionMode.invalidate();
							Toast.makeText(activity.getApplicationContext(), 
									"Task is saved", Toast.LENGTH_SHORT).show();
							
							MapReminder.addingLocation = false;						
																																																		
							//set prevlatlong to be null
							MapReminder.prevLatlong = null;
							
							//set total distance to 0
							MapReminder.totalDistance = 0;
						}																														
					}
				}).setNegativeButton("Discard", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(v.getContext(), "Task discarded", Toast.LENGTH_SHORT).show();				
					}
				});
			}
		}										
		return locationListBuilder.create();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO what to do when an item is clicked??
		OnLocationListItemClickListener act = (OnLocationListItemClickListener) getActivity();
		act.onLocationClick(position);
	}
}
