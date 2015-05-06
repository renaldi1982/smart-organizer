package edu.uco.rnolastname.program6.utilities;

import java.util.Calendar;

import edu.uco.rnolastname.program6.app.MainActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{	
	private static Activity act;
	
	public interface OnClickTimeSetListener{
		public void onClickTimeSet(String finalTime);
	}
	
	public static TimePickerFragment newInstance(){
		TimePickerFragment f = new TimePickerFragment();				
		
		return f;
	}
	
	@Override public void onAttach(Activity activity){
		super.onAttach(activity);
		this.act = activity;			
	}		
	
	@Override 
	public Dialog onCreateDialog(Bundle savedInstanceState){
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);	
		
		TimePickerDialog timePicker = new TimePickerDialog(getActivity(),this,hour,minute,false);
		timePicker.setTitle("Set Task Time");
		return timePicker;				
	}
	
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		String finalTime = getTimeMinute(hourOfDay,minute);		
				
		Intent i = new Intent();
		i.putExtra("time", finalTime);
		getTargetFragment().onActivityResult(MainActivity.TIMEPICKER_ID, Activity.RESULT_OK, i);
	}

	private static String getTimeMinute(int hourOfDay, int minute){
		int hour=0;
		String min="",amPm = "";
		String result = "";
		
		if(hourOfDay > 12){
			hour = hourOfDay - 12;
			amPm = "PM";
		}else{
			hour =  hourOfDay;
			amPm = "AM";
		}
		
		if(minute < 10){
			min = "0" + minute;
		}else{
			min = "" + minute;
		}
		
		result = ((hour < 10) ?"0" + hour : hour) + ":" + min + ":" + "00" + " " + amPm;				
		return result;
	}
	
}
