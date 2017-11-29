package com.example.zanj.cen4020;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserTypeActivity extends AppCompatActivity {
    String channelTemp;
    DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ref = FirebaseDatabase.getInstance().getReference("channels");    //get an instance of the firebase database
        setContentView(R.layout.activity_user_type_activitiy);
    }

    public void teacherClicked(View v)
    {
        //give teachers the option to create a channel
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create a new channel?");

        // Set up the input
        final EditText input = new EditText(this);
         // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Create Channel.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                channelTemp = input.getText().toString();
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean found = false;
                        for(DataSnapshot data : dataSnapshot.getChildren())
                        {
                            String channel = String.valueOf(data.getKey());
                            if(channel.equals(channelTemp))
                            {
                                Toast.makeText(UserTypeActivity.this, "Channel already exists. Please create a new one.", Toast.LENGTH_SHORT).show();
                                builder.show();
                                found = true;
                                break;
                            }
                        }
                        if(found == false)
                        {
                            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                            intent.putExtra("type", "teacher");
                            ref.child(channelTemp).setValue("inactive");    //add the channel to the database and set it to not active
                            intent.putExtra("channelTemp", channelTemp);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        builder.setNegativeButton("Go to sign in/register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                intent.putExtra("type", "teacher");
                startActivity(intent);
            }
        });

        builder.show();

    }

    public void studentClicked(View v)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("type", "student");
        startActivity(intent);
    }
}
