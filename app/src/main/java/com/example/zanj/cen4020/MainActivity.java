package com.example.zanj.cen4020;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Future reference, maybe add this to a main method so it loads
        // and runs before app specific info is being set up
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-ad43af60-b290-11e7-b4e4-2675c721e615");
        pnConfiguration.setPublishKey("pub-c-a499a424-2f8a-4205-9c96-2492afd6349f");

        PubNub pubnub = new PubNub(pnConfiguration);


    }
}
