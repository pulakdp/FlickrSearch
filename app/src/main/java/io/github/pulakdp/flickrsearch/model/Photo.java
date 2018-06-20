
package io.github.pulakdp.flickrsearch.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Photo {

    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("owner")
    public String owner;

    @SerializedName("secret")
    @Expose
    public String secret;

    @SerializedName("server")
    @Expose
    public String server;

    @SerializedName("farm")
    @Expose
    public long farm;

    @SerializedName("title")
    @Expose
    public String title;

    @SerializedName("ispublic")
    public long ispublic;

    @SerializedName("isfriend")
    public long isfriend;

    @SerializedName("isfamily")
    public long isfamily;

    public String getTitle() {
        return title;
    }

    public String getPhotoUrl() {
        return "https://farm" +
                farm +
                ".staticflickr.com/" +
                server +
                "/" +
                id +
                "_" +
                secret +
                "_b.jpg";
    }

}
