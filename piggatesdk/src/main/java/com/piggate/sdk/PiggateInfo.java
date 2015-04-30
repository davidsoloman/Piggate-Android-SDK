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
PiggateInfo class: class for museum artworks information or similar, associated to iBeacons
--------------------------------------------------------------------------------------------
This class contains all the attributes for the info content and all the essential methods
*/
public class PiggateInfo {

    public String _id; //ID of the content
    public String _name; //Name of the content
    public String _description; //Description of the content
    public String _imgURL; //Image for the content (or URL)
    public String _videoURL; //Video (or URL)
    private Date _lastcall; //Last call to the info content
    static ArrayList<PiggateInfo> internal_info=new ArrayList<>(); //Array of internal info contents
    private static ReadWriteLock rwLock2 =new ReentrantReadWriteLock(); //ReadWriteLock for synchronize

    //Public PiggateInfo constructor
    public PiggateInfo(JSONObject object) {
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
            _videoURL=object.getString("video");
        } catch (JSONException e) {
        }
        try {
            _imgURL=object.getString("img");
        } catch (JSONException e) {
        }
    }

    public PiggateInfo(String id, String title, String description, String imageURL, String videoURL) {
        this._id = id;
        this._name = title;
        this._description = description;
        this._imgURL = imageURL;
        this._videoURL = videoURL;
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

    public String get_imgURL() {
        return _imgURL;
    }

    public void set_imgURL(String _imgURL) {
        this._imgURL = _imgURL;
    }

    public String get_videoURL() {
        return _videoURL;
    }

    public void set_videoURL(String _videoURL) {
        this._videoURL = _videoURL;
    }

    public Date getLastCall(){
        return _lastcall;
    }

    public void setLastCall(Date date){
        _lastcall=date;
    }

    //Convert the parameters of an offer to String
    @Override
    public String toString(){
        return getID()+" "+getName()+" "+getDescription()+" "+get_imgURL()+" "+get_videoURL();
    }

    //Return if an info content is equal to another
    @Override
    public boolean equals(Object obj){
        try {

            return ((PiggateInfo)obj).getID().equals(getID());
        }
        catch (Exception e){
            return false;
        }
    }

    private static void registryInfo(ArrayList<PiggateInfo> infos){

        ArrayList< PiggateInfo> _new_registry = new ArrayList<PiggateInfo>();
        Date date=new Date();
        PiggateInfo info;

        for(int x=0;x<infos.size();x++){
            info=infos.get(x);
            info.setLastCall(date);
            int index=internal_info.indexOf(info);
            if(index>=0){
                internal_info.remove(index);
            }
            internal_info.add(info);
        }

        for(int x=0;x<internal_info.size();x++){
            info=internal_info.get(x);
            if(Piggate.getDateDiff(info.getLastCall(),date, TimeUnit.SECONDS)<=35){
                _new_registry.add(info);
            }
        }
        internal_info=_new_registry;
    }

    private static void registryInfo(JSONArray offers) {
        ArrayList<PiggateInfo> listaOffers = new ArrayList<PiggateInfo>();
        for (int x = 0; x < offers.length(); x++) {
            try {
                listaOffers.add(new PiggateInfo(offers.getJSONObject(x)));
            } catch (JSONException e) {

            }
        }
        registryInfo(listaOffers);
    }

    public static ArrayList<PiggateInfo> getInfo() {
        Lock l = rwLock2.readLock();
        ArrayList<PiggateInfo> result = new ArrayList<PiggateInfo>();
        l.lock();
        try {
            result.addAll(internal_info);
        }
        catch(Exception ex) {
        }
        finally {
            l.unlock();
        }
        return result;
    }

    public static void addInfo(JSONArray bar){

        Lock l = rwLock2.writeLock();
        l.lock();
        try {
            registryInfo(bar);
        }
        catch(Exception ex) {
        }
        finally {
            l.unlock();
        }
    }
}