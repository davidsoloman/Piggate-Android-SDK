/**
*
*  Copyright 2015-present Piggate
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*
*/
package com.piggate.samples.PiggateLoginService;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.piggate.sdk.Piggate;
import com.piggate.sdk.PiggateBeacon;
import com.piggate.sdk.PiggateOffers;
import com.piggate.sdk.PiggateUser;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/*
Activity for the logged user. Allows to view the nearby beacon offers
*/
public class Activity_Logged extends Activity {

    TextView textEmail; //Displays the logged user email
    Piggate _piggate; //Object of the Piggate class
    private boolean handledClick = false; //Handles click in buttons
    private boolean notification=false; //Handles the notifications
    private View viewContainer;
    private TextView textNotification; //Displays the text of the beacon notification
    private Timer timer; //Timers for the TimerTask for notifications
    private Timer timer2;
    Random rnd=new Random(); //Random used for notifications

    //Method onBackPressed of the activity: handles the back button click listener
    @Override
    public void onBackPressed(){
        _piggate.RequestCloseSession().setListenerRequest(new Piggate.PiggateCallBack() {

            //Send a request for close the active user session
            @Override
            public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }

            //Method onError for JSONObject. Uses the reload() method for the Piggate object
            @Override
            public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                _piggate.reload();
            }

            //Method onComplete for JSONArray
            @Override
            public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {
                //Unused
            }

            //Method onError for JSONArray
            @Override
            public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {
                //Unused
            }
        }).exec();
        super.onBackPressed();
    }

    //Method onPause of the activity
    //Cancel the timers to suspend the updates when activity is inactive
    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
        timer2.cancel();
    }

    //Function for show a notification with a custom message in the activity
    synchronized void showNotification(final String message) {
        if(notification){
           return ;
        }
        notification=true;
        textNotification.setText(message);
        viewContainer.setVisibility(View.VISIBLE);
        viewContainer.setAlpha(1);
        viewContainer.animate().alpha(0.4f).setDuration(5000)
                .withEndAction(new Runnable() {

                    @Override
                    public void run() {
                        notification=false;
                        viewContainer.setVisibility(View.GONE);
                    }
                });
    }

    //Function for show a notification in the user inteface of the activity if there's an offer nearby
    synchronized void showUINotification(final ArrayList<PiggateOffers> offers) {
        if(offers.size()>0){
            runOnUiThread(new Runnable(){
                public void run() {
                    showNotification(offers.get(rnd.nextInt(offers.size())).getDescription());
                }
            });
        }
    }

    //Method onCreate of the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        _piggate=new Piggate(this,null);
        setContentView(R.layout.activity_logged);
        textEmail = (TextView) findViewById(R.id.nametext);
        viewContainer = findViewById(R.id.notificationbar);
        textNotification = (TextView) findViewById(R.id.notification_message);
        Button logout = (Button)findViewById(R.id.buttonlogout);
        final ImageButton return_button = (ImageButton)findViewById(R.id.return1);
        textEmail.setText("Hi, "+PiggateUser.getEmail());

        //Set the onClick listener of the return button
        return_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //Set the onClick listener of the logout button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            synchronized public void onClick(View v) {
                if (!handledClick) {
                    handledClick = true;
                    Intent slideactivity = new Intent(Activity_Logged.this, Activity_Main.class);
                    slideactivity.putExtra("ACTIVITY_MAIN_CREATED_BY_BUTTON_LOGOUT", true);

                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.flip1, R.anim.flip2).toBundle();
                    startActivity(slideactivity, bndlanimation);

                    if (checkInternetConnection() == true) { //If the internet connection is working
                        //Request a session close for the logged user
                        _piggate.RequestCloseSession().setListenerRequest(new Piggate.PiggateCallBack() {

                            //Method onComplete for JSONObject
                            @Override
                            public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            }

                            //Method onError for JSONObject
                            @Override
                            public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                _piggate.reload();
                            }

                            //Method onComplete for JSONArray
                            @Override
                            public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {
                                //Unused
                            }

                            //Method onError for JSONArray
                            @Override
                            public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {
                                //Unused
                            }
                        }).exec();

                    } else { //If internet conexion is not working displays an error message
                        Toast.makeText(getApplicationContext(), "Network is not working", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        if(checkInternetConnection() == true) { //If the internet connection is working
            //Get the notification message
            _piggate.requestGetNotification().setListenerRequest(new Piggate.PiggateCallBack() {
                @Override
                public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                    Service_Notify.notificationMsg = data.optString("message");
                }

                @Override
                public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                    Service_Notify.notificationMsg = "Click to see the offer"; //Default message if there's an error
                }

                @Override
                public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {
                    //Unused
                }

                @Override
                public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {
                    //Unused
                }
            }).exec();
        }
        else{
            Service_Notify.notificationMsg = "Click to see the offer"; //Default message if there's a network error
        }
    }

    //Method onStart of the activity
    @Override
    public void onStart(){
        super.onStart();
    }

    //Method onStop of the activity
    @Override
    public void onStop(){
        super.onStop();
    }

    //Method onDestroy of the activity
    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    //Method onResume of the activity
    @Override
    protected void onResume(){
        super.onResume();
        handledClick = false;
        timer = new Timer();
        timer2 = new Timer();
        timer.schedule(new TimerTask() { //Load offers data from the server using a request
            @Override
            public void run() {
                _piggate.refreshOffers(); //Refresh the offers for every Beacon calling the server every "timer" seconds
            }
        }, 0, 10000);
        timer2.schedule(new TimerTask() { //The offers were shown by timer callback
            @Override
            public void run() {
                showUINotification(_piggate.getOffers()); //Show notifications where are offers nearby
            }
        }, 0, 15000);
    }

    //Method onActivityResult of the activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
    }

    //Check if the internet connection is working
    private boolean checkInternetConnection(){
        Context context=this;
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService (context.CONNECTIVITY_SERVICE);
        if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected())
            return true;
        else
            return false;
    }
}
