package com.codepath.apps.restclienttemplate.activities;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    public static final int COMPOSE_REQUEST_CODE = 1;
    public static final int REPLY_REQUEST_CODE = 2;
    public static final int DETAILS_REQUEST_CODE = 3;
    RestClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;
    MenuItem miActionProgressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        getSupportActionBar().setTitle("");
        client = RestApplication.getRestClient(this);

        // Set up the recycler view and its adapter
        rvTweets = findViewById(R.id.rvTweets);
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        ((SimpleItemAnimator) rvTweets.getItemAnimator()).setSupportsChangeAnimations(false);
        rvTweets.setAdapter(adapter);

        // Add a divider between items in the recycler view
        DividerItemDecoration divider = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        rvTweets.addItemDecoration(divider);

        // Initial fetch of tweets
        populateHomeTimeline();

        // Setup the swipe refresh listener which triggers new data loading
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    /* Sends the network request to fetch the updated data. */
    public void fetchTimelineAsync() {
        showProgressBar();
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    // Update the list with the newly fetched tweets and notify the adapter
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    // Log the error
                    Log.e(TAG, "Json exception", e);
                }
                hideProgressBar();
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                // Log the error
                Log.e(TAG, "Fetch timeline error " + response, throwable);
            }
        });
    }

    /* Adds items to the action bar and returns true for the menu to be displayed. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /* Stores the instance of the menu item containing the progress loading indicator. */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        return super.onPrepareOptionsMenu(menu);
    }

    /* Shows the progress bar for background network tasks. */
    public void showProgressBar() {
        miActionProgressItem.setVisible(true);
    }

    /* Hides the progress bar for background network tasks. */
    public void hideProgressBar() {
        miActionProgressItem.setVisible(false);
    }

    /* Handles what happens when different icons in the menu bar are clicked. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Compose icon: navigate to the compose activity
        if (item.getItemId() == R.id.compose) {
            Intent intent = new Intent(this, ComposeActivity.class);
            startActivityForResult(intent, COMPOSE_REQUEST_CODE);
            return true;
        }

        // Logout icon: logout
        if (item.getItemId() == R.id.logout) {
            client.clearAccessToken();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /* Handles the data passed back from calls to startActivityForResult. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Get tweet from the intent and update the recycler view with the tweet
        if ((requestCode == COMPOSE_REQUEST_CODE || requestCode == REPLY_REQUEST_CODE)
                && resultCode == RESULT_OK) {
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            tweets.add(0, tweet);
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0); // scroll back to top
        } else if (requestCode == DETAILS_REQUEST_CODE && resultCode == RESULT_OK) {
            populateHomeTimeline();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* Initial fetch of tweets on the user's timeline. */
    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    // Add the tweets to the list and notify the adapter of the change
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
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
}