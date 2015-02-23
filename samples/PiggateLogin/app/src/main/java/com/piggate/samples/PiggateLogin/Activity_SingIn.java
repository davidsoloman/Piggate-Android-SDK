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
package com.piggate.samples.PiggateLogin;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.piggate.sdk.Piggate;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/*
Activity for register into the application
The activity have two fields (same as login activity) user email and password, and a submit button
*/
public class Activity_SingIn extends Activity {
    EditText editEmail;
    EditText editPass;
    Piggate _piggate; //Object of the Piggate class
    private boolean handledClick = false; //Handles click in buttons

    //Method onCreate for the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _piggate=new Piggate(this,null); //Initialize the Piggate object

        setContentView(R.layout.activity_login);
        //EditTexts for user email and password
        editEmail = (EditText) findViewById(R.id.editText1);
        editPass = (EditText) findViewById(R.id.editText2);
        Button login = (Button)findViewById(R.id.buttonlogin2);
        final ImageButton return_button = (ImageButton)findViewById(R.id.return1);

        //Handles the top left back button of the activity
        return_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //OnClick listener for the login button
        //Handles the register request to the server with the email and password fields
        //and the login request if the fields are correct
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            synchronized public void onClick(View v) {
                    if (!handledClick) {
                        handledClick = true;
                        String email = editEmail.getText().toString();
                        String pass = editPass.getText().toString();

                        //If the internet connection is working
                        if(checkInternetConnection()==true){
                            RequestParams params=new RequestParams();
                            params.put("email",email);
                            params.put("password",pass);

                            //Request of the Piggate object. Handles the register into the application
                            // and the login with the user email and password
                            _piggate.RequestOpenSession(params).setListenerRequest(new Piggate.PiggateCallBack() {

                                //Method onComplete for JSONObject
                                //When the request is correct start Activity_Logged activity
                                @Override
                                public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                    Intent slideactivity = new Intent(Activity_SingIn.this, Activity_Logged.class);
                                    Bundle bndlanimation =
                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.flip1, R.anim.flip2).toBundle();
                                    startActivity(slideactivity, bndlanimation);
                                }

                                //Method onError for JSONObject
                                //If there's an error with the request displays the error message to the user
                                @Override
                                public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                    handledClick = false;

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
                        }
                        else{ //If the internet connection is not working
                            Toast.makeText(getApplicationContext(), "Network is not working", Toast.LENGTH_LONG).show();
                            handledClick = false;

                        }
                    }
            }
        });
    }

    //Method onResume of the activity
    @Override
    protected void onResume(){
        super.onStart();
        handledClick = false;
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
