package edu.uco.rnolastname.program6.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import edu.uco.rnolastname.program6.R;

public class ActionSeparatorView extends ImageView {
	public ActionSeparatorView (Context context) {
	    super(context);
	    Drawable b = getResources().getDrawable(R.drawable.divider_actionbar);
	    
	    setImageDrawable(b);
	}
}
