package com.codepath.apps.restclienttemplate.activities;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import org.json.JSONException;
import org.parceler.Parcels;
import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    public static final String TAG = "ComposeActivity";
    public static final int MAX_TWEET_LENGTH = 280;
    EditText etCompose;
    Button btnTweet;
    RestClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        client = RestApplication.getRestClient(this);
        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);

        // Get the tweet being replied to
        Tweet replyTweet = null;
        if (getIntent().hasExtra(Tweet.class.getSimpleName())) {
            replyTweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
        }

        // The user that wrote the original tweet is automatically "@" replied in compose
        if (replyTweet != null) {
            etCompose.setText("@" + replyTweet.user.screenName + " ");
        }

        // Set click listener on the tweet button
        final Tweet finalReplyTweet = replyTweet;
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etCompose.getText().toString();

                // Validate content
                if (tweetContent.isEmpty()) {
                    makeToast("Sorry, your tweet can't be empty");
                    return;
                } else if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    makeToast("Sorry, your tweet is too long");
                    return;
                }

                // Make an API call to Twitter to publish the tweet
                String replyId = finalReplyTweet == null? "": finalReplyTweet.idStr;
                client.publishTweet(tweetContent, replyId, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        try {
                            // Pass the new tweet back and close the activity
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (JSONException e) {
                            // Log the error
                            Log.e(TAG, "jsonObject error");
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        // Log the error
                        Log.e(TAG, "onFailure to publish tweet " + response, throwable);
                    }
                });
            }
        });
    }

    /* Helper method for making a short toast. */
    private void makeToast(String message) {
        Toast.makeText(ComposeActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}