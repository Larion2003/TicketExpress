package com.example.ticketexpress;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    //Classname lekérése
    private static final String LOG_TAG = MainActivity.class.getName();
    //Package név lekérése
    private static final String PREFERENCE_KEY = MainActivity.class.getPackage().toString();
    //Ellenőrző kulcs
    private static final int SECRET_KEY = 1215;

    EditText userNameET;
    EditText passwordET;

    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Adatok lekérése
        userNameET= findViewById(R.id.editTextUserName);
        passwordET = findViewById(R.id.editTextPassword);

        preferences = getSharedPreferences(PREFERENCE_KEY,MODE_PRIVATE);


        Log.i(LOG_TAG,"onCreate");
    }

    public void login(View view) {
        String userName = userNameET.getText().toString();
        String password = passwordET.getText().toString();

        //Teszt
        Log.i(LOG_TAG,"Bejelentkezett: " + userName + ", jelszó: " + password);
    }

    public void register(View view) {
        Intent intent = new Intent(this,RegisterActivity.class);
        intent.putExtra("SECRET_KEY",SECRET_KEY);
        startActivity(intent);
    }

    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG,"onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG,"onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG,"onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG,"onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Adatok letárolása
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userName", userNameET.getText().toString());
        editor.putString("password", passwordET.getText().toString());
        editor.apply();

        Log.i(LOG_TAG,"onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG,"onResume");
    }
}