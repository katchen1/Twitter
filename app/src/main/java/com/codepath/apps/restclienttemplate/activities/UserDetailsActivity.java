package com.codepath.apps.restclienttemplate.activities;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.apps.restclienttemplate.R;

import org.parceler.Parcels;

public class UserDetailsActivity extends AppCompatActivity {

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        ImageView ivProfileBanner = findViewById(R.id.ivProfileBanner);
        ImageView ivProfileImage = findViewById(R.id.ivProfileImage);
        TextView tvName = findViewById(R.id.tvName);
        ImageView ivVerified = findViewById(R.id.ivVerified);
        TextView tvScreenName = findViewById(R.id.tvScreenName);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvLocation = findViewById(R.id.tvLocation);
        TextView tvUrl = findViewById(R.id.tvUrl);
        TextView tvCreatedAt = findViewById(R.id.tvCreatedAt);
        TextView tvFollowingCount = findViewById(R.id.tvFollowingCount);
        TextView tvFollowerCount = findViewById(R.id.tvFollowerCount);

        if (!user.profileBannerUrl.isEmpty())
            Glide.with(this).load(user.profileBannerUrl).into(ivProfileBanner);
        Glide.with(this).load(user.profileImageUrl).circleCrop().into(ivProfileImage);
        tvName.setText(user.name);
        if (user.verified) ivVerified.setImageDrawable(getResources().getDrawable(R.drawable.twitter));
        tvScreenName.setText("@" + user.screenName);
        tvDescription.setText(user.description);
        tvLocation.setText(user.location);
        tvUrl.setText(user.url);
        tvCreatedAt.setText(user.createdAt);
        tvFollowingCount.setText(user.friendsCount.toString());
        tvFollowerCount.setText(user.followerCount.toString());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("user", Parcels.wrap(user));
        setResult(RESULT_OK, intent);
        finish();
    }
}