package com.aekan.navya.lpgbooking.utilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by arunramamurthy on 16/04/16.
 */
public class LPG_SQLOpenHelperClass extends SQLiteOpenHelper {
    //create database name and version
    public static final String SQLDBNAME = "LPGCON";
    public static final int VERSION = 3;
    //String for Create table  query statement
    public static final String QUERY = "CREATE TABLE " + LPG_SQL_ContractClass.LPG_CONNECTION_ROW.TABLE_NAME
            + " ( " + LPG_SQL_ContractClass.LPG_CONNECTION_ROW.CONNECTION_NAME + " VARCHAR(350) ,"
                    + LPG_SQL_ContractClass.LPG_CONNECTION_ROW.AGENCY + " VARCHAR(1000) , "
                    + LPG_SQL_ContractClass.LPG_CONNECTION_ROW.PROVIDER + " VARCHAR(1000) , "
            + LPG_SQL_ContractClass.LPG_CONNECTION_ROW.AGENCY_LANDLINE_NUMBER + " INT , "
            + LPG_SQL_ContractClass.LPG_CONNECTION_ROW.AGENCY_IVRS_NUMBER + " INT , "
                    + LPG_SQL_ContractClass.LPG_CONNECTION_ROW.AGENCY_SMS_NUMBER + " INT, "
                    + LPG_SQL_ContractClass.LPG_CONNECTION_ROW.CONNECTION_ID + " VARCHAR(30) , "
                    + LPG_SQL_ContractClass.LPG_CONNECTION_ROW.LAST_BOOKED_DATE + " DATE , "
                    + LPG_SQL_ContractClass.LPG_CONNECTION_ROW._ID + " INT , "
                    + LPG_SQL_ContractClass.LPG_CONNECTION_ROW.CONNECTION_EXPIRY_DAYS + " INT"
            + " );" ;

    public LPG_SQLOpenHelperClass(Context context) {
        super(context, SQLDBNAME, null, VERSION);
    }


   // public LPG_SQLOpenHelperClass()

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create statements to create the databas

    db.execSQL(QUERY);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Nothing needed in this method for now
        db.execSQL("ALTER TABLE " + LPG_SQL_ContractClass.LPG_CONNECTION_ROW.TABLE_NAME + " ADD "
                + LPG_SQL_ContractClass.LPG_CONNECTION_ROW.AGENCY_LANDLINE_NUMBER + " INT"
        );
//        db.execSQL(QUERY);
    }


}
