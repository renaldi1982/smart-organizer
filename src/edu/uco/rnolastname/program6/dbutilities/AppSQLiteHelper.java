/*
 * Migrate using Loader
 */

package edu.uco.rnolastname.program6.dbutilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

public class AppSQLiteHelper extends SQLiteOpenHelper{	
	//LogCat tag
	private static final String LOG = "AppSQLiteHelper";
	
	//Database name/s
	private static final String DATABASE_NAME = "appscheduler.db";
	
	//Database version
	private static final int DATABASE_VERSION = 3;		
	
	//Table name
	public static final String ACCOUNT_TABLE_NAME = "accounts";
	public static final String TASK_TABLE_NAME = "tasks";
//	public static final String MAPTASKLOCATION_TABLE_NAME = "maptasklocations";
	
	//Account column names
	public static final String ACCOUNT_COLUMN_ID = "account_id";
	public static final String ACCOUNT_MODIFIED_AT = "modified_at";
	public static final String ACCOUNT_COLUMN_USERNAME = "username";
	public static final String ACCOUNT_COLUMN_PASSWORD = "password";
	public static final String ACCOUNT_COLUMN_FNAME = "fname";
	public static final String ACCOUNT_COLUMN_LNAME = "lname";
	public static final String ACCOUNT_COLUMN_DOB = "dob";
	public static final String ACCOUNT_COLUMN_ADDRESS = "address";
	public static final String ACCOUNT_COLUMN_CITY = "city";
	public static final String ACCOUNT_COLUMN_STATE = "state";
	public static final String ACCOUNT_COLUMN_ZIP = "zip";
	public static final String ACCOUNT_COLUMN_GENDER = "gender";
	public static final String ACCOUNT_COLUMN_EMAIL = "email";		
	
	//task column name
	public static final String TASK_COLUMN_ID = "task_id";
	public static final String TASK_MODIFIED_AT = "modified_at";
	public static final String TASK_ACCOUNT_ID = "task_account_id";
	public static final String TASK_NAME = "task_name";		
	public static final String TASK_CATEGORY = "category";
	public static final String TASK_DATE = "date";
	public static final String TASK_PRIORITY = "priority"; //1 - high, 2 - normal 3 - not important
	public static final String TASK_MAP_DISTANCE = "map_distance";
	public static final String TASK_MAP_LATLONGNAME = "map_latlongname";
	public static final String TASK_MAP_LATLONG = "map_latlong"; //latitude longitude (double) - what if its a path?	
	public static final String TASK_AUDIO_PATH = "audio_path"; //
	public static final String TASK_PICTURE_PATH = "picture_path";	
	
	//Table create statements
	//Account table create statements
	private static final String CREATE_TABLE_ACCOUNT = "CREATE TABLE " 
			+ ACCOUNT_TABLE_NAME + "(" 
			+ ACCOUNT_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
			+ ACCOUNT_MODIFIED_AT + " DATETIME," 
			+ ACCOUNT_COLUMN_USERNAME + " TEXT,"
			+ ACCOUNT_COLUMN_PASSWORD + " TEXT,"
			+ ACCOUNT_COLUMN_FNAME + " TEXT,"
			+ ACCOUNT_COLUMN_LNAME + " TEXT,"			
			+ ACCOUNT_COLUMN_ADDRESS + " TEXT,"
			+ ACCOUNT_COLUMN_CITY + " TEXT,"
			+ ACCOUNT_COLUMN_STATE + " TEXT,"
			+ ACCOUNT_COLUMN_ZIP + " INTEGER,"
			+ ACCOUNT_COLUMN_DOB + " TEXT,"
			+ ACCOUNT_COLUMN_GENDER + " TEXT,"
			+ ACCOUNT_COLUMN_EMAIL + " TEXT"
			+ ")";		
	
	//Task table create statements
	private static final String CREATE_TABLE_TASK = "CREATE TABLE "
			+ TASK_TABLE_NAME + "("
			+ TASK_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
			+ TASK_NAME + " TEXT,"
			+ TASK_ACCOUNT_ID + " INTEGER NOT NULL,"
			+ TASK_MODIFIED_AT + " DATETIME,"
			+ TASK_CATEGORY + " TEXT,"
			+ TASK_DATE + " DATETIME,"
			+ TASK_PRIORITY + " INTEGER,"
			+ TASK_MAP_LATLONGNAME + " TEXT,"
			+ TASK_MAP_LATLONG + " TEXT,"			
			+ TASK_AUDIO_PATH + " TEXT,"
			+ TASK_PICTURE_PATH + " TEXT"
			+ ")";	
						
	private static String convertToHex(byte[] data) throws java.io.IOException{
		StringBuffer sb = new StringBuffer();
		String hex = null;
		
		hex = Base64.encodeToString(data, 0, data.length, 0x00000000);
		
		sb.append(hex);
		return sb.toString();		 		
	}
			
	public String hashPass(String pass)
	{	
		String encPass = null;
		MessageDigest mdSha1 = null;
		try{
			mdSha1 = MessageDigest.getInstance("SHA-1");
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}
		
		try{
			mdSha1.update(pass.getBytes("ASCII"));
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		
		byte[] data = mdSha1.digest();
		
		try{
			encPass = convertToHex(data);
		}catch(IOException e){
			e.printStackTrace();
		}
		return encPass;
	}		
				
	public AppSQLiteHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	public AppSQLiteHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version); 
	}
	@Override
	public void onCreate(SQLiteDatabase db) {		
		try{
			Log.d("DEBUG","creating table ");
			db.execSQL(CREATE_TABLE_ACCOUNT);
			db.execSQL(CREATE_TABLE_TASK);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DEBUG","error in creating table");
		}	
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(AppSQLiteHelper.class.getName(), 
				"Upgrading db from ver: " + oldVersion + " to "
				+ " ver: " + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + ACCOUNT_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE_NAME);
		onCreate(db);
	}

}
