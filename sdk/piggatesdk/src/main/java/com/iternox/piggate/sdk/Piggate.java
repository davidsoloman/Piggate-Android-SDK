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
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Looper;

import org.json.*;

import com.iternox.piggate.sdk.bridges.BaseBridge;
import com.iternox.piggate.sdk.bridges.PiggateEstimoteBridge;
import com.loopj.android.http.*;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Piggate{
    private Activity _activity;
    private Application _app;
    private Context _context;
    private BaseBridge _bridge;


    PersistentCookieStore _cookieStore;
    String APP_ID;

    public Context getApplicationContext(){
        return _context;
    }


    public static interface PiggateBeaconCallback{
        public void DeviceNotCompatible();
        public void BluetoohNotConnect();
        public void PreScanning();
        public void onReady();
        public void onErrorScanning();
        public void GetNewBeacons(ArrayList<PiggateBeacon> beacons);
        public void GetBeacons(ArrayList<PiggateBeacon> beacons);
    }

    public static interface PiggateCallBack{
        public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data);
        public void onError(int statusCode, Header[] headers, String msg, JSONObject data);
        public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data);
        public void onError(int statusCode, Header[] headers, String msg, JSONArray data);
    }
    interface PreCallable{
        void call();
    }
    public void setListenerBeacon(PiggateBeaconCallback callBack){
        _bridge.setPiggateCallback(callBack);
    }
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
        public Request setListenerRequest(PiggateCallBack callBack){
            _callBack=callBack;
            return this;
        }
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

        private void run(Runnable hilo) {
            new Thread(hilo).start();
        }
        }

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

    public Piggate(Activity activity){
        _activity=activity;
        internal_constructor(_context = _activity.getApplicationContext(),new PiggateEstimoteBridge(this));
    }
    public Piggate(Application app){
        _app=app;
        internal_constructor(_context=app.getApplicationContext(),new PiggateEstimoteBridge(this));
    }
    public Piggate(Context context){
        internal_constructor(_context=context,new PiggateEstimoteBridge(this));
    }
    public Piggate(Activity activity,BaseBridge bridge){
        _activity=activity;
        internal_constructor(_context = _activity.getApplicationContext(),bridge);
    }
    public Piggate(Application app,BaseBridge bridge){
        _app=app;
        internal_constructor(_context=app.getApplicationContext(),bridge);
    }
    public Piggate(Context context,BaseBridge bridge){
        internal_constructor(_context=context,bridge);
    }

    public void cancelRequests(
                                      boolean mayInterruptIfRunning){
        PiggateRestClient.cancelRequests(_context,
                mayInterruptIfRunning);
    }
    public void cancelAllRequests(
            boolean mayInterruptIfRunning){
        PiggateRestClient.cancelAllRequests(
                mayInterruptIfRunning);
    }
    private void internal_constructor(Context context,BaseBridge bridge){
        APP_ID=getMetadata(context,"com.iternox.piggate.sdk.ApplicationId");
        _cookieStore = new PersistentCookieStore(context);
        PiggateRestClient.setCookieStore(_cookieStore);
        _bridge =bridge;

    }

    public void reload(){
        _cookieStore.clear();
    }
    public void onDestroy() {
            _bridge.onDestroy();
    }


    public void onStart() {
            _bridge.onStart();
    }

    public void onStop() {
        _bridge.onStop();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
            _bridge.onActivityResult(requestCode, resultCode, data);
    }

    public Request RequestNewUser(RequestParams params){
        final Request request=new Request(this);
        request._method="POST";
        params.put("app", APP_ID);
        request._params=params;
        request._url="client/signup";

        request._precallable=new PreCallable() {
            @Override
            public void call() {
                reload();
            }
        };
        request._rest_callback=new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers,  Throwable e, JSONArray response) {

            }
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

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

            }
        };
        return request;
    }
    public Request RequestOpenSession(RequestParams params){
        final Request request=new Request(this);
        request._method="POST";
        params.put("app", APP_ID);
        request._params=params;
        request._url="client/login";

        request._precallable=new PreCallable() {
            @Override
            public void call() {
                reload();
            }
        };
        request._rest_callback=new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers,  Throwable e, JSONArray response) {

            }
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

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

            }
        };
        return request;
    }
    public Request RequestCloseSession(){
        final Request request=new Request(this);
        request._method="GET";
        request._params=null;
        request._url="client/logout";
        request._rest_callback=new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray response) {

            }

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

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

            }
        };
        return request;
    }
    public static long getDateDiff(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return diffInMillies;
    }
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }
    public Request RequestOffers(PiggateBeacon beacon){
        final Request request=new Request(this);
        request._method="GET";
        request._params=null;
        request._url="client/get/ibeacon/major/"+beacon.getMajor()+"/minor/"+beacon.getMinor();



        request._rest_callback=new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray response) {

            }

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
                if (request._callBack != null) {
                    request._callBack.onError(statusCode, headers,msg, (JSONArray)null);
                }



            }

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
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

            }
        };
        return request;
    }
    public Request RequestUser(){
        final Request request=new Request(this);
        request._method="GET";
        request._params=null;
        request._url="client";

        request._rest_callback=new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray response) {

            }

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

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String msg = "";
                JSONObject obj = null;
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


                if (request._callBack != null) {
                    request._callBack.onComplete(statusCode, headers,msg, obj);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

            }
        };
        return request;
    }


}
