//Progress: add alarm and email notification

//testing: adding users - done

package edu.uco.rnolastname.program6.app;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.audioreminder.AudioReminder;
import edu.uco.rnolastname.program6.calendar.FragmentCalendar;
import edu.uco.rnolastname.program6.dbutilities.AccDataSource;
import edu.uco.rnolastname.program6.dbutilities.Account;
import edu.uco.rnolastname.program6.dbutilities.DbHelper;
import edu.uco.rnolastname.program6.dbutilities.SQLiteAccountDataLoader;
import edu.uco.rnolastname.program6.dbutilities.SQLiteTaskDataLoader;
import edu.uco.rnolastname.program6.dbutilities.Task;
import edu.uco.rnolastname.program6.dbutilities.TaskDataSource;
import edu.uco.rnolastname.program6.mapreminder.MapReminder;
import edu.uco.rnolastname.program6.utilities.SmartOrganizerAlarmReceiver;
import edu.uco.rnolastname.program6.utilities.SwipeDetector;
import edu.uco.rnolastname.program6.utilities.SwipeDetector.Direction;
import edu.uco.rnolastname.program6.utilities.TimePickerFragment;

public class MainActivity extends Activity implements 
OnClickListener,
FragmentBar.OnOptionItemSelected, 
FragmentLogin.LoginDialogListener, 
//LoaderManager.LoaderCallbacks,
FragmentCalendar.OnDateSelectedListener, 
FragmentReminder.OnCreateNewTaskListener,
FragmentReminder.OnTaskClickListener,
FragmentSetReminder.OnImageButtonClickListener,
FragmentPictureReminder.OnPictureTakenListener,
FragmentPictureReminderNote.OnNoteFinishDialogListener,
ActionBar.TabListener
{	
	private static Activity activity;
	
	// Account ID
	private static int ACCOUNT_ID = -1;
	public static Account activeAcc = null;
	
	private static SmartOrganizerAlarmReceiver alarmManager = null;
	
	// Global variables IDs
	public static final int MAINACTIVITY_ID = 10101001;
	public static final int CONTAINER_ID = 10101010;
	public static final int SIGNUP_CONTAINER_ID = 10101010;
	public static final int CALENDAR_CONTAINER_ID = 10101011;
	public static final int LOGIN_FRAGMENT_ID = 10101100;
	public static final int VOICEREMINDER_ID = 10101101;
	public static final int PICTUREREMINDER_ID = 10101110;	
	public static final int REMINDER_ID = 10101111;
	public static final int MAPREMINDER_ID = 10110000;	
	public static final int TIMEPICKER_ID = 10110001;
	public static final int PICTUREREMINDERNOTE_ID = 10101111;

	//Global variables Tags
	public static final String REMINDER_FRAGMENT_TAG = "reminder";
	public static final String SIGNUP_FRAGMENT_TAG = "signup";	
	public static final String CALENDAR_FRAGMENT_TAG = "calendar";
	public static final String LOGIN_FRAGMENT_TAG = "login";
	public static final String PICTURE_FRAGMENT_TAG = "picture";
	public static final String SETREMINDER_FRAGMENT_TAG = "setreminder";	
	public static final String BAR_FRAGMENT_TAG = "fragmentbar";
	public static final String WEBVIEW_FRAGMENT_TAG = "webview";
	public static final String CONTENT_FRAGMENT_TAG = "content";
	public static final String TIMEPICKER_TAG = "timepicker";
	private static final String PICTUREREMINDERNOTE_TAG = "pictureremindertag";   
	
	//Global variable Loader IDs
	public static final int MAIN_ACIVITY_ACCOUNT_LOADER_ID = 1;
	public static final int MAIN_ACIVITY_TASK_LOADER_ID = 2;
	public static final int MAPREMINDER_ACIVITY_LOADER_ID = 3;
	public static final int CALENDAR_ACTIVITY_LOADER_ID = 4;		
	
	// Global Reminder Category
	public static final String regularReminder = "regular";
	public static final String mapReminder = "map";
	public static final String audioReminder = "audio";
	public static final String pictureReminder = "picture";	
	
	//Fragments
	private static FragmentBar fb;		
	private static FragmentSignup fSignup;		
	
	//Frame Layouts
	private static FrameLayout barContainer;
	private static FrameLayout contentContainer;
	private static FrameLayout signup_container;
	private static FrameLayout calendar_container;
	private static FrameLayout reminder_container;
	private static FrameLayout picturereminder_container;
	private static FrameLayout notereminder_container;
	private static FrameLayout webview_container;	
	
	//Layouts
	public static TextView labelTitle;
	private static RelativeLayout mainLayout;
	public static boolean webviewOpen = false;
	
	//Gesture
	private static SwipeDetector swipe = new SwipeDetector();	 
	
	//Account
	private ListView listAccount;	
	private static List<Account> accounts = new ArrayList<Account>();
	private Bundle accInfo;
			
	//Database
	public static DbHelper dbHelper; 	
	public static AccDataSource accDataSource;
	public static TaskDataSource taskDataSource;
	private static SQLiteAccountDataLoader accountDataLoader;
	public static SQLiteTaskDataLoader taskDataLoader;	
	
	//ArrayAdapters
	private static ArrayAdapter<Account> listAccAdapter = null;
	public static ArrayAdapter<Task> listTaskAdapter = null;	
	
	//Tasks
	private static ArrayList<Task> tasks = new ArrayList<Task>();
	private static Task activeTask = null;
	
	//MenuItems
	private static MenuItem itemCalendar = null;
	private static MenuItem itemSignup = null; 	
	private static MenuItem itemLogin = null;
	private static MenuItem itemReminder = null;
	private static MenuItem itemDivider = null;
	private static ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
	
	// Storage for camera image URI components
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";

    // Required for camera operations in order to save the image file on resume.
    private String mCurrentPhotoPath = null;
    private Uri mCapturedImageURI = null;	    
    
    //Threads
    private static Thread mapActivityThread = null;
    private static Thread checkLoginThread = null;
    
    //task time
    private static ArrayAdapter timeAdapter = null;
    
    //Tabs        
    private static ActionBar actionBar;
    private static Method actionBarMethod = null;
    private static ArrayList<String> barTabs = new ArrayList<String>();
    private final ArrayList<Tab> tabs = new ArrayList<Tab>();        
        
    //Login Dialog
    private static AlertDialog listAccDialog = null;
    private static AlertDialog.Builder listAccBuilder = null;
    
    //handler
  	Handler handler = new Handler(){
  		@Override 
  		public void handleMessage(Message msg){
  			if(msg.what == MAIN_ACIVITY_TASK_LOADER_ID){  				
  				
  				if(getLoaderManager().getLoader(MAIN_ACIVITY_TASK_LOADER_ID) == null){
  					getLoaderManager().initLoader(MAIN_ACIVITY_TASK_LOADER_ID, null, mLoaderCallbackTask);
  				}else{
  					getLoaderManager().restartLoader(MAIN_ACIVITY_TASK_LOADER_ID, null, mLoaderCallbackTask);
  				}
  				
  				showFragmentBar();
  				
  				invalidateOptionsMenu();
  				checkLoginThread = null;
  				
  				Log.d("DEBUG","task count: " + tasks.size());
  				
  			}  				  			  			
  		}
  	};

  	@Override
	public void onPictureTaken(int resultCode, Task task) {
		if(resultCode == RESULT_OK && task != null 
				&& (task.getPicturePath() != null || !task.getPicturePath().equals(""))){
			Toast.makeText(getApplicationContext(), "Picture has been set for " 
				+ task.getTaskName(), Toast.LENGTH_SHORT).show();			
		}else{
			Toast.makeText(getApplicationContext(), 
					"No picture has been set for the reminder", Toast.LENGTH_SHORT).show();			
			
		}
		activeTask = task;
		setNewReminder(activeTask);
		closeFragmentPictureReminder();
	}
  	
  	    
  	//	public static AccountDataSource dbHelper;
	
  	//	private static SQLiteDataLoader<List<Account>> accountLoader;
  	//	private static SQLiteDataLoader<List<Task>> taskLoader;
  	
  	//callback for fragment reminder when 
    //user click on create new task so main activity can show the set new reminder fragment
    @Override
	public void onCreateNewTask() {				
    	//close fragment reminder
    	Fragment f = getFragmentManager().findFragmentByTag(REMINDER_FRAGMENT_TAG);
    	if(f != null){
    		getFragmentManager().popBackStack();
    	}
    	
    	setNewReminder(null);
	}
        
    @Override
	public void onTaskClick(final Task task) {
    	if(actionBar.getTabCount() != 0){
    		actionBar.removeAllTabs();
    		barTabs.clear();
    	}
    	if(task.getCategory().equals("mapreminder")){
    		Runnable mapActivity = new Runnable(){
				@Override
				public void run() {		
					Intent i = new Intent(getApplicationContext(),MapReminder.class);										
					//sending active user account					
					i.putExtra("activeAcc", activeAcc);		
					i.putExtra("task", task);
					i.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
					startActivity(i);
					//TODO enable this when doing the swipe UI
					overridePendingTransition(android.R.animator.fade_in,android.R.animator.fade_out);
				}				
			};
			mapActivityThread = new Thread(mapActivity);
			mapActivityThread.start();
    	}else{
    		labelTitle.setText(activeAcc.getUsername() + " - " + task.getTaskName());
    		//open a dialog to display task information
        	setNewReminder(task);
    	}
    	
	}
    
    @Override
	public void onDateSelected(Task task) 
    {	
    	//set date for a task
    	showCalendar(null);
    	if(task != null){
    		if(task.getDate() != null && !task.getDate().equalsIgnoreCase("")){    			    			
    			Toast.makeText(getApplicationContext(), 
        				"Task date has been set", Toast.LENGTH_SHORT).show();
        		activeTask = task;
        		setNewReminder(activeTask);
        	}        	
    	}
    	
    			
	}	
    
    @Override
	protected void onActivityResult(int requestCode, int responseCode, Intent data){    	
    	if(responseCode != RESULT_OK){
    		Log.d("DEBUG","Error on activity result - Main Activity from request code: " + requestCode);
    		return;
    	}    	    	    	    	
//    	Log.d("DEBUG","MainActivity on activity result");
    	switch(requestCode){
    	case MAPREMINDER_ID:    		
    		synchronized(this){
    			try {
    				if(responseCode == Activity.RESULT_CANCELED){
    					if(data != null){
    						String msg = data.getStringExtra("errorMessage");
        					Log.d("DEBUG","Error message: " + msg);
    					}    					
    				}
					mapActivityThread.join();					
				} catch (InterruptedException e) { 
					e.printStackTrace();
				}
    		}
    		Log.d("DEBUG","check if mapActivityThread is still active: " + mapActivityThread.isAlive());
    		break;
    	case MAINACTIVITY_ID:
    		if(data != null){
    			activeTask = data.getParcelableExtra("task");    
        		setNewReminder(activeTask);        		
    		}    		
    		break;    	
    	}
    	
    }
        
    
    private void confirmNoteSave(final String note){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Save Note?");
	    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {				// 
				if(note != null && !note.equalsIgnoreCase("")){
	        		activeTask.setNotes(note);
	        		if(activeTask.getCategory() != null && !activeTask.getCategory().equalsIgnoreCase("")){
						activeTask.setCategory(activeTask.getCategory() + MainActivity.regularReminder);						
					}else{
						activeTask.setCategory(MainActivity.regularReminder);
					}
	        			        		
	        		Toast.makeText(getApplicationContext(), "Note has been saved", Toast.LENGTH_SHORT).show();
	        		setNewReminder(activeTask);	        		
	        	}    
				else {
		            Toast.makeText(getApplicationContext(), "Please make sure note is not empty", Toast.LENGTH_SHORT)
		                    .show();
		            dialog.dismiss();
		        }
			}
	    	
	    }).setNegativeButton("Discard", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getApplicationContext(), "Note discard", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		}).show();
    }
    
    @Override
	public void onNoteFinishDialog(String note) {
		Log.d("DEBUG","note: " + note);
		confirmNoteSave(note);		    			
	}	
    
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mCurrentPhotoPath != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
        if (mCapturedImageURI != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_URI_KEY, mCapturedImageURI.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }
	
	@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)) {
            mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
        }
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_URI_KEY)) {
            mCapturedImageURI = Uri.parse(savedInstanceState.getString(CAPTURED_PHOTO_URI_KEY));
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
	
	/* LOADERS */
	private void handleResultOnLoadFinishedAccount(List<Account> data){
		listAccAdapter.clear();
		for(Account acc: data){
			listAccAdapter.add(acc);
		}		
		showListAccount();
	}
	
	private void handleResultOnLoadFinishedTask(List<Task> data){
		listTaskAdapter.clear();
		for(Task task: data){
			listTaskAdapter.add(task);
			tasks.add(task);
		}
		listTaskAdapter.notifyDataSetChanged();			
	}
	
	private LoaderCallbacks<List<Account>> mLoaderCallbackAccount = new LoaderCallbacks<List<Account>>(){

		@Override
		public Loader<List<Account>> onCreateLoader(int id, Bundle args) {		
			Log.d("DEBUG","Loading accounts...");
			accountDataLoader = new SQLiteAccountDataLoader(getApplicationContext(),accDataSource,
					null,null,null,null,null);
			return accountDataLoader;
		}

		@Override
		public void onLoadFinished(Loader<List<Account>> loader, List<Account> data) {			
			handleResultOnLoadFinishedAccount(data);
		}

		@Override
		public void onLoaderReset(Loader<List<Account>> loader) {
			// TODO Auto-generated method stub
			
		}
		
	};
		
	private LoaderCallbacks<List<Task>> mLoaderCallbackTask = new LoaderCallbacks<List<Task>>(){

		@Override
		public Loader<List<Task>> onCreateLoader(int id, Bundle args) {				
			taskDataLoader = new SQLiteTaskDataLoader(
				getApplicationContext(),taskDataSource,TaskDataSource.TASK_ACCOUNT_ID + " = ?",
				new String[] {String.valueOf(ACCOUNT_ID)},null,null, null); 			
			return taskDataLoader;
		}

		@Override
		public void onLoadFinished(Loader<List<Task>> loader, List<Task> data) {
			Log.d("DEBUG","data loaded: " + data.size());
			handleResultOnLoadFinishedTask(data);
		}

		@Override
		public void onLoaderReset(Loader<List<Task>> loader) {
			// TODO Auto-generated method stub
			
		}
		
	};
	/* END LOADERS */	
	
	@Override		
	protected void onCreate(Bundle savedInstanceState) {				
		super.onCreate(savedInstanceState);		
		activity = this;
		setContentView(R.layout.activity_main);																
		
		alarmManager = new SmartOrganizerAlarmReceiver();
		
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);			
					
		mainLayout = (RelativeLayout) findViewById(R.id.main_layout);		
		labelTitle = (TextView) findViewById(R.id.label_title);
				
		if (getLoaderManager().getLoader(MAIN_ACIVITY_ACCOUNT_LOADER_ID) == null) {
	          Log.i("DEBUG", "+++ Initializing the new Loader MainActivity... +++");
        } else {
	          Log.i("DEBUG", "+++ Reconnecting with existing Loader MainActivity(id '1')... +++");
	    }				
		
		//set adapter for listtaskadapter
		listTaskAdapter = new ArrayAdapter<Task>(this,
				android.R.layout.simple_list_item_1,tasks);				
		
		//set adapter for listaccountadapter
		listAccAdapter = new ArrayAdapter<Account>(this, 
				android.R.layout.simple_list_item_1,accounts);		
		
		//Prepare the database
		dbHelper = new DbHelper(this);
		SQLiteDatabase dbsql = dbHelper.getWritableDatabase();
		accDataSource = new AccDataSource(dbsql);			
		taskDataSource = new TaskDataSource(dbsql);
		
		//DELETING ALL TASKS
//		boolean res = taskDataSource.deleteAll();
//		Log.d("DEBUG","deleted all tasks result: " + res);
		
		//init loader
		getLoaderManager().initLoader(MAIN_ACIVITY_ACCOUNT_LOADER_ID, null, mLoaderCallbackAccount);			
		
		runCheckLogin();
				
		mainLayout.setOnTouchListener(swipe);
		mainLayout.setOnClickListener(this);
		
		//Login Dialog properties
		if(listAccount == null){
			listAccount = new ListView(this);
			listAccount.setAdapter(listAccAdapter);
			
			listAccBuilder = new AlertDialog.Builder(this);		
			listAccBuilder.setTitle("Please select username or click New User");
			listAccBuilder.setNeutralButton("New User", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {				
					showFormSignup();				
				}
			});
						
			
			listAccBuilder.setView(listAccount);
			listAccDialog = listAccBuilder.create();
		}
		
		listAccount.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {					
				if(listAccDialog != null){
					listAccDialog.dismiss();
					showFormLogin(accounts.get(position).getUsername());
				}														
			}
			
		});
		
		// TODO: make sure to keep the login instance when user flip the device		
		
