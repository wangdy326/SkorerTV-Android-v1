package com.skorer.tv;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.comscore.analytics.comScore;
import com.milliyet.tv.utilities.VideoListUtilities;
import com.skorer.tv.R;
import com.skorer.tv.model.Category;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class MainActivity extends ActionBarActivity
{
	/***************************************
	 * Variables
	 */
	
	private static final int SEARCH_TRIGGER_TIMEOUT = 1000;	// in milliseconds
	private static final int SEARCH_KEYWORD_MINCHARACTERCOUNT = 0;
	
	private CategoryNavigationDrawerFragment categoryNavigationDrawerFragment = null;
	private WeakReference<DrawerLayout> drawerLayoutWeak = null;
	
	private Handler handler = null;
	
	private String searchKeyword = null;
	private Runnable searchRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			MainActivity activity = MainActivity.this;
			
			if(activity != null)
			{
				activity.performSearch(activity.searchKeyword);
			}
		}
	};
	
	/***************************************
	 * Activity state handlers
	 */
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		comScore.setAppName("Skorer TV");
		comScore.setAppContext(this.getApplicationContext());
		
		setDrawerLayout((DrawerLayout) findViewById(R.id.main_drawerlayout));
		
		// Setup navigation drawer fragment
		getCategoryNavigationDrawerFragment().setUp(R.id.main_categorynavigationdrawerfragment, getDrawerLayout());
		getCategoryNavigationDrawerFragment().setOnCategorySelectedListener(new CategoryNavigationDrawerFragment.OnCategorySelectedListener()
		{
			@Override
			public void onCategorySelected(Category category, boolean alreadySelected)
			{
				showCategory(category, !alreadySelected);
			}
		});
		getCategoryNavigationDrawerFragment().setOnCategoriesLoadedListener(new CategoryNavigationDrawerFragment.OnCategoriesLoadedListener()
		{
			@Override
			public void onCategoriesLoaded()
			{
				supportInvalidateOptionsMenu();
				showCategory(getCategoryNavigationDrawerFragment().getActiveCategory(), true);
			}
		});
		
		// Lock drawer till the category fragment ready
		DrawerLayout drawerLayout = getDrawerLayout();
		
		if(drawerLayout != null)
		{
			drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
		
		// Initialize handler with current looper
		this.handler = new Handler();
	}
	
	public void onResume() 
	{
        super.onResume();
        // Notify comScore about lifecycle usage
        comScore.onEnterForeground();
	}
	@Override
	public void onPause() 
	{
		super.onPause();
		// Notify comScore about lifecycle usage
		comScore.onExitForeground();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
	    getMenuInflater().inflate(R.menu.main, menu);
	    MenuItem searchItem = menu.findItem(R.id.action_search);
	    SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
	    
	    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String text)
			{
				Log.d("Query", "Submit: " + text);
				
				promptSearch(text);
				
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String text)
			{
				Log.d("Query", "Change: " + text);
				
				if(text != null && !text.equalsIgnoreCase(searchKeyword))
				{
					promptSearch(text);
				}
				
				return false;
			}
		});
	    
	    // Listen for search UI open/close events
	    MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener()
		{
			@Override
			public boolean onMenuItemActionExpand(MenuItem item)
			{
				// Lock usage of drawer
				getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
				
				// Show empty list fragment UI
				Category searchCategory = new Category();
				searchCategory.setId(VideoListUtilities.VIDEOLIST_SEARCH);
				// Use name as search keyword container
				searchCategory.setName("");
				
				// Display search
				showCategory(searchCategory, true);
				
				return true;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item)
			{
				// Unlock usage of drawer
				getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
				
				// restore last category
				showCategory(getCategoryNavigationDrawerFragment().getActiveCategory(), true);
				
				return true;
			}
		});
	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		boolean drawerOpen = false;
		boolean drawerReady = false;
		
		if(categoryNavigationDrawerFragment != null)
		{
			drawerOpen = categoryNavigationDrawerFragment.isDrawerOpen();
			drawerReady = categoryNavigationDrawerFragment.isCategoriesLoaded();
		}
		
        menu.findItem(R.id.action_search).setVisible(!drawerOpen && drawerReady);
        return super.onPrepareOptionsMenu(menu);
	}
	
	
	/***************************************
	 * ???
	 */
	
	private static boolean isInfiniteScrollRequiredForCategory(String identifier)
	{
		boolean required = true;
		
		if(VideoListUtilities.VIDEOLIST_HEADLINE.equalsIgnoreCase(identifier) || VideoListUtilities.VIDEOLIST_HOMEPAGE.equalsIgnoreCase(identifier))
		{
			required = false;
		}
		
		return required;
	}
	
	private synchronized void showCategory(final Category category, final boolean reloadOnConflict)
	{
		CategoryNavigationDrawerFragment fragment = getCategoryNavigationDrawerFragment();
		
		Category activeCategory = (fragment == null)?(null):(fragment.getActiveCategory());
		
		if(reloadOnConflict || activeCategory == null || (activeCategory != null && !activeCategory.equals(category)))
		{
			Log.d("FOO", "Will show:" + category);
			
			setTitle(category.getName());
			loadVideoList(category, isInfiniteScrollRequiredForCategory(category.getId()), reloadOnConflict);
		}
	}
	
	private void loadVideoList(final Category category, boolean infiniteScrollEnabled, boolean reloadOnConflict)
	{
		if(reloadOnConflict || getSupportFragmentManager().findFragmentByTag(category.getId()) == null)
		{
			// Create new fragment and transaction
			Fragment videoListFragment = null;
			
			if(VideoListUtilities.VIDEOLIST_FAVOURITES.equalsIgnoreCase(category.getId()))
			{
				videoListFragment = FavouriteVideoListFragment.newInstance();
			}
			else
			{
				videoListFragment = VideoListFragment.newInstance(category, infiniteScrollEnabled);
			}
			
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_placeholder view with this fragment,
			// and add the transaction to the back stack
			transaction.replace(R.id.main_fragmentplaceholder, videoListFragment, category.getId());
			//transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commitAllowingStateLoss();
		}
	}
	
	private void promptSearch(String string)
	{
		// Cancel any waiting search operations
		this.handler.removeCallbacks(searchRunnable);
		
		// Update search keyword
		this.searchKeyword = string;
		
		// Wait for search to perform
		this.handler.postDelayed(searchRunnable, SEARCH_TRIGGER_TIMEOUT);
	}
	
	private void performSearch(String string)
	{
		if(!TextUtils.isEmpty(string) && string.length() > SEARCH_KEYWORD_MINCHARACTERCOUNT)
		{
			// Create search category
			// Yeah, it might seem weird at first. 
			// As we are searching video list,
			// category class is the best fit for a container...
			Category searchCategory = new Category();
			searchCategory.setId(VideoListUtilities.VIDEOLIST_SEARCH);
			// Use name as search keyword container
			searchCategory.setName(string);
			
			// Display search
			showCategory(searchCategory, true);
		}
		else
		{
			// TODO: Display empty UI with requirements
		}
	}
	
	
	/***************************************
	 * Accessors
	 */
	
	private void setDrawerLayout(DrawerLayout drawerLayout)
	{
		if(drawerLayout == null)
		{
			this.drawerLayoutWeak = null;
		}
		else
		{
			this.drawerLayoutWeak = new WeakReference<DrawerLayout>(drawerLayout);
		}
	}
	
	private DrawerLayout getDrawerLayout()
	{
		return (this.drawerLayoutWeak == null)?(null):(this.drawerLayoutWeak.get());
	}
	
	private CategoryNavigationDrawerFragment getCategoryNavigationDrawerFragment()
	{
		if(this.categoryNavigationDrawerFragment == null)
		{
			this.categoryNavigationDrawerFragment = (CategoryNavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.main_categorynavigationdrawerfragment);
		}
		
		return this.categoryNavigationDrawerFragment;
	}
}