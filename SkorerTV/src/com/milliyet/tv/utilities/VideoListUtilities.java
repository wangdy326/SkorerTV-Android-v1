package com.milliyet.tv.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.skorer.tv.VideoClipDataSource;
import com.skorer.tv.listeners.OnJobDoneListener;
import com.skorer.tv.listeners.OnJobDoneListener.JobStatus;
import com.skorer.tv.model.Category;
import com.skorer.tv.model.VideoClip;
import com.skorer.tv.requests.VideoClipListRequest;
import com.skorer.tv.requests.VideoClipRelatedRequest;
import com.skorer.tv.requests.VideoListCountRequest;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class VideoListUtilities
{
	class Status
	{
		public static final int IDLE = 0;
		public static final int BUSY = 1;
	}
	
	public static final String VIDEOLIST_HOMEPAGE = "videoutilities.videolist.homepage";
	public static final String VIDEOLIST_HEADLINE = "videoutilities.videolist.headline";
	public static final String VIDEOLIST_SEARCH = "videoutilities.videolist.search";
	public static final String VIDEOLIST_FAVOURITES = "videoutilities.videolist.favourites";
	
	
	/***************************************
	 * Log
	 */
	public static final boolean LOG = true;
	public static final String TAG = "VideoListUtilities";
	
	
	/***************************************
	 * Singleton reference map
	 */
	
	private static final int SHAREDINSTANCE_SEARCH_MAX = 20;
	
	private static Map<String, VideoListUtilities> sharedInstanceMap = new HashMap<String, VideoListUtilities>();
	// Search instances contained in map with limited size as they have huge possibility to expand
	// and we don't want to bloat memory
	private static Map<String, VideoListUtilities> sharedSearchInstanceMap = new LinkedHashMap<String, VideoListUtilities>()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -4174549705920288627L;

		@Override
	    protected boolean removeEldestEntry(Map.Entry<String, VideoListUtilities> eldest)
	    {
			boolean remove = this.size() > SHAREDINSTANCE_SEARCH_MAX;
			
			// ???: Clear search result cache too?..
					
			return remove;
	    }
	};
	
	
	/***************************************
	 * Variables
	 */
	
	private VideoClipDataSource dataSource = null;
	private int videoClipRequestStatus = Status.IDLE;
	private List<OnJobDoneListener<Cursor>> asynchGetterStack = new ArrayList<OnJobDoneListener<Cursor>>();
	
	private Category category = null;
	
	
	/***************************************
	 * Constructors & instance providers
	 */
	private VideoListUtilities(Context context, Category category)
	{
		/*No public constructor*/
		
		String categoryIdentifier = null;
		
		if(VIDEOLIST_SEARCH.equalsIgnoreCase(category.getId()))
		{
			categoryIdentifier = category.getId() + category.getName();
		}
		else
		{
			categoryIdentifier = category.getId();
		}
		
		this.category = category;
		this.dataSource = new VideoClipDataSource(context, categoryIdentifier);
	}

	public static synchronized VideoListUtilities sharedInstance(Context context, Category category)
	{
		VideoListUtilities sharedInstance = null;
		
		if(category == null || TextUtils.isEmpty(category.getId()))
		{
			if(LOG)
			{
				Log.e(TAG, "Perhaps you forgot providing non-empty category identifier?");
			}
		}
		else
		{
			String categoryId = category.getId();
			
			if(VIDEOLIST_SEARCH.equalsIgnoreCase(categoryId))
			{
				// Get previously initialized shared instance for given identifier
				sharedInstance = sharedSearchInstanceMap.get(category.getName());
				
				// If sharedInstance not initialized before or removed from cache
				if(sharedInstance == null)
				{
					// Allocate & initialize new video utilities
					sharedInstance = new VideoListUtilities(context, category);
					
					// Save utilities for later use
					sharedSearchInstanceMap.put(category.getName(), sharedInstance);
				}
			}
			else
			{
				// Get previously initialized shared instance for given identifier
				sharedInstance = sharedInstanceMap.get(categoryId);
				
				// If sharedInstance not initialized before
				if(sharedInstance == null)
				{
					// Allocate & initialize new video utilities
					sharedInstance = new VideoListUtilities(context, category);
					
					// Save utilities for later use
					sharedInstanceMap.put(categoryId, sharedInstance);
				}
			}
		}
		
		return sharedInstance;
	}

		
	/****************************************
	 * ???
	 */
	
	public synchronized void fetchVideoClipCountAsynch(final RequestQueue requestQueue, final OnJobDoneListener<Integer> asynchGetter)
	{
		Listener<JSONObject> requestListener = new Listener<JSONObject>()
		{
			@Override
			public void onResponse(JSONObject response)
			{
				if(response != null)
				{
					JSONArray countJSONArray = JSONUtilities.getJsonArray(response, "root", null);
					
					if(countJSONArray != null)
					{
						JSONObject countJSONObject = JSONUtilities.getArrayObject(countJSONArray, 0, null);
						
						if(countJSONObject != null)
						{
							int count = JSONUtilities.getJsonInteger(countJSONObject, "TotalCount", Integer.MIN_VALUE);
							
							asynchGetter.completeJobAsynch(JobStatus.SUCCEED, count);
						}
						else
						{
							asynchGetter.completeJobAsynch(JobStatus.FAILED, Integer.MIN_VALUE);
						}
					}
					else
					{
						asynchGetter.completeJobAsynch(JobStatus.FAILED, Integer.MIN_VALUE);
					}
				}
				else
				{
					asynchGetter.completeJobAsynch(JobStatus.FAILED, Integer.MIN_VALUE);
				}
			}
		};
		
		ErrorListener requestErrorListener = new ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				asynchGetter.completeJobAsynch(JobStatus.FAILED, Integer.MIN_VALUE);
			}
		};
		
		String url = null;
		
		if(VIDEOLIST_SEARCH.equalsIgnoreCase(category.getId()))
		{
			url = VideoListCountRequest.getSearchCountRequestUrl(category.getName());
		}
		else
		{
			url = VideoListCountRequest.getCountRequestUrl(category.getId());
		}
		
		VideoListCountRequest request = VideoListCountRequest.newInstance(url, requestListener, requestErrorListener);
		
		requestQueue.add(request);
	}
	
	public static void fetchRelatedVideoClipsAsynch(final String videoId, final RequestQueue requestQueue, final OnJobDoneListener<List<VideoClip>> asynchGetter)
	{
		VideoClipRelatedRequest request = VideoClipRelatedRequest.newInstance(videoId, new Response.Listener<JSONObject>()
		{
			@Override
			public void onResponse(JSONObject response)
			{
				// Received JSON object response
				// Now, we have to parse and drain waiting asynchronous getter
				
				int jobStatus = JobStatus.FAILED;
				List<VideoClip> videoClipList = null;
					
				if(response != null)
				{
					JSONArray videoClipJSONArray = JSONUtilities.getJsonArray(response, "root", null);
					
					if(videoClipJSONArray != null)
					{
						// We may assume we retrieved the category list successfully as JSONArray instance
						// Now we have to parse JSONArray to list
						
						// Fetch video clip quantity within JSONArray
						int quantity = videoClipJSONArray.length();
							
						// Mark status as succeed
						jobStatus = JobStatus.SUCCEED;
						
						// Initialize new category container list
						videoClipList = new ArrayList<VideoClip>(quantity);
						
						for (int i = 0; i < quantity; i++)
						{
							JSONObject videoClipJSONObject = JSONUtilities.getArrayObject(videoClipJSONArray, i, null);
							
							if(videoClipJSONObject != null)
							{
								VideoClip videoClip = new VideoClip(videoClipJSONObject);
								videoClipList.add(videoClip);
							}
						}
					}
				}
				
				asynchGetter.completeJobAsynch(jobStatus, videoClipList);
			}
		}, new Response.ErrorListener()
		{

			@Override
			public void onErrorResponse(VolleyError error)
			{
				asynchGetter.completeJobAsynch(JobStatus.FAILED);
			}
		});
		
		// Start request operation
		requestQueue.add(request);
	}
	
	public void fetchVideoClipsAsynch(final int pageIndex, final int pageSize, final RequestQueue requestQueue, final OnJobDoneListener<Cursor> asynchGetter)
	{
		synchronized (asynchGetterStack)
		{	
			switch (videoClipRequestStatus)
			{
				// If there exists an on-going fetch operation
				case Status.BUSY:
				{
					// Will wait for fetch operation's completion
					// Push asynchronous getter to stack
					// (Stack will be drained after request completion)
					asynchGetterStack.add(asynchGetter);
					
					break;
				}
				// If there exist no active fetch operation
				case Status.IDLE:
				default:
				{
					// Get local videoListCollection
					int videoClipQuantity = dataSource.getVideoClipQuantity();
					
					// If local cache not-available
					// Or non cached video list required
					if(videoClipQuantity <= (pageIndex * pageSize))
					{
						// Start fetch operation
						
						// If Volley request queue provided
						if(requestQueue != null)
						{
							// Mark status as busy
							setVideoClipRequestStatus(Status.BUSY);
							
							// Push asynchronous getter to stack
							// (Stack will be drained after request completion)
							asynchGetterStack.add(asynchGetter);
							
							// Generate request URL
							String requestUrl = getVideoClipListRequestUrl(pageIndex, pageSize);
							
							Log.d("Foo", "will request: " + requestUrl);
							
							// Start request operation
							VideoClipListRequest request = VideoClipListRequest.newInstance(requestUrl, new Listener<JSONObject>()
							{
								@Override
								public void onResponse(JSONObject response)
								{
									// Received JSON object response
									// Now, we have to parse and drain waiting asynchronous getter
									
									int jobStatus = JobStatus.FAILED;
									List<VideoClip> videoClipList = null;
										
									if(response != null)
									{
										JSONArray videoClipJSONArray = JSONUtilities.getJsonArray(response, "root", null);
										
										if(videoClipJSONArray != null)
										{
											// We may assume we retrieved the category list successfully as JSONArray instance
											// Now we have to parse JSONArray to list
											
											// Fetch category quantity within JSONArray
											int quantity = videoClipJSONArray.length();
												
											// Mark status as succeed
											jobStatus = JobStatus.SUCCEED;
											
											// Initialize new category container list
											videoClipList = new ArrayList<VideoClip>(quantity);
											
											for (int i = 0; i < quantity; i++)
											{
												JSONObject videoClipJSONObject = JSONUtilities.getArrayObject(videoClipJSONArray, i, null);
												
												if(videoClipJSONObject != null)
												{
													VideoClip videoClip = new VideoClip(videoClipJSONObject);
													videoClipList.add(videoClip);
												}
											}
										}
									}
									
									// Update video clip list cache
									appendVideoClipCollection(videoClipList);
									
									drainAsynchGetterStack(jobStatus, dataSource.getVideoClipCursor());
									
									// Request operation completed!
									setVideoClipRequestStatus(Status.IDLE);
								}
							}, new ErrorListener() {
								
								@Override
								public void onErrorResponse(VolleyError error)
								{
									drainAsynchGetterStack(JobStatus.FAILED, null);

									setVideoClipRequestStatus(Status.IDLE);
								}
							});
							
							requestQueue.add(request);
						}
						else
						{
							Log.e("VideoUtilities", "Volley request queue must be provided!!!");
						}
					}
					else
					{
						Log.d("Foo", "Using cached data!");
						
						// Perform asynchronous getter callback with local cache
						asynchGetter.completeJobAsynch(JobStatus.SUCCEED, this.dataSource.getVideoClipCursor());
					}
					
					break;
				}
			}
		}
	}
	
	private synchronized void drainAsynchGetterStack(final int jobStatus, final Cursor videoClipCursor)
	{
		// While we got queued asynch getters
		while (asynchGetterStack.size() > 0)
		{
			// Pop top stack element
			OnJobDoneListener<Cursor> asynchGetter = asynchGetterStack.remove(asynchGetterStack.size() - 1);
			
			// Perform asynchronous callback
			asynchGetter.completeJobAsynch(jobStatus, videoClipCursor);
		}
	}
	
	
	/****************************************
	 * Accessors
	 */
	
	private synchronized void appendVideoClipCollection(final List<VideoClip> videoClipSet)
	{
		// If new VideoClip dataset provided
		if(videoClipSet != null)
		{
			for (VideoClip videoClip : videoClipSet)
			{
				this.dataSource.insertVideoClip(videoClip);
			}
		}
	}
	
	private void setVideoClipRequestStatus(final Integer status)
	{
		this.videoClipRequestStatus = status;
	}
	
	private String getVideoClipListRequestUrl(final int pageIndex, final int pageSize)
	{
		String categoryId = category.getId();
		String requestUrl = null;
		
		if(VIDEOLIST_HOMEPAGE.equalsIgnoreCase(categoryId))
		{
			requestUrl = VideoClipListRequest.getHomepageRequestUrl();
		}
		else if(VIDEOLIST_HEADLINE.equalsIgnoreCase(categoryId))
		{
			requestUrl = VideoClipListRequest.getHeadlineRequestUrl();
		}
		else if(VIDEOLIST_SEARCH.equalsIgnoreCase(categoryId))
		{
			String queryString = category.getName();
			
			requestUrl = VideoClipListRequest.getSearchVideoListRequestUrl(queryString, pageIndex, pageSize);
		}
		else
		{
			requestUrl = VideoClipListRequest.getCategoryVideoListRequestUrl(categoryId, pageIndex, pageSize);
		}
		
		return requestUrl;
	}
	
	public int getVideoClipQuantity()
	{
		int quantity = 0;
		
		if(dataSource != null)
		{
			quantity = dataSource.getVideoClipQuantity();
		}
		
		return quantity;
	}
	
	public void openCache()
	{
		synchronized (this)
		{
			if(dataSource != null)
			{
				dataSource.open();
			}
		}
	}
	
	public void closeCache()
	{
		synchronized (this)
		{
			if(dataSource != null)
			{
				dataSource.close();
			}
		}
	}
	
	public void clearCache()
	{
		synchronized (this)
		{
			if(dataSource != null)
			{
				dataSource.deleteAllVideoClips();
			}
		}
	}
}
