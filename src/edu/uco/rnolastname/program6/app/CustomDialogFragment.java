package edu.uco.rnolastname.program6.app;

import android.app.DialogFragment;
import android.os.Bundle;

public class CustomDialogFragment extends DialogFragment {

	public interface CustomDialogListener {
	    void onFinishDialog(String input1, String input2);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
}
