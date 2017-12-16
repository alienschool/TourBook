package com.example.news.tourbook;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    // Session Manager Class
    SessionManager session;

    EditText mNameInput,mUsernameInput,mEmailInput,mPasswordInput;
    private RadioGroup mRadioGroup;
    private RadioButton radioButton;
    Button mSignInButton,mRegisterButtom;
    Context mContext;
    User user=new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mContext = RegisterActivity.this;

        mNameInput = (EditText) findViewById(R.id.register_name_editText);
        mUsernameInput = (EditText) findViewById(R.id.register_username_editText);
        mEmailInput = (EditText) findViewById(R.id.register_email_editText);
        mPasswordInput = (EditText) findViewById(R.id.register_password_editText);
        mRadioGroup = (RadioGroup) findViewById(R.id.register_radio);
        user.gender = "male";

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {

                if (i == R.id.register_male_radioButton) {
                    radioButton = (RadioButton) findViewById(R.id.register_male_radioButton);
                } else if (i == R.id.register_female_radioButton) {
                    radioButton = (RadioButton) findViewById(R.id.register_female_radioButton);
                }
                user.gender=radioButton.getText().toString();
                Toast.makeText(mContext,radioButton.getText(), Toast.LENGTH_SHORT).show();
            }
        });
        mSignInButton = (Button) findViewById(R.id.register_login_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,LoginActivity.class);
                mContext.startActivity(intent);
                RegisterActivity.this.finish();
            }
        });
        mRegisterButtom = (Button) findViewById(R.id.register_register_button);
        mRegisterButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();

            }
        });
    }

    private void attemptRegister() {
        String name,username,email,password;
        // Reset errors.
        mNameInput.setError(null);
        mUsernameInput.setError(null);
        mEmailInput.setError(null);
        mPasswordInput.setError(null);

        user.name = mNameInput.getText().toString();
        user.username = mUsernameInput.getText().toString();
        user.email = mEmailInput.getText().toString();
        user.password = mPasswordInput.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(user.password) && !isPasswordValid(user.password)) {
            mPasswordInput.setError("error_invalid_password");
            focusView = mPasswordInput;
            cancel = true;
        }
        // Check for a valid name.
        if (TextUtils.isEmpty(user.name)) {
            mNameInput.setError("field required");
            focusView = mNameInput;
            cancel = true;
        } else if (!isUsernameValid(user.name)) {
            mNameInput.setError("invalid name");
            focusView = mNameInput;
            cancel = true;
        }
        // Check for a valid username.
        if (TextUtils.isEmpty(user.username)) {
            mUsernameInput.setError("field required");
            focusView = mUsernameInput;
            cancel = true;
        } else if (!isUsernameValid(user.username)) {
            mUsernameInput.setError("invalid username");
            focusView = mUsernameInput;
            cancel = true;
        }
        // Check for a valid email.
        if (TextUtils.isEmpty(user.email)) {
            mEmailInput.setError("field required");
            focusView = mEmailInput;
            cancel = true;
        } else if (!isEmailValid(user.email)) {
            mEmailInput.setError("invalid email");
            focusView = mEmailInput;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // perform the user login attempt.
            APIMyInterface apiInterface= APIClient.getApiClient().create(APIMyInterface.class);
            Call<User> call=apiInterface.Signup(user.name,user.username,user.email,user.password,user.gender);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    User u=response.body();
                    if(u.response.equalsIgnoreCase("success")) {
                        Toast.makeText(mContext, "Server response: "+u.response, Toast.LENGTH_SHORT).show();
                        mNameInput.setText("");
                        mUsernameInput.setText("");
                        mEmailInput.setText("");
                        mPasswordInput.setText("");
                        // Session Manager
                        //session = new SessionManager(getApplicationContext());
                        //session.createLoginSession(u.id, u.email, u.password);
                    }else if(u.response.equalsIgnoreCase("already registered")) {
                        mUsernameInput.setError("Username or email already registered");
                        mEmailInput.setError("Username or email already registered");
                    }
                    else{
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
    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() > 2;
    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }
    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 2;
    }
}
