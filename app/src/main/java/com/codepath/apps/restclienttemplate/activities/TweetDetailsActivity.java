package com.codepath.apps.restclienttemplate.activities;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.ComposeDialogFragment;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.adapters.ImagesAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import org.parceler.Parcels;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetDetailsActivity extends AppCompatActivity implements ComposeDialogFragment.ComposeDialogListener {

    public final int RADIUS = 70;
    public final int MARGIN = 15;
    public final String TAG = "TweetDetailsActivity";
    public final int REPLY_REQUEST_CODE = 2;
    ImageView ivProfileImage;
    TextView tvName, tvScreenName, tvBody;
    RecyclerView rvImages;
    ImageView ivImage;
    TextView tvRetweetCount, tvFavoriteCount;
    TextView tvCreatedAt;
    View vDivider1;
    LinearLayout statsRow;
    Tweet tweet;
    ImageButton imgBtnRetweet, imgBtnFavorite;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);
        getSupportActionBar().setTitle("");

        tweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
        position = getIntent().getIntExtra("position", -1);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvName = findViewById(R.id.tvName);
        tvScreenName = findViewById(R.id.tvScreenName);
        tvBody = findViewById(R.id.tvBody);
        rvImages = findViewById(R.id.rvImages);
        ivImage = findViewById(R.id.ivImage);
        tvRetweetCount = findViewById(R.id.tvRetweetCount);
        tvFavoriteCount = findViewById(R.id.tvFavoriteCount);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        vDivider1 = findViewById(R.id.vDivider1);
        statsRow = findViewById(R.id.statsRow);
        imgBtnRetweet = findViewById(R.id.imgBtnRetweet);
        imgBtnFavorite = findViewById(R.id.imgBtnFavorite);

        Glide.with(this).load(tweet.user.profileImageUrl).circleCrop().into(ivProfileImage);
        tvName.setText(tweet.user.name);
        tvScreenName.setText("@" + tweet.user.screenName);
        tvBody.setText(tweet.body);
        tvRetweetCount.setText(tweet.retweetCount.toString());
        tvFavoriteCount.setText(tweet.favoriteCount.toString());
        tvCreatedAt.setText(tweet.getCreatedAt());

        int favoriteIcon = tweet.favorited? R.drawable.ic_vector_heart: R.drawable.ic_vector_heart_stroke;
        int retweetIcon = tweet.retweeted? R.drawable.ic_vector_retweet: R.drawable.ic_vector_retweet_stroke;
        imgBtnFavorite.setImageResource(favoriteIcon);
        imgBtnRetweet.setImageResource(retweetIcon);

        // Populate the images view based on the number of images embedded in the tweet
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvCreatedAt.getLayoutParams();
        if (tweet.imageUrls.isEmpty()) {
            rvImages.setVisibility(View.GONE);
            ivImage.setVisibility(View.GONE);
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
        rvImages.setVisibility(View.GONE);
        ivImage.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(tweet.imageUrls.get(0))
                .transform(new RoundedCornersTransformation(RADIUS, MARGIN))
                .into(ivImage);
    }

    /* Handles binding when the tweet has more than one image. */
    private void bindMultiImageView(Tweet tweet) {
        ivImage.setVisibility(View.GONE);
        rvImages.setVisibility(View.VISIBLE);
        rvImages.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        ImagesAdapter adapter = new ImagesAdapter(this, tweet.imageUrls);
        rvImages.setAdapter(adapter);
    }

    // When the reply button is clicked
    public void replyOnClick(View view) {
        // Compose a tweet and pass in the tweet being replied to
        //Intent i = new Intent(this, ComposeActivity.class);
        //i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
        //startActivityForResult(i, REPLY_REQUEST_CODE);

        FragmentManager fm = getSupportFragmentManager();
        ComposeDialogFragment composeDialogFragment = ComposeDialogFragment.newInstance("Some Title", tweet, REPLY_REQUEST_CODE);
        composeDialogFragment.show(fm, "fragment_compose");
    }

    // When the retweet button is clicked
    public void retweetOnClick(View view) {
        // retweet or un-retweet the tweet
        RestClient client = RestApplication.getRestClient(imgBtnFavorite.getContext());
        client.retweet(tweet.idStr, tweet.retweeted, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                if (tweet.retweeted) {
                    tweet.retweetCount--;
                    tweet.retweeted = false;
                    imgBtnRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
                } else {
                    tweet.retweetCount++;
                    tweet.retweeted = true;
                    imgBtnRetweet.setImageResource(R.drawable.ic_vector_retweet);
                }
                tvRetweetCount.setText(tweet.retweetCount.toString());
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to favorite or un-favorite tweet " + response, throwable);
            }
        });
    }

    // When the favorite button is clicked
    public void favoriteOnClick(View view) {
        RestClient client = RestApplication.getRestClient(this);
        client.favorite(tweet.idStr, tweet.favorited, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                if (tweet.favorited) {
                    tweet.favoriteCount--;
                    tweet.favorited = false;
                    imgBtnFavorite.setImageResource(R.drawable.ic_vector_heart_stroke);
                } else {
                    tweet.favoriteCount++;
                    tweet.favorited = true;
                    imgBtnFavorite.setImageResource(R.drawable.ic_vector_heart);
                }
                tvFavoriteCount.setText(tweet.favoriteCount.toString());
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to favorite or un-favorite tweet " + response, throwable);
            }
        });
    }

    public void shareOnClick(View view) {

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("position", position);
        intent.putExtra("tweet", Parcels.wrap(tweet));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onFinishComposeDialog(Tweet tweet, int requestCode) {
        if (requestCode == REPLY_REQUEST_CODE) {
            Toast.makeText(this, "Added tweet to timeline", Toast.LENGTH_SHORT).show();
        }
    }
}