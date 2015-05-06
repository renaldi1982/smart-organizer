package edu.uco.rnolastname.program6.dbutilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AccountDataSource {
	private Context context;
	
	//DB fields
	private SQLiteDatabase db;
	private AppSQLiteHelper dbHelper;
	
	public AccountDataSource(Context context){
		this.context = context;
		dbHelper = new AppSQLiteHelper(context);	
	}
	
	public void open()throws SQLException{
		db = dbHelper.getWritableDatabase();
	}	
	
	public boolean isTableExists(String tableName){
		open();			
		String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='"
				+ tableName +"';";
		
		Cursor rs = db.rawQuery(query,null);		
				
		return rs.moveToFirst();
		
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
	
	public String HashPassword(String pass){
		String hashPass = dbHelper.hashPass(pass);
		return hashPass;
	}
	
	public String getDateTime(){
		SimpleDateFormat df = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date();
		
		return df.format(date);
	}
	
	/* task SQL interface */
	public boolean isTaskExists(String taskName){		
		open();
		String query = "SELECT * FROM " + AppSQLiteHelper.TASK_TABLE_NAME 
				+ " WHERE " + AppSQLiteHelper.TASK_NAME + " = ?"; 
		
		Cursor rs = db.rawQuery(query, new String[] {taskName});
				
		return rs.moveToFirst();
	}
	
	public void taskInsertValues(Task task){
		
		open();
		ContentValues vals = new ContentValues();
		vals.put(AppSQLiteHelper.TASK_ACCOUNT_ID, task.getAccountId());
		vals.put(AppSQLiteHelper.TASK_NAME, task.getTaskName());
		vals.put(AppSQLiteHelper.TASK_MODIFIED_AT, getDateTime());
		vals.put(AppSQLiteHelper.TASK_CATEGORY, task.getCategory());
		vals.put(AppSQLiteHelper.TASK_DATE, task.getDate());
		vals.put(AppSQLiteHelper.TASK_PRIORITY, task.getPriority());	
		vals.put(AppSQLiteHelper.TASK_MAP_LATLONGNAME, task.getMapLatlongName());
		vals.put(AppSQLiteHelper.TASK_MAP_LATLONG, task.getMapLatlong());
		vals.put(AppSQLiteHelper.TASK_AUDIO_PATH, task.getAudioPath());
		vals.put(AppSQLiteHelper.TASK_PICTURE_PATH, task.getPicturePath());
		
		db.insert(AppSQLiteHelper.TASK_TABLE_NAME, null, vals);
		db.close();
	}		
	
	public ArrayList<Task> taskFetchAllValues(int account_id,String category){
		open();
		ArrayList<Task> tasks = new ArrayList<Task>();

		Cursor rs = db.query(true, AppSQLiteHelper.TASK_TABLE_NAME, 
				new String[] {AppSQLiteHelper.TASK_COLUMN_ID,AppSQLiteHelper.TASK_NAME,AppSQLiteHelper.TASK_ACCOUNT_ID,
				AppSQLiteHelper.TASK_MODIFIED_AT,AppSQLiteHelper.TASK_CATEGORY,AppSQLiteHelper.TASK_DATE,
				AppSQLiteHelper.TASK_PRIORITY,AppSQLiteHelper.TASK_MAP_LATLONGNAME,AppSQLiteHelper.TASK_MAP_LATLONG,
				AppSQLiteHelper.TASK_AUDIO_PATH,AppSQLiteHelper.TASK_PICTURE_PATH}, 
				AppSQLiteHelper.TASK_ACCOUNT_ID + " = ? AND " + AppSQLiteHelper.TASK_CATEGORY + " = ?", 
				new String[] {String.valueOf(account_id),category}, 
				AppSQLiteHelper.TASK_NAME, null, null, null, null);
		
		if(rs.moveToFirst()){
			do{
				Task task = new Task();
				task.setId(Integer.parseInt(rs.getString(0)));		
				task.setTaskName(rs.getString(1));
				task.setAccountId(Integer.parseInt(rs.getString(2)));				
				task.setModifiedAt(getStringDate(rs.getString(3)));
				task.setCategory(rs.getString(4));
				task.setDate(rs.getString(5));	
				task.setPriority(Integer.parseInt(rs.getString(6)));
				task.setMapLatlongName(rs.getString(7));
				task.setMapLatlong(rs.getString(8));
				task.setAudioPath(rs.getString(9));
				task.setPicturePath(rs.getString(10));
				
				tasks.add(task);
			}while(rs.moveToNext());
		}else{
			Log.d("DEBUG","Error fetching task - AccountDataSource - null cursor");
		}
		
		return tasks;
	}
	
	public void taskDeleteAll(){
		open();
		
		String query = "DELETE FROM " + AppSQLiteHelper.TASK_TABLE_NAME;		
		db.execSQL(query);
		Log.d("DEBUG","deleted all tasks");
	}
	
	public boolean taskDelete(int id){
		open();
		
		return db.delete(AppSQLiteHelper.TASK_TABLE_NAME, 
				AppSQLiteHelper.TASK_COLUMN_ID + " = ?", new String[] {String.valueOf(id)}) > 0;
	}
	
	/* account SQL interface*/
	public void accInsertValues(Account acc){	
		
		open();
		ContentValues vals = new ContentValues();
		vals.put(AppSQLiteHelper.ACCOUNT_MODIFIED_AT, getDateTime());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_USERNAME, acc.getUsername());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_PASSWORD, dbHelper.hashPass(acc.getPassword()));
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_FNAME, acc.getfName());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_LNAME, acc.getlName());		
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_ADDRESS, acc.getAddress());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_CITY, acc.getCity());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_STATE, acc.getState());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_ZIP, acc.getZip());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_DOB, acc.getDob());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_GENDER, acc.getGender());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_EMAIL, acc.getEmail());
		
		//insert rows
		db.insert(AppSQLiteHelper.ACCOUNT_TABLE_NAME, null, vals);
		db.close();				
	}				
			
	public List<Account> accFetchAllValues(){
		open();
		List<Account> accounts = new ArrayList<Account>();
		String query = "SELECT * FROM " + AppSQLiteHelper.ACCOUNT_TABLE_NAME;
				
		Cursor rs = db.rawQuery(query, null);
		if(rs.moveToFirst()){
			do{
				Account acc = new Account();
				acc.setId(Integer.parseInt(rs.getString(0)));
				acc.setModifiedat(getStringDate(rs.getString(1)));
				acc.setUsername(rs.getString(2));
				// NOTE: disable this later because we don't want to retrieve hashed password
				acc.setPassword(rs.getString(3));
				acc.setfName(rs.getString(4));
				acc.setlName(rs.getString(5));
				acc.setAddress(rs.getString(6));
				acc.setCity(rs.getString(7));
				acc.setState(rs.getString(8));
				acc.setZip(Integer.parseInt(rs.getString(9)));
				acc.setDob(rs.getString(10));
				acc.setGender(rs.getString(11));
				acc.setEmail(rs.getString(12));
				
				accounts.add(acc);
			}while(rs.moveToNext());
		}
		
		return accounts;
	}
	
	public Account fetchAccount(String username, String password){		
		open();				
		Cursor rs = db.query(AppSQLiteHelper.ACCOUNT_TABLE_NAME, new String[] {AppSQLiteHelper.ACCOUNT_COLUMN_ID,
				AppSQLiteHelper.ACCOUNT_MODIFIED_AT, AppSQLiteHelper.ACCOUNT_COLUMN_USERNAME,
				AppSQLiteHelper.ACCOUNT_COLUMN_PASSWORD, AppSQLiteHelper.ACCOUNT_COLUMN_FNAME,
				AppSQLiteHelper.ACCOUNT_COLUMN_LNAME, AppSQLiteHelper.ACCOUNT_COLUMN_ADDRESS,
				AppSQLiteHelper.ACCOUNT_COLUMN_CITY, AppSQLiteHelper.ACCOUNT_COLUMN_STATE,
				AppSQLiteHelper.ACCOUNT_COLUMN_ZIP, AppSQLiteHelper.ACCOUNT_COLUMN_DOB,
				AppSQLiteHelper.ACCOUNT_COLUMN_GENDER, AppSQLiteHelper.ACCOUNT_COLUMN_EMAIL},
				AppSQLiteHelper.ACCOUNT_COLUMN_USERNAME + " = ? AND " + AppSQLiteHelper.ACCOUNT_COLUMN_PASSWORD + " = ?",
				new String[] {username,password},null,null,null,null); 
		
		Account acc = new Account();		
		if(rs != null){
			rs.moveToFirst();
						
			acc.setId(Integer.parseInt(rs.getString(0)));
			acc.setModifiedat(getStringDate(rs.getString(1)));
			acc.setUsername(rs.getString(2));
			acc.setPassword(rs.getString(3));
			acc.setfName(rs.getString(4));
			acc.setlName(rs.getString(5));		
			acc.setAddress(rs.getString(6));
			acc.setCity(rs.getString(7));
			acc.setState(rs.getString(8));
			acc.setZip(Integer.parseInt(rs.getString(9)));
			acc.setGender(rs.getString(10));
			acc.setEmail(rs.getString(11));
			
		}else{
			Log.d("DEBUG","Error fetching account - AccountDataSource - null cursor ");
		}
							
		return acc;
	}
	
	public int accUpdateAllColumns(Account acc){		
		open();
		
		ContentValues vals = new ContentValues();
		vals.put(AppSQLiteHelper.ACCOUNT_MODIFIED_AT,getDateTime());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_USERNAME, acc.getUsername());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_PASSWORD, dbHelper.hashPass(acc.getPassword()));
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_FNAME, acc.getfName());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_LNAME, acc.getlName());		
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_ADDRESS, acc.getAddress());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_CITY, acc.getCity());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_STATE, acc.getState());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_ZIP, acc.getZip());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_DOB, acc.getDob());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_GENDER, acc.getGender());
		vals.put(AppSQLiteHelper.ACCOUNT_COLUMN_EMAIL, acc.getEmail());
		
		return db.update(AppSQLiteHelper.ACCOUNT_TABLE_NAME, vals, AppSQLiteHelper.ACCOUNT_COLUMN_ID + " = ?", 
				new String[] {String.valueOf(acc.getId())});
	}
	
	public void accDeleteAccount(Account acc){
		open();
		
		db.delete(AppSQLiteHelper.ACCOUNT_TABLE_NAME, AppSQLiteHelper.ACCOUNT_COLUMN_ID + " = ?", 
				new String[] {String.valueOf(acc.getId())});
		db.close();		
	}
	
	public void accDeleteAllAccount(){
		open();
		
		String query = "DELETE FROM " + AppSQLiteHelper.ACCOUNT_TABLE_NAME;		
		db.execSQL(query);
		Log.d("DEBUG","deleted all accounts");
	}
	
}


