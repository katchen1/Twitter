package com.codepath.apps.restclienttemplate.models;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class User {

	public Long id; // Unique id for the user
	public String name; // ex. Katherine Chen
	public String screenName; // The handle ex. @kc.kc.kc
	public String profileImageUrl; // Link to their profile iamge
	public String location; // The user-defined location for this account’s profile
	public String url; // A URL provided by the user in association with their profile
	public String description; // The user-defined UTF-8 string describing their account.
	public Boolean verified; // When true, indicates that the user has a verified account
	public Integer followerCount; // The number of followers this account currently has.
	public Integer friendsCount; // The number of users this account is following
	public String createdAt; // The UTC datetime that the user account was created on Twitter
	public String profileBannerUrl; // The user's uploaded profile banner

	// empty constructor needed by the Parceler library
	public User() {}

	/* Constructor - reads a JSON object and converts it to a user object. */
	public static User fromJson(JSONObject jsonObject) throws JSONException {
		User user = new User();
		user.id = jsonObject.getLong("id");
		user.name = jsonObject.getString("name");
		user.screenName = jsonObject.getString("screen_name");
		user.profileImageUrl = jsonObject.getString("profile_image_url_https");
		user.location = jsonObject.getString("location");
		user.url = jsonObject.getString("url");
		user.description = jsonObject.getString("description");
		user.verified = jsonObject.getBoolean("verified");
		user.followerCount = jsonObject.getInt("followers_count");
		user.friendsCount = jsonObject.getInt("friends_count");
		user.createdAt = jsonObject.getString("created_at");
		if (jsonObject.has("profile_banner_url")) {
			user.profileBannerUrl = jsonObject.getString("profile_banner_url");
		} else {
			user.profileBannerUrl = "";
		}
		return user;
	}

	/* Takes a JSON array and converts it to a list of user objects. */
	public static List<User> fromJsonArray(JSONArray jsonArray) throws JSONException {
		List<User> users = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			users.add(fromJson(jsonArray.getJSONObject(i)));
		}
		return users;
	}
}