package com.milliyet.tv.utilities;

import java.io.File;

import android.os.Environment;
import android.text.TextUtils;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class FileUtilities
{
	private static FileUtilities sharedInstance = null;
	
	public static FileUtilities sharedInstance()
	{
		if(sharedInstance == null)
		{
			sharedInstance = new FileUtilities();
		}
		
		return sharedInstance;
	}
	
	public boolean createParentDirectories(File file)
	{
		boolean succeed = false;
		
		if(file != null)
		{
			File parent = file.getParentFile();
			
			succeed = createDirectoryPath(parent);
		}
		
		return succeed;
	}
	
	public boolean createDirectoryPath(File directory)
	{
		boolean succeed = false;
		
		if(directory != null)
		{	
			// If parent directory path created before
			if(directory.exists() && directory.isDirectory())
			{
				succeed = true;
			}
			// Else if, creation of new folders succeed
			else if(directory.mkdirs())
			{
				succeed = true;
			}
			else
			{
				// We failed
			}
		}
		
		return succeed;
	}
	
	public synchronized boolean renameFile(String filePath, String newName)
	{
		boolean succeed = false;
		File from = new File(filePath);
		
		if(from.exists())
		{
			String extension = getFileExtension(from);
			String toFileName = newName + extension;
			String toFilePath = from.getParent();
			
//			File to = new File(toFilePath + toFileName);
			File to = new File(toFilePath, toFileName);
			
			succeed = from.renameTo(to);
		}
		
		return succeed;
	}
	
	public synchronized String getFileExtension(File file)
	{
		String extension = "";
		
		if(file != null)
		{
			String fileName = file.getName();
			
			if(!TextUtils.isEmpty(fileName))
			{
				int extensionStartIndex = fileName.lastIndexOf('.');
				
				// If (we might assume) extension found
				if(extensionStartIndex > 0)
				{
					extension = fileName.substring(extensionStartIndex);
				}
			}
		}
		
		return extension;
	}
	
	public synchronized String stripExtension(String fileName)
	{
		String name = "";
		
		if(!TextUtils.isEmpty(fileName))
		{
			int extensionIndex = fileName.lastIndexOf('.');
			
			if(extensionIndex > 0)
			{
				name = fileName.substring(0, extensionIndex);
			}
		}
		
		return name;
	}
	
	public boolean isExternalStorageWritable()
	{
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		
		return mExternalStorageWriteable;
	}
	
	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
}
