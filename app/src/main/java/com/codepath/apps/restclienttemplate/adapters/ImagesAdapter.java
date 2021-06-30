package com.codepath.apps.restclienttemplate.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.databinding.ItemFollowerBinding;
import com.codepath.apps.restclienttemplate.databinding.ItemImageBinding;

import java.util.List;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    private final int RADIUS = 70;
    private final int MARGIN = 15;
    Context context;
    List<String> imageUrls;

    /* Constructor takes in the context and list of image urls. */
    public ImagesAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    /* For each image item, inflate the layout. */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemImageBinding binding = ItemImageBinding.inflate(LayoutInflater.from(context));
        return new ViewHolder(binding);
    }

    /* Fill in the ImageView's image based on the position of the image. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context)
                .load(imageUrl)
                .transform(new RoundedCornersTransformation(RADIUS, MARGIN))
                .override(Target.SIZE_ORIGINAL)
                .into(holder.ivImage);
    }

    /* Returns how many items are in the list of images. */
    @Override
    public int getItemCount() { return imageUrls.size(); }

    /* Defines a view holder for the image in the recycler view. */
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        public ViewHolder(ItemImageBinding binding) {
            super(binding.getRoot());
            ivImage = binding.ivImage;
        }
    }
}
