package com.skorer.tv.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.skorer.tv.DatabaseHelper;
import com.skorer.tv.R;
import com.skorer.tv.SkorerTVApplication;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class VideoListCursorAdapter extends SimpleCursorAdapter
{
	public static final int RESOURCEID_ROWLAYOUT = R.layout.videolist_row;
	
	public VideoListCursorAdapter(Context context, Cursor c)
	{
		super(context, RESOURCEID_ROWLAYOUT, c, new String[] {DatabaseHelper.Table.VideoClip.COLUMN_TITLE}, new int[] {R.id.videolist_row_titletextview}, 0);
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		// Allocate & initialize new layout
		LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup rootViewGroup = (ViewGroup) inflator.inflate(RESOURCEID_ROWLAYOUT, parent, false);
		
		// Attach a view holder class, for performance improvements
		ViewHolder holder = new ViewHolder();
		holder.titleTextView = (TextView) rootViewGroup.findViewById(R.id.videolist_row_titletextview);
		holder.previewImageView = (NetworkImageView) rootViewGroup.findViewById(R.id.videolist_row_previewimageview);
		rootViewGroup.setTag(holder);
		
		int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_ID);
		int titleColumnIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_TITLE);
		int imageUrlIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_IMAGEURL);
		
		String id = cursor.getString(idColumnIndex);
		String title = cursor.getString(titleColumnIndex);
		String imageUrl = cursor.getString(imageUrlIndex);
		
		holder.id = id;
		
		if(holder.titleTextView != null)
		{
			holder.titleTextView.setText(title);
		}
		
		if(holder.previewImageView != null)
		{
			holder.previewImageView.setImageUrl(imageUrl, SkorerTVApplication.imageLoader);
		}
		
		return rootViewGroup;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		ViewHolder holder = (ViewHolder) view.getTag();
		
		int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_ID);
		int titleColumnIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_TITLE);
		int imageUrlIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_IMAGEURL);
		
		String id = cursor.getString(idColumnIndex);
		String title = cursor.getString(titleColumnIndex);
		String imageUrl = cursor.getString(imageUrlIndex);
		
		holder.id = id;
		
		if(holder.titleTextView != null)
		{
			holder.titleTextView.setText(title);
		}
		
		if(holder.previewImageView != null)
		{
			holder.previewImageView.setImageUrl(imageUrl, SkorerTVApplication.imageLoader);
		}
	}
	
	
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent)
//	{
//		ViewGroup rootViewGroup = (ViewGroup) convertView;
//		
//		// If no pre-initialized template provided
//		if(rootViewGroup == null)
//		{
//			// Allocate & initialize new layout
//			LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			rootViewGroup = (ViewGroup) inflator.inflate(RESOURCEID_ROWLAYOUT, parent, false);
//			
//			// Attach a view holder class, for performance improvements
//			ViewHolder holder = new ViewHolder();
//			holder.titleTextView = (TextView) rootViewGroup.findViewById(R.id.videolist_row_titletextview);
//			holder.previewImageView = (NetworkImageView) rootViewGroup.findViewById(R.id.videolist_row_previewimageview);
//			rootViewGroup.setTag(holder);
//		}
//		
//		VideoClip videoClip = dataSet.get(position);
//		ViewHolder viewHolder = (ViewHolder) rootViewGroup.getTag();
//		viewHolder.id = videoClip.getId();
//		
//		if(viewHolder.titleTextView != null)
//		{
//			viewHolder.titleTextView.setText(videoClip.getTitle());
//		}
//		
//		if(viewHolder.previewImageView != null)
//		{
//			viewHolder.previewImageView.setImageUrl(videoClip.getImageUrl(), MilliyetTVApplication.imageLoader);
//		}
//		
//		return rootViewGroup;
//	}
	
	public static class ViewHolder
	{
		public TextView titleTextView = null;
		public NetworkImageView previewImageView = null;
		public String id = null;
	}
}
