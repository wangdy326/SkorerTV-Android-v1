package com.skorer.tv;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
	public static class Table
	{
		public static class VideoClip
		{
			public static final String COLUMN_ID = "_id";
			public static final String COLUMN_CODE = "code";
			public static final String COLUMN_TITLE = "title";
			public static final String COLUMN_IMAGEURL = "imageurl";
			public static final String COLUMN_THUMBURL = "thumburl";
			public static final String COLUMN_LINK = "link";
			public static final String COLUMN_CATEGORY = "category";
			public static final String COLUMN_CATEGORYID = "categoryid";
			public static final String COLUMN_SPOT = "spot";
			public static final String COLUMN_VIDEOURL = "videourl";
			public static final String COLUMN_PUBLISHTIME = "publishtime";
			public static final String COLUMN_VIEWCOUNT = "viewcount";
			public static final String COLUMN_COMMENTCOUNT = "commentcount";
			public static final String COLUMN_POSITIVEVOTECOUNT = "positivevotecount";
			public static final String COLUMN_NEGATIVEVOTECOUNT = "negativevotecount";
			
//			public static String getTableName(String categoryIdentifier)
//			{
//				// Return database safe generated table name
//				return ("videoclips" + categoryIdentifier).replace('.', '_');
//			}
		}
	}
	
	public static final String DATABASE_NAME = "milliyettv_db";
	private static final int DATABASE_VERSION = 1;
	
	private String tableName = "";
	
	public DatabaseHelper(Context context, String categoryIdentifier)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		this.tableName = generateSQLiteTableSafeString(categoryIdentifier);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(getTableCreatorQuery(this.tableName));
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w(DatabaseHelper.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		
		// Clear the current database
		db.execSQL("DROP TABLE IF EXISTS " + this.tableName);
		
		// Re-direct to onCreate for mint-fresh initialization
		onCreate(db);
	}
	
	private static String getTableCreatorQuery(String tableName)
	{
		return "create table if not exists " + tableName + 
				"(" + Table.VideoClip.COLUMN_ID + " text primary key, " 
				+ Table.VideoClip.COLUMN_CODE + " text, "
				+ Table.VideoClip.COLUMN_TITLE + " text, "
				+ Table.VideoClip.COLUMN_IMAGEURL + " text, "
				+ Table.VideoClip.COLUMN_THUMBURL + " text, "
				+ Table.VideoClip.COLUMN_LINK + " text, "
				+ Table.VideoClip.COLUMN_CATEGORY + " text, "
				+ Table.VideoClip.COLUMN_CATEGORYID + " text, "
				+ Table.VideoClip.COLUMN_SPOT + " text, "
				+ Table.VideoClip.COLUMN_VIDEOURL + " text, "
				+ Table.VideoClip.COLUMN_PUBLISHTIME + " unsigned big int, "
				+ Table.VideoClip.COLUMN_VIEWCOUNT + " int, "
				+ Table.VideoClip.COLUMN_COMMENTCOUNT + " int, "
				+ Table.VideoClip.COLUMN_POSITIVEVOTECOUNT + " int, "
				+ Table.VideoClip.COLUMN_NEGATIVEVOTECOUNT + " int)";
	}
	
	public static boolean isTableExists(final SQLiteDatabase db, String tableName)
	{
	    if (tableName == null || db == null || !db.isOpen())
	    {
	        return false;
	    }
	    
	    tableName = escapedSQLiteTableString(tableName);
	    
	    Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
	    if (!cursor.moveToFirst())
	    {
	        return false;
	    }
	    int count = cursor.getInt(0);
	    cursor.close();
	    return count > 0;
	}
	
	public static String generateSQLiteTableSafeString(String string)
	{
		return (TextUtils.isEmpty(string))?("_empty_dbWt5MDOrR"):("[" + string + "]");
	}
	
	public static String escapedSQLiteTableString(String string)
	{
		StringBuilder builder = new StringBuilder(string);
		
		// Search for start safety character
		int startSafetyIndex = builder.indexOf("[");
		// Search for end safety character
		int endSafetyIndex = builder.lastIndexOf("]");
		
		// If valid safety applied
		if(startSafetyIndex != -1 && endSafetyIndex != -1)
		{
			// Remove safety characters
			builder.deleteCharAt(startSafetyIndex);
			builder.deleteCharAt(endSafetyIndex - 1);
		}
		
		return builder.toString();
	}
	
	public String getTableName()
	{
		return this.tableName;
	}
}
