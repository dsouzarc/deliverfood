package com.ryan.deliverfood;

import android.app.Application;

import android.widget.Toast;

import com.parse.Parse;
import com.parse.PushService;
import com.parse.ParseInstallation;
/**
 * Created by Ryan on 9/18/14.
 */
public class ParseReceiver extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(getApplicationContext(), "H7vwuy3u4duhsYm9MyVMi0f1riIs6aixBLVD551V", "P16oPFyMpAaAsWBUC41XkUCmSkVIS8TA0fUIavkM");
        PushService.setDefaultPushCallback(getApplicationContext(), MainOrders.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
