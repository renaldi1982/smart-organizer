package edu.uco.rnolastname.program6.dbutilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

public class AccDataSource extends DataSource<Account>{
	//table name
	public static final String ACCOUNT_TABLE_NAME = "accounts";
	
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
	
	//Account table create statements
	public static final String CREATE_TABLE_ACCOUNT = "CREATE TABLE " 
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
	
	public AccDataSource(SQLiteDatabase database) {
		super(database);
	}

	public boolean isTableExists(){		
		String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='"
				+ ACCOUNT_TABLE_NAME +"';";
		
		Cursor rs = mDatabase.rawQuery(query,null);		
				
		return rs.moveToFirst();
		
	}	
	
	@Override
	public boolean insert(Account entity) {
		if(entity == null){
			return false;
		}
		
		long result = mDatabase.insert(ACCOUNT_TABLE_NAME, null, generateContentValuesFromObject(entity));
		
		return result != -1;
	}

	@Override
	public boolean delete(Account entity) {
		if(entity == null){
			return false;
		}
		
		int result = mDatabase.delete(ACCOUNT_TABLE_NAME, 
				ACCOUNT_COLUMN_ID + " = " + entity.getId() , null);
		
		return result != 0;
	}
	
	public boolean deleteAll(){
		int result = mDatabase.delete(ACCOUNT_TABLE_NAME,null,null);
		return result != 0;
	}

	@Override
	public boolean update(Account entity) {
		if(entity == null){
			return false;
		}
		
		int result = mDatabase.update(ACCOUNT_TABLE_NAME,
				generateContentValuesFromObject(entity),
				ACCOUNT_COLUMN_ID + " = " + entity.getId(),null);
		
		return result != 0;
	}

	@Override
	public boolean update(Account entity, String[] selectionArgs) {
		if(entity == null){
			return false;
		}
		
		int result = mDatabase.update(ACCOUNT_TABLE_NAME,
				generateContentValuesFromObject(entity),
				ACCOUNT_COLUMN_ID + " = ?",selectionArgs);
		
		return result != 0;
	}
	
	@Override
	public List<Account> read() {
		Cursor rs = mDatabase.query(ACCOUNT_TABLE_NAME, getAllColumns(), 
				null, null, null, null, null);
		
		List<Account> accounts = new ArrayList<Account>();
		
		if(rs != null && rs.moveToFirst()){
			do{
				accounts.add(generateObjectFromCursor(rs));
			}
			while(rs.moveToNext());
			rs.close();
		}
		
		return accounts;
	}	
	
	@Override
	public List<Account> read(String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
		
		Cursor rs = mDatabase.query(ACCOUNT_TABLE_NAME, getAllColumns(), 
				selection, selectionArgs, groupBy, having, orderBy);			
		
		List<Account> accounts = new ArrayList<Account>();
		
		if(rs != null && rs.moveToFirst()){
			do{
				accounts.add(generateObjectFromCursor(rs));
			}
			while(rs.moveToNext());
			rs.close();
		}
		
		return accounts;
	}

	public String[] getAllColumns(){
		return new String[] {ACCOUNT_COLUMN_ID, ACCOUNT_MODIFIED_AT, ACCOUNT_COLUMN_USERNAME,
				ACCOUNT_COLUMN_PASSWORD, ACCOUNT_COLUMN_FNAME, ACCOUNT_COLUMN_LNAME,
				ACCOUNT_COLUMN_ADDRESS,ACCOUNT_COLUMN_CITY,ACCOUNT_COLUMN_STATE,ACCOUNT_COLUMN_ZIP,
				ACCOUNT_COLUMN_DOB,ACCOUNT_COLUMN_GENDER,ACCOUNT_COLUMN_EMAIL};
	}
	
	public Account generateObjectFromCursor(Cursor rs){
		if(rs == null){
			return null;
		}
		
		Account acc = new Account();
		
		acc.setId(Integer.parseInt(rs.getString(0)));
		acc.setModifiedat(getStringDate(rs.getString(1)));
		acc.setUsername(rs.getString(2));
		/* NOTE: disable this later because we don't want to retrieve hashed password
		 * or if someone can retrieve the hash then they can decrypt it
		 */
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
				
		return acc;
	}
	
	public ContentValues generateContentValuesFromObject(Account acc){
		if(acc == null){
			return null;
		}
		
		ContentValues vals = new ContentValues();
		vals.put(ACCOUNT_MODIFIED_AT, getDateTime());
		vals.put(ACCOUNT_COLUMN_USERNAME, acc.getUsername());
		vals.put(ACCOUNT_COLUMN_PASSWORD, hashPass(acc.getPassword()));
		vals.put(ACCOUNT_COLUMN_FNAME, acc.getfName());
		vals.put(ACCOUNT_COLUMN_LNAME, acc.getlName());		
		vals.put(ACCOUNT_COLUMN_ADDRESS, acc.getAddress());
		vals.put(ACCOUNT_COLUMN_CITY, acc.getCity());
		vals.put(ACCOUNT_COLUMN_STATE, acc.getState());
		vals.put(ACCOUNT_COLUMN_ZIP, acc.getZip());
		vals.put(ACCOUNT_COLUMN_DOB, acc.getDob());
		vals.put(ACCOUNT_COLUMN_GENDER, acc.getGender());
		vals.put(ACCOUNT_COLUMN_EMAIL, acc.getEmail());
		
		return vals;
		
	}
	
	public String getDateTime(){
		SimpleDateFormat df = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date();
		
		return df.format(date);
	}
	
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
	
	public String getStringDate(String date){		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());		
		Date dt = null;
		try {
			dt = df.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return df.format(dt);
	}	
}
