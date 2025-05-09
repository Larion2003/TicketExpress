package com.example.ticketexpress;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ShoppingItemAdapter adapter;
    private ArrayList<ShoppingItem> cartItems;

    private BroadcastReceiver themeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            recreate();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cartRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartItems = (ArrayList<ShoppingItem>) CartManager.getInstance().getCartItems();

        adapter = new ShoppingItemAdapter(this, cartItems, true);
        recyclerView.setAdapter(adapter);

        TextView totalPriceText = findViewById(R.id.total_price);
        float totalPrice = CartManager.getInstance().getTotalPrice();
        totalPriceText.setText("Összesen: " + String.format("%.0f Ft", totalPrice));

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        Button buyButton = findViewById(R.id.buyButton);
        buyButton.setOnClickListener(view -> {
            Toast.makeText(this, "Rendelés sikeresen leadva!", Toast.LENGTH_SHORT).show();
            new NotificationHandler(this).send("Köszönjük a vásárlást!");
            CartManager.getInstance().clearCart();
            adapter.notifyDataSetChanged();

            totalPriceText.setText("Összesen: 0 Ft");

            Intent intent = new Intent(CartActivity.this, ShopListActivity.class);
            intent.putExtra("clear_cart", true);
            startActivity(intent);


            finish();
        });

        Button backButton = findViewById(R.id.returnButton);
        backButton.setOnClickListener(view -> finish());

        registerReceiver(themeChangeReceiver, new IntentFilter("com.example.ticketexpress.THEME_CHANGED"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(themeChangeReceiver);
    }

}
