package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.text.TextUtils;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.oauth.OAuthBaseClient;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.api.BaseApi;
import java.util.List;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/scribejava/scribejava/tree/master/scribejava-apis/src/main/java/com/github/scribejava/apis
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class RestClient extends OAuthBaseClient {
	public static final BaseApi REST_API_INSTANCE = TwitterApi.instance();
	public static final String REST_URL = "https://api.twitter.com/1.1";
	public static final String REST_CONSUMER_KEY = BuildConfig.CONSUMER_KEY;       // Change this inside apikey.properties
	public static final String REST_CONSUMER_SECRET = BuildConfig.CONSUMER_SECRET; // Change this inside apikey.properties

	// Landing page to indicate the OAuth flow worked in case Chrome for Android 25+ blocks navigation back to the app.
	public static final String FALLBACK_URL = "https://codepath.github.io/android-rest-client-template/success.html";

	// See https://developer.chrome.com/multidevice/android/intents
	public static final String REST_CALLBACK_URL_TEMPLATE = "intent://%s#Intent;action=android.intent.action.VIEW;scheme=%s;package=%s;S.browser_fallback_url=%s;end";

	public RestClient(Context context) {
		super(context, REST_API_INSTANCE,
				REST_URL,
				REST_CONSUMER_KEY,
				REST_CONSUMER_SECRET,
				null,  // OAuth2 scope, null for OAuth1
				String.format(REST_CALLBACK_URL_TEMPLATE, context.getString(R.string.intent_host),
						context.getString(R.string.intent_scheme), context.getPackageName(), FALLBACK_URL));
	}

	/* Sends a GET request to retrieve the user's home timeline. maxId is the maximum (newest)
	 * id of the tweet in the returned list, used for endless pagination. */
	public void getHomeTimeline(Long maxId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", 25);
		params.put("since_id", 1);
		params.put("tweet_mode", "extended");
		params.put("include_entities", true);
		if (maxId != -1) params.put("max_id", maxId.toString());
		client.get(apiUrl, params, handler);
	}

	/* Sends a POST request to post a tweet. */
	public void publishTweet(String tweetContent, Long replyId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		params.put("status", tweetContent);
		if (replyId != -1) {
			params.put("in_reply_to_status_id", replyId.toString());
			params.put("auto_populate_reply_metadata", true);
		}
		params.put("tweet_mode", "extended");
		client.post(apiUrl, params, "", handler);
	}

	/* Sends a POST request to favorite or un-favorite a tweet. */
	public void favorite(Long tweetId, Boolean tweetFavorited, JsonHttpResponseHandler handler) {
		String path = tweetFavorited? "favorites/destroy.json": "favorites/create.json";
		String apiUrl = getApiUrl(path);
		RequestParams params = new RequestParams();
		params.put("id", tweetId.toString());
		client.post(apiUrl, params, "", handler);
	}

	/* Sends a POST request to retweet or un-retweet a tweet. */
	public void retweet(Long tweetId, Boolean tweetRetweeted, JsonHttpResponseHandler handler) {
		String path = tweetRetweeted? "statuses/unretweet.json": "statuses/retweet.json";
		String apiUrl = getApiUrl(path);
		RequestParams params = new RequestParams();
		params.put("id", tweetId.toString());
		client.post(apiUrl, params, "", handler);
	}

	/* Sends a GET request to retrieve followers or friends of a user. */
	public void getFollowers(String mode, Long userId, JsonHttpResponseHandler handler) {
		String path = mode.equals("Followers") ? "followers/ids.json": "friends/ids.json";
		String apiUrl = getApiUrl(path);
		RequestParams params = new RequestParams();
		params.put("user_id", userId.toString());
		client.get(apiUrl, params, handler);
	}

	/* Sends a GET request to retrieve followers or friends of a user.
	 * Takes in a comma-separated list of ids, up to 100 are allowed in a single request.
	 * Strongly encouraged to use a POST for larger (up to 100 ids) rather than smaller requests. */
	public void lookupUsers(List<Long> userIds, JsonHttpResponseHandler handler) {
		String path = "users/lookup.json";
		String apiUrl = getApiUrl(path);
		RequestParams params = new RequestParams();
		params.put("user_id", TextUtils.join(",", userIds));
		client.get(apiUrl, params, handler);
	}
}
