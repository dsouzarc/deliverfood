package com.ryan.deliverfood;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class ParseReceiver extends Application {

    private static ParseReceiver instance = new ParseReceiver();

    public ParseReceiver() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(getApplicationContext(), "H7vwuy3u4duhsYm9MyVMi0f1riIs6aixBLVD551V",
                "P16oPFyMpAaAsWBUC41XkUCmSkVIS8TA0fUIavkM");
        PushService.setDefaultPushCallback(getApplicationContext(), MainOrders.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
