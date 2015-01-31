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
package com.iternox.piggate.sdk.bridges;

import android.content.Intent;
import android.os.RemoteException;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.iternox.piggate.sdk.Piggate;
import com.iternox.piggate.sdk.PiggateBeacon;

import java.util.ArrayList;
import java.util.List;


public class PiggateEstimoteBridge implements BaseBridge {
    public Region _region;
    private BeaconManager _beaconManager;
    public Piggate.PiggateBeaconCallback _piggatecallback;
    private Piggate _piggate;

    public PiggateEstimoteBridge(Piggate piggate){
        _piggate=piggate;
        String UUID=Piggate.getMetadata(piggate.getApplicationContext(), "com.iternox.piggate.sdk.ApplicationUUID");
        _region = new Region(piggate.getApplicationContext().getPackageName()+"-"+UUID, UUID, null, null);
        _beaconManager = new BeaconManager(piggate.getApplicationContext());
        _beaconManager.setRangingListener(new BeaconManager.RangingListener() {
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
                _piggatecallback.GetBeacons(listBeacon);


            }
        });
    }
    public void onDestroy(){
        if(_piggatecallback!=null)
            _beaconManager.disconnect();
    }
    private void connectToService() {

        _piggatecallback.PreScanning();

        _beaconManager.connect(new BeaconManager.ServiceReadyCallback() {

            @Override
            public void onServiceReady() {
                if(_piggatecallback!=null)
                    _piggatecallback.onReady();
                try {
                    _beaconManager.startRanging(getRegion());
                } catch (RemoteException e) {
                    if(_piggatecallback!=null)
                        _piggatecallback.onErrorScanning();
                }
            }
        });
    }
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
            _piggatecallback.BluetoohNotConnect();
        }
    }
    public void setPiggateCallback(Piggate.PiggateBeaconCallback callback){
        _piggatecallback=callback;
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
    public void onStop() {
        try {
            _beaconManager.stopRanging(getRegion());
        } catch (RemoteException e) {

        }
    }
    public Region getRegion(){
        return _region;
    }


}
