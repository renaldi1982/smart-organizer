package edu.uco.rnolastname.program6.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import edu.uco.rnolastname.program6.R;

public class FragmentPictureReminderNote extends DialogFragment{
	OnNoteFinishDialogListener mCallback;
	private Activity activity;		
	private View v;
    private EditText txtNote;	    
	
	static FragmentPictureReminderNote newInstance(){
		FragmentPictureReminderNote f = new FragmentPictureReminderNote();
					
		return f;	
	}		
	
	public interface OnNoteFinishDialogListener {
	    void onNoteFinishDialog(String note);
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		this.activity = activity;	
		
		try{
			mCallback = (OnNoteFinishDialogListener)activity;
		}catch(ClassCastException e){
			throw new ClassCastException(this.getActivity() + " need to implement OnNoteDialogFinishListener");
		}
	}				
	
	@Override
	public void onResume(){
		super.onResume();
		Window window = getDialog().getWindow();
		window.setLayout(900, 900);
		window.setGravity(Gravity.CENTER);
		
//		/* set ontouchevent so that the keyboard get off the screen when the user press anywhere on the screen */
//		/* Frame Layout for account signup */	     
//		View entire_v = (View) v.findViewById(R.id.acclogin_layout);
//		entire_v.setOnTouchListener(new OnTouchListener(){
//	    	@Override
//	    	public boolean onTouch(View v, MotionEvent e){	   	    		
//	    		InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//	    		imm.hideSoftInputFromWindow(v.getWindowToken(),0);
//	    		return true;
//	    	}			
//	    });
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){			
		LayoutInflater i = getActivity().getLayoutInflater();
		v = i.inflate(R.layout.picturereminder_note,null);		
		txtNote = (EditText) v.findViewById(R.id.txt_note);						
		final String note;
		
		if(txtNote == null){
			Log.d("DEBUG","Error has occured FragmentPictureReminderNote txtNote is null");
			this.dismiss();
		}				
						
		AlertDialog.Builder b =  new  AlertDialog.Builder(getActivity())
	    .setTitle("Picture Note")
	    .setPositiveButton("OK",
	        new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	String note = txtNote.getText().toString();
	            	((OnNoteFinishDialogListener)activity).onNoteFinishDialog(note);  	            	
	            	dialog.dismiss();
	            }
	        }
	    )
	    .setNegativeButton("Cancel",
	        new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {	            		            		            	
	            	((OnNoteFinishDialogListener)activity).onNoteFinishDialog("");
	            	dialog.dismiss();	            	
	            }
	        }
	    );	    	    	    	    	    		
		
	    b.setView(v);
	    return b.create();
	}
}
