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
package com.piggate.samples.PiggateLoginApplication;

import android.app.Application;

import com.piggate.sdk.Piggate;
import com.piggate.sdk.PiggateBeacon;

import java.util.ArrayList;

/*
Application class: set the listener of the Piggate object for handle notifications here
*/
public class Application_Notify extends Application {

    Piggate _piggate; //Create an object of the Piggate class

    public void onCreate() {
        super.onCreate();
        _piggate = new Piggate(this); //Initialize the Piggate object
        _piggate.setListenerBeacon(new Piggate.PiggateBeaconCallback() { //Set the beacon listener

            //Handle if the device is not compatible
            @Override
            public void DeviceNotCompatible() {
                //Unused
            }

            //Handle if the bluetooth is not connected
            @Override
            public void BluetoohNotConnect() {
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
                //Do a post notification to the user when a beacon (offer) is nearby
                _piggate.postNotification("New offer!", "Click to see the offer", Activity_Main.class, R.drawable.ic_launcher);
            }

            //Handles the actions where beacons are detected
            @Override
            public void GetBeacons(ArrayList<PiggateBeacon> beacons) {
                //Unused
            }
        });
        _piggate.onStart(); //Handles the onStart method for the Piggate object

    }

}