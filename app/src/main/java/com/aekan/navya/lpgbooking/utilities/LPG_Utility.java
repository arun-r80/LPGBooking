package com.aekan.navya.lpgbooking.utilities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;

import com.aekan.navya.lpgbooking.LPGBooking;
import com.aekan.navya.lpgbooking.LPG_AlarmReceiver;
import com.aekan.navya.lpgbooking.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by aruramam on 1/9/2017.
 */

public class LPG_Utility {
    //Define constants to be used in the project
    public final static String LPG_CONNECTION_ID = "This is used to transfer connectionid across activities";
    public final static String LPG_CONNECTION_NAME = "This string as key for LPG Connection name";
    public final static String LPG_ALARMINTENT_NOTIFICATIONTITLE = "NotificationTitle";
    public final static String LPG_ALARMINTENT_NOTIFICATIONID = "NotificationID";
    public final static String LPG_ALARMINTENT_NOTIFICATIONCONTENT = "Notification content";
    public final static String LPG_ALARMINTENT_FLAG_DIDBOOKINGSTARTFROMNOTIFICATION = "Flag to check if LPG Booking started from notification";
    /*Constants to be defined for usage in permission checks*/
    public final static int PERMISSION_SMS_ALLOWED = 0;
    public final static int PERMISSION_SMS_NOT_ALLOWED = 1;
    public final static int PERMISSION_CALL_ALLOWED = 2;
    public final static int PERMISSION_CALL_NOT_ALLOWED = 3;
    //constants for lpg booking activity
    public final static String PERMISSION_INTIMATION_MESSAGE = "This string identifies the message to be pasted in permission check activity, to display SMS initimation message";
    public final static String PERMISSION_SMS_INTIMATION = "SHOW SMS MESSAGE";
    public final static String PERMISSION_CALL_INTIMATION = "SHOW CALL MESSAGE";
    public final static String PERMISSION_STATUS = "This string defines permission status";
    public final static String PERMISSION_DENIED = "PERMISSION DENIED";
    public final static String PERMISSION_ACCEPTED = "PERMISSION PROVIDED";
    public final static String LPG_PROVIDER_INDANE = "INDANE";
    public final static String LPG_PROVIDER_BHARATH = "BHARATH GAS";
    public final static String LPG_PROVIDER_HP = "HP GAS";
    public final static String LPG_REFILL_TEXT_BHARATH = "LPG";
    public final static String LPG_REFILL_TEXT_INDANE = "IOC";
    public final static String LPG_REFILL_TEXT_HP = "HPGAS";
    public final static String[] LPG_PROVIDERS = {LPG_PROVIDER_INDANE, LPG_PROVIDER_BHARATH, LPG_PROVIDER_HP};
    public final static String[] LPG_PROVIDERS_EXTENDEDLIST = {"Indane", "Indian Oil Corp", "IOC", "Hindustan Petroleum", "HPCL", "HP Gas", "Bharath Gas"};
    public final static int LPG_PROVIDER_NOT_FOUND = 100;
    public final static int SENT_REFILL_SMS = 12;
    public final static int DELIVERED_REFILL_SMS = 14;
    public final static String SMS_DELIVERY_MILESTONE = "This will represent present state of SMS Delivery";
    public final static String PROGRESS_SMS_SENT = "SMS Sent...";
    public final static String PROGRESS_SMS_DELIVERED = "SMS Delivered ...";
    public final static String PROGRESS_START = "Sending SMS....";
    public final static int NAVDRAWER_HEADER = 23;
    public final static int NAVDRAWER_LINEITEM = 12;
    public final static int HASH_CAPACITY = 4;
    public final static float HASH_LOAD_FACTOR = 0.8f;

    public final static int MSG_GETALLCYLINDERS = 0;
    public final static int MSG_INCREMENTPRIMARYKEY = 1243;
    public final static int MSG_POPULATECONNECTION = 234;

    public final static int FINAL_NOTIFICATION_BUFFER = 8;
    public final static int PHONE_BOOKING_REGISTRATION = 34;
    public final static int SMS_BOOKING_REGISTRATIION = 343;
    public final static String REGISTRATION_TYPE = "THIS STRING IDENTIFIES AN ACTIVITY AS PHONE BOOKING OR SMS BOOKING";
    public final static String PROVIDER_NAME_UNDEFINED = "PROVIDER_NAME_UNDEFINED";

