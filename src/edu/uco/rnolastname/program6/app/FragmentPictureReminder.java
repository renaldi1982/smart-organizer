package edu.uco.rnolastname.program6.app;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import edu.uco.rnolastname.program6.R;
import edu.uco.rnolastname.program6.audioreminder.AudioReminder;
import edu.uco.rnolastname.program6.dbutilities.Task;

public class FragmentPictureReminder extends Fragment implements OnClickListener{	
	OnPictureTakenListener mCallback;
	public Activity activity;
	private View view;
	private static Task activeTask = null;
	private Button takeNoteButton;
	private ImageView mImageView;
    private ImageView mThumbnailImageView;
    private static final int PICTUREREMINDER_ID = 10101110;
                     
    public interface OnPictureTakenListener{
    	public void onPictureTaken(int resultCode, Task task);
    }
    
	public FragmentPictureReminder(){
		super();
	}
	
	public static FragmentPictureReminder newInstance(Task task){
		FragmentPictureReminder f = new FragmentPictureReminder();
		
		Bundle b = new Bundle();
		b.putParcelable("task", task);
		f.setArguments(b);
		
		return f;
	}
	
	@Override 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		activeTask = getArguments().getParcelable("task");
		
	}
	
	@Override 
	public void onAttach(Activity activity){
		super.onAttach(activity);
		this.activity = activity;
		
		try{
			mCallback = (OnPictureTakenListener)activity;
		}catch(ClassCastException e){
			throw new ClassCastException(activity.getClass().toString() + " need to implement"
					+ " OnPictureTakenListener");
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		if(activeTask.getPicturePath() != null && !activeTask.getPicturePath().equalsIgnoreCase("")){
			((OnPictureTakenListener)activity).onPictureTaken(Activity.RESULT_OK, activeTask);
		}
		
//		Fragment fPictureReminderNote = getFragmentManager().findFragmentByTag(PICTUREREMINDERNOTE_TAG);
//		if(fPictureReminderNote != null){
//			getFragmentManager().beginTransaction().remove(fPictureReminderNote).addToBackStack(null).commit();
//		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.picturereminder, container, false);
		
		// Set the image view
        mImageView = (ImageView)view.findViewById(R.id.imageViewFullSized);
        mThumbnailImageView = (ImageView)view.findViewById(R.id.imageViewThumbnail);        
        Button takePictureButton = (Button)view.findViewById(R.id.btn_takephoto);
        takeNoteButton = (Button) view.findViewById(R.id.btn_takenote);        
        
        takeNoteButton.setEnabled(false);
        
        // Set OnItemClickListener so we can be notified on button clicks
        takePictureButton.setOnClickListener(this);
        takeNoteButton.setOnClickListener(this);		       
        
		return view;
	}		
	
	@Override
	public void onStart(){
		super.onStart();	
		if(activeTask == null){
			Toast.makeText(activity, "task does not exists", 
					Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
     * The activity returns with the photo.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {       	    	
    	if (requestCode == PICTUREREMINDER_ID && resultCode == Activity.RESULT_OK) {
            addPhotoToGallery();
            MainActivity act = (MainActivity)getActivity();

            // Show the full sized image.
            setFullImageFromFilePath(act.getCurrentPhotoPath(), mImageView);
            setFullImageFromFilePath(act.getCurrentPhotoPath(), mThumbnailImageView);   
            
            if(activeTask != null){            	
            	confirmPictureSave(act);            	         	            
            }                        
            
            takeNoteButton.setEnabled(true);
        } 
    }	   
    
    private void confirmPictureSave(final MainActivity act){
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setMessage("Save Picture?");
	    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				activeTask.setPicturePath(act.getCurrentPhotoPath());
            	if(activeTask.getCategory() != null){
            		activeTask.setCategory(activeTask.getCategory() + MainActivity.pictureReminder);
            	}else{
            		activeTask.setCategory(MainActivity.pictureReminder);
            	}
            	
				Toast.makeText(getActivity(), "Picture saved", Toast.LENGTH_SHORT).show();
			}
		}).setNegativeButton("Discard", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getActivity(), "Picture discarded", Toast.LENGTH_SHORT).show();
				dialog.dismiss();				
			}
		});
	    
	    builder.show();
    }
    
    
    /**
     * Scale the photo down and fit it to our image views.
     *
     */
    public static void setFullImageFromFilePath(String imagePath, ImageView imageView) {    	
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        
        Log.d("DEBUG","targetW : " + targetW + " targetH: " + targetH);
        
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 0;
        if(targetW != 0 && targetH != 0){
            // Determine how much to scale down the image
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * Add the picture to the photo gallery.
     * Must be called on all camera images or they will
     * disappear once taken.
     */
    protected void addPhotoToGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        MainActivity act = (MainActivity)getActivity();
        File f = new File(act.getCurrentPhotoPath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.getActivity().sendBroadcast(mediaScanIntent);
    }    	
	
	protected File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PictureReminder_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        MainActivity act = (MainActivity)getActivity();
        act.setCurrentPhotoPath("file:" + image.getAbsolutePath());
        return image;
    }
	
	protected void dispatchTakePictureIntent() {

        // Check if there is a camera.
        PackageManager packageManager = activity.getApplicationContext().getPackageManager();
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false){
            Toast.makeText(getActivity(), "This device does not have a camera.", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // Camera exists? Then proceed...
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                
        // Ensure that there's a camera activity to handle the intent - but we need context from main activity
        MainActivity act = (MainActivity) getActivity();
        if (takePictureIntent.resolveActivity(act.getPackageManager()) != null) {
            // Create the File where the photo should go.
            // If you don't do this, you may get a crash in some devices.
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast toast = Toast.makeText(act.getApplicationContext(), "There was a problem saving the photo...", Toast.LENGTH_SHORT);
                toast.show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri fileUri = Uri.fromFile(photoFile);
                act.setCapturedImageURI(fileUri);
                act.setCurrentPhotoPath(fileUri.getPath());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                		act.getCapturedImageURI());
                startActivityForResult(takePictureIntent, PICTUREREMINDER_ID);
            }
        }
    }
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch(id){
		case R.id.btn_takephoto:
			dispatchTakePictureIntent();			
			break;
		case R.id.btn_takenote:						
			//NOTHING HERE DELETE THIS CASE				
			break;
		}
		 			 
	}		
}
