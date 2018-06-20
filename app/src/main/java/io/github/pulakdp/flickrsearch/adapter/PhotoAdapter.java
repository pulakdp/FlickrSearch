package io.github.pulakdp.flickrsearch.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.pulakdp.flickrsearch.ui.activities.FullScreenActivity;
import io.github.pulakdp.flickrsearch.R;
import io.github.pulakdp.flickrsearch.model.Photo;
import io.github.pulakdp.flickrsearch.utils.FlickrSearchUtil;
import io.github.pulakdp.flickrsearch.views.SquareImageView;

/**
 * Author: PulakDebasish
 */

public class PhotoAdapter extends BaseAdapter<Photo> {

    private AppCompatActivity activity;
    private FooterItemViewHolder footerViewHolder;

    public PhotoAdapter(AppCompatActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (isLastPosition(position)) ? FOOTER : ITEM;
    }

    public boolean isLastPosition(int position) {
        return (position == items.size() - 1);
    }

    @Override
    protected RecyclerView.ViewHolder createItemViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(activity).inflate(R.layout.grid_item_photo, parent, false);
        return new PhotoItemViewHolder(itemView);
    }

    @Override
    protected RecyclerView.ViewHolder createFooterViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(activity).inflate(R.layout.footer_item_layout, parent, false);
        return new FooterItemViewHolder(itemView);
    }

    @Override
    protected void bindItemViewHolder(PhotoItemViewHolder viewHolder, int position) {
        final PhotoItemViewHolder holder = viewHolder;

        Photo photo = items.get(position);
        holder.title.setText(photo.getTitle());
        Glide.with(activity)
                .load(photo.getPhotoUrl())
                .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .apply(new RequestOptions().onlyRetrieveFromCache(!FlickrSearchUtil.hasInternetConnection(activity)))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Bitmap photo = ((BitmapDrawable) resource).getBitmap();
                        Palette.from(photo).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                Palette.Swatch swatch = palette.getVibrantSwatch();
                                if (swatch != null) {
                                    int vibrantColor = swatch.getRgb();
                                    int textColor = FlickrSearchUtil.getBlackWhiteColor(vibrantColor);
                                    holder.photoFooter.setBackgroundColor(vibrantColor);
                                    holder.title.setTextColor(textColor);
                                } else {
                                    Palette.Swatch mutedSwatch = palette.getMutedSwatch();
                                    if (mutedSwatch != null) {
                                        int mutedColor = mutedSwatch.getRgb();
                                        int textColor = FlickrSearchUtil.getBlackWhiteColor(mutedColor);
                                        holder.photoFooter.setBackgroundColor(mutedColor);
                                        holder.title.setTextColor(textColor);
                                    }
                                }
                            }
                        });
                        return false;
                    }
                })
                .into(viewHolder.photo);
    }

    @Override
    protected void bindFooterViewHolder(FooterItemViewHolder viewHolder) {
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.setFullSpan(true);
        footerViewHolder = viewHolder;
    }

    public void updateFooter(FooterType footerType) {
        switch (footerType) {
            case LOAD_MORE:
                displayLoadMoreFooter();
                break;
            case ERROR:
                displayErrorFooter();
                break;
            default:
                break;
        }
    }

    protected void displayLoadMoreFooter() {
        if (footerViewHolder != null) {
            footerViewHolder.errorView.setVisibility(View.GONE);
            footerViewHolder.progressBar.setVisibility(View.VISIBLE);
        }
    }

    protected void displayErrorFooter() {
        if (footerViewHolder != null) {
            footerViewHolder.progressBar.setVisibility(View.GONE);
            footerViewHolder.errorView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void addFooter() {
        isFooterAdded = true;
        add(new Photo());
    }

    public class PhotoItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.photo)
        SquareImageView photo;

        @BindView(R.id.photo_footer)
        View photoFooter;

        @BindView(R.id.photo_title)
        TextView title;

        public PhotoItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @SuppressLint("NewApi")
        @Override
        public void onClick(View view) {
            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, photo, photo.getTransitionName()).toBundle();
            activity.startActivity(new Intent(activity, FullScreenActivity.class)
                    .putExtra(activity.getString(R.string.photo_url), getItem(getAdapterPosition()).getPhotoUrl()), bundle);
        }
    }

    public class FooterItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.loading_bar)
        ProgressBar progressBar;

        @BindView(R.id.error_layout)
        View errorView;

        @BindView(R.id.reload_button)
        Button reloadButton;

        public FooterItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            progressBar.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
            reloadButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onReloadClickListener != null) {
                onReloadClickListener.onReloadClick();
            }
        }
    }
}