//		/* set ontouchevent so that the keyboard get off the screen when the user press anywhere on the screen */	    
//	    mainLayout.setOnTouchListener(new OnTouchListener(){
//	    	@Override
//	    	public boolean onTouch(View v, MotionEvent e){
//	    		if(e.getAction() == MotionEvent.ACTION_DOWN){
//	    			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		    		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);		    		
//	    		}
//	    		return true;
//	    	}			
//	    });	    
	}		
	
	private void runCheckLogin(){
		if(!isLogin() && checkLoginThread == null){
			checkLoginThread = new Thread(checkLogin);
			checkLoginThread.start();
		}			
	}
	
	final Runnable checkLogin = new Runnable(){
		public boolean login;
		@Override
		public void run() {	
			login = false;
			while(!login){					
				if(isLogin()){
					login = true;
				}
			}			
			handler.sendEmptyMessage(MAIN_ACIVITY_TASK_LOADER_ID);		
		}
		
	};
	
	public static boolean isLogin(){
		boolean login = false;
		if(ACCOUNT_ID != -1 && activeAcc != null){
			login = true;
		}
		return login;
	}	
	
	@Override
	public void onResume(){
		super.onResume();
				
	}
	
	@Override
	public void onLoginFinish(String inputUsername, String inputPassword) {		
		if(!inputUsername.equals("") && !inputPassword.equals("")){
			
			List<Account> listAcc = new ArrayList<Account>(); 
			
			listAcc = accDataSource.read(			
					AccDataSource.ACCOUNT_COLUMN_USERNAME + " = ? AND " + AccDataSource.ACCOUNT_COLUMN_PASSWORD + " = ?", 
					new String[] {inputUsername,accDataSource.hashPass(inputPassword)}, null, null, null); 
			
			if(listAcc.size() != 0){
				
				activeAcc = listAcc.get(0);
				if(activeAcc.getId() == 0){
					Toast.makeText(getApplicationContext(), "Your account does not exist, please signup",
							Toast.LENGTH_SHORT).show();
				}else{					
					ACCOUNT_ID = activeAcc.getId();					
					String username = activeAcc.getUsername().substring(0,1).toUpperCase() 
							+ activeAcc.getUsername().substring(1);
					labelTitle.setText("Welcome " + username);																										
				}
			}else{
				Toast.makeText(getApplicationContext(), "Your account does not exist, please signup",
						Toast.LENGTH_SHORT).show();
			}
		}else{
			return;		
		}				
	}	
	
	@Override
	public void onClick(View v) {
		if(isLogin()){
			int id = v.getId();
			switch(id){
			case R.id.main_layout:			
				if(swipe.swipeDetected()){										
					Direction swipeDirection = swipe.getAction();
					
					//swipe bottom to top
					if(swipeDirection == Direction.BT){
						
					}
					//swipe top to bottom
					else if(swipeDirection == Direction.TB){
						
					}
					//swipe left to right
					else if(swipeDirection == Direction.LR){
						
					}
					//swipe right to left to right
					else if(swipeDirection == Direction.RL){
						if(actionBar.getTabCount() != 0){
							actionBar.removeAllTabs();
							barTabs.clear();
						}											
						
						if(isLogin()){								
							showCalendar(null);
						}else{
							showListAccount();
							runCheckLogin();
						}
						labelTitle.setText("");
					}										
				}
				break;
			}
		}
		
		// A view in main layout is clicked, then take care of the action here 
	}									
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);		
		itemSignup = menu.findItem(R.id.action_signup);		
		itemLogin = menu.findItem(R.id.action_logout);
