package com.example.zanj.cen4020;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class PubSubTabContentFragment extends Fragment {
    private PubSubListAdapter psAdapter; //fragment that stores the listview
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pubsub, container, false);
        final ListView listView = (ListView) view.findViewById(R.id.message_list);
        listView.setAdapter(psAdapter);
        return view;
    }

    public void setAdapter(PubSubListAdapter psAdapter) {
        this.psAdapter = psAdapter;
    }
}