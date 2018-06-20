package io.github.pulakdp.flickrsearch.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import io.github.pulakdp.flickrsearch.R;
import uk.co.senab.photoview.PhotoView;

/**
 * Author: PulakDebasish
 */

public class FullScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_view);

        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        String photoUrl = getIntent().getStringExtra("url");
        if (!photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .into(photoView);
        }
    }
}
