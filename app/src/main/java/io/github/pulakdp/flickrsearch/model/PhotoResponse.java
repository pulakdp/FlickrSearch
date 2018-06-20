
package io.github.pulakdp.flickrsearch.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhotoResponse {

    @SerializedName("photos")
    @Expose
    public Photos photos;

    @SerializedName("stat")
    public String stat;

    public Photos getPhotos() {
        return photos;
    }
}
