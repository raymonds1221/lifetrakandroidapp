package com.salutron.lifetrakwatchapp.db;

import java.lang.reflect.Field;
import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.salutron.lifetrakwatchapp.model.BaseModel;
import com.salutron.lifetrakwatchapp.util.SalutronObjectList;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;
import com.salutron.lifetrakwatchapp.annotation.DataTable;
import com.salutron.lifetrakwatchapp.annotation.DataColumn;

/**
 * DataSource
 * 
 * @author rsarmiento
 *
 */
public final class DataSource implements SalutronLifeTrakUtility {
	private static final Object LOCK_OBJECT = DataSource.class;
	private static DataSource sDataSource;
	private DataSourceWrite mDataSourceWrite;
	private DataSourceRead mDataSourceRead;
	private DatabaseHelper mDatabaseHelper;
	private SQLiteDatabase mSQLiteWrite;
	private SQLiteDatabase mSQLiteRead;
	private Context mContext;
	
	private DataSource(Context context) {
		mDatabaseHelper = DatabaseHelper.getInstance(context);
		mContext = context;
	}
	
	public static final DataSource getInstance(Context context) {
		synchronized(LOCK_OBJECT) {
			if(sDataSource == null)
				sDataSource = new DataSource(context);
			return sDataSource;
		}
	}
	
	/**
	 * Method for retrieving (DataSourceWrite) interface
	 * 
	 * @return Returns (DataSourceWrite) interface
	 */
	public DataSourceWrite getWriteOperation() {
		synchronized(LOCK_OBJECT) {
			if(mDataSourceWrite == null)
				mDataSourceWrite = new DataSourceWriteClass();
			return mDataSourceWrite;
		}
	}
	
	/**
	 * Method for retrieving (DataSourceRead) interface
	 * 
	 * @return Returns (DataSourceRead) interface
	 */
	public DataSourceRead getReadOperation() {
		synchronized(LOCK_OBJECT) {
			if(mDataSourceRead == null)
				mDataSourceRead = new DataSourceReadClass();
			return mDataSourceRead;
		}
	}
	
	// Close databases
	public void closeDB() {
		mSQLiteRead.close();
		mSQLiteWrite.close();
	}
	
	private class DataSourceWriteClass implements DataSourceWrite {
		
