package com.codepath.apps.restclienttemplate.activities;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
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
import android.widget.Toast;
import com.codepath.apps.restclienttemplate.ComposeDialogFragment;
import com.codepath.apps.restclienttemplate.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.databinding.ActivityFollowersBinding;
import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements ComposeDialogFragment.ComposeDialogListener {

    public static final String TAG = "TimelineActivity";
    public static final int COMPOSE_REQUEST_CODE = 1;
    public static final int REPLY_REQUEST_CODE = 2;
    public static final int DETAILS_REQUEST_CODE = 3;
    RestClient client;
    //RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    //SwipeRefreshLayout swipeContainer;
    MenuItem miActionProgressItem;
    private EndlessRecyclerViewScrollListener scrollListener;
    ActivityTimelineBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTimelineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //setContentView(R.layout.activity_timeline);
        getSupportActionBar().setTitle("");
        client = RestApplication.getRestClient(this);

        // Set up the recycler view and its adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.rvTweets.setLayoutManager(linearLayoutManager);
        ((SimpleItemAnimator) binding.rvTweets.getItemAnimator()).setSupportsChangeAnimations(false);
        binding.rvTweets.setAdapter(adapter);

        // Add a divider between items in the recycler view
        DividerItemDecoration divider = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        binding.rvTweets.addItemDecoration(divider);

        // Initial fetch of tweets
        populateHomeTimeline();

        // Setup the swipe refresh listener which triggers new data loading
        //swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync();
            }
        });

        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Enable endless pagination on the recycler view
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);
            }
        };
        binding.rvTweets.addOnScrollListener(scrollListener);

        // 1. First, clear the array of data
        tweets.clear();
        // 2. Notify the adapter of the update
        adapter.notifyDataSetChanged(); // or notifyItemRangeRemoved
        // 3. Reset endless scroll listener when performing a new search
        scrollListener.resetState();
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
        showProgressBar();
        client.getHomeTimeline(tweets.get(tweets.size() - 1) .idStr, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    // Append the list with the newly fetched tweets and notify the adapter
                    tweets.remove(tweets.size() - 1);
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
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

    /* Sends the network request to fetch the updated data. */
    public void fetchTimelineAsync() {
        showProgressBar();
        client.getHomeTimeline("", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    // Update the list with the newly fetched tweets and notify the adapter
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    binding.swipeContainer.setRefreshing(false);
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
        if (item.getItemId() == R.id.compose) {
            FragmentManager fm = getSupportFragmentManager();
            ComposeDialogFragment composeDialogFragment = ComposeDialogFragment.newInstance("Some Title", null, COMPOSE_REQUEST_CODE);
            composeDialogFragment.show(fm, "fragment_compose");
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
            binding.rvTweets.smoothScrollToPosition(0); // scroll back to top
        } else if (requestCode == DETAILS_REQUEST_CODE && resultCode == RESULT_OK) {
            populateHomeTimeline();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* Initial fetch of tweets on the user's timeline. */
    private void populateHomeTimeline() {
        client.getHomeTimeline("", new JsonHttpResponseHandler() {
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

    @Override
    public void onFinishComposeDialog(Tweet tweet, int requestCode) {
        tweets.add(0, tweet);
        adapter.notifyItemInserted(0);
        if (requestCode == COMPOSE_REQUEST_CODE) {
            binding.rvTweets.smoothScrollToPosition(0); // scroll back to top
        } else if (requestCode == REPLY_REQUEST_CODE) {
            Toast.makeText(this, "Added tweet to timeline", Toast.LENGTH_SHORT).show();
        }
    }
}