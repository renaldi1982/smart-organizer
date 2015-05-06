package edu.uco.rnolastname.program6.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.ParseException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.calendar.FragmentCalendar.OnDateSelectedListener;
import edu.uco.rnolastname.program6.utilities.SwipeDetector;
import edu.uco.rnolastname.program6.utilities.SwipeDetector.Direction;

public class GridCellAdapterCalendar extends BaseAdapter  
implements OnClickListener,OnLongClickListener{	
	
	private final Context context;
	private static Activity activity;
	private static final String tag = "DEBUG";
	public static RelativeLayout gridcellMainLayout;
	private static SwipeDetector swipe = new SwipeDetector();		
	
	private static final int DAY_OFFSET = 1;
	private static ArrayList<Integer> sundays = new ArrayList<Integer>();
 	private final List<String> list = new ArrayList<String>();
	private final String[] weekdays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
	private final String[] months = {"January", "February", "March", "April", "May", "June", "July",
			"August","September","October","November","December"};
	private final int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	
	private static int selectedYear = -1; 
	private static int selectedMonth = -1;
	private static int selectedDay = -1;
	
	private int daysInMonth; 
	private int currentDayOfMonth; 
	private int currentWeekDay; 
	private int currentMonth;
	private Button gridcell; 
	private TextView num_events_per_day; 
	private HashMap<String, Integer> eventsPerMonthMap = new HashMap<String,Integer>(); 
	private static String taskDate;	
	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMMMM-yyyy");	

	public GridCellAdapterCalendar(Context context,Activity activity, int textViewResourceId, int month, int year) {		
		this.context = context;		
//		//Log.d(tag, "==> Passed in Date FOR Month: " + month + " " + "Year: " + year);
		this.activity = activity;
		
		selectedYear = year;
		selectedMonth = month;
		
		Calendar calendar = Calendar.getInstance(); 
		Date d = new Date();
		calendar.setTime(d);
		currentMonth = calendar.get(Calendar.MONTH);				
		setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));		
		setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK)); 
