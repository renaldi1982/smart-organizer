package edu.uco.rnolastname.program6.utilities;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SwipeDetector implements View.OnTouchListener{	
	
	public static enum Direction {
		LR, // Left to Right
		RL, // Right to Left
		TB, // Top to Bottom
		BT, // Bottom to Top
		None
	}
	
	private static final int MIN_DISTANCE = 100;
	private float downX, downY, upX, upY;
	
	private Direction mSwipeDetected = Direction.None;
	
	public boolean swipeDetected(){
		return mSwipeDetected != Direction.None;
	}
	
	public Direction getAction(){
		return mSwipeDetected;
	}		
	
	@Override	
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN: {
			downX = event.getX();
			downY = event.getY();
			mSwipeDetected = Direction.None;
			return false;
		}
		case MotionEvent.ACTION_MOVE: {
			upX = event.getX();
			upY = event.getY();
			
			float deltaX = downX - upX;
			float deltaY = downY - upY;
			
			/* process horizontal swipe */
			if(Math.abs(deltaX) > MIN_DISTANCE){
				/* check for right to left swipe */
				if(deltaX < 0){
					Log.d("DEBUG","Swipe left to right");
					mSwipeDetected = Direction.LR;
					return true;
				}
				/* check for left to left swipe */
				if(deltaX > 0){
					Log.d("DEBUG","Swipe right to left");
					mSwipeDetected = Direction.RL;
					return true;
				}				
			}else 
				//process vertical swipe
				if(Math.abs(deltaY) > MIN_DISTANCE){
					/* check for top to bottom swipe */
					if(deltaY < 0){
						Log.d("DEBUG","Swipe top to bottom detected");
						mSwipeDetected = Direction.TB;
						return false;
					}
					/* check for bottom to top swipe */
					if(deltaY > 0){
						Log.d("DEBUG","Swipe bottom to top detected");
						mSwipeDetected = Direction.BT;
						return false;
					}
			}
			return true;			
		}		
		}
		return false;
	}

}
