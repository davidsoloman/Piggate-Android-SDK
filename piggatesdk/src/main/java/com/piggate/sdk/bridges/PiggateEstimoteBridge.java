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
package com.piggate.sdk.bridges;

import android.content.Intent;
import android.os.RemoteException;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.piggate.sdk.Piggate;
import com.piggate.sdk.PiggateBeacon;

import java.util.ArrayList;
import java.util.List;

/*
PiggateEstimoteBridge class: bridge with the Estimote SDK
----------------------------------------------------------
Handle all the essential methods for the Estimote SDK
*/
public class PiggateEstimoteBridge implements BaseBridge {

    public Region _region; //Region of the beacon
    private BeaconManager _beaconManager; //Beacon manager
    public Piggate.PiggateBeaconCallback _piggatecallback; //CallBack for the Piggate class
    private Piggate _piggate; //Piggate object

    //Public constructor of PiggateEstimoteBridge
    public PiggateEstimoteBridge(Piggate piggate){
        _piggate=piggate;
        String UUID=Piggate.getMetadata(piggate.getApplicationContext(), "com.piggate.sdk.ApplicationUUID"); //Set the UUID
        _region = new Region(piggate.getApplicationContext().getPackageName()+"-"+UUID, UUID, null, null); //Set the region
        _beaconManager = new BeaconManager(piggate.getApplicationContext()); //Set the beacon manager
        _beaconManager.setRangingListener(new BeaconManager.RangingListener() { //Set the ranging listener
            //Handles the actions when the beacon are discovered
            //Put the beacons into a list and send them into the piggatecallback object
            @Override
            synchronized public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                final ArrayList<PiggateBeacon> listBeacon=new ArrayList<PiggateBeacon>();
                final ArrayList<PiggateBeacon> listBeacon2;
                Beacon beacon;
                for(int x=0;x<beacons.size();x++){
                    beacon=beacons.get(x);
                    listBeacon.add(new PiggateBeacon(beacon.getProximityUUID(),beacon.getMacAddress(),beacon.getMajor(),beacon.getMinor(),beacon.getMeasuredPower(),beacon.getRssi()));
                }
                listBeacon2=PiggateBeacon.registryBeacon(listBeacon);
                if(listBeacon2.size()>0)
                    _piggatecallback.GetNewBeacons(listBeacon2);
                PiggateBeacon.addPendingBeacons(listBeacon);
                _piggatecallback.GetBeacons(listBeacon);
            }
        });
    }

    //onDestroy method of the class
    public void onDestroy(){
        if(_piggatecallback!=null)
            _beaconManager.disconnect();
    }

    //Connect the beaconManager to the ServiceReadyCallback
    private void connectToService() {

        _piggatecallback.PreScanning();

        _beaconManager.connect(new BeaconManager.ServiceReadyCallback() {

            @Override
            public void onServiceReady() {
                if(_piggatecallback!=null)
                    _piggatecallback.onReady(); //if the service is ready call the onReady method of _piggatecallback
                try {
                    _beaconManager.startRanging(getRegion()); //Start ranging beacons
                } catch (RemoteException e) {
                    if(_piggatecallback!=null)
                        _piggatecallback.onErrorScanning(); //if there is an error call the onError method of _piggatecallback
                }
            }
        });
    }

    //onStart method of the class
    //Handles if the bluetooth is enabled and if the device is compatible
    public void onStart(){
        if (!_beaconManager.hasBluetooth()) {
            if(_piggatecallback!=null)
            _piggatecallback.DeviceNotCompatible();
            return ;
        }

        // If Bluetooth is not enabled, let user enable it.
        if (_beaconManager.isBluetoothEnabled()) {
            connectToService();
        } else if(_piggatecallback!=null) {
             connectToService();
            _piggatecallback.BluetoothNotConnect();
        }
    }

    //Set the PiggateCallback object
    public void setPiggateCallback(Piggate.PiggateBeaconCallback callback){
        _piggatecallback=callback;
    }

    //onActivityResult method of the class
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Unused
    }

    //onStop method of the class
    public void onStop() {
        try {
            _beaconManager.stopRanging(getRegion());
        } catch (RemoteException e) {

        }
    }

    //Get the region
    public Region getRegion(){
        return _region;
    }
}
