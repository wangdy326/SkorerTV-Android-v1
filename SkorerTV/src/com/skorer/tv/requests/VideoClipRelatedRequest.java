package com.skorer.tv.requests;

import org.json.JSONObject;

import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class VideoClipRelatedRequest extends JsonObjectRequest
{
	private static final String VIDEOCLIPRELATED_BASEREQUESTURL = "http://mw.milliyet.com.tr/ashx/Milliyet.ashx?aType=MobileAPI_SimilarVideoClipsByID";
	
	public static VideoClipRelatedRequest newInstance(final String videoId, final Listener<JSONObject> listener, final ErrorListener errorListener)
	{
		String url = getVideoClipRelatedRequestUrl(videoId);
		
		VideoClipRelatedRequest request = new VideoClipRelatedRequest(Request.Method.GET, url, null, listener, errorListener);
		
		return request;
	}
	
	public VideoClipRelatedRequest(int method, String url,
			JSONObject jsonRequest, Listener<JSONObject> listener,
			ErrorListener errorListener)
	{
		super(method, url, jsonRequest, listener, errorListener);
	}
	
	public static final String getVideoClipRelatedRequestUrl(final String videoId)
	{	
		return Uri.parse(VIDEOCLIPRELATED_BASEREQUESTURL)
				  .buildUpon()
				  .appendQueryParameter("VideoClipID", videoId)	
				  .appendQueryParameter("PageIndex", "0")
				  .appendQueryParameter("PageSize", "4")
				  .build()
				  .toString();
	}
}
