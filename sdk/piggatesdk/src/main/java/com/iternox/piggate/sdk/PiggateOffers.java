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


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class PiggateOffers {
    static ArrayList<PiggateOffers> _registry = new ArrayList<PiggateOffers>();

    public String _id;
    public String _name;
    public String _description;
    public Double _price;
    public Double _discount;
    public double _latitude;
    public double _longitude;
    private Date _lastcall;

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
            _discount=object.getDouble("discount");
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

    public Double getDiscount() {
        return _discount;
    }

    public void setDiscount(Double _discount) {
        this._discount = _discount;
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
    @Override
    public String toString(){
        return getID()+" "+getName()+" "+getDescription();
    }
    @Override
    public boolean equals(Object obj){
        try {

            return ((PiggateOffers)obj).getID().equals(getID());
        }
        catch (Exception e){
            return false;
        }
    }
    public Date getLastCall(){
        return _lastcall;
    }
    public void setLastCall(Date date){
        _lastcall=date;
    }


}
