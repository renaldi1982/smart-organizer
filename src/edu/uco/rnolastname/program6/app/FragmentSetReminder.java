package edu.uco.rnolastname.program6.app;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.dbutilities.Account;
import edu.uco.rnolastname.program6.dbutilities.Task;
import edu.uco.rnolastname.program6.utilities.TaskNameEditText;
import edu.uco.rnolastname.program6.utilities.TaskNameEditText.TaskNameEditTextListener;

public class FragmentSetReminder extends Fragment implements View.OnClickListener,TaskNameEditTextListener{
	OnImageButtonClickListener mCallback;
	//Views
	private static Activity activity = null;
	private static View v = null;
	
	//task attributes
	public static ImageButton btnCalendar;
	private static ImageButton btnVoice;
	private static ImageButton btnPicture;
	private static ImageButton btnMap;
	private static ImageButton btnNote;
	private static Button save;
	private static Button reset;
	public static EditText txtTaskName;
	public static TextView txtDate;
	private static TextView txtNotes;
	private static ImageView imgHasVoice;
	private static ImageView imgHasPicture;
    private static ArrayAdapter<CharSequence> adtPriority;
    private static ArrayAdapter<CharSequence> adtReminder;
    private static Spinner spinPriority;
    private static Spinner spinReminder;
    
    private static String[] reminderTypes;
    private static String[] priorityTypes;
    private static Boolean reminderClicked = false;
    private static Boolean priorityClicked = false;
    
	//task
	private static Account activeAcc = null;
	private static Task activeTask = null;		
	
	private static String taskName = "";
	
	public interface OnImageButtonClickListener{
		public void onImageButtonClick(int which, Task task);
	}
	
	public static FragmentSetReminder newInstance(Account acc, Task task){
		FragmentSetReminder f = new FragmentSetReminder();
		
		Bundle b = new Bundle(2);
		b.putParcelable("activeAcc", acc);
		b.putParcelable("activeTask", task);
		f.setArguments(b);
		
		return f;
	}
	
	@Override 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		activeAcc = getArguments().getParcelable("activeAcc");
		activeTask = getArguments().getParcelable("activeTask");
		if(activeTask == null){
			activeTask = new Task();
		}
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onStart();
		this.activity = activity;
		
		try{
			mCallback = (OnImageButtonClickListener) activity;
		}catch(ClassCastException e){
			throw new ClassCastException(activity.toString() + " have to implement OnImageButtonClickListener");
		}		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
			 
		if(activeAcc == null){
			this.getFragmentManager().popBackStack();
		}			
				
		if(activeTask.getTaskName() == null){
//			Log.d("DEBUG","fragmentsetreminder new task");
			MainActivity.labelTitle.setText("Set New Reminder");
		}else{
			MainActivity.labelTitle.setText("Task: " + activeTask.getTaskName());
			populateFields();
		}
		
		v = inflater.inflate(R.layout.setreminder_fragment, null);
		
		btnCalendar = (ImageButton) v.findViewById(R.id.setreminder_date); 
		btnVoice = (ImageButton) v.findViewById(R.id.setreminder_audioreminder);
		btnPicture = (ImageButton) v.findViewById(R.id.setreminder_picturereminder);
		btnMap = (ImageButton) v.findViewById(R.id.setreminder_mapreminder_map);
		btnNote = (ImageButton) v.findViewById(R.id.setreminder_notebutton);
		save = (Button) v.findViewById(R.id.setreminder_save);
		reset = (Button) v.findViewById(R.id.setreminder_reset);
		txtTaskName = (EditText) v.findViewById(R.id.setreminder_txttaskname);
		txtDate = (TextView) v.findViewById(R.id.setreminder_txtdate);
		txtNotes = (TextView) v.findViewById(R.id.setreminder_picturenote);
		imgHasVoice = (ImageView) v.findViewById(R.id.setreminder_hasaudio);
		imgHasPicture = (ImageView) v.findViewById(R.id.setreminder_picturereminder_picture);
		spinPriority = (Spinner) v.findViewById(R.id.setreminder_spinPriority);
		spinReminder = (Spinner) v.findViewById(R.id.setreminder_spinReminderType);
		
		adtPriority = ArrayAdapter.createFromResource(v.getContext(), 
				R.array.priority, R.layout.spinner_item);
		adtReminder = ArrayAdapter.createFromResource(v.getContext(), 
				R.array.remindertype, R.layout.spinner_item);
		
		spinPriority.setAdapter(adtPriority);
		spinReminder.setAdapter(adtReminder);
		reminderTypes = getResources().getStringArray(R.array.remindertype);
		priorityTypes = getResources().getStringArray(R.array.priority);
		
		populateFields();
		
		save.setOnClickListener(this);
		reset.setOnClickListener(this);
		btnCalendar.setOnClickListener(this);
		btnVoice.setOnClickListener(this);
		btnPicture.setOnClickListener(this);
		btnMap.setOnClickListener(this);
		btnNote.setOnClickListener(this);			
		
		//FIGURE THIS ONE OUT HOW TO DETECT USER CLOSE THE SOFT KEYBOARD
//		txtTaskName.setOnEditorActionListener(l)
		
