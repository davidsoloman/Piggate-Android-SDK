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

package samples.piggate.com.piggateCompleteExample;

import android.app.Application;
import android.content.Intent;

/*
Application class: start the service when the application is open
*/
public class Application_Notify extends Application {

    public static boolean exchangeRequest = false; //If true: there are offers to exchange

    public void onCreate() {
        super.onCreate();
        startService(new Intent(getApplicationContext(), Service_Notify.class)); //Start the notification service
    }
}