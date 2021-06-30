package com.codepath.apps.restclienttemplate;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import org.json.JSONException;
import okhttp3.Headers;

public class ComposeDialogFragment extends DialogFragment {

    private static int requestCode;
    public final String TAG = "ComposeDialogFragment";
    private static Tweet replyTweet;
    public final int MAX_TWEET_LENGTH = 280;
    private EditText etCompose;
    private Button btnTweet;

    public interface ComposeDialogListener {
        void onFinishComposeDialog(Tweet tweet, int requestCode);
    }

    public ComposeDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static ComposeDialogFragment newInstance(String title, Tweet tweet, int code) {
        ComposeDialogFragment frag = new ComposeDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        replyTweet = tweet;
        requestCode = code;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        etCompose = (EditText) view.findViewById(R.id.etCompose);
        btnTweet = (Button) view.findViewById(R.id.btnTweet);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
        // The user that wrote the original tweet is automatically "@" replied in compose
        if (replyTweet != null) {
            etCompose.setText("@" + replyTweet.user.screenName + " ");
        }
        // Show soft keyboard automatically and request focus to field
        etCompose.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // 2. Setup a callback when the "Done" button is pressed on keyboard
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
                String replyId = replyTweet == null? "": replyTweet.idStr;
                RestClient client = RestApplication.getRestClient(getContext());
                client.publishTweet(tweetContent, replyId, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        try {
                            // Pass the new tweet back and close the activity
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            ComposeDialogListener listener = (ComposeDialogListener) getActivity();
                            listener.onFinishComposeDialog(tweet, requestCode);
                            // Close the dialog and return back to the parent activity
                            dismiss();
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
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}