//public void mapTaskLocationInsert(Task task, MapTaskLocation location){
//open();
//ContentValues vals = new ContentValues();
//vals.put(AppSQLiteHelper.MAPTASK_COLUMN_TASK_ID, task.getId());
//vals.put(AppSQLiteHelper.MAPTASK_COLUMN_MODIFIED_AT, getDateTime());		
//vals.put(AppSQLiteHelper.MAPTASK_COLUMN_MAP_LAT, location.getMapLat());
//vals.put(AppSQLiteHelper.MAPTASK_COLUMN_MAP_LONG, location.getMapLong());
//
//db.insert(AppSQLiteHelper.MAPTASKLOCATION_TABLE_NAME, null, vals);
//db.close();	
//}
//
//public List<MapTaskLocation> mapTaskFetchAllValues(int task_id){
//open();
//List<MapTaskLocation> mapTaskLocations = new ArrayList<MapTaskLocation>();
//String query = "SELECT * FROM " + AppSQLiteHelper.MAPTASKLOCATION_TABLE_NAME + " WHERE " 
//+ AppSQLiteHelper.MAPTASK_COLUMN_TASK_ID + " = ?";
//
//Cursor rs = db.rawQuery(query, new String[] {String.valueOf(task_id)});
//if(rs.moveToFirst()){
//	do{
//		MapTaskLocation mapLoc = new MapTaskLocation();
//		mapLoc.setId(Integer.parseInt(rs.getString(0)));
//		mapLoc.setTaskID(Integer.parseInt(rs.getString(1)));
//		mapLoc.setModifiedAt(getStringDate(rs.getString(2)));
//		mapLoc.setMapLat(Long.parseLong(rs.getString(2)));
//		mapLoc.setMapLong(Long.parseLong(rs.getString(2)));
//		
//		mapTaskLocations.add(mapLoc);
//	}while(rs.moveToNext());
//}
//
//return mapTaskLocations;
//}