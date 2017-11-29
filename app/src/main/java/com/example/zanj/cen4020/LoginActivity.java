package com.example.zanj.cen4020;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

import static android.Manifest.permission.READ_CONTACTS;
import static android.R.attr.phoneNumber;

//This is the first screen of the app, prompts user for email/password or asks to register for an account

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    DatabaseReference ref, ref2;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private FirebaseAuth mAuth;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView, mChannelName;
    private View mProgressView;
    private View mLoginFormView;
    private CallbackManager mCallbackManager;
    private String fbemail;
    String userType;
    String TAG = "Facebook:";
    String tempChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userType = getIntent().getStringExtra("type");
        mAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mChannelName = (EditText) findViewById(R.id.channelName);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        tempChannel = getIntent().getStringExtra("channelTemp");
        if(tempChannel != null)
        {
            mChannelName.setText(tempChannel);
        }
        else
            tempChannel = "none";

        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mEmailView.isEnabled() && !mPasswordView.isEnabled())
                    login(fbemail, null, mChannelName.getText().toString());
                else
                    login(mEmailView.getText().toString(), mPasswordView.getText().toString(), mChannelName.getText().toString());
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        ref = FirebaseDatabase.getInstance().getReference("emails");    //get an instance of the firebase database
        ref2 = FirebaseDatabase.getInstance().getReference("channels");
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

    private void login(String email, String password, final String channelName) {
        final String username = email.split("@")[0];

        if (password != null) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (!channelName.contains(".")) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("Auth", "signInWithEmail:success");
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
                                                String type = String.valueOf(data.getValue());
                                                if (user.equals(tempUsername)) {
                                                    if (!type.equals(userType)) {
                                                        Toast.makeText(getApplicationContext(), "User is not a " + userType + ". Changing to correct user type.",
                                                                Toast.LENGTH_LONG).show();
                                                        if (userType.equals("student"))
                                                            userType = "teacher";
                                                        else
                                                            userType = "student";

                                                    }
                                                    found = true;
                                                    break;
                                                }
                                            }

                                            if (found == false) {
                                                Toast.makeText(getApplicationContext(), "User does not exist", Toast.LENGTH_LONG).show();
                                            } else {
                                                if(userType.equals("student"))
                                                {
                                                    ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            boolean found = false;
                                                            for (DataSnapshot data2 : dataSnapshot.getChildren())
                                                            {
                                                                String tempChannel = String.valueOf(data2.getKey());
                                                                if(tempChannel.equals(channelName))
                                                                {
                                                                    found = true; //teacher has already created the channel
                                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                    intent.putExtra("username", username); //pass the username and channel name to the mainactivity class
                                                                    intent.putExtra("channel", channelName);
                                                                    intent.putExtra("type", userType);
                                                                    startActivity(intent);
                                                                }
                                                            }

                                                            if(found == false)
                                                            {
                                                                Toast.makeText(getApplicationContext(), "Teacher has not created channel yet.", Toast.LENGTH_LONG).show();
                                                                mEmailView.setText("");
                                                                mPasswordView.setText("");
                                                                mChannelName.setText("");
                                                                CountDownTimer timer = new CountDownTimer(5000, 5000)
                                                                {
                                                                    public void onTick(long millisUntilFinished)
                                                                    {
                                                                    }

                                                                    public void onFinish()
                                                                    {
                                                                        restartActivity();
                                                                    }
                                                                };
                                                                timer.start();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                                else { //if teacher

                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    intent.putExtra("username", username); //pass the username and channel name to the mainactivity class
                                                    intent.putExtra("channel", channelName);
                                                    intent.putExtra("type", userType);
                                                    startActivity(intent);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError firebaseError) {

                                        }
                                    });

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("Auth", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                                // ...
                            } else {
                                Toast.makeText(LoginActivity.this, "Invalid Channel Name. Channel Name cannot contain \".\"",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else {
            if (!channelName.contains(".")) {
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
                            String type = String.valueOf(data.getValue());
                            if (user.equals(tempUsername)) {
                                if (!type.equals(userType)) {
                                    Toast.makeText(getApplicationContext(), "User is not a " + userType + ". Changing to correct user type.",
                                            Toast.LENGTH_LONG).show();
                                    if (userType.equals("student"))
                                        userType = "teacher";
                                    else
                                        userType = "student";

                                }
                                found = true;
                                break;
                            }
                        }

                        if (found == false) {
                            Toast.makeText(getApplicationContext(), "User does not exist", Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("username", username); //pass the username and channel name to the mainactivity class
                            intent.putExtra("channel", channelName);
                            intent.putExtra("type", userType);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

                    }
                });
            } else {
                Toast.makeText(LoginActivity.this, "Invalid Channel Name. Channel Name cannot contain \".\"",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }


    void restartActivity()
    {
        finish();
        startActivity(getIntent());     //restart activity
    }


    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
      /*  FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Intent intent = new Intent(this, UserActivity.class);
            startActivity(intent);
        }
        // segue to UserActivity*/
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
       //     Toast.makeText(LoginActivity.this, "Password must be at least 5 characters.",
      //              Toast.LENGTH_SHORT).show();
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);

        }

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

//            try {
//                // Simulate network access.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }

//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }



            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }


    }

    public void registerClicked(View v) //if user is not currently registered
    {
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("type", userType);
        intent.putExtra("tempChannel", tempChannel);
        startActivity(intent);
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
                            mEmailView.setFocusable(false);
                            mEmailView.setEnabled(false);
                            mPasswordView.setFocusable(false);
                            mPasswordView.setEnabled(false);

                            fbemail = mAuth.getCurrentUser().getEmail();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
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

