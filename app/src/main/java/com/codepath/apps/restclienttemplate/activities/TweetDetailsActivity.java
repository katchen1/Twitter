package com.codepath.apps.restclienttemplate.activities;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.ComposeDialogFragment;
import com.codepath.apps.restclienttemplate.Constants;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.adapters.ImagesAdapter;
import com.codepath.apps.restclienttemplate.databinding.ActivityTweetDetailsBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import org.parceler.Parcels;
import java.util.Locale;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetDetailsActivity extends AppCompatActivity implements ComposeDialogFragment.ComposeDialogListener {

    private final String TAG = "TweetDetailsActivity";
    private Tweet tweet; // The tweet being displayed
    private int position; // Position of the tweet in the adapter
    ActivityTweetDetailsBinding binding; // For view binding
    RestClient client; // For making Twitter API calls

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply view binding to reduce view boilerplate
        binding = ActivityTweetDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle("Tweet");

        // Initialize member variables
        tweet = Parcels.unwrap(getIntent().getParcelableExtra(Constants.TWEET_KEY_NAME));
        position = getIntent().getIntExtra(Constants.POSITION_KEY_NAME, -1);
        client = RestApplication.getRestClient(this);

        // Populate the view's components
        Glide.with(this).load(tweet.user.profileImageUrl).circleCrop().into(binding.ivProfileImage);
        binding.tvScreenName.setText(String.format(Locale.US,"@%s", tweet.user.screenName));
        binding.tvRetweetCount.setText(String.format(Locale.US, "%d", tweet.retweetCount));
        binding.tvFavoriteCount.setText(String.format(Locale.US, "%d", tweet.favoriteCount));
        binding.tvBody.setText(tweet.body);
        binding.tvName.setText(tweet.user.name);
        binding.tvCreatedAt.setText(tweet.getCreatedAt());

        // Determine the states of the retweeted and favorited icons
        int favoriteIcon = tweet.favorited? R.drawable.ic_baseline_favorite_24: R.drawable.ic_baseline_favorite_border_24;
        int retweetIcon = tweet.retweeted? R.drawable.ic_vector_retweet: R.drawable.ic_vector_retweet_stroke;
        binding.imgBtnFavorite.setImageResource(favoriteIcon);
        binding.imgBtnRetweet.setImageResource(retweetIcon);

        // Populate the images view based on the number of images embedded in the tweet
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.tvCreatedAt.getLayoutParams();
        if (tweet.imageUrls.isEmpty()) {
            binding.rvImages.setVisibility(View.GONE);
            binding.ivImage.setVisibility(View.GONE);
            params.addRule(RelativeLayout.BELOW, R.id.tvBody);
        } else if (tweet.imageUrls.size() == 1) {
            bindSingleImageView(tweet);
            params.addRule(RelativeLayout.BELOW, R.id.ivImage);
        } else {
            bindMultiImageView(tweet);
            params.addRule(RelativeLayout.BELOW, R.id.rvImages);
        }
    }

    /* Handles binding when the tweet has exactly one image. */
    private void bindSingleImageView(Tweet tweet) {
        binding.rvImages.setVisibility(View.GONE);
        binding.ivImage.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(tweet.imageUrls.get(0))
                .transform(new RoundedCornersTransformation(Constants.RADIUS, Constants.MARGIN))
                .into(binding.ivImage);
    }

    /* Handles binding when the tweet has more than one image. */
    private void bindMultiImageView(Tweet tweet) {
        binding.ivImage.setVisibility(View.GONE);
        binding.rvImages.setVisibility(View.VISIBLE);
        binding.rvImages.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        ImagesAdapter adapter = new ImagesAdapter(this, tweet.imageUrls);
        binding.rvImages.setAdapter(adapter);
    }

    /* When reply is clicked, compose a tweet and pass in the tweet being replied to. */
    public void replyOnClick(View view) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialogFragment cdf;
        cdf = ComposeDialogFragment.newInstance("Reply", tweet, Constants.REPLY_REQUEST_CODE);
        cdf.show(fm, "fragment_reply");
    }

    /* When retweet is clicked, retweet or un-retweet the tweet. */
    public void retweetOnClick(View view) {
        RestClient client = RestApplication.getRestClient(binding.imgBtnFavorite.getContext());
        client.retweet(tweet.id, tweet.retweeted, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                if (tweet.retweeted) {
                    tweet.retweetCount--;
                    tweet.retweeted = false;
                    binding.imgBtnRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
                } else {
                    tweet.retweetCount++;
                    tweet.retweeted = true;
                    binding.imgBtnRetweet.setImageResource(R.drawable.ic_vector_retweet);
                }
                binding.tvRetweetCount.setText(String.format(Locale.US, "%d", tweet.retweetCount));
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to favorite or un-favorite tweet " + response, throwable);
            }
        });
    }

    /* When the favorite button is clicked, favorite or un-favorite the tweet. */
    public void favoriteOnClick(View view) {
        client.favorite(tweet.id, tweet.favorited, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                if (tweet.favorited) {
                    tweet.favoriteCount--;
                    tweet.favorited = false;
                    binding.imgBtnFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                } else {
                    tweet.favoriteCount++;
                    tweet.favorited = true;
                    binding.imgBtnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24);
                }
                binding.tvFavoriteCount.setText(String.format(Locale.US, "%d", tweet.retweetCount));
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to favorite or un-favorite tweet " + response, throwable);
            }
        });
    }

    /* If back is pressed, return to the timeline activity and update the tweet. */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.POSITION_KEY_NAME, position);
        intent.putExtra(Constants.TWEET_KEY_NAME, Parcels.wrap(tweet));
        setResult(RESULT_OK, intent);
        finish();
    }

    /* If finish replying, return to the timeline and update both the tweet and the newly added tweet. */
    @Override
    public void onFinishComposeDialog(Tweet t) {
        Intent intent = new Intent();
        intent.putExtra(Constants.POSITION_KEY_NAME, position);
        intent.putExtra(Constants.TWEET_KEY_NAME, Parcels.wrap(tweet));
        intent.putExtra(Constants.TWEET_ADDED_KEY_NAME, Parcels.wrap(t));
        setResult(RESULT_OK, intent);
        finish();
    }

    /* If the user of the tweet is clicked, view details of that user. */
    public void userOnClick(View view) {
        Intent i = new Intent(TweetDetailsActivity.this, UserDetailsActivity.class);
        i.putExtra(Constants.USER_KEY_NAME, Parcels.wrap(tweet.user));
        startActivityForResult(i, Constants.USER_DETAIL_REQUEST_CODE);
    }

    /* When coming back from a user detail activity, update the user that was passed back. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.USER_DETAIL_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            tweet.user = Parcels.unwrap(data.getParcelableExtra(Constants.USER_KEY_NAME));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}