package com.example.news.tourbook;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


public interface APIMyInterface {

    @FormUrlEncoded
    @POST("login-script.php")
    Call<User> Login(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("validate-key-script.php")
    Call<User> ValidateKey(@Field("id") String id,@Field("registrationKey") String registrationKey);

    @FormUrlEncoded
    @POST("signup-script.php")
    Call<User> Signup(@Field("name") String name, @Field("username") String username, @Field("email") String email, @Field("password") String password, @Field("gender") String gender);

    @FormUrlEncoded
    @POST("home-places-script.php")
    Call<List<City>> GetMainCities(@Field("city") String city);

    @FormUrlEncoded
    @POST("home-guides-script.php")
    Call<List<User>> GetMainGuides(@Field("temp") String temp);

    @FormUrlEncoded
    @POST("following-script.php")
    Call<List<User>> GetFollowing(@Field("username") String username);

    @FormUrlEncoded
    @POST("followers-script.php")
    Call<List<User>> GetFollowers(@Field("username") String username);

    @FormUrlEncoded
    @POST("following-Checkins-script.php")
    Call<List<City>> GetFollowingCheckins(@Field("username") String username);

    @FormUrlEncoded
    @POST("travelCircle-like-script.php")
    Call<City> LikeTravelCircle(@Field("travelCircleId") String travelCircleId,@Field("userId") String userId);

    @FormUrlEncoded
    @POST("profile-checkins-script.php")
    Call<List<City>> GetCheckins(@Field("username") String username);

    @FormUrlEncoded
    @POST("checkin-script.php")
    Call<City> Checkin(@Field("lat") String lat,@Field("lng") String lng,
                       @Field("place_id") String place_id,@Field("placename") String placename,
                       @Field("username") String username);

    @FormUrlEncoded
    @POST("placeDetail-script.php")
    Call<City> GetPlaceDetail(@Field("id") String id);

    @FormUrlEncoded
    @POST("placeDetail-getReviews-script.php")
    Call<List<Review>> GetPlaceReviews(@Field("place_id") String place_id);

    @FormUrlEncoded
    @POST("placeDetail-addReview-script.php")
    Call<Review> addReview(@Field("place_id") String place_id,
                           @Field("username") String username,
                           @Field("review") String review,
                           @Field("rating") String rating);

    @FormUrlEncoded
    @POST("addPlace-script.php")
    Call<City> addPlace(@Field("name") String name,
                           @Field("address") String address,
                           @Field("lat") String lat,
                           @Field("lng") String lng,
                          @Field("city") String city,
                          @Field("image") String image);

    @FormUrlEncoded
    @POST("profile-user-script.php")
    Call<User> GetProfile(@Field("id") String id);

    @FormUrlEncoded
    @POST("follow-script.php")
    Call<User> ChangeFollowStatus(@Field("user1") String user1,@Field("user2") String user2,@Field("status") String status);

    @FormUrlEncoded
    @POST("getFollowStatus-script.php")
    Call<User> GetFollowStatus(@Field("user1") String user1,@Field("user2") String user2);

    @FormUrlEncoded
    @POST("profile-update-script.php")
    Call<User> updateProfile(@Field("id") String id, @Field("name") String name, @Field("upload") String upload);

}
