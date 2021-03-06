package com.aekan.navya.lpgbooking;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.aekan.navya.lpgbooking.utilities.LPG_AlertBoxClass;
import com.aekan.navya.lpgbooking.utilities.LPG_SQLOpenHelperClass;
import com.aekan.navya.lpgbooking.utilities.LPG_SQL_ContractClass;
import com.aekan.navya.lpgbooking.utilities.LPG_Utility;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by aruramam on 1/8/2017.
 * Analytics code was added on 18 Jan 2018
 */

public class ConfirmLPGBookingCompletion extends AppCompatActivity {

    public final String LPGCONNECTIONEXPIRYDAYS = "FAILURE TO FETCH CONNECTION EXPIRY DAYS FROM DB";
    private SQLiteDatabase mdb;
    private LPG_AlertBoxClass mAlertBox;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String mEventConfirmation;
    private String mEventFailure;
    {
        mEventConfirmation = "INVALID";
        mEventFailure = "INVALID";
    }
    /*Create the activity which would request confirmation from
    user about LPG Booking attempt - the activity will reset the corresponding
    LPG Cylinder's last booked date to current date, and create alarma.
    If the attempt was a failure, the app would redirect the user to try boooking one more time. */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        //Call onCreate from super class
        super.onCreate(savedInstanceState);

        //initialise FireBase Analytics object
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        // Define the local variables for onCreate method
        //get connection id for the cylinder
        final String LPG_CONNECTION_ID = getIntent().getStringExtra(LPG_Utility.LPG_CONNECTION_ID) ;
        final String LPG_CONNECTION_NAME = getIntent().getStringExtra(LPG_Utility.LPG_CONNECTION_NAME);

        //initialise value for event channel
        if(getIntent().hasExtra(LPG_Utility.CONFIRMATION_CHANNEL)) {
            String confirmationChannel = getIntent().getStringExtra(LPG_Utility.CONFIRMATION_CHANNEL);
            if (confirmationChannel.equals(LPG_Utility.CONFIRMATION_CHANNEL_PHONE)){
            mEventConfirmation = LPG_Utility.CONFIRM_REFILL_PHONE;
            mEventFailure = LPG_Utility.BOOKING_FAILED_PHONE ;
        } else if (confirmationChannel.equals(LPG_Utility.CONFIRMATION_CHANNEL_SMS)) {
                mEventConfirmation = LPG_Utility.CONFIRM_REFILL_SMS;
                mEventFailure = LPG_Utility.BOOKING_FAILED_SMS;
            }
        }

        final AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        final String LPGConnectionExpiryDays = getLPGCONNECTIONEXPIRYDAYS(LPG_CONNECTION_ID);
        final String currentDateString = getCurrentDateString();
        mAlertBox = new LPG_AlertBoxClass();
        Log.v("In ConfirmBooking", " LPG Connection Id = " + LPG_CONNECTION_ID);
        //set content for the activity
        setContentView(R.layout.confirm_lpg_submission);

        //set on click listeners for Yes and no buttons
        //get the button controls
        Button buttonConfirmLPGBookingYes =  findViewById(R.id.confirmbooking_yes);
        Button buttonConfirmLPGBookingNo =  findViewById(R.id.confirmbooking_no);
        FloatingActionButton floatingActionButtonBack =  findViewById(R.id.fabBack);
        mdb = new LPG_SQLOpenHelperClass(getApplicationContext()).getWritableDatabase();

        floatingActionButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intentMainActivity);
            }
        });

        /*set on-click listener for the yes buttonConfirmLPGBookingYes
        Now this has many parts to it.
        We need to update the Database with the last booked date as current date
        have started tracking as per new booking.
        When this is all done, we will give a notification to user that we
        And , set alarms for notification for new expired expiry date*/
        buttonConfirmLPGBookingYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update the current date string in database as the last booked date
                // for the cyliner in question

                if (updateConnectionExpiryDate(mdb, currentDateString, LPG_CONNECTION_ID)) {
                    //remove cached data

                    LPG_Utility.removeCacheLocalConnectionDetails(LPG_CONNECTION_ID);
                    //set alarms for new expiry date
                    LPG_Utility.RefillAlarmNotification[] newRefillAlarmDates = LPG_Utility.getRefillRemainder(ConfirmLPGBookingCompletion.this, currentDateString, LPGConnectionExpiryDays, LPG_CONNECTION_ID, LPG_CONNECTION_NAME, LPG_Utility.LPG_GET_REGULAR_ALARM_NOTIFICATION_DATES);
                    for (LPG_Utility.RefillAlarmNotification counterRefillAlarmDates : newRefillAlarmDates) {
                        if (!(alarmManager == null)) {
                            alarmManager.set(AlarmManager.RTC, counterRefillAlarmDates.getGregorialCalendar().getTimeInMillis(), counterRefillAlarmDates.getRefillCylinder());
                        }
                    }
                    Log.v("Alarm","Last Booked date is " + currentDateString + " and expiry date is " +  LPGConnectionExpiryDays );
                    Log.v("Alarm", "Midway alarm date is " + LPG_Utility.getDateFromCalendar(newRefillAlarmDates[0].getGregorialCalendar()));
                    Log.v("Alarm", "Expiry alarm date is " + LPG_Utility.getDateFromCalendar(newRefillAlarmDates[1].getGregorialCalendar()));
                    //Set a success message to user
                    mAlertBox.showDialogHelper(getResources().getString(R.string.confirmbooking_success),
                            "Ok",
                            null,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }, null).show(getSupportFragmentManager(), "Successful Booking");



                    //Log FireBase Analytics for confirmation

                        Bundle eventBundle = new Bundle();
                        eventBundle.putString( LPG_Utility.PARAMETER_ANALYTICS_EVENT_PARAM,mEventConfirmation);
                        eventBundle.putString(LPG_Utility.PARAMETER_ANALYTICS_ACTIVITY_PARAM,"ConfirmLPGBookingCompletion");
                        mFirebaseAnalytics.logEvent(LPG_Utility.EVENT_CONFIRM_BOOKING,eventBundle);


                }



            }
        });


        //set onclick listener for No button
        buttonConfirmLPGBookingNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set an alarm to notify user in a day's time
                LPG_Utility.RefillAlarmNotification[] alarmSnooze = LPG_Utility.getRefillRemainder(ConfirmLPGBookingCompletion.this, currentDateString, LPGConnectionExpiryDays, LPG_CONNECTION_ID, LPG_CONNECTION_NAME, LPG_Utility.LPG_GET_SNOOZE_ALARM_DATES);
                if (alarmManager != null) {
                    alarmManager.set(AlarmManager.RTC, alarmSnooze[0].getGregorialCalendar().getTimeInMillis(), alarmSnooze[0].getRefillCylinder());
                }

                //log event for failure to get confirmation




                Bundle eventBundle = new Bundle();
                eventBundle.putString( LPG_Utility.PARAMETER_ANALYTICS_EVENT_PARAM,mEventFailure);
                eventBundle.putString(LPG_Utility.PARAMETER_ANALYTICS_ACTIVITY_PARAM,"ConfirmLPGBookingCompletion");
                mFirebaseAnalytics.logEvent(LPG_Utility.EVENT_BOOKING_FAILED,eventBundle);

                //Set a toast message that user can try booking
                //after some time
                mAlertBox.showDialogHelper(getResources().getString(R.string.confirmbooking_failure),
                        "Ok",
                        null,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                if ( intent.resolveActivity(getPackageManager()) != null){
                                    startActivity(intent);
                                }
                            }
                        },null
                ).show(getSupportFragmentManager(),"Failure to update");


            }
        });
    }

    public String getLPGCONNECTIONEXPIRYDAYS (String LPGCONNECTIONID){
        mdb = (new LPG_SQLOpenHelperClass(getApplicationContext())).getReadableDatabase();
        Log.v("In ConfirmBooking ", " getLPGConnectionExpiryDays LPG Connection Id = " + LPGCONNECTIONID);
        if (mdb == null) {
            Log.v("In ConfirmBooking ", "DB is null");

        } else {
            Log.v("In ConfirmBooking ", "DB is not null");
        }

        String[] fieldListExpectedExpiryDate = {LPG_SQL_ContractClass.LPG_CONNECTION_ROW.CONNECTION_EXPIRY_DAYS};
        String whereClause = LPG_SQL_ContractClass.LPG_CONNECTION_ROW._ID + " =?";
        String[] whereClauseFilter = {LPGCONNECTIONID};

        String ConnectionExpiryDays;
        try {
            Log.v("In ConfirmBoooking", " Before DB Query");
            SQLiteCursor c = (SQLiteCursor) mdb.query(
                    LPG_SQL_ContractClass.LPG_CONNECTION_ROW.TABLE_NAME,
                    fieldListExpectedExpiryDate,
                    whereClause,
                    whereClauseFilter,
                    null,
                    null,
                    null);
            Log.v("In ConfirmBooking", "Count " + Integer.toString(c.getCount()));
            Log.v("In ConfirmBooking ", "C.movetofirst " + Boolean.toString(c.moveToFirst()));


            ConnectionExpiryDays = c.getString(0);
            Log.v("In Cursor ", ConnectionExpiryDays);

            c.close();


        } catch (Exception e){
            ConnectionExpiryDays = LPGCONNECTIONEXPIRYDAYS;
            Log.v("Error in Confirm LPG", e.toString());

            mAlertBox.showDialogHelper(getResources().getString(R.string.confirmbooking_cursorfetchfailure),
                    "Ok",
                    null,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    },
                    null
            ).show(getSupportFragmentManager(),"Failure to get Connection Expiry Days");
        }

        //close DB
        //  db.close();

        //return connection expiry days
        return ConnectionExpiryDays ;
    }

    public String getCurrentDateString() {
        GregorianCalendar currentCalendar = (GregorianCalendar) Calendar.getInstance();
        int currentDate = currentCalendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = currentCalendar.get(Calendar.MONTH);
        int currentYear = currentCalendar.get(Calendar.YEAR);
        return Integer.toString(currentDate) + "/" + Integer.toString(currentMonth + 1) + "/" + Integer.toString(currentYear);
    }

    public boolean updateConnectionExpiryDate(SQLiteDatabase db, String currentDateString, String LPG_CONNECTION_ID) {
        boolean isUpdateSuccessful = true;

        ContentValues updateFieldsList = new ContentValues();
        updateFieldsList.put(LPG_SQL_ContractClass.LPG_CONNECTION_ROW.LAST_BOOKED_DATE, currentDateString);
        String whereClause = LPG_SQL_ContractClass.LPG_CONNECTION_ROW._ID + " = ?";
        String[] whereClauseFilter = {LPG_CONNECTION_ID};
        try {
            int count = db.update(LPG_SQL_ContractClass.LPG_CONNECTION_ROW.TABLE_NAME,
                    updateFieldsList,
                    whereClause,
                    whereClauseFilter
            );
            Log.v("UpdateDB ", "whereClause " + whereClause);
            Log.v("UpdateDB ", "Connection id " + LPG_CONNECTION_ID);
            Log.v("UpdateDB ", "Current Date String " + currentDateString);
            Log.v("UpdateDB ", "whereClauseFilter " + Arrays.toString(whereClauseFilter));
            Log.v("UpdateDB ", "updated count " + Integer.toString(count));




        } catch (Exception e) {
            //DB update has failed. Print the exception to log, and give an error message to user
            mAlertBox.showDialogHelper(getResources().getString(R.string.confirmbooking_updatefailure), "Ok", null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }, null).show(getSupportFragmentManager(), "Failure to Update DB");
            Log.v("Update failure in DB", e.toString());
            isUpdateSuccessful = false;

        }


        return isUpdateSuccessful;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }



}
