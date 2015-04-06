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

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.RequestParams;
import com.piggate.sdk.Piggate;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/*
Activity for login into the application
The activity have two fields, user email and password, and a submit button
*/
public class Activity_SingIn extends ActionBarActivity {
    EditText editEmail;
    EditText editPass;
    Piggate _piggate; //Object of the Piggate class
    ProgressDialog loadingDialog;
    AlertDialog errorDialog;
    AlertDialog networkErrorDialog;

    //Method onCreate for the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _piggate=new Piggate(this,null); //Initialize the Piggate object

        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setContentView(R.layout.activity_login);
        //EditTexts for user email and password
        editEmail = (EditText) findViewById(R.id.editText1);
        editPass = (EditText) findViewById(R.id.editText2);
        Button login = (Button)findViewById(R.id.buttonlogin);

        errorDialog = new AlertDialog.Builder(this).create();
        errorDialog.setTitle("Login error");
        errorDialog.setMessage("There is an error with the login");
        errorDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        networkErrorDialog = new AlertDialog.Builder(this).create();
        networkErrorDialog.setTitle("Network error");
        networkErrorDialog.setMessage("There is an error with the network connection");
        networkErrorDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //OnClick listener for the login button
        //Handles the login request to the server with the email and password fields
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            synchronized public void onClick(View v) {
                        String email = editEmail.getText().toString();
                        String pass = editPass.getText().toString();

                        //If the internet connection is working
                        if(checkInternetConnection()==true){

                            if(editEmail.getText().toString().equals("") || editPass.getText().toString().equals("") ){
                                if(editEmail.getText().toString().equals(""))
                                    editEmail.setError("Enter your email");
                                if(editPass.getText().toString().equals(""))
                                    editPass.setError("Enter your password");
                            }
                            else{
                                loadingDialog = ProgressDialog.show(v.getContext(), "Singing In", "Wait a few seconds", true);
                                RequestParams params=new RequestParams();
                                params.put("email",email);
                                params.put("password",pass);

                                //Request of the Piggate object. Handles the login into the application with the user email and password
                                _piggate.RequestOpenSession(params).setListenerRequest(new Piggate.PiggateCallBack() {

                                    //Method onComplete for JSONObject
                                    //When the request is correct start Activity_Logged activity
                                    @Override
                                    public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                                        loadingDialog.dismiss();
                                        Intent slideactivity = new Intent(Activity_SingIn.this, Activity_Logged.class);
                                        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        Bundle bndlanimation =
                                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromright, R.anim.slidetoleft).toBundle();
                                        startActivity(slideactivity, bndlanimation);
                                    }

                                    //Method onError for JSONObject
                                    //If there's an error with the request displays the error message to the user
                                    @Override
                                    public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                                        loadingDialog.dismiss();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                errorDialog.show();
                                            }
                                        });
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
                        }
                        else{ //If the internet connection is not working
                            networkErrorDialog.show();
                        }
            }
        });
    }

    @Override
    public void onBackPressed(){
        backButton();
    }

    //Method onResume of the activity
    @Override
    protected void onResume(){
        super.onStart();
    }

    public void backButton(){
        Intent slideactivity = new Intent(Activity_SingIn.this, Activity_Main.class);
        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle bndlanimation =
                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromleft, R.anim.slidetoright).toBundle();
        startActivity(slideactivity, bndlanimation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                backButton();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
