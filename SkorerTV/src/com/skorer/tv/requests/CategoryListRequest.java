package com.skorer.tv.requests;

import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class CategoryListRequest extends JsonObjectRequest
{
	private static final String CATEGORYLIST_REQUESTURL = "http://mw.milliyet.com.tr/ashx/Milliyet.ashx?aType=MobileAPI_SkorerTVCategories";
	
	public static CategoryListRequest newInstance(Listener<JSONObject> listener, ErrorListener errorListener)
	{
		CategoryListRequest request = new CategoryListRequest(Request.Method.GET, CATEGORYLIST_REQUESTURL, null, listener, errorListener);
		
		return request;
	}
	
	private CategoryListRequest(int method, String url, JSONObject jsonRequest,
			Listener<JSONObject> listener, ErrorListener errorListener)
	{
		super(method, url, jsonRequest, listener, errorListener);
	}
	
}