    public final static int COMMUNICATE_PHONE = 234;
    public final static int COMMUNICATE_SMS = 9876;

    public final static String PHONELISTENER_FROM_REGISTRATION = "CALL FROM PHONE BOOKING ACTIVITY";

    public final static String EXPN_LIST_QUESTION = "Map for question";
    public final static String EXPN_LIST_ANSWER = "Map Key for answers";

    public final static int LPG_GET_REGULAR_ALARM_NOTIFICATION_DATES = 34;
    public final static int LPG_GET_SNOOZE_ALARM_DATES = 45;

    public static int getDeliveredRefillSms() {
        return DELIVERED_REFILL_SMS;
    }

    public static int getIndexOfProvider(String Provider) {
        for (int i = 0; i < LPG_PROVIDERS.length; ++i) {
            if (LPG_PROVIDERS[i].equals(Provider)) {
                return i;
            }

        }
        return LPG_PROVIDER_NOT_FOUND;
    }

    public static String getLPGProvider(String input) {
        if (input.equals("Indane") || input.equals("Indian Oil Corp") || input.equals("IOC")) {
            return LPG_PROVIDER_INDANE;
        } else {
            if (input.equals("Hindustan Petroleum") || input.equals("HPCL") || input.equals("HP Gas")) {
                return LPG_PROVIDER_HP;
            } else {
                if (input.equals("Bharath Gas")) {
                    return LPG_PROVIDER_BHARATH;
                }

            }   //"Hindustan Petroleum","HPCL","HP Gas"
        }
        return input;
    }

