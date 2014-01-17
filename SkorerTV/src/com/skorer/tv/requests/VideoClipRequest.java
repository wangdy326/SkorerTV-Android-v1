package com.skorer.tv.requests;

import org.json.JSONObject;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class VideoClipRequest extends JsonObjectRequest
{
	private static final String VIDEOCLIP_REQUESTURL = "http://mw.milliyet.com.tr/ashx/Milliyet.ashx?aType=MobileAPI_VideoClipByID&VideoClipID=";
	
	public static VideoClipRequest newInstance(final String videoId, final Listener<JSONObject> listener, final ErrorListener errorListener)
	{
		VideoClipRequest request = new VideoClipRequest(Request.Method.GET, VIDEOCLIP_REQUESTURL + videoId, null, listener, errorListener);
		
		request.setRetryPolicy(new DefaultRetryPolicy(
                20000, 
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		
		return request;
	}
	
	private VideoClipRequest(int method, String url, JSONObject jsonRequest,
			Listener<JSONObject> listener, ErrorListener errorListener)
	{
		super(method, url, jsonRequest, listener, errorListener);
	}
}
