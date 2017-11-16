package com.example.zanj.cen4020;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class UserTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type_activitiy);
    }

    public void teacherClicked(View v)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("type", "teacher");
        startActivity(intent);
    }

    public void studentClicked(View v)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("type", "student");
        startActivity(intent);
    }
}
