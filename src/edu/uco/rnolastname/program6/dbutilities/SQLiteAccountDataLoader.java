package edu.uco.rnolastname.program6.dbutilities;

import java.util.List;

import android.content.Context;

public class SQLiteAccountDataLoader extends AbstractDataLoader<List<Account>>{
	private DataSource<Account> mDataSource;
	private String mSelection;
	private String[] mSelectionArgs;
	private String mGroupBy;
	private String mHaving;
	private String mOrderBy;
	
	public SQLiteAccountDataLoader(Context context, DataSource dataSource, String selection,
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
	protected List<Account> buildList() {
		List<Account> accountList = mDataSource.read(mSelection, 
				mSelectionArgs, mGroupBy, mHaving, mOrderBy);
		
		return accountList;
		
	}
	
	public void insert(Account entity){
		new InsertTask(this).execute(entity);
	}
	
	public void update(Account entity){
		new UpdateTask(this).execute(entity);
	}
	
	public void delete(Account entity){
		new DeleteTask(this).execute(entity);
	}
	
	private class InsertTask extends ContentChangingTask<Account, Void, Void>{
		InsertTask(SQLiteAccountDataLoader loader){
			super(loader);
		}
		
		@Override
		protected Void doInBackground(Account... params){
			mDataSource.insert(params[0]);
			return (null);
		}
	}
	
	private class UpdateTask extends ContentChangingTask<Account, Void, Void>{
		UpdateTask(SQLiteAccountDataLoader loader){
			super(loader);
		}
		
		@Override 
		protected Void doInBackground(Account... params){
			mDataSource.update(params[0]);
			return (null);
		}
	}
	
	private class DeleteTask extends ContentChangingTask<Account, Void, Void>{
		DeleteTask(SQLiteAccountDataLoader loader){
			super(loader);
		}
		
		@Override 
		protected Void doInBackground(Account... params){
			mDataSource.delete(params[0]);
			return (null);
		}
	}
}
