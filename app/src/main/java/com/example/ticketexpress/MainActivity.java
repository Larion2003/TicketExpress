package com.example.ticketexpress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>
{
    //Classname lekérése
    private static final String LOG_TAG = MainActivity.class.getName();
    //Package név lekérése
    private static final String PREFERENCE_KEY = MainActivity.class.getPackage().toString();
    //Ellenőrző kulcs
    private static final int SECRET_KEY = 1215;

    EditText userNameET;
    EditText passwordET;

    private SharedPreferences preferences;
    private FirebaseAuth FBAuth;

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
        setContentView(R.layout.activity_main);

        //Adatok lekérése
        userNameET= findViewById(R.id.editTextUserName);
        passwordET = findViewById(R.id.editTextPassword);

        preferences = getSharedPreferences(PREFERENCE_KEY,MODE_PRIVATE);
        FBAuth = FirebaseAuth.getInstance();

        //Random async task tesztelése
        //Button button = findViewById(R.id.guestLoginButton);
        //new RandomAsyncTask(button).execute();

        //Random async loader tesztelése
        getSupportLoaderManager().restartLoader(0,null,this);

        Log.i(LOG_TAG,"onCreate");

        registerReceiver(themeChangeReceiver, new IntentFilter("com.example.ticketexpress.THEME_CHANGED"));
    }

    public void login(View view) {
        String userName = userNameET.getText().toString();
        String password = passwordET.getText().toString();

        //Teszt
        //Log.i(LOG_TAG,"Bejelentkezett: " + userName + ", jelszó: " + password);

        FBAuth.signInWithEmailAndPassword(userName,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(LOG_TAG,"A felhasználó sikeresen belépett!");
                    startShopping();
                }  else {
                    Log.d(LOG_TAG,"A felhasználó belépése elutasítva!");
                    Toast.makeText(MainActivity.this,"A felhasználó belépése elutasítva!" + task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startShopping(){
        Intent intent = new Intent(this,ShopListActivity.class);
        startActivity(intent);
    }


    public void loginAsGuest(View view) {
        //TODO login
        FBAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(LOG_TAG,"Az anoním felhasználó sikeresen belépett!");
                    startShopping();
                }  else {
                    Log.d(LOG_TAG,"Az anoním felhasználó belépése elutasítva!");
                    Toast.makeText(MainActivity.this,"A felhasználó belépése elutasítva!" + task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
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
        unregisterReceiver(themeChangeReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();

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


    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        return new RandomAsyncLoader(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        Button button = findViewById(R.id.guestLoginButton);
        button.setText(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {}
}