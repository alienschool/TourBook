package com.example.news.tourbook;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Session Manager Class
    SessionManager session;
    String SessionId,SessionEmail,SessionPassword;

    Intent getInten;
    TextView mUsername,mEmail,mName,mFollowers,mFollowing;
    ImageView mProfileImage;
    Button mEditButton,mFollowButton,mUnfollowButton;
    Boolean allowEdit;
    Context mContext;
    String profileId;
    private static final int IMG_REQUEST=111;
    private Bitmap bitmap;
    private int DeviceTotalWidth;
    private int DeviceTotalHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        DeviceTotalWidth = metrics.widthPixels;
        DeviceTotalHeight = metrics.heightPixels;
        mContext=ProfileActivity.this;
        session = new SessionManager(getApplicationContext());
        /**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in
         * */
        session.checkLogin();
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        // name
        SessionId = user.get(SessionManager.KEY_NAME);
        // email
        SessionEmail = user.get(SessionManager.KEY_EMAIL);
        // password
        SessionPassword = user.get(SessionManager.KEY_PASSWORD);
        //get intent values from previous activity
        getInten = getIntent();
        allowEdit= (Boolean) getInten.getExtras().get("allowEdit");

        setNavigationViewListener();

        mName=(TextView) findViewById(R.id.profile_name_textView);
        mUsername=(TextView)findViewById(R.id.profile_username_textview);
        mEmail=(TextView)findViewById(R.id.profile_email_textview);
        mFollowers=(TextView)findViewById(R.id.profile_followers_textView);
        mFollowing=(TextView)findViewById(R.id.profile_following_textView);
        mProfileImage=(ImageView)findViewById(R.id.profile_image_imageView);
        mEditButton=(Button)findViewById(R.id.profile_edit_button);
        mFollowButton=(Button)findViewById(R.id.profile_follow_button);
        mUnfollowButton=(Button)findViewById(R.id.profile_unfollow_button);
        if(allowEdit){
            mFollowButton.setVisibility(View.INVISIBLE);
            mUnfollowButton.setVisibility(View.INVISIBLE);
            profileId=SessionId;
            GetProfile();
            GetCheckins(SessionEmail);
        }else{
            profileId=getInten.getExtras().get("id").toString();
            GetProfile();
        }
        mFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                follow();
            }

        });
        mUnfollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unfollow();
            }

        });
        /*mTravelCircle=(Button)findViewById(R.id.profile_travelCircle_button);
        mTravelCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,TravelCircleActivity.class);
                mContext.startActivity(intent);
            }

        });*/

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,EditProfileActivity.class);
                mContext.startActivity(intent);
            }

        });
    }

    private void setFollowButtons(){
        APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
        Call<User> call=apiInterface.GetFollowStatus(SessionEmail,mUsername.getText().toString());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User c=response.body();
                //Toast.makeText(mContext, "intent-Extra: "+ getInten.getExtras().get("id") + " sessionId: "+SessionId, Toast.LENGTH_LONG).show();
                if(c.response.equalsIgnoreCase("true")) {
                    mFollowButton.setVisibility(View.INVISIBLE);
                    mUnfollowButton.setVisibility(View.VISIBLE);
                }else if(c.response.equalsIgnoreCase("false")){
                    mFollowButton.setVisibility(View.VISIBLE);
                    mUnfollowButton.setVisibility(View.INVISIBLE);
                }else{
                    Toast.makeText(mContext, "Server response: "+c.response, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(mContext, "Fail "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void follow(){
        mFollowButton.setVisibility(View.INVISIBLE);
        APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
        Call<User> call=apiInterface.ChangeFollowStatus(SessionEmail,mUsername.getText().toString(),"follow");
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User c=response.body();
                //Toast.makeText(mContext, "intent-Extra: "+ getInten.getExtras().get("id") + " sessionId: "+SessionId, Toast.LENGTH_LONG).show();
                if(c.response.equalsIgnoreCase("success")) {
                    setFollowButtons();
                    Toast.makeText(mContext, "Server response: "+c.response, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(mContext, "Server response: "+c.response, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(mContext, "Fail "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void unfollow(){
        mUnfollowButton.setVisibility(View.INVISIBLE);
        APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
        Call<User> call=apiInterface.ChangeFollowStatus(SessionEmail,mUsername.getText().toString(),"unfollow");
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User c=response.body();
                //Toast.makeText(mContext, "intent-Extra: "+ getInten.getExtras().get("id") + " sessionId: "+SessionId, Toast.LENGTH_LONG).show();
                if(c.response.equalsIgnoreCase("success")) {
                    setFollowButtons();
                    Toast.makeText(mContext, "Server response: "+c.response, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(mContext, "Server response: "+c.response, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(mContext, "Fail "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void GetProfile(){
        APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
        Call<User> call=apiInterface.GetProfile(profileId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User c=response.body();
                //Toast.makeText(mContext, "intent-Extra: "+ getInten.getExtras().get("id") + " sessionId: "+SessionId, Toast.LENGTH_LONG).show();
                if(c.response.equalsIgnoreCase("success")) {
                    //image
                    //Toast.makeText(mContext, "Profile pic: "+c.profile_picture, Toast.LENGTH_LONG).show();
                    if(c.profile_picture!=null && !c.profile_picture.equalsIgnoreCase("null") && c.profile_picture!="") {
                        byte[] decodedString = Base64.decode(c.profile_picture, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        mProfileImage.setImageBitmap(decodedByte);
                    }
                    mName.setText(c.name);
                    mUsername.setText(c.username);
                    mEmail.setText(c.email);
                    if(c.followers==null || c.followers.equalsIgnoreCase("")){
                        c.followers="0";
                    }
                    mFollowers.setText(c.followers);
                    mFollowers.setTag(c.username);
                    mFollowers.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext,UsersActivity.class);
                            intent.putExtra("allowEdit",allowEdit);
                            intent.putExtra("followType","followers");
                            intent.putExtra("username", view.getTag().toString());
                            mContext.startActivity(intent);
                        }

                    });
                    if(c.following==null || c.following.equalsIgnoreCase("")){
                        c.following="0";
                    }
                    mFollowing.setText(c.following);
                    mFollowing.setTag(c.username);
                    mFollowing.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext,UsersActivity.class);
                            intent.putExtra("allowEdit",allowEdit);
                            intent.putExtra("followType","following");
                            intent.putExtra("username", view.getTag().toString());
                            mContext.startActivity(intent);
                        }

                    });
                    if(allowEdit){
                        mEditButton.setVisibility(View.VISIBLE);
                        //mTravelCircle.setVisibility(View.VISIBLE);
                    }else{
                        setFollowButtons();
                        GetCheckins(c.username.toString());
                    }
                }else{
                    Toast.makeText(mContext, "Server response: "+c.response, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(mContext, "Fail "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void GetCheckins(String username){
        final LinearLayout layout=(LinearLayout) findViewById(R.id.profile_checkins_linearlayout);
        APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
        Call<List<City>> call=apiInterface.GetCheckins(username);
        call.enqueue(new Callback<List<City>>() {

            @Override
            public void onResponse(Call<List<City>> call, Response<List<City>> response) {
                List<City> c=response.body();
                //Toast.makeText(mContext, "Server response: "+ c.get(0).response, Toast.LENGTH_LONG).show();
                if(c.get(0).response.equalsIgnoreCase("success")) {
                    //LinearLayout layout=(LinearLayout) findViewById(R.id.main_places_linearlayout);
                    ImageView imageView;
                    TextView textView;
                    for (final City item:c) {
                        //name
                        textView = new TextView(mContext);
                        textView.setText("Checkedin at "+item.place_name);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
                        layout.addView(textView);

                        textView = new TextView(mContext);
                        textView.setText(item.address);
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
                Intent intent = new Intent(mContext,TravelCircleActivity.class);
                mContext.startActivity(intent);
                finish();
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
