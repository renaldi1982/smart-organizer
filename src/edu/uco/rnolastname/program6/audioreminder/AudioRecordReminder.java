package edu.uco.rnolastname.program6.audioreminder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import edu.uco.rnolastname.program6.R;

public class AudioRecordReminder extends Activity implements OnClickListener{	
	/***************************************************************************
	* Public constants
	**/
	// Message codes - XXX Also see BooRecorder
	public static final int MSG_OK                    = 0;
	public static final int MSG_INVALID_FORMAT        = 1;
	public static final int MSG_HARDWARE_UNAVAILABLE  = 2;
	public static final int MSG_ILLEGAL_ARGUMENT      = 3;
	public static final int MSG_READ_ERROR            = 4;
	public static final int MSG_WRITE_ERROR           = 5;
	public static final int MSG_AMPLITUDES            = 6;
	
	//objects in xml
	private static ProgressBar pBar;
	private Button btnPlay;
	private Button btnRecord;
	private TextView pText;

	//variables for progress bar
	private MediaRecorder mRecorder;
	private static final String MUSIC_PATH = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MUSIC).getPath();
	private boolean isRecording = false;
	private boolean isPlaying = false;
	private int counter = 0;
	private long timerStart;
	private long timerEnd;	
	private CountDownTimer cd;
	private static MediaPlayer mPlayer;
	private String fullPathLastRecord;
		
	private static FLACRecorder fr;
	private static Handler handler;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audioreminder);
		
		//init xml objects
		pBar = (ProgressBar) findViewById(R.id.progressBar);
		btnPlay = (Button) findViewById(R.id.btn_play);
		btnRecord = (Button) findViewById(R.id.btn_record);
		pText = (TextView) findViewById(R.id.txtTimer);				
						
		btnPlay.setText("Start Playing");
		btnRecord.setText("Start Recording");
		btnPlay.setOnClickListener(this);
		btnRecord.setOnClickListener(this);
	}
		
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_record:
			if(!isRecording){				
				btnRecord.setText("Stop Recording");		
				startRecording();
			}else{
				btnRecord.setText("Start Recording");
				stopRecording();
			}				
			break;
		case R.id.btn_play:
			if(!isPlaying){				
				btnRecord.setText("Start Playing");	
				startPlaying();
			}else{
				btnRecord.setText("Stop Playing");
				stopPlaying();
			}
			break;
		}				
	}
	
	private void startRecording(){
		//init handler and override the handle message 
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){				
				FLACRecorder.Amplitudes amp = (FLACRecorder.Amplitudes)msg.obj;
				Log.d("DEBUG","Handler message: " + amp.toString());
				if(msg.toString() == String.valueOf(MSG_AMPLITUDES)){					
					Log.d("DEBUG","Amplitude message");
					//update progress bar here
				}
			}			
		};
		
		//init FLACRecorder
		fullPathLastRecord = MUSIC_PATH + "/voiceRecord.wav";
		fr = new FLACRecorder(fullPathLastRecord,handler);
		fr.run();
		isRecording = true;
	}
	
	private void stopRecording(){
		if(null != fr && isRecording){
			synchronized(fr){
				fr.interrupt();
			}
			fr = null;
			handler.removeCallbacks(fr);
		}
		isRecording = false;
	}
	
	private void startPlaying() {
        mPlayer = new MediaPlayer();
        if(!fullPathLastRecord.equals("") && !isRecording){
        	try {        	            	
                mPlayer.setDataSource(fullPathLastRecord);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e("DEBUG", "prepare() failed");
            }
        }else{
        	Toast.makeText(getApplicationContext(), "Please finish recording first", Toast.LENGTH_SHORT).show();
        }
        
    }

    private void stopPlaying() {    	    	    	
        mPlayer.release();
        mPlayer = null;
    }
	
	
	  
	  /***************************************************************************
	   * Implementation
	   **/
	  public static class FLACRecorder extends Thread{	  
	  
		  /***************************************************************************
		   * Public data
		   **/
		  // Flag that keeps the thread running when true.
		  public boolean mShouldRun;
 
		  
	  	/***************************************************************************
		   * Private data
		   **/
		  // Flag that signals whether the thread should record or ignore PCM data.
		  private boolean                 mShouldRecord = false;

		  // Stream encoder
		  private FLACStreamEncoder       mEncoder;

		  // File path for the output file.
		  private String                  mPath;

		  // Handler to notify at the above report interval
		  private Handler                 mHandler;

		  // Remember the duration of the recording. This is in msec.
		  private double                  mDuration;

	  	
		  public static class Amplitudes
		  {
		    public long   mPosition;
		    public float  mPeak;
		    public float  mAverage;

		    public Amplitudes()
		    {
		    }

		    public Amplitudes(Amplitudes other)
		    {
		      mPosition = other.mPosition;
		      mPeak = other.mPeak;
		      mAverage = other.mAverage;
		    }

		    public String toString()
		    {
		      return String.format(Locale.US, "%dms: %f/%f", mPosition, mAverage, mPeak);
		    }

		    public void accumulate(Amplitudes other)
		    {
		      // Position is simple; the overall position is the sum of
		      // both positions.
		      long oldPos = mPosition;
		      mPosition += other.mPosition;

		      // The higher peak is the overall peak.
		      if (other.mPeak > mPeak) {
		        mPeak = other.mPeak;
		      }

		      // Average is more complicated, because it needs to be weighted
		      // on the time it took to calculate it.
		      float weightedOld = mAverage * (oldPos / (float) mPosition);
		      float weightedNew = other.mAverage * (other.mPosition / (float) mPosition);
		      mAverage = weightedOld + weightedNew;
		    }
		  }	
		  
		  public FLACRecorder(String path, Handler handler)	
		  {
		    mPath = path;
		    mHandler = handler;
		    Log.d("DEBUG", "New FLACRecorder, path: " + mPath);
		  }
	  		  	  	  	  	  
		  public void resumeRecording()
		  {
		    mShouldRecord = true;
		  }
		  
		  public void pauseRecording()
		  {
		    mShouldRecord = false;
		  }
	
		  public boolean isRecording()
		  {
		    return mShouldRun && mShouldRecord;
		  }
	
		  public double getDuration()
		  {
		    // Duration for Boos is normally in secs, and we're remembering msecs here,
		    // so we'll need to convert.
		    return mDuration / 1000;
		  }



		  public Amplitudes getAmplitudes()
		  {
		    if (null == mEncoder) {
		      return null;
		    }
	
		    Amplitudes amp = new Amplitudes();
		    amp.mPosition = (long) mDuration;
		    amp.mPeak = mEncoder.getMaxAmplitude();
		    amp.mAverage = mEncoder.getAverageAmplitude();
	
		    return amp;
		  }
	
	
	
		  public static int mapChannelConfig(int channelConfig)
		  {
		    switch (channelConfig) {
		      case AudioFormat.CHANNEL_IN_MONO:
		        return 1;
	
		      case AudioFormat.CHANNEL_IN_STEREO:
		        return 2;
	
		      default:
		        return 0;
		    }
		  }
	
	
		  public static int mapFormat(int format)
		  {
		    switch (format) {
		      case AudioFormat.ENCODING_PCM_8BIT:
		        return 8;
	
		      case AudioFormat.ENCODING_PCM_16BIT:
		        return 16;
	
		      default:
		        return 0;
		    }
		  }
	
	
	
		  public void run()
		  {
		    // Determine audio config to use.
		    final int sample_rates[] = { 96000, /* Samsung galaxy S2 phones broken here 48000, */ 44100, 22050, 11025, 8000 };
		    final int configs[] = { AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT };
		    final int formats[] = { AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT };
	
		    int sample_rate = -1;
		    int channel_config = -1;
		    int format = -1;
	
		    int bufsize = AudioRecord.ERROR_BAD_VALUE;
		    AudioRecord recorder = null;
	
		    boolean found = false;
		    for (int x = 0 ; !found && x < formats.length ; ++x) {
		      format = formats[x];
	
		      for (int y = 0 ; !found && y < sample_rates.length ; ++y) {
		        sample_rate = sample_rates[y];
	
		        for (int z = 0 ; !found && z < configs.length ; ++z) {
		          channel_config = configs[z];
	
		          Log.d("DEBUG", "Trying: " + format + "/" + channel_config + "/" + sample_rate);
		          bufsize = 2 * AudioRecord.getMinBufferSize(sample_rate, channel_config, format);
		          Log.d("DEBUG", "Bufsize: " + bufsize);
	
		          // Handle invalid configs
		          if (AudioRecord.ERROR_BAD_VALUE == bufsize) {
		            continue;
		          }
		          if (AudioRecord.ERROR == bufsize) {
		            Log.e("DEBUG", "Unable to query hardware!");
		            mHandler.obtainMessage(MSG_HARDWARE_UNAVAILABLE).sendToTarget();
		            return;
		          }
	
		          try {
		            // Set up recorder
		            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sample_rate,
		                channel_config, format, bufsize);
					int istate = recorder.getState();
					if (istate != AudioRecord.STATE_INITIALIZED) // it lied to us
						continue;
		          } catch (IllegalArgumentException ex) {
		            recorder = null;
		            Log.e("DEBUG", "Failed to set up recorder!");
		            continue;
		          }
	
		          // Got a valid config.
		          found = true;
		          break;
		        }
		      }
		    }
	
		    if (!found) {
		      Log.e("DEBUG", "Sample rate, channel config or format not supported!");
		      mHandler.obtainMessage(MSG_INVALID_FORMAT).sendToTarget();
		      return;
		    }
		    Log.d("DEBUG", "Using: " + format + "/" + channel_config + "/" + sample_rate);
	
		    mShouldRun = true;
		    mShouldRecord = true;
		    boolean oldShouldRecord = false;
		
		    try {
		      // Initialize variables for calculating the recording duration.
		      int mapped_format = mapFormat(format);
		      int mapped_channels = mapChannelConfig(channel_config);
		      int bytesPerSecond = sample_rate * (mapped_format / 8) * mapped_channels;
	
		      // Set up encoder. Create path for the file if it doesn't yet exist.
		      Log.d("DEBUG", "Setting up encoder " + mPath + " rate: " + sample_rate + " channels: " + mapped_channels + " format " + mapped_format);
	
		      //mEncoder = new FLACStreamEncoder(mPath, sample_rate, mapped_channels, mapped_format);
	
		      // Start recording loop
		      mDuration = 0.0;
		      ByteBuffer buffer = ByteBuffer.allocateDirect(bufsize);
		      while (mShouldRun) {
		        // Toggle recording state, if necessary
		        if (mShouldRecord != oldShouldRecord) {
		          // State changed! Let's see what we are supposed to do.
		          if (mShouldRecord) {
		             Log.d("DEBUG", "Start recording!");
		            recorder.startRecording();
		          }
		          else {
		            Log.d("DEBUG", "Stop recording!");
		            recorder.stop();
		            mEncoder.flush();
		          }
		          oldShouldRecord = mShouldRecord;
		        }
	
		        // If we're supposed to be recording, read data.
		        if (mShouldRecord) {
		          int result = recorder.read(buffer, bufsize);
		          switch (result) {
		            case AudioRecord.ERROR_INVALID_OPERATION:
		              Log.e("DEBUG", "Invalid operation.");
		              mHandler.obtainMessage(MSG_READ_ERROR).sendToTarget();
		              break;
	
		            case AudioRecord.ERROR_BAD_VALUE:
		              Log.e("DEBUG", "Bad value.");
		              mHandler.obtainMessage(MSG_READ_ERROR).sendToTarget();
		              break;
	
		            default:
		              if (result > 0) {
		                //Log.d(LTAG, "*** CHIPMUNK got: " + result);
		                //java.nio.ShortBuffer s = buffer.asShortBuffer();
		                //s.rewind();
		                //for (int i = 0 ; i < (result / (format / 8)) ; ++i) {
		                //  short v = s.get(i);
		                //  if (Math.abs(v) <= 255) {
		                //    Log.d(LTAG, "*** CHIPMUNK sample " + i + ": " + v);
		                //  }
		                //}
	
		                // Compute time recorded
		                double read_ms = (1000.0 * result) / bytesPerSecond;
		                mDuration += read_ms;
		                Log.d("DEBUG","result: " + result);
		                //long start = System.currentTimeMillis();
		                
//		                int write_result = mEncoder.write(buffer, result);
//		                if (write_result != result) {
//		                  Log.e("DEBUG", "Attempted to write " + result
//		                      + " but only wrote " + write_result);
//		                  mHandler.obtainMessage(MSG_WRITE_ERROR).sendToTarget();
//		                }
//		                else {
		                  Amplitudes amp = getAmplitudes();
		                  //Log.d("DEBUG","Amplitudes: " + amp.toString());
		                  //TODO update progress bar here by sending the amplitude
		                  mHandler.obtainMessage(MSG_AMPLITUDES, amp).sendToTarget();
//		                }
		                //long end = System.currentTimeMillis();
		                //Log.d(LTAG, "Write of " + result + " bytes took " + (end - start) + " msec.");
		              }
		          }
		        }
		      }
	
		      recorder.release();
		      mEncoder.release();
		      mEncoder = null;
	
		    } catch (IllegalArgumentException ex) {
		      Log.e("DEBUG", "Illegal argument: " + ex.getMessage());
		      mHandler.obtainMessage(MSG_ILLEGAL_ARGUMENT, ex.getMessage()).sendToTarget();
		    }
	
		    mHandler.obtainMessage(MSG_OK).sendToTarget();
		  }
	  }
}