    public static RefillAlarmNotification[] getRefillRemainder(Context applicationContext, String lastBookedDate, String expiryDaysStr, String rowID, String connectionName, int notificationType) {
        RefillAlarmNotification[] alarmTimes = new RefillAlarmNotification[2];
        RefillAlarmNotification[] alarmSnooze = new RefillAlarmNotification[0];
        String[] strDateFields = lastBookedDate.split("/");
        int lpglastbookeddate, lpglastbookedmonth, lpglastbookedyear;
        int pendingIntentRequestCode = Integer.parseInt(rowID) * 10 + 1;
        Calendar sysDate = Calendar.getInstance();

        GregorianCalendar lpgLastBookedGregCalendar;
        int expiryDays = Integer.parseInt(expiryDaysStr);

        switch (notificationType) {

            case LPG_GET_REGULAR_ALARM_NOTIFICATION_DATES:

                if (strDateFields.length != 0) {

                    // From the split string, get the integer values for date, month and year fields
                    // and create a date object with these values Integer.getInteger
                    lpglastbookeddate = Integer.parseInt(strDateFields[1]);
                    lpglastbookedmonth = Integer.parseInt(strDateFields[0]);
                    lpglastbookedyear = Integer.parseInt(strDateFields[2]);
                    //Create the AlarmManager object to set alarms


                    // Create the gregorian calendar based on the last booked date
                    // add half of expected expiry time to this date, to arrive at mid-way expiry date
                    lpgLastBookedGregCalendar = new GregorianCalendar(lpglastbookedyear, lpglastbookedmonth - 1, lpglastbookeddate);
                    GregorianCalendar midwayExpiryDate = lpgLastBookedGregCalendar;
                    midwayExpiryDate.add(Calendar.DATE, Math.round(expiryDays / 2));

                    //compare this midway expiry time with sys date and
                    //create an alarm only if this date is in future

                    if (sysDate.compareTo(midwayExpiryDate) <= 0) {
                        //set the alarm for this date with the notification class
                        // Create an intent and use that to create the pending intent for alarm manager
                        midwayExpiryDate = (GregorianCalendar) sysDate;
                        midwayExpiryDate.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    midwayExpiryDate.set(Calendar.HOUR_OF_DAY, 12);
                    midwayExpiryDate.set(Calendar.MINUTE, 1);

                    //Test notification creation
                    GregorianCalendar newNotification = ((GregorianCalendar) Calendar.getInstance());
                    newNotification.add(Calendar.SECOND, 20);


                    Log.v("Notification1", newNotification.toString());

                    // Create a pending intent request code, which will refer to the alarm being set for this lpg connnection id
                    // Utilising the same pending intent request code will help to replace the existing alarm, if there is a change in
                    // lpg expiry days by any chance. In this application we explicitly do not check to reset the alarm
                    // if the lpg expiry days has been changed. We use inherent behaviour of alarm manager to replace alarms if we use the same pending
                    // intent, which in this case would be identified with pending intent request
                    // Additionally, we will identify alarms used for mid-term expiry reminder with trailing numeral one to int request
                    // This is evident in pendingIntentRequestCode variable initialization below


                    Intent notificationIntent = new Intent(applicationContext, LPG_AlarmReceiver.class);
                    notificationIntent.putExtra(LPG_Utility.LPG_ALARMINTENT_NOTIFICATIONTITLE, "Cylinder is half done");
                    notificationIntent.putExtra(LPG_Utility.LPG_ALARMINTENT_NOTIFICATIONID, rowID);
                    notificationIntent.putExtra(LPG_Utility.LPG_ALARMINTENT_NOTIFICATIONCONTENT, connectionName + " is half empty now. Please click on Book icon for refill booking!!");


                    PendingIntent notificationPendingIntent = PendingIntent.getBroadcast(applicationContext, pendingIntentRequestCode, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                    //Midway alarm notification record - will be put in first index of Alarm Notification
                    //test notification banner for now
                    alarmTimes[0] = new RefillAlarmNotification(notificationPendingIntent, newNotification);
                    //alarmTimes[0] = new RefillAlarmNotification(notificationPendingIntent,midwayExpiryDate);

                    // Set another alarm before final expiry
                    Intent notificationExpiryUltmate = new Intent(applicationContext, LPG_AlarmReceiver.class);
                    int requestCodeNotificationUltimate = Integer.parseInt(rowID) * 10 + 2;
                    notificationExpiryUltmate.putExtra(LPG_ALARMINTENT_NOTIFICATIONTITLE, "You need to refill now");
                    notificationExpiryUltmate.putExtra(LPG_ALARMINTENT_NOTIFICATIONID, rowID);
                    notificationExpiryUltmate.putExtra(LPG_ALARMINTENT_NOTIFICATIONCONTENT, connectionName + " is about to expire. Please click on Book to refill now!!");
                    PendingIntent notificationFinal = PendingIntent.getBroadcast(applicationContext, requestCodeNotificationUltimate, notificationExpiryUltmate, PendingIntent.FLAG_CANCEL_CURRENT);
                    // This notification would be set 8 days before final expiry date
                    // this expiry
                    GregorianCalendar calendarNotificationUltimate = new GregorianCalendar(lpglastbookedyear, lpglastbookedmonth - 1, lpglastbookeddate);
                    calendarNotificationUltimate.add(Calendar.DAY_OF_MONTH, expiryDays - FINAL_NOTIFICATION_BUFFER);

                    // If this alarm notification is in the past,
                    // provide alartm notification the next day
                    if (sysDate.compareTo(calendarNotificationUltimate) <= 0) {
                        calendarNotificationUltimate = (GregorianCalendar) sysDate;
                        calendarNotificationUltimate.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    calendarNotificationUltimate.set(Calendar.HOUR_OF_DAY, 13);
                    calendarNotificationUltimate.set(Calendar.SECOND, 30);

                    //Test notification
                    GregorianCalendar newnotification2 = (GregorianCalendar) Calendar.getInstance();
                    newnotification2.add(Calendar.MINUTE, 3);

                    //test notificaiton firing for now
                    alarmTimes[1] = new RefillAlarmNotification(notificationFinal, newnotification2);
                    //alarmTimes[1] = new RefillAlarmNotification(notificationFinal,calendarNotificationUltimate);
                }
                return alarmTimes;


            case LPG_GET_SNOOZE_ALARM_DATES:

                //set an alarm to notify user in a day's time
                GregorianCalendar nextNotification = (GregorianCalendar) Calendar.getInstance();
                nextNotification.add(Calendar.DATE, 1);
                nextNotification.set(Calendar.HOUR_OF_DAY, 12);
                nextNotification.set(Calendar.MINUTE, 1);

                //set a notification for next day
                Intent intentAlarmForFailureBooking = new Intent(applicationContext, LPGBooking.class);
                intentAlarmForFailureBooking.putExtra(LPG_Utility.LPG_ALARMINTENT_NOTIFICATIONTITLE, applicationContext.getResources().getString(R.string.confirmbooking_alarm_notificationtitle_yes));
                intentAlarmForFailureBooking.putExtra(LPG_Utility.LPG_ALARMINTENT_NOTIFICATIONID, LPG_CONNECTION_ID);
                intentAlarmForFailureBooking.putExtra(LPG_Utility.LPG_ALARMINTENT_NOTIFICATIONCONTENT, LPG_CONNECTION_ID + applicationContext.getResources().getString(R.string.confirmbooking_failure_notificationmessage));


                PendingIntent pendingIntentFailureIntent = PendingIntent.getBroadcast(applicationContext,
                        pendingIntentRequestCode,
                        intentAlarmForFailureBooking,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                alarmSnooze[0] = new RefillAlarmNotification(pendingIntentFailureIntent, nextNotification);
                return alarmSnooze;

        }

        return alarmTimes;

    }

    public static String getSMSTextMessage(String provider) {
        String textMessage = "LPG";
        if (provider.equals(LPG_PROVIDER_BHARATH)) {
            textMessage = LPG_REFILL_TEXT_BHARATH;
        }
        if (provider.equals(LPG_PROVIDER_INDANE)) {
            textMessage = LPG_REFILL_TEXT_INDANE;
        }
        if (provider.equals(LPG_PROVIDER_HP)) {
            textMessage = LPG_REFILL_TEXT_HP;
        }
        return textMessage;

    }

    public static void callOrTextUtility(Context callingActivity, String PhoneNumber, String TextNumber, String Provider, int forCall) {
        //do action based on method purpose - call or sms
        switch (forCall) {
            case COMMUNICATE_PHONE:
                //check if app has permission to make call
                if (ContextCompat.checkSelfPermission(callingActivity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    Intent callIntent = new Intent();
                    callIntent.setAction(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + PhoneNumber));
                    callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    callingActivity.startActivity(callIntent);

                }
                break;
            case COMMUNICATE_SMS:
                SmsManager smsManager = SmsManager.getDefault();
                //send sms message
                smsManager.sendTextMessage(PhoneNumber, null, getSMSTextMessage(Provider), null, null);

                break;

        }

    }

    public static boolean isSMSEnabledForProvider(String provider) {
        boolean result = false;
        for (int i = 0; i < LPG_PROVIDERS.length; ++i) {
            if (LPG_PROVIDERS[i] == provider) {
                result = true;
            }
        }


        return result;
    }

    public static List<Map<String, String>> getExpandableGroupData(Context context) {
        List<Map<String, String>> expandableGroupData = new ArrayList<Map<String, String>>(20);

        String[] resExpandableGroupData = context.getResources().getStringArray(R.array.Questions);
        Log.v("Strength Question ", " = " + resExpandableGroupData.length);
        for (int i = 0; i < resExpandableGroupData.length; ++i) {
            Map<String, String> questionMap = new ArrayMap<String, String>();
            questionMap.put(EXPN_LIST_QUESTION, resExpandableGroupData[i]);

            expandableGroupData.add(i, questionMap);
        }

        //return quesiton list
        return expandableGroupData;

    }

    public static List<List<Map<String, CharSequence>>> getExpandableChildData(Context context) {
        List<List<Map<String, CharSequence>>> resExpandableChildList = new ArrayList<List<Map<String, CharSequence>>>(10);
        //get string array for answers
        String[] resAnswers = context.getResources().getStringArray(R.array.Answers);
        Log.v("Strenght ans", "= " + resAnswers.length);
        for (int i = 0; i < resAnswers.length; ++i) {
            ArrayList<Map<String, CharSequence>> AnswerList = new ArrayList<Map<String, CharSequence>>(10);
            Map<String, CharSequence> answerMap = new ConcurrentHashMap<String, CharSequence>();

            CharSequence answerSpanned = Html.fromHtml(resAnswers[i]);
            answerMap.put(EXPN_LIST_ANSWER, resAnswers[i]);
            AnswerList.add(answerMap);
            resExpandableChildList.add(i, AnswerList);
        }

        return resExpandableChildList;


    }

    public static class RefillAlarmNotification {
        private PendingIntent RefillCylinder;
        private GregorianCalendar AlarmTickerTime;

        public RefillAlarmNotification(PendingIntent pendingIntent, GregorianCalendar alarmTickerTime) {
            RefillCylinder = pendingIntent;
            AlarmTickerTime = alarmTickerTime;
        }

        public PendingIntent getRefillCylinder() {
            return RefillCylinder;
        }

        public GregorianCalendar getGregorialCalendar() {
            return AlarmTickerTime;
        }
    }


}