		@Override
		public <T extends BaseModel> DataSourceWrite update(T t) {
			String tableName = tableNameFromModel(t.getClass());
			
			try {
				Field fieldId = t.getClass().getSuperclass().getDeclaredField("id");
				fieldId.setAccessible(true);
				ContentValues contentValues = valuesFromModel(t);
				long id = fieldId.getLong(t);
				mSQLiteWrite.update(tableName, contentValues, "_id=?", 
										new String[] { String.valueOf(id) });
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
			
			return this;
		}

		@Override
		public <T extends BaseModel> DataSourceWrite insert(T t) {
			if(t == null)
				return this;
			
			String tableName = tableNameFromModel(t.getClass());
			
			try {
				Field fieldId = t.getClass().getSuperclass().getDeclaredField("id");
				ContentValues contentValues = valuesFromModel(t);
				long id = mSQLiteWrite.insert(tableName, null, contentValues);

				fieldId.setAccessible(true);
				fieldId.set(t, id);
				
				insertChild(t);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
			
			return this;
		}
		
		public <T extends BaseModel> DataSourceWrite insert(List<T> l) {
			if(l == null)
				return this;
			
			for(T t : l) {
				insert(t);
			}
			return this;
		}
		
		private <T extends BaseModel> void insertChild(T parent) throws IllegalAccessException, IllegalArgumentException {
			Field[] fields = parent.getClass().getDeclaredFields();
			
			for(Field field : fields) {
				if(field.getType().isAssignableFrom(List.class)) {
					field.setAccessible(true);
					@SuppressWarnings("unchecked")
					List<T> child = (List<T>) field.get(parent);
					insert(child);
				}
			}
		}

		@Override
		public <T extends BaseModel> DataSourceWrite delete(T t) {
			String tableName = tableNameFromModel(t.getClass());
			
			try {
                Field id = t.getClass().getSuperclass().getDeclaredField("id");
                id.setAccessible(true);
                mSQLiteWrite.delete(tableName, "_id=?",
                        new String[] { String.valueOf(id.getLong(t)) });

			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
            catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			
			return this;
		}

		@Override
		public <T extends BaseModel> DataSourceWrite deleteAll(T t) {
			String tableName = tableNameFromModel(t.getClass());

			try {
				Field id = t.getClass().getSuperclass().getDeclaredField("id");
				id.setAccessible(true);
				mSQLiteWrite.delete(tableName, null,
						null);

			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}catch (IllegalArgumentException e) {
				e.printStackTrace();
			}

			return this;
		}
		
		@Override
		public DataSourceWrite open() {
			mSQLiteWrite = mDatabaseHelper.getWritableDatabase();
			return this;
		}

		@Override
		public DataSourceWrite execQuery(String query) {
			mSQLiteWrite.execSQL(query);
			return this;
		}

		@Override
		public DataSourceWrite beginTransaction() {
			mSQLiteWrite.beginTransaction();
			return this;
		}

		@Override
		public DataSourceWrite endTransaction() {
			mSQLiteWrite.setTransactionSuccessful();
			mSQLiteWrite.endTransaction();
			return this;
		}

		@Override
		public void close() {
		}
		
		private <T extends BaseModel> ContentValues valuesFromModel(T t) throws IllegalAccessException, IllegalArgumentException {
			ContentValues contentValues = new ContentValues();
			Field[] fields = t.getClass().getDeclaredFields();
					
			for(Field field: fields) {
				Annotation annotation = field.getAnnotation(DataColumn.class);
				
				if(annotation != null) {
					DataColumn dataColumn = (DataColumn) annotation;
					field.setAccessible(true);
					
					if(field.getType().isAssignableFrom(String.class)) {
						String value = "";
						
						if(field.get(t) != null)
							value = field.get(t).toString();
						contentValues.put(dataColumn.name(), value);
					} else if(field.getType().isAssignableFrom(int.class)) {
						contentValues.put(dataColumn.name(), field.getInt(t));
					} else if(field.getType().isAssignableFrom(long.class)) {
						contentValues.put(dataColumn.name(), field.getLong(t));
					} else if(field.getType().isAssignableFrom(short.class)) {
						contentValues.put(dataColumn.name(), field.getShort(t));
					} else if(field.getType().isAssignableFrom(boolean.class)) {
						contentValues.put(dataColumn.name(), field.getBoolean(t));
					} else if(field.getType().isAssignableFrom(float.class)) {
						contentValues.put(dataColumn.name(), field.getFloat(t));
					} else if(field.getType().isAssignableFrom(double.class)) {
						contentValues.put(dataColumn.name(), field.getDouble(t));
					} else if(field.getType().isAssignableFrom(Date.class)) {
						Date date = (Date) field.get(t);
						
						if(date != null) {
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(date);
							
							long timeMillis = calendar.getTimeInMillis();
							
							contentValues.put(dataColumn.name(), timeMillis);
						}
					} else if(field.getType().getSuperclass() != null &&
							field.getType().getSuperclass().isAssignableFrom(BaseModel.class) &&
							dataColumn.isPrimary() && !dataColumn.isForeign()) {
						BaseModel baseModel = (BaseModel) field.get(t);
						if(baseModel != null) {
							contentValues.put(dataColumn.name(), baseModel.getId());
						}
					}
				}
			}
			
			return contentValues;
		}
	}
	
	private class DataSourceReadClass implements DataSourceRead {
		private String mQuery;
		private String[] mQueryArgs;
		private String mSortField = "_id";
		private String mSortOrder = SORT_ASC;
		private int mLimit;
		
		@Override
		public DataSourceRead query(String query, String... args) {
			mQuery = null;
			mQueryArgs = null;
			mSortField = "_id";
			mSortOrder = SORT_ASC;
			mLimit = 0;
			mQuery = query;
			mQueryArgs = args;
			return this;
		}
		
		@Override
		public DataSourceRead orderBy(String field, String sort) {
			mSortField = field;
			mSortOrder = sort;
			return this;
		}

		@Override
		public DataSourceRead limit(int limit) {
			mLimit = limit;
			return this;
		}
		
		private <T extends BaseModel> SalutronObjectList<T> getResults(Class<? extends T> cls, boolean includeForeign, String query, String... args)  {
			SalutronObjectList<T> objectList = new SalutronObjectList<T>();
			String tableName = tableNameFromModel(cls);
			
			if (mSQLiteRead == null || !mSQLiteRead.isOpen())
				mSQLiteRead = mDatabaseHelper.getReadableDatabase();
			
			Cursor cursor = null;
			
			if(mLimit > 0) {
				cursor = mSQLiteRead.query(tableName, null, query, args, null, null, mSortField + " " + mSortOrder, String.valueOf(mLimit));
			} else {
				cursor = mSQLiteRead.query(tableName, null, query, args, null, null, mSortField + " " + mSortOrder);
			}
			
			mQuery = null;
			mQueryArgs = null;
			mSortField = "_id";
			mSortOrder = SORT_ASC;
			mLimit = 0;
			
			try {
				while(cursor.moveToNext()) {
					T t = cls.newInstance();
					t.setContext(mContext);
					Field fieldId = cls.getSuperclass().getDeclaredField("id");
					fieldId.setAccessible(true);
					long id = cursor.getLong(cursor.getColumnIndex("_id"));
					fieldId.set(t, id);
					
					Field[] fields = cls.getDeclaredFields();
					
					for(Field field: fields) {
						Annotation  annotation = field.getAnnotation(DataColumn.class);
						
						if(annotation != null) {
							field.setAccessible(true);
							DataColumn column = (DataColumn) annotation;
							
							if(field.getType().isAssignableFrom(String.class)) {
								field.set(t, cursor.getString(cursor.getColumnIndex(column.name())));
							} else if(field.getType().isAssignableFrom(int.class)) {
								field.set(t, cursor.getInt(cursor.getColumnIndex(column.name())));
							} else if(field.getType().isAssignableFrom(long.class)) {
								field.set(t, cursor.getLong(cursor.getColumnIndex(column.name())));
							} else if(field.getType().isAssignableFrom(short.class)) {
								field.set(t, cursor.getShort(cursor.getColumnIndex(column.name())));
							} else if(field.getType().isAssignableFrom(boolean.class)) {
                                String columnName = column.name();
                                int index = cursor.getColumnIndex(columnName);
								field.set(t, cursor.getInt(cursor.getColumnIndex(columnName)) == 1);
							} else if(field.getType().isAssignableFrom(float.class)) {
								field.set(t, cursor.getFloat(cursor.getColumnIndex(column.name())));
							} else if(field.getType().isAssignableFrom(double.class)) {
								field.set(t, cursor.getDouble(cursor.getColumnIndex(column.name())));
							} else if(field.getType().isAssignableFrom(Date.class)) {
								Date date = new Date(cursor.getLong(cursor.getColumnIndex(column.name())));
								field.set(t, date);
							} else if(field.getType().getSuperclass() != null &&
									field.getType().getSuperclass().isAssignableFrom(BaseModel.class) &&
									column.isForeign() && !column.isPrimary()) {
								@SuppressWarnings("unchecked")
								Class<? extends BaseModel> clsBaseModel = (Class<? extends BaseModel>) field.getType();
								List<BaseModel> results = getResults(clsBaseModel, includeForeign, column.name() + " == ?", new String[] { String.valueOf(id) });
								
								if(results.size() > 0) {
									field.set(t, results.get(0));
								}
							} else if(field.getType().isAssignableFrom(List.class) &&
									column.isForeign() && !column.isPrimary() && includeForeign) {
								List<BaseModel> results = getResults(column.model(), includeForeign, column.name() + " == ?", new String[] { String.valueOf(id) });
								
								if(results.size() > 0) {
									field.set(t, results);
								}
							}
						}
					}
					
					objectList.add(t);
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
			
			return objectList;
		}

		@Override
		public <T extends BaseModel> SalutronObjectList<T> getResults(Class<? extends T> cls) {
			return getResults(cls, false, mQuery, mQueryArgs);
		}
		
		public <T extends BaseModel> SalutronObjectList<T> getResults(Class<? extends T> cls, boolean includeForeign) {
			return getResults(cls, includeForeign, mQuery, mQueryArgs);
		}

		@Override
		public <T extends BaseModel> List<Number> getResults(Class<? extends T> cls, String column) {
			synchronized(LOCK_OBJECT) {
				List<Number> values = new ArrayList<Number>();
				String tableName = tableNameFromModel(cls);
				
				if(mSQLiteRead == null || !mSQLiteRead.isOpen())
					mSQLiteRead = mDatabaseHelper.getReadableDatabase();
				
				Cursor cursor = null;
				
				if(mLimit > 0) {
					cursor = mSQLiteRead.query(tableName, new String[] { column }, mQuery, mQueryArgs, null, null, mSortField + " " + mSortOrder, String.valueOf(mLimit));
				} else {
					cursor = mSQLiteRead.query(tableName, new String[] { column }, mQuery, mQueryArgs, null, null, mSortField + " ");
				}
				
				mQuery = null;
				mQueryArgs = null;
				mSortField = "_id";
				mSortOrder = SORT_ASC;
				mLimit = 0;
				
				while(cursor.moveToNext()) {
					double value = cursor.getDouble(0);
					values.add(value);
				}
				
				cursor.close();
				
				return values;
			}
		}

		@Override
		public Cursor rawQuery(String query, String... selectionArgs) {
			if(mSQLiteRead == null || !mSQLiteRead.isOpen())
				mSQLiteRead = mDatabaseHelper.getReadableDatabase();
			
			Cursor cursor = mSQLiteRead.rawQuery(query, selectionArgs);
			
			return cursor;
		}
		
		@Override
		public Cursor rawQuery(String query) {
			if(mSQLiteRead == null || !mSQLiteRead.isOpen())
				mSQLiteRead = mDatabaseHelper.getReadableDatabase();
			
			Cursor cursor = mSQLiteRead.rawQuery(query, null);
			
			return cursor;
		}



		@Override
		public List<Double> cursorToList(Cursor cursor) {
			List<Double> values = new ArrayList<Double>();
			
			while(cursor.moveToNext()) {
				values.add(cursor.getDouble(0));
			}
			
			return values;
		}

		@Override
		public <T extends BaseModel> Cursor getResults(Class<? extends T> cls, String[] columns) {
			String tableName = tableNameFromModel(cls);
			
			if(mSQLiteRead == null || !mSQLiteRead.isOpen())
				mSQLiteRead = mDatabaseHelper.getReadableDatabase();
			
			Cursor cursor = null;
			
			if(mLimit > 0) {
				cursor = mSQLiteRead.query(tableName, null, mQuery, mQueryArgs, null, null, mSortField + " " + mSortOrder, String.valueOf(mLimit));
			} else {
				cursor = mSQLiteRead.query(tableName, null, mQuery, mQueryArgs, null, null, mSortField + " " + mSortOrder);
			}
			
			return cursor;
		}
	}
	
	private String tableNameFromModel(Class<? extends BaseModel> cls) {
		Annotation annotation = cls.getAnnotation(DataTable.class);
		
		if(annotation != null) {
			DataTable dataTable = (DataTable) annotation;
			return dataTable.name();
		}
		return null;
	}
}
