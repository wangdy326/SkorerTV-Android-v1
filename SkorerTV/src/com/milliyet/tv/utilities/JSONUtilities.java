package com.milliyet.tv.utilities;

import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Bundle;
import android.text.TextUtils;

/**
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
public class JSONUtilities
{	
	// =============== Parsers ===============//

	/**
	 * Parses JSON formatted string to corresponding object structure. Default
	 * value will be returned on empty/null string input or failure.
	 * 
	 * @param rawJson
	 *            JSON formatted string
	 * @param defaultValue
	 *            default return value in case of failure
	 * @return parsed object (could be a a JSONObject, JSONArray, String,
	 *         Boolean, Integer, Long, Double or NULL)
	 */
	public static Object parse(String rawJson, Object defaultValue) {
		Object json = defaultValue;

		if (!TextUtils.isEmpty(rawJson)) {
			try {
				json = new JSONTokener(rawJson).nextValue();
			} catch (JSONException e) {
				/** Ignored exception, default value will be returned */
			}
		}

		return json;
	}

	/**
	 * Parses JSON formatted string to corresponding object structure to
	 * JSONObject. Default value will be returned on empty/null string input or
	 * failure.
	 * 
	 * @param rawJson
	 *            JSON formatted string
	 * @param defaultValue
	 *            default return value in case of failure
	 * @return parsed JSONObject
	 */
	public static JSONObject parseObject(String rawJson, JSONObject defaultValue) {
		Object json = JSONUtilities.parse(rawJson, defaultValue);

		// If parsed instance is not null and instance of JSONObject, return
		// parsed instance.
		// Otherwise return default value
		return (json instanceof JSONObject) ? ((JSONObject) json)
				: (defaultValue);
	}

	/**
	 * Parses JSON formatted string to corresponding object structure to
	 * JSONArray. Default value will be returned on empty/null string input or
	 * failure.
	 * 
	 * @param rawJson
	 *            JSON formatted string
	 * @param defaultValue
	 *            default return value in case of failure
	 * @return parsed JSONArray
	 */
	public static JSONArray parseArray(String rawJson, JSONArray defaultValue) {
		Object json = JSONUtilities.parse(rawJson, defaultValue);

		// If parsed instance is not null and instance of JSONArray, return
		// parsed instance.
		// Otherwise return default value
		return (json instanceof JSONArray) ? ((JSONArray) json)
				: (defaultValue);
	}

	// =============== JSONObject getters ===============//

	/**
	 * Returns object with given key from JSONObject. Default value will be
	 * returned in case of failure or null JSONObject input.
	 * 
	 * @param json
	 *            JSONObject instance holding data.
	 * @param key
	 *            JSONObject key for value.
	 * @param defaultValue
	 *            default return value, in case of failure.
	 * @return Object corresponding given key.
	 */
	public static Object getJsonValue(JSONObject json, String key,
			Object defaultValue) {
		Object value = defaultValue;

		if (json != null) {
			try {
				if (!json.isNull(key)) {
					value = json.get(key);
				}
			} catch (Exception e) {
				/** Ignored exception, default value will be returned */
			}
		}

		return value;
	}

	/**
	 * Returns String with given key from JSONObject. Default value will be
	 * returned in case of failure or null JSONObject input. !!!: If requested
	 * object is not a string, its toString() method output will be returned
	 * 
	 * @param json
	 *            JSONObject instance holding data.
	 * @param key
	 *            JSONObject key for value.
	 * @param defaultValue
	 *            default return value, in case of failure.
	 * @return String string corresponding given key.
	 */
	public static String getJsonString(JSONObject json, String key,
			String defaultString) {
		Object value = getJsonValue(json, key, defaultString);

		String string = (value == null) ? ("") : (value.toString());

		return string;
	}

	/**
	 * Returns Boolean with given key from JSONObject. Default value will be
	 * returned in case of failure or null JSONObject input.
	 * 
	 * @param json
	 *            JSONObject instance holding data.
	 * @param key
	 *            JSONObject key for value.
	 * @param defaultBoolean
	 *            default return value, in case of failure.
	 * @return Boolean boolean corresponding given key.
	 */
	public static Boolean getJsonBoolean(JSONObject json, String key,
			Boolean defaultBoolean) {
		Object booleanValue = getJsonValue(json, key, defaultBoolean);

		return ((booleanValue instanceof Boolean) ? ((Boolean) booleanValue)
				: (defaultBoolean));
	}
	
	/**
	 * Returns Integer with given key from JSONObject. Default value will be
	 * returned in case of failure or null JSONObject input.
	 * 
	 * @param json
	 *            JSONObject instance holding data.
	 * @param key
	 *            JSONObject key for value.
	 * @param defaultInteger
	 *            default return value, in case of failure.
	 * @return Integer integer corresponding given key.
	 */
	public static Integer getJsonInteger(JSONObject json, String key,
			Integer defaultInteger) {
		Object integerValue = getJsonValue(json, key, defaultInteger);

		return ((integerValue instanceof Integer) ? ((Integer) integerValue)
				: (defaultInteger));
	}

	/**
	 * Returns JSONObject with given key from JSONObject. Default value will be
	 * returned in case of failure or null JSONObject input.
	 * 
	 * @param json
	 *            JSONObject instance holding data.
	 * @param key
	 *            JSONObject key for value.
	 * @param defaultValue
	 *            default return value, in case of failure.
	 * @return JSONObject JSONObject corresponding given key.
	 */
	public static JSONObject getJsonObject(JSONObject json, String key,
			JSONObject defaultJsonObject) {
		Object jsonObject = getJsonValue(json, key, defaultJsonObject);

		return ((jsonObject instanceof JSONObject) ? ((JSONObject) jsonObject)
				: (defaultJsonObject));
	}

	/**
	 * Returns JSONArray with given key from JSONObject. Default value will be
	 * returned in case of failure or null JSONObject input.
	 * 
	 * @param json
	 *            JSONObject instance holding data.
	 * @param key
	 *            JSONObject key for value.
	 * @param defaultValue
	 *            default return value, in case of failure.
	 * @return JSONArray JSONArray corresponding given key.
	 */
	public static JSONArray getJsonArray(JSONObject json, String key,
			JSONArray defaultJsonArray) {
		Object jsonArray = getJsonValue(json, key, defaultJsonArray);

		return ((jsonArray instanceof JSONArray) ? ((JSONArray) jsonArray)
				: (defaultJsonArray));
	}

	// =============== JSONArray getters ===============//

	public static Object getArrayValue(JSONArray array, int index,
			Object defaultValue) {
		Object value = defaultValue;

		if (array != null) {
			try {
				if (!array.isNull(index)) {
					value = array.get(index);
				}
			} catch (Exception e) {
				/** Ignored exception, default value will be returned */
			}
		}

		return value;
	}

	public static String getArrayString(JSONArray array, int index,
			String defaultString) {
		Object value = getArrayValue(array, index, defaultString);

		String string = (value == null) ? ("") : (value.toString());

		return string;
	}

	public static JSONObject getArrayObject(JSONArray array, int index,
			JSONObject defaultJsonObject) {
		Object value = JSONUtilities.getArrayValue(array, index,
				defaultJsonObject);

		return ((value instanceof JSONObject) ? ((JSONObject) value)
				: (defaultJsonObject));
	}
	
	public static Bundle getArrayBundle(JSONArray array, int index, Bundle defaultBundle)
	{
		Bundle bundle = null;
		Object value = JSONUtilities.getArrayValue(array, index, defaultBundle);
		
		// Bundles stored as string encoded in JSON
		// If we are able to retrieve raw bundle
		if(value instanceof String)
		{
			// Parse raw bundle string to native JSONObject instance
			JSONObject jsonObject = parseObject((String) value, null);
			
			// If raw bundle string parsed successfully
			if(jsonObject != null)
			{
				// Parse JSONObject to bundle
				bundle = parseBundle(jsonObject);
			}
		}
		
		return bundle;
	}
	
	public static JSONObject parseObject(Bundle bundle)
	{
		JSONObject jsonObject = new JSONObject();
		
		if(bundle != null)
		{
			Set<String> keys = bundle.keySet();
			
			// For each key in bundle
			for (String key : keys)
			{
				// Fetch object from bundle
				Object object = bundle.get(key);
				
				// If received bundle 
				if(object instanceof Bundle)
				{
					// Convert bundle to JSONObject
					// (a.k.a JSONObject attach-able type ^^)
					object = parseObject((Bundle) object);
				}
				
				// Try inserting key/value pair to JSON object
				try
				{
					jsonObject.put(key, object);
				}
				catch (JSONException e)
				{
					if(isLogEnabled())
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		return jsonObject;
	}
	
	// TODO: Move this method to bundle utilities or something!
	public static Bundle parseBundle(JSONObject jsonObject)
	{
		Bundle bundle = null;
		
		if(jsonObject != null)
		{
			bundle = new Bundle(jsonObject.length());
			
			for(Iterator<String> iterator = jsonObject.keys(); iterator.hasNext();)
			{
				// Get key value
				String key = iterator.next();
				
				// Try fetching object corresponding to acquired key
				Object object = null;
				try
				{
					object = jsonObject.get(key);
				}
				
				catch (JSONException e)
				{
					if(isLogEnabled())
					{
						e.printStackTrace();
					}
				}
				
				// Try inserting retrieved object
				putBundleObject(bundle, key, object);
			}
		}
		
		return bundle;
	}
	
	/*************************************
	 * Bundle stuff
	 */
	
	private static void putBundleObject(Bundle bundle, String key, Object object)
	{
		if(bundle !=null && object != null && !TextUtils.isEmpty(key))
		{
			if(object instanceof String)
			{
				bundle.putString(key, (String) object);
			}
			else if(object instanceof Boolean)
			{
				bundle.putBoolean(key, (Boolean) object);
			}
			else if(object instanceof Double)
			{
				bundle.putDouble(key, (Double) object);
			}
			else if(object instanceof Float)
			{
				Float value = (Float) object;
				
				bundle.putDouble(key, (double) value);
			}
			else if(object instanceof Integer)
			{
				bundle.putInt(key, (Integer) object);
			}
			else if(object instanceof Long)
			{
				bundle.putLong(key, (Long) object);
			}
			else if(object instanceof JSONObject)
			{
				object = parseBundle((JSONObject) object);
				
				bundle.putBundle(key, (Bundle) object);
			}
			else if(object instanceof JSONArray)
			{
				int elementQuantity = ((JSONArray) object).length();
				Bundle subBundle = new Bundle(elementQuantity);
				
				for (int i = 0; i < elementQuantity; i++)
				{
					Object subObject = getArrayValue((JSONArray) object, i, null);
					
					if(subObject != null)
					{
						putBundleObject(subBundle, key, subObject);
					}
				}
			}
		}
	}
	
	
	/*************************************
	 * Log
	 */
	
	protected static boolean isLogEnabled()
	{
		return true;
	}

	protected static String getLogTag()
	{
		return "JSON Utilities";
	}
}
