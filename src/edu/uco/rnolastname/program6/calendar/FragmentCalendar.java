package edu.uco.rnolastname.program6.calendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.ParseException;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;
import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.app.CustomCalendarLinearLayout;
import edu.uco.rnolastname.program6.app.MainActivity;
import edu.uco.rnolastname.program6.dbutilities.Task;
import edu.uco.rnolastname.program6.utilities.SwipeDetector;
import edu.uco.rnolastname.program6.utilities.SwipeDetector.Direction;
import edu.uco.rnolastname.program6.utilities.TimePickerFragment;

public class FragmentCalendar extends Fragment implements OnClickListener{
	OnDateSelectedListener mCallBack;
	public static Button selectedDayMonthYearButton; 	
	
	
	public static Task activeTask = null;
	public static SwipeDetector swipe = new SwipeDetector();	
	private static CustomCalendarLinearLayout mainLayout;
	public static Activity activity;
	private TextView currentMonth; 
	private ImageView prevMonth; private 
	ImageView nextMonth; 
	private static Button btnAddTime;
	private GridView calendarView; 
	private GridCellAdapterCalendar adapter; 
	private Calendar _calendar;
	private int month, year;	
	public static String selectedDate = "";
	public static String selectedTime = "";

	//private final DateFormat dateFormatter = new DateFormat(); 
	private static final String dateTemplate = "MMMM yyyy";
	public static final SimpleDateFormat dateFormatter= new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss aa");	
	public static final SimpleDateFormat dateFormatWriter = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

	public static FragmentCalendar newInstance(Task task){
		FragmentCalendar f = new FragmentCalendar();
		
		Bundle data = new Bundle();
		data.putParcelable("task", task);
		f.setArguments(data);
		return f;
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		this.activity = activity;
		
		try{
			mCallBack = (OnDateSelectedListener) activity;
		}catch(ClassCastException e){
			throw new ClassCastException(activity.toString() + " needs to implement OnDateSelectedListener");
		}
	}
	