//		itemCalendar = menu.findItem(R.id.action_calendar);
		itemReminder = menu.findItem(R.id.action_reminder);
		
		//save all menu items
		for(int i=0; i < menu.size(); i++ )
		{
			menuItems.add(menu.getItem(i));
		}
		
		if(webviewOpen && isLogin()){
			itemReminder.setIcon(R.drawable.ic_done);
			itemReminder.setTitle("Close Web");
		}else{
			itemReminder.setIcon(R.drawable.reminder_add);
			itemReminder.setTitle("Add Reminder");
		}
		
		if(isLogin()){
			itemReminder.setVisible(true);
			itemSignup.setIcon(R.drawable.ic_editprofile);
			itemSignup.setTitle("Profile");
			itemLogin.setTitle("Logout");
		}else{
			itemSignup.setIcon(R.drawable.ic_signup_submit);
			itemSignup.setTitle("Sign up");
			itemLogin.setTitle("Login");
			itemReminder.setVisible(false);
			
		}
		return true;		
	}		
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		int id = item.getItemId();		

		switch(id){		
		case R.id.action_signup:
			if(itemSignup == null){
				itemSignup = item;
			}												
			showFormSignup();			
			break;		
		case R.id.action_reminder:
			if(isLogin() && !webviewOpen){
				labelTitle.setText("Set New Reminder");
				activeTask = new Task();
				activeTask.setAccountId(activeAcc.getId());
				setNewReminder(activeTask);												
			}else if(webviewOpen){
				//close the webview 
				closeFragmentWebView();
				
				//clean the tabs
				if(actionBar.getTabCount() != 0){
					actionBar.removeAllTabs();
					barTabs.clear();
				}
				
				webviewOpen = false;
				labelTitle.setText("Welcome " + activeAcc.getUsername());
				invalidateOptionsMenu();
				showFragmentBar();
			}						
			else{
				showListAccount();
				runCheckLogin();
			}
						
			break;
		case R.id.action_logout:
			if(isLogin()){

				activeAcc = null;
				ACCOUNT_ID = -1;
				
				//remove all open fragments
				closeAllFragments();																																																													
					
				actionBar.removeAllTabs();
				if(labelTitle == null){
					labelTitle = (TextView) findViewById(R.id.label_title);					
				}
				
				webviewOpen = false;
				labelTitle.setText("Home");
				this.setTitle(R.string.app_name);
				invalidateOptionsMenu();					
			}else{				
				showListAccount();
				runCheckLogin();
			}
			
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}	
	
	private void closeAllFragments(){
		closeFragmentBar();
		closeFragmentSetNewReminder();
		closeFragmentWebView();
		closeFragmentSignup();
		closeFragmentCalendar();
		closeFragmentCalendar();
		closeFragmentReminder();
		closeFragmentSetReminder();
		closeFragmentPictureReminder();
	}
	
	private void closeFragmentBar(){
		Fragment fBar = getFragmentManager().findFragmentByTag(BAR_FRAGMENT_TAG);	
		
		if(fBar != null){					
			getFragmentManager().beginTransaction().remove(fBar).addToBackStack(null).commit();
		}
	}
	
	private void closeFragmentSetNewReminder(){
		Fragment f = getFragmentManager().findFragmentByTag(SETREMINDER_FRAGMENT_TAG);
		
		if(f != null){			
			getFragmentManager().beginTransaction().remove(f).addToBackStack(null).commit();
		}
	}
	
	private void closeFragmentWebView(){
		Fragment fWebView = getFragmentManager().findFragmentByTag(WEBVIEW_FRAGMENT_TAG);
		
		if(fWebView != null){
			getFragmentManager().beginTransaction().remove(fWebView).addToBackStack(null).commit();
		}
	}
	
	private void closeFragmentSignup(){
		Fragment f = getFragmentManager().findFragmentByTag(SIGNUP_FRAGMENT_TAG);
		
		if(f != null){
			showFormSignup();
		}
	}	
	
	private void closeFragmentCalendar(){
		Fragment f = getFragmentManager().findFragmentByTag(CALENDAR_FRAGMENT_TAG);
		
		if(f != null){
			showCalendar(null);
		}
	}
	
	private void closeFragmentReminder(){
		Fragment fReminder = getFragmentManager().findFragmentByTag(REMINDER_FRAGMENT_TAG);
		
		if(fReminder != null){
			getFragmentManager().beginTransaction().remove(fReminder).addToBackStack(null).commit();
		}
	}
	
	private void closeFragmentSetReminder(){
		Fragment fSetReminder = getFragmentManager().findFragmentByTag(SETREMINDER_FRAGMENT_TAG);
		
		if(fSetReminder != null){
			getFragmentManager().beginTransaction().remove(fSetReminder).addToBackStack(null).commit();
		}
	}		
	
	private void closeFragmentPictureReminder(){
		Fragment fPictureReminder = getFragmentManager().findFragmentByTag(PICTURE_FRAGMENT_TAG);
		
		if(fPictureReminder != null){
			getFragmentManager().beginTransaction().remove(fPictureReminder).addToBackStack(null).commit();
		}
	}
	
	private ActionMode.Callback signupCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub			
			return false;
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {			
			onOptionsItemSelected(itemSignup);
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			getMenuInflater().inflate(R.menu.signup_contextmenu, menu);			
			return true;
		}				
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch(item.getItemId()){														
			case R.id.signup_clear:
				/* clear the signup form */		
				TextView username = (TextView) findViewById(R.id.txt_username);
				TextView password = (TextView) findViewById(R.id.txt_password);
				TextView name = (TextView) findViewById(R.id.txt_fname);
				TextView address = (TextView) findViewById(R.id.txt_address);							
				Spinner state = (Spinner) findViewById(R.id.list_state);
				TextView zip = (TextView) findViewById(R.id.txt_zip);
				RadioButton male = (RadioButton) findViewById(R.id.rad_male);
				RadioButton female = (RadioButton) findViewById(R.id.rad_female);
				TextView email = (TextView) findViewById(R.id.txt_email);
				
				username.setText("");
				password.setText("");
				name.setText("");
				address.setText("");
				state.setSelection(0);
				zip.setText("");
				male.setChecked(false);
				female.setChecked(false);
				email.setText("");
				
				break;	
			case R.id.signup_submit:
				onOptionsItemSelected(itemSignup);
				mode.finish();
				
				try {
					accInfo = fSignup.getValues();						
				} catch (ParseException e) {
					e.printStackTrace();
				}						
				
				//insert new user
				if(accInfo != null && !isLogin()){
					Account acc = new Account(accInfo);
					if(!acc.getUsername().equals("") && !acc.getPassword().equals("")){

						//check if username is already exists
						List<Account> listAcc = new ArrayList<Account>(); 
						listAcc = accDataSource.read(			
								AccDataSource.ACCOUNT_COLUMN_USERNAME + " = ?", 
								new String[] {acc.getUsername()}, null, null, null); 
						
						if(listAcc.size() != 0){
							Toast.makeText(getApplicationContext(), 
									"Username is already exists", Toast.LENGTH_SHORT).show();
							break;
						}else{							
							accountDataLoader.insert(acc);
//							accountDataLoader.commitContentChanged();
//							listAccAdapter.notifyDataSetChanged();							
							
							Toast.makeText(getApplicationContext(), 
								"Hi " + accInfo.getString("fname") + "," + accInfo.getString("lname") 
								+ ", your account has been created, please login ", Toast.LENGTH_SHORT).show();						
							//showFormLogin(acc.getUsername());
							showListAccount();
						}												
					}else{
						Toast.makeText(getApplicationContext(), "Invalid entry please re-signup", Toast.LENGTH_SHORT)
						.show();
					}												
				}
				//update existing user
				else if(accInfo != null && isLogin()){
					Account updatedAcc = new Account(accInfo);
					
					Log.d("DEBUG","old address vs newAddress: " + 
					!activeAcc.getAddress().equals(updatedAcc.getAddress()));
					
					if(!activeAcc.getUsername().equals(updatedAcc.getUsername())							
							|| !activeAcc.getfName().equals(updatedAcc.getfName()) 
							|| !activeAcc.getlName().equals(updatedAcc.getlName())
							|| !activeAcc.getAddress().equals(updatedAcc.getAddress())
							|| !activeAcc.getCity().equals(updatedAcc.getCity())
							|| !activeAcc.getState().equals(updatedAcc.getState())
							|| activeAcc.getZip() != updatedAcc.getZip()
							|| !activeAcc.getEmail().equals(updatedAcc.getEmail())){													
						
							accDataSource.update(updatedAcc);
													
							Toast.makeText(getApplicationContext(), 
									"Hi " + accInfo.getString("fname") + "," + accInfo.getString("lname") 
									+ ", your account has been updated", Toast.LENGTH_SHORT).show();													
					}else{						
						break;
					}						
				}											
				break;
			default:
				return false;
			}
			return true;
		}
	};				
	
	private void setNewReminder(Task task){				
		if(findViewById(R.id.fContent) != null){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment prev = getFragmentManager().findFragmentByTag(SETREMINDER_FRAGMENT_TAG);
			if(prev != null){
				ft.remove(prev);			
			}		
			ft.addToBackStack(null);
			
			Fragment newReminder = FragmentSetReminder.newInstance(activeAcc, task); 
			ft.setCustomAnimations(android.R.animator.fade_in,android.R.animator.fade_out,
					android.R.animator.fade_in,android.R.animator.fade_out)
					.replace(R.id.fContent,newReminder,SETREMINDER_FRAGMENT_TAG).addToBackStack(null)
				.commit();
		}		
	}		
	
	private void showFormReminderNote(){
    	//create fragment reminder note here
		FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
		Fragment prev = activity.getFragmentManager().findFragmentByTag(MainActivity.PICTUREREMINDERNOTE_TAG);
		
		if(prev != null){
			ft.remove(prev);			
		}				
		
		ft.addToBackStack(null);
		
		DialogFragment df = FragmentPictureReminderNote.newInstance();
//		df.setTargetFragment(this,PICTUREREMINDERNOTE_ID);		
//		df.onActivityResult(PICTUREREMINDERNOTE_ID, 1, null);
		
		df.show(ft, PICTUREREMINDERNOTE_TAG);
    }
	
	private void showListAccount(){						
		listAccDialog.show();
	}
	
	private void showFragmentBar(){
		//Show Fragment Bar
		if(findViewById(R.id.fBar_container) != null){  					
			fb = FragmentBar.newInstance();
			getFragmentManager().beginTransaction().add(R.id.fBar_container,fb,BAR_FRAGMENT_TAG).commit();
		}
	}
	
	private void showCalendar(Task task){
		if(calendar_container == null){
			calendar_container = new FrameLayout(this);
			calendar_container.setId(CALENDAR_CONTAINER_ID);
			FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			mainLayout.addView(calendar_container, clp);
		}
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment f = getFragmentManager().findFragmentByTag(CALENDAR_FRAGMENT_TAG);
        if (f != null) {
            ft.remove(f);   
            getFragmentManager().popBackStack();
        }else{
        	FragmentCalendar fc = FragmentCalendar.newInstance(task);
    		getFragmentManager().beginTransaction()
    			.setCustomAnimations(R.animator.slide_left, R.animator.slide_right,
    					R.animator.slide_left, R.animator.slide_right)          
                .add(CALENDAR_CONTAINER_ID,fc,
                		CALENDAR_FRAGMENT_TAG
                ).addToBackStack(null).commit();
        }                                    
	}
	
	private void showPictureReminder(Task task){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag(PICTURE_FRAGMENT_TAG);
		
		if(prev != null){
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		
		FragmentPictureReminder picReminder = FragmentPictureReminder.newInstance(task);
		ft.setCustomAnimations(android.R.animator.fade_in,android.R.animator.fade_out,
				android.R.animator.fade_in,android.R.animator.fade_out)
				.add(PICTUREREMINDER_ID, picReminder,PICTURE_FRAGMENT_TAG).addToBackStack(null).commit();		
	}
	
	private void showFormLogin(String username){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag(LOGIN_FRAGMENT_TAG);
		if(prev != null){
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		
		DialogFragment loginFragment = FragmentLogin.newInstance(username);
		
		loginFragment.setTargetFragment(loginFragment, LOGIN_FRAGMENT_ID);
		loginFragment.onActivityResult(LOGIN_FRAGMENT_ID, 1, null);
		
		loginFragment.show(ft, LOGIN_FRAGMENT_TAG);		
	}		
	
	private void showFormSignup() {		
		if(signup_container == null){
			//initialize new FrameLayout
			signup_container = new FrameLayout(this);
			signup_container.setId(SIGNUP_CONTAINER_ID);
			FrameLayout.LayoutParams slp = new FrameLayout
					.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
			mainLayout.addView(signup_container, slp);
		}
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();		
        Fragment f = getFragmentManager().findFragmentByTag(SIGNUP_FRAGMENT_TAG);
        if (f != null) {
            ft.remove(f);      
            getFragmentManager().popBackStack();
        }else{         	    	
	    	
	        fSignup = new FragmentSignup();
	        getFragmentManager().beginTransaction()
	                .setCustomAnimations(R.animator.slide_up,
	                        R.animator.slide_down,
	                        R.animator.slide_up,
	                        R.animator.slide_down)
	                .add(CONTAINER_ID, fSignup,
	                		SIGNUP_FRAGMENT_TAG
	                ).addToBackStack(null).commit();
	        startActionMode(signupCallback);
        }                
    }
	
	private void showReminder(){		
		
		if(findViewById(R.id.fContent) != null){
			FragmentTransaction ft = getFragmentManager().beginTransaction(); 
			Fragment f = getFragmentManager().findFragmentByTag(REMINDER_FRAGMENT_TAG);
			if(f != null){
				ft.remove(f);				
			}
			ft.addToBackStack(null);
			
			FragmentReminder fr = FragmentReminder.newInstance(tasks);
			ft.setCustomAnimations(android.R.animator.fade_in,android.R.animator.fade_out,
					android.R.animator.fade_in,android.R.animator.fade_out)
					.replace(R.id.fContent,fr,SETREMINDER_FRAGMENT_TAG).addToBackStack(null)
				.commit();

		}
	}
					
	/**
     * Getters and setters for picture reminder.
     */

    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public void setCurrentPhotoPath(String mCurrentPhotoPath) {
        this.mCurrentPhotoPath = mCurrentPhotoPath;
    }

    public Uri getCapturedImageURI() {
        return mCapturedImageURI;
    }

    public void setCapturedImageURI(Uri mCapturedImageURI) {    	
        this.mCapturedImageURI = mCapturedImageURI;
    }

    public void showFragmentWebview(String url){
    	if(findViewById(R.id.fWeb_container) != null){
    		FragmentTransaction ft = getFragmentManager().beginTransaction();
    		Fragment f = getFragmentManager().findFragmentByTag(WEBVIEW_FRAGMENT_TAG);
    		if(f != null){
    			ft.remove(f);    			
    		}
    		ft.addToBackStack(null);
    		
			FragmentWebView fWeb = FragmentWebView.newInstance(url);
    		ft.setCustomAnimations(android.R.animator.fade_in,android.R.animator.fade_out,
					android.R.animator.fade_in,android.R.animator.fade_out)
					.replace(R.id.fWeb_container,fWeb, WEBVIEW_FRAGMENT_TAG).addToBackStack(null).commit();
    		webviewOpen = true;
    		labelTitle.setText("");
			invalidateOptionsMenu();	
			closeFragmentBar();
			closeFragmentSetNewReminder();
    	}    	    	
    }
    
    @Override
	public void onOptionSelected(int position) {		
    	if(actionBar.getTabCount() != 0){
			actionBar.removeAllTabs();
			barTabs.clear();
		}
    	switch(position){
    	//News
    	case 0:    		    		
    		//Tabs for News
    		barTabs.add("CNN");
    		barTabs.add("Fox News");
    		barTabs.add("BBC");
    		barTabs.add("Reddit");				
    		
    		//add the tab to action bar
			for(String s: barTabs){
				Tab tab = actionBar.newTab();
				tab.setText(s);
				tab.setTabListener(this);
				actionBar.addTab(tab);						
			}						
			break;						
		//Shows
		case 1:			    		
    		//Tabs for Watch Movie
    		barTabs.add("Youtube");
    		barTabs.add("Netflix");
    		barTabs.add("Hulu");
    		barTabs.add("TV");				
    		
    		//add the tab to action bar
			for(String s: barTabs){
				Tab tab = actionBar.newTab();
				tab.setText(s);
				tab.setTabListener(this);
				actionBar.addTab(tab);						
			}
			break;
		//Music Player		
		case 2:
			
			break;
		//TO DO List
		case 3:
			if(isLogin()){	
				this.setTitle(activeAcc.getUsername() + "'s Task list");
				showReminder();
			}else{
				showListAccount();
				runCheckLogin();
			}
			break;
		case 4:
			
			break;
    	}
    	labelTitle.setText("");
    	this.setTitle("");
		//forceTabs();
	}
    
    //Force Action Bar Tabs appear below Action Bar
    private void forceTabs(){
    	try {
			actionBarMethod = actionBar.getClass().getDeclaredMethod("setHasEmbeddedTabs", boolean.class);
			actionBarMethod.setAccessible(true);
			actionBarMethod.invoke(actionBar, false);
		} catch (NoSuchMethodException e) {			
			e.printStackTrace();
		} catch (IllegalAccessException e) {			
			e.printStackTrace();
		} catch (IllegalArgumentException e) {			
			e.printStackTrace();
		} catch (InvocationTargetException e) {			
			e.printStackTrace();
		}
    }
    
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		String id = (String) tab.getText();
		String url = "http://www." + id.replaceAll("\\s","").trim() + ".com";		
		
		showFragmentWebview(url);
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		String id = (String) tab.getText();
		Log.d("DEBUG","onTabUnselected: " + id);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		String id = (String) tab.getText();
		Log.d("DEBUG","onTabReselected: " + id);
	}
	
	//actually calling alarm manager or notification manager to send pending intent
	//to be received by broadcast receiver 
	private void setTaskReminder(Account account,Task task){
		if(task.getReminderId() == 1){

		}else if(task.getReminderId() == 2){
			
		}else if(task.getReminderId() == 3){
			
		}else{
			Log.d("DEBUG","Reminder type not recognized");
		}
	}
	
	@Override
	public void onImageButtonClick(int which,final Task task) {				
		switch(which){
		case 0:
			//calendar
			if(isLogin()){							
				showCalendar(activeTask);
			}else{
				showListAccount();
				runCheckLogin();
			}
			break;
		case 1:
			//voice
			if(task.getDate() != null && !task.getDate().equalsIgnoreCase("")){
				Intent intentVReminder = new Intent(this, AudioReminder.class);
				intentVReminder.putExtra("task",activeTask);
				//intentVReminder.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				//intentVReminder.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
				startActivityForResult(intentVReminder,MAINACTIVITY_ID);						
				//overridePendingTransition(android.R.animator.fade_in,android.R.anim.fade_out);				
			}else{
				Toast.makeText(getApplicationContext(), "Please set task date", Toast.LENGTH_SHORT).show();
			}
					
			break;
		case 2:
			if(task.getDate() != null && !task.getDate().equalsIgnoreCase("")){
				//picture
				if(picturereminder_container == null){
					picturereminder_container = new FrameLayout(this);
					picturereminder_container.setId(PICTUREREMINDER_ID);
					FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT);
					mainLayout.addView(picturereminder_container, plp);													
				}
				if(activeTask != null){
					showPictureReminder(activeTask);
				}			
			}else{
				Toast.makeText(getApplicationContext(), "Please set task date", Toast.LENGTH_SHORT).show();
			}
			break;
		case 3:
			//save			
			//taskDataLoader.insert(task);
			listTaskAdapter.notifyDataSetChanged();
			Toast.makeText(this, "Task has been saved", Toast.LENGTH_SHORT).show();
			break;	
		case 4: 
			if(task.getDate() != null && !task.getDate().equalsIgnoreCase("")){
				//map 
				Runnable mapActivity = new Runnable(){
					@Override
					public void run() {		
						Intent i = new Intent(getApplicationContext(),MapReminder.class);										
						//sending active user account									
						i.putExtra("activeAcc", activeAcc);		
						i.putExtra("task", task);
						i.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
						startActivityForResult(i,MAPREMINDER_ID);
						overridePendingTransition(android.R.animator.fade_in,android.R.animator.fade_out);
					}				
				};
				mapActivityThread = new Thread(mapActivity);
				mapActivityThread.start();
			}else{
				Toast.makeText(getApplicationContext(), "Please set task date", Toast.LENGTH_SHORT).show();
			}
			
			break;
		case 5:
			if(task.getDate() != null && !task.getDate().equalsIgnoreCase("")){
				//picture
				if(notereminder_container == null){
					notereminder_container = new FrameLayout(this);
					notereminder_container.setId(PICTUREREMINDERNOTE_ID);
					FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT);
					mainLayout.addView(notereminder_container, plp);													
				}
				if(activeTask != null){
					showFormReminderNote();
				}			
			}else{
				Toast.makeText(getApplicationContext(), "Please set task date", Toast.LENGTH_SHORT).show();
			}
			break;
		}		
	}

}

