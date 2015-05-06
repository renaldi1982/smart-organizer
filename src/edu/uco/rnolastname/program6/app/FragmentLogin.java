package edu.uco.rnolastname.program6.app;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.uco.rnolastname.program6.R;

public class FragmentLogin extends DialogFragment implements OnClickListener{
	private Activity activity;	
	private View v;
	
	private Button loginCancel;
	private Button loginOK;
	private EditText txtUsername;
	private EditText txtPassword;	
	private static String username;
	public static TextView txtHelp;
	
	private Bundle loginInfo;
	
	static FragmentLogin newInstance(String username){
		FragmentLogin f = new FragmentLogin();
				
		Bundle data = new Bundle();
		data.putString("username", username);
		f.setArguments(data);
		return f;	
	}
	
	public interface LoginDialogListener {
	    void onLoginFinish(String inputUsername, String inputPassword);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		username = getArguments().getString("username");
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		this.activity = activity;					
	}		
	
	@Override
	public void onResume(){
		super.onResume();
		Window window = getDialog().getWindow();
		window.setLayout(900, 900);
		window.setGravity(Gravity.CENTER);				
		
		/* set ontouchevent so that the keyboard get off the screen when the user press anywhere on the screen */
		/* Frame Layout for account signup */	     
		View entire_v = (View) v.findViewById(R.id.acclogin_layout);
		entire_v.setOnTouchListener(new OnTouchListener(){
	    	@Override
	    	public boolean onTouch(View v, MotionEvent e){	   	    		
	    		InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	    		imm.hideSoftInputFromWindow(v.getWindowToken(),0);
	    		return true;
	    	}			
	    });
	}
	
	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);								
		v = inflater.inflate(R.layout.acc_login,container,false); 
		
		loginCancel = (Button) v.findViewById(R.id.btnlogin_cancel);
		loginOK = (Button) v.findViewById(R.id.btnlogin_ok);
		txtUsername = (EditText) v.findViewById(R.id.login_username);
		txtPassword = (EditText) v.findViewById(R.id.login_password);
		txtHelp = (TextView) v.findViewById(R.id.txt_help);						      	
		
		if(!username.equals("")){
			txtUsername.setText(username);
		}
		
		getDialog().setTitle("Login");										
		
		loginCancel.setOnClickListener(this);
		loginOK.setOnClickListener(this);
		
		txtPassword.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN){
					if(keyCode == KeyEvent.KEYCODE_ENTER){
						((LoginDialogListener) activity).onLoginFinish(txtUsername.getText().toString(),txtPassword.getText().toString());
			        	getDialog().dismiss();
					}
				}
				return false;
			}
			
		});
		
		return v; 
	}

	@Override
	public void onClick(View v) {				
		LoginDialogListener activity = (LoginDialogListener) getActivity();
		switch(v.getId()){
		case R.id.btnlogin_cancel:			
        	activity.onLoginFinish("","");
        	getDialog().dismiss();
			break;
		case R.id.btnlogin_ok:			
        	activity.onLoginFinish(txtUsername.getText().toString(),txtPassword.getText().toString());
        	getDialog().dismiss();
			break;
		}		
	}
	
//	@Override
//	public Dialog onCreateDialog(Bundle savedInstanceState){	
//		LayoutInflater inflater = activity.getLayoutInflater();
//		v = inflater.inflate(R.layout.acc_login,null); 
//		
//		txtUsername = (EditText) v.findViewById(R.id.login_username);
//		txtPassword = (EditText) v.findViewById(R.id.login_password);
//		txtHelp = (TextView) v.findViewById(R.id.txt_help);		
//		
//		AlertDialog.Builder b =  new  AlertDialog.Builder(getActivity())
//	    .setTitle("Login Information")
//	    .setPositiveButton("OK",
//	        new DialogInterface.OnClickListener() {
//	            public void onClick(DialogInterface dialog, int whichButton) {
//	            	LoginDialogListener activity = (LoginDialogListener) getActivity();
//	            	activity.onFinishDialog(txtUsername.getText().toString(),txtPassword.getText().toString());
//	            	dialog.dismiss();
//	            }
//	        }
//	    )
//	    .setNegativeButton("Cancel",
//	        new DialogInterface.OnClickListener() {
//	            public void onClick(DialogInterface dialog, int whichButton) {
//	            	LoginDialogListener activity = (LoginDialogListener) getActivity();
//	            	activity.onFinishDialog("","");
//	            	dialog.dismiss();
//	            }
//	        }
//	    );	    	    	    
//	    	    
//	    b.setView(v);
//	    return b.create();
//	}
}
