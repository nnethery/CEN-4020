package com.example.zanj.cen4020;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//fragment that contains the listview
public class PubSubTabContentFragment extends Fragment implements View.OnClickListener{
    private PubSubListAdapter psAdapter; //fragment that stores the listview
    TextView userAndChannelTV;
    String userChannel;
    View view;
    Button sessionB;
    String userT;
    String theChannel;
    DatabaseReference ref2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pubsub, container, false);
        final ListView listView = (ListView) view.findViewById(R.id.message_list);
        listView.setAdapter(psAdapter);
        userAndChannelTV = (TextView) view.findViewById(R.id.user_name);
        sessionB = (Button) view.findViewById(R.id.sessionButton);
        sessionB.setOnClickListener(this);
        ref2 = FirebaseDatabase.getInstance().getReference("channels");
        userAndChannelTV.setText(userChannel);
        if(userT.equals("student"))
            sessionB.setVisibility(View.GONE);
        return view;
    }

    public void setAdapter(PubSubListAdapter psAdapter) {
        this.psAdapter = psAdapter;
    }

    public void setUserAndChannel(String userAndChannel)
    {
        userChannel = userAndChannel;
    }

    public void setUserType(String userType, String channel) { userT = userType; theChannel = channel; }

    public void onClick(View view){ //start or end session
        String buttonText = sessionB.getText().toString();
        if(buttonText.equals("Start Session")) //set chat to active
        {
            sessionB.setText("End Session");
            ref2.child(theChannel).setValue("active");
        }
        else//set chat to inactive
        {
            sessionB.setText("Start Session");
            ref2.child(theChannel).setValue("inactive");
        }
    }
}