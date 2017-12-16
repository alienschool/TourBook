package com.example.news.tourbook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TravelCircleActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Context mContext;
    // Session Manager Class
    SessionManager session;
    String SessionId,SessionEmail,SessionPassword;
    int DeviceTotalHeight,DeviceTotalWidth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_circle);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        DeviceTotalWidth = metrics.widthPixels;
        DeviceTotalHeight = metrics.heightPixels;
        mContext= TravelCircleActivity.this;
        session = new SessionManager(getApplicationContext());
        /**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in
         **/
        session.checkLogin();
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        // name
        SessionId = user.get(SessionManager.KEY_NAME);
        // email
        SessionEmail = user.get(SessionManager.KEY_EMAIL);
        // password
        SessionPassword = user.get(SessionManager.KEY_PASSWORD);

        setNavigationViewListener();
        GetFollowingCheckins();
    }

    private void GetFollowingCheckins(){
        final LinearLayout layout=(LinearLayout) findViewById(R.id.travel_linearlayout);
        APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
        Call<List<City>> call=apiInterface.GetFollowingCheckins(SessionEmail);
        call.enqueue(new Callback<List<City>>() {

            @Override
            public void onResponse(Call<List<City>> call, Response<List<City>> response) {
                List<City> c=response.body();
                //Toast.makeText(mContext, "Server response: "+ c.get(0).response, Toast.LENGTH_LONG).show();
                if(c.get(0).response.equalsIgnoreCase("success")) {
                    //LinearLayout layout=(LinearLayout) findViewById(R.id.main_places_linearlayout);
                    ImageView imageView;
                    TextView textView,likes;
                    Button likeButton;

                    for (final City item:c) {

                        //name
                        textView = new TextView(mContext);
                        textView.setText("\n\n"+item.username);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
                        layout.addView(textView);

                        //person image
                        imageView=new ImageView(mContext);
                        byte[] decodedString = Base64.decode(item.profile_picture, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageView.setImageBitmap(decodedByte);
                        imageView.setTag(item.userid);
                        layout.addView(imageView);
                        imageView.getLayoutParams().height=200;
                        imageView.getLayoutParams().width=200;
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View v){
                                //Toast.makeText(mContext, "tag: "+ v.getTag(), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                intent.putExtra("id",v.getTag().toString());
                                intent.putExtra("allowEdit",false);
                                startActivity(intent);

                            }
                        });


                        textView = new TextView(mContext);
                        textView.setText("recently checkedin");
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
                        layout.addView(textView);

                        textView = new TextView(mContext);
                        textView.setText(item.place_name);
                        textView.setTypeface(null, Typeface.BOLD);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                        textView.setTag(item.place_Id);
                        layout.addView(textView);

                        textView = new TextView(mContext);
                        textView.setText(item.address);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        textView.setTag(item.place_Id);
                        layout.addView(textView);

                        textView.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v){
                                //Toast.makeText(mContext, "tag: "+ v.getTag(), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(mContext, PlaceDetailActivity.class);
                                intent.putExtra("id",v.getTag().toString());
                                startActivity(intent);
                            }
                        });

                        //total likes
                        likes = new TextView(mContext);
                        likes.setText(item.likes + " Likes");
                        layout.addView(likes);

                        //like button
                        likeButton = new Button(mContext);
                        likeButton.setText("Like");
                        likeButton.setTag(item.travelCircleId);
                        layout.addView(likeButton);
                        likeButton.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v){
                                likePost(v.getTag().toString());
                            }
                        });

                        //image
                        imageView=new ImageView(mContext);
                        imageView.setTag(item.place_Id);
                        layout.addView(imageView);
                        imageView.getLayoutParams().height=(int)DeviceTotalHeight/3;
                        imageView.getLayoutParams().width=DeviceTotalWidth;
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        Picasso.with(mContext).load("http://dibukhanmathematician.com/tourbook/uploads/"+ item.place_image).into(imageView);
                    }
                }else{
                    Toast.makeText(mContext, "Server response: "+c.get(0).response, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<List<City>> call, Throwable t) {
                Toast.makeText(mContext, "Fail "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void likePost(String travelCircleId){
        APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
        Call<City> call=apiInterface.LikeTravelCircle(travelCircleId,SessionId);
        call.enqueue(new Callback<City>() {

            @Override
            public void onResponse(Call<City> call, Response<City> response) {
                City c=response.body();
                //Toast.makeText(mContext, "Server response: "+ c.response, Toast.LENGTH_LONG).show();
                if(c.response.equalsIgnoreCase("success")) {
                    //LinearLayout layout=(LinearLayout) findViewById(R.id.main_places_linearlayout);
                    //Toast.makeText(mContext, "success ", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(mContext,TravelCircleActivity.class);
                    mContext.startActivity(intent);
                    finish();

                }else{
                    Toast.makeText(mContext, "Server response: "+c.response, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<City> call, Throwable t) {
                Toast.makeText(mContext, "Fail "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_home: {
                Intent intent = new Intent(mContext,MainActivity.class);
                mContext.startActivity(intent);
                finish();
                break;
            }
            case R.id.nav_addPlace: {
                Intent intent = new Intent(mContext,AddPlaceActivity.class);
                mContext.startActivity(intent);
                finish();
                break;
            }
            case R.id.nav_profile: {
                Intent intent = new Intent(mContext,ProfileActivity.class);
                intent.putExtra("allowEdit",true);
                mContext.startActivity(intent);
                finish();
                break;
            }
            case R.id.nav_travelCircle: {
                break;
            }
            case R.id.nav_logout: {
                session.logoutUser();
                break;
            }
        }
        //close navigation drawer
        //mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
