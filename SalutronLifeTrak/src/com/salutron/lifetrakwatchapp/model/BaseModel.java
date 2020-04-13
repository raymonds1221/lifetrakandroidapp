package com.salutron.lifetrakwatchapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.content.Context;

import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

public abstract class BaseModel implements Parcelable, SalutronLifeTrakUtility {
	protected long id;
	protected Context mContext;
	private LifeTrakApplication mLifeTrakApplication;
	
	public BaseModel() {}
	
	public BaseModel(Context context) {
		mContext = context;
		mLifeTrakApplication = (LifeTrakApplication) context.getApplicationContext();
	}
	
	public BaseModel(Parcel source) {
		readFromParcel(source);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public abstract void writeToParcel(Parcel dest, int flags);

	public long getId() {
		return id;
	}
	
	public abstract void readFromParcel(Parcel source);
	
	public void update() {
		DataSource.getInstance(mContext).getWriteOperation()
										.open()
										.beginTransaction()
										.update(this)
										.endTransaction()
										.close();
	}
	
	public void insert() {
		DataSource.getInstance(mContext).getWriteOperation()
										.open()
										.beginTransaction()
										.insert(this)
										.endTransaction()
										.close();
	}
	
	public void delete() {
		DataSource.getInstance(mContext).getWriteOperation()
										.open()
										.beginTransaction()
										.delete(this)
										.endTransaction()
										.close();
	}
	
	public void setContext(Context context) {
		mContext = context;
	}

	public Context getContext() {
		return mContext;
	}
	
	protected LifeTrakApplication getLifeTrakApplication() {
		return mLifeTrakApplication;
	}
}
