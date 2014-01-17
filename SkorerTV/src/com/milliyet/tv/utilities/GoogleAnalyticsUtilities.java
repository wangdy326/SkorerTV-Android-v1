package com.milliyet.tv.utilities;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class GoogleAnalyticsUtilities
{
	/***************************************
	 * Singleton reference
	 */
	private static GoogleAnalyticsUtilities sharedInstance = null;
	
	
	/***************************************
	 * Trackers
	 */
	private static final String TRACKER_ID_ANDROID = "UA-15581378-24";
	private static final String TRACKER_ID_UNIVERSAL = "UA-15581378-26";
	
	private static final String INSTANCESTATE_ACTIVITYSTATETRACKER_KEY = "googleanalytics.activitystatetracker";
	
	private Tracker androidTracker = null;
	private Tracker universalTracker = null;
	
	private Map<String, Boolean> activityStateTracker = new HashMap<String, Boolean>();
	
	/***************************************
	 * Constructors & instance providers
	 */
	private GoogleAnalyticsUtilities(Context context)
	{
		/*No public constructor*/
		
		if(context != null)
		{
			this.androidTracker = GoogleAnalytics.getInstance(context).getTracker(TRACKER_ID_ANDROID);
			this.universalTracker = GoogleAnalytics.getInstance(context).getTracker(TRACKER_ID_UNIVERSAL);
		}
	}

	public static synchronized GoogleAnalyticsUtilities sharedInstance(Context context)
	{
		if(GoogleAnalyticsUtilities.sharedInstance == null)
		{
			GoogleAnalyticsUtilities.sharedInstance = new GoogleAnalyticsUtilities(context);
			
			// When dry run is set, hits will not be dispatched, but will still be logged as
			// though they were dispatched.
			// Enable below line for debug purposes!
//			GoogleAnalytics.getInstance(context).setDryRun(true);
		}
		
		return GoogleAnalyticsUtilities.sharedInstance;
	}
	
	/***************************************
	 * Analytics
	 */
	
	public void onCreate(final String activityTrackLabel, final Bundle savedInstanceState)
	{
		if(!TextUtils.isEmpty(activityTrackLabel))
		{
			// Activity making a fresh start
			if(savedInstanceState == null)
			{
				// Mark activity as tracked before (false).
				this.activityStateTracker.put(activityTrackLabel, false);
			}
			else
			{
				boolean freshStart = savedInstanceState.getBoolean(INSTANCESTATE_ACTIVITYSTATETRACKER_KEY, true);
				
				this.activityStateTracker.put(activityTrackLabel, freshStart);
			}
		}
	}
	
	public void onSaveInstanceState(final Bundle outState)
	{
		if(outState != null)
		{
			outState.putBoolean(INSTANCESTATE_ACTIVITYSTATETRACKER_KEY, true);
		}
	}
	
	public void onStart(final String activityTrackLabel)
	{
		if(isMakingFreshStart(activityTrackLabel))
		{
			sendActivityStart(activityTrackLabel);
		}
	}
	
	private boolean isMakingFreshStart(final String trackLabel)
	{
		Boolean freshStart = true;
		
		if(!TextUtils.isEmpty(trackLabel))
		{
			freshStart = activityStateTracker.get(trackLabel);
			if(freshStart == null) freshStart = true;
		}
		
		return freshStart;
	}
	
	private void sendActivityStart(final String activityTrackLabel)
	{
		if(androidTracker != null) sendActivityStart(activityTrackLabel, androidTracker);
		if(universalTracker != null) sendActivityStart(activityTrackLabel, universalTracker);
	}
	
	private void sendActivityStart(final String activityTrackLabel, final Tracker tracker)
	{
		if(tracker != null)
		{
			tracker.send(MapBuilder.createAppView().set(Fields.SCREEN_NAME, activityTrackLabel).build());
		}
	}
}
