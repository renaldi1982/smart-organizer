package edu.uco.rnolastname.program6.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.uco.rnolastname.program6.R;

public class ReminderSettingsActivity extends PreferenceActivity{

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().add(new ReminderSettingsFragment(), "reminder").
        addToBackStack(null).commit();
    }
     
    
    
    public static class ReminderSettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
    {
    	private Activity activity;
    	private View v;
    	@Override
    	public void onAttach(Activity activity){
    		this.activity = activity;
    	}
    	
    	/*@Override
    	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    		v = inflater.inflate(resource, root)
    	}*/
    	
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.reminder_settings);
        }
        
        @Override
		public void onResume(){
            super.onResume();
            getPreferenceScreen().getSharedPreferences().
                                    registerOnSharedPreferenceChangeListener(this);
            // TODO update preferences
        }
     
        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                                    .unregisterOnSharedPreferenceChangeListener(this);
        }    
     
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePreference(key);
        }   
        private void updatePreference(String key){
	        Preference pref = findPreference(key);
	        
	        if (pref instanceof ListPreference) {
	            ListPreference listPref = (ListPreference) pref;
	            pref.setSummary(listPref.getEntry());
	            return;
	        }       
	         
	        if (pref instanceof EditTextPreference){
	            EditTextPreference editPref =  (EditTextPreference) pref;
	            editPref.setSummary(editPref.getText());
	            return;
	        }
	         
	        if (pref instanceof RingtonePreference) {	            
				//Uri ringtoneUri = Uri.parse(ReminMe.getRingtone());
	            Ringtone ringtone = RingtoneManager.getRingtone(activity.getApplicationContext(),
	            		RingtoneManager.getDefaultUri(1));
	            if (ringtone != null) pref.setSummary(ringtone.getTitle(activity.getApplicationContext()));
	        }       
	    }
    }
}
