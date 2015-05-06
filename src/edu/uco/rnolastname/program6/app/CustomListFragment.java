package edu.uco.rnolastname.program6.app;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.uco.rnolastname.program6.R;

public class CustomListFragment extends DialogFragment implements AdapterView.OnItemClickListener{
	OnCustomListItemClickListener mCallback;
	
	private ArrayList<String> dName = new ArrayList<String>();
	private Activity activity;
	private String listName;	
	private ListView lv;		
	private View v;
				
	static CustomListFragment newInstance(ArrayList<String> list,String listName){
		CustomListFragment f = new CustomListFragment();
		
		Bundle args = new Bundle(2);				
		args.putStringArrayList("dName", list);
		args.putString("listName", listName);
		f.setArguments(args);
		return f;	
	}
	
	public void onAttach(Activity activity){
		super.onAttach(activity);
		this.activity = activity;
		
		try{
			mCallback = (OnCustomListItemClickListener) activity;
		}catch(ClassCastException e){
			throw new ClassCastException(activity.toString() + " must implement OnSelectedItemListener");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		// populate local arraylist var with the department name
		dName = getArguments().getStringArrayList("dName");		
		listName = getArguments().getString("listName");		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		final Context cw = new ContextThemeWrapper(activity,R.style.AppBaseTheme);
		LayoutInflater li = inflater.cloneInContext(cw);
		v = li.inflate(R.layout.deptlists_fragment,container, false);
		lv = (ListView) v.findViewById(R.id.dept_list);				
				
		ArrayAdapter<String> ad = new ArrayAdapter<String>(v.getContext(),android.R.layout.simple_list_item_1,dName);
		lv.setAdapter(ad);				
											
		lv.setOnItemClickListener(this);
		Dialog d = getDialog();
		d.setTitle(listName);		
		
		return v;
	}

	public interface OnCustomListItemClickListener{
		public void onItemClick(int position);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {				
		try{			
			((OnCustomListItemClickListener)activity).onItemClick(position);
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}				
}
