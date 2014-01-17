package com.milliyet.tv.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.skorer.tv.SkorerTVApplication;
import com.skorer.tv.listeners.OnJobDoneListener;
import com.skorer.tv.listeners.OnJobDoneListener.JobStatus;
import com.skorer.tv.model.VideoClip;

public class FavouritesUtilities
{
	/***************************************
	 * Log
	 */
	public static final boolean LOG = true;
	public static final String TAG = "Favourites";
	
	/***************************************
	 * Singleton reference
	 */
	private static FavouritesUtilities sharedInstance = null;
	
	private static final String PREFERENCES_FAVOURITES_NAME = "videolistutilities.favourites";
	
	/***************************************
	 * Constructors & instance providers
	 */
	private FavouritesUtilities(){ /*No public constructor*/ }

	public static synchronized FavouritesUtilities sharedInstance()
	{
		if(FavouritesUtilities.sharedInstance == null)
		{
			FavouritesUtilities.sharedInstance = new FavouritesUtilities();
		}
		
		return FavouritesUtilities.sharedInstance;
	}
	
	/****************************************
	 * Utilities
	 */
	
	private SharedPreferences getFavouritesStorage(final Context context)
	{
		SharedPreferences preferences = null;
		
		if(context != null)
		{
			preferences = context.getSharedPreferences(PREFERENCES_FAVOURITES_NAME, Context.MODE_PRIVATE);
		}
		
		return preferences;
	}
	
	public Set<String> getFavouriteIds(final Context context)
	{
		synchronized (this)
		{
			Set<String> set = null;
			
			SharedPreferences preferences = getFavouritesStorage(context);
			
			if(preferences != null)
			{
				set = preferences.getAll().keySet();
			}
			
			return set;
		}
	}
	
	public boolean addFavourite(final Context context, final String videoId)
	{
		synchronized (this)
		{
			boolean success = false;
			
			SharedPreferences preferences = getFavouritesStorage(context);
			
			if(preferences != null && !TextUtils.isEmpty(videoId))
			{
				success = preferences.edit().putString(videoId, videoId).commit();
			}
			
			return success;
		}
	}
	
	public boolean removeFavourite(final Context context, final String videoId)
	{
		synchronized (this)
		{
			boolean success = false;
			
			SharedPreferences preferences = getFavouritesStorage(context);
			
			if(preferences != null && !TextUtils.isEmpty(videoId))
			{
				success = preferences.edit().remove(videoId).commit();
			}
			
			return success;
		}
	}
	
	public boolean containsVideoId(final Context context, final String videoId)
	{
		synchronized (this)
		{
			boolean contains = false;
			
			SharedPreferences preferences = getFavouritesStorage(context);
			
			if(preferences != null && !TextUtils.isEmpty(videoId))
			{
				contains = preferences.contains(videoId);
			}
			
			return contains;
		}
	}
	
	public synchronized void fetchFavouriteVideos(final Context context, final OnJobDoneListener<List<VideoClip>> asynchGetter)
	{
		if(asynchGetter != null)
		{
			final Set<String> favouriteIdsSet = getFavouriteIds(context);
			final List<VideoClip> harvestedItems = new ArrayList<VideoClip>(favouriteIdsSet.size());
			final Counter counter = new Counter();
			
			final OnJobDoneListener<VideoClip> videoCollector = new OnJobDoneListener<VideoClip>()
			{
				@Override
				public void onJobDone(int status, VideoClip result)
				{
					synchronized (harvestedItems)
					{
						// If we are able to harvest new item
						if(result != null)
						{
							// Attach to harvested items
							harvestedItems.add(result);
						}
						
						// Increment counter
						counter.increment();
						
						Log.d(TAG, "Harvest attempt #" + counter.getCount() + "is completed for " + result);
						
						// If our attempt to harvest all items is done
						if(favouriteIdsSet.size() == counter.getCount())
						{
							asynchGetter.completeJobAsynch(JobStatus.SUCCEED, harvestedItems);
						}
					}
				}
			};
			
			if(favouriteIdsSet != null && favouriteIdsSet.size() > 0)
			{
				for (String videoId : favouriteIdsSet)
				{
					Log.d(TAG, "Will harvest: " + videoId);
					
					VideoClipUtilities.sharedInstance().fetchVideoClipAsynch(videoId, SkorerTVApplication.requestQueue, new OnJobDoneListener<VideoClip>()
					{
						@Override
						public void onJobDone(int status, VideoClip result)
						{
							videoCollector.completeJobAsynch(status, result);
						}
					});
				}
			}
			else
			{
				asynchGetter.completeJobAsynch(JobStatus.FAILED);
			}
		}
	}
	
	class Counter
	{
		private int count = 0;
		
		void increment()
		{
			count++;
		}
		
		int getCount()
		{
			return count;
		}
	}
}