//TODO try to make it work with only one loader listener and use ID to differentiate the two
//@Override
//public Loader<List<?>> onCreateLoader(int id, Bundle args) {
//	switch(id){
//	case MAIN_ACIVITY_ACCOUNT_LOADER_ID:
//		Log.d("DEBUG","init acc data loader");
//		accountLoader = new SQLiteDataLoader<List<Account>>(this,
//				accDataSource,null,null,null,null, null);
//		return accountLoader;
//		break;
//	case MAIN_ACIVITY_TASK_LOADER_ID:
//		Log.d("DEBUG","init task data loader");
////		taskDataLoader = new SQLiteTaskDataLoader(
////				this,taskDataSource,null,null,null,null, null); 
//		break;
//	default: 
//		return null;
//	}		
//	
//}
//
//@Override
//public void onLoadFinished(Loader<List<?>> loader, List<?> data) {		
//	int id = loader.getId();
//	switch(id){
//	case MAIN_ACIVITY_ACCOUNT_LOADER_ID:
//		Log.d("DEBUG","onLoadFinished Account data length: " + data.size());
//			
//		break;
//	case MAIN_ACIVITY_TASK_LOADER_ID:
//		
//		break;
//	}											
//}
//
//@Override
//public void onLoaderReset(Loader<List<?>> loader) {
//	int id = loader.getId();
//	switch(id){
//	case MAIN_ACIVITY_ACCOUNT_LOADER_ID:
//		listAccAdapter.clear();
//		break;
//	case MAIN_ACIVITY_TASK_LOADER_ID:
//		listTaskAdapter.clear();
//		break;
//	}		
//}	
/* END LOADER ACCOUNT */		


