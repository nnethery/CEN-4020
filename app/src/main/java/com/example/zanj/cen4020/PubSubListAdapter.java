package com.example.zanj.cen4020;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

public class PubSubListAdapter extends ArrayAdapter<PubSubPojo> {
    private final Context context;
    private final LayoutInflater inflater;
    private final List<PubSubPojo> values = new ArrayList<PubSubPojo>();
    String username, channelname;

    public PubSubListAdapter(Context context) {
        super(context, R.layout.list_row_pubsub); //set the contextview
        this.context = context;
        this.inflater = LayoutInflater.from(context); //inflate layout
    }

    @Override
    public void add(PubSubPojo message) {
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

            convertView.setTag(msgView);

        } else {
            msgView = (PubSubListRowUi) convertView.getTag();
        }

        convertView.setOnClickListener(new View.OnClickListener() { //makes each row clickable
            @Override
            public void onClick(View view) {

                String usr = dsMsg.getSender(); //obtains the username, message, and timestamp from the message that was clicked
                String message = dsMsg.getMessage();
                String time = dsMsg.getTimestamp();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( //creating a dialog to alert the user
                        context);
                alertDialogBuilder.setTitle("Message Info");
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Upvote?",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) { //not implemented yet
                                //will store message upvoted
                            }
                        })
                        .setNegativeButton("View Thread",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) { //not implemented yet
                                    //will open thread activity once created
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });

        msgView.sender.setText(dsMsg.getSender());
        msgView.message.setText(dsMsg.getMessage());
        msgView.timestamp.setText(dsMsg.getTimestamp());

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
}