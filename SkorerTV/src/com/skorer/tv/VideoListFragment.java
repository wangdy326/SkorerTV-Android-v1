package com.skorer.tv;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.milliyet.tv.utilities.GoogleAnalyticsUtilities;
import com.milliyet.tv.utilities.VideoListUtilities;
import com.skorer.tv.R;
import com.skorer.tv.adapters.VideoListCursorAdapter;
import com.skorer.tv.adapters.VideoListCursorAdapter.ViewHolder;
import com.skorer.tv.listeners.EndlessListScrollListener;
import com.skorer.tv.listeners.OnJobDoneListener;
import com.skorer.tv.model.Category;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class VideoListFragment extends Fragment
{
	private static final String BUNDLE_CATEGORY_KEY = "videolistfragment.category";
	private static final String BUNDLE_LISTITEMPOSITION_KEY = "videolistfragment.listitemposition";
	private static final String BUNDLE_LISTITEMTOP_KEY = "videolistfragment.listitemtop";
	
	private final static int PAGE_SIZE = 20;
	protected Category category = null;
	protected boolean infiniteScrollEnabled = false;
	private EndlessListScrollListener<Cursor> scrollListener = null;
	int savedListItemTop = 0;
	int savedListItemPosition = 0;
	private WeakReference<View> progressFooterViewWeak = null;
	private WeakReference<ListView> listViewWeak = null;
	// This is a counter for progress view requests.
	// If its value bigger than 0, it indicates there still exists a background operation
	// and the progress view should be visible. Otherwise, progressview should be hidden
	private int progressViewRequestQuantity = 0;
	
	private OnJobDoneListener<Cursor> onVideoListFirstLoadListener = null;
	
	public static VideoListFragment newInstance(final Category category, final boolean infiniteScrollEnabled)
	{
		// Allocate & initialize new fragment
		VideoListFragment fragment = new VideoListFragment();
		
		fragment.category = category;
		fragment.infiniteScrollEnabled = infiniteScrollEnabled;
		
		return fragment;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		
		Log.d("Test", "Test save state");
		
		outState.putParcelable(BUNDLE_CATEGORY_KEY, this.category);
		
		int position = getListView().getFirstVisiblePosition();
		View v = getListView().getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();
		
		outState.putInt(BUNDLE_LISTITEMPOSITION_KEY, position);
		outState.putInt(BUNDLE_LISTITEMTOP_KEY, top);
		
		GoogleAnalyticsUtilities.sharedInstance(getActivity()).onSaveInstanceState(outState);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		if( savedInstanceState != null)
		{
			if(savedInstanceState.containsKey(BUNDLE_CATEGORY_KEY))
			{
				this.category = savedInstanceState.getParcelable(BUNDLE_CATEGORY_KEY);
			}
			
			if(savedInstanceState.containsKey(BUNDLE_LISTITEMPOSITION_KEY))
			{
				this.savedListItemPosition = savedInstanceState.getInt(BUNDLE_LISTITEMPOSITION_KEY);
			}
			
			if(savedInstanceState.containsKey(BUNDLE_LISTITEMTOP_KEY))
			{
				this.savedListItemTop = savedInstanceState.getInt(BUNDLE_LISTITEMTOP_KEY);
			}
			
			hideStatusView(false);
			
			Log.d("asdf", "restore pos: " + this.savedListItemPosition + ", top: " + this.savedListItemTop);
		}
		else
		{
			// Assume first load & show progress
			showStatusView(null, true, null, null, false);
		}
		
		GoogleAnalyticsUtilities.sharedInstance(getActivity()).onCreate(this.category.getName(), savedInstanceState);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		// Establish database connection
		VideoListUtilities.sharedInstance(getActivity(), this.category).openCache();
		
		// Add item select listener to ListView instance
		ListView listView = getListView();
		
		if(listView != null)
		{
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id)
				{
					showDetails(((ViewHolder) view.getTag()).id);
				}
			});
		}
		
		onVideoListFirstLoadListener = new OnJobDoneListener<Cursor>()
		{
			@Override
			public void onJobDone(int status, Cursor result)
			{
				// Acquire current fragment reference...
				final VideoListFragment fragment = VideoListFragment.this;
				
				// If fragment is still available
				if(fragment != null && !fragment.isRemoving())
				{
					switch (status)
					{
						case JobStatus.SUCCEED:
						{
							// Construct list view with initial data set
							fragment.updateVideoList(result);
							
							break;
						}
						default:
						{
							Log.d("Foooo", "Failed category request");
							
							if(category.getId() == VideoListUtilities.VIDEOLIST_SEARCH)
							{
								if(TextUtils.isEmpty(category.getName()))
								{
									// Show search requirements
									fragment.showStatusView("Herhangi bir konuda arama yapmak için yukarıda ki arama çubuğuna ilgili kriterleri yazın", false, null, null, false);
								}
								else
								{
									//  
									fragment.showStatusView("\"" + category.getName() + "\" kriterlerine ait arama sonucu bulunamadı", false, null, null, false);
								}
							}
							else
							{
								fragment.showStatusView("Beklenmedik bir hata oluştu", false, "Tekrar dene", new View.OnClickListener()
								{
									@Override
									public void onClick(View v)
									{
										fragment.showStatusView(null, true, null, null, false);
										
										fragment.loadVideoList(onVideoListFirstLoadListener);
									}
								}, false);
							}
							
							break;
						}
					}
				}
				else
				{
					Log.d("Video List Fragment", "Fragment is gone!");
				}
			}
		};
		
		// If infinite scroll required
		if(infiniteScrollEnabled)
		{
			// Fetch remote video list count (@server)
			loadVideoListCount(new OnJobDoneListener<Integer>()
			{
				@Override
				public void onJobDone(int status, Integer result)
				{
					Log.d("Foo", "Found #" + result + " item");
					
					VideoListFragment fragment = VideoListFragment.this;
					
					if(fragment != null && !fragment.isRemoving())
					{
						fragment.scrollListener = new EndlessListScrollListener<Cursor>(result)
						{
							@Override
							public void onMoreDataRequiredAsynch(final OnJobDoneListener<Cursor> onFetchDoneListener)
							{
								VideoListFragment fragment = VideoListFragment.this;
								
								if(fragment != null)
								{
									fragment.showProgressFooterView();
									fragment.loadVideoList(onFetchDoneListener);
								}
							}

							@Override
							public void onNewDataSetArrived(final Cursor dataSetCursor)
							{
								Log.d("Bar", "Arrived: " + dataSetCursor.getCount());
								
								final VideoListFragment fragment = VideoListFragment.this;
								
								if(fragment != null)
								{
									final ListView listView = fragment.getListView();
									
									if(listView != null)
									{
										listView.post(new Runnable()
										{
											@Override
											public void run()
											{
												fragment.hideProgressFooterView();
												
												VideoListCursorAdapter adapter = (VideoListCursorAdapter) listView.getAdapter();
												adapter.changeCursor(dataSetCursor);
												adapter.notifyDataSetChanged();
											}
										});
									}
								}
							}
							
							@Override
							public void onNewDataSetFailed()
							{
								Log.d("Foo", "Failed to load new dataset");
								
								final VideoListFragment fragment = VideoListFragment.this;
								
								if(fragment != null)
								{
									Activity activity = fragment.getActivity();
									
									if(activity != null)
									{
										activity.runOnUiThread(new Runnable()
										{
											@Override
											public void run()
											{
												fragment.hideProgressFooterView();
											}
										});
									}
								}
							}
						};
						
						loadVideoList(onVideoListFirstLoadListener);
					}
				}
			});
		}
		else
		{
			loadVideoList(onVideoListFirstLoadListener);
		}
		
		GoogleAnalyticsUtilities.sharedInstance(getActivity()).onStart(this.category.getName());
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		
		// Disconnect database connection
		VideoListUtilities.sharedInstance(getView().getContext(), category).closeCache();
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		
		Log.d("VLF", "Attached to: " + activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_videolist, null, false);
		
		if(rootView != null)
		{
			setListView((ListView) rootView.findViewById(R.id.videolist_listview));
			setProgressFooterView(rootView.findViewById(R.id.videolist_progressfooter));
		}
		
		return rootView;
	}
	
	private void loadVideoListCount(final OnJobDoneListener<Integer> listener)
	{
		VideoListUtilities.sharedInstance(getActivity(), this.category).fetchVideoClipCountAsynch(SkorerTVApplication.requestQueue, listener);
	}
	
	protected synchronized void loadVideoList(final OnJobDoneListener<Cursor> listener)
	{
		int dataQuantity = VideoListUtilities.sharedInstance(getActivity(), this.category).getVideoClipQuantity();
		int pageIndex = dataQuantity / PAGE_SIZE;
		
		Log.d("Video List Fragment", "Fetching ("+ category +"): " + pageIndex + " - " + (PAGE_SIZE));
		
		Context context = getActivity();
		
		VideoListUtilities util = VideoListUtilities.sharedInstance(context, category);
		
		util.fetchVideoClipsAsynch(pageIndex, PAGE_SIZE, SkorerTVApplication.requestQueue, listener);
	}
	
	protected void updateVideoList(final Cursor videoClipCursor)
	{
		View view = getView();
		
		if(view != null)
		{
			getView().post(new Runnable()
			{
				@Override
				public void run()
				{
					// Acquire current fragment reference...
					VideoListFragment fragment = VideoListFragment.this;
					
					// If fragment is still available
					if(fragment != null && !fragment.isRemoving())
					{
						try
						{
							// If given data set is empty.
							if(videoClipCursor == null || videoClipCursor.isClosed() || videoClipCursor.getCount() == 0)
							{
								if(VideoListUtilities.VIDEOLIST_SEARCH == category.getId())
								{
									showStatusView("Herhangi bir arama sonucu yok", false, null, null, false);
								}
								else
								{
									showStatusView("Bu kategoriye ait video bulunamadı", false, "Diğer kategorilere göz at", new View.OnClickListener()
									{
										@Override
										public void onClick(View v)
										{
											Log.d("Foo", "Open navigation drawer");
											
											Activity activity = getActivity();
											
											if(activity != null)
											{
												DrawerLayout drawerLayout = (DrawerLayout) activity.findViewById(R.id.main_drawerlayout);
												
												drawerLayout.openDrawer(Gravity.LEFT);
											}
										}
									}, true);
								}
							}
							else
							{
								// Fetch ListView instance
								ListView listView = fragment.getListView();
								
								if(listView != null)
								{
									// Allocate & initialize new adapter with given data-set
									VideoListCursorAdapter adapter = new VideoListCursorAdapter(fragment.getActivity(), videoClipCursor);
									
									// Attach adapter to fragment
									listView.setAdapter(adapter);
									
									// Inject infinite scroll functionality
									listView.setOnScrollListener(scrollListener);
									
									// Scroll to saved position
									listView.setSelectionFromTop(fragment.savedListItemPosition, fragment.savedListItemTop);
									
									Log.d("asdf", "pos: " + fragment.savedListItemPosition + ", top: " + fragment.savedListItemTop);
									
									// Hide progress view as we are done updating video list
									fragment.hideStatusView(true);
								}
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
							
							showStatusView("Bu kategoriye ait video bulunamadı", false, "Diğer kategorilere göz at", new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									Log.d("Foo", "Open navigation drawer");
									
									Activity activity = getActivity();
									
									if(activity != null)
									{
										DrawerLayout drawerLayout = (DrawerLayout) activity.findViewById(R.id.main_drawerlayout);
										
										drawerLayout.openDrawer(Gravity.LEFT);
									}
								}
							}, true);
							
//							displayErrorUI("Beklenmedik bir hata oluştu. Lütfen daha sonra tekrar deneyin.");
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
	
	private void showDetails(String videoId)
	{
		if(!TextUtils.isEmpty(videoId))
		{
			Intent videoClipIntent = new Intent(getView().getContext(), VideoClipActivity.class);
			
			videoClipIntent.putExtra(VideoClipActivity.BUNDLE_VIDEOCLIP_KEY, videoId);
			videoClipIntent.putExtra(VideoClipActivity.BUNDLE_PREROLLURL_KEY, getPreRollUrl());
			
			startActivity(videoClipIntent);
		}
	}
	
	private String getPreRollUrl()
	{
		String categorySuffix = "";
		
		if(category != null)
		{
			try
			{
				categorySuffix = URLEncoder.encode(category.getName(), "utf-8");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return "http://mobworkz.com/video/skorertv_android_" + categorySuffix + ".xml";
	}
	
	private void showProgressFooterView()
	{
		View view = getProgressFooterView();
		
		if(view != null)
		{
			synchronized (view)
			{
				progressViewRequestQuantity++;
				crossfade(null, view, 1000);
			}
		}
	}
	
	private void hideProgressFooterView()
	{
		View view = getProgressFooterView();
		
		if(view != null)
		{
			synchronized (view)
			{
				progressViewRequestQuantity--;
				
				if(progressViewRequestQuantity <= 0)
				{
					progressViewRequestQuantity = 0;
					crossfade(null, view, 1000);
				}
			}
		}
	}
	
	void showStatusView(final String message, final boolean showProgressBar, final String actionButtonText, final View.OnClickListener actionButtonClickListener, final boolean animated)
    {
    	View view = getView();
    	
    	if(view != null)
    	{
    		final View statusView = view.findViewById(R.id.statusview);
        	
        	if(statusView != null)
        	{
        		statusView.post(new Runnable()
    			{
    				@Override
    				public void run()
    				{
    		    		// Progress bar
    		    		ProgressBar progressBar = (ProgressBar) statusView.findViewById(R.id.statusview_progressbar);
    		    		
    		    		if(progressBar != null)
    		    		{
    		    			progressBar.setVisibility((showProgressBar)?(View.VISIBLE):(View.INVISIBLE));
    		    		}
    		    		
    		    		// Message
    		    		TextView textView = (TextView) statusView.findViewById(R.id.statusview_textview);
    		    		
    		    		if(textView != null)
    		    		{
    		    			textView.setText(message);
    		    		}
    		    		
    		    		// Action button
    		    		Button actionButton = (Button) statusView.findViewById(R.id.statusview_button);
    		    		
    		    		if(actionButton !=  null)
    		    		{
    		    			actionButton.setText(actionButtonText);
    		    			actionButton.setOnClickListener(actionButtonClickListener);
    		    			
    		    			actionButton.setVisibility((actionButtonClickListener == null)?(View.INVISIBLE):(View.VISIBLE));
    		    		}
    		    		
    		    		crossfade(null, statusView, (animated)?(2000):(0));
    		    		
    		    		statusView.bringToFront();
    				}
    			});
        	}
    	}
    	else
    	{
    		Log.d("Foo", "Headless fragment requested progress view!");
    	}
    }
    
    void hideStatusView(final boolean animated)
    {
    	View view = getView();
    	
    	if(view != null)
    	{
	    	final View statusView = view.findViewById(R.id.statusview);
	    	
	    	if(statusView != null)
	    	{
	    		statusView.post(new Runnable()
				{
	    			@Override
	    			public void run()
	    			{
    		    		crossfade(statusView, null, (animated)?(2000):(0));
	    			}
				});
	    	}
    	}
    	else
    	{
    		Log.d("Foo", "Headless fragment requested progress view!");
    	}
    }
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void crossfade(final View ancestorView, final View successorView, final int animationDuration)
	{	
	    View view = getView();
	    
	    if(view != null)
	    {
	    	view.post(new Runnable()
			{
				@Override
				public void run()
				{
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				    {
				    	if(successorView != null && successorView.getVisibility() != View.VISIBLE && successorView.getAlpha() < 1.0f)
				    	{
				    		// Set the content view to --0%-- !!!not 0 but current!!! opacity but visible, so that it is visible
						    // (but fully transparent) during the animation.
							//successorView.setAlpha(successorView.getAlpha());
							successorView.setVisibility(View.VISIBLE);

						    // Animate the content view to 100% opacity, and clear any animation
						    // listener set on the view.
							successorView.animate()
						            	 .alpha(1f)
						            	 .setDuration(animationDuration)
						            	 .setListener(null);
				    	}

					    if(ancestorView != null && ancestorView.getVisibility() == View.VISIBLE)
					    {
					    	// Animate the loading view to 0% opacity. After the animation ends,
						    // set its visibility to GONE as an optimization step (it won't
						    // participate in layout passes, etc.)
							ancestorView.animate()
						    .alpha(0f)
						    .setDuration(animationDuration)
						    .setListener(new AnimatorListenerAdapter() {
						            		
						    	@Override
						        public void onAnimationEnd(Animator animation) {
						    		ancestorView.setVisibility(View.GONE);
						    	}
						    });
					    }
				    }
				    else
				    {
				    	if(successorView != null)
				    	{
				    		successorView.setVisibility(View.VISIBLE);
				    	}
				    	
				    	if(ancestorView != null)
				    	{
				    		ancestorView.setVisibility(View.GONE);
				    	}
				    }
				}
			});
	    }
	    else
	    {
	    	Log.d("Foo", "Cross-fade requested on headless fragment!");
	    }
	}
	
	private void setListView(ListView listView)
	{
		if(listView == null)
		{
			this.listViewWeak = null;
		}
		else
		{
			this.listViewWeak = new WeakReference<ListView>(listView);
		}
	}
	
	ListView getListView()
	{
		return (this.listViewWeak == null)?(null):(this.listViewWeak.get());
	}
	
	private void setProgressFooterView(View view)
	{
		if(view == null)
		{
			this.progressFooterViewWeak = null;
		}
		else
		{
			this.progressFooterViewWeak = new WeakReference<View>(view);
		}
	}
	
	private View getProgressFooterView()
	{
		return (this.progressFooterViewWeak == null)?(null):(this.progressFooterViewWeak.get());
	}
}