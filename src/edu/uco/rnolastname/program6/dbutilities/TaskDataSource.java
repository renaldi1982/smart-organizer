package edu.uco.rnolastname.program6.dbutilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TaskDataSource extends DataSource<Task>{	
	
	//table name
	public static final String TASK_TABLE_NAME = "Tasks";
	
	//task column name
	public static final String TASK_COLUMN_ID = "task_id";
	public static final String TASK_MODIFIED_AT = "modified_at";
	public static final String TASK_ACCOUNT_ID = "task_account_id";
	public static final String TASK_REMINDER_ID = "task_reminder_id";
	public static final String TASK_NAME = "task_name";		
	public static final String TASK_CATEGORY = "task_category";
	public static final String TASK_DATE = "task_date";	
	public static final String TASK_PRIORITY = "task_priority"; //1 - high, 2 - normal 3 - not important
	public static final String TASK_MAP_DISTANCE = "task_map_distance";
	public static final String TASK_MAP_LATLONGNAME = "task_map_latlongname";
	public static final String TASK_MAP_LATLONG = "task_map_latlong"; //latitude longitude (double) - what if its a path?	
	public static final String TASK_AUDIO_PATH = "task_audio_path"; //
	public static final String TASK_PICTURE_PATH = "task_picture_path";	
	public static final String TASK_NOTES = "task_notes";
	
	//Task table create statements
	public static final String CREATE_TABLE_TASK = "CREATE TABLE " 
			+ TASK_TABLE_NAME + "(" 
			+ TASK_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
			+ TASK_MODIFIED_AT + " DATETIME," 
			+ TASK_ACCOUNT_ID + " TEXT,"
			+ TASK_REMINDER_ID + " INTEGER,"
			+ TASK_NAME + " TEXT,"
			+ TASK_CATEGORY + " TEXT,"
			+ TASK_DATE + " TEXT,"			
			+ TASK_PRIORITY + " TEXT,"
			+ TASK_MAP_DISTANCE + " REAL,"
			+ TASK_MAP_LATLONGNAME + " TEXT,"
			+ TASK_MAP_LATLONG + " INTEGER,"
			+ TASK_AUDIO_PATH + " TEXT,"
			+ TASK_PICTURE_PATH + " TEXT,"
			+ TASK_NOTES + " TEXT"
			+ ")";		
	
	public TaskDataSource(SQLiteDatabase database) {
		super(database);
	}

	public boolean isTableExists(){		
		String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='"
				+ TASK_TABLE_NAME +"';";
		
		Cursor rs = mDatabase.rawQuery(query,null);		
				
		return rs.moveToFirst();
		
	}
	
	@Override
	public boolean insert(Task entity) {
		if(entity == null){
			return false;
		}
		
		long result = mDatabase.insert(TASK_TABLE_NAME, null, generateContentValuesFromObject(entity));		
		return result != -1;
	}

	public boolean deleteAll(){
		int result = mDatabase.delete(TASK_TABLE_NAME, null, null);		
		return result != 0;		
	}
	
	@Override
	public boolean delete(Task entity) {
		if(entity == null){
			return false;
		}
		
		int result = mDatabase.delete(TASK_TABLE_NAME, 
				TASK_COLUMN_ID + " = " + entity.getId() , null);
		
		return result != 0;
	}

	@Override
	public boolean update(Task entity) {
		if(entity == null){
			return false;
		}
		
		int result = mDatabase.update(TASK_TABLE_NAME,
				generateContentValuesFromObject(entity),
				TASK_COLUMN_ID + " = " + entity.getId(),null);
		
		return result != 0;
	}
	
	@Override
	public boolean update(Task entity, String[] selectionArgs) {
		if(entity == null){
			return false;
		}
		
		int result = mDatabase.update(TASK_TABLE_NAME,
				generateContentValuesFromObject(entity),
				TASK_COLUMN_ID + " = ?", selectionArgs);
		
		return result != 0;
	}

	@Override
	public List<Task> read() {
		Cursor rs = mDatabase.query(TASK_TABLE_NAME, getAllColumns(), 
				null, null, null, null, null);
		
		List<Task> Tasks = new ArrayList<Task>();
		
		if(rs != null && rs.moveToFirst()){
			do{
				Tasks.add(generateObjectFromCursor(rs));				
			}while(rs.moveToNext());
			rs.close();
		}
		
		return Tasks;
	}	
	
	@Override
	public List<Task> read(String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
		
		Cursor rs = mDatabase.query(TASK_TABLE_NAME, getAllColumns(), 
				selection, selectionArgs, groupBy, having, orderBy);
		
		List<Task> Tasks = new ArrayList<Task>();
		
		if(rs != null && rs.moveToFirst()){
			do{
				Tasks.add(generateObjectFromCursor(rs));				
			}while(rs.moveToNext());
			
			rs.close();
		}
		
		return Tasks;
	}

	public String[] getAllColumns(){
		return new String[] {TASK_COLUMN_ID,TASK_MODIFIED_AT,TASK_ACCOUNT_ID,TASK_REMINDER_ID,
				TASK_NAME,TASK_CATEGORY,TASK_DATE,
				TASK_PRIORITY,TASK_MAP_DISTANCE,TASK_MAP_LATLONGNAME,
				TASK_MAP_LATLONG,TASK_AUDIO_PATH,TASK_PICTURE_PATH,TASK_NOTES};
	}
	
	public Task generateObjectFromCursor(Cursor rs){
		if(rs == null){
			return null;
		}		
		Task task = new Task();
		task.setId(Integer.parseInt(rs.getString(0)));		
		task.setModifiedAt(getStringDate(rs.getString(1)));
		task.setAccountId(Integer.parseInt(rs.getString(2)));
		task.setReminderId(Integer.parseInt(rs.getString(3)));
		task.setTaskName(rs.getString(4));								
		task.setCategory(rs.getString(5));
		task.setDate(rs.getString(6));	
		task.setPriority(Integer.parseInt(rs.getString(7)));
		task.setMapDistance(Double.parseDouble(rs.getString(8)));
		task.setMapLatlongName(rs.getString(9));
		task.setMapLatlong(rs.getString(10));
		task.setAudioPath(rs.getString(11));
		task.setPicturePath(rs.getString(12));
		task.setNotes(rs.getString(13));
		
		return task;
	}
	
	public ContentValues generateContentValuesFromObject(Task task){
		if(task == null){
			return null;
		}
		
		ContentValues vals = new ContentValues();
		vals.put(TASK_MODIFIED_AT, getDateTime());
		vals.put(TASK_ACCOUNT_ID, task.getAccountId());
		vals.put(TASK_REMINDER_ID, task.getReminderId());
		vals.put(TASK_NAME, task.getTaskName());		
		vals.put(TASK_CATEGORY, task.getCategory());
		vals.put(TASK_DATE, task.getDate());
		vals.put(TASK_PRIORITY, task.getPriority());	
		vals.put(TASK_MAP_DISTANCE, task.getMapDistance());
		vals.put(TASK_MAP_LATLONGNAME, task.getMapLatlongName());
		vals.put(TASK_MAP_LATLONG, task.getMapLatlong());
		vals.put(TASK_AUDIO_PATH, task.getAudioPath());
		vals.put(TASK_PICTURE_PATH, task.getPicturePath());
		vals.put(TASK_NOTES, task.getNotes());
		
		return vals;
		
	}
	
	public boolean isTaskNameExist(String taskName){		
		
		Cursor rs = mDatabase.query(TASK_TABLE_NAME, getAllColumns(), 
				TASK_NAME + " = ?", new String[] {taskName}, null, null, null);
		Log.d("DEBUG","task count in task data source: " + rs.getCount());									
		return rs.getCount() > 0;
	}
	
	public String getDateTime(){
		SimpleDateFormat df = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date();
		
		return df.format(date);
	}		
	
	public static String formatDateTime(Context context, String timeToFormat) {

	    String finalDateTime = "";          

	    SimpleDateFormat iso8601Format = new SimpleDateFormat(
	            "yyyy-MM-dd HH:mm:ss");

	    Date date = null;
	    if (timeToFormat != null) {
	        try {
	            date = iso8601Format.parse(timeToFormat);
	        } catch (ParseException e) {
	            date = null;
	        }

	        if (date != null) {
	            long when = date.getTime();
	            int flags = 0;
	            flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
	            flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
	            flags |= android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
	            flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR;

	            finalDateTime = android.text.format.DateUtils.formatDateTime(context,
	            when + TimeZone.getDefault().getOffset(when), flags);               
	        }
	    }
	    return finalDateTime;
	}
	
	public String getStringDate(String date){		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());		
		Date dt = null;
		try {
			dt = df.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//use this only when we want to do formatting
//		long when = dt.getTime();
//		int flags = 0;
//		flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
//		flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
//		flags |= android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
//		flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR;
		return df.format(dt);
		
	}
}
