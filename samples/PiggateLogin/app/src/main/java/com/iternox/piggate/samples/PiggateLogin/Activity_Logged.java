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
package com.iternox.piggate.samples.PiggateLogin;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.iternox.piggate.sdk.Piggate;
import com.iternox.piggate.sdk.PiggateBeacon;
import com.iternox.piggate.sdk.PiggateOffers;
import com.iternox.piggate.sdk.PiggateUser;
import com.iternox.samples.PiggateLogin.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Activity_Logged extends Activity {

    TextView textEmail;
    Piggate _piggate;

    private boolean handledClick = false;
    private boolean notification=false;
    private View viewContainer;
    private TextView textNotification;
    private Timer timer;
    private Timer timer2;
    ArrayList<PiggateBeacon> pending=new ArrayList<PiggateBeacon>();
    private ReadWriteLock rwLock =new ReentrantReadWriteLock();
    private ReadWriteLock rwLock2 =new ReentrantReadWriteLock();
    ArrayList<PiggateOffers> internal_offers=new ArrayList<PiggateOffers>();
    Random rnd=new Random();
    @Override
    public void onBackPressed(){
        _piggate.RequestCloseSession().setListenerRequest(new Piggate.PiggateCallBack() {
            @Override
            public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {

            }

            @Override
            public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                _piggate.reload();
            }

            @Override
            public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {

            }

            @Override
            public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {

            }
        }).exec();
        super.onBackPressed();
    }
    private void registryOffers(ArrayList<PiggateOffers> offers){

        ArrayList< PiggateOffers> _new_registry = new ArrayList<PiggateOffers>();

        Date date=new Date();
        PiggateOffers offer;
        for(int x=0;x<offers.size();x++){
            offer=offers.get(x);
            offer.setLastCall(date);
            int index=internal_offers.indexOf(offer);
            if(index>=0){
                internal_offers.remove(index);
            }
            internal_offers.add(offer);
        }
        for(int x=0;x<internal_offers.size();x++){
            offer=internal_offers.get(x);
            if(Piggate.getDateDiff(offer.getLastCall(),date, TimeUnit.SECONDS)<=35){
                _new_registry.add(offer);
            }
        }
        internal_offers=_new_registry;
    }
    private void registryOffers(JSONArray offers) {
        ArrayList<PiggateOffers> listaOffers = new ArrayList<PiggateOffers>();
        for (int x = 0; x < offers.length(); x++) {
            try {
                listaOffers.add(new PiggateOffers(offers.getJSONObject(x)));
            } catch (JSONException e) {

            }
        }
        registryOffers(listaOffers);
    }


    public ArrayList<PiggateOffers> getOffers() {
        Lock l = rwLock2.readLock();
        ArrayList<PiggateOffers> result = new ArrayList<PiggateOffers>();
        l.lock();
        try {
            result.addAll(this.internal_offers);
        }
        catch(Exception ex) {
        }
        finally {
            l.unlock();
        }
        return result;
    }

    public void addOffers(JSONArray bar){

        Lock l = rwLock2.writeLock();
        l.lock();
        try {
            registryOffers(bar);
        }
        catch(Exception ex) {
        }
        finally {
            l.unlock();
        }
    }



    public ArrayList<PiggateBeacon> getPendingBeacons() {
        Lock l = rwLock.readLock();
        ArrayList<PiggateBeacon> result = new ArrayList<PiggateBeacon>();
        l.lock();
        try {
            result.addAll(this.pending);
            this.pending.clear();
        }
        catch(Exception ex) {
        }
        finally {
            l.unlock();
        }
        return result;
    }

    public void addPendingBeacons(ArrayList<PiggateBeacon> bar){
        Lock l = rwLock.writeLock();
        l.lock();
        try {
            this.pending.addAll(bar);
        }
        catch(Exception ex) {
        }
        finally {
            l.unlock();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Suspend the updates when the activity is inactive.
        timer.cancel();
        timer2.cancel();
    }
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

    synchronized void showUINotification(final ArrayList<PiggateOffers> offers) {
        if(offers.size()>0){
            runOnUiThread(new Runnable(){
                public void run() {
                    showNotification(offers.get(rnd.nextInt(offers.size())).getDescription());
                }
            });

        }
    }
    synchronized public void callOffers(){
        final ArrayList<PiggateBeacon> beacons= getPendingBeacons();
        if(beacons.size()>0) {
            for(int x=0;x<beacons.size();x++) {//Load offers data from the server using a GET request for each Beacon

                _piggate.RequestOffers(beacons.get(x)).setListenerRequest(new Piggate.PiggateCallBack() {
                    @Override
                    public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                        //Unused
                    }

                    @Override
                    public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                        //Unused
                    }

                    @Override
                    public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {
                        addOffers(data);

                    }

                    @Override
                    public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {
                        addPendingBeacons(beacons);
                    }
                }).exec();
            }
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _piggate=new Piggate(this);
        _piggate.setListenerBeacon(new Piggate.PiggateBeaconCallback() {
            @Override
            public void DeviceNotCompatible() {

            }

            @Override
            public void BluetoohNotConnect() {

            }

            @Override
            public void PreScanning() {
            }

            @Override
            public void onReady() {

            }

            @Override
            public void onErrorScanning() {

            }

            @Override
            public void GetNewBeacons(final ArrayList<PiggateBeacon> beacons) {
                addPendingBeacons(beacons);
            }


            @Override
            public void GetBeacons(final ArrayList<PiggateBeacon> beacons) {


            }
        });

        setContentView(R.layout.activity_logged);

        textEmail = (TextView) findViewById(R.id.nametext);
        viewContainer = findViewById(R.id.notificationbar);
        textNotification = (TextView) findViewById(R.id.notification_message);
        Button logout = (Button)findViewById(R.id.buttonlogout);
        final ImageButton return_button = (ImageButton)findViewById(R.id.return1);
        return_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        textEmail.setText("Hi, "+PiggateUser.getEmail());
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

                    if (checkInternetConnection() == true) {
                        _piggate.RequestCloseSession().setListenerRequest(new Piggate.PiggateCallBack() {
                            @Override
                            public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                _piggate.reload();
                            }

                            @Override
                            public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {

                            }

                            @Override
                            public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {

                            }
                        }).exec();


                    } else {
                        Toast.makeText(getApplicationContext(), "Network is not working", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });



    }
    @Override
    public void onStart(){
        super.onStart();
        _piggate.onStart();
    }
    @Override
    public void onStop(){
        _piggate.onStop();
        super.onStop();
    }
    @Override
    public void onDestroy(){
        _piggate.onDestroy();
        super.onDestroy();
    }
    @Override
    protected void onResume(){
        super.onResume();
        handledClick = false;
        timer = new Timer();
        timer2 = new Timer();
        timer.schedule(new TimerTask() { //Load offers data from the server using a request
            @Override
            public void run() {
                callOffers();
            }
        }, 0, 3000);
        timer2.schedule(new TimerTask() { //The offers were shown by timer callback
            @Override
            public void run() {
                showUINotification(getOffers());
            }
        }, 0, 15000);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        _piggate.onActivityResult(requestCode, resultCode, data);
    }

    private boolean checkInternetConnection(){
        Context context=this;
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService (context.CONNECTIVITY_SERVICE);
        if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected())
            return true;
        else
            return false;
    }

}
