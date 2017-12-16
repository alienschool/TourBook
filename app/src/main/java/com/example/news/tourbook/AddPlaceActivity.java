package com.example.news.tourbook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPlaceActivity extends AppCompatActivity implements OnMapReadyCallback,NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    PlaceAutocompleteFragment placeAutoComplete;
    Context mContext;
    private Bitmap bitmap;
    EditText mName,mAddress,mCity,mLat,mLng;
    // Session Manager Class
    SessionManager session;
    String SessionId,SessionEmail,SessionPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        mContext=AddPlaceActivity.this;
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

        setNavigationViewListener();

        mName=(EditText)findViewById(R.id.addPlace_name_editText);
        mAddress=(EditText)findViewById(R.id.addPlace_address_editText);
        mCity=(EditText)findViewById(R.id.addPlace_city_editText);
        mLat=(EditText)findViewById(R.id.addPlace_lat_editText);
        mLng=(EditText)findViewById(R.id.addPlace_lng_editText);

        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("PAK")
                .build();

        placeAutoComplete.setFilter(typeFilter);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                String cityName="";
                try {
                    cityName=getCityNameByCoordinates(place.getLatLng().latitude,place.getLatLng().longitude);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Log.d("Maps", "Place selected: " + place.getName());
                mName.setText(place.getName());
                mAddress.setText(place.getAddress());
                mLat.setText(String.valueOf(place.getLatLng().latitude));
                mLng.setText(String.valueOf(place.getLatLng().longitude));
                if(!cityName.equalsIgnoreCase("") || !cityName.equals(null)){
                    mCity.setText(cityName);
                }
                Toast.makeText(mContext, "Name: "+cityName+"\naddress"+place.getAddress(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {
                //Log.d("Maps", "An error occurred: " + status);
                Toast.makeText(mContext, "error"+status, Toast.LENGTH_SHORT).show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.addPlace_map);
        mapFragment.getMapAsync(this);

        Button uploadImage,save;

        //selected image button click
        uploadImage=(Button)findViewById(R.id.addPlace_image_button);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryImageintent=new Intent();
                galleryImageintent.setType("image/jpg");
                galleryImageintent.setAction(Intent.ACTION_PICK);
                startActivityForResult(galleryImageintent,123 );
            }

        });

        //upload and save button click
        uploadImage=(Button)findViewById(R.id.addPlace_save_button);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadImage();
            }

        });
    }

    private Geocoder mGeocoder = new Geocoder(AddPlaceActivity.this, Locale.getDefault());

// ...

    private String getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return null;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    //upload image
    private void UploadImage()
    {
        String Image= imagetoString();

        APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);

        //calling upload function in APIMyInterface and sending parameters
        //Toast.makeText(EditNewsActivity.this, "report ID "+ reportId, Toast.LENGTH_SHORT).show();
        Call<City> call=apiInterface.addPlace(mName.getText().toString(),mAddress.getText().toString(),
                mLat.getText().toString(),mLng.getText().toString(),mCity.getText().toString(),Image);
        call.enqueue(new Callback<City>() {

            //when response is received from server
            @Override
            public void onResponse(Call<City> call, Response<City> response) {
                City c=response.body();
                if(response.body() != null){
                    Toast.makeText(mContext, "Server response: "+c.response, Toast.LENGTH_SHORT).show();
                    if(c.response.equalsIgnoreCase("success")){
                        //go to main activity
                        Intent intent = new Intent(mContext,MainActivity.class);
                        mContext.startActivity(intent);
                        AddPlaceActivity.this.finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<City> call, Throwable t) {
                Toast.makeText(mContext, "nhe chla"+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    File myFile;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK && data != null) {
            // Get the image from data
            Uri path = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //convert image into string
    private String imagetoString()
    {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,20,byteArrayOutputStream);
        byte[] imgbyt=byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgbyt, Base64.DEFAULT);

    }
    public String getRealPathFromURI1(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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
