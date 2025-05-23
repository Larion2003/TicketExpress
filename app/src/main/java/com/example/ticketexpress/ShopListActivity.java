package com.example.ticketexpress;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ShopListActivity extends AppCompatActivity{
    private static final String LOG_TAG = ShopListActivity.class.getName();
    private FirebaseUser user;
    private FirebaseAuth mAuth;

    private RecyclerView mRecyclerView;
    private ArrayList<ShoppingItem> mItemsData;
    private ShoppingItemAdapter mAdapter;

    private FrameLayout redCircle;
    private TextView countTextView;

    private int gridNumber = 1;
    private int cartItems = 0;
    private int queryLimit = 10;

    private boolean viewRow = true;
    private boolean shouldUpdateCart = false;

    private FirebaseFirestore mFireStore;
    private CollectionReference mItems;

    private NotificationHandler mNotificationHandler;
    private AlarmManager mAlarmManager;
    private JobScheduler mJobScheduler;

    private SharedPreferences preferences;

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
        setContentView(R.layout.activity_shop_list);
        mAuth = FirebaseAuth.getInstance();

        //Aktuális felhasználó vizsgálata
        user = FirebaseAuth.getInstance().getCurrentUser();

        //Valid-e a felhasználó
        if (user != null) {
            Log.i(LOG_TAG,"A felhasználó azonosítva!");
        } else {
            Log.i(LOG_TAG,"Azonosítatlan felhasználó!");
            finish();
        }

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,gridNumber));

        mItemsData = new ArrayList<>();

        mAdapter = new ShoppingItemAdapter(this, mItemsData);
        mRecyclerView.setAdapter(mAdapter);


        mFireStore = FirebaseFirestore.getInstance();
        mItems = mFireStore.collection("Items");

        // Adatok kinyerése
        queryData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver,filter);


        //Értesítések kezelése
        mNotificationHandler = new NotificationHandler(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mJobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        //TODO TESZTEK - ALARM_MANAGER, JOB_SCHEDULER
        setAlarmManager();
        setJobScheduler();

        // Kosár kiürítésének ellenőrzése az Intent-ből
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("clear_cart", false)) {
            updateAlertIcon(null); // Kosár kiürítése
        }


        registerReceiver(themeChangeReceiver, new IntentFilter("com.example.ticketexpress.THEME_CHANGED"));

    }

    // Akkumlátor töltöttsége alapján töltjük be az adatokat
    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action==null){
                return;
            }

            switch (action){
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 10;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 5;
                    break;
            }
            queryData();
        }
    };

    // Adatok betöltése FireStore-ból, ha már van collection
    @SuppressLint("NotifyDataSetChanged")
    private void queryData(){
        // Létező adatok törlése - duplikáció elkerülése
        mItemsData.clear();

        // Termék db alapján csökkenő sorrend
        mItems.orderBy("cartedCount", Query.Direction.DESCENDING).limit(queryLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                ShoppingItem item = document.toObject(ShoppingItem.class);
                item.setId(document.getId());
                mItemsData.add(item);
            }

            // Ha nem volt még adatok, akkor feltöltöm
            if (mItemsData.size()==0){
                initializeData();
                queryData();
            }

            // Adatok változásának észlelése
            mAdapter.notifyDataSetChanged();
        });
    }

    //Adatok törlése
    public void deleteItem(ShoppingItem item){
        DocumentReference ref = mItems.document(item._getId());
        ref.delete().addOnSuccessListener(success -> {
            Log.d(LOG_TAG,"Sikeres törlés!" + item._getId());
        })
        .addOnFailureListener(failure -> {
            Toast.makeText(this, "Az Item" + item._getId() + " nem került törlésre!",Toast.LENGTH_LONG).show();
        });

        //TODO TESZT
        queryData();
        mNotificationHandler.cancel();
    }


    private void initializeData() {
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_description);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemsRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        for (int i = 0; i < itemsList.length; i++) {
            mItems.add(new ShoppingItem(
                    itemsList[i],
                    itemsInfo[i],
                    itemsPrice[i],
                    itemsRate.getFloat(i,0),
                    itemsImageResource.getResourceId(i,0),
                    0));
        }
        itemsImageResource.recycle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shop_list_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) { return false; }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG,s);
                mAdapter.getFilter().filter(s);
                return false;

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.log_out_button:
                Log.d(LOG_TAG,"Kijelentkezés...");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.setting_button:
                Log.d(LOG_TAG,"Beállítások...");
                Intent settingsIntent = new Intent(this, ThemeSettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.cart:
                Log.d(LOG_TAG,"Kosárba...");
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                return true;
            case R.id.view_selector:
                Log.d(LOG_TAG,"Nézet...");
                if (viewRow) {
                    changeSpanCount(item,R.drawable.ic_view_grid,1);
                } else {
                    changeSpanCount(item,R.drawable.ic_view_row,2);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();
        redCircle = rootView.findViewById(R.id.view_alert_red_circle);
        countTextView = rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(v -> onOptionsItemSelected(alertMenuItem));

        if (shouldUpdateCart && countTextView != null && redCircle != null) {
            if (cartItems > 0) {
                countTextView.setText(String.valueOf(cartItems));
                redCircle.setVisibility(View.VISIBLE);
            } else {
                countTextView.setText("");
                redCircle.setVisibility(View.GONE);
            }
            shouldUpdateCart = false;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon(ShoppingItem item) {
        if (item == null) {
            // Kosár ürítése esetén nullázom
            cartItems = 0;
            countTextView.setText("");
            redCircle.setVisibility(View.GONE);
        } else {
            // Kosárba történő hozzáadás
            cartItems = (cartItems + 1);
            if (cartItems > 0) {
                countTextView.setText(String.valueOf(cartItems));
            } else {
                countTextView.setText("");
            }
            redCircle.setVisibility((cartItems > 0) ? View.VISIBLE : View.GONE);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
        unregisterReceiver(themeChangeReceiver);
    }

    private void setAlarmManager() {
        long repeatInterval = AlarmManager.INTERVAL_HALF_DAY;
        long triggerTIme = SystemClock.elapsedRealtime() + repeatInterval;

        Intent intent = new Intent(this,AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        mAlarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTIme,
                repeatInterval,
                pendingIntent);

        //mAlarmManager.cancel(pendingIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setJobScheduler(){
        int networkType = JobInfo.NETWORK_TYPE_UNMETERED;
        int hardDeadLine = 5000;

        ComponentName name = new ComponentName(getPackageName(),NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(0,name)
                .setRequiredNetworkType(networkType)
                .setRequiresCharging(true)
                .setOverrideDeadline(hardDeadLine);

        mJobScheduler.schedule(builder.build());
        //mJobScheduler.cancel(0);
    }

}