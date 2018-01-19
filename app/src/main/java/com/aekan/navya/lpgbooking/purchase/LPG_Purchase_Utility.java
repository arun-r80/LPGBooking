package com.aekan.navya.lpgbooking.purchase;

import android.content.Context;
import android.content.SharedPreferences;

import com.aekan.navya.lpgbooking.R;

/**
 * Created by arunramamurthy on 01/01/18.
 */

public class LPG_Purchase_Utility {
    public static final String PREMIUM_USER_SKU = "lpg_booking_premium_user";
    public static final String USER_STATUS_UNDEFINED = "User status has not been defined yet";
    public static final String USER_STATUS_REGULAR  ="User needs to buy In-App feature";
    public static final String USER_STATUS_PREMIUM = "User is a premium user" ;
    public static final String SKU_STATUS_DEFAULT="Default status for SKU";
    public static boolean isPurchaseChecked = false;
    private static String userStatus = USER_STATUS_UNDEFINED;


    public static void setPremiumUserSku(Context context,String skuID){
        userStatus = USER_STATUS_PREMIUM;
        updateUserStatus(context,skuID);


    }

    private  static void updateUserStatus(Context context,String skuID){
        //update share preference

        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.billing_sharedpref_filename),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getResources().getString(R.string.billing_sharedpref_key),skuID);
        editor.commit();
    }

    public static boolean getSKUStatus(Context context,String skuID){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.billing_sharedpref_filename),Context.MODE_PRIVATE);
        String SKUStatus = sharedPreferences.getString(context.getResources().getString(R.string.billing_sharedpref_key),SKU_STATUS_DEFAULT);

        if(SKUStatus.equals(SKU_STATUS_DEFAULT)){
            return false;
        } else {
            return true;
        }
    }

}
