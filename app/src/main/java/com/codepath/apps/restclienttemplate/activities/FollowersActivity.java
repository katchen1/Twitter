package com.codepath.apps.restclienttemplate.activities;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.adapters.FollowersAdapter;
import com.codepath.apps.restclienttemplate.databinding.ActivityFollowersBinding;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Headers;

public class FollowersActivity extends AppCompatActivity {

    public final String TAG = "FollowersActivity";
    RestClient client;
    List<String> followerIds;
    List<User> followers;
    FollowersAdapter adapter;
    int startIndex, endIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_followers);

        ActivityFollowersBinding binding = ActivityFollowersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("");
        client = RestApplication.getRestClient(this);

        User user = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        String mode = getIntent().getStringExtra("mode");

        followers = new ArrayList<>();
        followerIds = new ArrayList<>();
        startIndex = 0;
        adapter = new FollowersAdapter(this, followers);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.rvFollowers.setLayoutManager(linearLayoutManager);
        binding.rvFollowers.setAdapter(adapter);

        // Add a divider between items in the recycler view
        DividerItemDecoration divider = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        binding.rvFollowers.addItemDecoration(divider);

        System.out.println("HIHIHI " + mode);
        client.getFollowers(mode, user.idStr, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    // Add the users and notify the adapter
                    JSONArray jsonIds = jsonObject.getJSONArray("ids");
                    // Up to 5000 users
                    for (int i = 0; i < jsonIds.length(); i++) {
                        followerIds.add(new Long(jsonIds.getLong(i)).toString());
                    }
                    lookupUsers();
                } catch (JSONException e) {
                    // Log the error
                    Log.e(TAG, "Json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                // Log the error
                Log.e(TAG, "onFailure! " + response, throwable);
            }
        });
    }

    public void lookupUsers() {
        if (followerIds.isEmpty()) return;
        endIndex = startIndex + 100;
        if (endIndex >= followerIds.size()) endIndex = followerIds.size();
        client.lookupUsers(followerIds.subList(startIndex, endIndex), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    followers.addAll(User.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                    startIndex = endIndex;
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception");
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure! " + response, throwable);
            }
        });
    }
}