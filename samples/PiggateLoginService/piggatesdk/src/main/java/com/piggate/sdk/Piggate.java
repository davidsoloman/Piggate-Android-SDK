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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.piggate.sdk.bridges.BaseBridge;
import com.piggate.sdk.bridges.PiggateEstimoteBridge;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/*
Piggate class: the main class of the Piggate SDK
-------------------------------------------------
We use an object of this class to do the scanning, request and notifications with beacons
The constructors of this object contains the Estimote bridge to use the functions of the Estimote SDK
*/
public class Piggate{

    private Activity _activity; //Activity where Piggate is used
    private Application _app; //Application where Piggate is used
    private Service _service; //Service where Piggate is used
    private Context _context; //Context where Piggate is used
    private BaseBridge _bridge; //BaseBridge is the base class of PiggateEstimoteBridge

    NotificationManager notificationManager; //For the notifications
    PersistentCookieStore _cookieStore; //For saving the cookies of the application
    String APP_ID; //ID of the application

    //Return the context of the application
    public Context getApplicationContext(){
        return _context;
    }

    //Callback that allows the user override these methods and handle these events
    public static interface PiggateBeaconCallback{
        public void DeviceNotCompatible();
        public void BluetoohNotConnect();
        public void PreScanning();
        public void onReady();
        public void onErrorScanning();
        public void GetNewBeacons(ArrayList<PiggateBeacon> beacons);
        public void GetBeacons(ArrayList<PiggateBeacon> beacons);
    }

