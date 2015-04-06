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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

/*
PiggateRestClient class: define the base class to handle the request to the server
----------------------------------------------------------------------------------
This class contains all the attributes of a request and the essential methods
*/
public class PiggateRestClient {
    private static final String PROTOCOL = "http"; //Protocol
    private static final String PORT = "80"; //Port (80)
    private static final String HOST = "piggate.com"; //Host (piggate.com)
    private static final String BASE_URL = PROTOCOL+"://"+HOST+":"+PORT+"/api/v1/"; //Complete URL for the API
    private static final AsyncHttpClient syncClient = new SyncHttpClient(); //AsyncHttpClient object

    //Function to cancel the requests
    public static void cancelRequests(android.content.Context context,
                                      boolean mayInterruptIfRunning){
        getClient().cancelRequests(context,
                mayInterruptIfRunning);
    }

    //Function to cancel all the requests
    public static void cancelAllRequests(
                                      boolean mayInterruptIfRunning){
        getClient().cancelAllRequests(
                mayInterruptIfRunning);
    }

    //Function to set the cookie store
    public static void setCookieStore(PersistentCookieStore cookie){
        getClient().setCookieStore(cookie);
    }

    //Function to do a GET request
    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().get(getAbsoluteUrl(url), params, responseHandler);
    }

    //Function to do a POST request
    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().post(getAbsoluteUrl(url), params, responseHandler);
    }

    //Function to do a delete request
    public static void delete(String url, AsyncHttpResponseHandler responseHandler) {
        getClient().delete(getAbsoluteUrl(url), responseHandler);
    }

    //Function to do a PUT request
    public static void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().put(getAbsoluteUrl(url), params, responseHandler);
    }

    //Function to get the AsyncHttpClient object
    public static AsyncHttpClient getClient()
    {
            syncClient.setTimeout(150000);
            return syncClient;
    }

    //Function to get the absolute url
    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}