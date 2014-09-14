package com.ryan.deliverfood;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.os.AsyncTask;
import android.widget.Button;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewOrderActivity extends Activity {

    private Order theOrder;
    private Context theC;
    private LinearLayout orderItemsLayout;
    private Button orderStatus;
    private TextView clientName, clientAddress, clientPhone, restaurantName;

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

        }
    };

    private class UpdateOrderAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        public Void doInBackground(Void... params) {
            return null;
        }

    }


    private void initializeVariables() {
        this.theC = this;
        this.theOrder = Order.getOrder(getIntent().getExtras().getString("Order"));

        this.orderItemsLayout = (LinearLayout) findViewById(R.id.orderItemsLayout);
        this.orderStatus = (Button) findViewById(R.id.orderStatus);
        this.clientName = (TextView) findViewById(R.id.clientNameTV);
        this.clientAddress = (TextView) findViewById(R.id.clientAddress);
        this.clientPhone = (TextView) findViewById(R.id.clientPhone);
        this.restaurantName = (TextView) findViewById(R.id.restaurantTV);
    }

    private TextView getOrderItem(final String text) {
        final TextView theView = new TextView(theC);
        theView.setText(text);
        theView.setTextColor(Color.RED);
        return theView;
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
