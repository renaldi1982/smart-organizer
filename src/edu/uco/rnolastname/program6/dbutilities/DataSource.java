package edu.uco.rnolastname.program6.dbutilities;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;

public abstract class DataSource<T> {
	protected SQLiteDatabase mDatabase;
	public DataSource(SQLiteDatabase database){
		this.mDatabase = database;
	}
	
	public abstract boolean insert(T entity);
	public abstract boolean delete(T entity);
	public abstract boolean update(T entity);
	public abstract boolean update(T entity, String[] selectionArgs);
	
	public abstract List<T> read();
	
	public abstract List<T> read(String selection, String[] selectionArgs, 
			String groupBy, String having, String orderBy);
}
