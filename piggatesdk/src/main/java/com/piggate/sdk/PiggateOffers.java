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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
PiggateOffers class: define the offers that can be associated with a beacon
----------------------------------------------------------------------------
This class contains all the attributes of an offer and all the essential methods
*/
public class PiggateOffers {

    static ArrayList<PiggateOffers> _registry = new ArrayList<PiggateOffers>(); //Registry ArrayList
    public String _id; //ID of the offer
    public String _exchangeID; //ID of the exchanged offer
    public String _name; //Name of the offer
    public String _description; //Description of the offer
    public Double _price; //Price of the offer
    public String _currency; //Type of coin ($, €, ...)
    public String _imgURL; //Offer image URL
    public double _latitude; //Latitude of the location
    public double _longitude; //Longitude of the location
    private Date _lastcall; //Last call to the offer
    static ArrayList<PiggateOffers> internal_offers=new ArrayList<PiggateOffers>(); //Array of internal offers
    private static ReadWriteLock rwLock2 =new ReentrantReadWriteLock(); //ReadWriteLock for synchronize

    //Public PiggateOffers constructor
    public PiggateOffers(JSONObject object) {
        try {
            _id=object.getString("_id");
        } catch (JSONException e) {
        }
        try {
            _name=object.getString("name");
        } catch (JSONException e) {
        }
        try {
            _description=object.getString("description");
        } catch (JSONException e) {

        }
        try {
            _price=object.getDouble("price");
        } catch (JSONException e) {
        }
        try {
            _currency=object.getString("currency");
        } catch (JSONException e) {
        }
        try {
            _imgURL=object.getString("img");
        } catch (JSONException e) {
        }
        try {
            _exchangeID=object.getString("exchanged");
        } catch (JSONException e) {
        }


        JSONObject aux= null;
        try {
            aux = object.getJSONObject("position");
            _latitude=aux.getDouble("latitude");
            _longitude=aux.getDouble("length");
        } catch (JSONException e) {
        }
    }

    //Getters and settlers of the PiggateOffers attributes

    public String getID() {
        return _id;
    }

    public void setID(String _id) {
        this._id = _id;
    }

    public String getName() {
        return _name;
    }

    public void setName(String _name) {
        this._name = _name;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String _description) {
        this._description = _description;
    }

    public Double getPrice() {
        return _price;
    }

    public void setPrice(Double _price) {
        this._price = _price;
    }

    public String getCurrency(){
        return _currency;
    }

    public void setCurrency(String _currency){
        this._currency = _currency;
    }

    public String getImgURL(){
        return _imgURL;
    }

    public void setImgURL(String _imgURL){
        this._imgURL = _imgURL;
    }

    public double getLatitude() {
        return _latitude;
    }

    public void setLatitude(double _latitude) {
        this._latitude = _latitude;
    }

    public double getLongitude() {
        return _longitude;
    }

    public void setLongitude(double _longitude) {
        this._longitude = _longitude;
    }

    public Date getLastCall(){
        return _lastcall;
    }

    public void setLastCall(Date date){
        _lastcall=date;
    }

    public String getExchangeID() {
        return _exchangeID;
    }

    public void setExchangeID(String _exchangeID) {
        this._exchangeID = _exchangeID;
    }

    //Convert the parameters of an offer to String
    @Override
    public String toString(){
        return getID()+" "+getName()+" "+getDescription()+" "+getImgURL()+" "+getCurrency()+" "+getExchangeID();
    }

    //Return if an offer is equal to another
    @Override
    public boolean equals(Object obj){
        try {

            return ((PiggateOffers)obj).getID().equals(getID());
        }
        catch (Exception e){
            return false;
        }
    }

    //Algorithm to put the offers into the registry for an ArrayList
    private static void registryOffers(ArrayList<PiggateOffers> offers){

        ArrayList< PiggateOffers> _new_registry = new ArrayList<PiggateOffers>();
        Date date=new Date();
        PiggateOffers offer;

        for(int x=0;x<offers.size();x++){ //Set the last call and put offers into internal_offers
            offer=offers.get(x);
            offer.setLastCall(date);
            int index=internal_offers.indexOf(offer);
            if(index>=0){
                internal_offers.remove(index);
            }
            internal_offers.add(offer);
        }
        //If the time since the last call to the offers until now is less than 35 seconds
        //put the offers into a new registry
        for(int x=0;x<internal_offers.size();x++){
            offer=internal_offers.get(x);
            if(Piggate.getDateDiff(offer.getLastCall(),date, TimeUnit.SECONDS)<=35){
                _new_registry.add(offer);
            }
        }
        internal_offers=_new_registry;
    }

    //Registry the offers for a JSONArray object
    private static void registryOffers(JSONArray offers) {
        ArrayList<PiggateOffers> listaOffers = new ArrayList<PiggateOffers>();
        for (int x = 0; x < offers.length(); x++) {
            try {
                listaOffers.add(new PiggateOffers(offers.getJSONObject(x)));
            } catch (JSONException e) {

            }
        }
        registryOffers(listaOffers);
    }

    //Get the offers to an ArrayList
    public static ArrayList<PiggateOffers> getOffers() {
        Lock l = rwLock2.readLock();
        ArrayList<PiggateOffers> result = new ArrayList<PiggateOffers>();
        l.lock();
        try {
            result.addAll(internal_offers);
        }
        catch(Exception ex) {
        }
        finally {
            l.unlock();
        }
        return result;
    }

    //Add the offers to the registry using the function registryOffers
    public static void addOffers(JSONArray bar){

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
}
