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
package com.iternox.piggate.sdk;


import android.util.Log;

import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PiggateBeacon {
    static HashMap<String, PiggateBeacon> _registry = new HashMap<String, PiggateBeacon>();
    private String _proximityUUID;

    private java.lang.String _macAddress;
    private int _major;
    private int _minor;
    private int _measuredPower;
    private int _rssi;
    private Date _lastcall;

    public PiggateBeacon(java.lang.String proximityUUID, String macAddress, int major, int minor, int measuredPower, int rssi) {
        _proximityUUID=proximityUUID;
        _macAddress=macAddress;
        _major=major;
        _minor=minor;
        _measuredPower=measuredPower;
        _rssi=rssi;
        _lastcall=null;
    }
    public static RequestParams getParams(ArrayList<PiggateBeacon> beacons) {
        RequestParams params=new RequestParams();
        Set<Integer> major = new HashSet<>(); // unordered collection
        Set<Integer> minor = new HashSet<>(); // unordered collection
        Log.d("URL",beacons.toString());

        if(beacons.size()>0){
            for(int x=0;x<beacons.size();x++){
                major.add(beacons.get(0).getMajor());
                minor.add(beacons.get(0).getMinor());
            }
            params.put("major",major);
            params.put("minor",minor);
            Log.d("URL",params.toString());
        }
        else{
            return null;
        }
        return params;
    }
    public java.lang.String getProximityUUID() {return _proximityUUID; }

    public java.lang.String getMacAddress() { return _macAddress; }

    public int getMajor() { return _major; }

    public int getMinor() { return _minor; }

    public int getMeasuredPower() { return _measuredPower; }

    public int getRssi() { return _rssi; }

    public Date getLastCall(){
        return _lastcall;
    }
    public void setLastCall(Date date){
        _lastcall=date;
    }

    public String toString() { return this._proximityUUID+" "+this._major+" "+this._minor; }

    public boolean equals(PiggateBeacon piggateBeacon) {  return this._proximityUUID.equals(piggateBeacon._proximityUUID)&&this._major==piggateBeacon._major&&this._minor==piggateBeacon._minor; }

    public static ArrayList<PiggateBeacon> registryBeacon(ArrayList<PiggateBeacon> beacons){

        ArrayList<PiggateBeacon> listBeacon=new ArrayList<PiggateBeacon>();
        Date date=new Date();
        for(int x=0;x<beacons.size();x++){
            PiggateBeacon beacon=_registry.get(beacons.get(x).toString());


            if(beacon!=null){

                if(Piggate.getDateDiff(beacon.getLastCall(),date, TimeUnit.SECONDS)>35){
                    beacon=beacons.get(x);
                    beacon.setLastCall(date);
                    _registry.put(beacon.toString(),beacon);
                    listBeacon.add(beacon);
                }
                else{
                    beacons.get(x).setLastCall(beacon.getLastCall());
                    _registry.put(beacons.get(x).toString(),beacons.get(x));
                }
                
            }
            else{

                beacon=beacons.get(x);
                beacon.setLastCall(date);
                _registry.put(beacon.toString(),beacon);
                listBeacon.add(beacon);
            }
        }
        return listBeacon;
    }
    public int hashCode() {
        int hash=1;
        hash = hash * 17 + (65536-_major);
        hash = hash * 101 + (65536-_minor);
        hash=hash * 13 + ((_proximityUUID == null) ? 0 : _proximityUUID.hashCode());
        return hash;
    }

}
