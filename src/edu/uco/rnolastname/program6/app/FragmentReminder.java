package edu.uco.rnolastname.program6.app;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.dbutilities.Task;
import edu.uco.rnolastname.program6.utilities.StableArrayAdapterTask;
import edu.uco.rnolastname.program6.utilities.SwipeDetector;
import edu.uco.rnolastname.program6.utilities.SwipeDetector.Direction;

public class FragmentReminder extends Fragment implements OnClickListener,OnItemClickListener{
	OnTaskClickListener itemCallback;
	OnCreateNewTaskListener mCallback;
	private static Activity activity;
	private static View v;
	
	private static SwipeDetector swipe = new SwipeDetector();
	private ArrayList<Task> tasks = new ArrayList<Task>();
	private static ListView listTask;
	private static ImageButton btnNewTask;
	private static ImageButton btnLeft;
	private static ImageButton btnRight;
	private TextView rangeTV;	
	private TextView headingTV;
	private static ArrayAdapter<Task> adapter;
	
    //StableArrayAdapterTask
    private static boolean mItemPressed;
    private static boolean mSwiping;
    public static StableArrayAdapterTask listTaskStableAdapter;	    
    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;
    HashMap<Long, Integer> mItemIdTopMap = new HashMap<Long, Integer>();
    
	public interface OnTaskClickListener{
		public void onTaskClick(Task task);
	}
	
	public interface OnCreateNewTaskListener{
		public void onCreateNewTask();
	}
	
	private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
		float mDownX;
		private int mSwipeSlop = -1;
		
