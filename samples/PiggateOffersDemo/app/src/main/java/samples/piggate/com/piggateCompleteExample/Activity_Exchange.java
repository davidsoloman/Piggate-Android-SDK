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
import com.loopj.android.image.SmartImageView;
import com.piggate.sdk.Piggate;
import com.piggate.sdk.PiggateUser;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/*
Activity for exchange offers (already payed) with a secret store code
*/
public class Activity_Exchange extends ActionBarActivity {

    EditText exchangeCode; //Code to exchange an offer
    Button exchangeButton; //Button to accept
    AlertDialog networkErrorDialog;
    AlertDialog errorDialog;
    SmartImageView image;
    Piggate piggate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);

        piggate = new Piggate(this); //Initialize Piggate Object

        //Set action bar fields
        getSupportActionBar().setTitle(PiggateUser.getEmail());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        exchangeCode = (EditText) findViewById(R.id.editExchange);
        exchangeButton = (Button) findViewById(R.id.buttonExchange);
        image = (SmartImageView) findViewById(R.id.offerImage3);

        image.setImageUrl(getIntent().getExtras().getString("offerImgURL")); //Set the offer image from URL

        errorDialog = new AlertDialog.Builder(this).create();
        errorDialog.setTitle("Exchange error");
        errorDialog.setMessage("There is an error with the exchange");
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

        exchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = exchangeCode.getText().toString();

                if(checkInternetConnection()==true){
                    if(exchangeCode.getText().toString().equals("")) {
                        exchangeCode.setError("Enter the code");
                    }
                    else{
                        RequestParams params = new RequestParams();

                        params.put("code_unlock", code); //Exchange code

                        //Request to the server sending offer payed
                        piggate.RequestExchange(params, getIntent().getExtras().getString("exchangeID")).setListenerRequest(new Piggate.PiggateCallBack() {
                            @Override
                            public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                                goBackActivity(true);
                            }

                            @Override
                            public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                                errorDialog.show();
                            }

                            @Override
                            public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {
                                goBackActivity(true);
                            }

                            @Override
                            public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {
                                errorDialog.show();
                            }
                        }).exec();
                    }
                }
                else{
                    networkErrorDialog.show();
                }
            }
        });
    }

    public void goBackActivity(boolean exchanged){
        //Go back to the offer list passing the offer attributes (to exchange)
        Intent slideactivity = new Intent(Activity_Exchange.this, Activity_Logged.class);
        slideactivity.putExtra("exchanged", exchanged);
        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle bndlanimation =
                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromleft, R.anim.slidetoright).toBundle();
        startActivity(slideactivity, bndlanimation);
    }

    //Create the options menu for action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    //Handles the click on a menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                goBackActivity(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //onBackPressed method for the activity
    @Override
    public void onBackPressed(){
        goBackActivity(false);
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
