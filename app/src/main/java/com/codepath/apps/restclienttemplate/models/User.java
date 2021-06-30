package com.codepath.apps.restclienttemplate.models;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class User {

	public String name;
	public String screenName;
	public String profileImageUrl;
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

	public static User fromJson(JSONObject jsonObject) throws JSONException {
		User user = new User();
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
}