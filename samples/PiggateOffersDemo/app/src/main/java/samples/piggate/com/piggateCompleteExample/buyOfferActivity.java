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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.loopj.android.http.RequestParams;
import com.loopj.android.image.SmartImageView;
import com.piggate.sdk.Piggate;
import com.piggate.sdk.PiggateUser;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/*
Activity for buying the selected offer. Do a request to our server for the payment using Stripe
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
    LinearLayout headerLayout;
    LinearLayout cardlayout;
    LinearLayout buylayout;
    AlertDialog errorDialog;
    AlertDialog networkDialog;
    ProgressDialog loadingDialog;
    SmartImageView image;
    Animation slidetoLeft;
    Animation slidetoRight;
    Animation slidefromRight;
    Animation slidefromLeft;

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
        headerLayout = (LinearLayout) findViewById(R.id.headerLayout);
        cardlayout = (LinearLayout) findViewById(R.id.cardlayout);
        buylayout = (LinearLayout) findViewById(R.id.buyLayout);
        image = (SmartImageView) findViewById(R.id.offerImage2);

        //Animations
        slidetoLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidetoleft);
        slidetoRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidetoright);
        slidefromRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidefromright);
        slidefromLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidefromleft);

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

        image.setImageUrl(getIntent().getExtras().getString("offerImgURL")); //Set the offer image from URL
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

                        openBuyLayout();
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
                //Get the latest credit card Stripe token, amount to pay, type of coin and ID
                params.put("stripeToken", piggate.get_creditCards().get(piggate.get_creditCards().size()-1).getTokenID().toString());
                params.put("amount", getIntent().getExtras().getString("offerPrice"));
                params.put("offerID", getIntent().getExtras().getString("offerID"));
                params.put("currency", getIntent().getExtras().getString("offerCurrency"));

                //Loading payment ProgressDialog
                loadingDialog = ProgressDialog.show(v.getContext(), "Payment", "Wait while the payment is finished", true);

                //Do the buy request to the server (The server do the payment with Stripe)
                piggate.RequestBuy(params).setListenerRequest(new Piggate.PiggateCallBack() {

                    //onComplete method for JSONObject
                    @Override
                    public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                        loadingDialog.dismiss();

                        //Go back to the offer list passing the offer attributes (to exchange)
                        Intent slideactivity = new Intent(buyOfferActivity.this, Activity_Logged.class);
                        slideactivity.putExtra("offerID", getIntent().getExtras().getString("offerID"));
                        slideactivity.putExtra("offerName", getIntent().getExtras().getString("offerName"));
                        slideactivity.putExtra("offerDescription", getIntent().getExtras().getString("offerDescription"));
                        slideactivity.putExtra("payment", true);
                        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromleft, R.anim.slidetoright).toBundle();
                        startActivity(slideactivity, bndlanimation);
                    }

                    //onError method for JSONObject
                    @Override
                    public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {

                        //Show an error and go back to the credit card
                        loadingDialog.dismiss();
                        //Go back to the offer list with the variable payment set to false (payment error)
                        Intent slideactivity = new Intent(buyOfferActivity.this, Activity_Logged.class);
                        slideactivity.putExtra("payment", false);
                        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromleft, R.anim.slidetoright).toBundle();
                        startActivity(slideactivity, bndlanimation);
                    }

                    //onComplete method for JSONArray
                    @Override
                    public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {
                        loadingDialog.dismiss();

                        //Go back to the offer list passing the offer attributes (to exchange)
                        Intent slideactivity = new Intent(buyOfferActivity.this, Activity_Logged.class);
                        slideactivity.putExtra("offerID", getIntent().getExtras().getString("offerID"));
                        slideactivity.putExtra("offerName", getIntent().getExtras().getString("offerName"));
                        slideactivity.putExtra("offerDescription", getIntent().getExtras().getString("offerDescription"));
                        slideactivity.putExtra("payment", true);
                        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromleft, R.anim.slidetoright).toBundle();
                        startActivity(slideactivity, bndlanimation);
                    }

                    //onError method for JSONArray (Show an error and go back to the credit card)
                    @Override
                    public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {

                        //Show an error and go back to the credit card
                        loadingDialog.dismiss();
                        //Go back to the offer list with the variable payment set to false (payment error)
                        Intent slideactivity = new Intent(buyOfferActivity.this, Activity_Logged.class);
                        slideactivity.putExtra("payment", false);
                        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromleft, R.anim.slidetoright).toBundle();
                        startActivity(slideactivity, bndlanimation);
                    }
                }).exec();
            }
        });

        //OnClick listener for the cancel button (Go back to the credit card)
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeBuyLayout();
            }
        });
    }

    public void openBuyLayout(){
        cardlayout.startAnimation(slidetoLeft);
        cardlayout.setVisibility(LinearLayout.GONE);
        headerLayout.startAnimation(slidetoLeft);
        headerLayout.setVisibility(LinearLayout.GONE);
        buylayout.setVisibility(View.VISIBLE);
        buylayout.startAnimation(slidefromRight);
    }

    public void closeBuyLayout(){
        buylayout.startAnimation(slidetoRight);
        buylayout.setVisibility(View.GONE);
        cardlayout.setVisibility(View.VISIBLE);
        cardlayout.setAnimation(slidefromLeft);
        headerLayout.setVisibility(View.VISIBLE);
        headerLayout.setAnimation(slidefromLeft);
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
