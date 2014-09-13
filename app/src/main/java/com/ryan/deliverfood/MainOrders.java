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
import android.text.InputType;
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
    private String driverUDID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_orders);
        getActionBar().setTitle("All Orders");
        allLayout = (LinearLayout) findViewById(R.id.allOrdersLL);

        driverUDID = Secure.getString(theC.getContentResolver(), Secure.ANDROID_ID);

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
                    final String orderCost = orderDeets[6];
                    final String clientAddress = orderDeets[7];
                    final String orderStatus = "0";

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
                allLayout.addView(getView(order));
            }
        }
    }

    private class ClaimOrderListener implements View.OnClickListener {
        private final Order theOrder;

        public ClaimOrderListener(final Order theOrder) {
            this.theOrder = theOrder;
        }

        @Override
        public void onClick(final View view) {
            if(theOrder.isClaimed() || !theOrder.getRawStatus().equals("0")) {
                return;
            }

            final EditText numMinutes = new EditText(theC);
            numMinutes.setHint("Estimated delivery time");
            numMinutes.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

            final AlertDialog.Builder claimOrder = new AlertDialog.Builder(theC);
            claimOrder.setTitle("Claim Order");
            claimOrder.setView(numMinutes);
            claimOrder.setMessage("Claim order for $" + theOrder.getOrderCost() +
                        " of " + theOrder.getRestaurantName() + "?");
            claimOrder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            claimOrder.setPositiveButton("Claim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    makeToast("Claiming...");
                    final String claimTime = numMinutes.getText().toString();
                    new ClaimOrder(theOrder, claimTime).execute();
                }
            });

            claimOrder.show();
        }
    }

    private class ClaimOrder extends AsyncTask<Void, Void, String> {

        private final Order theOrder;
        private final String estimatedTime;

        public ClaimOrder(final Order order, final String time) {
            this.theOrder = order;
            this.estimatedTime = time;
        }

        @Override
        public String doInBackground(Void... params) {
            final String claimOrder = String.format(
                    "http://barsoftapps.com/scripts/PrincetonFoodDelivery.py?" +
                            "driver=%s&claimOrder=%s&dudid=%s&estTime=%s&udid=%s&id=%s",
                    Uri.encode("1"), Uri.encode("1"), Uri.encode(driverUDID),
                    Uri.encode(estimatedTime), Uri.encode(theOrder.getUniqueDeviceIdentifier()),
                    Uri.encode(theOrder.getIdNumber()));

            final HttpClient claimClient = new DefaultHttpClient();
            final HttpPost toPost = new HttpPost(claimOrder);

            try {
                final HttpResponse theResponse = claimClient.execute(toPost);
                final String response = EntityUtils.toString(theResponse.getEntity());
                if(response.equals("ACK")) {
                    return "Claimed";
                }
                else {
                    return "Nope";
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                log("Sorry, something went wrong" + e.toString());
                return "Error";
            }
        }

        @Override
        public void onPostExecute(final String result) {
            if(result.equals("Claimed")) {
                makeToast("Order successfully claimed!");
            }
            else if(result.equals("Nope")) {
                makeToast("Sorry, order was already claimed");
            }
            else {
                makeToast("Sorry, something went wrong");
            }
        }
    }
    /** Returns the items in an Order */
    private String[] getItems(String theString) {
        if(!theString.contains("||")) {
            return new String[]{theString};
        }
        theString = theString.replace("||", "|");
        return theString.split("\\|");
    }

    /** Prints log statements */
    private void log(final String message) {
        Log.e("com.ryan.deliverfood", message);
    }

    /** Shows toast message */
    private void makeToast(final String text) {
        Toast.makeText(getApplication(), text, Toast.LENGTH_LONG).show();
    }

    /** Returns a TextView */
    public TextView getView(final Order theOrder) {
        final TextView theView = new TextView(theC);
        theView.setText(theOrder.toString());
        theView.setOnClickListener(new ClaimOrderListener(theOrder));
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
