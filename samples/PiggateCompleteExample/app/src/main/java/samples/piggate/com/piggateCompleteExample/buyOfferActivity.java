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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.loopj.android.http.RequestParams;
import com.piggate.sdk.Piggate;
import com.piggate.sdk.PiggateUser;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
/*
Activity for buying the selected offer. Do a request to our server for the payment
*/
public class buyOfferActivity extends ActionBarActivity{

    Piggate piggate;
    EditText EditCardNumber;
    EditText EditCVC;
    Spinner SpinnerYear;
    Spinner SpinnerMonth;
    Button validate;
    Button buttonBuy;
    Button buttonCancel;
    LinearLayout cardlayout;
    LinearLayout buylayout;
    AlertDialog errorDialog;
    AlertDialog networkDialog;
    ProgressDialog loadingDialog;
    AlertDialog successDialog;
    AlertDialog errorBuyDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card);

        //Set action bar fields
        getSupportActionBar().setTitle(PiggateUser.getEmail());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Views
        EditCardNumber =  (EditText) findViewById(R.id.cardNumber);
        EditCVC =  (EditText) findViewById(R.id.CVC);
        SpinnerYear = (Spinner) findViewById(R.id.spinerYear);
        SpinnerMonth = (Spinner) findViewById(R.id.spinerMonth);
        validate = (Button) findViewById(R.id.buttonValidate);
        buttonBuy = (Button) findViewById(R.id.buttonBuy);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        cardlayout = (LinearLayout) findViewById(R.id.cardlayout);
        buylayout = (LinearLayout) findViewById(R.id.buyLayout);

        //Animations
        final Animation slidetoLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidetoleft);
        final Animation slidetoRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidetoright);
        final Animation slidefromRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidefromright);
        final Animation slidefromLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidefromleft);

        //Validation Error AlertDialog
        errorDialog = new AlertDialog.Builder(buyOfferActivity.this).create();
        errorDialog.setTitle("Validation error");
        errorDialog.setMessage("The credit card is not valid");
        errorDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Network Error AlertDialog
        networkDialog = new AlertDialog.Builder(this).create();
        networkDialog.setTitle("Network error");
        networkDialog.setMessage("Network is not working");
        networkDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Successful payment AlertDialog
        successDialog = new AlertDialog.Builder(this).create();
        successDialog.setTitle("Payment completed");
        successDialog.setMessage("Your payment has been completed");
        successDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent slideactivity = new Intent(getApplicationContext(), Activity_Logged.class);
                Bundle bndlanimation =
                        ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.flip1, R.anim.flip2).toBundle();
                getApplicationContext().startActivity(slideactivity, bndlanimation);
            }
        });

        //Buy error AlertDialog
        errorBuyDialog = new AlertDialog.Builder(this).create();
        errorBuyDialog.setTitle("Payment failed");
        errorBuyDialog.setMessage("There is an error with your payment");
        errorBuyDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                buylayout.startAnimation(slidetoRight);
                buylayout.setVisibility(View.GONE);
                cardlayout.setVisibility(View.VISIBLE);
                cardlayout.setAnimation(slidefromLeft);
            }
        });

        //(Provisional) set the default fields of the credit card
        EditCardNumber.setText("4242424242424242");
        EditCVC.setText("123");

        piggate = new Piggate(this); //Initialize Piggate Object

        //OnClick listener for the validate button
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Validate the credit card with Stripe
                if (checkInternetConnection() == true) {
                    // Credit card for testing: ("4242-4242-4242-4242", 12, 2016, "123")
                    if (piggate.validateCard(EditCardNumber.getText().toString(),
                            Integer.parseInt(SpinnerMonth.getSelectedItem().toString()),
                            Integer.parseInt(SpinnerYear.getSelectedItem().toString()),
                            EditCVC.getText().toString(),
                            buyOfferActivity.this,
                            "Validating",
                            "Wait while the credit card is validated")) {

                        cardlayout.startAnimation(slidetoLeft);
                        cardlayout.setVisibility(LinearLayout.GONE);
                        buylayout.setVisibility(View.VISIBLE);
                        buylayout.startAnimation(slidefromRight);
                    } else
                        errorDialog.show();
                }else{
                    networkDialog.show();
                }
            }
        });

        //OnClick listener for the buy button
        buttonBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Do the buy request to the server
                RequestParams params = new RequestParams();
                params.put("stripeToken", piggate.get_creditCards().get(piggate.get_creditCards().size()-1).getTokenID().toString());
                params.put("amount", getIntent().getExtras().getString("offerPrice"));
                params.put("offerID", getIntent().getExtras().getString("offerID"));

                loadingDialog = ProgressDialog.show(v.getContext(), "Payment", "Wait while the payment is finished", true);

                //Do the buy request to the server (The server do the payment with Stripe)
                piggate.BuyRequest(params).setListenerRequest(new Piggate.PiggateCallBack() {

                    //onComplete method for JSONObject
                    @Override
                    public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                        Log.d("TEST", "Buy request done!!");
                        loadingDialog.dismiss();
                        successDialog.show();

                        Intent slideactivity = new Intent(buyOfferActivity.this, Activity_Logged.class);
                        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromleft, R.anim.slidetoright).toBundle();
                        startActivity(slideactivity, bndlanimation);
                    }

                    //onError method for JSONObject
                    @Override
                    public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                        Log.d("TEST", "Buy request fail!!");
                        loadingDialog.dismiss();
                        errorDialog.show();

                        buylayout.startAnimation(slidetoRight);
                        buylayout.setVisibility(View.GONE);
                        cardlayout.setVisibility(View.VISIBLE);
                        cardlayout.setAnimation(slidefromLeft);
                    }

                    //onComplete method for JSONArray
                    @Override
                    public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {
                        Log.d("TEST", "Buy request done!!");
                        loadingDialog.dismiss();
                        successDialog.show();

                        Intent slideactivity = new Intent(buyOfferActivity.this, Activity_Logged.class);
                        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromleft, R.anim.slidetoright).toBundle();
                        startActivity(slideactivity, bndlanimation);
                    }

                    //onError method for JSONArray
                    @Override
                    public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {
                        Log.d("TEST", "Buy request done!!");
                        loadingDialog.dismiss();
                        errorDialog.show();

                        buylayout.startAnimation(slidetoRight);
                        buylayout.setVisibility(View.GONE);
                        cardlayout.setVisibility(View.VISIBLE);
                        cardlayout.setAnimation(slidefromLeft);
                    }
                }).exec();
            }
        });

        //OnClick listener for the cancel button (Go back to the credit card)
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buylayout.startAnimation(slidetoRight);
                buylayout.setVisibility(View.GONE);
                cardlayout.setVisibility(View.VISIBLE);
                cardlayout.setAnimation(slidefromLeft);
            }
        });
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
                backButton();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //onBackPressed method for the activity
    @Override
    public void onBackPressed(){
        backButton();
    }

    //Action for the back button (Go back to Activity_Logged)
    public void backButton(){
        Intent slideactivity = new Intent(buyOfferActivity.this, Activity_Logged.class);
        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle bndlanimation =
                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromleft, R.anim.slidetoright).toBundle();
        startActivity(slideactivity, bndlanimation);
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
