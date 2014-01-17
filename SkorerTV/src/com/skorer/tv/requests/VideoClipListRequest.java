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
public class VideoClipListRequest extends JsonObjectRequest
{
	private static final String VIDEOCLIPLIST_HEADLINE_REQUESTURL = "http://mw.milliyet.com.tr/ashx/Milliyet.ashx?aType=MobileAPI_SkorerTVHeadlines";
	private static final String VIDEOCLIPLIST_HOMEPAGE_REQUESTURL = "http://mw.milliyet.com.tr/ashx/Milliyet.ashx?aType=MobileAPI_SkorerTVHomePage";
	private static final String VIDEOCLIPLIST_CATEGORY_REQUESTBASEURL = "http://mw.milliyet.com.tr/ashx/Milliyet.ashx?aType=MobileAPI_SkorerTVVideoClips";
	private static final String VIDEOCLIPLIST_SEARCH_REQUESTBASEURL = "http://mw.milliyet.com.tr/ashx/Milliyet.ashx?aType=MobileAPI_SkorerTVSearch";
	
	public static VideoClipListRequest newInstance(String url, Listener<JSONObject> listener, ErrorListener errorListener)
	{
		VideoClipListRequest request = new VideoClipListRequest(Request.Method.GET, url, null, listener, errorListener);
		
		return request;
	}
	
	private VideoClipListRequest(int method, String url, JSONObject jsonRequest,
			Listener<JSONObject> listener, ErrorListener errorListener)
	{
		super(method, url, jsonRequest, listener, errorListener);
	}
	
	public static final String getHeadlineRequestUrl()
	{
		return VIDEOCLIPLIST_HEADLINE_REQUESTURL;
	}
	
	public static final String getHomepageRequestUrl()
	{
		return VIDEOCLIPLIST_HOMEPAGE_REQUESTURL;
	}
	
	public static final String getSearchVideoListRequestUrl(final String queryString, final int pageIndex, final int pageSize)
	{
		return Uri.parse(VIDEOCLIPLIST_SEARCH_REQUESTBASEURL)
				  .buildUpon()
				  .appendQueryParameter("Query", queryString)
				  .appendQueryParameter("PageIndex", String.valueOf(pageIndex))
				  .appendQueryParameter("PageSize", String.valueOf(pageSize))
				  .build()
				  .toString();
	}
	
	public static final String getCategoryVideoListRequestUrl(final String categoryId, final int pageIndex, final int pageSize)
	{
		return Uri.parse(VIDEOCLIPLIST_CATEGORY_REQUESTBASEURL)
				  .buildUpon()
				  .appendQueryParameter("CategoryID", categoryId)
				  .appendQueryParameter("PageIndex", String.valueOf(pageIndex))
				  .appendQueryParameter("PageSize", String.valueOf(pageSize))
				  .build()
				  .toString();
		
//		return new StringBuffer(VIDEOCLIPLIST_CATEGORY_REQUESTBASEURL).append("&CategoryID=")
//																	  .append(category)
//																	  .append("&PageIndex=")
//																	  .append(pageIndex)
//																	  .append("&PageSize=")
//																	  .append(pageSize)
//																	  .toString();
	}
}
