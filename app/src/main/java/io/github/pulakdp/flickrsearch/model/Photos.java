
package io.github.pulakdp.flickrsearch.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Photos {

    @SerializedName("page")
    @Expose
    public long page;

    @SerializedName("pages")
    @Expose
    public long pages;

    @SerializedName("perpage")
    @Expose
    public long perpage;

    @SerializedName("total")
    @Expose
    public String total;

    @SerializedName("photo")
    @Expose
    public List<Photo> photos = null;

    public List<Photo> getPhotoList() {
        return photos;
    }
}
