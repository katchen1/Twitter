package com.codepath.apps.restclienttemplate.activities;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.codepath.apps.restclienttemplate.ComposeDialogFragment;
import com.codepath.apps.restclienttemplate.Constants;
import com.codepath.apps.restclienttemplate.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
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

    private final String TAG = "TimelineActivity";
    private ActivityTimelineBinding binding; // The view binding
    private List<Tweet> tweets; // List of tweets shown in the timeline recycler view
    private TweetsAdapter adapter; // Adapter for the recycler view
    private RestClient client; // For making Twitter API calls
    private MenuItem miActionProgressItem; // Shown when any background or network task is happening
    private EndlessRecyclerViewScrollListener scrollListener; // For endless pagination

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View binding to reduce view boilerplate
        binding = ActivityTimelineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("");

        // Initialize member variables
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        client = RestApplication.getRestClient(this);

        // Set up the timeline recycler view with divider between items
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.rvTweets.setLayoutManager(linearLayoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        binding.rvTweets.addItemDecoration(divider);
        binding.rvTweets.setAdapter(adapter);

        // Remove the default on-change animations of the recycler view
        SimpleItemAnimator animator = (SimpleItemAnimator) binding.rvTweets.getItemAnimator();
        if (animator != null) animator.setSupportsChangeAnimations(false);

        // Initial fetch of tweets
        appendHomeTimeline((long) -1);

        // Setup the swipe refresh listener which triggers new data loading
        binding.swipeContainer.setOnRefreshListener(this::refreshTimelineAsync);
        binding.swipeContainer.setColorSchemeResources(R.color.twitter_blue);

        // Enable endless pagination on the recycler view
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Append older tweets to the bottom of the list
                Long maxId = tweets.get(tweets.size() - 1).id - 1;
                appendHomeTimeline(maxId);
                scrollListener.resetState();
            }
        };
        binding.rvTweets.addOnScrollListener(scrollListener);
    }

    /* Fetches tweets in the user's timeline with ids limited to maxId or below.
     * maxId is ignored is -1 is passed in. */
    private void appendHomeTimeline(Long maxId) {
        if (miActionProgressItem != null) showProgressBar();
        client.getHomeTimeline(maxId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    // Add the tweets to the list and notify the adapter of the change
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                    binding.swipeContainer.setRefreshing(false);
                    if (miActionProgressItem != null) hideProgressBar();
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

    /* When the user pulls down to refresh, clear and re-fetch the timeline. */
    public void refreshTimelineAsync() {
        tweets.clear();
        appendHomeTimeline((long) -1);
    }

    /* Adds items to the action bar and returns true for the menu to be displayed. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /* Initializes the menu item containing the progress loading indicator. */
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
        // Compose icon: open a compose modal overlay
        if (item.getItemId() == R.id.compose) {
            FragmentManager fm = getSupportFragmentManager();
            ComposeDialogFragment cdf;
            cdf = ComposeDialogFragment.newInstance("Compose", null, Constants.COMPOSE_REQUEST_CODE);
            cdf.show(fm, "fragment_compose");
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
        if (requestCode == Constants.TWEET_DETAIL_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Update the tweet whose details were shown
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra(Constants.TWEET_KEY_NAME));
            int position = data.getIntExtra(Constants.POSITION_KEY_NAME, -1);
            tweets.set(position, tweet);
            adapter.notifyItemChanged(position);

            // If the user replied to the tweet, insert the reply and scroll to top
            if (data.hasExtra(Constants.TWEET_ADDED_KEY_NAME)) {
                Tweet reply = Parcels.unwrap(data.getParcelableExtra(Constants.TWEET_ADDED_KEY_NAME));
                onFinishComposeDialog(reply);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* When the user finishes composing, add it to the top. */
    @Override
    public void onFinishComposeDialog(Tweet tweet) {
        tweets.add(0, tweet);
        adapter.notifyItemInserted(0);
        binding.rvTweets.smoothScrollToPosition(0);
        Toast.makeText(this, "Tweet added", Toast.LENGTH_SHORT).show();
    }
}