//		//Log.d(tag, "New Calendar:= " + calendar.getTime().toString()); 
//		//Log.d(tag, "CurrentDayOfWeek :" + getCurrentWeekDay()); 
//		//Log.d(tag, "CurrentDayOfMonth :" + getCurrentDayOfMonth()); 
		// Print Month 
		printMonth(month, year); 
		// Find Number of Events 
		eventsPerMonthMap = findNumberOfEventsPerMonth(year, month);		
	}
	
	private String getMonthAsString(int i) { 
		return months[i]; 
	} 
	private String getWeekDayAsString(int i) { 
		return weekdays[i]; 
	} 
	private int getNumberOfDaysOfMonth(int i) { 
		return daysOfMonth[i]; 
	} 
	public String getItem(int position) { 
		return list.get(position); 
	} 
	@Override 
	public int getCount() { return list.size(); }		
	
	@Override
	public void onClick(View v) {
		if(swipe.swipeDetected()){
			
			Direction d = swipe.getAction();
			
			if(d == Direction.LR){				
				OnDateSelectedListener act = (OnDateSelectedListener) activity;
				act.onDateSelected(null);
			}
		}			
		
		String date_month_year = (String) v.getTag();
		FragmentCalendar.selectedDayMonthYearButton.setText("Selected: " + date_month_year);
		////Log.e("Selected date", date_month_year);
		try {
			
			Date parsedDate = dateFormatter.parse(date_month_year);									
			taskDate = dateFormatter.format(parsedDate);
			//Log.d(tag, "Parsed Date: " + parsedDate.toString());
		} catch (ParseException|java.text.ParseException e) {
			e.printStackTrace();
		} 				
	}		
	
	@Override
	public boolean onLongClick(View v) {
		String tag = (String) v.getTag();
		int indexOfDash = tag.indexOf("-");
		
		String date = tag.substring(0,indexOfDash);
		
		//To be saved in database
		String year_month_date = String.valueOf(selectedYear) + "-" + months[selectedMonth-1] + "-" + ((Integer.valueOf(date) < 10) ? "0" + date : date);		
//		Log.d("DEBUG","date: " + tag.substring(0,indexOfDash));		
				
		//To be compared with today's date
		String date_month_year = (String) v.getTag();		
		
		try {
			Date parsedDate = dateFormatter.parse(date_month_year);			
			Date todayDate = new Date();
//			Log.d("DEBUG","grid cell parsed date: " + getZeroTimeDate(parsedDate).toString());
//			Log.d("DEBUG","grid cell today date: " + getZeroTimeDate(todayDate).toString());
//			Log.d("DEBUG","compared selectedDate with todayDate: " + getZeroTimeDate(parsedDate)
//					.compareTo(getZeroTimeDate(todayDate)));
			if(getZeroTimeDate(parsedDate)
					.compareTo(getZeroTimeDate(todayDate)) < 0){
				Toast.makeText(activity, "Date has to be after today!", Toast.LENGTH_SHORT).show();
				FragmentCalendar.setSelectedDate("");
//				FragmentCalendar.selectedDate = "";					
			}else{
//				Log.d("DEBUG","set selected date: " + year_month_date);
				Toast.makeText(activity, "Set Time by pressing the plus button", Toast.LENGTH_SHORT).show();
				FragmentCalendar.setSelectedDate(year_month_date);	
			}
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
									
		return false;
	}		
	
	public static Date getZeroTimeDate(Date d) {
	    Date res = d;
	    Calendar calendar = Calendar.getInstance();

	    calendar.setTime( d );
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);

	    res = calendar.getTime();

	    return res;
	}
	
	private void printMonth(int mm, int yy) {
		//Log.d(tag, "==> printMonth: mm: " + mm + " " + "yy: " + yy); 
		int trailingSpaces = 0; 
		int daysInPrevMonth = 0; 
		int prevMonth = 0; 
		int prevYear = 0; 
		int nextMonth = 0; 
		int nextYear = 0;

		int currentMonth = mm - 1; 
		String currentMonthName = getMonthAsString(currentMonth); 
		daysInMonth = getNumberOfDaysOfMonth(currentMonth); 
		//Log.d(tag, "Current Month: " + " " + currentMonthName + " having " + daysInMonth + " days."); 
		GregorianCalendar cal = new GregorianCalendar(yy, currentMonth, 1); 
		//Log.d(tag, "Gregorian Calendar:= " + cal.getTime().toString()); 
		 
		if (currentMonth == 11) { 
			prevMonth = currentMonth - 1; 
			daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth); 
			nextMonth = 0; 
			prevYear = yy; 
			nextYear = yy + 1; 
			//Log.d(tag, "*->PrevYear: " + prevYear + " PrevMonth:" + prevMonth + " NextMonth: " + nextMonth + " NextYear: " + nextYear); 
 		} else if (currentMonth == 0) { 
			prevMonth = 11; 
			prevYear = yy - 1; 
			nextYear = yy; 
			daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
			nextMonth = 1; 
			//Log.d(tag, "**--> PrevYear: " + prevYear + " PrevMonth:" + prevMonth + " NextMonth: " + nextMonth + " NextYear: " + nextYear); 
 		} else { 
			prevMonth = currentMonth - 1; 
			nextMonth = currentMonth + 1; 
			nextYear = yy; prevYear = yy; 
			daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth); 
			//Log.d(tag, "***---> PrevYear: " + prevYear + " PrevMonth:" + prevMonth + " NextMonth: " + nextMonth + " NextYear: " + nextYear); 
		}

		int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1; 
		trailingSpaces = currentWeekDay; 
		//Log.d(tag, "Week Day:" + currentWeekDay + " is " + getWeekDayAsString(currentWeekDay)); 
		//Log.d(tag, "No. Trailing space to Add: " + trailingSpaces); 
		//Log.d(tag, "No. of Days in Previous Month: " + daysInPrevMonth);

		if (cal.isLeapYear(cal.get(Calendar.YEAR))) 
			if (mm == 2) ++daysInMonth; 
			else if (mm == 3) ++daysInPrevMonth; 
 
		// Trailing Month days 
		for (int i = 0; i < trailingSpaces; i++) { 
//			//Log.d(tag, "PREV MONTH:= " + prevMonth + " => " + getMonthAsString(prevMonth) + " " + String.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET) + i)); 
		list.add(String .valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET) + i) + "-GREY" + "-" + getMonthAsString(prevMonth) + "-" + prevYear); 
		} 
 
		//get what day is date 1 is? 
		int weekDayCounter = trailingSpaces;
		// Current Month Days 
		for (int i = 1; i <= daysInMonth; i++) { 						
			//Log.d(tag, String.valueOf(i) + " " + getMonthAsString(currentMonth) + " " + yy); 
			
			if(weekDayCounter == 7){
				weekDayCounter = 0;
			}			
//			//Log.d("DEBUG","WEEK DAY COUNTER: " + weekdays[weekDayCounter] 
//					+ " - date: " + i + " - month: " + getMonthAsString(currentMonth)
//					+ " - compared with: " + weekdays[0]);
			if (i == getCurrentDayOfMonth() && 
					getMonthAsString(currentMonth).equalsIgnoreCase(months[currentMonth])) { 				
				list.add(String.valueOf(i) + "-BLUE" + "-" + getMonthAsString(currentMonth) + "-" + yy); 
			}
			else if(weekdays[weekDayCounter].equalsIgnoreCase(weekdays[6]) 
					|| weekdays[weekDayCounter].equalsIgnoreCase(weekdays[0])){
				//Log.d("DEBUG","WEEKENDS");
				list.add(String.valueOf(i) + "-RED" + "-" + getMonthAsString(currentMonth) + "-" + yy);
			}			
			else { 
				list.add(String.valueOf(i) + "-WHITE" + "-" + getMonthAsString(currentMonth) + "-" + yy); 
				} 
			
			
			weekDayCounter++;
			////Log.d("DEBUG","REY DAY: " + weekdays[weekDayCounter-1]);
		}

		// Leading Month days 
		for (int i = 0; i < list.size() % 7; i++) { 
			//Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth)); 
			list.add(String.valueOf(i + 1) + "-GREY" + "-" + getMonthAsString(nextMonth) + "-" + nextYear); 
		} 
	 
	}
	 
	private HashMap<String, Integer> findNumberOfEventsPerMonth(int year, int month) { 
		HashMap<String, Integer> map = new HashMap<String, Integer>(); 
		return map; 
	} 
	 
	@Override 
	public long getItemId(int position) { 
		return position; 
	} 

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.calendar_gridcell, parent, false);
		}
	
		// Get a reference to the Day gridcell
		gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
		gridcell.setOnClickListener(this);
		gridcell.setOnLongClickListener(this);
		gridcell.setOnTouchListener(swipe);
		// ACCOUNT FOR SPACING
	
		//Log.d(tag, "Current Day: " + getCurrentDayOfMonth());
		
		String[] day_color = list.get(position).split("-");
		String theday = day_color[0];
		String themonth = day_color[2];
		String theyear = day_color[3];
		if ((!eventsPerMonthMap.isEmpty()) && (eventsPerMonthMap != null)) {
			if (eventsPerMonthMap.containsKey(theday)) {
				num_events_per_day = (TextView) row
						.findViewById(R.id.num_events_per_day);
				Integer numEvents = (Integer) eventsPerMonthMap.get(theday);
				num_events_per_day.setText(numEvents.toString());
			}
		}
	
		// Set the Day GridCell
		gridcell.setText(theday);
		gridcell.setTag(theday + "-" + themonth + "-" + theyear);
		//Log.d(tag, "Setting GridCell " + theday + "-" + themonth + "-"
		//		+ theyear);
		
		if (day_color[1].equals("GREY")) {
			gridcell.setTextColor(context.getResources()
					.getColor(R.color.Gray));
		}
		if(day_color[1].equals("RED")){
			gridcell.setTextColor(context.getResources().getColor(
					R.color.red));
		}
		if (day_color[1].equals("WHITE")) {
			gridcell.setTextColor(context.getResources().getColor(
					R.color.white));
		}		
		if (day_color[1].equals("BLUE") && themonth.equals(months[currentMonth])) {					
				gridcell.setTextColor(context.getResources().getColor(R.color.yellow));
			}
		return row;
	}
	
	public int getCurrentDayOfMonth() {
		return currentDayOfMonth;
	}

	private void setCurrentDayOfMonth(int currentDayOfMonth) {
		this.currentDayOfMonth = currentDayOfMonth;
	}

	public void setCurrentWeekDay(int currentWeekDay) {	
		if(currentWeekDay == Calendar.SUNDAY){
			sundays.add(currentWeekDay);
		}
		this.currentWeekDay = currentWeekDay;
	}

	public int getCurrentWeekDay() {
		return currentWeekDay;
	}		
}
