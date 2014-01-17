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
public class VideoListCountRequest extends JsonObjectRequest
{
	private static final String VIDEOCLIPLIST_COUNT_REQUESTBASEURL = "http://mw.milliyet.com.tr/ashx/Milliyet.ashx?aType=MobileAPI_SkorerTVVideoClipsCount&CategoryID=";
	private static final String VIDEOCLIPLIST_COUNT_SEARCHREQUESTBASEURL = "http://mw.milliyet.com.tr/ashx/Milliyet.ashx?aType=MobileAPI_SkorerTVSearchCount";
	
	public static VideoListCountRequest newInstance(String url, Listener<JSONObject> listener, ErrorListener errorListener)
	{	
		VideoListCountRequest request = new VideoListCountRequest(Request.Method.GET, url, null, listener, errorListener);
		
		return request;
	}
	
	private VideoListCountRequest(int method, String url, JSONObject jsonRequest,
			Listener<JSONObject> listener, ErrorListener errorListener)
	{
		super(method, url, jsonRequest, listener, errorListener);
	}
	
	public static final String getCountRequestUrl(String identifier)
	{
		return VIDEOCLIPLIST_COUNT_REQUESTBASEURL + identifier;
	}
	
	public static final String getSearchCountRequestUrl(String queryString)
	{
		return Uri.parse(VIDEOCLIPLIST_COUNT_SEARCHREQUESTBASEURL)
				  .buildUpon()
				  .appendQueryParameter("Query", queryString)
				  .build()
				  .toString();
	}
}
