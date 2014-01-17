package com.skorer.tv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.skorer.tv.model.VideoClip;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class VideoClipDataSource
{
	// Database fields
	private SQLiteDatabase database = null;
	private DatabaseHelper databaseHelper = null;
	
	private String[] allColumns = { DatabaseHelper.Table.VideoClip.COLUMN_ID, 
									DatabaseHelper.Table.VideoClip.COLUMN_CODE,
									DatabaseHelper.Table.VideoClip.COLUMN_TITLE,
									DatabaseHelper.Table.VideoClip.COLUMN_IMAGEURL,
									DatabaseHelper.Table.VideoClip.COLUMN_THUMBURL,
									DatabaseHelper.Table.VideoClip.COLUMN_LINK,
									DatabaseHelper.Table.VideoClip.COLUMN_CATEGORY,
									DatabaseHelper.Table.VideoClip.COLUMN_CATEGORYID,
									DatabaseHelper.Table.VideoClip.COLUMN_SPOT,
									DatabaseHelper.Table.VideoClip.COLUMN_VIDEOURL,
									DatabaseHelper.Table.VideoClip.COLUMN_PUBLISHTIME,
									DatabaseHelper.Table.VideoClip.COLUMN_VIEWCOUNT,
									DatabaseHelper.Table.VideoClip.COLUMN_COMMENTCOUNT,
									DatabaseHelper.Table.VideoClip.COLUMN_POSITIVEVOTECOUNT,
									DatabaseHelper.Table.VideoClip.COLUMN_NEGATIVEVOTECOUNT };
	
	public VideoClipDataSource(Context context, String categoryIdentifier)
	{
		databaseHelper = new DatabaseHelper(context, categoryIdentifier);
	}
	
	public void open() throws SQLException
	{
		synchronized (this)
		{
			// If we have already initialized database instance
			if(database != null)
			{
				// If database is open (usable)
				if(database.isOpen())
				{
					// Use existing database
				}
				else
				{
					// Close current database as it contains dead connection,
					// New connection will be established @below
					close();
					
					database = null;
				}
			}
			
			// If not initialized before, create new database connection
			if(database == null && databaseHelper != null)
			{	
				// Received databaseHelper null once, no idea why.
				database = databaseHelper.getWritableDatabase();
				
				// Use a dirty trick here
				databaseHelper.onCreate(database);
			}
		}
	}
	
	public void close()
	{
		synchronized (this)
		{
			if(database != null)
			{
				database.close();
			}
			
			database = null;
		}
	}
	
	public synchronized void insertVideoClip(VideoClip videoClip)
	{
		// Open database, if closed (This is a re-check)
		open();
		
		// Parse VideoClip instance to database compatible object
		ContentValues values = new ContentValues();
		
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_ID, videoClip.getId());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_CODE, videoClip.getCode());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_TITLE, videoClip.getTitle());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_IMAGEURL, videoClip.getImageUrl());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_THUMBURL, videoClip.getThumbUrl());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_LINK, videoClip.getLink());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_CATEGORY, videoClip.getCategory());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_CATEGORYID, videoClip.getCategoryId());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_SPOT, videoClip.getSpot());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_VIDEOURL, videoClip.getVideoUrl());
		long publishTime = (videoClip.getPublishTime() == null)?(0):(videoClip.getPublishTime().getTime());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_PUBLISHTIME, publishTime);
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_VIEWCOUNT, videoClip.getViewCount());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_COMMENTCOUNT, videoClip.getCommentCount());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_POSITIVEVOTECOUNT, videoClip.getPositiveVoteCount());
		values.put(DatabaseHelper.Table.VideoClip.COLUMN_NEGATIVEVOTECOUNT, videoClip.getNegativeVoteCount());
		
		// Insert as new video clip record
		//database.insert(DatabaseHelper.Table.VideoClip.getTableName(databaseHelper.getCategoryIdentifier()), null, values);
		database.insertWithOnConflict(databaseHelper.getTableName(), null, values, SQLiteDatabase.CONFLICT_IGNORE);
	}
	
	public synchronized void deleteVideoClip(VideoClip videoClip)
	{
		if(videoClip != null)
		{
			// Open database, if closed (This is a re-check)
			open();
			
			database.delete(databaseHelper.getTableName(), DatabaseHelper.Table.VideoClip.COLUMN_ID + " = " + videoClip.getId(), null);
		}
	}
	
	public synchronized void deleteAllVideoClips()
	{
		// Open database, if closed (This is a re-check)
		open();
		
		database.delete(databaseHelper.getTableName(), "1", null);
	}
	
	public Cursor getVideoClipCursor()
	{
		// Open database, if closed (This is a re-check)
		open();
		
		String orderBy =  DatabaseHelper.Table.VideoClip.COLUMN_ID + " DESC";
		return database.query(databaseHelper.getTableName(), allColumns, null, null, null, null, orderBy);
	}
	
	public int getVideoClipQuantity()
	{
		int quantity = 0;
		
		// Open database, if closed (This is a re-check)
		open();
		
		// Generate table name
		String tableName = databaseHelper.getTableName();
		
		try
		{
			if(DatabaseHelper.isTableExists(database, tableName))
			{
				Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
				
				if(cursor.moveToFirst())
				{
					quantity = cursor.getInt(cursor.getColumnIndex("COUNT(*)"));
				}
				
				// Release artifact resources
				// cursor.close();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return quantity;
	}
	
	public static void clearCache(Context context)
	{
		if(context != null)
		{
			context.deleteDatabase(DatabaseHelper.DATABASE_NAME);
		}
	}
}
