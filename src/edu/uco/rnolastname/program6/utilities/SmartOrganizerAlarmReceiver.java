package edu.uco.rnolastname.program6.utilities;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.app.MainActivity;
import edu.uco.rnolastname.program6.dbutilities.Account;
import edu.uco.rnolastname.program6.dbutilities.Task;

public class SmartOrganizerAlarmReceiver extends BroadcastReceiver{

	private Task task;
	private Account account;
	private Context context;
	private static AutoGeneratedEmail emailGenerator = null;
	
	//play sound
	private static MediaPlayer mMediaPlayer = null;
	
	private static final String ONE_TIME = "onetime";
	private static final String ALARM = "alarm";
	private static final String EMAIL = "email";
	
	@Override
	public void onReceive(Context context, Intent intent) {				
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "onreceivewakelock");
		
		wl.acquire();	
				
		if(intent.getBooleanExtra(ALARM, Boolean.FALSE)){
			Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			playSound(context,alarmUri);
			displayNotification();
			Log.d("DEBUG","play alarm");
		}
		
		if(intent.getBooleanExtra(EMAIL, Boolean.FALSE)){
			sendEmail();
			Log.d("DEBUG","send email");
		}			
		
		wl.release();				
	}
	
	public void setAlarm(Context context, Task task, Account account){
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);	
		if(task != null && account != null){
			this.task = task;
			this.account = account;
		}		
		
        Intent intent = new Intent(context, SmartOrganizerAlarmReceiver.class);		
        intent.putExtra(ALARM, Boolean.TRUE);		
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        
        //After 5 seconds		
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5 , pi);
	}
			
	public void setOnetimeTimer(Context context){
		AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);		
	    Intent intent = new Intent(context, SmartOrganizerAlarmReceiver.class);		
	    intent.putExtra(ONE_TIME, Boolean.TRUE);
	    PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
	    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
    }

	public void CancelAlarm(Context context)
    {
	        Intent intent = new Intent(context, SmartOrganizerAlarmReceiver.class);
	        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
	        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	        alarmManager.cancel(sender);
	    }

	
	private void playSound(Context context, Uri alert) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            Log.d("DEBUG","IOException error");
        }
    }
	
	private void sendEmail(){
		if(account != null && task != null){
			emailGenerator = new AutoGeneratedEmail(account,task);
		}
				
		try {
			if(emailGenerator.send()){
				Log.d("DEBUG","Email has been sent to: " + account.getEmail());
			}else{
				Log.d("DEBUG","Failed to send email to: " + account.getEmail());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("AutoGeneratedEmail","Error sending mail");
		}
	}
	
	private void displayNotification(){
		NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
	    .setSmallIcon(R.drawable.ic_notification)
	    .setContentTitle("Reminder for: " + account.getUsername() + ", Task: " + task.getTaskName())
	    .setContentText("Click here if you would like to open Smart Organizer")
	    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
//	    only use this for mobile phone
//	    .setVibrate(new long[] {0,100,200,300});		
						
		Intent i = new Intent(context, MainActivity.class);		
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
		        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		
		/*TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);		
		stackBuilder.addNextIntent(i);*/
		
		PendingIntent pendingIntent = PendingIntent.getActivity(
		        context,
		        0,
		        i,
		        PendingIntent.FLAG_UPDATE_CURRENT
		);		
		nBuilder.setContentIntent(pendingIntent);
		NotificationManager nm= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(1, nBuilder.build());		
	}
}


//String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
//Toast.makeText(context, "Alarm hah!! " + task.getTaskName() +
//		" fired on " + timeStamp, Toast.LENGTH_SHORT).show();