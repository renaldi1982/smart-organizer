package edu.uco.rnolastname.program6.mapreminder;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

public class MapReminderService extends Service implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener{
		
	private final static String TAG = "mapreminderlocationservice";	
	private static LocationManager lm;
	private static LocationClient locationClient;
	
	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG,location.toString());
		
		locationClient = new LocationClient(this, this, this);
        locationClient.connect();
		
//		if (location != null) {	    		  		    			    	
//    		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//    				new LatLng(location.getLatitude(),location.getLongitude()),17));    		    		
//		}
//		
//		//last location where we moved from
//		Location lastLocation = MapReminder.location.getLastLocation();
//		LatLng lastPosition = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
//		
//		//new location where we moved to
//		LatLng newPosition = new LatLng(location.getLatitude(),location.getLongitude());
//		myLocationLatlong = newPosition; 		
		
	}
	
	@Override
	public void onCreate(){
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

}
