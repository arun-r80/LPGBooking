package com.aekan.navya.lpgbooking;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aekan.navya.lpgbooking.utilities.LPGDataAPI;
import com.aekan.navya.lpgbooking.utilities.LPGServiceCallBackHandler;
import com.aekan.navya.lpgbooking.utilities.LPGServiceResponseCallBack;
import com.aekan.navya.lpgbooking.utilities.LPG_PhoneListener;
import com.aekan.navya.lpgbooking.utilities.LPG_SQL_ContractClass;
import com.aekan.navya.lpgbooking.utilities.LPG_Utility;
import com.aekan.navya.lpgbooking.utilities.lpgconnectionparcel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.aekan.navya.lpgbooking.utilities.LPG_Utility.Alert;
import static com.aekan.navya.lpgbooking.utilities.LPG_Utility.ifWeCanShowInterstitialAdNow;

/**
 * Created by arunramamurthy on 16/10/16.
 */

public class LPGBooking extends AppCompatActivity implements LPGServiceResponseCallBack, ActivityCompat.OnRequestPermissionsResultCallback {

    //constants to identify permission requests
    private final static int LPG_BOOKING_REQUEST_PERMISSION_CALL_PHONE = 1;
    private final static int LPG_BOOKING_REQUEST_PERMISSION_SMS = 2;
    private final static int PERMISSION_DENIED = 765324;
    private final static int PERMISSION_NOT_REQUESTED = 234;
    String lpgparcelConnectionProvider;
    private  int mPermissionStatus;

    Intent lpgBookingCallIntent;

    private InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFireBaseAnalytics;
    // use a field within this class to store phone no
    // this field would be initialised during onCreate and would
    // be used subsequently during permission response handling
    private String LPG_CONNECTION_PHONE_NO;
    private String LPG_SMS_REFILL_NO;
    private TelephonyManager telephonyManager;
    private LPG_PhoneListener phoneStateListener;
    private String lpgparcelConnectionId; // connection id of record being booked
   // private mSMSBroadcastReceiver mSMSBroadcast;
    //handle progress loader for SMS sending
    private Intent smsSentActionIntent;
    private Intent smsDeliveredActionIntent;
    //broadcast receiver objects for sms sent and delivered actions
    private LPGBooking.mSMSBroadcastReceiver smsSentReceiver;
    private boolean isSmsSentReceiverRegistered;
    private LPGBooking.mSMSBroadcastReceiver smsDeliveryReceiver;
    private boolean isSmsDeliveredReceiverRegistered;

    @Override
    @RequiresPermission(allOf = {Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE})

