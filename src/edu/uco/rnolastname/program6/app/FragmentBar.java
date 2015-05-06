package edu.uco.rnolastname.program6.app;

import java.util.ArrayList;

import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.R.array;
import edu.uco.rnolastname.program6.utilities.SwipeDetector;
import edu.uco.rnolastname.program6.utilities.SwipeDetector.Direction;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class FragmentBar extends Fragment implements AdapterView.OnItemClickListener{
	OnOptionItemSelected mCallBack; 
			
	private Activity a;
	private View v;		
	//private static SwipeDetector sd;
	//Direction d;
	
	public static FragmentBar newInstance(){
		FragmentBar f = new FragmentBar();
		
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);				
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		this.a = activity;
		
		try{
			mCallBack = (OnOptionItemSelected) activity;
		}catch(ClassCastException e){
			throw new ClassCastException(a.toString() + " must implement OnOptionItemSelected");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		v = inflater.inflate(R.layout.bar_fragment,container, false);
		ListView lv = (ListView) v.findViewById(R.id.list_bar);		
				
		ArrayAdapter ad = ArrayAdapter.createFromResource(getActivity(), 
				R.array.navigationdrawer, android.R.layout.simple_list_item_1)	;	
		lv.setAdapter(ad);
//		lv.setOnTouchListener(sd);
		lv.setOnItemClickListener(this);
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try{
//			if(sd.swipeDetected()){
//				Direction as = sd.getAction();
//				if(as == Direction.LR || as == Direction.RL){
//					Toast.makeText(v.getContext(), "Nice swipe!", Toast.LENGTH_LONG).show();					
//				}								
//			}else{
				((OnOptionItemSelected)a).onOptionSelected(position);
			//}
		}catch(ClassCastException e){
			e.printStackTrace();
		}
		
	}
	
	public interface OnOptionItemSelected{
		public void onOptionSelected(int position);					
	}
}
