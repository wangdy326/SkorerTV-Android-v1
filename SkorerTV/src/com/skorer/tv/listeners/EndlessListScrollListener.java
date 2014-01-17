package com.skorer.tv.listeners;

import java.util.List;

import android.database.Cursor;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.skorer.tv.model.VideoClip;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public abstract class EndlessListScrollListener<E> implements OnScrollListener
{
	private int dataSetQuantity = 0;
	private boolean serverSideBugSafetyLockOpen = false;
	private boolean loadingNewDataSet = false;
	private int maxDataQuantity = Integer.MAX_VALUE;
	
	public EndlessListScrollListener(int maxDataQuantity)
	{
		this.maxDataQuantity = maxDataQuantity;
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount)
	{
		final int paddingQuantity = 4;
		
		// If we approached to near end-of-list and we may assume there is more data to fetch
		boolean loadMore = /* maybe add a padding */ ((firstVisibleItem + visibleItemCount + paddingQuantity) >= totalItemCount)
				&& (totalItemCount < maxDataQuantity) 
				&& !serverSideBugSafetyLockOpen;
		
		if(loadMore)
		{
			cacheMoreData();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) { }
	
	public synchronized void cacheMoreData()
	{
		// If there exist no active load operation
		if(!loadingNewDataSet)
		{
			// We will start loading new data set
			Log.d("Endless List Adapter", "Starting new load operation");
			
			// Mark scroll listener as loading
			loadingNewDataSet = true;
			
			onMoreDataRequiredAsynch(new OnJobDoneListener<E>()
			{
				@Override
				public void onJobDone(int status, E result)
				{
					switch (status)
					{
						case JobStatus.SUCCEED:
						{
							if(result instanceof Cursor)
							{
								int successorDataSetQuantity = ((Cursor) result).getCount();
								
								// If server returned same result
								if(successorDataSetQuantity == dataSetQuantity)
								{
									serverSideBugSafetyLockOpen = true;
									
									Log.d("Foo", "Safety lock enabled");
								}
								
								dataSetQuantity = successorDataSetQuantity;
							}
							else if(result instanceof List<?>)
							{
								int successorDataSetQuantity = ((List) result).size();
								
								// If server returned same result
								if(successorDataSetQuantity == dataSetQuantity)
								{
									serverSideBugSafetyLockOpen = true;
									

									Log.d("Foo", "Safety lock enabled");
								}
								
								dataSetQuantity = successorDataSetQuantity;
							}
							
							onNewDataSetArrived(result);
							
							break;
						}
						default:
						{
							onNewDataSetFailed();
							
							break;
						}
					}
					
					loadingNewDataSet = false;
				}
			});
		}
		else
		{
			Log.d("Endless List Adapter", "There exist ongoing load operation!");
		}
	}
	
	/**
	 * Inside of this method must work asynchronously,
	 * in order to escape blocking UI thread.
	 * 
	 * 
	 * @param onFetchDoneListener
	 */
	public abstract void onMoreDataRequiredAsynch(final OnJobDoneListener<E> onFetchDoneListener);
	
	public abstract void onNewDataSetArrived(final E dataSetCursor);
	
	public abstract void onNewDataSetFailed();
}
