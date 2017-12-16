package com.example.news.tourbook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Session Manager Class
    SessionManager session;
    String SessionId,SessionEmail,SessionPassword;

    Context mContext;
    Intent getInten;
    String followType,username;
    Boolean allowEdit;
    private int DeviceTotalWidth,DeviceTotalHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        DeviceTotalWidth = metrics.widthPixels;
        DeviceTotalHeight = metrics.heightPixels;

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

        mContext=UsersActivity.this;

        //get intent values from previous activity
        getInten = getIntent();
        followType= getInten.getExtras().get("followType").toString();
        allowEdit= (Boolean) getInten.getExtras().get("allowEdit");
        username=getInten.getExtras().get("username").toString();

        setNavigationViewListener();

        TextView heading=(TextView)findViewById(R.id.users_heading_textView);
        heading.setText(followType.toUpperCase());
        AllFollow();
    }

    private void AllFollow(){
        APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
        Call<List<User>> call;
        if(followType.equalsIgnoreCase("following")){
            call=apiInterface.GetFollowing(username);

        }else{
            call=apiInterface.GetFollowers(username);
        }
        Toast.makeText(mContext, "username "+ username, Toast.LENGTH_LONG).show();
        call.enqueue(new Callback<List<User>>() {

            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                List<User> c=response.body();
                //Toast.makeText(mContext, "Server response: "+ c.get(0).response, Toast.LENGTH_LONG).show();
                if(c.get(0).response.equalsIgnoreCase("success")) {
                    //Toast.makeText(mContext, "Server response success: "+ c.get(0).response, Toast.LENGTH_LONG).show();
                    LinearLayout layout=(LinearLayout) findViewById(R.id.users_follow_linearlayout);
                    ImageView imageView;
                    TextView textView;
                    for (User item:c) {
                        //image
                        imageView=new ImageView(mContext);
                        byte[] decodedString = Base64.decode(item.profile_picture, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageView.setImageBitmap(decodedByte);
                        imageView.setTag(item.id);
                        Toast.makeText(mContext, "tag: "+ item.id, Toast.LENGTH_LONG).show();
                        layout.addView(imageView);
                        imageView.getLayoutParams().height=(int)DeviceTotalHeight/3;
                        imageView.getLayoutParams().width=DeviceTotalWidth;
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View v){
                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                intent.putExtra("id",v.getTag().toString());
                                intent.putExtra("allowEdit",false);
                                startActivity(intent);
                            }
                        });
                        //name
                        textView = new TextView(mContext);
                        textView.setText(item.name + " \nRank:" +item.rank + " \nEmail:" +item.email + " \nFollowing:"+item.following+" \nFollowers:"+item.followers+" \nCheckin:"+item.checkin);
                        textView.setTag(item.id);
                        layout.addView(textView);
                        textView.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View v){
                                //Toast.makeText(mContext, "tag: "+ v.getTag(), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                intent.putExtra("id",v.getTag().toString());
                                intent.putExtra("allowEdit",false);
                                startActivity(intent);
                            }
                        });
                    }

                    //  Toast.makeText(mContext, "Welcome "+c.get(0).name, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(mContext, "Server response: "+c.get(0).response, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
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
