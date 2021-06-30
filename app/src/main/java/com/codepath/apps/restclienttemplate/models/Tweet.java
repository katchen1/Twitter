// models/Tweet.java
package com.codepath.apps.restclienttemplate.models;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.room.Entity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
public class Tweet {

	public String idStr;
	public String body;
	public String createdAt;
	public List<String> imageUrls;
	public User user;

	public Tweet() {} // empty constructor needed by the Parceler library

	public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
		Tweet tweet = new Tweet();
		tweet.idStr = jsonObject.getString("id_str");
		tweet.body = jsonObject.getString("text");
		tweet.createdAt = jsonObject.getString("created_at");
		tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
		tweet.imageUrls = new ArrayList<>();
		if (jsonObject.has("extended_entities")) {
			JSONArray mediaObjects = jsonObject.getJSONObject("extended_entities").getJSONArray("media");
			for (int i = 0; i < mediaObjects.length(); i++) {
				tweet.imageUrls.add(mediaObjects.getJSONObject(i).getString("media_url_https"));
			}
		}
		return tweet;
	}

	public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
		List<Tweet> tweets = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			tweets.add(fromJson(jsonArray.getJSONObject(i)));
		}
		return tweets;
	}

	public String getRelativeTimeAgo() {
		String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
		sf.setLenient(true);

		String relativeDate = "";
		try {
			long dateMillis = sf.parse(createdAt).getTime();
			relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
					System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
		} catch (ParseException e) {
			Log.e("Tweet", "error getting relative time ago");
		}
		return relativeDate;
	}
}