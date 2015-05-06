package edu.uco.rnolastname.program6.app;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

public class CustomCalendarLinearLayout extends LinearLayout{
	private float xFraction = 0;		
		
	public CustomCalendarLinearLayout(Context context) {
		super(context); 
	}
	
	public CustomCalendarLinearLayout(Context context, AttributeSet attrs){
		super(context,attrs);
	}
	
	public CustomCalendarLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
	
	private ViewTreeObserver.OnPreDrawListener preDrawListener = null;
	
	public void setXFraction(final float fraction){
		
		this.xFraction = fraction;		
		if(getWidth() == 0){
			if(preDrawListener == null){
				preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
					
					@Override
					public boolean onPreDraw() {						
						getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
						setXFraction(xFraction);
						return true;
					}
				};
				getViewTreeObserver().addOnPreDrawListener(preDrawListener);
			}
			return;
		}		
		float translationX = getWidth() * fraction;
		setTranslationX(translationX);
	}
	
	public float getXFraction(){
		return this.xFraction;
	}
}
