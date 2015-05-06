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
package samples.piggate.com.piggateInfoDemo;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.piggate.sdk.Piggate;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/*
Main activity for the login application
The activity have a button for login and a button for register
*/
public class Activity_Main extends ActionBarActivity {

    Piggate _piggate; //Object of the Piggate class
    ProgressDialog loadingDialog;
    AlertDialog networkErrorDialog;

    //Method onCreate of activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _piggate=new Piggate(this,null); //Initialize a Piggate object

            getSupportActionBar().setTitle("Piggate Info Demo");

        networkErrorDialog = new AlertDialog.Builder(this).create();
        networkErrorDialog.setTitle("Network error");
        networkErrorDialog.setMessage("There is an error with the network connection");
        networkErrorDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        setContentView(R.layout.activity_main);
        final Button login = (Button)findViewById(R.id.buttonloginmain);
        final Button register = (Button)findViewById(R.id.buttonregistermain);

        //onClick listener of the login button: go to Activity_SingIn
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            synchronized public void onClick(View v) {

                    Intent slideactivity = new Intent(Activity_Main.this, Activity_SingIn.class);
                    slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromright, R.anim.slidetoleft).toBundle();
                    startActivity(slideactivity, bndlanimation);
                }
        });

        //onClick listener of the register button: go to Activity_SingUp
        register.setOnClickListener(new View.OnClickListener(){
            @Override
            synchronized public void onClick(View v) {

                    Intent slideactivity = new Intent(Activity_Main.this, Activity_SingUp.class);
                    slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromright, R.anim.slidetoleft).toBundle();
                    startActivity(slideactivity, bndlanimation);
                }
        });

        //If you close the application without logout, the session will be active
        //Call a listener of the RequestUser method for Piggate object
        if(Service_Notify.logout == false) {
            if (checkInternetConnection()) {

                loadingDialog = ProgressDialog.show(this, "Singing In", "Wait a few seconds", true);
                _piggate.RequestUser().setListenerRequest(new Piggate.PiggateCallBack() {
                    //Method onComplete for JSONObject
                    //If the request is completed correctly the user is redirected to Activity_Logged
                    @Override
                    public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {

                        loadingDialog.dismiss();
                        Intent slideactivity = new Intent(Activity_Main.this, Activity_Logged.class);
                        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromright, R.anim.slidetoleft).toBundle();
                        startActivity(slideactivity, bndlanimation);
                    }

                    //Method onError for JSONObject
                    //When we have an error, reload the Piggate object
                    @Override
                    public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                        loadingDialog.dismiss();
                        _piggate.reload();
                    }

                    //Method onComplete for JSONArray
                    @Override
                    public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {
                        //Unused
                    }

                    //Method onError for JSONArray
                    @Override
                    public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {
                        //Unused
                    }
                }).exec();
            } else {
                networkErrorDialog.show();
            }
        }
    }

    //Method onResume of the activity
    @Override
    protected void onResume(){
        super.onStart();
    }

    //Check if the internet connection is working
    private boolean checkInternetConnection(){
        Context context=this;
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService (context.CONNECTIVITY_SERVICE);
        if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected())
            return true;
        else
            return false;
    }
}
