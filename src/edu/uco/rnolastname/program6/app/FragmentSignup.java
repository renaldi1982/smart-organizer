package edu.uco.rnolastname.program6.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.dbutilities.Account;

public class FragmentSignup extends Fragment implements 
	OnFocusChangeListener{	
	
	private static Activity activity;
	private View v;	
	
	private int cityIndex = -1;
		
	private static Account activeAcc; 
	
	private String[] list_states;
	private ArrayAdapter<CharSequence> adt_states;
	private ArrayAdapter<String> adt_city;
	private ArrayAdapter<CharSequence> adt_months;
	private ArrayAdapter<Integer> adt_days;
	private ArrayAdapter<Integer> adt_years;
	private ArrayList<String> list_city = new ArrayList<String>();
	private static ArrayList<Integer> list_day = new ArrayList<>();
	private static ArrayList<Integer> list_year = new ArrayList<>();
	
	private static String URL = null;
	
	//Account information views
	public static EditText txtUsername;
	public static EditText txtPassword;
	
	//User information views
	public static EditText txtName;
	public static EditText txtAddress;
	public static EditText txtZip;
	public static EditText txtEmail;
	public static RadioGroup radgroupSex;
	public static RadioButton radMale;
	public static RadioButton radFemale;	
	public static Spinner spinner_list_city;
	public static Spinner spinner_list_state;
	public static Spinner spinner_list_months;
	public static Spinner spinner_list_days;
	public static Spinner spinner_list_years;
	
	//Account information
	public static String username;
	public static String password;
	
	//User information		
	public static String name;
	public static String gender;
	public static String dob;
	public static String address;
	public static String city;
	public static String state;
	public static int zip;
	public static String email;	
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		FragmentSignup.activity = activity;
	}		
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){		
		//super.onCreateView(inflater, container, savedInstanceState);
		final Context cw = new ContextThemeWrapper(activity,R.style.AppBaseTheme);
		LayoutInflater li = inflater.cloneInContext(cw);
		v = li.inflate(R.layout.acc_signup,container,false);					
		
		Resources res = activity.getResources();		
		list_states = res.getStringArray(R.array.state_list);
		
		// Account Information
		txtUsername = (EditText) v.findViewById(R.id.txt_username);
		txtPassword = (EditText) v.findViewById(R.id.txt_password);		
		
		// User Information
		txtName = (EditText) v.findViewById(R.id.txt_fname);		
		radgroupSex = (RadioGroup) v.findViewById(R.id.radgroup_sex);
		radMale = (RadioButton) v.findViewById(R.id.rad_male);
		radFemale = (RadioButton) v.findViewById(R.id.rad_female);
		txtAddress = (EditText) v.findViewById(R.id.txt_address);
		txtZip = (EditText) v.findViewById(R.id.txt_zip);
		txtEmail = (EditText) v.findViewById(R.id.txt_email);		
		spinner_list_months = (Spinner) v.findViewById(R.id.spinner_dob_month);
		spinner_list_days = (Spinner) v.findViewById(R.id.spinner_dob_day);
		spinner_list_years = (Spinner) v.findViewById(R.id.spinner_dob_year);
		spinner_list_state = (Spinner) v.findViewById(R.id.list_state);
		spinner_list_city = (Spinner) v.findViewById(R.id.list_city);	
		
		//Set listener on focus change for all of the edit text
		txtUsername.setOnFocusChangeListener(this);
		txtPassword.setOnFocusChangeListener(this);
		txtName.setOnFocusChangeListener(this);
		txtAddress.setOnFocusChangeListener(this);
		txtZip.setOnFocusChangeListener(this);
		txtEmail.setOnFocusChangeListener(this);
				
		// Spinner adapter for months
		adt_months = ArrayAdapter.createFromResource(v.getContext(), R.array.list_month, 
				android.R.layout.simple_list_item_1);
		adt_months.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_list_months.setAdapter(adt_months);
		
		// Get days and years 
		getDays();
		getYears();
		
		// Spinner adapter for days		
		adt_days = new ArrayAdapter<Integer>(v.getContext(), android.R.layout.simple_list_item_1, list_day);
		adt_days.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_list_days.setAdapter(adt_days);
		
		// Spinner adapter for years		
		adt_years = new ArrayAdapter<Integer>(v.getContext(), android.R.layout.simple_list_item_1, list_year);
		adt_years.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_list_years.setAdapter(adt_years);						
		
		// Spinner adapter for states
		adt_states = ArrayAdapter.createFromResource(v.getContext(),
		        R.array.state_list, android.R.layout.simple_spinner_item);
		adt_states.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		if(adt_states != null && spinner_list_state != null){
			spinner_list_state.setAdapter(adt_states);			
		}				
						
		// Spinner adapter for city
		adt_city = new ArrayAdapter<String>(v.getContext(),android.R.layout.simple_spinner_item,list_city);
		adt_city.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_list_city.setAdapter(adt_city);	
		
		spinner_list_state.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(spinner_list_state.getAdapter().getCount()> 0 && isConnected()){
					URL = "http://api.sba.gov/geodata/city_links_for_state_of/" + list_states[position]
							.toLowerCase() + ".json";
					
					/* get list city based on the state */
					new HttpAsnycTask().execute(URL);						
				}else{
					Toast.makeText(v.getContext(), "You are offline", Toast.LENGTH_SHORT).show();
				}				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});				
		
		spinner_list_city.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		radgroupSex.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {								
				switch(checkedId){
				case R.id.rad_male:
					gender = radMale.getText().toString();
					break;
				case R.id.rad_female:
					gender = radFemale.getText().toString();
					break;
				}
			}		
		});				
		
		/* set ontouchevent so that the keyboard get off the screen when the user press anywhere on the screen */
		/* Frame Layout for account signup */
	    View entire_v = (View) v.findViewById(R.id.signup_layout);
	    entire_v.setOnTouchListener(new OnTouchListener(){
	    	@Override
	    	public boolean onTouch(View v, MotionEvent e){
	    		if(activity != null){
	    			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		    		imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),0);		    		
	    		}
	    		return true;
	    	}			
	    });	    	    
	    
	    return v;
	}						
	
	@Override 
	public void onStart(){
		super.onStart();
	
		//check if there's an active Acc 
	    FragmentSignup.activeAcc = MainActivity.activeAcc;	    
	    if(FragmentSignup.activeAcc != null){	    	
	    	populateFields();
	    }
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(!MainActivity.isLogin()){
			Toast.makeText(getActivity(), "New user has not been created", Toast.LENGTH_SHORT).show();
		}
	}
				
	public Bundle getValues() throws java.text.ParseException{				
		username = txtUsername.getText().toString();
		password = txtPassword.getText().toString();		
		
		//user's first and last name
		name = txtName.getText().toString();
		String fname = "";
		String lname = "";		
		if(!name.equals("")){
			String[] name_count;
			if(name.contains(",")){
				name_count = name.split(","); 
				
				if(name_count.length > 1){
					fname = name.substring(0, name.indexOf(","));
					lname = name.substring(name.indexOf(" ")+1,name.length());
				}else{
					fname = name;
					lname = "";
				}
			}else if(name.contains(".")){
				name_count = name.split(".");
				
				if(name_count.length > 1){
					fname = name.substring(0, name.indexOf("."));
					lname = name.substring(name.indexOf(" ")+1,name.length());
				}else{
					fname = name;
					lname = "";
				}
			}			
			else{
				name_count = name.split(" ");
				
				if(name_count.length > 1){
					fname = name.substring(0, name.indexOf(" "));
					lname = name.substring(name.indexOf(" ")+1,name.length());
				}else{
					fname = name;
					lname = "";
				}
			}							
		}
		
		// convert string date to properly date format
		String ddob = String.valueOf(spinner_list_years.getSelectedItem().toString() + "-"
				+ spinner_list_months.getSelectedItem().toString() + "-"
				+ spinner_list_days.getSelectedItem().toString());		
		SimpleDateFormat sddf = new SimpleDateFormat("yyyy-MMM-dd",Locale.US);
		Date d = null;
		try{
			d = sddf.parse(ddob);
		}catch(ParseException e){
			e.printStackTrace();
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd",Locale.US);		
		dob =  sdf.format(d);
		
		address = txtAddress.getText().toString();
		if(!txtZip.getText().toString().equals(""))
			zip = Integer.parseInt(txtZip.getText().toString());
		if(spinner_list_city.getSelectedItem() != null){
			city = spinner_list_city.getSelectedItem().toString();			
		}		
		if(spinner_list_city.getSelectedItem() != null){
			state = spinner_list_state.getSelectedItem().toString();
		}
				
		email = txtEmail.getText().toString();
		
		Bundle accountInfo = new Bundle(11);				
		accountInfo.putString("username", username);
		accountInfo.putString("password", password);
		accountInfo.putString("fname", fname);
		accountInfo.putString("lname", lname);		
		accountInfo.putString("address", address);
		accountInfo.putString("city", city);
		accountInfo.putString("state", state);
		accountInfo.putInt("zip", zip);
		accountInfo.putString("dob", dob);
		accountInfo.putString("gender", gender);
		accountInfo.putString("email", email);		
						
		return accountInfo;
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		int id = v.getId();						
		
		if(!hasFocus){								
			switch(id){
			case R.id.txt_username:
				if(txtUsername.length() <= 0){
					Toast.makeText(v.getContext(), "Please enter username", Toast.LENGTH_SHORT).show();				
					//txtUsername.requestFocus();
					//activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
				break;
			case R.id.txt_password:
				if(txtPassword.length() <= 0){
					Toast.makeText(v.getContext(), "Please enter password", Toast.LENGTH_SHORT).show();					
				}
				break;
			case R.id.txt_fname:
				if(txtName.length() <= 0){
					Toast.makeText(v.getContext(), "Please enter name", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.txt_address:
				if(txtAddress.length() <= 0){
					Toast.makeText(v.getContext(), "Please enter address", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.txt_zip:
				if(txtZip.length() <= 0){
					Toast.makeText(v.getContext(), "Please enter zip code", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.txt_email:
				if(txtEmail.length() <= 0){
					Toast.makeText(v.getContext(), "Please enter email", Toast.LENGTH_SHORT).show();
				}
				break;			
			}
		}			
	}	
	
	private void populateFields(){
		txtUsername.setText(activeAcc.getUsername());
		txtPassword.setEnabled(false);			
		
		// User Information
		txtName.setText(activeAcc.getlName() + " " + activeAcc.getfName());		
		if(activeAcc.getGender().equalsIgnoreCase("male"))			
			radMale.setChecked(true);
		else
			radFemale.setChecked(true);
		
		txtAddress.setText(activeAcc.getAddress());		
		
		txtZip.setText(String.valueOf(activeAcc.getZip()),BufferType.EDITABLE);
		txtEmail.setText(activeAcc.getEmail());
		
		String dob = activeAcc.getDob();
		
		String dob_month,dob_day,dob_year;		
		String[] dob_array = dob.split("-");		
		
		if(dob_array.length > 0){
			dob_year = dob_array[0];
			dob_month = dob_array[1];
			dob_day = dob_array[2];
			
			int month_pos,day_pos,year_pos;
			
			month_pos = getMonthPosition(dob_month);
			day_pos = getDayPosition(dob_day);
			year_pos = getYearPosition(dob_year);
			
			if(month_pos == -1 || day_pos == -1 || year_pos == -1){
				Log.d("DEBUG","Error - FragmentSignup loading user dob");
				return;
			}
			
			spinner_list_months.setSelection(month_pos);
			spinner_list_days.setSelection(day_pos);
			spinner_list_years.setSelection(year_pos);
			
		}else{
			Log.d("DEBUG","Error - FragmentSignup loading user dob");
		}
				
		String state = activeAcc.getState();		
		
		if(state != null){			
			int stateIndex = getStateIndex(state);
			
			Log.d("DEBUG","city index: " + cityIndex);
			
			spinner_list_state.setSelection(stateIndex);
//			spinner_list_city = (Spinner) v.findViewById(R.id.list_city);
		}else{
			Log.d("DEBUG","Error - FragmentSignup loading user city and state");
		}
					
	}	
	
	private int getMonthPosition(String month){
		int monthIndex = -1;
		monthIndex = Integer.parseInt(month);
		
		return monthIndex-1;
	}
	
	private int getDayPosition(String day){
		int dayIndex = -1;		
		dayIndex = Integer.parseInt(day);
				
		return dayIndex-1;
	}
	
	private int getYearPosition(String year){
		int position = -1;
		
		int index = 0;
		for(Integer i: list_year){
			if(Integer.parseInt(year) == i){
				position = index;
			}
			index++;
		}
		
		return position;
	}	
	
	private int getStateIndex(String state){
		int position = -1;
		
		int index = 0;
		String[] states = getResources().getStringArray(R.array.state_list);
		for(String s: list_states){
			if(state.equals(s)){
				position = index;
			}
			index++;
		}
		
		return position;
	}
	
	private static void getDays(){
		for(int i=1;i<=31;i++){
			list_day.add(i);
		}
	}
	
	private static void getYears(){
		for(int i=1940;i<2015;i++){
			list_year.add(i);
		}
	}
	
	private static boolean isConnected(){
		boolean connected = false;
		
		ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if(ni != null && ni.isConnected()){
			connected = true;
		}
		
		return connected;
	}
	
	private static String convertInputStreamToString(InputStream is) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = "";
		String result = "";
		
		while((line = br.readLine()) != null){
			result += line;
		}
		
		is.close();
		return result;
	}
	
	private String GET(String url){
		String result = "";
		if(isConnected()){
			InputStream is = null;						
			try{
				HttpClient hc = new DefaultHttpClient();
				
				HttpResponse hr = hc.execute(new HttpGet(url));
				
				is = hr.getEntity().getContent();
				if(is != null){
					result = convertInputStreamToString(is);
				}else
					result = "An Error occured when converting input stream to string";
				
			}catch(IOException e){
				e.printStackTrace();
			}
		}				
		return result;
	}
	
	private class HttpAsnycTask extends AsyncTask<String,Void,String>{
		private ProgressDialog progressDialog;
		
		@Override
		protected void onPreExecute(){
			progressDialog = new ProgressDialog(v.getContext());
	        progressDialog.setMessage("Load city...");
	        progressDialog.setIndeterminate(true);
	        progressDialog.show();
			
		}
		
		@Override
		protected String doInBackground(String... urls) {
			
			return GET(urls[0]);
		}
		
		@Override
        protected void onPostExecute(String result) {             
			progressDialog.hide(); 
            try {            				
            	list_city.clear();
				JSONArray cities = new JSONArray(result);
				for(int i = 0; i< cities.length();i++){
					JSONObject city = cities.getJSONObject(i);
					list_city.add(city.getString("name"));										
				}							
				Collections.sort(list_city);
				adt_city.notifyDataSetChanged();
				if(!MainActivity.isLogin()){
					spinner_list_city.setSelection(0, true);
				}else{
					int position = -1;
					int index = 0;
					for(String c: list_city){
						if(activeAcc.getCity().equalsIgnoreCase(c)){
							position = index;
						}
						index++;
					}
					if(position != -1){
						spinner_list_city.setSelection(position, true);
					}					
				}
				
			} catch (JSONException e) { 
				e.printStackTrace();
			}
       }
	}	
}

/* Use below if want to use your own style for the spinner*/
/*{
    public View getView(int position, View convertView,
            ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        //((TextView) v).setTextSize(16);
        ((TextView) v).setTextColor(
                getResources()
                .getColorStateList(R.color.black_50transparent));
        //((TextView) v).setText("City...");
        return v;
    }

    public View getDropDownView(int position, View convertView,
            ViewGroup parent) {
        View v = super.getDropDownView(position, convertView,
                parent);
        //v.setBackgroundResource(R.drawable.item_pressed);

        ((TextView) v).setTextColor(getResources().getColorStateList(
                        R.color.black));				                    
        ((TextView) v).setGravity(Gravity.CENTER);

        return v;
    }
};*/
