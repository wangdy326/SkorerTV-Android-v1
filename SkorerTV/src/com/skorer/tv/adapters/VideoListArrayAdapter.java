package com.skorer.tv.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.skorer.tv.R;
import com.skorer.tv.SkorerTVApplication;
import com.skorer.tv.adapters.VideoListCursorAdapter.ViewHolder;
import com.skorer.tv.model.VideoClip;

public class VideoListArrayAdapter extends ArrayAdapter<VideoClip>
{
	public static final int RESOURCEID_ROWLAYOUT = R.layout.videolist_row;
	public List<VideoClip> dataSet = null;
	
	public VideoListArrayAdapter(Context context, List<VideoClip> objects)
	{
		super(context, RESOURCEID_ROWLAYOUT, objects);
		
		dataSet = objects;
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
			holder.titleTextView = (TextView) rootViewGroup.findViewById(R.id.videolist_row_titletextview);
			holder.previewImageView = (NetworkImageView) rootViewGroup.findViewById(R.id.videolist_row_previewimageview);
			rootViewGroup.setTag(holder);
		}
		
		VideoClip videoClip = dataSet.get(position);
		ViewHolder viewHolder = (ViewHolder) rootViewGroup.getTag();
		viewHolder.id = videoClip.getId();
		
		if(viewHolder.titleTextView != null)
		{
			viewHolder.titleTextView.setText(videoClip.getTitle());
		}
		
		if(viewHolder.previewImageView != null)
		{
			viewHolder.previewImageView.setImageUrl(videoClip.getImageUrl(), SkorerTVApplication.imageLoader);
		}
		
		return rootViewGroup;
	}
}
