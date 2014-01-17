package com.skorer.tv.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.milliyet.tv.utilities.JSONUtilities;
import com.skorer.tv.DatabaseHelper;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class VideoClip implements Parcelable, Comparable<VideoClip>
{
	/****************************************
	 * JSON keys
	 */
	private static final String ID_KEY = "ID";
	private static final String CODE_KEY = "Code";
	private static final String TITLE_KEY = "Title";
	private static final String IMAGEURL_KEY = "ImageURL";
	private static final String THUMBURL_KEY = "ThumbURL";
	private static final String LINK_KEY = "Link";
	private static final String CATEGORY_KEY = "Category";
	private static final String CATEGORYID_KEY = "CategoryId";
	private static final String SPOT_KEY = "Spot";
	private static final String VIDEOURL_KEY = "VideoURL";
	private static final String PUBLISHTIME_KEY = "PublishTime";
	private static final String PUBLISHTIME_FORMAT = "dd.MM.yyyy HH:mm"; // 08.10.2013 15:03
	private static final String VIEWCOUNT_KEY = "ViewCount";
	private static final String COMMENTCOUNT_KEY = "CommentCount";
	private static final String POSITIVEVOTECOUNT_KEY = "PositiveVoteCount";
	private static final String NEGATIVEVOTECOUNT_KEY = "NegativeVoteCount";
	
	
	/****************************************
	 * Variables
	 */
	
	private String id = null;
	private String code = null;
	private String title = null;
	private String imageUrl = null;
	private String thumbUrl = null;
	private String link = null;
	private String category = null;
	private String categoryId = null;
	private String spot = null;
	private String videoUrl = null;
	private Date publishTime = null;
	private int viewCount = 0;
	private int commentCount = 0;
	private int positiveVoteCount = 0;
	private int negativeVoteCount = 0;
	
	
	/****************************************
	 * Constructors & Instance providers
	 */
	
	public static VideoClip fromJson(JSONObject json)
	{
		if(json != null)
		{
			return new VideoClip(json);
		}
		
		return null;
	}
	
	public static VideoClip fromCursor(Cursor cursor)
	{
		if(cursor != null)
		{
			return new VideoClip(cursor);
		}
		
		return null;
	}
	
	public VideoClip(JSONObject jsonObject)
	{
		if(jsonObject != null)
		{
			this.id = JSONUtilities.getJsonString(jsonObject, ID_KEY, null);
			this.code = JSONUtilities.getJsonString(jsonObject, CODE_KEY, null);
			this.title = JSONUtilities.getJsonString(jsonObject, TITLE_KEY, null);
			this.imageUrl = JSONUtilities.getJsonString(jsonObject, IMAGEURL_KEY, null);
			this.thumbUrl = JSONUtilities.getJsonString(jsonObject, THUMBURL_KEY, null);
			this.link = JSONUtilities.getJsonString(jsonObject, LINK_KEY, null);
			this.category = JSONUtilities.getJsonString(jsonObject, CATEGORY_KEY, null);
			this.categoryId = JSONUtilities.getJsonString(jsonObject, CATEGORYID_KEY, null);
			this.spot = JSONUtilities.getJsonString(jsonObject, SPOT_KEY, null);
			this.videoUrl = JSONUtilities.getJsonString(jsonObject, VIDEOURL_KEY, null);
			this.publishTime = parsePublishTime(jsonObject);
			this.viewCount = JSONUtilities.getJsonInteger(jsonObject, VIEWCOUNT_KEY, 0);
			this.commentCount = JSONUtilities.getJsonInteger(jsonObject, COMMENTCOUNT_KEY, 0);
			this.positiveVoteCount = JSONUtilities.getJsonInteger(jsonObject, POSITIVEVOTECOUNT_KEY, 0);
			this.negativeVoteCount = JSONUtilities.getJsonInteger(jsonObject, NEGATIVEVOTECOUNT_KEY, 0);
		}
	}
	
	public VideoClip(Cursor cursor)
	{
		if(cursor != null)
		{
			int idIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_ID);
			int codeIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_CODE);
			int titleIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_TITLE);
			int imageIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_IMAGEURL);
			int thumbIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_THUMBURL);
			int linkIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_LINK);
			int categoryIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_CATEGORY);
			int categoryIdIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_CATEGORYID);
			int spotIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_SPOT);
			int videoUrlIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_VIDEOURL);
			int publishTimeIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_PUBLISHTIME);
			int viewCountIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_VIEWCOUNT);
			int commentCountIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_COMMENTCOUNT);
			int positiveVoteCountIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_POSITIVEVOTECOUNT);
			int negativeVoteCountIndex = cursor.getColumnIndex(DatabaseHelper.Table.VideoClip.COLUMN_NEGATIVEVOTECOUNT);
			
			this.id = cursor.getString(idIndex);
			this.code = cursor.getString(codeIndex);
			this.title = cursor.getString(titleIndex);
			this.imageUrl = cursor.getString(imageIndex);
			this.thumbUrl = cursor.getString(thumbIndex);
			this.link = cursor.getString(linkIndex);
			this.category = cursor.getString(categoryIndex);
			this.categoryId = cursor.getString(categoryIdIndex);
			this.spot = cursor.getString(spotIndex);
			this.videoUrl = cursor.getString(videoUrlIndex);
			this.publishTime = new Date(cursor.getLong(publishTimeIndex));
			this.viewCount = cursor.getInt(viewCountIndex);
			this.commentCount = cursor.getInt(commentCountIndex);
			this.positiveVoteCount = cursor.getInt(positiveVoteCountIndex);
			this.negativeVoteCount = cursor.getInt(negativeVoteCountIndex);
		}
	}
	
	
	/****************************************
	 * ???
	 */
	
	private static Date parsePublishTime(JSONObject jsonObject)
	{
		Date publishTime = null;
		
		if(jsonObject != null)
		{
			String publishTimeString = JSONUtilities.getJsonString(jsonObject, PUBLISHTIME_KEY, null);
			
			SimpleDateFormat format = new SimpleDateFormat(PUBLISHTIME_FORMAT, new Locale("tr", "TR"));
			
			try
			{
				publishTime = format.parse(publishTimeString);
			}
			catch (Exception e) { /** Ignored */ }
		}
		
		return publishTime;
	}
	
	
	/****************************************
	 * Accessors
	 */
	
	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}
	/**
	 * @return the code
	 */
	public String getCode()
	{
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(String code)
	{
		this.code = code;
	}
	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}
	/**
	 * @return the imageUrl
	 */
	public String getImageUrl()
	{
		return imageUrl;
	}
	/**
	 * @param imageUrl the imageUrl to set
	 */
	public void setImageUrl(String imageUrl)
	{
		this.imageUrl = imageUrl;
	}
	/**
	 * @return the thumbUrl
	 */
	public String getThumbUrl()
	{
		return thumbUrl;
	}
	/**
	 * @param thumbUrl the thumbUrl to set
	 */
	public void setThumbUrl(String thumbUrl)
	{
		this.thumbUrl = thumbUrl;
	}
	/**
	 * @return the link
	 */
	public String getLink()
	{
		return link;
	}
	/**
	 * @param link the link to set
	 */
	public void setLink(String link)
	{
		this.link = link;
	}
	/**
	 * @return the category
	 */
	public String getCategory()
	{
		return category;
	}
	/**
	 * @param category the category to set
	 */
	public void setCategory(String category)
	{
		this.category = category;
	}
	/**
	 * @return the categoryId
	 */
	public String getCategoryId()
	{
		return categoryId;
	}
	/**
	 * @param categoryId the categoryId to set
	 */
	public void setCategoryId(String categoryId)
	{
		this.categoryId = categoryId;
	}
	/**
	 * @return the spot
	 */
	public String getSpot()
	{
		return spot;
	}
	/**
	 * @param spot the spot to set
	 */
	public void setSpot(String spot)
	{
		this.spot = spot;
	}
	/**
	 * @return the videoUrl
	 */
	public String getVideoUrl()
	{
		return videoUrl;
	}
	/**
	 * @param videoUrl the videoUrl to set
	 */
	public void setVideoUrl(String videoUrl)
	{
		this.videoUrl = videoUrl;
	}
	/**
	 * @return the publishTime
	 */
	public Date getPublishTime()
	{
		return publishTime;
	}
	/**
	 * @param publishTime the publishTime to set
	 */
	public void setPublishTime(Date publishTime)
	{
		this.publishTime = publishTime;
	}
	/**
	 * @return the displayQuantity
	 */
	public int getViewCount()
	{
		return viewCount;
	}
	/**
	 * @param displayQuantity the displayQuantity to set
	 */
	public void setViewCount(int viewCount)
	{
		this.viewCount = viewCount;
	}
	/**
	 * @return the commentQuantity
	 */
	public int getCommentCount()
	{
		return commentCount;
	}
	/**
	 * @param commentQuantity the commentQuantity to set
	 */
	public void setCommentCount(int commentCount)
	{
		this.commentCount = commentCount;
	}
	/**
	 * @return the positiveVoteCount
	 */
	public int getPositiveVoteCount()
	{
		return positiveVoteCount;
	}
	/**
	 * @param positiveVoteCount the positiveVoteCount to set
	 */
	public void setPositiveVoteCount(int positiveVoteCount)
	{
		this.positiveVoteCount = positiveVoteCount;
	}
	/**
	 * @return the negativeVoteCount
	 */
	public int getNegativeVoteCount()
	{
		return negativeVoteCount;
	}
	/**
	 * @param negativeVoteCount the negativeVoteCount to set
	 */
	public void setNegativeVoteCount(int negativeVoteCount)
	{
		this.negativeVoteCount = negativeVoteCount;
	}
	
	
	/*****************************************
	 * Objectful stuff
	 */
	
	@Override
	public boolean equals(Object o)
	{
		boolean equal = false;
		
		if((o instanceof VideoClip) && (this.id != null))
		{
			equal = this.id.equalsIgnoreCase(((VideoClip) o).id);
		}
		else
		{
			equal = super.equals(o);
		}
		
		return equal;
	}
	
	@Override
	public int compareTo(VideoClip another)
	{
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
		
		//this optimization is usually worthwhile, and can
	    //always be added
	    if (this == another) return EQUAL;

		return this.id.compareTo(another.id);
	}
	
	@Override
	public String toString()
	{
		return this.id + " - " + this.title;
	}
	
	
	/*****************************************
	 * Parcel implementation
	 */
	
	protected VideoClip(Parcel in)
	{
        id = in.readString();
        code = in.readString();
        title = in.readString();
        imageUrl = in.readString();
        thumbUrl = in.readString();
        link = in.readString();
        category = in.readString();
        categoryId = in.readString();
        spot = in.readString();
        videoUrl = in.readString();
        long tmpPublishTime = in.readLong();
        publishTime = tmpPublishTime != -1 ? new Date(tmpPublishTime) : null;
        viewCount = in.readInt();
        commentCount = in.readInt();
        positiveVoteCount = in.readInt();
        negativeVoteCount = in.readInt();
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(id);
        dest.writeString(code);
        dest.writeString(title);
        dest.writeString(imageUrl);
        dest.writeString(thumbUrl);
        dest.writeString(link);
        dest.writeString(category);
        dest.writeString(categoryId);
        dest.writeString(spot);
        dest.writeString(videoUrl);
        dest.writeLong(publishTime != null ? publishTime.getTime() : -1L);
        dest.writeInt(viewCount);
        dest.writeInt(commentCount);
        dest.writeInt(positiveVoteCount);
        dest.writeInt(negativeVoteCount);
    }

    public static final Parcelable.Creator<VideoClip> CREATOR = new Parcelable.Creator<VideoClip>()
    {
        public VideoClip createFromParcel(Parcel in)
        {
            return new VideoClip(in);
        }

        public VideoClip[] newArray(int size)
        {
            return new VideoClip[size];
        }
    };
}
