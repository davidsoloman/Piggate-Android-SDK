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
package com.piggate.sdk.bridges;

import android.content.Intent;

import com.piggate.sdk.Piggate;

/*
PiggateUser class: define the base class for the bridge
--------------------------------------------------------
Contains the basic methods used in PiggateEstimoteBridge
*/
public interface BaseBridge {
    public void onDestroy();
    public void onStart();
    public void onActivityResult(int requestCode, int resultCode, Intent data);
    public void onStop() ;
    public void setPiggateCallback(Piggate.PiggateBeaconCallback callback);
}