	@Override 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		activeTask = getArguments().getParcelable("task");
	}
	
	public static void setSelectedDate(String date){
		selectedDate = date;
	}
	
	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{ 
		//super.onCreateView(inflater, container, savedInstanceState); 
		View v = inflater.inflate(R.layout.calendar, container, false);
		
		//setContentView(R.layout.calendar); 
		_calendar = Calendar.getInstance(Locale.getDefault()); 
		month = _calendar.get(Calendar.MONTH) + 1; 
		year = _calendar.get(Calendar.YEAR); 
		
		//Log.d("DEBUG", "Calendar Instance:= " + "Month: " + month + " " + "Year: " + year); 
		
		selectedDayMonthYearButton = (Button) v.findViewById(R.id.selectedDayMonthYear); 
		selectedDayMonthYearButton.setText("Selected: "); 
		prevMonth = (ImageView) v.findViewById(R.id.prevMonth); 
		prevMonth.setOnClickListener(this); 
		currentMonth = (TextView) v.findViewById(R.id.currentMonth); 
		currentMonth.setText(DateFormat.format(dateTemplate, _calendar.getTime())); 
		nextMonth = (ImageView) v.findViewById(R.id.nextMonth); 
		nextMonth.setOnClickListener(this); 
		calendarView = (GridView) v.findViewById(R.id.calendar);				
		btnAddTime =(Button) v.findViewById(R.id.addTime);
		
		// Initialized 
		adapter = new GridCellAdapterCalendar(v.getContext(),activity, R.id.calendar_day_gridcell, month, year); 
		adapter.notifyDataSetChanged(); 
		calendarView.setAdapter(adapter);	
		
		mainLayout = (CustomCalendarLinearLayout) v.findViewById(R.id.calendar_mainlayout);		
		
		mainLayout.setOnTouchListener(swipe);
		mainLayout.setOnClickListener(this);
		btnAddTime.setOnClickListener(this);				
		
		return v;
	}
	
	@Override
	public void onStart(){
		super.onStart();
		Display d = getActivity().getWindowManager().getDefaultDisplay();
		Point s = new Point();
		d.getSize(s);
		int h = s.x;
		int w = s.y;
		
		if(activeTask == null){
			Toast.makeText(activity, "Please set a task " +
					"first if you want to assign a date for a particular task", Toast.LENGTH_SHORT).show();
		}else{
			if(activeTask.getDate() != null){
				Toast.makeText(activity, "You have already " +
						"assign date for this task: " + activeTask.getDate(), Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(activity, "Long click on date" +
						" to assign date and add time by pressing the plus button", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch(id){
		case R.id.calendar_mainlayout:			
			if(swipe.swipeDetected()){
//				Log.d("DEBUG","swipe detected calendar layout");
				Direction d = swipe.getAction();
				
				if(d == Direction.LR){
					OnDateSelectedListener act = (OnDateSelectedListener) activity;
					act.onDateSelected(null);
				}
			}
			break;
		case R.id.prevMonth:
			if (v == prevMonth) {
				if (month <= 1) {
					month = 12;
					year--;
				} else {
					month--;
				}
				Log.d("DEBUG", "Setting Prev Month in GridCellAdapter: " + "Month: "
						+ month + " Year: " + year);
				setGridCellAdapterToDate(month, year);
			}
			break;
		case R.id.nextMonth:
			if (v == nextMonth) {
				if (month > 11) {
					month = 1;
					year++;
				} else {
					month++;
				}
				Log.d("DEBUG", "Setting Next Month in GridCellAdapter: " + "Month: "
						+ month + " Year: " + year);
				setGridCellAdapterToDate(month, year);
			}		
			break;	
		case R.id.addTime:	
			if(selectedDate != null && !selectedDate.equalsIgnoreCase("")){
				//getTime(selectedDate);
				//((OnDateSelectedListener)activity).onDateSelected(activeTask);
				showTimePickerDialog();
			}else{
				Toast.makeText(getActivity(), "Please set date first", Toast.LENGTH_SHORT).show();
			}
									 			
			break;
		}						
	}

	private void setGridCellAdapterToDate(int month, int year) {
		adapter = new GridCellAdapterCalendar(activity.getApplicationContext(),activity,
				R.id.calendar_day_gridcell, month, year);
		_calendar.set(year, month - 1, _calendar.get(Calendar.DAY_OF_MONTH));
		currentMonth.setText(DateFormat.format(dateTemplate,
				_calendar.getTime()));
		adapter.notifyDataSetChanged();
		calendarView.setAdapter(adapter);		
	}		
	
	
	public static void setSelectedTime(String time) {
		selectedTime = time;		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == MainActivity.TIMEPICKER_ID && resultCode == Activity.RESULT_OK){
			String time = data.getStringExtra("time");
			String finalDate = "";			
			finalDate = selectedDate + " " + time;			
			
//			Log.d("DEBUG","selected date: " + selectedDate);			
			
			//process the time
//			Log.d("DEBUG","pre parsed final date: " + finalDate);
			
			try {
			
				Date parsedDate = dateFormatter.parse(finalDate);
				Date now = new Date();

//				Log.d("DEBUG","parsed date: " + parsedDate);							
//				Log.d("DEBUG","fragment calendar today date: " + now.toString());
				
				if(parsedDate.before(now)){
					Toast.makeText(activity.getApplicationContext(), "Task time can't be before now"
							, Toast.LENGTH_SHORT).show();
					return;
				}else{
					finalDate = dateFormatWriter.format(parsedDate);					
				}															
			} catch (ParseException | java.text.ParseException e) {
				Log.d("DEBUG",e.getMessage());				
			}
		
			if(finalDate != null && !finalDate.equalsIgnoreCase("")){
				activeTask.setDate(finalDate);
				Log.d("DEBUG","task date: " + activeTask.getDate());
				((OnDateSelectedListener)FragmentCalendar.activity).onDateSelected(activeTask);
			}
		
		}
	}
	
	private void showTimePickerDialog(){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment f = getFragmentManager().findFragmentByTag(MainActivity.TIMEPICKER_TAG);
		if(f != null){
			ft.remove(f);						
		}
		ft.addToBackStack(null);
		
		DialogFragment timePickerDialog = new TimePickerFragment();
		timePickerDialog.setTargetFragment(this, MainActivity.TIMEPICKER_ID);
		timePickerDialog.onActivityResult(MainActivity.TIMEPICKER_ID, Activity.RESULT_OK, null);
		
		timePickerDialog.show(getFragmentManager().beginTransaction(), MainActivity.TIMEPICKER_TAG);
		//timePickerDialog.show(ft, MainActivity.TIMEPICKER_TAG);		
							
	}
	
	public void getTime(final String date){					
		Log.d("DEBUG","final date: " + date);
		if(date == null || date.equalsIgnoreCase("")){
			Toast.makeText(activity.getApplicationContext(), "Please select date first by long click on the date", 
					Toast.LENGTH_SHORT).show();
			return;
		}		
		
		
		

		
	}		
	
	public interface OnDateSelectedListener{
		public void onDateSelected(Task task);
	}

	
}
