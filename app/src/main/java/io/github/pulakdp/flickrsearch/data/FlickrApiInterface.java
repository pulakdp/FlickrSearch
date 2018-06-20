package io.github.pulakdp.flickrsearch.data;

import io.github.pulakdp.flickrsearch.model.PhotoResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Author: PulakDebasish
 */

public interface FlickrApiInterface {
    @GET("/services/rest/?method=flickr.photos.search&format=json&nojsoncallback=1")
    Call<PhotoResponse> searchPhotos(@Query("api_key") String apiKey,
                                     @Query("tags") String query,
                                     @Query("page") int page,
                                     @Query("perpage") int perPage);
}
