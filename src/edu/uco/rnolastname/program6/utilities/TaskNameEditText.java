package edu.uco.rnolastname.program6.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class TaskNameEditText extends EditText{
	private TaskNameEditTextListener mOnImeBack;

    public TaskNameEditText(Context context) {
        super(context);
    }

    public TaskNameEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TaskNameEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeBack != null) mOnImeBack.onImeBack(this, this.getText().toString());
        }        
        return super.dispatchKeyEvent(event);
    }

    public void setOnEditTextImeBackListener(TaskNameEditTextListener listener) {
        mOnImeBack = listener;
    }
    
    public interface TaskNameEditTextListener{
    	public abstract void onImeBack(TaskNameEditText ctrl, String text);    	        	
    }
}