    protected void onCreate(@Nullable Bundle savedInstanceState) {

        //call superclass oncreate function
        super.onCreate(savedInstanceState);
        Log.v("LPGBooking ", "Inside LPG Booking");
        //set content view for the activity
        setContentView(R.layout.lpg_booking);

        //Disable editability of fields in the screen
        //for some reason, inputType is not working from layout file
        findViewById(R.id.lpgbooking_connectionname_edittext).setEnabled(false);
        findViewById(R.id.lpgbooking_provider_edittext).setEnabled(false);
        findViewById(R.id.lpgbooking_expected_expiry_date_edittext).setEnabled(false);

        //instantiate Firebase analytics;
        mFireBaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

        final ImageView lpgBookingCall = (ImageView) findViewById(R.id.lpgbooking_call_img);
        final ImageView lpgBookingSMS = (ImageView) findViewById(R.id.lpgbooking_sms_img);
        final TextView smsTipTextView = (TextView) findViewById(R.id.lpgbooking_smsnotification);
        mPermissionStatus = PERMISSION_NOT_REQUESTED;
        //make the edittext as non clickable


        lpgBookingCall.setEnabled(false);
        lpgBookingSMS.setEnabled(false);
        smsDeliveryReceiver = null;
        smsSentReceiver = null;
        isSmsDeliveredReceiverRegistered = false;
        isSmsSentReceiverRegistered = false;


        //set back stack for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.lpgbooking_toolbar);

        //set support action bar
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show interstitial ad or transtion to home activity
                if (!showInterStitialAd()) {
                    Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                    if (homeIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(homeIntent);
                    }

                }

            }
        });
        toolbar.setTitle(R.string.lpgbooking_title);

        //load interstitial ad
        loadInterstitialAd();

        //set null values for Phone and SMS - before binding lPG Connection details
        LPG_CONNECTION_PHONE_NO = null;
        LPG_SMS_REFILL_NO = null;

        //get the connection id from the parcel, which has been added to the intent
        lpgconnectionparcel lpgconnectioninfo = getIntent().getParcelableExtra(lpgconnectionparcel.LPG_CONNECTIONRECORD_PARCEL);
        lpgparcelConnectionId = lpgconnectioninfo.getId();
        boolean isFromNotification = lpgconnectioninfo.getNotificationFlag();

        //dismiss notification if call to this activity was made from
        //notification
        if (isFromNotification == true) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(Integer.parseInt(lpgparcelConnectionId));
        }






        if (!LPG_Utility.hasBeenCached(lpgparcelConnectionId)){
            LPGDataAPI addBookingAPI = new LPGDataAPI(getApplicationContext(), "Service Call from Add Booking");
            addBookingAPI.populateCylinderInfoThroughCursorWithRowID(lpgparcelConnectionId, new Messenger(new Handler(new LPGServiceCallBackHandler(this))));
        } else {
            updateActivityWithLPGDetailsCursor(LPG_Utility.getCacheLocalData(lpgparcelConnectionId));
        }


        // set onclick listener on image
        lpgBookingCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //trigger analytics code
                    if (mFireBaseAnalytics != null) {
                        // Code to be executed when the user has left the app.
                        Bundle bundleInterstitialAd = new Bundle();
                        bundleInterstitialAd.putString(LPG_Utility.PARAMETER_ANALYTICS_EVENT_PARAM, LPG_Utility.BOOK_THRU_PHONE);
                        bundleInterstitialAd.putString(LPG_Utility.PARAMETER_ANALYTICS_ACTIVITY_PARAM, "AddLPGConnection");
                        mFireBaseAnalytics.logEvent(LPG_Utility.INTENT_BOOK, bundleInterstitialAd);
                    }


                //check if the app has been given permissions to make a call
                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    //get permission from user to use the phone again
                    //before we inform the user, we need to check if we need user permission

                    if ((android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale(LPGBooking.this, Manifest.permission.CALL_PHONE))) {
                        Log.v("LPGBooking", "Inside Show request permission");
                        // if user permission is needed, inform a message to user through a dialogbox
                        //android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale(getParent(), Manifest.permission.CALL_PHONE)
                        // show the dialog box in a seperate thread so as not to stop the current execution

                        Intent permissionRequestor = new Intent(getApplicationContext(), PermissionCheckForFeature.class);
                        permissionRequestor.putExtra(LPG_Utility.PERMISSION_INTIMATION_MESSAGE, LPG_Utility.PERMISSION_CALL_INTIMATION);
                        startActivityForResult(permissionRequestor, LPG_BOOKING_REQUEST_PERMISSION_CALL_PHONE);


                    } else {
                        //need not show intimation to user
                        ActivityCompat.requestPermissions(LPGBooking.this, new String[]{Manifest.permission.CALL_PHONE}, LPG_BOOKING_REQUEST_PERMISSION_CALL_PHONE);
                    }

                } else {
                    // call the phone application
                    Log.v("LPGBooking", " About to call Booking Call Intent");
                    startActivity(lpgBookingCallIntent);
                }

            }
        });

        //set sms option to the app
        final Intent lpgBookThroughSMSIntent = new Intent(Intent.ACTION_VIEW);

        lpgBookingSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("SMS", "Within SMS Click");
                //trigger analytics code
                if (mFireBaseAnalytics != null) {
                    // Code to be executed when the user has left the app.
                    Bundle bundleInterstitialAd = new Bundle();
                    bundleInterstitialAd.putString(LPG_Utility.PARAMETER_ANALYTICS_EVENT_PARAM, LPG_Utility.BOOK_THRU_PHONE);
                    bundleInterstitialAd.putString(LPG_Utility.PARAMETER_ANALYTICS_ACTIVITY_PARAM, "AddLPGConnection");
                    mFireBaseAnalytics.logEvent(LPG_Utility.INTENT_BOOK, bundleInterstitialAd);
                }

                //check if application has permission to send sms
                if ((ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.SEND_SMS)) != PackageManager.PERMISSION_GRANTED) {
                    // no permission to send sms
                    // request permission for sending SMS

                    Intent SMSPermissonRequestor = new Intent(getApplicationContext(), PermissionCheckForFeature.class);
                    SMSPermissonRequestor.putExtra(LPG_Utility.PERMISSION_INTIMATION_MESSAGE, LPG_Utility.PERMISSION_SMS_INTIMATION);
                    startActivityForResult(SMSPermissonRequestor, LPG_BOOKING_REQUEST_PERMISSION_SMS);

                } else {
                    //the app has permission to send sms
                    Log.v("SMS", "Send SMS - App has SMS permission");


                    sendSMS();
                }
            }
        });
        //

    }

    @Override
    public void onRequestPermissionsResult(int RequestCode, String permissions[], int[] grantResults) {
        // handle cases for permissions result for call and sms based on RequestCode

        switch (RequestCode) {
            case LPG_BOOKING_REQUEST_PERMISSION_CALL_PHONE: {
                if ((grantResults.length != 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission request was successful with the user
                    // start the intent to call the user
                    if (LPG_CONNECTION_PHONE_NO != null) {

                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        startActivity(lpgBookingCallIntent);
                    }


                } else {

                    mPermissionStatus = PERMISSION_DENIED;


                }
                break;
            }

            case LPG_BOOKING_REQUEST_PERMISSION_SMS: {
                // check if permission has been granted

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // send sms
                    Log.v("SMS", "Send SMS after permission grant");
                    sendSMS();
                } else {

                    mPermissionStatus = PERMISSION_DENIED;
                }
                break;
            }

        }

    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        //register the broadcast
//        Log.v("Resume", "In ONResume");
//        mSMSBroadcast = new LPGBooking.mSMSBroadcastReceiver();
//        IntentFilter smsIntentFilter = new IntentFilter(	Telephony.Sms.Intents.SMS_DELIVER_ACTION);
//        smsIntentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
//        this.registerReceiver(mSMSBroadcast,smsIntentFilter);
//    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister broadcast
        //unregister broadcast receivers for sms delivery and sending
        if (isSmsSentReceiverRegistered == true) {
            unregisterReceiver(smsDeliveryReceiver);
        }
        if (isSmsDeliveredReceiverRegistered == true) {
            unregisterReceiver(smsSentReceiver);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        //unregister broadcast receivers for sms delivery and sending
        if (isSmsSentReceiverRegistered == true) {
            unregisterReceiver(smsDeliveryReceiver);
        }
        if (isSmsDeliveredReceiverRegistered == true) {
            unregisterReceiver(smsSentReceiver);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        // check if the result is for Call or SMS
        switch (requestCode) {
            case LPG_BOOKING_REQUEST_PERMISSION_CALL_PHONE:
                //check if access was granted :
                switch (resultCode) {
                    case Activity.RESULT_CANCELED:
                        //User has not given permission to use call feature of the phone
                        // Present a dialog box and go back to Home screen
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(homeIntent);
                        break;
                    case Activity.RESULT_OK:
                        //Request for permissions
                        ActivityCompat.requestPermissions(LPGBooking.this, new String[]{Manifest.permission.CALL_PHONE}, LPG_BOOKING_REQUEST_PERMISSION_CALL_PHONE);
                        break;
                }
                break;

            case LPG_BOOKING_REQUEST_PERMISSION_SMS:
                switch (resultCode) {
                    case Activity.RESULT_CANCELED:
//                        ((LPGApplication) getApplication()).LPG_Alert.showDialogHelper(getResources().getString(R.string.lpgbooking_permissioncancellation_request), "OK", null, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                                Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
//                                startActivity(homeIntent);
//                            }
//                        }, null).show(getSupportFragmentManager(), "Permission not granted for SMS");
                        Toast.makeText(getApplicationContext(), "SMS Permission Denied", Toast.LENGTH_LONG).show();
                        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(homeIntent);

                        break;
                    case Activity.RESULT_OK:
                        //Request for SMS permission
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, LPG_BOOKING_REQUEST_PERMISSION_SMS);
                }
        }


    }

    @Override
    public void updateActivityWithLPGDetailsCursor(Cursor c) {
        EditText lpgConnectionName = (EditText) findViewById(R.id.lpgbooking_connectionname_edittext);
        EditText lpgProvider = (EditText) findViewById(R.id.lpgbooking_provider_edittext);
        EditText lpgExpiryDate = (EditText) findViewById(R.id.lpgbooking_expected_expiry_date_edittext);
        ImageView ClickToCall = (ImageView) findViewById(R.id.lpgbooking_call_img);
        ImageView ClickToSMS = (ImageView) findViewById(R.id.lpgbooking_sms_img);
        String connectionName;

        if(c==null){
            //May the connection is deleted, gracefully move to home screen
            Toast.makeText(getApplicationContext(),getApplicationContext().getResources().getString(R.string.connection_detail_missing),Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }

        if (c.moveToFirst()) {
            connectionName = c.getString(c.getColumnIndex(LPG_SQL_ContractClass.LPG_CONNECTION_ROW.CONNECTION_NAME));
            lpgConnectionName.setText(connectionName);
            lpgProvider.setText(c.getString(c.getColumnIndex(LPG_SQL_ContractClass.LPG_CONNECTION_ROW.PROVIDER)));
            LPG_CONNECTION_PHONE_NO = c.getString(c.getColumnIndex(LPG_SQL_ContractClass.LPG_CONNECTION_ROW.AGENCY_IVRS_NUMBER));
            LPG_SMS_REFILL_NO = c.getString(c.getColumnIndex(LPG_SQL_ContractClass.LPG_CONNECTION_ROW.AGENCY_SMS_NUMBER));
            lpgparcelConnectionProvider = c.getString(c.getColumnIndex(LPG_SQL_ContractClass.LPG_CONNECTION_ROW.PROVIDER));

            // Set expected expiry date
            lpgExpiryDate.setText(getExpectedExpiryDate(c));

            //Enable command buttons
            ClickToCall.setEnabled(true);
            ClickToSMS.setEnabled(true);

            //register phone listener
            telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            phoneStateListener = new LPG_PhoneListener(getApplication(), lpgparcelConnectionId, connectionName);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

            //disable sms sending if provider is not present
            if (LPG_Utility.getIndexOfProvider(lpgparcelConnectionProvider) == LPG_Utility.LPG_PROVIDER_NOT_FOUND) {
                ClickToSMS.setEnabled(false);
            }

            //Set telephone call intent for call image
            lpgBookingCallIntent = new Intent(Intent.ACTION_CALL);
            lpgBookingCallIntent.setData(Uri.parse("tel:" + LPG_CONNECTION_PHONE_NO));

        } else {
            // the given cylinder does not exist anymore.
            //disable call buttons
            ClickToCall.setEnabled(false);
            ClickToSMS.setEnabled(false);

            //Update text fields with a notification message for user
            String notificationMessage = getResources().getString(R.string.addlpg_connection_not_found);
            lpgConnectionName.setText(notificationMessage);
            lpgProvider.setText(notificationMessage);
            lpgExpiryDate.setText(notificationMessage);
            LPG_SMS_REFILL_NO = null;

        }


    }

    @Override
    public void updatePrimaryKeyIncrement(String incrementedPrimaryKey) {

    }

    public void sendSMS() {
        Log.v("SMS", "Within Send SMS");
        final ImageView lpgBookingCall = (ImageView) findViewById(R.id.lpgbooking_call_img);
        final ImageView lpgBookingSMS = (ImageView) findViewById(R.id.lpgbooking_sms_img);
        final TextView smsTipTextView = (TextView) findViewById(R.id.lpgbooking_smsnotification);
        final EditText lpgProvider = (EditText) findViewById(R.id.lpgbooking_provider_edittext);


        //the app has permission to send sms
        SmsManager smsManager = SmsManager.getDefault();
        //Get text message to send as SMS
        String SMSTextMessage = LPG_Utility.getSMSTextMessage(lpgProvider.getText().toString());
        PendingIntent smsSentActionPendingIntent = null;
        PendingIntent smsDeliveredActionPendingIntent = null;
        //sent pending intent - will this work?
        try {
            smsSentActionIntent = new Intent(getApplicationContext(), LPGBooking.mSMSBroadcastReceiver.class);
            smsSentActionIntent.setClass(getApplicationContext(), LPGBooking.mSMSBroadcastReceiver.class);
            smsSentActionIntent.putExtra(LPG_Utility.SMS_DELIVERY_MILESTONE, LPG_Utility.SENT_REFILL_SMS);
            smsSentActionPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, smsSentActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            //register sms send broadcast
            smsSentReceiver = new LPGBooking.mSMSBroadcastReceiver(this);
            registerReceiver(smsSentReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_DELIVER_ACTION));
            //isSmsSentReceiverRegistered = true;
        } catch (Exception e) {
            Log.v("SMS", "Exception sent Intent");

        }
        //delivery intent
        try {
            smsDeliveredActionIntent = new Intent(getApplicationContext(), LPGBooking.mSMSBroadcastReceiver.class);
            smsDeliveredActionIntent.setClass(getApplicationContext(), LPGBooking.mSMSBroadcastReceiver.class);
            smsDeliveredActionIntent.putExtra(LPG_Utility.SMS_DELIVERY_MILESTONE, LPG_Utility.DELIVERED_REFILL_SMS);
            smsDeliveredActionPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, smsDeliveredActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            //register sms delivery broadcast
            smsDeliveryReceiver = new LPGBooking.mSMSBroadcastReceiver(this);
            registerReceiver(smsDeliveryReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_DELIVER_ACTION));
            // isSmsDeliveredReceiverRegistered = true;
        } catch (Exception e) {
            Log.v("SMS", "Exception deliver Intent");
        }


        //send SMS

        Log.v("SMS", "SMS No " + LPG_SMS_REFILL_NO);
        if (LPG_SMS_REFILL_NO != null) {
            //enable SMS send notification area
            Log.v("SMS", "Refill no not null");
            ViewGroup smsSendingNotification = (LinearLayout) findViewById(R.id.progressloader);
            smsSendingNotification.setVisibility(View.VISIBLE);
            Log.v("SMS", "Before calling sendTextMessage");
            smsManager.sendTextMessage(LPG_SMS_REFILL_NO, null, SMSTextMessage, null, null);
            //smsManager.sendTextMessage(LPG_SMS_REFILL_NO, null, SMSTextMessage, smsSentActionPendingIntent, smsDeliveredActionPendingIntent);
            smsTipTextView.setText(getString(R.string.lpgbooking_smsnotificationsuccess));
            lpgBookingCall.setEnabled(false);
            lpgBookingSMS.setEnabled(false);

            smsSendingNotification.setVisibility(View.GONE);

        } else {
            Toast.makeText(getApplicationContext(), "SMS Cound not be sent", Toast.LENGTH_SHORT).show();
        }

    }

    public void updateAllConnectionData(Cursor c) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //No call for super(). Bug on API Level > 11.
    }

    private String getExpectedExpiryDate(Cursor c) {
        String lpgparcelLastBookedDate = c.getString(c.getColumnIndex(LPG_SQL_ContractClass.LPG_CONNECTION_ROW.LAST_BOOKED_DATE));
        String lpgparcelExpectedExpiryDays = c.getString(c.getColumnIndex(LPG_SQL_ContractClass.LPG_CONNECTION_ROW.CONNECTION_EXPIRY_DAYS));

        String[] lpgStringArrayForLastBookedDate = lpgparcelLastBookedDate.split("/");
        int lpgLastExpiryDateDay = Integer.valueOf(lpgStringArrayForLastBookedDate[0]);
        int lpgLastExpiryDateMonth = Integer.valueOf(lpgStringArrayForLastBookedDate[1]);
        int lpgLastExpiryDateYear = Integer.valueOf(lpgStringArrayForLastBookedDate[2]);

        GregorianCalendar lpgCalendarLastBookedDate = new GregorianCalendar();
        lpgCalendarLastBookedDate.set(Calendar.DATE, lpgLastExpiryDateDay);
        lpgCalendarLastBookedDate.set(Calendar.MONTH, lpgLastExpiryDateMonth - 1);
        lpgCalendarLastBookedDate.set(Calendar.YEAR, lpgLastExpiryDateYear);
        lpgCalendarLastBookedDate.add(Calendar.DATE, Integer.valueOf(lpgparcelExpectedExpiryDays));
        //set this value to expected expiry date field
        // Get the String value of the Date
        String lpgStringExpiryDate = Integer.toString(lpgCalendarLastBookedDate.get(Calendar.DATE)) + "/" +
                Integer.toString(lpgCalendarLastBookedDate.get(Calendar.MONTH) + 1) + "/" +
                Integer.toString(lpgCalendarLastBookedDate.get(Calendar.YEAR));

        return lpgStringExpiryDate;
    }

    public boolean showInterStitialAd() {
        boolean flagShowInterstitial = ifWeCanShowInterstitialAdNow();
        boolean showAd = mInterstitialAd.isLoaded() && flagShowInterstitial;
        Log.v("Ads", "Interstial Ad loaded : " + mInterstitialAd.isLoaded());
        Log.v("Ads", "Interstitial can we show : " + flagShowInterstitial);
        Log.v("Ads", " Interstitial showAd " + showAd);

        if (showAd) {
            Log.v("Ads", "Showing Interstital Ad");
            mInterstitialAd.show();

        }

        return showAd;

    }

    private void loadInterstitialAd() {
        mInterstitialAd = new InterstitialAd(getApplicationContext());
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.AdMob_InterstitialAd_FAQs));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.v("Ads", " Failed to Load Interstitial error " + errorCode);
            }

            @Override
            public void onAdLoaded() {
                Log.v("Ads", " Interstitial Ad Loaded ");
            }

            @Override
            public void onAdClosed() {
                Intent homeActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(homeActivity);
            }

            @Override
            public void onAdLeftApplication() {

                if (mFireBaseAnalytics != null) {
                    // Code to be executed when the user has left the app.
                    Bundle bundleInterstitialAd = new Bundle();
                    bundleInterstitialAd.putString(LPG_Utility.PARAMETER_ANALYTICS_EVENT_PARAM, LPG_Utility.CLICK_INTERSTITIAL);
                    bundleInterstitialAd.putString(LPG_Utility.PARAMETER_ANALYTICS_ACTIVITY_PARAM, "AddLPGConnection");
                    mFireBaseAnalytics.logEvent(LPG_Utility.CLICK_INTERSTITIAL, bundleInterstitialAd);
                }
            }


        });
        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice(getResources().getString(R.string.AdMob_TestDevice)).build());
    }

    @Override
    public void onResume(){
        super.onResume();

        //provide status to user
        switch (mPermissionStatus){
            case  PERMISSION_DENIED:

                Alert.showDialogHelper(getResources().getString(R.string.lpgbooking_callpermissiondialog_title), "OK", null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(homeIntent);
                    }
                }, null).show(getSupportFragmentManager(), "Cancel permission");
                break;
            default:
                break;
        }
    }

    public static class mSMSBroadcastReceiver extends BroadcastReceiver {
        private Activity lpgBooking;
        private Activity mContext;

        public mSMSBroadcastReceiver()
        {
            super();
        }
        public  mSMSBroadcastReceiver(Activity context) {
            super();
            mContext = context;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //get nature of intent from extras value
            int intentAction = intent.getIntExtra(LPG_Utility.SMS_DELIVERY_MILESTONE, 1);
            TextView progressText = (TextView) mContext.findViewById(R.id.progresstext);
            switch (intentAction) {
                case (LPG_Utility.SENT_REFILL_SMS):
                    progressText.setText(LPG_Utility.PROGRESS_SMS_SENT);
                    break;
                case (LPG_Utility.DELIVERED_REFILL_SMS):
                    progressText.setText(LPG_Utility.PROGRESS_SMS_DELIVERED);
                    break;
                default:
                    progressText.setText(LPG_Utility.PROGRESS_START);

            }


        }



    }

}
