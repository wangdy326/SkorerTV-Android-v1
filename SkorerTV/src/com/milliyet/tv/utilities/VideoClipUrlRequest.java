package com.milliyet.tv.utilities;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class VideoClipUrlRequest extends StringRequest
{
	private static final String REQUEST_BASEURL = "http://video.milliyet.com.tr/d/h/IosMobile.ashx?VideoCode=";

	public static VideoClipUrlRequest newInstance(final String videoCode, final Listener<String> listener, final ErrorListener errorListener)
	{
		VideoClipUrlRequest request = new VideoClipUrlRequest(Request.Method.GET, getRequestUrl(videoCode), listener, errorListener);
		
		return request;
	}
	
	public VideoClipUrlRequest(int method, String url,
			Listener<String> listener, ErrorListener errorListener)
	{
		super(method, url, listener, errorListener);
		// TODO Auto-generated constructor stub
	}
	
	private static String getRequestUrl(final String videoCode)
	{
		return REQUEST_BASEURL + videoCode;
	}
}
