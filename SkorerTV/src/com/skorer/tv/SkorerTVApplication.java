package com.skorer.tv;

import android.app.Application;
import android.util.DisplayMetrics;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class SkorerTVApplication extends Application
{
	public static RequestQueue requestQueue = null;
	public static ImageLoader imageLoader = null;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		// Allocate & initialize singleton network request queue for this application
		SkorerTVApplication.requestQueue = Volley.newRequestQueue(getApplicationContext());
		// Allocate & initialize singleton image cache for this application
		SkorerTVApplication.imageLoader = new ImageLoader(requestQueue, getImageCache());
		
		// Clear pre-cached data as it probably is expired
		VideoClipDataSource.clearCache(getApplicationContext());
	}
	
	private ImageCache getImageCache()
	{
		return new LruBitmapCache(getCacheSize());
	}
	
	// Guests optimal cache size for device
	public int getCacheSize()
	{
	    final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	    final int screenWidth = displayMetrics.widthPixels;
	    final int screenHeight = displayMetrics.heightPixels;
	    final int screenBytes = screenWidth * screenHeight * 4; // 4 bytes per pixel (ARGB)

	    return screenBytes * 3;
	}
}
