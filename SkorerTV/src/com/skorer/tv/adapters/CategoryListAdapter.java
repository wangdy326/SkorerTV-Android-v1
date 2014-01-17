package com.skorer.tv.adapters;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.skorer.tv.R;
import com.skorer.tv.model.Category;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class CategoryListAdapter extends ArrayAdapter<Category>
{
	public static final int RESOURCEID_ROWLAYOUT = R.layout.categorylist_row;
	
	private List<Category> dataSet = null;
	
	
	public CategoryListAdapter(Context context,	List<Category> objects)
	{
		super(context, RESOURCEID_ROWLAYOUT, objects);
		
		this.dataSet = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewGroup rootViewGroup = (ViewGroup) convertView;
		
		// If no pre-initialized template provided
		if(rootViewGroup == null)
		{
			// Allocate & initialize new layout
			LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rootViewGroup = (ViewGroup) inflator.inflate(RESOURCEID_ROWLAYOUT, parent, false);
			
			// Attach a view holder class, for performance improvements
			ViewHolder holder = new ViewHolder();
			holder.nameTextView = (TextView) rootViewGroup.findViewById(R.id.categorylist_row_titletextview);
			rootViewGroup.setTag(holder);
		}
		
		Category category = dataSet.get(position);
		ViewHolder viewHolder = (ViewHolder) rootViewGroup.getTag();
		
		if(viewHolder.nameTextView != null)
		{
			viewHolder.nameTextView.setText(category.getName());
		}
		
//		if(((ListView) parent).getCheckedItemPosition() == position)
//		{
//			rootViewGroup.setSelected(true);
//			
//			Log.d("BAZ", "Checked Selected");
//		}
//		else
//		{
//			rootViewGroup.setSelected(false);
//			
//			Log.d("BAZ", "Checked -");
//		}
		
		Log.d("ASDF", "Checked item: " + ((ListView) parent).getCheckedItemPosition());
		
		return rootViewGroup;
	}
	
	private static final class ViewHolder
	{
		public TextView nameTextView = null;
	}
}