    //Callback for onComplete and onError methods for JSONObject and JSONArray
    public static interface PiggateCallBack{
        public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data);
        public void onError(int statusCode, Header[] headers, String msg, JSONObject data);
        public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data);
        public void onError(int statusCode, Header[] headers, String msg, JSONArray data);
    }

    interface PreCallable{
        void call();
    }

    //Set the beacon listener with the Piggate callback
    public void setListenerBeacon(PiggateBeaconCallback callBack){
        _bridge.setPiggateCallback(callBack);
    }

    //Complete function for post notifications
    //The user can enter the title, the message, the class where notification is executed, the extras bundle and a boolean to force the notifications
    public void postNotification(String title, String msg ,Class myClass, int resource, Bundle extras,Boolean force) {
        if (!getApplicationContext().getPackageName().equalsIgnoreCase(((ActivityManager) getApplicationContext().getSystemService(getApplicationContext().ACTIVITY_SERVICE)).getRunningAppProcesses().get(0).processName) || force) {

            Intent notifyIntent = new Intent(_context, myClass);
            if(extras!=null)
                notifyIntent.putExtras(extras);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivities(
                    _context,
                    0,
                    new Intent[]{notifyIntent},
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new Notification.Builder(_context)
                    .setSmallIcon(resource)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build();
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_LIGHTS;
            notificationManager.notify(123, notification);
        }
    }

    //Do notifications whithout an extras bundle
    public void postNotification(String title, String msg ,Class myClass, int resource,Boolean force) {
        postNotification(title,msg,myClass,resource,null,force);
    }

    //Do notifications without an extras bundle and without force
    public void postNotification(String title, String msg ,Class myClass, int resource) {
        postNotification(title,msg,myClass,resource,(Bundle)null);
    }

    //Do post notifications without force
    public void postNotification(String title, String msg ,Class myClass, int resource, Bundle extras) {
        postNotification(title,msg,myClass,resource,extras,false);
    }

    /*
    Class Request: used for doing all the request of Piggate class
    */
    public static class Request{
        private Piggate _caller;
        private RequestParams _params;
        private String _method;
        private String _url;
        private PiggateCallBack _callBack;
        private JsonHttpResponseHandler _rest_callback;
        private PreCallable _precallable;
        private Request(Piggate caller){
            _caller=caller;
        }

        //Set the listener
        public Request setListenerRequest(PiggateCallBack callBack){
            _callBack=callBack;
            return this;
        }

        //Execute the GET, PUT, POST or DELETE request
        public void exec(){
            run(new Runnable() {
                public void run() {
                    Looper.prepare();
                    switch (_method) {
                        case "GET":
                            if (_precallable != null) {
                                _precallable.call();
                            }
                            PiggateRestClient.get(_url, _params, _rest_callback);
                            break;
                        case "PUT":
                            if (_precallable != null) {
                                _precallable.call();
                            }
                            PiggateRestClient.put(_url, _params, _rest_callback);
                            break;
                        case "POST":
                            if (_precallable != null) {
                                _precallable.call();
                            }
                            PiggateRestClient.post(_url, _params, _rest_callback);
                            break;
                        case "DELETE":
                            if (_precallable != null) {
                                _precallable.call();
                            }
                            PiggateRestClient.delete(_url, _rest_callback);
                            break;
                    }
                    Looper.loop();
                }
            });
        }

        //Create a thread
        private void run(Runnable hilo) {
            new Thread(hilo).start();
        }
    }

    //Function to get the metadata
    public static String getMetadata(Context context, String name) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                return appInfo.metaData.getString(name);
            }
        } catch (PackageManager.NameNotFoundException e) { }

        return null;
    }

    //Piggate constructor for Activity
    public Piggate(Activity activity){
        _activity=activity;
        internal_constructor(_context = _activity.getApplicationContext(),new PiggateEstimoteBridge(this));
    }

    //Piggate constructor for Application
    public Piggate(Application app){
        _app=app;
        internal_constructor(_context=_app.getApplicationContext(),new PiggateEstimoteBridge(this));
    }

    //Piggate constructor for Service
    public Piggate(Service service){
        _service=service;
        internal_constructor(_context=_service.getApplicationContext(),new PiggateEstimoteBridge(this));
    }

    //Piggate constructor for Context
    public Piggate(Context context){
        internal_constructor(_context=context,new PiggateEstimoteBridge(this));
    }

    //Piggate constructor for Activity and BaseBridge
    public Piggate(Activity activity,BaseBridge bridge){
        _activity=activity;
        internal_constructor(_context = _activity.getApplicationContext(),bridge);
    }

    //Piggate constructor for Application and BaseBridge
    public Piggate(Application app,BaseBridge bridge){
        _app=app;
        internal_constructor(_context=app.getApplicationContext(),bridge);
    }

    //Piggate constructor for Context and BaseBridge
    public Piggate(Context context,BaseBridge bridge){
        internal_constructor(_context=context,bridge);
    }

    //Function to cancel a request
    public void cancelRequests(boolean mayInterruptIfRunning){
        PiggateRestClient.cancelRequests(_context,
                mayInterruptIfRunning);
    }

    //Function to cancel all the requests
    public void cancelAllRequests(
            boolean mayInterruptIfRunning){
        PiggateRestClient.cancelAllRequests(
                mayInterruptIfRunning);
    }

    //Define the internal constructor of Piggate
    private void internal_constructor(Context context,BaseBridge bridge){
        APP_ID=getMetadata(context,"com.piggate.sdk.ApplicationId");
        _cookieStore = new PersistentCookieStore(context);
        PiggateRestClient.setCookieStore(_cookieStore);
        _bridge =bridge;
        notificationManager = (NotificationManager) _context.getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
    }

    //Clear the cookie store
    public void reload(){
        _cookieStore.clear();
    }

    //onDestroy method
    public void onDestroy() {
        _bridge.onDestroy();
    }

    //onStart method
    public void onStart() {
            _bridge.onStart();
    }

    //onStop method
    public void onStop() {
        _bridge.onStop();
    }

    //onActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
            _bridge.onActivityResult(requestCode, resultCode, data);
    }

    //Function to do the request for sign up with a new user
    //do a POST request to the server and register the new user if does not exist
    public Request RequestNewUser(RequestParams params){
        final Request request=new Request(this);
        request._method="POST"; //define the request method
        params.put("app", APP_ID);
        request._params=params; //define the params
        request._url="client/signup"; //define the url to do the request

        request._precallable=new PreCallable() {
            @Override
            public void call() {
                reload();
            }
        };

        //Handle the request events (if the request fail or is correct)
        request._rest_callback=new JsonHttpResponseHandler() {

            //Handle the request error for JSONArray
            @Override
            public void onFailure(int statusCode, Header[] headers,  Throwable e, JSONArray response) {
                //Unused
            }

            //Handle the request error for JSONObject
            //Take the error message to return to the user
            @Override
            public void onFailure(int statusCode, Header[] headers,Throwable throwable, JSONObject response){

                String msg="";

                if(response!=null){

                    try {
                        msg = response.getString("error");
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                }
                if(request._callBack!=null){
                    request._callBack.onError(statusCode, headers,msg,(JSONObject)null);
                }
            }

            //Handle the request success for JSONObject
            //Return a message to the user
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                String msg="";
                JSONObject obj=null;
                // If the response is JSONObject instead of expected JSONArray
                if(response!=null){
                    try {
                        obj = response.getJSONObject("data");
                        PiggateUser.getInstance(obj.getString("_id"),obj.getString("email"));
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                    try {
                        msg = response.getString("success");
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                }
                if(request._callBack!=null){
                    request._callBack.onComplete(statusCode, headers,msg,obj);
                }
            }

            //Handle the request success for JSONArray
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

            }
        };
        return request;
    }

    //Function to do the request for sign in into the application
    //do a POST request to the server and login with a registered user
    public Request RequestOpenSession(RequestParams params){
        final Request request=new Request(this);
        request._method="POST"; //define the request method
        params.put("app", APP_ID);
        request._params=params; //define the params
        request._url="client/login"; //define the url to do the request

        request._precallable=new PreCallable() {
            @Override
            public void call() {
                reload();
            }
        };

        //Handle the request events (if the request fail or is correct)
        request._rest_callback=new JsonHttpResponseHandler() {

            //Handle the request error for JSONArray
            @Override
            public void onFailure(int statusCode, Header[] headers,  Throwable e, JSONArray response) {
                //Unused
            }

            //Handle the request error for JSONObject
            @Override
            public void onFailure(int statusCode, Header[] headers,Throwable throwable, JSONObject response){
                String msg="";

                if(response!=null){

                    try {
                        msg = response.getString("error");
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                }
                PiggateUser.getInstance(null,null);
                if(request._callBack!=null){
                    request._callBack.onError(statusCode, headers,msg,(JSONObject)null);
                }
            }

            //Handle the request success for JSONObject
            //Return a message to the user
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                String msg="";
                JSONObject obj=null;
                // If the response is JSONObject instead of expected JSONArray
                if(response!=null){
                    try {
                        obj = response.getJSONObject("data");
                        PiggateUser.getInstance(obj.getString("_id"),obj.getString("email"));
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                    try {
                        msg = response.getString("success");
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                }

                if(request._callBack!=null){
                    request._callBack.onComplete(statusCode, headers,msg,obj);
                }
            }

            //Handle the request success for JSONArray
            //Return a message to the user
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                //Unused
            }
        };
        return request;
    }

    //Function to do the request for close a opened session
    //do a GET request to the server and close the session of the logged user
    public Request RequestCloseSession(){
        final Request request=new Request(this);
        request._method="GET"; //define the request method
        request._params=null; //define the params
        request._url="client/logout"; //define the url to do the request

        //Handle the request events (if the request fail or is correct)
        request._rest_callback=new JsonHttpResponseHandler() {

            //Handle the request error for JSONArray
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray response) {
                //Unused
            }

            //Handle the request error for JSONObject
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String msg = "";
                JSONObject obj = null;
                // If the response is JSONObject instead of expected JSONArray
                if(response!=null){

                    try {
                        msg = response.getString("error");
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                }
                PiggateUser.getInstance(null,null);

                if (request._callBack != null) {
                    request._callBack.onError(statusCode, headers,msg, (JSONObject)null);
                }
            }

            //Handle the request success for JSONObject
            //Return a message to the user
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String msg = "";
                Object obj = null;
                // If the response is JSONObject instead of expected JSONArray
                if(response!=null){
                    try {
                        obj = response.getString("data");
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                    try {
                        msg = response.getString("success");
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                }
                PiggateUser.getInstance(null,null);

                if (request._callBack != null) {
                    request._callBack.onComplete(statusCode, headers,msg, (JSONObject)null);
                }
            }

            //Handle the request success for JSONArray
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                //Unused
            }
        };
        return request;
    }

    //Get the difference between two dates
    public static long getDateDiff(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return diffInMillies;
    }

    //Get the difference between two dates in milliseconds
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    //Function to do the request for get the offers
    //do a GET request to the server to get the offers for an existing beacon
    public Request RequestOffers(final PiggateBeacon beacon){
        final Request request=new Request(this);
        request._method="GET"; //Define the request method
        request._params=null; //Define the params
        request._url="client/get/ibeacon/major/"+beacon.getMajor()+"/minor/"+beacon.getMinor(); //Define the url
        //Handle the request events (if the request fail or is correct)
        request._rest_callback=new JsonHttpResponseHandler() {

            //Handle the request error for JSONArray
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray response) {
                //Unused
            }

            //Handle the request error for JSONObject
            //Return a message to the user
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String msg = "";
                JSONObject obj = null;
                if(response!=null){ // If the response is JSONObject instead of expected JSONArray
                    try {
                        msg = response.getString("error");
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                }
                if (request._callBack != null) {
                    PiggateBeacon.addPendingBeacons(new ArrayList<>(Arrays.asList(beacon)));
                    request._callBack.onError(statusCode, headers,msg, (JSONArray)null);
                }
            }

            //Handle the request success for JSONObject
            //Return a message to the user
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String msg = "";
                JSONArray obj = null;
                // If the response is JSONObject instead of expected JSONArray
                if(response!=null){
                    try {
                        obj = response.getJSONArray("data");
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                    try {
                        msg = response.getString("success");
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                }
                if (request._callBack != null) {
                    request._callBack.onComplete(statusCode, headers,msg, (JSONArray)obj);
                    //Add the offers to the registry
                    PiggateOffers.addOffers(obj);
                }
            }

            //Handle the request success for JSONArray
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                //Unused
            }
        };
        return request;
    }

    //Function to do the GET request for get an user
    public Request RequestUser(){
        final Request request=new Request(this);
        request._method="GET"; //Define the request method
        request._params=null; //Define the params
        request._url="client"; //Define the url

        //Handle the request events (if the request fail or is correct)
        request._rest_callback=new JsonHttpResponseHandler() {

            //Handle the request error for JSONArray
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray response) {
                //Unused
            }

            //Handle the request error for JSONObject
            //return a message to the user
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {

                String msg = "";
                JSONObject obj = null;
                // If the response is JSONObject instead of expected JSONArray
                if(response!=null){

                    try {
                        msg = response.getString("error");
                        PiggateUser.getInstance(null,null);

                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                }

                if (request._callBack != null) {
                    request._callBack.onError(statusCode, headers,msg, (JSONObject)null);
                }
            }

            //Handle the request success for JSONObject
            //Return a message to the user
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String msg = "";
                JSONObject obj = null;
                if(response!=null){ // If the response is JSONObject instead of expected JSONArray
                    try {
                        obj = response.getJSONObject("data");
                        PiggateUser.getInstance(obj.getString("_id"),obj.getString("email"));
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                    try {
                        msg = response.getString("success");
                    } catch (JSONException a) {

                    }
                    catch (NullPointerException a) {

                    }
                }
                if (request._callBack != null) {
                    request._callBack.onComplete(statusCode, headers,msg, obj);
                }
            }

            //Handle the request success for JSONArray
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                //Unused
            }
        };
        return request;
    }
}
