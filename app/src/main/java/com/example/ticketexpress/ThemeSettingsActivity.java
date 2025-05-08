package com.example.ticketexpress;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ThemeSettingsActivity extends AppCompatActivity {

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
        setContentView(R.layout.theme_settings);

        registerReceiver(themeChangeReceiver, new IntentFilter("com.example.ticketexpress.THEME_CHANGED"));
    }


    public void changeTheme(View view) {
        ThemeHelper.toggleTheme(this);

        Intent intent = new Intent("com.example.ticketexpress.THEME_CHANGED");
        sendBroadcast(intent);

        recreate();
    }

    public void cancel(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(themeChangeReceiver);
    }
}
