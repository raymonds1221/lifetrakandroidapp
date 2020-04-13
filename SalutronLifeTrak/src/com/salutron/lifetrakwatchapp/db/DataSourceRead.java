package com.salutron.lifetrakwatchapp.db;

import java.util.List;

import android.database.Cursor;

import com.salutron.lifetrakwatchapp.model.BaseModel;
import com.salutron.lifetrakwatchapp.util.SalutronObjectList;

/**
 * DataSourceRead
 * 
 * @author rsarmiento
 *
 */
public interface DataSourceRead {
	/**
	 * Method for adding query
	 * 
	 * @param query		The query you want to execute
	 * @param args		The arguments of the query
	 * @return			Returns (DataSourceRead) interface
	 */
	public DataSourceRead query(String query, String... args);
	/**
	 * Method for sorting records
	 * 
	 * @param field		The field to sort
	 * @param sort		The ordering sequence of the records
	 * @return			Returns (DataSourceRead) interface
	 */
	public DataSourceRead orderBy(String field, String sort);
	/**
	 * Method for limiting the number of records
	 * 
	 * @param limit		The number of records to limit
	 * @return			Returns (DataSourceRead) interface
	 */
	public DataSourceRead limit(int limit);
	/**
	 * Method for getting all results
	 * 
	 * @param cls	The class to be return
	 * @return		Returns (SalutronObjectList<T>) object
	 */
	public <T extends BaseModel> SalutronObjectList<T> getResults(Class<? extends T> cls);
	/**
	 * Method for getting all results
	 * 
	 * @param cls 		     The class to be return
	 * @param includeForeign TRUE to include child models
	 * @return
	 */
	public <T extends BaseModel> SalutronObjectList<T> getResults(Class<? extends T> cls, boolean includeForeign);
	/**
	 * Method for getting results with a specific column
	 * 
	 * @param cls			 The class to search
	 * @param field			 The return field
	 * @return				 Returns (List<Number>) object
	 */
	public <T extends BaseModel> List<Number> getResults(Class<? extends T> cls, String column);
	/**
	 * Method for executing raw sql query
	 * @param query				The sql query to execute
	 * @return selectionArgs	The statements to pass
	 */
	public Cursor rawQuery(String query, String... selectionArgs);
	/**
	 * Method for executing raw sql query
	 * @param query				The sql query to execute
	 */
	public Cursor rawQuery(String query);
	/**
	 * Method for getting results with columns
	 * 
	 * @param cls			The class for getting the table name
	 * @param columns		The column to select
	 * @return
	 */
	public <T extends BaseModel> Cursor getResults(Class<? extends T> cls, String[] columns);
	/**
	 * Method for converting cursor to list
	 * 
	 * @param cursor The cursor to convert
	 * @return	 	 Returns (List<Double>) object
	 */
	public List<Double> cursorToList(Cursor cursor);
}
