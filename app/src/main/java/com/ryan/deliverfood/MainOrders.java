package com.ryan.deliverfood;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class MainOrders extends Activity {

    private final Context theC = this;
    private LinearLayout myClaimedOrdersLayout, unclaimedOrdersLayout;
    private SharedPreferences thePrefs;
    private SharedPreferences.Editor theEd;
    private String driverUDID;
    private Toast toastMessage;
    private LayoutInflater myClaimedInflater, unclaimedInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_orders);

        if(!haveNetworkConnection()) {
            AlertDialog.Builder theBuilder = new AlertDialog.Builder(MainOrders.this);
            theBuilder.setTitle("No Internet Connection Detected");
            theBuilder.setMessage("Sorry, you must have a working Internet Connection to use " +
                    "this app");
            theBuilder.setPositiveButton("Take me to Wifi Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    finish();
                }
            });
            theBuilder.show();
        }

        try {
            Parse.initialize(getApplicationContext(), "H7vwuy3u4duhsYm9MyVMi0f1riIs6aixBLVD551V",
                    "P16oPFyMpAaAsWBUC41XkUCmSkVIS8TA0fUIavkM");
            PushService.setDefaultPushCallback(getApplicationContext(), MainOrders.class);
            ParseInstallation.getCurrentInstallation().saveInBackground();
        }
        catch (Exception e) {
        }

        this.thePrefs = getApplicationContext().getSharedPreferences("com.ryan.deliverfood", Context.MODE_PRIVATE);
        this.theEd = thePrefs.edit();

        if(isFirstUse()) {
            PushService.subscribe(getApplicationContext(), "Drivers", MainOrders.class);
            ParseInstallation.getCurrentInstallation().saveInBackground();
        }

        getActionBar().setTitle("All Orders");

        this.toastMessage = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        this.unclaimedOrdersLayout = (LinearLayout) findViewById(R.id.unclaimedOrders);
        this.myClaimedOrdersLayout = (LinearLayout) findViewById(R.id.myClaimedOrders);
        this.driverUDID = Secure.getString(theC.getContentResolver(), Secure.ANDROID_ID);
        this.unclaimedInflater = (LayoutInflater)
                theC.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.myClaimedInflater = (LayoutInflater)
                theC.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        new GetLiveOrdersAsync().execute();
        new GetClaimedOrdersAsync().execute();
    }

    /** Updates display with an array of Orders it gets from the server */
    private class GetLiveOrdersAsync extends AsyncTask<Void, Void, Order[]> {

        private int problem = 0; //0 --> orders, 1 --> no orders, 2 --> Something went wrong
        @Override
        public Order[] doInBackground(Void... params) {
            final HttpClient theClient = new DefaultHttpClient();
            final HttpPost thePost = new HttpPost(
                    String.format("http://barsoftapps.com/scripts/PrincetonFoodDelivery.py?" +
                            "driver=%s&getAllOrders=%s", Uri.encode("1"), Uri.encode("1")));
            try {
                final HttpResponse theResponse = theClient.execute(thePost);
                final String responseString = EntityUtils.toString(theResponse.getEntity());

                if(responseString.contains("~")) {
                    problem = 0;
                    return getOrdersResponse(responseString, false);
                }
                else {
                    problem = 1;
                }
            }
            catch (Exception e) {
                problem = 2;
            }
            return null;
        }

        @Override
        public void onPostExecute(final Order[] theOrders) {
            unclaimedOrdersLayout.removeAllViews();
            if(theOrders == null) {
                if(problem == 1) {
                    makeToast("No unclaimed orders");
                    log("No unclaimed orders");
                    final TextView noUnclaimed = new TextView(theC);
                    noUnclaimed.setText("None");
                    noUnclaimed.setTextSize(16);
                    noUnclaimed.setTextColor(Color.RED);
                    unclaimedOrdersLayout.addView(noUnclaimed);
                }
                else if(problem == 2) {
                    makeToast("Sorry, something went wrong");
                    log("Something went wrong");
                }
                return;
            }
            for(Order order : theOrders) {
                unclaimedOrdersLayout.addView(getUnclaimedOrderTextView(order));
            }
        }
    }

    private boolean isFirstUse() {
        boolean isFirst = thePrefs.getBoolean("isFirst", true);
        theEd.putBoolean("isFirst", false);
        return isFirst;
    }

    private class GetClaimedOrdersAsync extends AsyncTask<Void, Void, Order[]> {

        private int problem = 0; //0 --> orders, 1 --> no orders, 2 --> Something went wrong

        @Override
        public Order[] doInBackground(Void... params) {

            final String getOrdersString = String.format(
                    "http://barsoftapps.com/scripts/PrincetonFoodDelivery.py?" +
                            "driver=%s&getMyOrders=%s&dudid=%s",
                    Uri.encode("1"), Uri.encode("1"), Uri.encode(driverUDID));

            final HttpClient myClient = new DefaultHttpClient();
            final HttpPost toPost = new HttpPost(getOrdersString);

            try {
                final HttpResponse theResponse = myClient.execute(toPost);
                final String responseString = EntityUtils.toString(theResponse.getEntity());

                if(responseString.contains("~")) {
                    return getOrdersResponse(responseString, true);
                }
                else {
                    problem = 1;
                }
            }
            catch (Exception e) {
                problem = 2;
            }
            return null;
        }

        @Override
        public void onPostExecute(final Order[] theOrders) {
            myClaimedOrdersLayout.removeAllViews();
            if(theOrders == null) {
                if (problem == 1) {
                    makeToast("You have no orders");
                    log("No orders to deliver");
                    final TextView noClaimed = new TextView(theC);
                    noClaimed.setText("None");
                    noClaimed.setTextSize(16);
                    noClaimed.setTextColor(Color.RED);
                    myClaimedOrdersLayout.addView(noClaimed);
                } else if (problem == 2) {
                    makeToast("Sorry, something went wrong");
                    log("Something wrong");
                }
                return;
            }
            for(Order order : theOrders) {
                myClaimedOrdersLayout.addView(getViewOrderTextView(order));
            }
        }
    }

    private Order[] getOrdersResponse(final String responseString, final boolean isClaimed) {
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
            final String orderStatus = orderDeets[9];
            //final String deliveryTime = orderDeets[8];

            theOrders[i] = new Order(clientName, clientPhone, clientAddress, restaurantName,
                    clientUDID, orderItems, orderID, orderCost,
                    String.valueOf(System.currentTimeMillis()), orderStatus);
            if(isClaimed) {
                theOrders[i].claim();
            }
        }
        return theOrders;
    }

    private class ViewOrderListener implements View.OnClickListener {
        private final Order theOrder;

        public ViewOrderListener(final Order theOrder) {
            this.theOrder = theOrder;
        }

        @Override
        public void onClick(final View view) {
            final String JSON = theOrder.toJSONObject().toString();
            final Intent viewOrder = new Intent(MainOrders.this, ViewOrderActivity.class);
            viewOrder.putExtra("Order", JSON);
            startActivity(viewOrder);
            finish();
        }
    }

    private class ClaimOrderListener implements View.OnClickListener {
        private final Order theOrder;

        public ClaimOrderListener(final Order theOrder) {
            this.theOrder = theOrder;
        }

        @Override
        public void onClick(final View view) {
            if(theOrder.isClaimed() || theOrder.getStatus() != Order.STATUS.UNCLAIMED) {
                return;
            }

            final EditText numMinutes = new EditText(theC);
            numMinutes.setHint("Estimated delivery time");
            numMinutes.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

            final AlertDialog.Builder claimOrder = new AlertDialog.Builder(theC);
            claimOrder.setTitle("Claim Order");
            claimOrder.setView(numMinutes);
            claimOrder.setMessage("Claim order for " + theOrder.getOrderCost() +
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
            claimOrder.setNeutralButton("View on Maps", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String geoLocation = "geo:" +
                            getResources().getString(R.string.princetonLatitude) + "," +
                            getResources().getString(R.string.princetonLongitude) + "?q=" +
                            theOrder.getRestaurantName().replaceAll(" ", "+");

                    final Intent openMaps = new Intent(Intent.ACTION_VIEW);
                    openMaps.setData(Uri.parse(geoLocation));
                    startActivity(openMaps);
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
                log("RESPONSE: " + response);
                if(response.contains("ACK")) {
                    return "0";
                }
                else if(response.contains("KCA")) {
                    return "1";
                }
            }
            catch (Exception e) {
            }
            return "2";
        }

        @Override
        public void onPostExecute(final String result) {
            new GetLiveOrdersAsync().execute();
            new GetClaimedOrdersAsync().execute();

            if(result.contains("0")) {
                makeToast("Order successfully claimed");
                final Intent viewOrder = new Intent(MainOrders.this, ViewOrderActivity.class);
                theOrder.setOrderStatus("1");
                viewOrder.putExtra("Order", theOrder.toJSONObject().toString());
                startActivity(viewOrder);
                finish();
            }
            else if(result.contains("1")) {
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

    public View getUnclaimedOrderTextView(final Order theOrder) {
        final View theView = unclaimedInflater.inflate(R.layout.unclaimed_order_layout, null);

        final TextView restaurantTV = (TextView) theView.findViewById(R.id.restaurantName);
        final TextView orderCost = (TextView) theView.findViewById(R.id.orderCost);
        final TextView orderItems = (TextView) theView.findViewById(R.id.orderItems);
        final TextView clientTV = (TextView) theView.findViewById(R.id.clientAddress);

        restaurantTV.setText("Restaurant: " + theOrder.getRestaurantName());
        orderCost.setText("Cost: $" + theOrder.getOrderCost().replace("$", ""));
        orderItems.setText("Number of items: " + theOrder.getMyOrder().length);
        clientTV.setText("Address: " + theOrder.getMyAddress());

        theView.setOnClickListener(new ClaimOrderListener(theOrder));

        theView.setPadding(0, 36, 0, 0);
        return theView;
    }

    public View getViewOrderTextView(final Order theOrder) {
        final View theView = unclaimedInflater.inflate(R.layout.claimed_order_layout, null);

        final TextView restaurantTV = (TextView) theView.findViewById(R.id.restaurantName);
        final TextView orderStatus = (TextView) theView.findViewById(R.id.orderStatus);
        final TextView estimatedTime = (TextView) theView.findViewById(R.id.estimatedTime);
        final TextView clientTV = (TextView) theView.findViewById(R.id.clientAddress);

        restaurantTV.setText("Restaurant: " + theOrder.getRestaurantName());
        orderStatus.setText("Status: " + theOrder.getStatus());
        estimatedTime.setText("Estimated time: " + theOrder.getDeliveryTime());
        clientTV.setText("Address: " + theOrder.getMyAddress());
        theView.setOnClickListener(new ViewOrderListener(theOrder));

        if(theOrder.getDeliveryTime().length() < 1) {
            estimatedTime.setVisibility(View.GONE);
        }
        theView.setPadding(0, 36, 0, 0);
        return theView;
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    /** Prints log statements */
    private void log(final String message) {
        Log.e("com.ryan.deliverfood", message);
    }

    /** Shows toast message */
    private void makeToast(final String text) {
        toastMessage.cancel();
        toastMessage = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toastMessage.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_orders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.refreshItem :
                makeToast("Refreshing...");
                new GetLiveOrdersAsync().execute();
                new GetClaimedOrdersAsync().execute();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
