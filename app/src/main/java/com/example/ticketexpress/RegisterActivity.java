package com.example.ticketexpress;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREFERENCE_KEY = RegisterActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 1215;


    EditText userNameET;
    EditText userEmailET;
    EditText phoneNumberET;
    Spinner phoneNumberSP;
    EditText addressET;

    RadioGroup accountTypeGroup;

    EditText passwordET;
    EditText passwordAgainET;


    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        int secret_key = getIntent().getIntExtra("SECRET_KEY",0);

        //Ha nem jó a kulcs, akkor eldobom
        if (secret_key!=1215){
            finish();
        }

        //Adatok lekérése
        userNameET= findViewById(R.id.userNameEditText);
        userEmailET = findViewById(R.id.userEmailEditText);
        phoneNumberET = findViewById(R.id.phoneNumberEditText);
        phoneNumberSP = findViewById(R.id.phoneNumberSpinner);
        addressET = findViewById(R.id.addressEditText);
        accountTypeGroup = findViewById(R.id.accountTypeGroup);
        passwordET = findViewById(R.id.passwordEditText);
        passwordAgainET = findViewById(R.id.passwordAgainEditText);
        //Alapvető check: buyer
        accountTypeGroup.check(R.id.buyerRadioButton);


        //Bejelentkezésnél lementem az adatokat és ha még nem regisztrált, akkor le lesz tárolva
        preferences = getSharedPreferences(PREFERENCE_KEY,MODE_PRIVATE);
        String userName = preferences.getString("userName","");
        String password = preferences.getString("password","");

        userNameET.setText(userName);
        passwordET.setText(password);
        passwordAgainET.setText(password);

        //Spinner megvalósítás
        phoneNumberSP.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.phonemodes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        phoneNumberSP.setAdapter(adapter);

        Log.i(LOG_TAG,"onCreate");
    }

    public void register(View view) {
        String userName = userNameET.getText().toString();
        String email = userEmailET.getText().toString();
        String phoneNumber = phoneNumberET.getText().toString();
        String phoneNumberType = phoneNumberSP.getSelectedItem().toString();
        String address = addressET.getText().toString();
        String password = passwordET.getText().toString();
        String passwordAgain = passwordAgainET.getText().toString();

        if (!password.equals(passwordAgain)){
            Log.e(LOG_TAG,"Nem egyeznek meg a jelszavak.");
            return;
        }

        //Radio gombok kezelése
        int checkedId = accountTypeGroup.getCheckedRadioButtonId();
        RadioButton radiobutton = accountTypeGroup.findViewById(checkedId);
        String accountType = radiobutton.getText().toString();


        //Adatok lekérése
        Log.i(LOG_TAG,"Regisztrált: " + userName + ", E-mail: " + email + ", Telefonszám: " + phoneNumber);
        //Log.i(LOG_TAG,address);
        //TODO A regisztrációt még meg kell valósítani backend-ben...
        startShopping();
    }

    public void cancel(View view) {
        finish();
    }

    private void startShopping(/*Regisztrált adatok*/){
        Intent intent = new Intent(this,ShopListActivity.class);
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
        Log.i(LOG_TAG,"onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG,"onResume");
    }


    //TODO AdapterView.OnItemSelectedListener megvalósítása
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = parent.getItemAtPosition(position).toString();
        Log.i(LOG_TAG,selectedItem);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}