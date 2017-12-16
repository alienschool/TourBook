package com.example.news.tourbook;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PlaceDetailActivity extends AppCompatActivity implements
        ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener ,NavigationView.OnNavigationItemSelectedListener{

    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;

    // Session Manager Class
    SessionManager session;
    String SessionId,SessionEmail,SessionPassword;

    Intent getInten;
    String placeId;
    Context mContext;
    TextView mName, mAddress, mRatingTextView;
    Button mCheckin,mReviewButton;
    ImageView mPlaceImage;
    Boolean checkInClicked,checkedIn;
    EditText mComment;
    RatingBar mRatingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);
        mContext = PlaceDetailActivity.this;
        checkInClicked=false;
        checkedIn=false;
        mName = (TextView) findViewById(R.id.placeDetail_placeName_textView);
        mAddress = (TextView) findViewById(R.id.placeDetail_placeAddress_textView);
        mRatingTextView = (TextView) findViewById(R.id.placeDetail_rating_textView);
        mPlaceImage = (ImageView) findViewById(R.id.placeDetail_placeImage_imageView);
        mCheckin = (Button) findViewById(R.id.placeDetail_checkin_button);
        mReviewButton = (Button) findViewById(R.id.placeDetail_review_button);
        mComment = (EditText)findViewById(R.id.placeDetail_comment_editText);
        mRatingBar = (RatingBar)findViewById(R.id.ratingBar);

        mCheckin.setClickable(false);

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
        setNavigationViewListener();
        placeId = getInten.getExtras().get("id").toString();
        //Toast.makeText(mContext, "place no : "+ placeId, Toast.LENGTH_LONG).show();
        placeDetail();
        getPlaceReview();
        mReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mComment.getText().toString().isEmpty()){
                    mComment.setError("cannot be empty");
                }else if(Float.toString(mRatingBar.getRating()).equalsIgnoreCase("0.0")){
                    mReviewButton.setError("select rating");
                }
                else{
                    mReviewButton.setEnabled(false);
                    mReviewButton.setText("wait...");
                    addReview();
                }

            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        mCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckin.setClickable(false);
                mCheckin.setText("checking in...");
                //checkInClicked=true;
                checkIn();

            }

        });
    }

    private void checkIn() {
        //Toast.makeText(this, currentLatitude + " in checkin " + currentLongitude + "", Toast.LENGTH_SHORT).show();
        APIMyInterface apiInterface = APIClient.getApiClient().create(APIMyInterface.class);
        Call<City> call = apiInterface.Checkin(Double.toString(currentLatitude), Double.toString(currentLongitude), placeId, mName.getText().toString(), SessionEmail);
        //Toast.makeText(mContext, "Server response: "+ Double.toString(currentLatitude)+" and "+Double.toString(currentLongitude), Toast.LENGTH_LONG).show();
        call.enqueue(new Callback<City>() {

            @Override
            public void onResponse(Call<City> call, Response<City> response) {
                City c = response.body();
                //Toast.makeText(mContext, "Server response: "+ c.response, Toast.LENGTH_LONG).show();
                if (c.response.equalsIgnoreCase("success")) {
                    //checkedIn=true;

                    Toast.makeText(mContext, "Successfully checked in ", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, c.response, Toast.LENGTH_LONG).show();
                }
                mCheckin.setText("check in");
                mCheckin.setClickable(true);
            }

            @Override
            public void onFailure(Call<City> call, Throwable t) {
                mCheckin.setClickable(true);
                mCheckin.setText("check in");
                Toast.makeText(mContext, "Fail " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void placeDetail() {
        APIMyInterface apiInterface = APIClient.getApiClient().create(APIMyInterface.class);
        Call<City> call = apiInterface.GetPlaceDetail(placeId);
        call.enqueue(new Callback<City>() {

            @Override
            public void onResponse(Call<City> call, Response<City> response) {
                City c = response.body();
                //Toast.makeText(mContext, "Server response: "+ c.get(0).response, Toast.LENGTH_LONG).show();
                if (c.response.equalsIgnoreCase("success")) {

                    //image
                    Picasso.with(mContext).load("http://dibukhanmathematician.com/tourbook/uploads/" + c.place_image).into(mPlaceImage);

                    //name
                    mName.setText(c.place_name);

                    //address
                    mAddress.setText(c.address);

                    //rating
                    mCheckin.setClickable(true);
                } else {
                    Toast.makeText(mContext, "Server response: " + c.response, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<City> call, Throwable t) {
                Toast.makeText(mContext, "Fail " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getPlaceReview() {
        APIMyInterface apiInterface = APIClient.getApiClient().create(APIMyInterface.class);
        Call<List<Review>> call = apiInterface.GetPlaceReviews(placeId);
        call.enqueue(new Callback<List<Review>>() {

            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                List<Review> c = response.body();
                //Toast.makeText(mContext, "Server response: "+ c.get(0).response, Toast.LENGTH_LONG).show();
                TextView textView;
                LinearLayout layout=(LinearLayout) findViewById(R.id.placeDetail_review_linearlayout);
                layout.removeAllViews();
                if (c.get(0).response.equalsIgnoreCase("success")) {
                    mRatingTextView.setText(c.get(0).averageRating+" out of 5");
                    for (Review item:c) {
                        //name
                        textView = new TextView(mContext);
                        textView.setText(item.username);
                        layout.addView(textView);
                        textView.setTypeface(null, Typeface.BOLD);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        textView.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));

                        textView = new TextView(mContext);
                        textView.setText(item.time + "\n" + item.review +"\n\n");
                        layout.addView(textView);

                    }
                    mCheckin.setClickable(true);
                } else {
                    Toast.makeText(mContext, "Server response: " + c.get(0).response, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                Toast.makeText(mContext, "Fail " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addReview() {
        APIMyInterface apiInterface = APIClient.getApiClient().create(APIMyInterface.class);
        Call<Review> call = apiInterface.addReview(placeId,SessionEmail,mComment.getText().toString(),Float.toString(mRatingBar.getRating()));
        call.enqueue(new Callback<Review>() {

            @Override
            public void onResponse(Call<Review> call, Response<Review> response) {
                Review c = response.body();
                //Toast.makeText(mContext, "Server response: "+ c.get(0).response, Toast.LENGTH_LONG).show();
                TextView textView;
                LinearLayout layout=(LinearLayout) findViewById(R.id.placeDetail_review_linearlayout);
                if (c.response.equalsIgnoreCase("success")) {
                    mComment.setText("");
                    mRatingBar.setRating(0);
                    mReviewButton.setText("Review");
                    mReviewButton.setEnabled(true);
                    getPlaceReview();
                } else {
                    Toast.makeText(mContext, "Server response: " + c.response, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Review> call, Throwable t) {
                Toast.makeText(mContext, "Fail " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }

    /**
     * If connected get lat and long
     *
     */
    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            if(!checkedIn && checkInClicked){
                //checkInClicked=false;
                //checkIn();
            }
            Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
            /*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this,  "loc chang", Toast.LENGTH_SHORT).show();
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        if(!checkedIn && checkInClicked){
            //checkInClicked=false;
            //checkIn();
        }
        Toast.makeText(this, currentLatitude + currentLongitude + "loc chang", Toast.LENGTH_SHORT).show();

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(PlaceDetailActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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
