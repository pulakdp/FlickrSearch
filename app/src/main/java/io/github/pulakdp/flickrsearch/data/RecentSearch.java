package io.github.pulakdp.flickrsearch.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.github.pulakdp.flickrsearch.model.Photo;

/**
 * Author: PulakDebasish
 */

public class RecentSearch extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "recent_search.db";

    private static final int VERSION = 2;

    private static RecentSearch sInstance = null;

    public RecentSearch(final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @SuppressLint("SQLiteString")
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + RecentSearchColumns.NAME + " ("
                + RecentSearchColumns.SEARCH_STRING + " STRING NOT NULL,"
                + RecentSearchColumns.TITLE + " STRING NOT NULL,"
                + RecentSearchColumns.FARM + " LONG NOT NULL,"
                + RecentSearchColumns.SERVER + " STRING NOT NULL,"
                + RecentSearchColumns.ID + " STRING NOT NULL,"
                + RecentSearchColumns.SECRET + " STRING NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    public static synchronized RecentSearch getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new RecentSearch(context.getApplicationContext());
        }
        return sInstance;
    }

    public void addRecentSearch(String query, List<Photo> photos) {

        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        try {
            for (int i = 0; i < photos.size(); i++) {

                ContentValues contentValues = new ContentValues();
                contentValues.put(RecentSearchColumns.SEARCH_STRING, query);
                contentValues.put(RecentSearchColumns.TITLE, photos.get(i).title);
                contentValues.put(RecentSearchColumns.FARM, photos.get(i).farm);
                contentValues.put(RecentSearchColumns.SERVER, photos.get(i).server);
                contentValues.put(RecentSearchColumns.ID, photos.get(i).id);
                contentValues.put(RecentSearchColumns.SECRET, photos.get(i).secret);

                database.insert(RecentSearchColumns.NAME, null, contentValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public List<Photo> getPhotosForQuery(String query) {
        final SQLiteDatabase database = getReadableDatabase();
        database.beginTransaction();

        Log.d("gettingFromCache", query);
        Cursor cursor = null;
        List<Photo> photos = new ArrayList<>();
        try {
            cursor = database.query(RecentSearchColumns.NAME,
                    new String[]{
                            RecentSearchColumns.TITLE,
                            RecentSearchColumns.FARM,
                            RecentSearchColumns.SERVER,
                            RecentSearchColumns.ID,
                            RecentSearchColumns.SECRET
                    },
                    RecentSearchColumns.SEARCH_STRING + " = ? COLLATE NOCASE",
                    new String[]{query}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Photo photo = new Photo();
                    photo.title = cursor.getString(0);
                    photo.farm = cursor.getLong(1);
                    photo.server = cursor.getString(2);
                    photo.id = cursor.getString(3);
                    photo.secret = cursor.getString(4);

                    Log.d("Photo Object", photo.title);

                    photos.add(photo);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor != null)
            cursor.close();
        return photos;

    }

    public interface RecentSearchColumns {
        String NAME = "recent_search";

        String SEARCH_STRING = "search_string";

        String TITLE = "title";

        String FARM = "farm";

        String SERVER = "server";

        String ID = "id";

        String SECRET = "secret";
    }
}
