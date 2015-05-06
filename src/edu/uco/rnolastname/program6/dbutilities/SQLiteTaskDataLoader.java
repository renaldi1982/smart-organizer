package edu.uco.rnolastname.program6.dbutilities;

import java.util.List;

import android.content.Context;
import android.util.Log;

public class SQLiteTaskDataLoader extends AbstractDataLoader<List<Task>>{

	private DataSource<Task> mDataSource;
	private String mSelection;
	private String[] mSelectionArgs;
	private String mGroupBy;
	private String mHaving;
	private String mOrderBy;
	
	public SQLiteTaskDataLoader(Context context, DataSource dataSource, String selection,
			String[] selectionArgs, String groupBy, String having, String orderBy){
		super(context);
		
		mDataSource = dataSource;
		mSelection = selection;
		mSelectionArgs = selectionArgs;
		mGroupBy = groupBy;
		mHaving = having;
		mOrderBy = orderBy;
	}

	@Override
	protected List<Task> buildList() {		
		List<Task> taskList = mDataSource.read(mSelection, 
				mSelectionArgs, mGroupBy, mHaving, mOrderBy);
		
		return taskList;
	}
	
	public void insert(Task entity){				
		new InsertTask(this).execute(entity);		
	}
	
	public void update(Task entity){
		new UpdateTask(this).execute(entity);
	}
	
	public void delete(Task entity){
		new DeleteTask(this).execute(entity);
	}
	
	private class InsertTask extends ContentChangingTask<Task, Void, Void>{
		InsertTask(SQLiteTaskDataLoader loader){
			super(loader);
		}
		
		@Override
		protected Void doInBackground(Task... params){
			mDataSource.insert(params[0]);
			return (null);
		}
	}
	
	private class UpdateTask extends ContentChangingTask<Task, Void, Void>{
		UpdateTask(SQLiteTaskDataLoader loader){
			super(loader);
		} 
		
		@Override 
		protected Void doInBackground(Task... params){
			mDataSource.update(params[0]);
			return (null);
		}
	}
	
	private class DeleteTask extends ContentChangingTask<Task, Void, Void>{
		DeleteTask(SQLiteTaskDataLoader loader){
			super(loader);
		}
		
		@Override 
		protected Void doInBackground(Task... params){
			mDataSource.delete(params[0]);
			return (null);
		}
	}
	
}
