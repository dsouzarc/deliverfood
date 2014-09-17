package com.ryan.deliverfood;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class ViewOrderActivity extends Activity {

    private Order theOrder;
    private Context theC;
    private LinearLayout orderItemsLayout;
    private Button orderStatus;
    private TextView clientName, clientAddress, clientPhone, restaurantName;
    private Toast toastMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);
        initializeVariables();
        updateLayout();
    }

    private void updateLayout() {
        orderStatus.setText(theOrder.getStatus());
        clientName.setText(theOrder.getMyName());
        clientPhone.setText(theOrder.getMyNumber());
        clientAddress.setText(theOrder.getMyAddress());
        restaurantName.setText(theOrder.getRestaurantName());

        orderItemsLayout.removeAllViews();
        final String[] items = theOrder.getMyOrder();
        for(String item : items) {
            orderItemsLayout.addView(getOrderItem(item));
        }
    }

    private final View.OnClickListener updateStatus = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(theOrder.statusInt() >= 3) {
                makeToast("Cannot update status any more");
                return;
            }
            theOrder.incrementStatus();
            makeToast("Updating status to: " + theOrder.getStatus());
            new UpdateOrderAsyncTask().execute();
        }
    };

    private class UpdateOrderAsyncTask extends AsyncTask<Void, Void, Void> {

        private boolean problem = true;

        @Override
        public Void doInBackground(Void... params) {
            final String updateStatusString =
                    String.format("http://barsoftapps.com/scripts/PrincetonFoodDelivery.py?" +
                            "driver=%s&updateStatus=%s&status=%s&udid=%s&id=%s",
                            Uri.encode("1"), Uri.encode("1"), Uri.encode(theOrder.getRawStatus()),
                            Uri.encode(theOrder.getUniqueDeviceIdentifier()),
                            Uri.encode(theOrder.getIdNumber()));

            final HttpClient theClient = new DefaultHttpClient();
            final HttpPost thePost = new HttpPost(updateStatusString);

            try {
                final HttpResponse theResponse = theClient.execute(thePost);
                final String response = EntityUtils.toString(theResponse.getEntity());

                problem = !response.contains("ACK");
            }
            catch (Exception e) {
            }
            return null;
        }

        @Override
        public void onPostExecute(Void param) {
            if(problem) {
                theOrder.decrementStatus();
                makeToast("Sorry, something went wrong. Please try again");
            }
            else {
                makeToast("Status updated to: " + theOrder.getStatus());
            }

            orderStatus.setText(theOrder.getStatus());
        }
    }

    private void initializeVariables() {
        this.theC = this;
        this.theOrder = Order.getOrder(getIntent().getExtras().getString("Order"));
        this.toastMessage = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        this.orderItemsLayout = (LinearLayout) findViewById(R.id.orderItemsLayout);
        this.orderStatus = (Button) findViewById(R.id.orderStatus);
        this.clientName = (TextView) findViewById(R.id.clientNameTV);
        this.clientAddress = (TextView) findViewById(R.id.clientAddress);
        this.clientPhone = (TextView) findViewById(R.id.clientPhone);
        this.restaurantName = (TextView) findViewById(R.id.restaurantTV);

        this.orderStatus.setOnClickListener(updateStatus);
    }

    private TextView getOrderItem(final String text) {
        final TextView theView = new TextView(theC);
        theView.setText(text);
        theView.setTextColor(Color.RED);
        return theView;
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
    public void onBackPressed() {
        final Intent toAllOrders = new Intent(ViewOrderActivity.this, MainOrders.class);
        startActivity(toAllOrders);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_order, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
