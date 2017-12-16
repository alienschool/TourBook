package com.example.news.tourbook;

import com.google.gson.annotations.SerializedName;

/**
 * Created by osamaiqbal on 20/11/2017.
 */

public class Review {
    @SerializedName("id")
    public String id;

    @SerializedName("username")
    public String username;

    @SerializedName("review")
    public String review;

    @SerializedName("rating")
    public String rating;

    @SerializedName("averageRating")
    public String averageRating;

    @SerializedName("time")
    public String time;

    @SerializedName("place_id")
    public String place_id;

    @SerializedName("response")
    public String response;
}
