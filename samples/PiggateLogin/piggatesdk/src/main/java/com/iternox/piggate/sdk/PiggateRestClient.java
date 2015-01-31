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

import com.loopj.android.http.*;

public class PiggateRestClient {
    private static final String PROTOCOL = "http";
    private static final String PORT = "80";
    private static final String HOST = "piggate.com";
    private static final String BASE_URL = PROTOCOL+"://"+HOST+":"+PORT+"/api/v1/";
    private static final AsyncHttpClient syncClient = new SyncHttpClient();

    public static void cancelRequests(android.content.Context context,
                                      boolean mayInterruptIfRunning){
        getClient().cancelRequests(context,
                mayInterruptIfRunning);
    }
    public static void cancelAllRequests(
                                      boolean mayInterruptIfRunning){
        getClient().cancelAllRequests(
                mayInterruptIfRunning);
    }
    public static void setCookieStore(PersistentCookieStore cookie){
        getClient().setCookieStore(cookie);
    }
    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().post(getAbsoluteUrl(url), params, responseHandler);

    }
    public static void delete(String url, AsyncHttpResponseHandler responseHandler) {
        getClient().delete(getAbsoluteUrl(url), responseHandler);

    }
    public static void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().put(getAbsoluteUrl(url), params, responseHandler);

    }
    public static AsyncHttpClient getClient()
    {
            syncClient.setTimeout(150000);
            return syncClient;
    }
    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}