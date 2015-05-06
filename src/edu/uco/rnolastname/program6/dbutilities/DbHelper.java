package edu.uco.rnolastname.program6.dbutilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper{
	private static final String DATABASE_NAME = "appsmartorganizer.db";
	private static final int DATABASE_VERSION = 7;
	
	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TaskDataSource.CREATE_TABLE_TASK);
		Log.d("DEBUG","Create Table accounts");
		db.execSQL(AccDataSource.CREATE_TABLE_ACCOUNT);
		Log.d("DEBUG","Create Table accounts");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {		
		Log.d("DEBUG","Updating database to version: " + DATABASE_VERSION);
		db.execSQL("DROP TABLE IF EXISTS " + AccDataSource.ACCOUNT_TABLE_NAME);
		Log.d("DEBUG","Drop Table accounts");
		db.execSQL("DROP TABLE IF EXISTS " + TaskDataSource.TASK_TABLE_NAME);
		Log.d("DEBUG","Drop Table tasks");
		onCreate(db);
	}

}
