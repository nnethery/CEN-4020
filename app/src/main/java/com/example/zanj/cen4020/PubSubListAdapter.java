package com.example.zanj.cen4020;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;

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
    String userType;

    public PubSubListAdapter(Context context, String type) {
        super(context, R.layout.list_row_pubsub); //set the contextview
        this.context = context;
        this.inflater = LayoutInflater.from(context); //inflate layout
        this.userType = type;
    }

    public class CustomComparator implements Comparator<PubSubPojo> {
        @Override
        public int compare(PubSubPojo o1, PubSubPojo o2) {
            if(o1.getUpvotes().equals("null") && !o2.getUpvotes().equals("null"))
            {
                return 1;
            }
            else if(!o1.getUpvotes().equals("null") && o2.getUpvotes().equals("null"))
            {
                return -1;
            }
            else if(o1.getUpvotes().equals("null") && o2.getUpvotes().equals("null"))
            {
                return 0;
            }
            return o2.getUpvotes().compareTo(o1.getUpvotes());
        }
    }

    @Override
    public void add(PubSubPojo message) {
        int i = 0;
        Boolean edit = false;
        for(PubSubPojo msg : values) //needed for upvoting, need to determine if there is a duplicate message
        {
            if(msg.getMessage_id().equals(message.getMessage_id()))
            {
                values.set(i, message); //replace the older message with the newer one that has more upvotes
                edit = true;
                break;
            }
            i++;
        }
        if(edit == false)
            this.values.add(0, message);

        if(userType.equals("teacher")) //teacher only views messages by most upvoted
            Collections.sort(values, new CustomComparator());

        ((Activity) this.context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            } //notifying that the listview has been updated
        });
    }

    @Override
    public int getItemViewType(int position)
    {
        return this.values.get(position).getSender().equals(username) ? 0:1;
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final PubSubPojo dsMsg = this.values.get(position);
        PubSubListRowUi msgView;

        if (convertView == null) {
            msgView = new PubSubListRowUi(); //row template

           // convertView = inflater.inflate(R.layout.list_row_pubsub, parent, false);

            if (getItemViewType(position) == 1) {
                convertView = inflater.inflate(R.layout.list_row_pubsub, parent, false);

                msgView.row_layout = (LinearLayout) convertView.findViewById(R.id.row_layout);
                msgView.msg_info_layout = (LinearLayout) convertView.findViewById(R.id.msg_info_layout);
                msgView.sender = (TextView) convertView.findViewById(R.id.sender);
                msgView.message = (TextView) convertView.findViewById(R.id.message);
                msgView.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
                msgView.upvotes = (TextView) convertView.findViewById(R.id.upvotes);
                convertView.setTag(msgView);
            }
            else
            {
                convertView = inflater.inflate(R.layout.list_row_pubsub, parent, false);

                msgView.row_layout = (LinearLayout) convertView.findViewById(R.id.row_layout);
                msgView.msg_info_layout = (LinearLayout) convertView.findViewById(R.id.msg_info_layout);
                msgView.sender = (TextView) convertView.findViewById(R.id.sender);
                msgView.message = (TextView) convertView.findViewById(R.id.message);
                msgView.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
                msgView.upvotes = (TextView) convertView.findViewById(R.id.upvotes);
                convertView.setTag(msgView);

                msgView.message.setBackgroundResource(R.drawable.messagebubble2);
                msgView.row_layout.setGravity(Gravity.END);
                msgView.msg_info_layout.setGravity(Gravity.END);
                msgView.message.setGravity(Gravity.END);
            }

        } else {
            msgView = (PubSubListRowUi) convertView.getTag();
        }
        if(!userType.equals("teacher") && !dsMsg.getUpvotes().equals("null")) { //teacher cannot view thread or upvote message
            //a teacher's message cannot be made a thread and cannot be upvoted
            convertView.setOnClickListener(new View.OnClickListener() { //makes each row clickable
                @Override
                public void onClick(View view) {

                    final String usr = dsMsg.getSender(); //obtains the username, message, upvotes, messageID and timestamp from the message that was clicked
                    final String message1 = dsMsg.getMessage();
                    final String time = dsMsg.getTimestamp();
                    final String upvotes = dsMsg.getUpvoted();
                   // final int upvoteInt = Integer.parseInt(upvotes);
                    final String message_id = dsMsg.getMessage_id();

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( //creating a dialog to alert the user
                            context);
                    alertDialogBuilder.setTitle("Message Info");

                    // if current channel is NOT a thread
                    if (!channelname.contains(".")) {
                        if(!upvotes.contains(username)) {
                            alertDialogBuilder
                                    .setCancelable(true)
                                    .setPositiveButton("Upvote?", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            //String newUpvote = String.valueOf(upvoteInt + 1);
                                            String newUpvote = upvotes + username + " ";
                                            //will store message upvoted
                                            final Map<String, String> message = ImmutableMap.<String, String>of("message_id", message_id, "sender", usr, "message", message1, "timestamp", time, "upvotes", newUpvote);
                                            pn.publish().message(message).channel(channelname).shouldStore(true) //publish message
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
                                        intent.putExtra("type", userType);
                                        intent.putExtra("originalMessage", dsMsg.getMessage());
                                        intent.putExtra("originalSender", dsMsg.getSender());
                                        context.startActivity(intent);
                                    }
                                });}
                        else{
                            alertDialogBuilder
                                    .setCancelable(true)
                                    .setPositiveButton("Downvote?", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            //String newUpvote = String.valueOf(upvoteInt + 1);
                                            String temp = username + " ";
                                            String newUpvote = upvotes.replace(temp,"");
                                            //will store message upvoted
                                            final Map<String, String> message = ImmutableMap.<String, String>of("message_id", message_id, "sender", usr, "message", message1, "timestamp", time, "upvotes", newUpvote);
                                            pn.publish().message(message).channel(channelname).shouldStore(true) //publish message
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
                                            intent.putExtra("type", userType);
                                            intent.putExtra("originalMessage", dsMsg.getMessage());
                                            intent.putExtra("originalSender", dsMsg.getSender());
                                            context.startActivity(intent);
                                        }
                                    });}

                    }
                    else    // if current channel IS a thread
                    {
                        if(!upvotes.contains(username)) {
                            alertDialogBuilder
                                    .setCancelable(true)
                                    .setPositiveButton("Upvote?", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) { //goes back to list of threads, which is the channel name before the "."

                                            //String newUpvote = String.valueOf(upvoteInt + 1);
                                            String newUpvote = upvotes + username + " ";
                                            //will store message upvoted
                                            final Map<String, String> message = ImmutableMap.<String, String>of("message_id", message_id, "sender", usr, "message", message1, "timestamp", time, "upvotes", newUpvote);
                                            pn.publish().message(message).channel(channelname).shouldStore(true) //publish message
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
                                    .setNegativeButton("Leave Thread", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            int dot = channelname.indexOf(".");
                                            String chan = channelname.substring(0, dot);
                                            Intent intent = new Intent(context, MainActivity.class);
                                            intent.putExtra("username", username); //pass the username and channel name to the mainactivity class
                                            intent.putExtra("channel", chan);
                                            intent.putExtra("type", userType);

                                            context.startActivity(intent);
                                        }
                                    });}
                        else {
                            alertDialogBuilder
                                    .setCancelable(true)
                                    .setPositiveButton("Downvote?", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) { //goes back to list of threads, which is the channel name before the "."

                                            //String newUpvote = String.valueOf(upvoteInt + 1);
                                            String temp = username + " ";
                                            String newUpvote = upvotes.replace(temp,"");
                                            //will store message upvoted
                                            final Map<String, String> message = ImmutableMap.<String, String>of("message_id", message_id, "sender", usr, "message", message1, "timestamp", time, "upvotes", newUpvote);
                                            pn.publish().message(message).channel(channelname).shouldStore(true) //publish message
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
                                    .setNegativeButton("Leave Thread", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            int dot = channelname.indexOf(".");
                                            String chan = channelname.substring(0, dot);
                                            Intent intent = new Intent(context, MainActivity.class);
                                            intent.putExtra("username", username); //pass the username and channel name to the mainactivity class
                                            intent.putExtra("channel", chan);
                                            intent.putExtra("type", userType);

                                            context.startActivity(intent);
                                        }
                                    });}
                    }

                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();

                }
            });
        }
        //set the textviews for each aspect of the message
        msgView.sender.setText(dsMsg.getSender());
        msgView.message.setText(dsMsg.getMessage());
        msgView.timestamp.setText(dsMsg.getTimestamp());
        if(dsMsg.getUpvotes().equals("null"))
        {
            msgView.sender.setText("Instructor");
            msgView.upvotes.setText(""); //teacher's message should not allow upvotes
        }
        else
        {
            msgView.upvotes.setText(dsMsg.getUpvotes() + " upvotes");
        }

   /*     if(dsMsg.getSender().equals(username))
        {
            msgView.message.setBackgroundResource(R.drawable.messagebubble2);
            msgView.row_layout.setGravity(Gravity.END);
            msgView.msg_info_layout.setGravity(Gravity.END);
            msgView.message.setGravity(Gravity.END);

        }*/


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

    public void setUserAndChannel(String user, String channel) //set username and channel
    {
        username = user;
        channelname = channel;
    }

    public void setPubNub(PubNub pub)
    {
        pn = pub;
    } //set pubnub instance
}