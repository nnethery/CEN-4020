package com.example.zanj.cen4020;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class UserActivity extends AppCompatActivity {

    EditText usernameET, channelET;
    String username, channel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_user);
        usernameET = findViewById(R.id.etUsername);
        channelET = findViewById(R.id.etChannelname);

    }

    public void loginClicked(View v)
    {
        username = usernameET.getText().toString();
        channel = channelET.getText().toString();
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("channel", channel);
        startActivity(intent);
    }

}
