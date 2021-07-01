package com.codepath.apps.restclienttemplate.models;
import com.codepath.apps.restclienttemplate.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;
import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {

	public Long id; // The unique identifier of the tweet
	public String body; // The text body
	public String createdAt; // The time it was created (in ugly format)
	public List<String> imageUrls; // A list of urls of the embedded images
	public User user; // The user who wrote the tweet
	public Boolean favorited; // Is it favorited by the logged in user
	public Boolean retweeted; // Is it retweeted by the logged in user
	public Integer favoriteCount; // How many people favorited it
	public Integer retweetCount; // How many people retweeted it

	public Tweet() {} // empty constructor needed by the Parceler library

	/* Constructor - reads a JSON object and converts it to a tweet object. */
	public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
		Tweet tweet = new Tweet();
		tweet.id = jsonObject.getLong("id");
		tweet.body = jsonObject.getString("text");
		tweet.createdAt = jsonObject.getString("created_at");
		tweet.favorited = jsonObject.getBoolean("favorited");
		tweet.retweeted = jsonObject.getBoolean("retweeted");
		tweet.favoriteCount = jsonObject.getInt("favorite_count");
		tweet.retweetCount = jsonObject.getInt("retweet_count");
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

	/* Takes a JSON array and converts it to a list of tweet objects. */
	public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
		List<Tweet> tweets = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			tweets.add(fromJson(jsonArray.getJSONObject(i)));
		}
		return tweets;
	}

	/* Returns the relative time ago - used in the timeline activity. */
	public String getRelativeTimeAgo() {
		return Constants.convertTimeFormat(createdAt, Constants.twitterTimeFormat, "relative");
	}

	/* Returns the time the tweet was created in the form used by the tweets detail activity. */
	public String getCreatedAt() {
		return Constants.convertTimeFormat(createdAt, Constants.twitterTimeFormat, Constants.myTimeFormat);
	}
}