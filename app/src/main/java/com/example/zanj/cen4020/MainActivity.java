package com.example.zanj.cen4020;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.history.PNHistoryItemResult;
import com.pubnub.api.models.consumer.history.PNHistoryResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import android.content.Intent;
import java.util.Arrays;
import com.example.zanj.cen4020.PubSubListAdapter;
import com.example.zanj.cen4020.PubSubPnCallback;
import com.example.zanj.cen4020.JsonUtil;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    String channelName, username;
    PubNub pubnub;                                                      //main connection the pubnub server
    private PubSubPnCallback mPubSubPnCallback;                         //call back class for posting and loading messages
    private PubSubListAdapter mPubSub;                                  //adapter that contains the list of messages
    public List<String> PUBSUB_CHANNEL;
    PubSubTabContentFragment chatFrag;                                  //fragment stores the listview of messages


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPubSub = new PubSubListAdapter(this);
        mPubSubPnCallback = new PubSubPnCallback(mPubSub);

        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //add a fragment
        chatFrag = new PubSubTabContentFragment();                              //set the fragment
        fragmentTransaction.add(R.id.myfragment, chatFrag);
        fragmentTransaction.commit();

        chatFrag.setAdapter(mPubSub);                                           //add the adapter to the fragment

        channelName = getIntent().getStringExtra("channel");                    //get the channel name and username
        username = getIntent().getStringExtra("username");
        PUBSUB_CHANNEL = Arrays.asList(channelName);
        mPubSub.setUserAndChannel(username, channelName);                       //setting the user and the channel
        PNConfiguration pnConfiguration = new PNConfiguration();                //create a config
        pnConfiguration.setSubscribeKey("sub-c-ad43af60-b290-11e7-b4e4-2675c721e615");  //set the subscribe and publish keys
        pnConfiguration.setPublishKey("pub-c-a499a424-2f8a-4205-9c96-2492afd6349f");
        pnConfiguration.setUuid(username);                                      //set the username
        pnConfiguration.setSecure(true);
        pubnub = new PubNub(pnConfiguration);
        initChannels();
        loadMessages();
    }

    private final void initChannels()
    {
        pubnub.addListener(mPubSubPnCallback);
        pubnub.subscribe().channels(PUBSUB_CHANNEL).withPresence().execute();
    }

    public void loadMessages()                      //load last 100 messages from the past 3 days
    {
        pubnub.history()
                .channel(channelName)
                .count(100)
                .async(new PNCallback<PNHistoryResult>() {
                    @Override
                    public void onResponse(PNHistoryResult result, PNStatus status) {
                        for(PNHistoryItemResult res : result.getMessages())
                        {
                            mPubSubPnCallback.loadMessage(res);     //store the messages inside the listview
                        }
                    }
                });
    }

    public void publish(View view){ //publish method for publishing the messages to the server
        final EditText mMessage = (EditText) MainActivity.this.findViewById(R.id.new_message);
        final Map<String, String> message = ImmutableMap.<String, String>of("sender", MainActivity.this.username, "message", mMessage.getText().toString(), "timestamp", DateTimeUtil.getTimeStampUtc());
        MainActivity.this.pubnub.publish().channel(channelName).message(message).shouldStore(true).async(
                new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        try {
                            if (!status.isError()) {
                                mMessage.setText("");
                                Log.v(TAG, "publish(" + JsonUtil.asJson(result) + ")");
                            } else {
                                Log.v(TAG, "publishErr(" + JsonUtil.asJson(status) + ")");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
}
