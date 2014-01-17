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
public class VideoClipVoteRequest extends JsonObjectRequest
{
	public static final String VIDEOCLIPVOTE_RESPONSE_ALERTMESSAGE_KEY = "AlertMessage";
	public static final String VIDEOCLIPVOTE_RESPONSE_VOTESUCCEED_IDENTIFIER = "Oyunuz ba";
	
	private static final String VIDEOCLIPVOTE_BASEREQUESTURL = "http://www.milliyet.tv/d/h/GenericHandler.ashx?hCase=LikeVideo";
	
	public static VideoClipVoteRequest newInstance(final String videoId, final boolean like, final Listener<JSONObject> listener, final ErrorListener errorListener)
	{
		String url = getVideoClipVoteRequestUrl(videoId, like);
		
		VideoClipVoteRequest request = new VideoClipVoteRequest(Request.Method.GET, url, null, listener, errorListener);
		
		return request;
	}
	
	public VideoClipVoteRequest(int method, String url, JSONObject jsonRequest,
			Listener<JSONObject> listener, ErrorListener errorListener)
	{
		super(method, url, jsonRequest, listener, errorListener);
	}
	
	public static final String getVideoClipVoteRequestUrl(final String videoId, final boolean like)
	{	
		// Logic error fix. Blame server-side!
		boolean notLike = !like;
		
		return Uri.parse(VIDEOCLIPVOTE_BASEREQUESTURL)
				  .buildUpon()
				  .appendQueryParameter("isPositive", (notLike)?("false"):("true"))	
				  .appendQueryParameter("VideoID", videoId)
				  .build()
				  .toString();
	}
}
