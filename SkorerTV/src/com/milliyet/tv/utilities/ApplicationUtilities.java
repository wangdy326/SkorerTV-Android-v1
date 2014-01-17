package com.milliyet.tv.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public final class ApplicationUtilities
{
	private static final String PUSHAD_APPLICATIONID_KEY = "pushad.applicationid";
	
	/***************************************
	 * Singleton reference
	 */
	private static ApplicationUtilities sharedInstance = null;
	
	/***************************************
	 * Constructors & instance providers
	 */
	private ApplicationUtilities(){/*No public constructor*/}

	public static ApplicationUtilities sharedInstance()
	{
		if(ApplicationUtilities.sharedInstance == null)
		{
			ApplicationUtilities.sharedInstance = new ApplicationUtilities();
		}
		
		return ApplicationUtilities.sharedInstance;
	}
	
	/**
	 * @return Application's name from the {@code PackageManager}.
	 */
	public String getApplicationName(Context context)
	{
		String applicationName = null;

		if (context != null)
		{
			int stringId = context.getApplicationInfo().labelRes;
			applicationName = context.getString(stringId);
		}

		return applicationName;
	}

	/**
	 * @return Application's version name from the {@code PackageManager}.
	 */
	public String getApplicationVersionName(Context context)
	{
		String applicationVersion = null;

		if (context != null)
		{
			PackageInfo packageInfo = null;
			
			try
			{
				packageInfo = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0);
				applicationVersion = packageInfo.versionName;
			}
			catch (NameNotFoundException e) {}
		}

		return applicationVersion;
	}
	
	/**
	 * @return Application's version number from the {@code PackageManager}.
	 */
	public int getApplicationVersionNumber(Context context)
	{
		int applicationVersion = 0;

		if (context != null)
		{
			PackageInfo packageInfo = null;
			
			try
			{
				packageInfo = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0);
				applicationVersion = packageInfo.versionCode;
			}
			catch (NameNotFoundException e) {}
		}

		return applicationVersion;
	}
	
	/**
	 * This method converts dp unit to equivalent pixels, depending on device density. 
	 * 
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public float convertDpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}

	/**
	 * This method converts device specific pixels to density independent pixels.
	 * 
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}
	
	public boolean isActivityAlive(Activity activity)
	{
		return ((activity != null) && (!activity.isFinishing()));
	}
}
