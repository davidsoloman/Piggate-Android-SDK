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

public class PiggateUser {
    private String _id;
    private String _email;
    private static PiggateUser _instance;
    private PiggateUser() {
        super();
    }

    private PiggateUser(String id, String email) {
        super();
        this._id = id;
        this._email = email;
    }
    public static String getId() {
        return getInstance()._id;
    }

    public static void setId(String id) {
        getInstance()._id = id;
    }

    public static String getEmail() {
        return getInstance()._email;
    }

    public static void setEmail(String email) {
        getInstance()._email = email;
    }

    public static PiggateUser getInstance(){
        if(_instance==null){
            _instance=new PiggateUser();
        }
        return _instance;
    }
    public static PiggateUser getInstance( String id, String email){
        if(_instance==null){
            _instance=new PiggateUser( id, email);
        }
        _instance._id = id;
        _instance._email = email;
        return _instance;
    }

}
