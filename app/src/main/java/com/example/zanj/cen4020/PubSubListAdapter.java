package com.example.zanj.cen4020;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.google.common.collect.ImmutableMap;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PubSubListAdapter extends ArrayAdapter<PubSubPojo> {
    private final Context context;
    private final LayoutInflater inflater;
    private final List<PubSubPojo> values = new ArrayList<PubSubPojo>();
    String username, channelname;
    PubNub pn;

    public PubSubListAdapter(Context context) {
        super(context, R.layout.list_row_pubsub); //set the contextview
        this.context = context;
        this.inflater = LayoutInflater.from(context); //inflate layout
    }

    @Override
    public void add(PubSubPojo message) {
        int i = 0;
        Boolean edit = false;
        for(PubSubPojo msg : values)
        {
            if(msg.getMessage_id().equals(message.getMessage_id()))
            {
                values.set(i, message);
                edit = true;
                break;
            }
            i++;
        }
        if(edit == false)
            this.values.add(0, message);

        ((Activity) this.context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            } //notifying that the listview has been updated
        });
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final PubSubPojo dsMsg = this.values.get(position);
        PubSubListRowUi msgView;

        if (convertView == null) {
            msgView = new PubSubListRowUi(); //row template

            convertView = inflater.inflate(R.layout.list_row_pubsub, parent, false);

            msgView.sender = (TextView) convertView.findViewById(R.id.sender);
            msgView.message = (TextView) convertView.findViewById(R.id.message);
            msgView.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
            msgView.upvotes = (TextView) convertView.findViewById(R.id.upvotes);
            convertView.setTag(msgView);

        } else {
            msgView = (PubSubListRowUi) convertView.getTag();
        }

        convertView.setOnClickListener(new View.OnClickListener() { //makes each row clickable
            @Override
            public void onClick(View view) {

                final String usr = dsMsg.getSender(); //obtains the username, message, and timestamp from the message that was clicked
                final String message1 = dsMsg.getMessage();
                final String time = dsMsg.getTimestamp();
                String upvotes = dsMsg.getUpvotes();
                final int upvoteInt = Integer.parseInt(upvotes);
                final String message_id = dsMsg.getMessage_id();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( //creating a dialog to alert the user
                        context);
                alertDialogBuilder.setTitle("Message Info");

                // if current channel is NOT a thread
                if(!channelname.contains(".")) {
                    alertDialogBuilder
                            .setCancelable(true)
                            .setPositiveButton("Upvote?", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) { //not implemented yet

                                    String newUpvote = String.valueOf(upvoteInt + 1);
                                    //will store message upvoted
                                    final Map<String, String> message = ImmutableMap.<String, String>of("message_id", message_id, "sender", usr, "message", message1, "timestamp", time, "upvotes", newUpvote);
                                    pn.publish().message(message).channel(channelname).shouldStore(true)
                                            .async(new PNCallback<PNPublishResult>() {
                                                       @Override
                                                       public void onResponse(PNPublishResult result, PNStatus status) {
                                                           try {
                                                               if (!status.isError()) {
                                                                   Log.v("App", "publish(" + JsonUtil.asJson(result) + ")");
                                                               } else {
                                                                   Log.v("App", "publishErr(" + JsonUtil.asJson(status) + ")");
                                                               }
                                                           } catch (Exception e) {
                                                               e.printStackTrace();
                                                           }
                                                       }
                                                   }
                                            );


                                }
                            })
                            .setNegativeButton("View Thread", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) { //enters the thread, which is a channel with the name channel.message_id
                                    String chan = channelname + "." + dsMsg.getMessage_id();
                                    Intent intent = new Intent(context, MainActivity.class);
                                    intent.putExtra("username", username); //pass the username and channel name to the mainactivity class
                                    intent.putExtra("channel", chan);

                                    Toast.makeText(context, chan, Toast.LENGTH_LONG).show();

                                    context.startActivity(intent);
                                }
                            });

                }
                else    // if current channel IS a thread
                {
                    alertDialogBuilder
                            .setCancelable(true)
                            .setPositiveButton("Upvote?", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) { //goes back to list of threads, which is the channel name before the "."

                                    String newUpvote = String.valueOf(upvoteInt + 1);
                                    //will store message upvoted
                                    final Map<String, String> message = ImmutableMap.<String, String>of("message_id", message_id, "sender", usr, "message", message1, "timestamp", time, "upvotes", newUpvote);
                                    pn.publish().message(message).channel(channelname).shouldStore(true)
                                            .async(new PNCallback<PNPublishResult>() {
                                                       @Override
                                                       public void onResponse(PNPublishResult result, PNStatus status) {
                                                           try {
                                                               if (!status.isError()) {
                                                                   Log.v("App", "publish(" + JsonUtil.asJson(result) + ")");
                                                               } else {
                                                                   Log.v("App", "publishErr(" + JsonUtil.asJson(status) + ")");
                                                               }
                                                           } catch (Exception e) {
                                                               e.printStackTrace();
                                                           }
                                                       }
                                                   }
                                            );


                                }
                            })
                            .setNegativeButton("Leave Thread", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id) { //not implemented yet
                                    int dot = channelname.indexOf(".");
                                    String chan = channelname.substring(0,dot);
                                    Intent intent = new Intent(context, MainActivity.class);
                                    intent.putExtra("username", username); //pass the username and channel name to the mainactivity class
                                    intent.putExtra("channel", chan);

                                    context.startActivity(intent);
                                }
                            });
                }

                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });

        msgView.sender.setText(dsMsg.getSender());
        msgView.message.setText(dsMsg.getMessage());
        msgView.timestamp.setText(dsMsg.getTimestamp());
        msgView.upvotes.setText(dsMsg.getUpvotes() + " upvotes");


        return convertView;
    }

    @Override
    public int getCount() {
        return this.values.size();
    }

    public void clear() { //clears the list
        this.values.clear();
        notifyDataSetChanged();
    }

    public void setUserAndChannel(String user, String channel)
    {
        username = user;
        channelname = channel;
    }

    public void setPubNub(PubNub pub)
    {
        pn = pub;
    }
}