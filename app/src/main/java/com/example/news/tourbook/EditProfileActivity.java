package com.example.news.tourbook;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Session Manager Class
    SessionManager session;
    String SessionId,SessionEmail,SessionPassword;

    TextView mUsername,mEmail;
    EditText mName;
    Button mUpload,mUpdate;
    ImageView mProfileImage;
    Context mContext;
    private static final int IMG_REQUEST=111;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        mContext=EditProfileActivity.this;
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
        //Toast.makeText(mContext, "Session: "+ SessionId+SessionEmail, Toast.LENGTH_LONG).show();
        mName=(EditText) findViewById(R.id.editProfile_name_editText);
        mUsername=(TextView)findViewById(R.id.editProfile_username_textview);
        mEmail=(TextView)findViewById(R.id.editProfile_email_textview);
        mProfileImage=(ImageView)findViewById(R.id.editProfile_image_imageView);
        mUpload=(Button)findViewById(R.id.editProfile_upload_button);
        mUpdate=(Button)findViewById(R.id.editProfile_update_button);

        //on upload button click
        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryImageintent=new Intent();
                galleryImageintent.setType("image/jpg");
                galleryImageintent.setAction(Intent.ACTION_PICK);
                startActivityForResult(galleryImageintent,IMG_REQUEST );
            }

        });
        //on update button click
        mUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateProfile();
            }

        });
        GetProfile();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMG_REQUEST&&resultCode ==RESULT_OK&&data!=null)
        {
            // Get the image from data
            Uri path=data.getData();

            try {
                bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),path);

                //preview selected image
                mProfileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //upload image
    private void UpdateProfile()
    {
        String Image= imagetoString();
        String name= mName.getText().toString();
        if(!isNameValid(name)){
            mName.setError("Enter a valid name");
        }else if(mProfileImage.getDrawable()==null){
            mUpload.setError("Must have a profile image");
        }else{
            //calling upload function in APIMyInterface and sending parameters
            APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
            Call<User> call=apiInterface.updateProfile(SessionId,name,Image);
            call.enqueue(new Callback<User>() {
                //when response is received from server
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    User imageClass=response.body();
                    if(imageClass.response.equalsIgnoreCase("success")){
                        Toast.makeText(mContext, "image uploaded", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(mContext, "Server response: "+imageClass.response, Toast.LENGTH_SHORT).show();
                    }

                }
                @Override
                public void onFailure(Call<User> call, Throwable t) {
                      Toast.makeText(mContext, "nhe chla", Toast.LENGTH_SHORT).show();
                }
            });
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

    private void GetProfile(){
        APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
        Call<User> call=apiInterface.GetProfile(SessionId);
        call.enqueue(new Callback<User>() {

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User c=response.body();
                //Toast.makeText(mContext, "Server response: "+ c.get(0).response, Toast.LENGTH_LONG).show();
                if(c.response.equalsIgnoreCase("success")) {
                    Toast.makeText(mContext, "Server response success: "+ c.response, Toast.LENGTH_LONG).show();
                    //image
                    if(c.profile_picture!=null && !c.profile_picture.equalsIgnoreCase("null") && c.profile_picture!="") {
                        byte[] decodedString = Base64.decode(c.profile_picture, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        mProfileImage.setImageBitmap(decodedByte);
                        bitmap=decodedByte;
                    }
                    mName.setText(c.name);
                    mUsername.setText(c.username);
                    mEmail.setText(c.email);
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
    private boolean isNameValid(String name) {
        //TODO: Replace this with your own logic
        return name.length() > 2;
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
