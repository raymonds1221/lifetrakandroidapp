package com.salutron.lifetrakwatchapp.db;

import java.util.List;

import com.salutron.lifetrakwatchapp.model.BaseModel;

/**
 * DataSourceWrite
 * 
 * @author rsarmiento
 *
 */
public interface DataSourceWrite {
	/**
	 * Method to open the database for write
	 * 
	 * @return Returns (DataSourceWrite) interface
	 */
	public DataSourceWrite open();

	public DataSourceWrite execQuery(String query);
	/**
	 * Method to begin the transaction
	 * 
	 * @return Returns (DataSourceWrite) interface
	 */
	public DataSourceWrite beginTransaction();
	/**
	 * Method for updating Model object
	 * 
	 * @param t The type of the model
	 */
	public <T extends BaseModel> DataSourceWrite update(T t);
	/**
	 * Method for inserting Model object
	 * 
	 * @param t The type of the model
	 * @return	Returns (DataSourceWrite) interface
	 */
	public <T extends BaseModel> DataSourceWrite insert(T t);
	/**
	 * Method for inserting Model Lists
	 * 
	 * @param t the List to be inserted in bulk
	 * @return Returns (DataSourceWrite) interface
	 */
	public <T extends BaseModel> DataSourceWrite insert(List<T> l);
	/**
	 * Mdthod for deleting Model object
	 * 
	 * @param t The type of the model
	 * @return	Returns (DataSourceWrite) interface
	 */
	public <T extends BaseModel> DataSourceWrite delete(T t);

	public <T extends BaseModel> DataSourceWrite deleteAll(T t);
	/**
	 * Method for ending the transaction
	 * 
	 * @return Returns (DataSourceWrite) interface
	 */
	public DataSourceWrite endTransaction();
	/**
	 * Method to close the writable database
	 */
	public void close();
}