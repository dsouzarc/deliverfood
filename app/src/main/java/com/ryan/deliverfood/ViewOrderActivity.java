package com.ryan.deliverfood;
import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.ryan.deliverfood.R;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
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
    }

    private void initializeVariables() {
        this.theOrder = Order.getOrder(getIntent().getExtras().getString("Order"));

        this.orderItemsLayout = (LinearLayout) findViewById(R.id.orderItemsLayout);
        this.orderStatus = (Button) findViewById(R.id.orderStatus);
        this.clientName = (TextView) findViewById(R.id.clientNameTV);
        this.clientAddress = (TextView) findViewById(R.id.clientAddress);
        this.clientPhone = (TextView) findViewById(R.id.clientPhone);
        this.restaurantName = (TextView) findViewById(R.id.restaurantTV);
    }

    private TextView getOrderItem(final String text) {
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
