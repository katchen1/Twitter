package com.codepath.apps.restclienttemplate.adapters;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.activities.TweetDetailsActivity;
import com.codepath.apps.restclienttemplate.activities.UserDetailsActivity;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;

import org.parceler.Parcels;

import java.util.List;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class FollowersAdapter extends RecyclerView.Adapter<FollowersAdapter.ViewHolder> {

    private final int RADIUS = 70;
    private final int MARGIN = 15;
    public final int USER_DETAIL_REQUEST_CODE = 4;
    Context context;
    List<User> followers;

    /* Constructor takes in the context and list of image urls. */
    public FollowersAdapter(Context context, List<User> followers) {
        this.context = context;
        this.followers = followers;
    }

    /* For each image item, inflate the layout. */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_follower, parent, false);
        return new ViewHolder(view);
    }

    /* Fill in the ImageView's image based on the position of the image. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User follower = followers.get(position);
        holder.bind(follower);
    }

    /* Returns how many items are in the list of images. */
    @Override
    public int getItemCount() { return followers.size(); }

    /* Defines a view holder for the image in the recycler view. */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivProfileImage;
        TextView tvName;
        ImageView ivVerified;
        TextView tvScreenName;
        TextView tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvName = itemView.findViewById(R.id.tvName);
            ivVerified = itemView.findViewById(R.id.ivVerified);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            itemView.setOnClickListener(this);
        }

        public void bind(User follower) {
            Glide.with(context).load(follower.profileImageUrl).circleCrop().into(ivProfileImage);
            tvName.setText(follower.name);
            if (follower.verified)
                ivVerified.setImageDrawable(context.getResources().getDrawable(R.drawable.twitter));
            tvScreenName.setText(follower.screenName);
            tvDescription.setText(follower.description);
        }

        @Override
        public void onClick(View view) {
            Intent i = new Intent(context, UserDetailsActivity.class);
            i.putExtra("user", Parcels.wrap(followers.get(getAdapterPosition())));
            ((Activity) context).startActivityForResult(i, USER_DETAIL_REQUEST_CODE);
        }
    }
}
