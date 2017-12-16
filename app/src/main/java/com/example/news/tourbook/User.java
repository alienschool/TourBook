package com.example.news.tourbook;

import com.google.gson.annotations.SerializedName;

class User {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("username")
    public String username;

    @SerializedName("profile_picture")
    public String profile_picture;

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    @SerializedName("dob")
    public String dob;

    @SerializedName("gender")
    public String gender;

    @SerializedName("rank")
    public String rank;

    @SerializedName("followers")
    public String followers;

    @SerializedName("following")
    public String following;

    @SerializedName("checkIn")
    public String checkin;

    @SerializedName("registrationKey")
    public String registrationKey;

    @SerializedName("response")
    public String response;

}
