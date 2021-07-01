package com.codepath.apps.restclienttemplate.activities;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.adapters.FollowersAdapter;
import com.codepath.apps.restclienttemplate.databinding.ActivityFollowersBinding;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Headers;

public class FollowersActivity extends AppCompatActivity {

    private final String TAG = "FollowersActivity";
    private User user; // The user whose follower/following list is shown
    private String mode; // Either "followers" or "following" depending on which list is shown
    private List<Long> followerIds; // 5000 most recent followers, ordered by recency
    private List<User> followers; // The current users shown in the followers recycler view
    private RestClient client; // For making Twitter API calls
    private FollowersAdapter adapter; // Adapter for the recycler view
    private int startIndex, endIndex; // For keeping track of which segment of followerIds to lookup

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View binding to reduce view boilerplate
        ActivityFollowersBinding binding = ActivityFollowersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize member variables
        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        mode = getIntent().getStringExtra("mode");
        followerIds = new ArrayList<>();
        followers = new ArrayList<>();
        client = RestApplication.getRestClient(this);
        adapter = new FollowersAdapter(this, followers);
        startIndex = 0;
        endIndex = 0;

        // Set up the follower recycler view with divider between items
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.rvFollowers.setLayoutManager(linearLayoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        binding.rvFollowers.addItemDecoration(divider);
        binding.rvFollowers.setAdapter(adapter);

        // Fetch the list of the user's followers' ids (supports up to 5000 followers)
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) setTitle(user.name + "'s " + mode);
        client.getFollowers(mode, user.id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    // Store the follower ids in the list
                    JSONArray jsonIds = json.jsonObject.getJSONArray("ids");
                    for (int i = 0; i < jsonIds.length(); i++) {
                        followerIds.add(jsonIds.getLong(i));
                    }

                    // Fetch User objects using the follower ids
                    lookupUsersWithIds();
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure! " + response, throwable);
            }
        });
    }

    /* Makes an Twitter API call to /users/lookup and appends the followers list with user objects. */
    public void lookupUsersWithIds() {
        // Avoid making the API call if there are no more users to fetch
        if (followerIds.isEmpty() || endIndex == followerIds.size()) return;

        // Specify the range in the follower ids list
        endIndex = startIndex + 100; // 100 is the upper limit of what a single request can take
        if (endIndex >= followerIds.size()) {
            endIndex = followerIds.size();
        }

        // Fetch the next batch of users
        List<Long> sublist = followerIds.subList(startIndex, endIndex);
        client.lookupUsers(sublist, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    // Add to the list and notify the adapter
                    followers.addAll(User.fromJsonArray(json.jsonArray));
                    adapter.notifyDataSetChanged();
                    startIndex = endIndex;
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure! " + response, throwable);
            }
        });
    }
}