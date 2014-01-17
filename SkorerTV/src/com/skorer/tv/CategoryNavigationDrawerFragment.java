package com.skorer.tv;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.milliyet.tv.utilities.CategoryUtilities;
import com.milliyet.tv.utilities.VideoListUtilities;
import com.skorer.tv.R;
import com.skorer.tv.adapters.CategoryListAdapter;
import com.skorer.tv.listeners.OnJobDoneListener;
import com.skorer.tv.model.Category;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class CategoryNavigationDrawerFragment extends Fragment
{
	/***************************************
	 * Log
	 */
	
	private static final boolean LOG = true;
	private static final String TAG = "Category navigation drawer";
	
	/***************************************
	 * Variables
	 */
	
	// Remember position of the selected item.
	private static final String INSTANCESTATE_ACTIVECATEGORY_KEY = "navigationdrawer.state.activecategory";
	
	// Navigation drawer's shared preferences file name
	private static final String PREFERENCE_NAME = "navigationdrawer.preference";
	
	// Tracker for user awareness of navigation drawer.
	// Drawer will be shown at launch, till user expands it manually.
	private static final String PREFERENCE_USERLEARNEDDRAWER_KEY = "navigationdrawer.preference.userlearned";
	
	// Callback listener for category selection events.
	private OnCategorySelectedListener onCategorySelectedListener = null;
	
	// Callback listener for end of category load operation event.
	private OnCategoriesLoadedListener onCategoriesLoadedListener = null;
	
	// Helper component that ties the action bar to the navigation drawer.
	private ActionBarDrawerToggle drawerToggle = null;
	
	private WeakReference<DrawerLayout> drawerLayoutWeak = null;
	private WeakReference<ListView> categoryListViewWeak = null;
	private WeakReference<View> fragmentContainerViewWeak = null;
	
	// Selected item instance pointer
	private Category activeCategory = null;
	private boolean fromSavedInstanceState = false;
	
	/***************************************
	 * Constructors & instance providers
	 */

	public static CategoryNavigationDrawerFragment newInstance()
	{
		CategoryNavigationDrawerFragment fragment = new CategoryNavigationDrawerFragment();
		
		
		return fragment;
	}
	
	public CategoryNavigationDrawerFragment() {}
	
	
	/***************************************
	 * Fragment state handlers
	 */
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null)
		{
            activeCategory = savedInstanceState.getParcelable(INSTANCESTATE_ACTIVECATEGORY_KEY);
            fromSavedInstanceState = true;
        }
		
		// Indicate that this fragment would like to influence the set of actions in the action bar.
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_categorynavigationdrawer, null, false);
		
		ListView categoryListView = (ListView) rootView.findViewById(R.id.categorynavigationdrawer_listview);
		setCategoryListView(categoryListView);
		
		if(categoryListView != null)
		{
			categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id)
				{
					selectCategory(position);
				}
			});
		}
		
		return rootView;
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		// Sync the toggle state
        drawerToggle.syncState();
        
        loadCategories();
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		
		
