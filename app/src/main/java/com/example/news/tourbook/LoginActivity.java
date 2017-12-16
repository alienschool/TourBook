package com.example.news.tourbook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    // Session Manager Class
    SessionManager session;
    String SessionId,SessionEmail,SessionPassword;

    EditText mUsernameInput,mPasswordInput;
    Button mSignInButton,testing;
    Context mContext;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = LoginActivity.this;
        session = new SessionManager(getApplicationContext());
        if(session.isLoggedIn()){
            Intent intent = new Intent(mContext,MainActivity.class);
            mContext.startActivity(intent);
            finish();
        }

        mUsernameInput = (EditText) findViewById(R.id.login_username_editText);
        mPasswordInput = (EditText) findViewById(R.id.login_password_editText);

        TextView mRegisterLink = (TextView) findViewById(R.id.login_register_textView);
        mRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,RegisterActivity.class);
                mContext.startActivity(intent);
            }
        });
        mSignInButton = (Button) findViewById(R.id.login_login_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();

            }
        });


    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        String email,password;
        // Reset errors.
        mUsernameInput.setError(null);
        mPasswordInput.setError(null);
        user=new User();
        user.username = mUsernameInput.getText().toString();
        user.password = mPasswordInput.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(user.password) && !isPasswordValid(user.password)) {
            mPasswordInput.setError("error_invalid_password");
            focusView = mPasswordInput;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(user.username)) {
            mUsernameInput.setError("error_field_required");
            focusView = mUsernameInput;
            cancel = true;
        } else if (!isUsernameValid(user.username)) {
            mUsernameInput.setError("error_invalid_email");
            focusView = mUsernameInput;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //ProgressBar progressBar=(ProgressBar)findViewById(R.id.login_progressBar);

            // perform the user login attempt.
            APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
            Call<User> call=apiInterface.Login(user.username,user.password);
            call.enqueue(new Callback<User>() {

                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    User u=response.body();
                    if(u.response.equalsIgnoreCase("success")) {
                        Toast.makeText(mContext,"Welcome " +u.name, Toast.LENGTH_SHORT).show();

                       // Session Manager
                            session = new SessionManager(getApplicationContext());
                            session.createLoginSession(u.id, u.username, user.password);
                        NextActivity();
                    }else if(u.response.equalsIgnoreCase("key required")){
                        registrationKeyDialog(u.id);
                    }else if(u.response.equalsIgnoreCase("unregistered")){
                        mUsernameInput.setError("Not registered");
                    }else if(u.response.equalsIgnoreCase("wrong password")){
                        mPasswordInput.setError("Wrong password");
                    }else{
                        Toast.makeText(mContext, "Server response: "+u.response, Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(mContext, "Fail"+t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void registrationKeyDialog(final String uId){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Registration Key");
        builder.setMessage("Key has been sent to you via email on signup");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_NORMAL);
        int maxLength=6;
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String uKey= input.getText().toString();
                confirmRegistrationKey(uId,uKey);
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

    private void confirmRegistrationKey(final String uId, final String regKey){
        // perform the user login attempt.
        APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
        Call<User> call=apiInterface.ValidateKey(uId,regKey);
        call.enqueue(new Callback<User>() {

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User u = response.body();
                if (u.response.equalsIgnoreCase("success")) {
                    Toast.makeText(mContext, "Welcome " + u.name, Toast.LENGTH_SHORT).show();
                    // Session Manager
                    session = new SessionManager(getApplicationContext());
                    session.createLoginSession(u.id, u.username, user.password);
                    NextActivity();
                }else{
                    Toast.makeText(mContext, "Server response: "+u.response, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(mContext, "Fail"+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void NextActivity(){
        Intent intent = new Intent(mContext,MainActivity.class);
        mContext.startActivity(intent);
        LoginActivity.this.finish();
    }
    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() > 2;
    }
    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 2;
    }
}
