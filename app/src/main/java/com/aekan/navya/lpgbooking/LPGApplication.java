package com.aekan.navya.lpgbooking;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.aekan.navya.lpgbooking.utilities.LPG_AlertBoxClass;
import com.aekan.navya.lpgbooking.utilities.LPG_SQLOpenHelperClass;

/**
 * Created by arunramamurthy on 17/04/16.
 * This Application is initiated through Android Manifest
 */
public class LPGApplication extends Application {

    //Initialize global state objects and properties
    public SQLiteDatabase LPGDB;
    public LPG_AlertBoxClass LPG_Alert ;

    public void LPG_AlertBoxInstantiate(){

       // LPG_Alert.showDialogHelper("Setting up","Ok","Cancel",new DialogInterface.OnClickListener());
        Log.v("Application", "Initialising LPGApplication");

        LPGDB = (new LPG_SQLOpenHelperClass(getApplicationContext())).getWritableDatabase();
        if (LPGDB == null) {
            Log.v("Application", "DB has not been created");
        }
        LPG_Alert = new LPG_AlertBoxClass();

    }

}
