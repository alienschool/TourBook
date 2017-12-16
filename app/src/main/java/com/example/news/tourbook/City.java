package com.example.news.tourbook;

import com.google.gson.annotations.SerializedName;


public class City {
    @SerializedName("place_id")
    public String place_Id;

    @SerializedName("place_name")
    public String place_name;

    @SerializedName("address")
    public String address;

    @SerializedName("place_image")
    public String place_image;

    @SerializedName("response")
    public String response;

    @SerializedName("lat")
    public String lat;
    @SerializedName("lng")
    public String lng;

    @SerializedName("username")
    public String username;
    @SerializedName("profile_picture")
    public String profile_picture;
    @SerializedName("userid")
    public String userid;

    @SerializedName("travelCircleId")
    public String travelCircleId;
    @SerializedName("likes")
    public String likes;


}
