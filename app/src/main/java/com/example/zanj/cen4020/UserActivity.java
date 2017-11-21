package com.example.zanj.cen4020;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//if the user doesnt have an account, this screen will come up if they press register.
//no error checking is done besides the built in error checking from firebase, does not alert user
//of any errors

public class UserActivity extends AppCompatActivity {

    EditText emailET, passwordET, channelET;
    String username, password, channel, email;
    private static final String TAG = UserActivity.class.getName();
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    DatabaseReference ref;
    private String fbemail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_user);
        mAuth = FirebaseAuth.getInstance();
        emailET = findViewById(R.id.etEmail);
        passwordET = findViewById(R.id.etPassword);
        channelET = findViewById(R.id.etChannelname);
        ref = FirebaseDatabase.getInstance().getReference("emails");    //get an instance of the firebase database

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
    }

    public void loginClicked(View v) //no error checking is done yet, only what firebase already does
    {
        final String userType = getIntent().getStringExtra("type");
        if (emailET.isEnabled() && passwordET.isEnabled()) {
            email = emailET.getText().toString(); //get email
            password = passwordET.getText().toString(); //get password
            username = emailET.getText().toString().split("@")[0]; //get just the username
            channel = channelET.getText().toString(); //get channel name
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                final String tempUsername;
                                if (username.contains(".")) {
                                    tempUsername = username.replace(".", ",");
                                } else
                                    tempUsername = username;
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    Boolean found = false;

                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                                            String user = String.valueOf(data.getKey());
                                            if (user.equals(tempUsername)) {
                                                Toast.makeText(getApplicationContext(), "Username is already taken.", Toast.LENGTH_LONG).show();
                                                emailET.setText("");
                                                passwordET.setText("");
                                                channelET.setText("");
                                                found = true;
                                                FirebaseUser firebaseAccount = mAuth.getCurrentUser();
                                                firebaseAccount.delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "User account deleted.");
                                                                }
                                                            }
                                                        });
                                                break;
                                            }
                                        }

                                        if (found == false) {
                                            ref.child(tempUsername).setValue(userType);
                                            Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                            intent.putExtra("username", username); //passes username and channel to the MainActivity class
                                            intent.putExtra("channel", channel);
                                            intent.putExtra("type", userType);
                                            startActivity(intent);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError firebaseError) {

                                    }
                                });
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            }
                        }
                    });
        } else {
            final String tempUsername;
            if (fbemail.contains(".")) {
                tempUsername = fbemail.replace(".", ",");
            } else
                tempUsername = fbemail;

            ref.child(tempUsername).setValue(userType);
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra("username", fbemail); //passes username and channel to the MainActivity class
            intent.putExtra("channel", channelET.getText().toString());
            intent.putExtra("type", userType);
            startActivity(intent);
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            fbemail = mAuth.getCurrentUser().getEmail().split("@")[0];;

                            emailET.setEnabled(false);
                            passwordET.setEnabled(false);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(UserActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
