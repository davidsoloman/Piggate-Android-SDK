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
package com.iternox.piggate.samples.PiggateLogin;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.iternox.piggate.sdk.Piggate;
import com.iternox.samples.PiggateLogin.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;


public class Activity_Main extends Activity {
    Piggate _piggate;
    private boolean handledClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _piggate=new Piggate(this,null);
        Boolean logout=false;
        if(getIntent().getExtras()!=null)
        logout = getIntent().getExtras().getBoolean("ACTIVITY_MAIN_CREATED_BY_BUTTON_LOGOUT",false);

        setContentView(R.layout.activity_main);


        final Button login = (Button)findViewById(R.id.buttonloginmain);
        final Button register = (Button)findViewById(R.id.buttonregistermain);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            synchronized public void onClick(View v) {
                if (!handledClick) {
                    handledClick = true;

                    Intent slideactivity = new Intent(Activity_Main.this, Activity_SingIn.class);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.flip1, R.anim.flip2).toBundle();
                    startActivity(slideactivity, bndlanimation);
                }

            }
        });
        register.setOnClickListener(new View.OnClickListener(){
            @Override
            synchronized public void onClick(View v) {
                if (!handledClick) {
                    handledClick = true;

                    Intent slideactivity = new Intent(Activity_Main.this, Activity_SingUp.class);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.flip1, R.anim.flip2).toBundle();
                    startActivity(slideactivity, bndlanimation);

                }

            }
        });
        if(!logout && checkInternetConnection()) //If you close the application without logout, the session will be active
        _piggate.RequestUser().setListenerRequest(new Piggate.PiggateCallBack() {
            @Override
            public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                Intent slideactivity = new Intent(Activity_Main.this, Activity_Logged.class);
                Bundle bndlanimation =
                        ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.flip1, R.anim.flip2).toBundle();
                startActivity(slideactivity, bndlanimation);
            }

            @Override
            public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                _piggate.reload();
            }

            @Override
            public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {
                //Unused
            }

            @Override
            public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {
                //Unused
            }
        }).exec();



    }
    @Override
    protected void onResume(){
        super.onStart();
        handledClick = false;
    }

    private boolean checkInternetConnection(){

        Context context=this;
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService (context.CONNECTIVITY_SERVICE);
        if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected())
            return true;
        else
            return false;
    }

}
