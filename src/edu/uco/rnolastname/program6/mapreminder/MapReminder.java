package edu.uco.rnolastname.program6.mapreminder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.app.MainActivity;
import edu.uco.rnolastname.program6.calendar.FragmentCalendar;
import edu.uco.rnolastname.program6.dbutilities.Account;
import edu.uco.rnolastname.program6.dbutilities.SQLiteTaskDataLoader;
import edu.uco.rnolastname.program6.dbutilities.Task;
import edu.uco.rnolastname.program6.dbutilities.TaskDataSource;
import edu.uco.rnolastname.program6.utilities.CustomArrayAdapter;
/*
 * ACTIVITY: MapReminder
 * PURPOSE: Allow user to pick points on map and setup a task based on the locations selected
 * PROGRESS: 90% 
 * TODO: finish setting up date and make sure loader working correctly after adding task. 
 */

public class MapReminder extends Activity implements ConnectionCallbacks,
OnConnectionFailedListener,LocationListener,OnMapLongClickListener,MapReminderLocationList.OnLocationListItemClickListener,
MapReminderTaskList.OnTaskListItemClickListener,LoaderManager.LoaderCallbacks<List<Task>>,
FragmentCalendar.OnDateSelectedListener,OnMyLocationButtonClickListener{	
	//constant
	private static final String GOOGLEMAP_TAG = "googlemap";
	private static final String TASKLIST_TAG = "tasklist";
	private static final String LOCATIONLIST_TAG = "locationlist";		
	
	//active account
	private static Account activeAcc = null;
			
	//main map
	private MapFragment mMapFragment;
	private FragmentManager fm = getFragmentManager();	
	private static GoogleMapOptions options = new GoogleMapOptions();
	private static GoogleMap mMap;	
	
	//tasks adder list of array list of tasks latitude and longitude
	private static Task activeTask = null;
	private static Marker myLocationMarker = null;	
	private static Marker myHomeMarker = null;
	private int locationCounter;	
	public static boolean addingLocation = false;	
	private static String taskValue;
	private static String markerValue;
	private static int taskPriority = -1;
	private static int taskReminderID = -1;
	private static LatLng addressLookupLatlong = null;
	private static TaskDataSource taskDataSource;
	protected static SQLiteTaskDataLoader taskDataLoader;	
	public static List<String> taskReminderIds = new ArrayList<String>();
	
	//public variables	
	public static ArrayList<Task> tasks = new ArrayList<Task>();
	public static ArrayList<String> taskNames;		
	public static String taskDate;
	public static ArrayAdapter<Task> taskListAdapter;	
	
	//lines and markers
	private boolean useDefault = false;
	private static Marker marker;	
	private static Polyline line;		
	private static List<Polyline> lines = new ArrayList<Polyline>();
	public static List<Marker> markers = new ArrayList<Marker>();
	public static ArrayList<HashMap<String,LatLng>> markersLatlong = new ArrayList<HashMap<String,LatLng>>();
		
	//location		
	private static LatLng myLocationLatlong = null;
	private static LatLng myHomeLatlong = null;
	public static LatLng prevLatlong = null;	
	private static LocationClient location;
	private static LocationRequest lRequest;
	private static LatLng latLng;		
	public static double totalDistance = 0;
	public static ArrayList<Double> distances = new ArrayList<Double>();
	
	//layout
	private Activity activity = this;	
	public static ActionMode mActionMode;
	
	//date picker
	private FragmentCalendar fc;
	private static FrameLayout calendar_container;
	private RelativeLayout mapReminderLayout;		
		
	/* LOADER MANAGER */
	@Override
	public Loader<List<Task>> onCreateLoader(int id, Bundle args) {					
		//Log.d("DEBUG","oncreateloader active acc null? " + String.valueOf(activeAcc == null));
		taskDataLoader = new SQLiteTaskDataLoader(this,taskDataSource,
				TaskDataSource.TASK_CATEGORY + " LIKE ? AND " 
				+ TaskDataSource.TASK_ACCOUNT_ID + " = ?",
				new String[] {"%mapreminder%",String.valueOf(activeAcc.getId())},null,null, null);
		return taskDataLoader;
	}

	@Override
	public void onLoadFinished(Loader<List<Task>> loader, List<Task> data) {
		handleResultOnLoadFinished(data);
	}

	@Override
	public void onLoaderReset(Loader<List<Task>> loader) {		
		taskListAdapter.clear();				
	}
	/* LOADER MANAGER */
	
	/* ACTIVITY */
	@Override
	public void onCreate(Bundle savedInstanceState) {		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mapreminder);	    	    	    	    	    	    	    	    
	    
	    //set adapter for task list
	    taskListAdapter = new ArrayAdapter<Task>(this,
				R.layout.mapreminder_singletasklistitem,R.id.mapreminder_tasklistitem, 
				tasks);
	    
	    //setup Loader and task data source
  		MapReminder.taskDataSource = MainActivity.taskDataSource;  		    		
  		
	    Intent i = getIntent();
	    if(i.getExtras() == null){
	    	this.finish();
	    	overridePendingTransition(android.R.animator.fade_in,android.R.animator.fade_out);
	    }	    	    	    	    
	    
	    //get account and task
	    Bundle acc = i.getExtras();
	    activeAcc = acc.getParcelable("activeAcc");	    
	    activeTask = acc.getParcelable("task");	    	    	    
	    
	    getLoaderManager().initLoader(MainActivity.MAPREMINDER_ACIVITY_LOADER_ID, null, this);	
	    	    	    
	    mapReminderLayout = (RelativeLayout) findViewById(R.id.mapreminder_layout);
	    	    	    
	    ActionBar action = getActionBar();
	    action.setDisplayHomeAsUpEnabled(true);	
	    
	    options.mapType(GoogleMap.MAP_TYPE_NORMAL)
		.compassEnabled(true);	
	    	    	    
	    mMapFragment = MapFragment.newInstance(options);			
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(R.id.mapcontainer,mMapFragment,GOOGLEMAP_TAG);
		ft.commit();											
		
		startActionMode(amCallback);				
		
		if(MapReminder.taskDataSource == null){
			Log.d("DEBUG","Error - MapReminder - taskDataSource is null");
		}
		
		if (getLoaderManager().getLoader(MainActivity.MAPREMINDER_ACIVITY_LOADER_ID) == null) {
	          Log.i("DEBUG", "+++ Initializing the new Loader MapReminder... +++");
		} else {
	          Log.i("DEBUG", "+++ Reconnecting with existing Loader MapReminder(id '2')... +++");
	    }	
		
		//check if task already exists
		if(taskExists(activeTask)){
			loadSelectedTask();
		}else{
			//creating new map reminder
			if(activeTask.getCategory() != null && !activeTask.getCategory().equalsIgnoreCase("")){
				activeTask.setCategory(activeTask.getCategory() + MainActivity.pictureReminder);
			}else{
				activeTask.setCategory(MainActivity.pictureReminder);
			}
			
		}
	}		
	
	@Override 
	public void onStart(){		
		super.onStart();
				
		mMap = this.mMapFragment.getMap();		
		
		mMap.setOnMapLongClickListener(this);
		mMap.setMyLocationEnabled(true);
		
		mMap.setOnMyLocationButtonClickListener(this);
		
		//if we want to pad the map
		//mMap.setPadding(0, 0, 256, 0);					
	}		
	
	@Override
	public void onResume() {
		super.onResume();		
	}
	
	@Override 
	protected void onActivityResult(int resultCode, int responseCode, Intent data){
		Log.d("DEBUG","result code: " + resultCode +"\n" + "responseCode: " 
				+ responseCode + "\n" + " Data is null? " + String.valueOf(data == null));
	}
			
	@Override
	public void onRestart() {
		super.onRestart();
		Log.i("DEBUG", "activity onRestart");
	}
	
	@Override
	public void onPause(){
		super.onPause();
		location.removeLocationUpdates(this);
	}	
	
	@Override 
	public void onStop(){
		super.onStop();
		Log.i("DEBUG", "activity onStop");
	}
	/* ACTIVITY */
	
	/* HANDLER CALLBACK FOR LOCATION LIST AND TASK LIST */
	@Override
	public void onLocationClick(int position) {
		if(markers.size() != 0){
			LatLng selectedPosition = markers.get(position).getPosition();
			
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
					selectedPosition,17));
		}else{
			Toast.makeText(getApplicationContext(), "No active markers", Toast.LENGTH_SHORT).show();
		}			
	}		
	
	@Override
	public void onTaskClick(Task task, Dialog dialog) {
		activeTask = task;						
		
		loadSelectedTask();		
		dialog.dismiss();		
	}
	/* END HERE */		
	
	//TODO adding task date on pressing DONE
	public ActionMode.Callback amCallback = new ActionMode.Callback() {		
		MenuItem itemDone,itemBack;
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			mActionMode = mode;
			String title = "";
			if(activeTask != null){
				if(activeTask.getTaskName() != null && !activeTask.getTaskName().equals("")){
					title += "  Task: " + activeTask.getTaskName() + "   ";
				}
				
				if(totalDistance != 0){
					title += "Distance: " + String.format("%.2f", totalDistance) + " miles" + "   ";
				}																
				
				if(activeTask.getDate() != null){
					if(activeTask.getDate() != null){
						title += "Date: " + activeTask.getDate() + "   ";
					}
				}
								
				mode.setTitle(title);
			}else{
				mode.setTitle("  No Task selected");
			}
			if(!addingLocation){
				itemDone.setVisible(false);
				itemDone.setEnabled(false);
				itemBack.setVisible(true);
				itemBack.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
				itemDone.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
			}else{
				itemDone.setVisible(true);
				itemDone.setEnabled(true);				
				itemBack.setVisible(false);
				itemDone.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
				itemBack.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
			}
			return true;			
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {			
			activity.finish();
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) { 
			getMenuInflater().inflate(R.menu.mapreminder_contextmenu, menu);	
			itemDone = menu.findItem(R.id.mapreminder_done);
			itemBack = menu.findItem(R.id.mapreminder_back);
			
			return true;
		}				
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			
			switch(item.getItemId()){
			case R.id.mapreminder_tasklist:				
				mode.invalidate();				
				showFormTaskList();
				break;
			case R.id.mapreminder_locationlist:				
				mode.invalidate();
				if(activeTask != null && activeAcc != null && addingLocation){					
					showFormLocationList();
				}else{					
					showFormLocationList();					
				}				
				break;
			case R.id.mapreminder_done:				
				mode.invalidate();
				if(addingLocation){
					//TODO set all adding location variables and reset all markers
				}
				break;
			case R.id.mapreminder_addresslookup:				
				mActionMode.invalidate();
				addressLookup();
				if(addingLocation){
					onMapLongClick(addressLookupLatlong);
				}
				break;
			case R.id.mapreminder_back:				
				mActionMode.invalidate();
				if(!addingLocation){
					Intent i = new Intent();
					i.putExtra("task", activeTask);
					MapReminder.this.setResult(RESULT_OK,i);
					MapReminder.this.finish();
				}												
				break;		
			case R.id.mapreminder_calendar:				
				mActionMode.invalidate();
				openCalendar();		
				break;
			case R.id.mapreminder_changeview:						
				mActionMode.invalidate();
				int mapType = mMap.getMapType();						
				mapType = (mapType == 4) ? mapType = 0 : mapType + 1;				
				mMap.setMapType(mapType);							
				break;
			default:
				return false;
			}			
			return true;
		}
	};	
	
	
	
	//Go to Home Address
	private void goHome(){
		LatLng homeLatlong = getAddressLatlong(activeAcc.getAddress()
				+ "," + activeAcc.getCity() + "," + activeAcc.getState());
		
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(homeLatlong,17));
		
		if(myHomeMarker == null){
			myHomeMarker = mMap.addMarker(new MarkerOptions().position(homeLatlong).title("My Home"));
		}
		
		myHomeLatlong = homeLatlong;
	}
	
	private void gotoMylocation(){				
		location = new LocationClient(this,this,this);
		location.connect();
						
		if(mMap != null && location.isConnected()){
			
			lRequest = LocationRequest.create();
			lRequest.setInterval(600000);
			location.requestLocationUpdates(lRequest,this);						
		}
	}
	
	@Override
	public void onLocationChanged(Location location) {		
		
	}
		
	@Override
	public void onConnectionFailed(ConnectionResult conn) {
		if(conn != null)
			Toast.makeText(getApplicationContext(), 
					"Failed to connect to Google Map", 
					Toast.LENGTH_SHORT).show();		
	}
		
	@Override
	public void onConnected(Bundle arg0) {	

		goHome();
		Log.d("DEBUG","on connected");
		String username = activeAcc.getUsername().substring(0,1).toUpperCase() + activeAcc.getUsername().substring(1);
		Toast.makeText(getApplicationContext(), 
				"Welcome to Map Reminder " + username + "!\n" +
				"To start a new task\n" +
				"long press anywhere on the map ", Toast.LENGTH_LONG).show();				
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(getApplicationContext(), 
				"Closing Map Reminder...", 
				Toast.LENGTH_SHORT).show();	
		
	}
	
	@Override
	public boolean onMyLocationButtonClick() {
		Log.d("DEBUG","on my location button click");
		//gotoMylocation();
		Location myLocation = location.getLastLocation();
		LatLng myLatlong = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
		
		if(myLocation != null){
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
    				myLatlong,17));    
		}
		
		if(myLocationMarker == null){
			myLocationMarker = mMap.addMarker(new MarkerOptions().position(myLatlong).title("My Location"));			
		}
				
		myLocationLatlong = myLatlong;
		return false;
	}	
	
	private boolean isMarkerExists(LatLng position){
		boolean exists = false;
		
		for(Marker m: markers){
			if(m.getPosition() == position){
				exists = true;
			}
		}
		
		return exists;
	}
	
	private void addressLookup(){		
		
		final EditText locationText = new EditText(this);		
		locationText.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
		locationText.setTextColor(Color.BLACK);
		locationText.setBackgroundColor(Color.WHITE);				
		locationText.setHint("Street,City,State");
		
		AlertDialog.Builder addressLookupBuilder = new AlertDialog.Builder(this);
		addressLookupBuilder.setTitle("Address Lookup");
		addressLookupBuilder.setMessage("Enter address");
		addressLookupBuilder.setView(locationText);
		addressLookupBuilder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(!locationText.getText().toString().trim().equalsIgnoreCase("")){
					boolean markerExist = false;
					String locationName = locationText.getText().toString();
					LatLng addressLatlong = getAddressLatlong(locationName);
					addressLookupLatlong = addressLatlong;
					if(addressLatlong != null){
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(addressLatlong,17));
						
						markerExist = isMarkerExists(addressLatlong);
						
						if(!markerExist && myLocationMarker.getPosition() != addressLatlong){
							mMap.addMarker(new MarkerOptions().position(addressLatlong).title(locationName));
						}
						
//						if(addingLocation){
//							setMarkerName(addressLatlong);
//						}
						
						Toast.makeText(getApplicationContext(), 
								"Long click on the map to start a new task", Toast.LENGTH_SHORT).show();
					}	
				}else{					
					Toast.makeText(getApplicationContext(), 
							"Please enter address", Toast.LENGTH_SHORT).show();
				}				
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
			}
		});								
		
		addressLookupBuilder.show();		
	}
	
	private LatLng getAddressLatlong(String locationName){				
		Geocoder gc = new Geocoder(this);
		
		List<Address> list = new ArrayList<Address>();
		
		try {
			list = gc.getFromLocationName(locationName, 1);
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("DEBUG","Error - MapReminder - Input Output exception");					
		}
		
		LatLng addressLatlong = null;
		
		if(list.size() != 0){
			Address address = list.get(0);
			addressLatlong = new LatLng(address.getLatitude(),address.getLongitude());
		}
		
		return addressLatlong;		
	}
	
	private boolean taskExists(Task task){
		boolean exists = false;
		
		//check if the task already exists
	    if(tasks.size() != 0){
	    	for(Task t: tasks){
	    		if(activeTask.getId() == t.getId()){
	    			//task already exists
	    			exists = true;		
	    		}
	    	}
	    }
	    return exists;
	}
	
	private void loadSelectedTask(){
		//clear markers and polylines if theres any 
		if(markers.size() != 0){
			clearAllMarkers();
		}
		
		if(lines.size() != 0){
			clearAllLines();
		}
		
		//create markers and polyline
		ArrayList<String> markerName = new ArrayList<String>(); 
		ArrayList<LatLng> latlong = new ArrayList<LatLng>();
		
		markerName = activeTask.getArrayMarkerTitle();
		latlong = activeTask.getArrayLatLng();		
		
		for(int i=0;i<markerName.size() && markerName.size() == latlong.size();i++){	
			Marker marker = mMap.addMarker(new MarkerOptions().position(latlong.get(i)).title(markerName.get(i))); 
			markers.add(marker);
			
			//drawing polygon
			if(i != (markerName.size()-1)){
				drawPath(latlong.get(i).latitude,latlong.get(i).longitude,
						latlong.get(i+1).latitude,latlong.get(i+1).longitude);
				
				//get distance between markers
				double distance = getDistance(latlong.get(i),latlong.get(i+1));
				
				distances.add(distance);
				
				//count total distance
				totalDistance += distance;
			}
		}
		
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
				latlong.get(0),17));
		Log.d("DEBUG","marker size: " + markers.size());
		mActionMode.invalidate();	
	}
		
	private void handleResultOnLoadFinished(List<Task> data){
		taskListAdapter.clear();
		for(Task t: data){
			taskListAdapter.add(t);
		}
		tasks = (ArrayList<Task>) data;
		
	}
	
	//TODO correctly implement Loader Manager so we dont have to create new adapter every time
	//show fragment task list
	private void showFormTaskList(){										
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag(TASKLIST_TAG);
		
		if(prev != null){
			ft.remove(prev);
		}
		ft.addToBackStack(null);					
		
		DialogFragment taskList = MapReminderTaskList.newInstance(tasks);
		
		taskList.show(ft, TASKLIST_TAG);
	}
	
	//show fragment location list
	private void showFormLocationList(){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag(LOCATIONLIST_TAG);
		
		if(prev != null){
			ft.remove(prev);
		}		
		ft.addToBackStack(null);
		
		DialogFragment locationList = MapReminderLocationList.newInstance(activeTask);
		locationList.show(ft, LOCATIONLIST_TAG);				
	}
			
	private void clearAllMarkers(){
		for(Marker marker: markers){
			marker.remove();
		}
		
		markers.clear();
	}
	
	private void clearAllLines(){
		for(Polyline line: lines){
			line.remove();
		}
		
		lines.clear();
	}						
	
	/* SET TASK DATE */
	@Override
	public void onDateSelected(Task task) {
		
		if(task != null){			
			activeTask = task;
			openCalendar();
			mActionMode.invalidate();
			Toast.makeText(getApplicationContext(), 
					"Task date has been set", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getApplicationContext(), 
					"No Task to set the date to", Toast.LENGTH_SHORT).show();
		}
		
	}	
	
	public void openCalendar(){		
		if(calendar_container == null){
			calendar_container = new FrameLayout(this);
			calendar_container.setId(MainActivity.CALENDAR_CONTAINER_ID);
			
			FrameLayout.LayoutParams mlp = new FrameLayout
					.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
			
			mapReminderLayout.addView(calendar_container,mlp);			
		}
		
		Fragment f = getFragmentManager().findFragmentByTag(MainActivity.CALENDAR_FRAGMENT_TAG);
        if (f != null) {
            getFragmentManager().popBackStack();      
            mapReminderLayout.removeView(calendar_container);	
        } else {        	
        	fc = FragmentCalendar.newInstance(activeTask);
    		getFragmentManager().beginTransaction()
    			.setCustomAnimations(R.animator.slide_left, R.animator.slide_right,
    					R.animator.slide_left, R.animator.slide_right)          
                .add(MainActivity.CALENDAR_CONTAINER_ID,fc,
                		MainActivity.CALENDAR_FRAGMENT_TAG
                ).addToBackStack(null).commit();    		
        }               
	}
	/* END SET TASK DATE */
	
	private void buildMarkerLatlong(){
		if(activeTask != null && markers.size() != 0){
			for(Marker mark: markers){
				HashMap<String,LatLng> markerLatlong = new HashMap<String,LatLng>();
				markerLatlong.put(mark.getTitle(),mark.getPosition());
				
				markersLatlong.add(markerLatlong);				
			}						
		}else{
			Log.d("DEBUG","failed to " +
					"build hash map - buildMarkerLatlong - check activeTask, markers, tasksLatlong");
		}		
	}	
	
	@Override
	public void onMapLongClick(final LatLng point) {	
		
		if(mMap == null){
			Log.d("DEBUG","Error occured - MapReminder - Map is null");
			return;
		}else{		
			if(addingLocation){
				setMarkerName(point);						
				
				//invalidate action mode so it will update the distance 						
				mActionMode.invalidate();	
			}else{
				setMyLocationAsTask();
			}													
		}				
	}
			
	private void setMarkerName(final LatLng point){
		//Layout 
		LayoutInflater inflater = getLayoutInflater();
		final View viewSetMarkerName = inflater.inflate(R.layout.mapreminder_setmarkerview,null);
		final CheckBox checkDefaultName = (CheckBox) viewSetMarkerName.findViewById(R.id.chk_markername);
		final EditText markerName = (EditText) viewSetMarkerName.findViewById(R.id.txt_markername); 					
		
		if(useDefault){		
			locationCounter++;
			marker = mMap.addMarker(new MarkerOptions().position(point).title("Location " + locationCounter));
			markers.add(marker);
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(markerName.getWindowToken(), 0);
            
            /* Add lat long 
             * draw path between previous marker and current one
             * accummulate total distance
             * set this point as previous to be used to draw path in next location
             * */			
			if(prevLatlong != null){
				drawPath(prevLatlong.latitude,prevLatlong.longitude,
						point.latitude,point.longitude);
				
				double curDistance = getDistance(prevLatlong,point);
				
				//get distance
				totalDistance += curDistance;
				
				//add distance to distance array
				distances.add(curDistance);							
										
				Toast.makeText(getApplicationContext(), "Location has been added", Toast.LENGTH_SHORT).show();				
			}			
			//set this location as previous location
			prevLatlong = point;
			
			//invalidate action mode so it will have a task name as title						
			mActionMode.invalidate();
			return;
		}
		
		AlertDialog.Builder alertSetMarker = new AlertDialog.Builder(this);		
		alertSetMarker.setTitle("Marker name");
		alertSetMarker.setView(viewSetMarkerName);		
		
		alertSetMarker.setMessage("Check Don't ask again if you want to use the default name \n" +
				"Note: Pressing OK without entering the name will enable the check default name box")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(markerName.getText().toString().trim().length() == 0 && useDefault){
					
	            	locationCounter++;
	            	marker = mMap.addMarker(new MarkerOptions().position(point).title("Location " + locationCounter));
					markers.add(marker);
					
					InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		            imm.hideSoftInputFromWindow(markerName.getWindowToken(), 0);
		            
		            /* 
		             * draw path between previous marker and current one
		             * accummulate total distance
		             * set this point as previous to be used to draw path in next location
		             * */			
		            if(prevLatlong != null){
		            	drawPath(prevLatlong.latitude,prevLatlong.longitude,
								point.latitude,point.longitude);	
		            	
		            	double curDistance = getDistance(prevLatlong,point);
		            	
		            	//get distance
						totalDistance += curDistance;
						
						//add distance to distance array
						distances.add(curDistance);											
																		
						Toast.makeText(getApplicationContext(), "Location has been added", Toast.LENGTH_SHORT).show();
		            }		            	
		            //set this location as previous location
					prevLatlong = point;
					
					//invalidate action mode so it will have a task name as title						
					mActionMode.invalidate();
	            }else if(markerName.getText().toString().trim().length() != 0 && !useDefault){
	            	locationCounter++;
	            	markerValue = markerName.getText().toString();
	            	//add marker to this location and add it to the list
					marker = mMap.addMarker(new MarkerOptions().position(point)
							.title(markerValue));	
					markers.add(marker);
					
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		            imm.hideSoftInputFromWindow(markerName.getWindowToken(), 0);		            		          
		            
		            /* Add lat long 
		             * draw path between previous marker and current one
		             * accummulate total distance
		             * set this point as previous to be used to draw path in next location
		             * */					
		            if(prevLatlong != null){
		            	drawPath(prevLatlong.latitude,prevLatlong.longitude,
								point.latitude,point.longitude);		
		            	
		            	double curDistance = getDistance(prevLatlong,point);
						
						//get distance
						totalDistance += curDistance;
						
						//add distance to distance array
						distances.add(curDistance);												
																		
						Toast.makeText(getApplicationContext(), "Location has been added", Toast.LENGTH_SHORT).show();
		            }				
		            //set this location as previous location
					prevLatlong = point;
					
					//invalidate action mode so it will have a task name as title						
					mActionMode.invalidate();
	            }else{	      	            	
	            	Toast.makeText(getApplicationContext(), "You need to either" +
						" insert marker's name or check don't ask again", Toast.LENGTH_SHORT).show();				            	
	            }
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
				InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(markerName.getWindowToken(), 0);
			}
		});
		
		markerName.setOnKeyListener(new View.OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN)
		        {
		            switch (keyCode)
		            {
		                case KeyEvent.KEYCODE_DPAD_CENTER:
		                case KeyEvent.KEYCODE_ENTER:			                			                					            
				            
				            if(markerName.getText().toString().trim().length() == 0 && useDefault){
				            	locationCounter++;
				            	marker = mMap.addMarker(new MarkerOptions().position(point)
				            			.title("Location " + locationCounter));
								markers.add(marker);
								InputMethodManager imm = (InputMethodManager) activity
										.getSystemService(Context.INPUT_METHOD_SERVICE);
					            imm.hideSoftInputFromWindow(markerName.getWindowToken(), 0);
					            
					            /* Add lat long 
					             * draw path between previous marker and current one
					             * accummulate total distance
					             * set this point as previous to be used to draw path in next location
					             * */
					            if(prevLatlong != null){
					            	drawPath(prevLatlong.latitude,prevLatlong.longitude,
											point.latitude,point.longitude);						
					            	
					            	double curDistance = getDistance(prevLatlong,point);
									
									//get distance
									totalDistance += curDistance;
									
									//add distance to distance array
									distances.add(curDistance);																		
																								
									Toast.makeText(getApplicationContext(), "Location has been added", Toast.LENGTH_SHORT).show();
					            }		
					            //set this location as previous location
								prevLatlong = point;
								
								//invalidate action mode so it will have a task name as title						
								mActionMode.invalidate();
				            }else if(markerName.getText().toString().trim().length() != 0 && !useDefault){
				            	locationCounter++;
				            	markerValue = markerName.getText().toString();
				            	//add marker to this location and add it to the list
								marker = mMap.addMarker(new MarkerOptions().position(point)
										.title(markerValue));	
								markers.add(marker);
								
								InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					            imm.hideSoftInputFromWindow(markerName.getWindowToken(), 0);
					            
					            /* Add lat long 
					             * draw path between previous marker and current one
					             * accummulate total distance
					             * set this point as previous to be used to draw path in next location
					             * */	
					            if(prevLatlong != null){
					            	drawPath(prevLatlong.latitude,prevLatlong.longitude,
											point.latitude,point.longitude);
					            	double curDistance = getDistance(prevLatlong,point);
									
									//get distance
									totalDistance += curDistance;
									
									//add distance to distance array
									distances.add(curDistance);																		
																								
									Toast.makeText(getApplicationContext(), "Location has been added", Toast.LENGTH_SHORT).show();
									
									//invalidate action mode so it will have a task name as title						
									mActionMode.invalidate();
					            }
					            //set this location as previous location
								prevLatlong = point;
				            }else{				            	
				            	Toast.makeText(getApplicationContext(), "You need to either" +
									" insert marker's name or check don't ask again", Toast.LENGTH_SHORT).show();				            	
				            }
				            
							break;																
		                default:
		                    break;
		            }
		        }
		        return false;				
			}			
		});
		
		alertSetMarker.setOnKeyListener(new OnKeyListener() {						

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN){
					switch(keyCode){
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:	
						if((markerName.getText().toString().trim().length() != 0 && !useDefault) 
								|| (markerName.getText().toString().trim().length() == 0 && useDefault)){
							dialog.dismiss();
						}else{
							Toast.makeText(getApplicationContext(), "You need to either" +
									" insert marker's name or check don't ask again", Toast.LENGTH_SHORT).show();
						}
						break;
					default:
						break;
					}
				}
				return false;
			}
		});
		
		checkDefaultName.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				if(markerName.isEnabled()){
					markerName.setEnabled(false);						
					useDefault = true;					
				}else{
					markerName.setEnabled(true);
					useDefault = false;
				}
					
			}
		});		
		alertSetMarker.show();						
	}
		
	private void setMyLocationAsTask(){
		
		AlertDialog.Builder setMyLocationAsTaskBuilder = new AlertDialog.Builder(this);
		setMyLocationAsTaskBuilder.setTitle("Starting point");
		setMyLocationAsTaskBuilder.setMessage("Please choose starting point?");
		setMyLocationAsTaskBuilder.setPositiveButton("Home", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//clear lines and markers
				if(lines.size() != 0){
					clearAllLines();
				}
				
				if(markers.size() != 0){
					clearAllMarkers();
				}
				
				if(useDefault){
					useDefault = false;
				}
				
				goHome();
				prevLatlong = myHomeLatlong;				
				markers.add(myHomeMarker);
				
				addingLocation = true;												
			}
		}).setNeutralButton("My Location", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {								
				//clear lines and markers
				if(lines.size() != 0){
					clearAllLines();
				}
				
				if(markers.size() != 0){
					clearAllMarkers();
				}
				
				if(useDefault){
					useDefault = false;
				}
				
				onMyLocationButtonClick();
				prevLatlong = myLocationLatlong;
				markers.add(myLocationMarker);
				
				addingLocation = true;
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {	
				prevLatlong = null;			
			}
		}).show();
		
	}
			
	private void drawPath(double srcLat, double srcLong, double destLat, double destLong){		
		String url = makeURL(srcLat,srcLong,destLat,destLong);		
		
		CreatePath draw = new CreatePath(url);		
		draw.execute(url);
	}
	
	//Convert lat long to URL format
	public String makeURL (double srcLat, double srcLong, double destLat, double destLong ){
        StringBuilder urlString = new StringBuilder();
        
        urlString.append("http://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(srcLat));
        urlString.append(",");
        urlString.append(Double.toString( srcLong));
        urlString.append("&destination=");// to
        urlString.append(Double.toString( destLat));
        urlString.append(",");
        urlString.append(Double.toString( destLong));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        
        return urlString.toString();
	}
			
	private static double getDistance(LatLng StartP, LatLng EndP) {
	    double lat1 = StartP.latitude;
	    double lat2 = EndP.latitude;
	    double lon1 = StartP.longitude;
	    double lon2 = EndP.longitude;
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLon = Math.toRadians(lon2-lon1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	    Math.sin(dLon/2) * Math.sin(dLon/2);
	    double c = 2 * Math.asin(Math.sqrt(a));
	    
	    // return distance in miles
	    return 3955.64901 * c;
	}
	
	/* DRAWING PATH BETWEEN MARKERS */
	private class CreatePath extends AsyncTask<String, Void, String>{
		private ProgressDialog progressDialog;
	    String url;
	    
	    CreatePath(String urlPass){
	        url = urlPass;
	    }
	    
	    @Override
		protected void onPreExecute(){
			super.onPreExecute();
			progressDialog = new ProgressDialog(MapReminder.this);
	        progressDialog.setMessage("Drawing path, Please wait...");
	        progressDialog.setIndeterminate(true);
	        progressDialog.show();	        	        
		}			    
	    
		@Override
		protected String doInBackground(String... urls){			
			JSONParser jParser = new JSONParser();
	        String json = jParser.getJSONFromUrl(url);
	        return json;			
		}
		
		@Override 
		protected void onPostExecute(String result){
			super.onPostExecute(result);
			
			progressDialog.cancel();        
	        if(result!=null){
	            drawPath(result);
	        }
		}	    	   
		
		//drawing the path
		public void drawPath(String result) {

		    try {
		            //Tranform the string into a json object
		           final JSONObject json = new JSONObject(result);
		           JSONArray routeArray = json.getJSONArray("routes");
		           JSONObject routes = routeArray.getJSONObject(0);
		           JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
		           String encodedString = overviewPolylines.getString("points");
		           List<LatLng> list = decodePoly(encodedString);

		           for(int z = 0; z<list.size()-1;z++){
		                LatLng src= list.get(z);
		                LatLng dest= list.get(z+1);
		                line = mMap.addPolyline(new PolylineOptions()		                
		                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
		                .width(8)
		                .color(Color.BLACK).geodesic(true));		                
		                lines.add(line);
		            }

		    } 
		    catch (JSONException e) {
		    }
		} 			
		
		private List<LatLng> decodePoly(String encoded) {

		    List<LatLng> poly = new ArrayList<LatLng>();
		    int index = 0, len = encoded.length();
		    int lat = 0, lng = 0;

		    while (index < len) {
		        int b, shift = 0, result = 0;
		        do {
		            b = encoded.charAt(index++) - 63;
		            result |= (b & 0x1f) << shift;
		            shift += 5;
		        } while (b >= 0x20);
		        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
		        lat += dlat;

		        shift = 0;
		        result = 0;
		        do {
		            b = encoded.charAt(index++) - 63;
		            result |= (b & 0x1f) << shift;
		            shift += 5;
		        } while (b >= 0x20);
		        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
		        lng += dlng;

		        LatLng p = new LatLng( (((double) lat / 1E5)),
		                 (((double) lng / 1E5) ));
		        poly.add(p);
		    }

		    return poly;
		}
		
	}			
	

	private static class JSONParser {

	    static InputStream inputStream = null;
	    static JSONObject jObj = null;
	    static String json = "";
	    
	    // constructor
	    public JSONParser() {
	    }
	    
	    //processing json request 
	    public String getJSONFromUrl(String url) {

	        // Making HTTP request
	        try {
	            // defaultHttpClient
	            DefaultHttpClient httpClient = new DefaultHttpClient();
	            HttpPost httpPost = new HttpPost(url);

	            HttpResponse httpResponse = httpClient.execute(httpPost);
	            HttpEntity httpEntity = httpResponse.getEntity();
	            inputStream = httpEntity.getContent();           

	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        try {
	            BufferedReader reader = new BufferedReader(new InputStreamReader(
	            		inputStream, "iso-8859-1"), 8);
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }

	            json = sb.toString();
	            inputStream.close();
	        } catch (Exception e) {
	            Log.e("Buffer Error", "Error converting result " + e.toString());
	        }
	        
	        //return json response from google
	        return json;
	    }
	}	
	/* END HERE */
	
}

//old code onmaplongclick
//AlertDialog.Builder alertTaskAdd = new AlertDialog.Builder(this);
//alertTaskAdd.setMessage("Add Location to task?")
//.setPositiveButton("Add", new DialogInterface.OnClickListener() {
//	
//	@Override
//	public void onClick(DialogInterface dialog, int which) {																																								
//		
//		setMarkerName(point);						
//																				
//		//invalidate action mode so it will update the distance a task name as title						
//		mActionMode.invalidate();	
//	}
//})				
//.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//	
//	@Override
//	public void onClick(DialogInterface dialog, int which) {					
//		return;					
//	}
//}).show();
//
//locationCounter = 0;				
//AlertDialog.Builder alertTaskAdd = new AlertDialog.Builder(this);
//alertTaskAdd.setMessage("Start adding location?")
//.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//	
//	@Override
//	public void onClick(DialogInterface dialog, int which) {						
//		//set active task to null
//		activeTask = null;						
//		
//		//reset useDefault to false
//		useDefault = false;												
//		
//		//clear lines and markers
//		if(lines.size() != 0){
//			clearAllLines();
//		}
//		
//		if(markers.size() != 0){
//			clearAllMarkers();
//		}
//		
//		if(useDefault){
//			useDefault = false;
//		}
//		
//		addingLocation = true;
//		
//		//check if user wants to include my location in the route
//		setMyLocationAsTask();																		
//	}
//})
//.setNegativeButton("No", new DialogInterface.OnClickListener() {
//	
//	@Override
//	public void onClick(DialogInterface dialog, int which) {
//		addingLocation = false;
//		return;
//	}
//}).show();

////old code set task name reminder type and priority
//private void setReminderID(){
//	ArrayAdapter<String> reminderIdAdapter = null;
//	final ListView taskReminderList = new ListView(this);		
//	if(taskReminderIds.size() != 0){
//		reminderIdAdapter = new ArrayAdapter<String>(this,
//				android.R.layout.simple_list_item_1,taskReminderIds);
//		taskReminderList.setAdapter(reminderIdAdapter);
//	}				
//	
//	final AlertDialog.Builder taskReminderIdBuilder = new AlertDialog.Builder(this);
//	taskReminderIdBuilder.setTitle("How do you want to be reminded?");
//	taskReminderIdBuilder.setView(taskReminderList);
//	taskReminderIdBuilder.setCancelable(false);	
//		
//	final DialogInterface taskReminderIdDialog = taskReminderIdBuilder.show();;
//	
//	
//	taskReminderList.setOnItemClickListener(new OnItemClickListener(){
//
//		@Override
//		public void onItemClick(AdapterView<?> parent, View view,
//				int position, long id) {				
//			taskReminderID = position + 1;		
//			
//			if(taskReminderID != -1 && taskReminderID != 0){					
//				activeTask.setReminderId(taskReminderID);	
//				taskReminderIdDialog.dismiss();
//				showFormLocationList();					
//				Log.d("DEBUG","MapReminder - setReminderID " +
//						"after show form locationlist taskListAdapter count: " + taskListAdapter.getCount());
//				//build marker latlong
//				buildMarkerLatlong();
//				
//			}	
//		}			
//	});
//	
//}

//private void setPriority(){		
//	final NumberPicker np = new NumberPicker(this);
//	np.setMaxValue(3);
//	np.setMinValue(1);				
//	
//	taskPriority = np.getValue();
//	
//	np.setOnValueChangedListener(new OnValueChangeListener(){
//
//		@Override
//		public void onValueChange(NumberPicker picker, int oldVal,
//				int newVal) {
//			taskPriority = newVal;				
//		}			
//	});				
//	
//	AlertDialog.Builder priorityDialog = new AlertDialog.Builder(this);
//	priorityDialog.setTitle("Set Priority");
//	priorityDialog.setMessage("1:Urgent - 2: Regular - 3: Not Important");
//	priorityDialog.setView(np);
//	priorityDialog.setCancelable(false);
//	
//	priorityDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//		
//		@Override
//		public void onClick(DialogInterface dialog, int which) {
//			activeTask.setPriority(taskPriority);
//			
//			setReminderID();
//		}
//	})
//	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//		
//		@Override
//		public void onClick(DialogInterface dialog, int which) {
//			Toast.makeText(getApplicationContext(), 
//					"Priority is set to: " + taskPriority, Toast.LENGTH_SHORT).show();
//			
//		}
//	});
//	
//	AlertDialog dialog = priorityDialog.create();
//	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//	dialog.show();
//}
//
//private void setTaskName(final LatLng point){		
//	LayoutInflater inflater = getLayoutInflater();
//	final View viewSetTaskName = inflater.inflate(R.layout.mapreminder_settasknameview,null);				
//	final EditText taskName = (EditText) viewSetTaskName.findViewById(R.id.txt_taskname);				
//			
//	final AlertDialog.Builder alertAddTaskName = new AlertDialog.Builder(this);
//	alertAddTaskName.setTitle("Task Name");		
//	alertAddTaskName.setMessage("Press enter or click OK when you're done")
//		.setView(viewSetTaskName)
//		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int which) {
//				taskValue = taskName.getText().toString();
//	            if (taskName.getText().toString().trim().length() == 0) {
//	                Toast.makeText(getApplicationContext(), "Please enter task name", Toast.LENGTH_SHORT).show();
//	            }else if(MainActivity.taskDataSource.isTaskNameExist(taskValue)){
//	            	Toast.makeText(getApplicationContext(), "Task name is already exists", Toast.LENGTH_SHORT).show();
//	            } else {
//	                //setup an active task 
//	                activeTask = new Task();		                
//	                activeTask.setAccountId(activeAcc.getId());
//	                activeTask.setReminderId(taskReminderID);
//	                activeTask.setTaskName(taskValue);		                
//	                activeTask.setCategory(MAPREMINDER_CATEGORY);
//	                activeTask.setPriority(taskPriority);
//	                activeTask.setAudioPath("");
//	                activeTask.setPicturePath("");	
//	                		                		                		                
//	                addingLocation = true;			                
//	                
//	                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
//		            imm.hideSoftInputFromWindow(taskName.getWindowToken(), 0);			            			            
//		            			            			            			            			            
//		            setMarkerName(point);
//		            
//		          	//invalidate action mode so it will have a task name as title												
//					Toast.makeText(getApplicationContext(), 
//	                		"New task has been created, \n" +
//	                		"you are ready to add locations to your task \n" +
//	                		"by long clicking on your next location", 
//	                		Toast.LENGTH_LONG).show();
//					mActionMode.invalidate();
//	            }		            
//	        }
//		})
//	    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//	
//	        public void onClick(DialogInterface dialog, int which) {		        	
//	        	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//	            imm.hideSoftInputFromWindow(taskName.getWindowToken(), 0);
//	            addingLocation = false;
//	            mActionMode.invalidate();
//	        }
//	
//	    });				
//					
//	taskName.setOnKeyListener(new View.OnKeyListener(){
//
//		@Override
//		public boolean onKey(View v, int keyCode, KeyEvent event) {				
//			if (event.getAction() == KeyEvent.ACTION_DOWN)
//	        {					
//	            switch (keyCode)
//	            {
//	                case KeyEvent.KEYCODE_DPAD_CENTER:
//	                case KeyEvent.KEYCODE_ENTER:			                	
//	                	taskValue = taskName.getText().toString();
//	                	if (taskName.getText().toString().trim().length() == 0) {
//			                Toast.makeText(getApplicationContext(), 
//			                		"Please enter task name", Toast.LENGTH_SHORT).show();
//			            }else if(MainActivity.taskDataSource.isTaskNameExist(taskValue)){
//			            	Toast.makeText(getApplicationContext(), 
//			            			"Task name is already exists", Toast.LENGTH_SHORT).show();		                
//			            } else {
//			                //setup an active task 
//			                activeTask = new Task();		                
//			                activeTask.setAccountId(activeAcc.getId());
//			                activeTask.setReminderId(taskReminderID);
//			                activeTask.setTaskName(taskValue);		                
//			                activeTask.setCategory(MAPREMINDER_CATEGORY);
//			                activeTask.setPriority(taskPriority);
//			                activeTask.setAudioPath("");
//			                activeTask.setPicturePath("");				                
//			                
//			                addingLocation = true;	
//			                		
//			                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//				            imm.hideSoftInputFromWindow(taskName.getWindowToken(), 0);					            					           					            					           
//				            
//				            setMarkerName(point);
//							
//							//invalidate action mode so it will have a task name as title														
//							Toast.makeText(getApplicationContext(), 
//			                		"New task has been created, \n" +
//			                		"you are ready to add locations to your task \n" +
//			                		"by long clicking on your next location", 
//			                		Toast.LENGTH_LONG).show();
//							mActionMode.invalidate();
//			            }				           				           				            	
//	                default:
//	                    break;
//	            }
//	        }
//	        return false;				
//		}			
//	});			
//
//	alertAddTaskName.setOnKeyListener(new OnKeyListener() {						
//
//		@Override
//		public boolean onKey(DialogInterface dialog, int keyCode,
//				KeyEvent event) {
//			if(event.getAction() == KeyEvent.ACTION_DOWN){
//				switch(keyCode){
//				case KeyEvent.KEYCODE_DPAD_CENTER:
//				case KeyEvent.KEYCODE_ENTER:	
//					if(taskName.getText().toString().trim().length() != 0){
//						dialog.dismiss();
//					}							
//					break;
//				default:
//					break;
//				}
//			}
//			return false;
//		}
//	});		
//	alertAddTaskName.show();
//	
//}	
//custom done key text
//taskName.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);