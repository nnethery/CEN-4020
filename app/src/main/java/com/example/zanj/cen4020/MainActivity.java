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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
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

import org.json.JSONObject;

//Main class that contains the messages
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    String channelName, username;
    PubNub pubnub;                                                      //main connection the pubnub server
    private PubSubPnCallback mPubSubPnCallback;                         //call back class for posting and loading messages
    private PubSubListAdapter mPubSub;                                  //adapter that contains the list of messages
    public List<String> PUBSUB_CHANNEL;
    PubSubTabContentFragment chatFrag;                                  //fragment stores the listview of messages
    DatabaseReference ref;
    String userType;
    private FirebaseMessaging fm;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userType = getIntent().getStringExtra("type");
        channelName = getIntent().getStringExtra("channel");                    //get the channel name and username
        username = getIntent().getStringExtra("username");
        mPubSub = new PubSubListAdapter(this, userType);
        mPubSubPnCallback = new PubSubPnCallback(mPubSub, getApplicationContext());
        fm = FirebaseMessaging.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //add a fragment
        chatFrag = new PubSubTabContentFragment();                              //set the fragment
        fragmentTransaction.add(R.id.myfragment, chatFrag);
        fragmentTransaction.commit();

        chatFrag.setAdapter(mPubSub);                                           //add the adapter to the fragment
        if(channelName.contains("."))
        {
            String originalMessage = getIntent().getStringExtra("originalMessage");
            String originalSender = getIntent().getStringExtra("originalSender");
            chatFrag.setUserAndChannel("Hello: " + username + ", Replying to " + originalSender + "'s message: " + originalMessage);
        }
        else
            chatFrag.setUserAndChannel("Hello: " + username + ", Channel: " + channelName);
        PUBSUB_CHANNEL = Arrays.asList(channelName);
        mPubSub.setUserAndChannel(username, channelName);                       //setting the user and the channel
        PNConfiguration pnConfiguration = new PNConfiguration();                //create a config
        pnConfiguration.setSubscribeKey("sub-c-ad43af60-b290-11e7-b4e4-2675c721e615");  //set the subscribe and publish keys
        pnConfiguration.setPublishKey("pub-c-a499a424-2f8a-4205-9c96-2492afd6349f");
        pnConfiguration.setUuid(username);                                      //set the username
        pnConfiguration.setSecure(true);
        pubnub = new PubNub(pnConfiguration);
        initChannels(); //initialize channels and load message hirtory
        loadMessages();
        mPubSub.setPubNub(pubnub);  //passing the pubnub instance to an additional class
        ref = FirebaseDatabase.getInstance().getReference("messageIDs");    //get an instance of the firebase database
        ref.addValueEventListener(new ValueEventListener() { //for firebase database, not in use currently
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the post
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });

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
        final String timestamp = DateTimeUtil.getTimeStampUtc();
        final String key = ref.push().getKey(); //get a unique hashed key for this message and add to firebase, necessary for message ids and threads
        ref.child(key).child("timestamp").setValue(timestamp); //add the message ID and timestamp to firebase, will use in future for deletion of firebase entries
        Map<String, String> message = null;
        if(userType.equals("teacher"))
        {
            message = ImmutableMap.<String, String>of("message_id", key, "sender", MainActivity.this.username, "message", mMessage.getText().toString(), "timestamp", timestamp, "upvotes", "null");
        }
        else {
            message = ImmutableMap.<String, String>of("message_id", key, "sender", MainActivity.this.username, "message", mMessage.getText().toString(), "timestamp", timestamp, "upvotes", "");
        }
        final Map<String, String> finalMessage = message;
        pubnub.publish().message(message).channel(channelName).shouldStore(true) //publish the message to the channel
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        try {
                            if (!status.isError()) {
                                mMessage.setText("");
                                Log.v(TAG, "publish(" + JsonUtil.asJson(result) + ")");

                                fm.send(new RemoteMessage.Builder("164390173589@gcm.googleapis.com")
                                        .setMessageId(finalMessage.get("message_id"))
                                        .addData("messageBody", finalMessage.get("message"))
                                        .addData("channel", channelName)
                                        .addData("sender", finalMessage.get("sender"))
                                        .addData("firebaseUID", user.getUid())
                                        .build());

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
