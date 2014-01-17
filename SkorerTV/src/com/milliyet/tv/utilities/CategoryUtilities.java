package com.milliyet.tv.utilities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.skorer.tv.listeners.OnJobDoneListener;
import com.skorer.tv.listeners.OnJobDoneListener.JobStatus;
import com.skorer.tv.model.Category;
import com.skorer.tv.requests.CategoryListRequest;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class CategoryUtilities
{
	class Status
	{
		public static final int IDLE = 0;
		public static final int BUSY = 1;
	}
	
	/***************************************
	 * Singleton reference
	 */
	private static CategoryUtilities sharedInstance = null;
	
	
	/***************************************
	 * Variables
	 */
	
	// TODO: Move this to disc cache (e.g SharedPreferences)
	private List<Category> categoryList = null;
	private int status = Status.IDLE;
	private List<OnJobDoneListener<List<Category>>> asynchGetterStack = new ArrayList<OnJobDoneListener<List<Category>>>();
	
	private Listener<JSONObject> requestListener = new Listener<JSONObject>()
	{
		@Override
		public void onResponse(JSONObject response)
		{
			synchronized (asynchGetterStack)
			{
				// Received JSON object response
				// Now, we have to parse and drain waiting asynchronous getter
				
				int jobStatus = JobStatus.FAILED;
				List<Category> categoryList = null;
				
				if(response != null)
				{
					JSONArray categoryJSONArray = JSONUtilities.getJsonArray(response, "root", null);
					
					if(categoryJSONArray != null)
					{
						// We may assume we retrieved the category list successfully as JSONArray instance
						// Now we have to parse JSONArray to list
						
						// Fetch category quantity within JSONArray
						int quantity = categoryJSONArray.length();
						
						// Mark status as succeed
						jobStatus = JobStatus.SUCCEED;
						
						// Initialize new category container list
						categoryList = new ArrayList<Category>(quantity);
						
						for (int i = 0; i < quantity; i++)
						{
							JSONObject categoryJSONObject = JSONUtilities.getArrayObject(categoryJSONArray, i, null);
							
							if(categoryJSONObject != null)
							{
								Category category = Category.fromJSONObject(categoryJSONObject);
								
								if(category != null)
								{
									categoryList.add(category);
								}
							}
						}
					}
				}
				
				drainAsynchGetterStack(jobStatus, categoryList);
				
				CategoryUtilities.this.status = Status.IDLE;
			}
		}
	};
	
	private ErrorListener requestErrorListener = new ErrorListener()
	{
		@Override
		public void onErrorResponse(VolleyError error)
		{
			synchronized (asynchGetterStack)
			{
				drainAsynchGetterStack(JobStatus.FAILED, null);

				CategoryUtilities.this.status = Status.IDLE;
			}
		}
	};
	
	
	/***************************************
	 * Constructors & instance providers
	 */
	private CategoryUtilities(){/*No public constructor*/}

	public static synchronized CategoryUtilities sharedInstance()
	{
		if(CategoryUtilities.sharedInstance == null)
		{
			CategoryUtilities.sharedInstance = new CategoryUtilities();
		}
		
		return CategoryUtilities.sharedInstance;
	}

	
	/****************************************
	 * ???
	 */
	
	public void fetchCategoriesAsynch(RequestQueue requestQueue, OnJobDoneListener<List<Category>> asynchGetter)
	{
		synchronized (asynchGetterStack)
		{
			switch (status)
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
					// If local cache not-available
					if(categoryList == null)
					{
						// Start fetch operation
						
						// If Volley request queue provided
						if(requestQueue != null)
						{
							// Mark status as busy
							status = Status.BUSY;
							
							// Push asynchronous getter to stack
							// (Stack will be drained after request completion)
							asynchGetterStack.add(asynchGetter);
							
							// Start request operation
							CategoryListRequest request = CategoryListRequest.newInstance(requestListener, requestErrorListener);
							requestQueue.add(request);
						}
					}
					else
					{
						// Perform asynchronous getter callback with local cache
						asynchGetter.completeJobAsynch(JobStatus.SUCCEED, categoryList);
					}
					
					break;
				}
			}
		}
	}
	
	private void drainAsynchGetterStack(int jobStatus, List<Category> categoryList)
	{
		synchronized (this.asynchGetterStack)
		{
			while (asynchGetterStack.size() > 0)
			{
				// Pop top stack element
				OnJobDoneListener<List<Category>> asynchGetter = asynchGetterStack.remove(asynchGetterStack.size() - 1);
				
				// Perform asynchronous callback
				asynchGetter.completeJobAsynch(jobStatus, categoryList);
			}
		}
	}
}
