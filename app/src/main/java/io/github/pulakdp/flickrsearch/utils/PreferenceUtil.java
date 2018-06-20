package io.github.pulakdp.flickrsearch.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

/**
 * Author: PulakDebasish
 */

public class PreferenceUtil {

    public static final String PHOTO_GRID_SIZE = "photo_grid_size";

    private static PreferenceUtil sInstance;

    private final SharedPreferences mPreferences;

    private PreferenceUtil(@NonNull final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceUtil getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    public void setPhotoGridSize(final int gridSize) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(PHOTO_GRID_SIZE, gridSize);
        editor.apply();
    }

    public final int getPhotoGridSize(Context context) {
        return mPreferences.getInt(PHOTO_GRID_SIZE, 2);
    }

}
