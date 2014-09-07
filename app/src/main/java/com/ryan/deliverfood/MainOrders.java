package com.ryan.deliverfood;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.os.AsyncTask;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import android.provider.Settings.Secure;


public class MainOrders extends Activity {

    private final Context theC = this;
    private LinearLayout allLayout;
    private String UDID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_orders);
        getActionBar().setTitle("All Orders");
        allLayout = (LinearLayout) findViewById(R.id.allOrdersLL);

        UDID = Secure.getString(theC.getContentResolver(), Secure.ANDROID_ID);

        new GetLiveOrdersAsync().execute();
    }

    private class GetLiveOrdersAsync extends AsyncTask<Void, Void, Order[]> {
        @Override
        public Order[] doInBackground(Void... params) {
            final HttpClient theClient = new DefaultHttpClient();
            final HttpPost thePost = new HttpPost(
                    String.format("http://barsoftapps.com/scripts/PrincetonFoodDelivery.py?driver=%s&getAllOrders=%s",
                            Uri.encode("1"), Uri.encode("1")));
            try {
                final HttpResponse theResponse = theClient.execute(thePost);
                final String responseString = EntityUtils.toString(theResponse.getEntity());
                final String[] allOrders = responseString.split("\n");
                final Order[] theOrders = new Order[allOrders.length];

                for(int i = 0; i < allOrders.length; i++) {
                    final String[] orderDeets = allOrders[i].split("~");
                    final String orderID = orderDeets[0];
                    final String clientUDID = orderDeets[1];
                    final String clientPhone = orderDeets[2];
                    final String clientName = orderDeets[3];
                    final String restaurantName = orderDeets[4];
                    final String[] orderItems = getItems(orderDeets[5]);
                    log("ITEM: " + orderDeets[5]);
                    final String orderCost = orderDeets[6];
                    final String clientAddress = orderDeets[7];
                    final String orderStatus = "0";

                    for(String o : orderItems) {
                        log("AN ITEM: " + o);
                    }

                    //Name, phone number, my address, restaurant address, UID, myOrder[], order ID, orderCost, time in millis, status

                    theOrders[i] = new Order(clientName, clientPhone, clientAddress, restaurantName,
                            clientUDID, orderItems, orderID, orderCost,
                            String.valueOf(System.currentTimeMillis()), orderStatus);
                }

                return theOrders;
            }
            catch (Exception e) {
                return null;
            }
        }

        @Override
        public void onPostExecute(final Order[] theOrders) {
            if(theOrders == null) {
                makeToast("Sorry, something went wrong");
            }

            for(Order order : theOrders) {
                allLayout.addView(getView(order.toString()));
            }
        }
    }

    private String[] getItems(String theString) {
        if(!theString.contains("||")) {
            return new String[]{theString};
        }
        theString = theString.replace("||", "|");
        return theString.split("\\|");
    }

    private void log(final String message) {
        Log.e("com.ryan.deliverfood", message);
    }

    private void makeToast(final String text) {
        Toast.makeText(getApplication(), text, Toast.LENGTH_LONG).show();
    }

    public TextView getView(final String text) {
        final TextView theView = new TextView(theC);
        theView.setText(text);
        return theView;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_orders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