		@Override
		public boolean onTouch(final View v, MotionEvent event) {
			if(mSwipeSlop < 0){
				mSwipeSlop = ViewConfiguration.get(getActivity())
						.getScaledTouchSlop();				
			}
			
			switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				if(mItemPressed){
					return false;
				}
				mItemPressed = true;
				mDownX = event.getX();
				break;
			case MotionEvent.ACTION_CANCEL:
				v.setAlpha(1);
				v.setTranslationX(0);
				mItemPressed = false;
				break;				
			case MotionEvent.ACTION_MOVE:
			{
				float x = event.getX() + v.getTranslationX();
				float deltaX = x - mDownX;
				float deltaXAbs = Math.abs(deltaX);
				
				if(!mSwiping){
					if(deltaXAbs > mSwipeSlop){
						mSwiping = true;
						listTask.requestDisallowInterceptTouchEvent(true);
						//mBackGroundContainer.showBackground(v.getTop(),v.getHeight());
					}
				}
				if(mSwiping){
					v.setTranslationX((x-mDownX));
					v.setAlpha(1 - deltaXAbs / v.getWidth());
				}
			}	
			break;
			case MotionEvent.ACTION_UP:
				// User let go - figure out whether to animate the view out, or back into place
				if(mSwiping){
					final Task deleteTask = (Task) listTask.getItemAtPosition(listTask.getPositionForView(v));
					float x = event.getX() + v.getTranslationX();
                    float deltaX = x - mDownX;
                    float deltaXAbs = Math.abs(deltaX);
                    float fractionCovered;
                    float endX;
                    float endAlpha;
                    final boolean remove;
                    if (deltaXAbs > v.getWidth() / 4) {
                        // Greater than a quarter of the width - animate it out
                        fractionCovered = deltaXAbs / v.getWidth();
                        endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                        endAlpha = 0;
                        remove = true;
                    } else {
                        // Not far enough - animate it back
                        fractionCovered = 1 - (deltaXAbs / v.getWidth());
                        endX = 0;
                        endAlpha = 1;
                        remove = false;
                    }
                    // Animate position and alpha of swiped item
                    // NOTE: This is a simplified version of swipe behavior, for the
                    // purposes of this demo about animation. A real version should use
                    // velocity (via the VelocityTracker class) to send the item off or
                    // back at an appropriate speed.
                    long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
                    listTask.setEnabled(false);
                    v.animate().setDuration(duration).
                            alpha(endAlpha).translationX(endX).
                            withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    // Restore animated values
                                    v.setAlpha(1);
                                    v.setTranslationX(0);
                                    if (remove) {                                        
                                    	animateRemoval(listTask, v);                                        
                                    	MainActivity.taskDataLoader.delete(deleteTask);
                                    	listTaskStableAdapter.notifyDataSetChanged();                                                                                   
                                        Toast.makeText(getActivity(), "Task: " + deleteTask.getTaskName() 
                                        		+ " has been deleted", Toast.LENGTH_SHORT).show();
                                    } else {
                                        //mBackgroundContainer.hideBackground();
                                        mSwiping = false;
                                        listTask.setEnabled(true);
                                    }
                                }
                            });
					
				}
				mItemPressed = false;	
				break;
			default:
				return false;
			}
			return true;
		}
	};
	
	/**
     * This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
     */
    private void animateRemoval(final ListView listview, View viewToRemove) {
        int firstVisiblePosition = listview.getFirstVisiblePosition();
        for (int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            if (child != viewToRemove) {
                int position = firstVisiblePosition + i;
                long itemId = listTaskStableAdapter.getItemId(position);
                mItemIdTopMap.put(itemId, child.getTop());
            }
        }
        // Delete the item from the adapter
        int position = listTask.getPositionForView(viewToRemove);
        listTaskStableAdapter.remove(listTaskStableAdapter.getItem(position));

        final ViewTreeObserver observer = listview.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listview.getFirstVisiblePosition();
                for (int i = 0; i < listview.getChildCount(); ++i) {
                    final View child = listview.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = listTaskStableAdapter.getItemId(position);
                    Integer startTop = mItemIdTopMap.get(itemId);
                    int top = child.getTop();
                    if (startTop != null) {
                        if (startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(MOVE_DURATION).translationY(0);
                            if (firstAnimation) {
                                child.animate().withEndAction(new Runnable() {
                                    public void run() {
                                        //mBackgroundContainer.hideBackground();
                                        mSwiping = false;
                                        listTask.setEnabled(true);
                                    }
                                });
                                firstAnimation = false;
                            }
                        }
                    } else {
                        // Animate new views along with the others. The catch is that they did not
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int childHeight = child.getHeight() + listview.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if (firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                public void run() {
                                    //mBackgroundContainer.hideBackground();
                                    mSwiping = false;
                                    listTask.setEnabled(true);
                                }
                            });
                            firstAnimation = false;
                        }
                    }
                }
                mItemIdTopMap.clear();
                return true;
            }
        });
    }
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		this.activity = activity;
		
		try{
			mCallback = (OnCreateNewTaskListener) activity;
			itemCallback = (OnTaskClickListener) activity;
		}catch(ClassCastException e){
			throw new ClassCastException(activity.toString() + " must implement callback OnCreateNewTaskListener" +
					" and OnTaskClickListener");
		}
	}
	
	public static FragmentReminder newInstance(ArrayList<Task> tasks){
		FragmentReminder fr = new FragmentReminder();
		
		Bundle b = new Bundle();
		b.putParcelableArrayList("tasks", tasks);
		fr.setArguments(b);
		return fr;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		tasks = getArguments().getParcelableArrayList("tasks");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){		
		//adapter = MainActivity.listTaskAdapter;
		v = inflater.inflate(R.layout.reminder, container, false);
		
		listTask = (ListView) v.findViewById(R.id.list_task);
		rangeTV = (TextView) v.findViewById(R.id.range_tv);
//		headingTV = (TextView) v.findViewById(R.id.heading_tv);
//		btnNewTask = (ImageButton) v.findViewById(R.id.imgbtn_add);
		btnLeft = (ImageButton) v.findViewById(R.id.imgbtn_left);
		btnRight =(ImageButton) v.findViewById(R.id.imgbtn_right);
		String date = DateFormat.getDateInstance().format(new Date());
		rangeTV.setText(date);
						
		//set StableArrayAdapter
		listTaskStableAdapter = new StableArrayAdapterTask(getActivity(),
			android.R.layout.simple_list_item_1,tasks,mTouchListener);
		
		String username = MainActivity.activeAcc.getUsername().substring(0,1)
				.toUpperCase() + MainActivity.activeAcc.getUsername().substring(1);
//		headingTV.setText(headingTV.getText().toString() + 
//				" - " + username + ", Tasks: " + listTaskStableAdapter.getCount());
//		
		
		listTask.setAdapter(listTaskStableAdapter);
				
		listTask.setOnItemClickListener(this);
		listTask.setOnTouchListener(swipe);
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
//		btnNewTask.setOnClickListener(this);
		MainActivity.labelTitle.setText(username 
				+ " - Tasks: " + FragmentReminder.listTaskStableAdapter.getCount());
		return v;
	}

	@Override
	public void onClick(View v) {		
		switch(v.getId()){
//		case R.id.imgbtn_add:			
//			OnCreateNewTaskListener act = (OnCreateNewTaskListener)activity;
//			act.onCreateNewTask();
//			break;
//		case R.id.imgbtn_preferences:
//			break;		
		case R.id.imgbtn_left:
			//go to next day
			break;
		case R.id.imgbtn_right:
			//go to previous day
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {		
		final Task editTask = (Task) listTask.getItemAtPosition(listTask.getPositionForView(view));
		OnTaskClickListener act = (OnTaskClickListener) activity;
		act.onTaskClick(editTask);						
	}

}
