package com.codepath.apps.restclienttemplate.adapters;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.ComposeDialogFragment;
import com.codepath.apps.restclienttemplate.Constants;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.activities.TweetDetailsActivity;
import com.codepath.apps.restclienttemplate.databinding.ItemTweetBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import org.parceler.Parcels;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    private final String TAG = "TweetsAdapter";
    private Context context;
    private List<Tweet> tweets;

    /* Constructor takes in the context and list of tweets. */
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    /* For each tweet item, inflate the layout. */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTweetBinding binding = ItemTweetBinding.inflate(LayoutInflater.from(context));
        return new ViewHolder(binding);
    }

    /* Bind values of the tweet view based on the tweet's position in the list. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tweet tweet = tweets.get(position);
        holder.bind(tweet);
    }

    /* Returns the number of tweets in the recycler view. */
    @Override
    public int getItemCount() { return tweets.size(); }

    /* Clears the adapter and notifies that data has changed. */
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    /* Defines a view holder for a tweet. */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemTweetBinding binding;

        /* Constructor gets the components of the tweet view and adds on click listeners. */
        public ViewHolder(ItemTweetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(this);
            binding.imgBtnReply.setOnClickListener(this::imgBtnReplyOnClick);
            binding.imgBtnFavorite.setOnClickListener(this::imgBtnFavoriteOnClick);
            binding.imgBtnRetweet.setOnClickListener(this::imgBtnRetweetOnClick);
        }

        /* Binds the values of the tweet to the view holder's components. */
        public void bind(final Tweet tweet) {
            binding.tvName.setText(tweet.user.name);
            binding.tvRelativeTimestamp.setText(tweet.getRelativeTimeAgo());
            binding.tvScreenName.setText(String.format(Locale.US, "@%s", tweet.user.screenName));
            binding.tvFavoriteCount.setText(String.format(Locale.US, "%d", tweet.favoriteCount));
            binding.tvRetweetCount.setText(String.format(Locale.US, "%d", tweet.retweetCount));
            Glide.with(context).load(tweet.user.profileImageUrl).circleCrop().into(binding.ivProfileImage);
            if (tweet.user.verified) binding.ivVerified.setVisibility(View.VISIBLE);
            else binding.ivVerified.setVisibility(View.INVISIBLE);

            // Alter the body text based on whether or not it's a reply
            String bodyText = tweet.body;
            if (!tweet.inReplyToScreenName.equals("null")) {
                bodyText = "Replying to @" + tweet.inReplyToScreenName + "\n\n";
                if (tweet.body.startsWith("@"))
                    bodyText += tweet.body.substring(tweet.inReplyToScreenName.length() + 1);
                else
                    bodyText += tweet.body;
            }
            binding.tvBody.setText(bodyText);

            // Button icon depends on whether or not the user favorited or retweeted the tweet
            int favoriteIcon = tweet.favorited? R.drawable.ic_baseline_favorite_24: R.drawable.ic_baseline_favorite_border_24;
            int retweetIcon = tweet.retweeted? R.drawable.ic_vector_retweet: R.drawable.ic_vector_retweet_stroke;
            binding.imgBtnFavorite.setImageResource(favoriteIcon);
            binding.imgBtnRetweet.setImageResource(retweetIcon);

            // Populate the images view based on the number of images embedded in the tweet
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.btnRow.getLayoutParams();
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
            Glide.with(context)
                    .load(tweet.imageUrls.get(0))
                    .transform(new RoundedCornersTransformation(Constants.RADIUS, Constants.MARGIN))
                    .into(binding.ivImage);
        }

        /* Handles binding when the tweet has more than one image. */
        private void bindMultiImageView(Tweet tweet) {
            binding.rvImages.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            ImagesAdapter adapter = new ImagesAdapter(context, tweet.imageUrls);
            binding.rvImages.setAdapter(adapter);
            binding.ivImage.setVisibility(View.GONE);
            binding.rvImages.setVisibility(View.VISIBLE);
        }

        /* Displays a new activity via an Intent when the user clicks on a tweet. */
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition(); // get the tweet's position
            if (position != RecyclerView.NO_POSITION) { // ensure that the position is valid
                Tweet tweet = tweets.get(position); // get the tweet at the position

                // Navigate to the details activity and pass in the tweet
                Intent intent = new Intent(context, TweetDetailsActivity.class);
                intent.putExtra(Constants.TWEET_KEY_NAME, Parcels.wrap(tweet));
                intent.putExtra(Constants.POSITION_KEY_NAME, position);
                ((Activity) context).startActivityForResult(intent, Constants.TWEET_DETAIL_REQUEST_CODE);
            }
        }

        /* When the user clicks reply, create a compose fragment and indicate that it's a reply. */
        public void imgBtnReplyOnClick(View view) {
            Tweet tweet = tweets.get(getAdapterPosition());
            FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
            ComposeDialogFragment composeDialogFragment = ComposeDialogFragment.newInstance("Some Title", tweet, Constants.REPLY_REQUEST_CODE);
            composeDialogFragment.show(fm, "fragment_compose");
        }

        /* When the user clicks on favorite, send a request it and update the UI. */
        public void imgBtnFavoriteOnClick(View view) {
            final Tweet tweet = tweets.get(getAdapterPosition());
            RestClient client = RestApplication.getRestClient(view.getContext());
            client.favorite(tweet.id, tweet.favorited, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    if (tweet.favorited) {
                        tweet.favoriteCount--;
                        tweet.favorited = false;
                    } else {
                        tweet.favoriteCount++;
                        tweet.favorited = true;
                    }
                    notifyItemChanged(getAdapterPosition());
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e(TAG, "onFailure to favorite or un-favorite tweet " + response, throwable);
                }
            });
        }

        /* When the user clicks retweet, send a request and update the UI. */
        public void imgBtnRetweetOnClick(View view) {
            final Tweet tweet = tweets.get(getAdapterPosition());
            RestClient client = RestApplication.getRestClient(view.getContext());
            client.retweet(tweet.id, tweet.retweeted, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    if (tweet.retweeted) {
                        tweet.retweetCount--;
                        tweet.retweeted = false;
                    } else {
                        tweet.retweetCount++;
                        tweet.retweeted = true;
                    }
                    notifyItemChanged(getAdapterPosition());
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e(TAG, "onFailure to favorite or un-favorite tweet " + response, throwable);
                }
            });
        }
    }
}
