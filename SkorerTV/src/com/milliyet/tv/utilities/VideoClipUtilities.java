package com.milliyet.tv.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.skorer.tv.listeners.OnJobDoneListener;
import com.skorer.tv.listeners.OnJobDoneListener.JobStatus;
import com.skorer.tv.model.VideoClip;
import com.skorer.tv.requests.VideoClipRequest;
import com.skorer.tv.requests.VideoClipVoteRequest;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class VideoClipUtilities
{
	class Status
	{
		public static final int IDLE = 0;
		public static final int BUSY = 1;
	}
	
	/***************************************
	 * Singleton reference
	 */
	private static VideoClipUtilities sharedInstance = null;
	
	
	/***************************************
	 * Variables
	 */
	
	// TODO: Move this to disc cache (e.g SharedPreferences)
//	private int status = Status.IDLE;
	private Map<String, Integer> statusMap = new HashMap<String, Integer>();
	// video clip id <--> asynch getter stack key/value pair
	private Map<String, List<OnJobDoneListener<VideoClip>>> asynchGetterMap = new HashMap<String, List<OnJobDoneListener<VideoClip>>>();
	
//	private Listener<JSONObject> requestListener = new Listener<JSONObject>()
//	{
//		@Override
//		public void onResponse(JSONObject response)
//		{
//			synchronized (asynchGetterMap)
//			{
//				// Received JSON object response
//				// Now, we have to parse and drain waiting asynchronous getter
//				
//				int jobStatus = JobStatus.FAILED;
//				VideoClip videoClip = null;
//				
//				if(response != null)
//				{
//					JSONArray videoClipJSONArray = JSONUtilities.getJsonArray(response, "root", null);
//					
//					if(videoClipJSONArray != null)
//					{
//						// We may assume we retrieved the VideoClip list successfully as JSONArray instance
//						// Now we have to fetch & parse first element of JSONArray to VideoClip instance
//						
//						// Mark status as succeed
//						jobStatus = JobStatus.SUCCEED;
//						
//						// Fetch video clip object
//						JSONObject videoClipJSONObject = JSONUtilities.getArrayObject(videoClipJSONArray, 0, null);
//						
//						if(videoClipJSONObject != null)
//						{
//							videoClip = new VideoClip(videoClipJSONObject);
//						}
//					}
//				}
//				
//				drainAsynchGetterStack(jobStatus, videoClip);
//				
//				VideoClipUtilities.this.status = Status.IDLE;
//			}
//		}
//	};
	
//	private ErrorListener requestErrorListener = new ErrorListener()
//	{
//		@Override
//		public void onErrorResponse(VolleyError error)
//		{
//			synchronized (asynchGetterMap)
//			{
//				drainAsynchGetterStack(JobStatus.FAILED, null);
//
//				VideoClipUtilities.this.status = Status.IDLE;
//			}
//		}
//	};
	
	
	/***************************************
	 * Constructors & instance providers
	 */
	private VideoClipUtilities(){ /*No public constructor*/ }

	public static synchronized VideoClipUtilities sharedInstance()
	{
		if(VideoClipUtilities.sharedInstance == null)
		{
			VideoClipUtilities.sharedInstance = new VideoClipUtilities();
		}
		
		return VideoClipUtilities.sharedInstance;
	}

	
	/****************************************
	 * ???
	 */
	
	public void voteVideoClipAsynch(final String videoIdentifier, final boolean like, final RequestQueue requestQueue, final OnJobDoneListener<String> onJobDoneListener)
	{
		// Allocate & initialize request operation
		VideoClipVoteRequest request = VideoClipVoteRequest.newInstance(videoIdentifier, like, new Response.Listener<JSONObject>()
		{
			@Override
			public void onResponse(JSONObject response)
			{
				// TODO Auto-generated method stub
				
				// If valid response received & response contains alert message (only distinct identifier for vote status for now!)
				if(response != null && response.has(VideoClipVoteRequest.VIDEOCLIPVOTE_RESPONSE_ALERTMESSAGE_KEY))
				{
					final String alertMessage = JSONUtilities.getJsonString(response, VideoClipVoteRequest.VIDEOCLIPVOTE_RESPONSE_ALERTMESSAGE_KEY, null);
					
					onJobDoneListener.completeJobAsynch(JobStatus.SUCCEED, alertMessage);
				}
				else
				{
					onJobDoneListener.completeJobAsynch(JobStatus.FAILED);
				}
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				onJobDoneListener.completeJobAsynch(JobStatus.FAILED);
			}
		});
		
		// Start request operation
		requestQueue.add(request);
	}
	
	public void fetchVideoClipUrlAsynch(final String videoCode, final RequestQueue requestQueue, final OnJobDoneListener<String> asynchGetter)
	{
		VideoClipUrlRequest request = VideoClipUrlRequest.newInstance(videoCode, new Response.Listener<String>()
		{
			@Override
			public void onResponse(String response)
			{
				asynchGetter.completeJobAsynch(JobStatus.SUCCEED, response);
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				asynchGetter.completeJobAsynch(JobStatus.FAILED);
			}
		});
		
		requestQueue.add(request);
	}
	
	public void fetchVideoClipAsynch(final String videoIdentifier, final RequestQueue requestQueue, final OnJobDoneListener<VideoClip> asynchGetter)
	{
		synchronized (asynchGetterMap)
		{
			switch (getStatus(videoIdentifier))
			{
				// If there exists an on-going fetch operation
				case Status.BUSY:
				{
					// Will wait for fetch operation's completion
					// Push asynchronous getter to stack
					// (Stack will be drained after request completion)
					addAsynchGetter(videoIdentifier, asynchGetter);
					
					break;
				}
				// If there exist no active fetch operation
				case Status.IDLE:
				default:
				{
					// Start fetch operation
					
					// If Volley request queue provided
					if(requestQueue != null)
					{
						// Mark status as busy
//						status = Status.BUSY;
						setStatus(videoIdentifier, Status.BUSY);
						
						// Push asynchronous getter to stack
						// (Stack will be drained after request completion)
						addAsynchGetter(videoIdentifier, asynchGetter);
						
						// Start request operation
						VideoClipRequest request = VideoClipRequest.newInstance(videoIdentifier, new ResponseJsonListener(videoIdentifier), new ResponseErrorListener(videoIdentifier));
						requestQueue.add(request);
					}
					
					break;
				}
			}
		}
	}
	
	private void addAsynchGetter(final String videoId, final OnJobDoneListener<VideoClip> asynchGetter)
	{
		synchronized (asynchGetterMap)
		{
			// If given parameters are valid for stack
			if(asynchGetter != null && !TextUtils.isEmpty(videoId))
			{
				// Fetch asynch getter stack for given video id request
				List<OnJobDoneListener<VideoClip>> asynchGetterStack = this.asynchGetterMap.get(videoId);
				
				// If stack not initialized before
				if(asynchGetterStack == null)
				{
					// Allocate & initialize new stack
					asynchGetterStack = new ArrayList<OnJobDoneListener<VideoClip>>();
					
					// Attach new stack to asynch getter map
					asynchGetterMap.put(videoId, asynchGetterStack);
				}
				
				// Push asynch getter to stack
				asynchGetterStack.add(asynchGetter);
			}
		}
	}
	
	private void drainAsynchGetterStack(final String videoIdentifier, final int jobStatus, final VideoClip videoClip)
	{
		synchronized (asynchGetterMap)
		{
			if(videoIdentifier != null)
			{
				List<OnJobDoneListener<VideoClip>> asynchGetterStack = this.asynchGetterMap.get(videoIdentifier);
				
				if(asynchGetterStack != null)
				{
					while (asynchGetterStack.size() > 0)
					{
						// Pop top stack element
						OnJobDoneListener<VideoClip> asynchGetter = asynchGetterStack.remove(asynchGetterStack.size() - 1);
						
						// Perform asynchronous callback
						asynchGetter.completeJobAsynch(jobStatus, videoClip);
					}
				}
			}
		}
	}
	
	private int getStatus(final String videoIdentifier)
	{
		Integer status = new Integer(Status.IDLE);
		
		if(videoIdentifier != null)
		{
			status = this.statusMap.get(videoIdentifier);
			
			if(status == null)
			{
				status = Status.IDLE;
			}
		}
		
		return status;
	}
	
	private void setStatus(final String videoIdentifier, int status)
	{
		if(videoIdentifier != null)
		{
			this.statusMap.put(videoIdentifier, status);
		}
	}
	
	class ResponseJsonListener implements Listener<JSONObject>
	{
		private String requestVideoIdentifier = null;
		
		public ResponseJsonListener(final String videoIdentifier)
		{
			this.requestVideoIdentifier = videoIdentifier;
		}
		
		@Override
		public void onResponse(JSONObject response)
		{
			synchronized (asynchGetterMap)
			{
				// Received JSON object response
				// Now, we have to parse and drain waiting asynchronous getter
				
				int jobStatus = JobStatus.FAILED;
				VideoClip videoClip = null;
				
				if(response != null)
				{
					JSONArray videoClipJSONArray = JSONUtilities.getJsonArray(response, "root", null);
					
					if(videoClipJSONArray != null)
					{
						// We may assume we retrieved the VideoClip list successfully as JSONArray instance
						// Now we have to fetch & parse first element of JSONArray to VideoClip instance
						
						// Mark status as succeed
						jobStatus = JobStatus.SUCCEED;
						
						// Fetch video clip object
						JSONObject videoClipJSONObject = JSONUtilities.getArrayObject(videoClipJSONArray, 0, null);
						
						if(videoClipJSONObject != null)
						{
							videoClip = new VideoClip(videoClipJSONObject);
						}
					}
				}
				
				drainAsynchGetterStack(this.requestVideoIdentifier, jobStatus, videoClip);
				
//				VideoClipUtilities.this.status = Status.IDLE;
				VideoClipUtilities.this.setStatus(requestVideoIdentifier, Status.IDLE);
			}
		}
	}
	
	class ResponseErrorListener implements Response.ErrorListener
	{
		private String requestVideoIdentifier = null;
		
		public ResponseErrorListener(final String videoIdentifier)
		{
			this.requestVideoIdentifier = videoIdentifier;
		}
		
		@Override
		public void onErrorResponse(VolleyError error)
		{
			synchronized (asynchGetterMap)
			{
				drainAsynchGetterStack(this.requestVideoIdentifier, JobStatus.FAILED, null);

//				VideoClipUtilities.this.status = Status.IDLE;
				VideoClipUtilities.this.setStatus(requestVideoIdentifier, Status.IDLE);
			}
		}
	};
}