/*@Override
public boolean dispatchKeyEvent(KeyEvent event) {
    if(amCallback != null) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
        	Toast.makeText(getApplicationContext(), "Please make sure you have all the fields completed", 
					Toast.LENGTH_SHORT).show();		
           return true; // consumes the back key event - ActionMode is not finished
        }
    }
    return super.dispatchKeyEvent(event);
}*/

//private static ArrayList<Department> dList = new ArrayList<Department>();
//private static ArrayList<String> dName = new ArrayList<String>();
//private static ArrayList<String> dPhone = new ArrayList<String>();
//private static ArrayList<String> dWeb = new ArrayList<String>();

//for(int i=0;i<dList.size();i++){
//	dName.add(dList.get(i).getDName());
//	dPhone.add(dList.get(i).getDPhone());
//	dWeb.add(dList.get(i).getDWeb());
//}

//private void initDummy(){
//	Department d1 = new Department("Biology","111-222-3333","http://biology.uco.edu/");
//	Department d2 = new Department("Chemistry","111-333-2222","http://www.uco.edu/cms/chemistry/");
//	Department d3 = new Department("Computer Science","222-111-3333","http://cs.uco.edu/Home4/index.jsp");
//	Department d4 = new Department("Engineering","333-222-3333","http://www.uco.edu/cms/engineering/");
//	Department d5 = new Department("Funeral Services","333-222-1111","http://www.uco.edu/cms/funeral/index.asp");
//	Department d6 = new Department("Mathematic and Statistic","222-333-1111","http://www.math.uco.edu/");
//	Department d7 = new Department("Nursing","111-111-1111","http://www.uco.edu/cms/nursing/");
//	Department d8 = new Department("Biology","111-222-3333","http://biology.uco.edu/");
//	Department d9 = new Department("Chemistry","111-333-2222","http://www.uco.edu/cms/chemistry/");
//	Department d10 = new Department("Computer Science","222-111-3333","http://cs.uco.edu/Home4/index.jsp");
//	Department d11 = new Department("Engineering","333-222-3333","http://www.uco.edu/cms/engineering/");
//	Department d12 = new Department("Funeral Services","333-222-1111","http://www.uco.edu/cms/funeral/index.asp");
//	Department d13 = new Department("Mathematic and Statistic","222-333-1111","http://www.math.uco.edu/");
//	Department d14 = new Department("Nursing","111-111-1111","http://www.uco.edu/cms/nursing/");
//	
//	dList.add(d1);
//	dList.add(d2);
//	dList.add(d3);
//	dList.add(d4);
//	dList.add(d5);
//	dList.add(d6);
//	dList.add(d7);
//	dList.add(d8);
//	dList.add(d9);
//	dList.add(d10);
//	dList.add(d11);
//	dList.add(d12);
//	dList.add(d13);
//	dList.add(d14);
//}


