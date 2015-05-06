package edu.uco.rnolastname.program6.utilities;

import java.util.List;

import edu.uco.rnolastname.program6.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomArrayAdapter<T> extends ArrayAdapter<T>{
	private final LayoutInflater mInflater;
	private int resource;
	public CustomArrayAdapter(Context context, int resource){
		super(context, resource);
		this.resource = resource;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public CustomArrayAdapter(Context context,int resource, int textViewID,List<T> data) {
		super(context,resource,textViewID,data);
		this.resource = resource;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setData(List<T> data){
		clear();
		if(data != null){
			for(T d: data){
				add(d);
			}
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view;
		if(convertView == null){
			view = mInflater.inflate(this.resource, parent,false);
		}else{
			view = convertView;
		}
		
		T item = getItem(position);
		((TextView)view.findViewById(R.id.mapreminder_tasklistitem)).setText(item.toString());
		return view;
	}

}
