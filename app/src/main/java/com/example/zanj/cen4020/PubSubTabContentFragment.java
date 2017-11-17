package com.example.zanj.cen4020;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//fragment that contains the listview
public class PubSubTabContentFragment extends Fragment {
    private PubSubListAdapter psAdapter; //fragment that stores the listview
    TextView userAndChannelTV;
    String userChannel;
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pubsub, container, false);
        final ListView listView = (ListView) view.findViewById(R.id.message_list);
        listView.setAdapter(psAdapter);
        userAndChannelTV = (TextView) view.findViewById(R.id.user_name);
        userAndChannelTV.setText(userChannel);
        return view;
    }

    public void setAdapter(PubSubListAdapter psAdapter) {
        this.psAdapter = psAdapter;
    }

    public void setUserAndChannel(String userAndChannel)
    {
        userChannel = userAndChannel;
    }
}