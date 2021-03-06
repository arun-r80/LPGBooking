package com.aekan.navya.lpgbooking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by arunramamurthy on 20/06/17.
 */

public class SMSBookingRegistration extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //call super class
        super.onCreate(savedInstanceState);
        //inflate
        setContentView(R.layout.sms_registration);
        //Configure tool bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.smsregistration_toolbar);

        toolbar.setTitle(R.string.smsbooking_activity_title);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(homeActivity);
            }
        });
        setSupportActionBar(toolbar);

    }

}
