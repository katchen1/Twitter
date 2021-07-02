package com.codepath.apps.restclienttemplate;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import org.json.JSONException;

import java.util.Locale;

import okhttp3.Headers;

public class ComposeDialogFragment extends DialogFragment {

    private final String TAG = "ComposeDialogFragment";
    private static Tweet replyTweet;
    private EditText etCompose;
    private Button btnTweet;
    private static int requestCode;

    public interface ComposeDialogListener {
        void onFinishComposeDialog(Tweet tweet);
    }

    /* Empty constructor is required for DialogFragment. Don't use this. Use newInstance instead. */
    public ComposeDialogFragment() { }

    /* Use this constructor. Pass in a title, the tweet being replied to (can be null), and the request code. */
    public static ComposeDialogFragment newInstance(String title, Tweet tweet, int code) {
        ComposeDialogFragment frag = new ComposeDialogFragment();
        Bundle args = new Bundle();
        args.putString(Constants.TITLE_KEY_NAME, title);
        frag.setArguments(args);
        replyTweet = tweet;
        requestCode = code;
        return frag;
    }

    /* Inflates the view with the layout. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose, container);
    }

    /* Called when the compose dialog fragment is created. */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get field from view
        etCompose = (EditText) view.findViewById(R.id.etCompose);
        btnTweet = (Button) view.findViewById(R.id.btnTweet);
        if (requestCode == Constants.COMPOSE_REQUEST_CODE) {
            etCompose.setHint("What's happening?");
        } else if (requestCode == Constants.REPLY_REQUEST_CODE) {
            etCompose.setHint("Tweet your reply");
        }


        // Fetch arguments from bundle and set title
        String title = "";
        Bundle arguments = getArguments();
        if (arguments != null) title = arguments.getString(Constants.TITLE_KEY_NAME, "Title");
        if (getDialog() != null) getDialog().setTitle(title);

        // The user that wrote the original tweet is automatically "@" replied in compose
        if (replyTweet != null) {
            etCompose.setText(String.format(Locale.US, "@%s ", replyTweet.user.screenName));
        }

        // Show soft keyboard automatically and request focus to field
        etCompose.requestFocus();
        Window window = getDialog().getWindow();
        if (window != null) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Setup a callback when the "Done" button is pressed on keyboard
        btnTweet.setOnClickListener(this::doneOnClick);
    }

    /* Helper method for making a short toast. */
    private void makeToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /* When the user clicks done, validate content, post the tweet, and close the dialog. */
    public void doneOnClick(View v) {
        String tweetContent = etCompose.getText().toString();

        // Validate content
        if (tweetContent.isEmpty()) {
            makeToast("Sorry, your tweet can't be empty");
            return;
        } else if (tweetContent.length() > Constants.MAX_TWEET_LENGTH) {
            makeToast("Sorry, your tweet is too long");
            return;
        }

        // Make an API call to Twitter to publish the tweet
        Long replyId = replyTweet == null? (long) -1: replyTweet.id;
        RestClient client = RestApplication.getRestClient(getContext());
        client.publishTweet(tweetContent, replyId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    // Pass the new tweet back and close the dialog
                    Tweet tweet = Tweet.fromJson(json.jsonObject);
                    ComposeDialogListener listener = (ComposeDialogListener) getActivity();
                    if (listener != null) listener.onFinishComposeDialog(tweet);
                    dismiss();
                } catch (JSONException e) {
                    Log.e(TAG, "jsonObject error", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to publish tweet " + response, throwable);
            }
        });
    }
}