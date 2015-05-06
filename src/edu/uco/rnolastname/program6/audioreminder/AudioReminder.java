package edu.uco.rnolastname.program6.audioreminder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.app.MainActivity;
import edu.uco.rnolastname.program6.dbutilities.Account;
import edu.uco.rnolastname.program6.dbutilities.Task;
import edu.uco.rnolastname.program6.utilities.SwipeDetector;
import edu.uco.rnolastname.program6.utilities.SwipeDetector.Direction;
import edu.uco.rnolastname.program6.utilities.Utilities;

public class AudioReminder extends Activity implements OnCompletionListener,OnClickListener{
	private static int callerID = -1;
	
	//Gesture
	private static SwipeDetector swipe = new SwipeDetector();
	
	//Layouts
	private static LinearLayout mainLayout;
	
	//action bar
	private static MenuItem itemSave = null;
	private static MenuItem itemDone = null;
	
	//task and account
	private static Task activeTask = null;
	private static Account activeAcc = null;
		
	//recorder
	private static AudioRecord recorder; // our recorder, must be initialized first
	private static Thread recordingThread = null;		
	private int bufferSize = 0;
	private static FileOutputStream os = null;
	private boolean isRecording = false; // indicates if sound is currently being captured	
	
	//progress bar 
	private static Handler handler = new Handler();
	private static ProgressBar pb; // our progress bar received from layout	
	private double sumRecord = 0;		
	//private static short[] buffer; // buffer where we will put captured samples to be shown in progress bar
	
	//timer
	private static TextView txtTimer;
	private long startTime; 
	private long elapsedTime; 
	private final int REFRESH_RATE = 100; 
	
	//recorder constant
	private static final int RECORDER_BPP = 16;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "Music";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static String fullPathLastRecord = "";		
    private static String mFileName = null;
    private Button mRecordButton = null;

    //player
    private boolean isPlaying = false;
    private static Visualizer visualizer;
    private static Button   mPlayButton = null;
    private static MediaPlayer   mPlayer = null;
    private static Utilities util = new Utilities();
        
    @Override
    public void onCreate(Bundle savedInstanceState) {        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.audioreminder);

        mainLayout = (LinearLayout) findViewById(R.id.audioreminder_mainlayout);
        
        ActionBar action = getActionBar();
	    action.setDisplayHomeAsUpEnabled(true);	
	    action.setTitle("Audio Reminder");
        
	    Intent callerIntent = getIntent();
	    
	    activeTask = callerIntent.getParcelableExtra("task");
	    