//		if(this.onCategorySelectedListener == null && 
//		   !(activity instanceof OnCategorySelectedListener) && 
//		   !(this instanceof OnCategorySelectedListener))
//		{
//			throw new ClassCastException("Someone must implement OnCategorySelectedListener... Seriously!");
//		}
	}
	
	@Override
	public void onDetach()
	{
		super.onDetach();
		
		// Clear callback mechanism on detach,
		// as it might be a context subclass instance (e.g Activity)
		this.onCategorySelectedListener = null;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		
		// Save active category
		outState.putParcelable(INSTANCESTATE_ACTIVECATEGORY_KEY, this.activeCategory);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		
		// Forward new configuration to the drawer toggle component.
		this.drawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// If drawer is open, show the global actions
		if(isDrawerOpen())
		{
			// No global action defined for Milliyet TV
			//inflater.inflate(R.menu.global, menu);
			showGlobalContextActionBar();
		}
		
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		DrawerLayout drawerLayout = getDrawerLayout();
		boolean drawerUnlocked = drawerLayout.getDrawerLockMode(Gravity.LEFT) == DrawerLayout.LOCK_MODE_UNLOCKED;
		
		if(drawerUnlocked && drawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		
		switch (item.getItemId())
		{
			default:
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout)
    {
        setFragmentContainerView(getActivity().findViewById(fragmentId));
        setDrawerLayout(drawerLayout);

        // set a custom shadow that overlays the main content when the drawer opens
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        this.drawerToggle = new ActionBarDrawerToggle(
                getActivity(),         /* host Activity */
                drawerLayout,          /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
                
                if(activeCategory != null)
                {
                	 getActionBar().setTitle(activeCategory.getName());
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                // The user manually opened the drawer; store this flag to prevent auto-showing
                // the navigation drawer automatically in the future.
                setUserLearnedDrawer(true);
            }
            
            @Override
            public void onDrawerStateChanged(int newState)
            {
            	super.onDrawerStateChanged(newState);
            	
            	switch (newState)
				{
					case DrawerLayout.STATE_IDLE:
					{
						ActionBarActivity activity = (ActionBarActivity) getActivity();
						if (activity != null) activity.supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
						
						break;
					}
					case DrawerLayout.STATE_DRAGGING:
					{
						ActionBarActivity activity = (ActionBarActivity) getActivity();
						if (activity != null) activity.supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
						
						break;
					}
					default:
						break;
				}
            }
        };

        // Defer code dependent on restoration of previous instance state.
        drawerLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                drawerToggle.syncState();
            }
        });

        drawerLayout.setDrawerListener(this.drawerToggle);
    }
	
	/***************************************
	 * ???
	 */
	
	private void loadCategories()
	{
		CategoryUtilities.sharedInstance().fetchCategoriesAsynch(SkorerTVApplication.requestQueue, new OnJobDoneListener<List<Category>>()
		{
			@Override
			public void onJobDone(int status, final List<Category> result)
			{
				switch (status)
				{
					case JobStatus.SUCCEED:
					{
						//Log.d("ASDF", "received: " + result.toString());
						
						// Acquire current fragment reference...
						CategoryNavigationDrawerFragment fragment = CategoryNavigationDrawerFragment.this;
						
						// If fragment is not still available
						if(fragment != null)
						{
							// Add static categories to the top of the list
							Category category = new Category();
							category.setId(VideoListUtilities.VIDEOLIST_HEADLINE);
							category.setName("Ana Sayfa");
							
							result.add(0, category);
							result.add(1, FavouriteVideoListFragment.getFavouritesCategory());
							fragment.updateCategoryList(result);
						}
						
						break;
					}
					default:
					{
						Log.d(TAG, "Failed category request");
						
						CategoryNavigationDrawerFragment fragment = CategoryNavigationDrawerFragment.this;
						
						// If fragment is not still available
						if(fragment != null)
						{
							fragment.showErrorUI("Beklenmedik bir sorun oluştu. Tekrar denemek için tamam'a tıklayın.");
							
							Log.d(TAG, "We got a problem!");
						}
						
						break;
					}
				}
			}
		});
	}
	
	private void updateCategoryList(final List<Category> categoryList)
	{
		// Try finding category list view
		ListView categoryListView = getCategoryListView();
		
		// If we found category list view
		if(categoryListView != null)
		{
			categoryListView.post(new Runnable()
			{
				@Override
				public void run()
				{
					// Acquire current fragment reference...
					CategoryNavigationDrawerFragment fragment = CategoryNavigationDrawerFragment.this;
					
					// If fragment is not still available
					if(fragment != null && fragment.isAdded())
					{
						// Display category items
						
						// Try finding category list view
						ListView categoryListView = fragment.getCategoryListView();
						
						// If we found category list view
						if(categoryListView != null)
						{
							// Allocate & initialize new category list adapter
							CategoryListAdapter adapter = new CategoryListAdapter(fragment.getActivity(), categoryList);
							
							// Attach adapter to list view
							categoryListView.setAdapter(adapter);
							
							// Ensure visibility of list view
							categoryListView.setVisibility(View.VISIBLE);
						}
						
						if(fragment.activeCategory == null)
						{
							fragment.activeCategory = categoryList.get(0);
						}
						
						// Unlock drawer, as it is ready for user now.
						DrawerLayout drawerLayout = getDrawerLayout();
						
						if(drawerLayout != null)
						{
							drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
						}
						
						// enable ActionBar app icon to behave as action to toggle nav drawer
				        ActionBar actionBar = getActionBar();
				        actionBar.setDisplayHomeAsUpEnabled(true);
				        actionBar.setHomeButtonEnabled(true);
						
						// If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
				        // per the navigation drawer design guidelines.
				        if (!isUserLearnedDrawer() && !CategoryNavigationDrawerFragment.this.fromSavedInstanceState)
				        {
				            drawerLayout.openDrawer(getFragmentContainerView());
				        }
						
				        selectCategory(activeCategory, false);
						
						if(fragment.onCategoriesLoadedListener != null)
						{
							fragment.onCategoriesLoadedListener.onCategoriesLoaded();
						}
					}
				}
			});
		}
		else
		{
			Log.d("BUG", "Unable to find list view for categories");
		}
	}
	
	private void selectCategory(int position)
	{
		ListView listView = getCategoryListView();
		
		boolean alreadySelected = false;
		
		if(listView != null)
		{
			// Update active category item
			ListAdapter adapter = listView.getAdapter();
			
			if(adapter != null)
			{
				// Get selected category
				Category selectedCategory = (Category) adapter.getItem(position);
				alreadySelected = selectedCategory.equals(activeCategory);
				this.activeCategory = selectedCategory;
			}
			
			// Mark list item as checked
			listView.setItemChecked(position, true);
		}
		
		// Close drawer
		DrawerLayout drawerLayout = getDrawerLayout();
		
		if(drawerLayout != null)
		{
			drawerLayout.closeDrawer(getFragmentContainerView());
		}
		
		// Perform callback
		if(this.onCategorySelectedListener != null)
		{
			this.onCategorySelectedListener.onCategorySelected(this.activeCategory, alreadySelected);
		}
	}
	
	private void selectCategory(Category category, boolean performCallback)
	{
		ListView listView = getCategoryListView();
		
		// Check if category already selected
		boolean alreadySelected = category.equals(this.activeCategory);
		// Mark given category as active
		this.activeCategory = category;
		
		if(listView != null)
		{
			// Update active category item
			ListAdapter adapter = listView.getAdapter();
			
			if(adapter != null)
			{
				// Search for category's position by iterating over data-set
				// Get category quantity
				int quantity = adapter.getCount();
				int position = -1;
				
				for (int i = 0; i < quantity; i++)
				{
					if(category.equals(adapter.getItem(i)))
					{
						position = i;
						break;
					}
				}
				
				// If found category position
				if(position >= 0)
				{
					// Mark list item as checked
					listView.setItemChecked(position, true);
				}
			}
			
			// ???: Should we close drawer here?
		}
		
		// Close drawer
		DrawerLayout drawerLayout = getDrawerLayout();
		
		if(drawerLayout != null)
		{
			drawerLayout.closeDrawer(getFragmentContainerView());
		}
		
		// Perform callback
		if(performCallback && this.onCategorySelectedListener != null)
		{
			this.onCategorySelectedListener.onCategorySelected(this.activeCategory, alreadySelected);
		}
	}
	
	/**
	 * Show global context actions only
	 */
    private void showGlobalContextActionBar()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar()
    {
        return ((ActionBarActivity)getActivity()).getSupportActionBar();
    }
    
    private void showErrorUI(final String errorMessage)
    {
    	final Activity activity = getActivity();
    	
    	if(activity != null)
    	{
    		activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					new AlertDialog.Builder(activity).setMessage(errorMessage).setNeutralButton("Tamam", new DialogInterface.OnClickListener()
		    		{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							loadCategories();
						}
		    		}).setCancelable(false).show();
				}
			});
    	}
    }
	
	
	/***************************************
	 * Accessors
	 */
	
	/**
	 * Setter method for user awareness of navigation drawer.
	 * See PREFERENCE_USERLEARNEDDRAWER_KEY for more details
	 * 
	 * @param learned If user available, you should provide true. Otherwise, false.
	 */
	private void setUserLearnedDrawer(boolean learned)
	{
		Activity activity = getActivity();
		
		// If our fragment possess valid activity
		if(activity != null)
		{
			SharedPreferences preferences = activity.getSharedPreferences(PREFERENCE_NAME,
																		  Context.MODE_PRIVATE);
			preferences.edit()
					   .putBoolean(PREFERENCE_USERLEARNEDDRAWER_KEY, learned)
					   .apply();
		}
		else if(LOG)
		{
			Log.d(TAG, "Unable to perform changes. Activity not assigned for this fragment, yet!");
		}
	}
	
	/**
	 * Getter method for user awareness of navigation drawer.
	 * See PREFERENCE_USERLEARNEDDRAWER_KEY for more details
	 * 
	 * @return If user learned true, otherwise false.
	 */
	private boolean isUserLearnedDrawer()
	{
		boolean userLearned = false;
		
		Activity activity = getActivity();
		
		// If our fragment possess valid activity
		if(activity != null)
		{
			SharedPreferences preferences = activity.getSharedPreferences(PREFERENCE_NAME,
																		  Context.MODE_PRIVATE);
			userLearned = preferences.getBoolean(PREFERENCE_USERLEARNEDDRAWER_KEY, false);
		}
		else if(LOG)
		{
			Log.d(TAG, "Unable to perform changes. Activity not assigned for this fragment, yet!");
		}
		
		return userLearned;
	}
	
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
	
	private void setCategoryListView(ListView listView)
	{
		if(listView == null)
		{
			this.categoryListViewWeak = null;
		}
		else
		{
			this.categoryListViewWeak = new WeakReference<ListView>(listView);
		}
	}
	
	private ListView getCategoryListView()
	{
		return (this.categoryListViewWeak == null)?(null):(this.categoryListViewWeak.get());
	}
	
	private void setFragmentContainerView(View view)
	{
		if(view == null)
		{
			this.fragmentContainerViewWeak = null;
		}
		else
		{
			this.fragmentContainerViewWeak = new WeakReference<View>(view);
		}
	}
	
	private View getFragmentContainerView()
	{
		return (this.fragmentContainerViewWeak == null)?(null):(this.fragmentContainerViewWeak.get());
	}
	
	public boolean isDrawerOpen()
	{
		DrawerLayout drawerLayout = getDrawerLayout();
		
		return (drawerLayout != null) && (drawerLayout.isDrawerOpen(getFragmentContainerView()));
	}
	
	/**
	 * Returns whether categories loaded or not.
	 * 
	 * @return true on categories loadeed. otherwise, false.
	 */
	public boolean isCategoriesLoaded()
	{
		return (this.activeCategory != null);
	}
	
	public Category getActiveCategory()
	{
		return this.activeCategory;
	}
	
	public void setOnCategorySelectedListener(OnCategorySelectedListener listener)
	{
		this.onCategorySelectedListener = listener;
	}
	
	public OnCategorySelectedListener getOnCategorySelectedListener()
	{
		return this.onCategorySelectedListener;
	}
	
	public void setOnCategoriesLoadedListener(OnCategoriesLoadedListener listener)
	{
		this.onCategoriesLoadedListener = listener;
	}
	
	public OnCategoriesLoadedListener getOnCategoriesLoadedListener()
	{
		return this.onCategoriesLoadedListener;
	}
	
	
	/***************************************
	 * Minions (Interfaces, classes, etc...)
	 */
	
	/**
	 * 
	 * <p>
	 * Callback interface for category changes. 
	 * Host activities should implement this,
	 * in order to respond category changes.
	 * </p>
	 * 
	 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com)
	 */
	public interface OnCategorySelectedListener
	{
		/**
		 * Called when new category selected on navigation drawer
		 * 
		 * @param category Selected Category class instance
		 * @param alreadySelected If user re-selected current active category, true. Otherwise, false.
		 */
		public void onCategorySelected(Category category, boolean alreadySelected);
	}
	
	/**
	 * <p>
	 * Callback interface for end of category load operation.
	 * This is useful on populating UI after categories ready, 
	 * as categories fetched over network.
	 * </p>
	 * 
	 * @author gokhanbarisaker
	 */
	public interface OnCategoriesLoadedListener
	{
		/**
		 * Called when categories are populated, and ready to use
		 */
		public void onCategoriesLoaded();
	}
}