		txtTaskName.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN){
					
					if(keyCode == KeyEvent.KEYCODE_ENTER){
						Log.d("DEBUG","code enter");
						
						String taskName = txtTaskName.getText().toString().toLowerCase();
						boolean taskNameExists = MainActivity.taskDataSource.isTaskNameExist(taskName);
						Log.d("DEBUG","task name exists? " + taskNameExists);
						if(taskNameExists){
							Toast.makeText(getActivity(), 
									taskName + " already exists", Toast.LENGTH_SHORT).show();
							txtTaskName.setText("");
						}else{
							activeTask.setTaskName(txtTaskName.getText().toString());
						}												
					}else if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
						String taskName = txtTaskName.getText().toString();
						boolean taskNameExists = MainActivity.taskDataSource.isTaskNameExist(taskName);
						if(taskNameExists){
							Toast.makeText(getActivity(), 
									taskName + " already exists", Toast.LENGTH_SHORT).show();
							txtTaskName.setText("");
						}else{
							activeTask.setTaskName(txtTaskName.getText().toString());
						}								
						return false;
					}
				}
				return false;
			}									
		});
		
		spinReminder.setOnItemSelectedListener(new OnItemSelectedListener(){
			
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
								
				if(reminderClicked){
					String reminderType = reminderTypes[position];			
					activeTask.setReminderId(position);
					Toast.makeText(getActivity(), "Task reminder type changed to " 
							+ reminderType, Toast.LENGTH_SHORT).show();
				}				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				activeTask.setReminderId(0);
			}			
			
		});
		
		spinReminder.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				reminderClicked = true;
				return false;
			}
			
		});
		
		spinPriority.setOnItemSelectedListener(new OnItemSelectedListener(){			

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(priorityClicked){
					String priorityType = priorityTypes[position];
					activeTask.setPriority(position);
					Toast.makeText(getActivity(), "Task priority changed to " 
							+ priorityType, Toast.LENGTH_SHORT).show();
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				if(!priorityClicked){
					activeTask.setPriority(0);
				}			
			}
			
		});
						
		spinPriority.setOnTouchListener(new OnTouchListener(){
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {				
				priorityClicked = true;
				return false;
			}
			
		});				
		
		EditText e = new EditText(inflater.getContext()){
			@Override
			public boolean onKeyPreIme(int keyCode, KeyEvent event){
				return super.onKeyPreIme(keyCode, event);
			}
		};
		
		return v;
	}		
	
	
	private void txtTaskName(Context context) {
		// TODO Auto-generated method stub
		
	}

	private void populateFields(){
		
		if(activeTask.getTaskName() != null && !activeTask.getTaskName().equalsIgnoreCase("")){
			txtTaskName.setText(activeTask.getTaskName());
		}
		
		if(activeTask.getDate() != null && !activeTask.getDate().equalsIgnoreCase("")){			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm aa");
			SimpleDateFormat parser = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
			Date d = null;
			try {
				d = parser.parse(activeTask.getDate());
			} catch (ParseException e) {
				Log.d("DEBUG","Error: FragmentSetReminder set date: " + e.getMessage());				
			}
			if(d != null){
				txtDate.setText(formatter.format(d));
				txtDate.setVisibility(0);
			}								
		}
		
		if(activeTask.getPriority() != -1){
			spinPriority.setSelection(activeTask.getPriority(),true);
		}
		
		if(activeTask.getReminderId() != -1){
			spinReminder.setSelection(activeTask.getReminderId(),true);		
		}
		
		if(activeTask.getAudioPath() != null && !activeTask.getAudioPath().equalsIgnoreCase("")){							
			imgHasVoice.setVisibility(0);
			imgHasVoice.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					((OnImageButtonClickListener)getActivity()).onImageButtonClick(1, activeTask);					
				}
				
			});
			
		}else{			
			imgHasVoice.setVisibility(4);
		}
		
		if(activeTask.getPicturePath() != null && !activeTask.getPicturePath().equalsIgnoreCase("")){			
			FragmentPictureReminder.setFullImageFromFilePath(activeTask.getPicturePath(), imgHasPicture);			
			imgHasPicture.setVisibility(0);
			btnPicture.setVisibility(4);
		}else{			
			btnPicture.setVisibility(0);
		}
		if(activeTask.getNotes() != null && !activeTask.getNotes().equalsIgnoreCase("")){
			txtNotes.setText(activeTask.getNotes());
		}else{
			txtNotes.setText("");
		}
	}	
	
	@Override
	public void onClick(View v) {		
		activeTask.setTaskName("");
		activeTask.setTaskName(txtTaskName.getText().toString());
		if(activeTask.getTaskName() == null || activeTask.getTaskName().equalsIgnoreCase("")){
			Toast.makeText(getActivity(), 
					"Please fill in task name first", Toast.LENGTH_SHORT).show();	
			return;
		}
		int id = v.getId();
		OnImageButtonClickListener act = (OnImageButtonClickListener) activity;
		switch(id){
		case R.id.setreminder_date:
			act.onImageButtonClick(0,activeTask);
			break;
		case R.id.setreminder_audioreminder:			
			act.onImageButtonClick(1,activeTask);
			break;
		case R.id.setreminder_picturereminder:
			act.onImageButtonClick(2,activeTask);
			break;
		case R.id.setreminder_save:
			act.onImageButtonClick(3,activeTask);
			break;
		case R.id.setreminder_reset:
			//reset everything to original state
			txtTaskName.setText("");
			txtDate.setText("");
			spinPriority.setSelection(0);
			spinReminder.setSelection(0);
			imgHasVoice.setVisibility(1);
			imgHasPicture.setVisibility(1);
			break;
		case R.id.setreminder_mapreminder_map:						
			act.onImageButtonClick(4,activeTask);
			break;		
		case R.id.setreminder_notebutton:
			act.onImageButtonClick(5, activeTask);
			break;
		}
		
	}

	@Override
	public void onImeBack(TaskNameEditText ctrl, String text) {
		Log.d("DEBUG","closed soft keyboard");
		
	}
}
