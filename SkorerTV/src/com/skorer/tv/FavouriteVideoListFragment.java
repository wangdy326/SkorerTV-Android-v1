package com.skorer.tv;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;

import com.milliyet.tv.utilities.FavouritesUtilities;
import com.milliyet.tv.utilities.VideoListUtilities;
import com.skorer.tv.R;
import com.skorer.tv.adapters.VideoListArrayAdapter;
import com.skorer.tv.listeners.OnJobDoneListener;
import com.skorer.tv.model.Category;
import com.skorer.tv.model.VideoClip;

/**
 * Beware, this class uses shared preferences as database implementation not designed for permanent data.
 * Instead database implemented for cache usage. So we got 2 alternatives for the future;
 * 
 *   - Alternative 1: Move favorites to a web-service & use database cache implementation, 
 *     which also requires a login system or device identification.
 *   
 *   - Alternative 2: Track cache & permanent tables on database or maybe shared preferences
 *     (this is required as categories are dynamic & we are unaware of what to clean/leave as is.). 
 *     Then clean only tracked cache tables on application launch, instead of wiping all database at application star!
 * 
 * 
 * 
 * @author gokhanbarisaker
 *
 */
public class FavouriteVideoListFragment extends VideoListFragment
{
	private List<VideoClip> favouriteVideoClipList = null;
	
	public static FavouriteVideoListFragment newInstance()
	{
		FavouriteVideoListFragment fragment = new FavouriteVideoListFragment();
		
		fragment.category = getFavouritesCategory();
		fragment.infiniteScrollEnabled = false;
		
		return fragment;
	}
	
	public static Category getFavouritesCategory()
	{
		Category category = new Category();
		category.setId(VideoListUtilities.VIDEOLIST_FAVOURITES);
		category.setName("Favorilerim");
		
		return category;
	}
	
	@Override
	protected synchronized void loadVideoList(OnJobDoneListener<Cursor> listener)
	{
		/*
		 * Ignore listener argument
		 */
		
		FavouritesUtilities.sharedInstance().fetchFavouriteVideos((Context) getActivity(), new OnJobDoneListener<List<VideoClip>>()
		{
			@Override
			public void onJobDone(int status, List<VideoClip> result)
			{
				Log.d("Foo", "Updated favourite list: " + result);
				
				FavouriteVideoListFragment fragment = FavouriteVideoListFragment.this;
				
				if(fragment != null)
				{
					fragment.favouriteVideoClipList = result;
					
					fragment.updateVideoList(null);
				}
			}
		});
	}
	
	@Override
	protected void updateVideoList(Cursor videoClipCursor)
	{
		/*
		 * Ignore cursor
		 */
		
		View view = getView();
		
		if(view != null)
		{
			getView().post(new Runnable()
			{
				@Override
				public void run()
				{
					// Acquire current fragment reference...
					FavouriteVideoListFragment fragment = FavouriteVideoListFragment.this;
					
					// If fragment is still available
					if(fragment != null && !fragment.isRemoving())
					{
						// If given data set is empty.
						if(fragment.favouriteVideoClipList == null || fragment.favouriteVideoClipList.size() == 0)
						{
							fragment.showStatusView("Favori videonuz yok", false, "Eklemek için kategorilere göz atın", new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									Activity activity = getActivity();
									
									if(activity != null)
									{
										DrawerLayout drawerLayout = (DrawerLayout) activity.findViewById(R.id.main_drawerlayout);
										
										drawerLayout.openDrawer(Gravity.LEFT);
									}
								}
							}, false);
						}
						else
						{
							Activity activity = fragment.getActivity();
							
							if(activity == null)
							{
								// Display not ready, UI???
							}
							else
							{
								// Fetch ListView instance
								ListView listView = fragment.getListView();
								
								if(listView != null)
								{
									VideoListArrayAdapter adapter = (VideoListArrayAdapter) listView.getAdapter();
									
									if(listView.getAdapter() == null)
									{
										// Allocate & initialize new adapter with given data-set
										adapter = new VideoListArrayAdapter(fragment.getActivity(), favouriteVideoClipList);
										
										// Attach adapter to fragment
										listView.setAdapter(adapter);
									}
									else
									{
										adapter.dataSet = favouriteVideoClipList;
										
										adapter.notifyDataSetChanged();
									}
									
									// Scroll to saved position
									listView.setSelectionFromTop(fragment.savedListItemPosition, fragment.savedListItemTop);
									
									Log.d("asdf", "pos: " + fragment.savedListItemPosition + ", top: " + fragment.savedListItemTop);
								}
								
								// Hide progress view as we are done updating video list
								fragment.hideStatusView(true);
							}
						}
					}
				}
			});
		}
		else
		{
			Log.d("VideoListFragment", "Current fragment lack UI!");
		}
	}
}
