package edu.uco.rnolastname.program6.dbutilities;

import android.content.Loader;
import android.os.AsyncTask;

public abstract class ContentChangingTask<T1,T2,T3> extends AsyncTask<T1,T2,T3> {
	private Loader<?> loader = null;
	ContentChangingTask(Loader<?> loader) {
		this.loader = loader;
	}
	
	@Override
	protected void onPostExecute(T3 param){
		loader.onContentChanged();
	}
}
