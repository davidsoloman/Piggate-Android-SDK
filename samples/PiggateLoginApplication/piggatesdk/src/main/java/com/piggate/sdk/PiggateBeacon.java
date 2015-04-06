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
package com.piggate.sdk;

import android.util.Log;

import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
PiggateBeacon class: the beacon class
-------------------------------------------------
This class contains all the attributes of a beacon and all the essential methods
*/
public class PiggateBeacon {

    static HashMap<String, PiggateBeacon> _registry = new HashMap<String, PiggateBeacon>();
    private String _proximityUUID; //UUID of the beacon
    private java.lang.String _macAddress; //Mac address of the beacon
    private int _major; //Major ID of the beacon
    private int _minor; //Minor ID of the beacon
    private int _measuredPower; //Battery level of the beacon
    private int _rssi; //The received signal strength indicator
    private Date _lastcall; //Date of the last time the beacon has been seen
    private Date _firstcall; //Date of the first time the beacon was discovered
    private static ReadWriteLock rwLock = new ReentrantReadWriteLock(); //ReadWriteLock for synchronize
    static ArrayList<PiggateBeacon> pending = new ArrayList<PiggateBeacon>(); //Array of pending beacons

    //Public constructor of the PiggateBeacon class
    public PiggateBeacon(java.lang.String proximityUUID, String macAddress, int major, int minor, int measuredPower, int rssi) {
        _proximityUUID=proximityUUID;
        _macAddress=macAddress;
        _major=major;
        _minor=minor;
        _measuredPower=measuredPower;
        _rssi=rssi;
        _lastcall=null;
        _firstcall=null;
    }

    //Request function to get the params of the beacon
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

    //Getters and settlers of the PiggateBeacon attributes

    public java.lang.String getProximityUUID() {return _proximityUUID; }

    public java.lang.String getMacAddress() { return _macAddress; }

    public int getMajor() { return _major; }

    public int getMinor() { return _minor; }

    public int getMeasuredPower() { return _measuredPower; }

    public int getRssi() { return _rssi; }

    public Date getLastCall(){
        return _lastcall;
    }

    public Date getFirstCall(){
        return _firstcall;
    }

    public void setLastCall(Date date){
        _lastcall=date;
    }

    public void setFirstCall(Date date){
        _firstcall=date;
    }
    //Convert the parameters of a beacon to String
    public String toString() { return this._proximityUUID+" "+this._major+" "+this._minor; }

    //Return if a beacon is equal to another
    public boolean equals(PiggateBeacon piggateBeacon) {  return this._proximityUUID.equals(piggateBeacon._proximityUUID)&&this._major==piggateBeacon._major&&this._minor==piggateBeacon._minor; }

    //Algorithm to put a beacon into the registry and control if a beacon is new or is a recognised beacon
    //Return a list of the new discovered beacons
    public static ArrayList<PiggateBeacon> registryBeacon(ArrayList<PiggateBeacon> beacons){

        ArrayList<PiggateBeacon> listBeacon=new ArrayList<PiggateBeacon>();
        Date date=new Date();

        for(int x=0;x<beacons.size();x++) { //For every beacon detected

            PiggateBeacon beacon = _registry.get(beacons.get(x).toString()); //Get the beacons of the registry

            if (beacon != null) { //If there is a beacon into registry

                //If time since last call until now is greater than 35 seconds the beacon is new
                if (Piggate.getDateDiff(beacon.getLastCall(), date, TimeUnit.SECONDS) > 35) {
                    beacon = beacons.get(x);
                    beacon.setLastCall(date);
                    beacon.setFirstCall(date);
                    _registry.put(beacon.toString(), beacon);
                    listBeacon.add(beacon);
                } else { //If this time is lower than 35 seconds there are two different possibilities
                    //If time since first call until now is greater than 3600 seconds (1 hour) beacon is new
                    if (Piggate.getDateDiff(beacon.getFirstCall(), date, TimeUnit.SECONDS) > 3600) {
                        beacon = beacons.get(x);
                        beacon.setLastCall(date);
                        beacon.setFirstCall(date);
                        _registry.put(beacon.toString(), beacon);
                        listBeacon.add(beacon);
                    } else { //If time since first call until now is lower than 3600 seconds (1 hour) beacon is not new (is a detected beacon)
                        beacons.get(x).setLastCall(date);
                        beacons.get(x).setFirstCall(beacon.getFirstCall());
                        _registry.put(beacons.get(x).toString(), beacons.get(x));
                    }
                }

            } else { //If there is not a beacon into registry
                beacon = beacons.get(x);
                beacon.setLastCall(date);
                beacon.setFirstCall(date);
                _registry.put(beacon.toString(), beacon);
                listBeacon.add(beacon);
            }
        }

        return listBeacon;
    }

    //Make a hash code with the major, minor and proximity UUID of the beacon
    public int hashCode() {
        int hash=1;
        hash = hash * 17 + (65536-_major);
        hash = hash * 101 + (65536-_minor);
        hash=hash * 13 + ((_proximityUUID == null) ? 0 : _proximityUUID.hashCode());
        return hash;
    }

    //Method for get all the pending beacons
    public static ArrayList<PiggateBeacon> getPendingBeacons() {
        Lock l = rwLock.readLock();
        ArrayList<PiggateBeacon> result = new ArrayList<PiggateBeacon>();
        l.lock();
        try {
            result.addAll(pending);
            pending.clear();
        }
        catch(Exception ex) {
        }
        finally {
            l.unlock();
        }
        return result;
    }

    //Method for add a pending beacon
    public static void addPendingBeacons(ArrayList<PiggateBeacon> bar){
        Boolean exist = false;
        Lock l = rwLock.writeLock();
        l.lock();
        try {
            //Check if the beacon is already into the pending beacons
            for(int x=0; x<bar.size(); x++) {
                for(int y=0; y<pending.size(); y++){
                    if(bar.get(x).toString().equals(pending.get(y).toString()))
                        exist = true;
                }
                if(exist != true)
                    pending.add(bar.get(x));
            }
        }
        catch(Exception ex) {
        }
        finally {
            l.unlock();
        }
    }
}