//void showDialog(){				
//FragmentTransaction ft = getFragmentManager().beginTransaction();
//Fragment prev = getFragmentManager().findFragmentByTag("departmentlist");
//if(prev != null){
//	ft.remove(prev);
//}
//ft.addToBackStack(null);
//
//DialogFragment newFragment = CustomListFragment.newInstance(dName, "UCO CMS DEPARTMENT LIST");
//
//newFragment.show(ft, "departmentlist");
//}

//@Override
//public void onItemClick(int position) {								
//	nId = 1;		
//	NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
//    .setSmallIcon(R.drawable.ic_notification)
//    .setContentTitle(dName.get(position) + " is selected")
//    .setContentText("Click here if you would like to open "+dName.get(position)+" department's website")
//    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//    //only use this for mobile phone
//    .setVibrate(new long[] {0,100,200,300});		
//	
//	Intent i = new Intent(Intent.ACTION_VIEW);
//	i.setData(Uri.parse(dWeb.get(position)));
//	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
//	        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//	
//	/*TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);		
//	stackBuilder.addNextIntent(i);*/
//	
//	PendingIntent pendingIntent = PendingIntent.getActivity(
//	        this,
//	        0,
//	        i,
//	        PendingIntent.FLAG_UPDATE_CURRENT
//	);		
//	nBuilder.setContentIntent(pendingIntent);
//	NotificationManager nm= (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//	nm.notify(nId, nBuilder.build());		
//	
//}

//void closeDialog(){
//	DialogFragment f = (DialogFragment) getFragmentManager().findFragmentByTag("departmentlist");
//	if(f != null){
//		f.dismiss();
//	}
//}
