package com.skorer.tv.model;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.milliyet.tv.utilities.JSONUtilities;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class Category implements Parcelable
{
	/****************************************
	 * JSON keys
	 */
	
	private static final String ID_KEY = "ID";
	private static final String NAME_KEY = "Name";
	private static final String SORTORDER_KEY = "SortOrder";
	private static final String FEATURED_KEY = "Featured";
	private static final String WEBSITE_KEY = "Website";
	
	
	/****************************************
	 * Variables
	 */
	
	private String id = null;
	private String name = null;
	private int sortOrder = Integer.MAX_VALUE;
	private boolean featured = false;
	private String website;
	
	
	/****************************************
	 * Constructors & Instance providers
	 */
	
	public Category()
	{
		
	}
	
	public static Category fromJSONObject(JSONObject jsonObject)
	{
		Category category = null;
		
		if(jsonObject != null)
		{
			category = new Category();
			
			category.id = JSONUtilities.getJsonString(jsonObject, ID_KEY, null);
			category.name = JSONUtilities.getJsonString(jsonObject, NAME_KEY, null);
			category.sortOrder = JSONUtilities.getJsonInteger(jsonObject, SORTORDER_KEY, Integer.MAX_VALUE);
			category.featured = JSONUtilities.getJsonBoolean(jsonObject, FEATURED_KEY, false);
			category.website = JSONUtilities.getJsonString(jsonObject, WEBSITE_KEY, null);
		}
		
		return category;
	}


	/****************************************
	 * Accessors
	 */
	
	/**
	 * @return the id
	 */
	public synchronized String getId()
	{
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public synchronized void setId(String id)
	{
		this.id = id;
	}


	/**
	 * @return the name
	 */
	public synchronized String getName()
	{
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public synchronized void setName(String name)
	{
		this.name = name;
	}


	/**
	 * @return the sortOrder
	 */
	public synchronized int getSortOrder()
	{
		return sortOrder;
	}


	/**
	 * @param sortOrder the sortOrder to set
	 */
	public synchronized void setSortOrder(int sortOrder)
	{
		this.sortOrder = sortOrder;
	}


	/**
	 * @return the featured
	 */
	public synchronized boolean isFeatured()
	{
		return featured;
	}


	/**
	 * @param featured the featured to set
	 */
	public synchronized void setFeatured(boolean featured)
	{
		this.featured = featured;
	}


	/**
	 * @return the website
	 */
	public synchronized String getWebsite()
	{
		return website;
	}


	/**
	 * @param website the website to set
	 */
	public synchronized void setWebsite(String website)
	{
		this.website = website;
	}
	
	
	/*****************************************
	 * Objectful stuff
	 */
	
	@Override
	public boolean equals(Object o)
	{
		boolean equal = false;
		
		if((o instanceof Category) && (this.id != null))
		{
			equal = this.id.equalsIgnoreCase(((Category) o).id);
		}
		else
		{
			equal = super.equals(o);
		}
		
		return equal;
	}
	
	@Override
	public String toString()
	{
		return this.id + " - " + this.name;
	}
	
	
	/*****************************************
	 * Parcel implementation
	 */
	
	protected Category(Parcel in)
	{
        id = in.readString();
        name = in.readString();
        sortOrder = in.readInt();
        featured = in.readByte() != 0x00;
        website = in.readString();
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(sortOrder);
        dest.writeByte((byte) (featured ? 0x01 : 0x00));
        dest.writeString(website);
    }

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>()
    {
        public Category createFromParcel(Parcel in)
        {
            return new Category(in);
        }

        public Category[] newArray(int size)
        {
            return new Category[size];
        }
    };
}