	    if(activeTask.getAudioPath() != null && !activeTask.getAudioPath().equalsIgnoreCase("")){
	    	fullPathLastRecord = activeTask.getAudioPath();
	    }
	    
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING);        
        pb = (ProgressBar) findViewById(R.id.progressBar);
        txtTimer = (TextView) findViewById(R.id.txtTimer);
        
        mRecordButton = (Button) findViewById(R.id.btn_record);
        mRecordButton.setText("Start Recording");        
        mRecordButton.setOnClickListener(new OnClickListener(){
        	boolean mStartRecording = true;
        	@Override
        	public void onClick(View v) {
        		
        		if(!fullPathLastRecord.equals("")){   
        			        			
        			if(!mStartRecording){
        				Log.d("DEBUG","existing task");
            			AlertDialog.Builder b = new AlertDialog.Builder(AudioReminder.this);
            			b.setMessage("You already have an existing recording do you want to replace it?")
            			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    						
    						@Override
    						public void onClick(DialogInterface dialog, int which) {														
    							onRecord(mStartRecording);							
    							if (mStartRecording) {
    								mRecordButton.setText("Stop recording");
    							} else {
    								mRecordButton.setText("Start recording");
    							}   
    							mStartRecording = !mStartRecording;
    						}
    					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
    						
    						@Override
    						public void onClick(DialogInterface dialog, int which) {	
    							fullPathLastRecord = "";
    							if (mStartRecording) {
    								mRecordButton.setText("Stop recording");
    							} else {
    								mRecordButton.setText("Start recording");
    							}   
    							mStartRecording = !mStartRecording;
    						}
    					}).show();
        			}else{
        				Log.d("DEBUG","task exists stop recording");
        				onRecord(mStartRecording);
        				if (mStartRecording) {
    						mRecordButton.setText("Stop recording");
    					} else {
    						mRecordButton.setText("Start recording");
    					}   
    					mStartRecording = !mStartRecording;   
        			}
        		}else{        		        			
    				Log.d("DEBUG","new task / stop button pressed");
    				onRecord(mStartRecording);
    				if (mStartRecording) {
						mRecordButton.setText("Stop recording");
					} else {
						mRecordButton.setText("Start recording");
					}   
					mStartRecording = !mStartRecording;            			        		
        		}
            }
        });
        
        mPlayButton = (Button) findViewById(R.id.btn_play);
        mPlayButton.setText("Start Playing");
        mPlayButton.setOnClickListener(new OnClickListener(){
        	boolean mStartPlaying = true;
        	@Override
        	public void onClick(View v) {
        		if(fullPathLastRecord.equals("")){
        			Toast.makeText(getApplicationContext(), "Please finish recording first", Toast.LENGTH_SHORT).show();
                	return;
        		}else{
        			onPlay(mStartPlaying);
                    if (mStartPlaying) {
                    	mPlayButton.setText("Stop playing");
                    } else {
                    	mPlayButton.setText("Start playing");
                    }
                    mStartPlaying = !mStartPlaying;
        		}               
            }
        });                    
        mainLayout.setOnTouchListener(swipe);
        mainLayout.setOnClickListener(this);
    }       
    
    @Override
	public void onClick(View v) {
    	int id = v.getId();
    	
    	switch(id){
    	case R.id.audioreminder_done:
    		this.finish();
//    		Direction swipeDirection = swipe.getAction();
//    		if(swipeDirection == Direction.LR){
//    			finish();
//    			overridePendingTransition(R.animator.right_out,R.animator.left_in);	
//    		}
    		break;
    	}
		
	}
    
    @Override 
    public void onBackPressed(){
    	super.onBackPressed();
    	this.finish();
    	//overridePendingTransition(android.R.animator.fade_in,android.R.anim.fade_out);
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();   
    	if(mPlayer != null){
    		mPlayer.reset();
            mPlayer.release();
            mPlayer = null;               	
    	}    	
    	if(null != recorder){    		          
            recorder.stop();
            recorder.release();            
            recorder = null;
    	}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    }
    
    @Override 
    public boolean onMenuItemSelected(int featureid, MenuItem item){    	
    	int id = item.getItemId();
    	switch(id){    		
    		case R.id.audioreminder_done:
    			Intent i = getIntent();
    			Log.d("DEBUG","audio reminder setresult " + String.valueOf(activeTask.getAudioPath() == null));
    			i.putExtra("task", activeTask);
    			setResult(RESULT_OK, i);
    			finish();
    			break;
    	}    	
    	return true;
    }
    
    @Override 
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	getMenuInflater().inflate(R.menu.audioreminder_menu, menu);    	
    	        		    			    		    	
    	return true;
    }        
    
    private void addActionSave(Menu menu){
    	MenuItem itemSave = menu.add("Save");
		itemSave.setIcon(R.drawable.reminder_add);
		itemSave.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }
    
    private void addActionDone(Menu menu){
    	MenuItem itemSave = menu.add("Done");
		itemSave.setIcon(R.drawable.ic_done);
		itemSave.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }                      
    
    private void onRecord(boolean start) {    	
        if (start && !isPlaying) {
            startRecording();
        } else {
            stopRecording();           
        }
    }

    private void onPlay(boolean start) {
        if (start && !isRecording) {
            startPlaying();
        } else {        	
            stopPlaying();            
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isRecording = false;
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }             
    
    private void startPlaying() {    	
        mPlayer = new MediaPlayer();
        mPlayer.setAudioSessionId(1);
                        	        
        visualizer = new Visualizer(mPlayer.getAudioSessionId());
        if(visualizer.getEnabled())
        	visualizer.setEnabled(false);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                        
        mPlayer.setOnCompletionListener(this); 
        pb.setMax(255+128);
    	try {        	        	    		
        	mFileName = fullPathLastRecord;        	
            mPlayer.setDataSource(mFileName);            
            mPlayer.prepare();            
            mPlayer.start();
            startTime = System.currentTimeMillis();    		
    		handler.removeCallbacks(updateTimer);
    		handler.postDelayed(updateTimer, 0);
            
            //Visualize progress bar when playing the recorded audio                        
            OnDataCaptureListener vListener = new Visualizer.OnDataCaptureListener() {
				
				@Override
				public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform,
						int samplingRate) {										
                    
                    //Calculating Amplitude and update ProgressBar                	
					for(byte wave: waveform){
						pb.setProgress(wave+128);										
					}					
				}
				
				@Override
				public void onFftDataCapture(Visualizer visualizer, byte[] fft,
						int samplingRate) {					
					
				}
			};
			
			visualizer.setDataCaptureListener(vListener, Visualizer.getMaxCaptureRate()/2, true, false);
			
			visualizer.setEnabled(true);    
			isPlaying = true;
        } catch (IOException e) {
            Log.e("DEBUG", "prepare() failed");
        }        
    }

    @Override
	public void onCompletion(MediaPlayer mp) {
    	mPlayButton.performClick();		
	}
    
    private void stopPlaying() { 
    	isPlaying = false;
    	visualizer.setEnabled(false);
    	visualizer.release();
    	if(mPlayer != null){
    		mPlayer.reset();
            mPlayer.release();
            mPlayer = null;               	
    	}    	
    	handler.removeCallbacks(updateTimer);  
    }

    private Runnable updateTimer = new Runnable(){
    	public void run(){
    		elapsedTime = System.currentTimeMillis() - startTime; 
    		txtTimer.setText(util.milliSecondsToTimer(elapsedTime)); 
    		handler.postDelayed(this,REFRESH_RATE);     		
    	}
    };
    
    private void startRecording() {
    	if(!isPlaying && !isRecording){
    		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

    		recorder.startRecording();
    		startTime = System.currentTimeMillis();    		
    		handler.removeCallbacks(updateTimer);
    		handler.postDelayed(updateTimer, 0);
    		
    		pb.setMax(4000);
    		isRecording = true;
    		
    		recordingThread = new Thread(new Runnable() {
    		
    			@Override
    			public void run() {			
    				if(!recordingThread.isInterrupted())
    					writeAudioDataToFile();
    			}
    		},"AudioRecorder Thread");    		
    		
    		recordingThread.setPriority(Thread.currentThread().getThreadGroup().getMaxPriority());
    		recordingThread.start();
    		
    		Toast.makeText(getApplicationContext(), "Recording...", Toast.LENGTH_SHORT).show();
    	}
    }
    
    private void stopRecording() {
    	isRecording = false;
    	if(null != recorder){    		
            
            recorder.stop();
            recorder.release();            
            recorder = null;
                    	
    		try {
				recordingThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}    		
    		handler.removeCallbacks(updateTimer);     	
            recordingThread = null;            
            Toast.makeText(getApplicationContext(), "Stop recording", Toast.LENGTH_SHORT).show();          
            
            copyWaveFile(getTempFilename(),getFilename());
    	    deleteTempFile();	    
    	    confirmSave();
    	}else{
    		return;
    	}    	    		   
    }       	   	    
    
    private void confirmSave(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Save Voice Record?");
	    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				activeTask.setAudioPath(fullPathLastRecord);
				if(activeTask.getCategory() != null && !activeTask.getCategory().equalsIgnoreCase("")){
					activeTask.setCategory(activeTask.getCategory() + MainActivity.audioReminder);
				}else{
					activeTask.setCategory(MainActivity.audioReminder);
				}
				
				Toast.makeText(getApplicationContext(), "Voice Record saved", Toast.LENGTH_SHORT).show();
			}
		}).setNeutralButton("Save and Exit", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				activeTask.setAudioPath(fullPathLastRecord);
				if(activeTask.getCategory() != null && !activeTask.getCategory().equalsIgnoreCase("")){
					activeTask.setCategory(activeTask.getCategory() + MainActivity.audioReminder);
				}else{
					activeTask.setCategory(MainActivity.audioReminder);
				}
				
				Toast.makeText(getApplicationContext(), "Voice Record saved", Toast.LENGTH_SHORT).show();
				Intent i = new Intent();
				i.putExtra("task", activeTask);
				AudioReminder.this.setResult(Activity.RESULT_OK,i);
				AudioReminder.this.finish();
			}
		}).setNegativeButton("Discard", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getApplicationContext(), "Voice Record discarded", Toast.LENGTH_SHORT).show();
				dialog.dismiss();				
			}
		});
	    
	    builder.show();
    }
    
    private void writeAudioDataToFile(){    	
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();             
        short [] buffer = new short[data.length/2];        
        
        if(os != null){
        	try {
				os.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        try {        		
                os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {                
                e.printStackTrace();
        }
        
        int read = 0;
        
        if(null != os){
                while(isRecording){
                    read = recorder.read(data, 0, data.length);
                    sumRecord = 0;                    
                    
                    //Populate short array with data
                    ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(buffer);
                    
                    if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    	try {
                    		os.write(data);                    		                    		
                    	} catch (IOException e) {
                            e.printStackTrace();
                    	}                    	                    
                    	
                    	//Calculating Amplitude and update ProgressBar
                    	for(int i=0 ; i < buffer.length; i++){   
                    		sumRecord += buffer[i] * buffer[i];                			
                		}                             	
                		if (read > 0) {
                    		final double amplitude = sumRecord / read;  
                    		Log.d("DEBUG","Amplitude: " + (int)Math.sqrt(amplitude));                    		
                    		pb.setProgress((int) Math.sqrt(amplitude)*2);
                    	}  
                    }
                }
                try {
                    os.close();
                    pb.setProgress(0);
	            } catch (IOException e) {
	                    e.printStackTrace();
	            }
                
        }
    }        

    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        
        if(!file.exists()){
                file.mkdirs();
        }
        fullPathLastRecord = file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV;
        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
    }

	private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        
        if(!file.exists()){
                file.mkdirs();
        }
        
        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);
        
        if(tempFile.exists())
                tempFile.delete();
        
        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
	}
    
	private void deleteTempFile() {
        File file = new File(getTempFilename());
        
        file.delete();
	}
           
    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;
        
        byte[] data = new byte[bufferSize];
        //Toast.makeText(getApplicationContext(), "Copying wave file" , Toast.LENGTH_SHORT).show();
        try {
                in = new FileInputStream(inFilename);
                out = new FileOutputStream(outFilename);
                totalAudioLen = in.getChannel().size();
                totalDataLen = totalAudioLen + 36;
                
                //Log.d("DEBUG","File size: " + totalDataLen);
                
                WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                                longSampleRate, channels, byteRate);
                
                while(in.read(data) != -1){
                		
                        out.write(data);
                }
                
                in.close();
                out.close();
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
    }
    
    private void WriteWaveFileHeader(
        FileOutputStream out, long totalAudioLen,
        long totalDataLen, long longSampleRate, int channels,
        long byteRate) throws IOException {

	    byte[] header = new byte[44];	    
	    
	    header[0] = 'R';  // RIFF/WAVE header
	    header[1] = 'I';
	    header[2] = 'F';
	    header[3] = 'F';
	    header[4] = (byte) (totalDataLen & 0xff);
	    header[5] = (byte) ((totalDataLen >> 8) & 0xff);
	    header[6] = (byte) ((totalDataLen >> 16) & 0xff);
	    header[7] = (byte) ((totalDataLen >> 24) & 0xff);
	    header[8] = 'W';
	    header[9] = 'A';
	    header[10] = 'V';
	    header[11] = 'E';
	    header[12] = 'f';  // 'fmt ' chunk
	    header[13] = 'm';
	    header[14] = 't';
	    header[15] = ' ';
	    header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
	    header[17] = 0;
	    header[18] = 0;
	    header[19] = 0;
	    header[20] = 1;  // format = 1
	    header[21] = 0;
	    header[22] = (byte) channels;
	    header[23] = 0;
	    header[24] = (byte) (longSampleRate & 0xff);
	    header[25] = (byte) ((longSampleRate >> 8) & 0xff);
	    header[26] = (byte) ((longSampleRate >> 16) & 0xff);
	    header[27] = (byte) ((longSampleRate >> 24) & 0xff);
	    header[28] = (byte) (byteRate & 0xff);
	    header[29] = (byte) ((byteRate >> 8) & 0xff);
	    header[30] = (byte) ((byteRate >> 16) & 0xff);
	    header[31] = (byte) ((byteRate >> 24) & 0xff);
	    header[32] = (byte) (2 * 16 / 8);  // block align
	    header[33] = 0;
	    header[34] = RECORDER_BPP;  // bits per sample
	    header[35] = 0;
	    header[36] = 'd';
	    header[37] = 'a';
	    header[38] = 't';
	    header[39] = 'a';
	    header[40] = (byte) (totalAudioLen & 0xff);
	    header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
	    header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
	    header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
	
	    out.write(header, 0, 44);	    
	}

	

		
}
