package com.example.news.tourbook;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.InputFilter;
import android.text.InputType;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener,NavigationView.OnNavigationItemSelectedListener {

    GoogleMap mGoogleMap;
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Context mContext;
    ArrayList<LatLng> latlngs;
    LatLng latLng;
    Button navProfile,navAddPlace;
    TextView mLocation;
    double mlatitude,mlongitude;
    String mylocation="",myNewLocation="";
    Boolean manualCheck;
    int DeviceTotalHeight,DeviceTotalWidth;

    // Session Manager Class
    SessionManager session;
    String SessionId,SessionEmail,SessionPassword;
    private DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        DeviceTotalWidth = metrics.widthPixels;
        DeviceTotalHeight = metrics.heightPixels;

        manualCheck=false;

        mLocation=(TextView)findViewById(R.id.main_location_textView);

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

        mContext=MainActivity.this;
        setNavigationViewListener();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*navAddPlace = (Button) findViewById(R.id.main_addPlace_button);
        navAddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,AddPlaceActivity.class);
                mContext.startActivity(intent);
                MainActivity.this.finish();
            }
        });
        navProfile = (Button) findViewById(R.id.main_navProfile_Button);
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,ProfileActivity.class);
                intent.putExtra("allowEdit",true);
                mContext.startActivity(intent);
            }
        });*/
        //AllPlaces();
        AllGuides();
    }
    private void AllGuides(){
        APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
        Call<List<User>> call=apiInterface.GetMainGuides("temp");
        call.enqueue(new Callback<List<User>>() {

            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                List<User> c=response.body();
                //Toast.makeText(mContext, "Server response: "+ c.get(0).response, Toast.LENGTH_LONG).show();
                if(c.get(0).response.equalsIgnoreCase("success")) {
                    //Toast.makeText(mContext, "Server response success: "+ c.get(0).response, Toast.LENGTH_LONG).show();
                    LinearLayout layout=(LinearLayout) findViewById(R.id.main_guides_linearlayout);
                    ImageView imageView;
                    TextView textView;
                    for (User item:c) {
                        //name
                        textView = new TextView(mContext);
                        textView.setText(item.name);
                        textView.setTag(item.id);
                        layout.addView(textView);
                        textView.setTypeface(null, Typeface.BOLD);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                        textView.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));


                        textView = new TextView(mContext);
                        textView.setText("Rank:" +item.rank + "\nFollowing:"+item.following+"\nFollowers:"+item.followers+"\nCheckin:"+item.checkin);
                        textView.setTag(item.id);
                        layout.addView(textView);
                        textView.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View v){
                                //Toast.makeText(mContext, "tag: "+ v.getTag(), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                intent.putExtra("id",v.getTag().toString());
                                intent.putExtra("allowEdit",false);
                                startActivity(intent);

                            }
                        });
                        //image
                        imageView=new ImageView(mContext);
                        byte[] decodedString = Base64.decode(item.profile_picture, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageView.setImageBitmap(decodedByte);
                        imageView.setTag(item.id);
                        layout.addView(imageView);
                        imageView.getLayoutParams().height=(int)DeviceTotalHeight/3;
                        imageView.getLayoutParams().width=DeviceTotalWidth;
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View v){
                                //Toast.makeText(mContext, "tag: "+ v.getTag(), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
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
    private void AllPlaces(){
        if( mylocation== null){
            if(!manualCheck){
                manualCheck=true;
                getLocationManually();
            }

    }else{
            final LinearLayout layout=(LinearLayout) findViewById(R.id.main_places_linearlayout);
            layout.removeAllViews();
            APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
            Call<List<City>> call=apiInterface.GetMainCities(mylocation);
            call.enqueue(new Callback<List<City>>() {

                @Override
                public void onResponse(Call<List<City>> call, Response<List<City>> response) {
                    List<City> c=response.body();
                    //Toast.makeText(mContext, "Server response: "+ c.get(0).response, Toast.LENGTH_LONG).show();
                    if(c.get(0).response.equalsIgnoreCase("success")) {
                        //LinearLayout layout=(LinearLayout) findViewById(R.id.main_places_linearlayout);
                        ImageView imageView;
                        TextView textView;
                        latlngs = new ArrayList<>();
                        for (final City item:c) {
                            //name
                            textView = new TextView(mContext);
                            textView.setText(item.place_name);
                            textView.setTag(item.place_Id);
                            layout.addView(textView);
                            textView.setTypeface(null, Typeface.BOLD);
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                            textView.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                            textView.setOnClickListener(new View.OnClickListener(){

                                @Override
                                public void onClick(View v){
                                    //Toast.makeText(mContext, "tag: "+ v.getTag(), Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(MainActivity.this, PlaceDetailActivity.class);
                                    intent.putExtra("id",v.getTag().toString());
                                    startActivity(intent);
                                    //MainActivity.this.finish();

                                }
                            });
                            //image
                            imageView=new ImageView(mContext);
                            imageView.setTag(item.place_Id);
                            layout.addView(imageView);
                            imageView.getLayoutParams().height=(int)DeviceTotalHeight/3;
                            imageView.getLayoutParams().width=DeviceTotalWidth;
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imageView.setOnClickListener(new View.OnClickListener(){

                                @Override
                                public void onClick(View v){
                                    //Toast.makeText(mContext, "tag: "+ v.getTag(), Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(MainActivity.this, PlaceDetailActivity.class);
                                    intent.putExtra("id",v.getTag().toString());
                                    startActivity(intent);
                                    //MainActivity.this.finish();

                                }
                            });
                            Picasso.with(mContext).load("http://dibukhanmathematician.com/tourbook/uploads/"+ item.place_image).into(imageView);
                            //latitude longitude

                            //latlngs.add(new LatLng(Double.parseDouble(item.lng),Double.parseDouble(item.lat)));
                            latLng = new LatLng(Double.parseDouble(item.lng),Double.parseDouble(item.lat));
                            //google map markers
                            MarkerOptions options = new MarkerOptions();
                            //get all markers and locations
                            builder.include(latLng);
                            options.position(latLng);
                            options.title(item.place_name);
                            options.snippet(item.address);
                            mGoogleMap.addMarker(options);
                            //AllPlaces();
                        /*for (LatLng point : latlngs) {
                            builder.include(point);
                            options.position(point);
                            options.title("someTitle");
                            options.snippet("someDesc");
                            mGoogleMap.addMarker(options);
                        }*/


                        }
                        // Set the camera to the greatest possible zoom level that includes the bounds
                        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                //googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
                                LatLngBounds bounds = builder.build();
                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                            }
                        });

                        //  Toast.makeText(mContext, "Welcome "+c.get(0).name, Toast.LENGTH_LONG).show();
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

    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mGoogleMap = googleMap;

        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

        /*MarkerOptions options = new MarkerOptions();
        //get all markers and locations
        //AllPlaces();
        latlngs = new ArrayList<>();
        latlngs.add(new LatLng( 33.7078948,73.04996119999998));
        latlngs.add(new LatLng( 33.72965380000001,73.0373085));
        latlngs.add(new LatLng(  33.76000430000001,73.06585599999994));
        latlngs.add(new LatLng( 33.7336617,73.05934359999992));
        latlngs.add(new LatLng(  33.7112415,73.13164129999996));
        for (LatLng point : latlngs) {
            builder.include(point);
            options.position(point);
            options.title("someTitle");
            options.snippet("someDesc");
            googleMap.addMarker(options);
        }

        // Set the camera to the greatest possible zoom level that includes the bounds
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
                LatLngBounds bounds = builder.build();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            }
        });*/

        //LatLng islamabad = new LatLng(  33.7112415, 73.13164129999996);//new LatLng(33.6688185,72.7047892);
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(islamabad));
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        //if (mGoogleApiClient != null) {
          //  LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        //}
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));

        mlatitude = (double) (location.getLatitude());
        mlongitude = (double) (location.getLongitude());
        //mlatitude=33.6518263;
        //mlongitude=73.15659329999994;
        Geocoder gcd = new Geocoder(mContext, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(mlatitude,mlongitude,1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            myNewLocation=addresses.get(0).getLocality();
            //mLocation.setText("Your current location is " + myNewLocation);

        }
        else {
            // do your stuff
            mLocation.setText("Your current location is not available");
        }
        if(mylocation!=null){
            if(!mylocation.trim().equalsIgnoreCase(myNewLocation)){
                //if(!manualCheck){
                    mylocation=myNewLocation;
                if(myNewLocation!=null){
                    mLocation.setText("Your current location is " + myNewLocation);
                }

                    AllPlaces();
                //}


            }
        }


    }

    private void getLocationManually(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your City Name");
        builder.setMessage("Couldnt find city name from current location");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        int maxLength=20;
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String uKey= input.getText().toString();
                mylocation = uKey;
                mLocation.setText("Your current location is " + uKey);
                AllPlaces();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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
                                ActivityCompat.requestPermissions(MainActivity.this,
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
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

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
                //do somthing
                //session.logoutUser();
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
        //mDrawerLayout = (DrawerLayout) findViewById(R.id.navigation);
        //mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    //@Override
    /*public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                // Permission was denied. Display an error message.
            }
        }
    }*/


}
