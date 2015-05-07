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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.piggate.sdk.Piggate;
import com.piggate.sdk.PiggateBeacon;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/*
Service_Notify class: start the service when the application is open
and keep it up when the application is killed or the phone is rebooted
*/
public class Service_Notify extends Service{

    Piggate _piggate; //Create an object of the Piggate class
    static String notificationMsg = "Click to see the offer"; //Default message to show in the notification

    @Override
    public void onCreate() {
        super.onCreate();
        _piggate = new Piggate(this);
        _piggate.setListenerBeacon(new Piggate.PiggateBeaconCallback(){ //Set the beacon listener

            //Handle if the device is not compatible
            @Override
            public void DeviceNotCompatible() {
                //Unused
            }

            //Handle if the bluetooth is not connected
            @Override
            public void BluetoothNotConnect() {
                //Unused
            }

            //Handle the pre - scanning of the beacons
            @Override
            public void PreScanning() {
                //Unused
            }

            //Handle the actions when the listener is ready
            @Override
            public void onReady() {
                //Unused
            }

            //Handle the beacon scanning errors
            @Override
            public void onErrorScanning() {
                //Unused
            }

            //Handle the actions where new beacons are discovered
            @Override
            public void GetNewBeacons(ArrayList<PiggateBeacon> beacons) {
                //Do a post notification to the notification bar when a beacon (an offer) is near with a custom message
                _piggate.postNotification("New offer!", notificationMsg, Activity_Main.class, R.drawable.ic_launcher);
            }

            //Handles the actions where beacons are detected
            @Override
            public void GetBeacons(ArrayList<PiggateBeacon> beacons) {
                //Unused
            }
        });
        _piggate.onStart(); //handles the onStart method of the Piggae object
    }

    //Method for add the flag START_STICKY: when the service is killed for memory problems,
    //this flag allow to restart the service when it is possible
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    //onBind method of the service
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    //onDestroy method of the service
    @Override
    public void onDestroy() {
        _piggate.onDestroy();
    }

    //onTaskRemoved method of service:
    //There's a bug in Android Kitkat 4.4.2 that destroy the service when activity is removed
    //This function handles that bug restarting the service when the application is killed
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartService = new Intent(getApplicationContext(), this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +1000, restartServicePI);
    }
}
