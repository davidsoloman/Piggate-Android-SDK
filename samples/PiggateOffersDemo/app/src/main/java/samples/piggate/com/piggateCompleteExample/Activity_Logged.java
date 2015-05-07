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
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.piggate.sdk.Piggate;
import com.piggate.sdk.PiggateOffers;
import com.piggate.sdk.PiggateUser;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/*
Activity for the logged user. Allows to view the nearby beacon offers
*/
public class Activity_Logged extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    Piggate _piggate; //Object of the Piggate class
    private Timer timer; //Timer for refresh the offer list
    private SwipeRefreshLayout mSwipeLayout; //Layout with refreshing listener
    private RecyclerView mRecyclerView; //Contain the offer list
    private OffersAdapter mAdapter; //Adapter for the offer list elements
    private ArrayList<PiggateOffers> offerList = new ArrayList<>(); //Offer list
    ProgressDialog loadingDialog;
    AlertDialog errorDialog;
    AlertDialog networkErrorDialog;
    AlertDialog successPaymentDialog;
    AlertDialog errorBuyDialog;
    AlertDialog successExchangeDialog;
    AlertDialog errorExchangeDialog;
    int REQUEST_ENABLE_BT = 4623;
    Drawer.Result mDrawer; //Left drawer
    ArrayList<PiggateOffers> exchangeOfferList = new ArrayList<>(); //List of offers to exchange

    //Method onPause of the activity
    //Cancel the timers to suspend the updates when activity is inactive
    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }

    //Method onCreate of the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        _piggate=new Piggate(this,null); //Initialize the Piggate object
        setContentView(R.layout.activity_logged);
        getSupportActionBar().setTitle(PiggateUser.getEmail());

        //Initialize and set all the left navigation drawer fields
        mDrawer = new Drawer()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withHeader(R.layout.drawerheader)
                .addDrawerItems( /* Add drawer items with this method */)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        // Open the exchange activity for the selected item
                        Intent slideactivity = new Intent(Activity_Logged.this, Activity_Exchange.class);
                        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        //Send the current offer information to the exchange activity
                        slideactivity.putExtra("offerName", exchangeOfferList.get(position).getName().toString());
                        slideactivity.putExtra("offerDescription", exchangeOfferList.get(position).getDescription().toString());
                        slideactivity.putExtra("offerImgURL", exchangeOfferList.get(position).getImgURL().toString());
                        slideactivity.putExtra("exchangeID", exchangeOfferList.get(position).getExchangeID().toString());

                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(Activity_Logged.this, R.anim.slidefromright, R.anim.slidetoleft).toBundle();
                        startActivity(slideactivity, bndlanimation);
                    }
                })
                .withSliderBackgroundColor(Color.parseColor("#FFFFFF"))
                .withSelectedItem(-1)
                .build();

        if(Application_Notify.exchangeRequest == true)
            getOffersToExchange(); //Get the offers to exchange and put them into the left drawer list

        //Check if the bluetooth is switched on
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        _piggate.refreshOffers(); //Look for the server offers
        offerList = _piggate.getOffers(); //Get the offers data and put into the lists
        mAdapter= new OffersAdapter(offerList,this); //Update the adapter

        //Initialize recycler view
        mRecyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //Initialize swipe layout
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);

        errorDialog = new AlertDialog.Builder(this).create();
        errorDialog.setTitle("Logout error");
        errorDialog.setMessage("There is an error with the logout");
        errorDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        successPaymentDialog = new AlertDialog.Builder(this).create();
        successPaymentDialog.setTitle("Successful payment");
        successPaymentDialog.setMessage("Open the left panel to exchange your purchased offers");
        successPaymentDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        errorBuyDialog = new AlertDialog.Builder(this).create();
        errorBuyDialog.setTitle("Payment failed");
        errorBuyDialog.setMessage("There is an error with your payment");
        errorBuyDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        successExchangeDialog = new AlertDialog.Builder(this).create();
        successExchangeDialog.setTitle("Offer successfully exchanged");
        successExchangeDialog.setMessage("The offer has been purchased and exchanged");
        successExchangeDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        errorExchangeDialog = new AlertDialog.Builder(this).create();
        errorExchangeDialog.setTitle("Exchange error");
        errorExchangeDialog.setMessage("There is an error with the exchange");
        errorExchangeDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
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

        if(getIntent().hasExtra("payment")) {
            if (getIntent().getExtras().getBoolean("payment") == true) {
                successPaymentDialog.show();
                Application_Notify.exchangeRequest = true;
            }
            else {
                errorBuyDialog.show();
            }
        }else if(getIntent().hasExtra("exchanged")){
            if (getIntent().getExtras().getBoolean("exchanged") == true) {
                successExchangeDialog.show();
            }
            else{
                errorExchangeDialog.show();
            }
        }

        updateUIoffers(); //Refresh the offers for the first time
        //Start the timer task to refresh the offers
        timer = new Timer();
        timer.schedule(new TimerTask() { //Load offers data from the server using a request
            @Override
            public void run() {
                updateUIoffers(); //refresh the recyclerview list
            }
        }, 0, 10000); //Time between calls
    }

    //Method onStart of the activity
    @Override
    public void onStart(){
        super.onStart();
    }

    //Method onStop of the activity
    @Override
    public void onStop(){
        super.onStop();
    }

    //Method onDestroy of the activity
    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    //Method onResume of the activity
    @Override
    protected void onResume(){
        super.onResume();
        updateUIoffers();
    }

    //runOnUiThread for refreshing the offer list of the activity
    void updateUIoffers(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Get the offers data and put into the list
                _piggate.refreshOffers();
                offerList = _piggate.getOffers();
                if(Application_Notify.exchangeRequest == true)
                    getOffersToExchange();
                mAdapter= new OffersAdapter(offerList,getApplicationContext());
                mRecyclerView.setAdapter(mAdapter);
            }
        });
    }

    //Do the logout request and exit to the main activity
    public void logout(){

        if (checkInternetConnection() == true) { //If the internet connection is working

            loadingDialog = ProgressDialog.show(this, "Singing Out", "Wait a few seconds", false);

            //Request a session close for the logged user
            _piggate.RequestCloseSession().setListenerRequest(new Piggate.PiggateCallBack() {

                //Method onComplete for JSONObject
                @Override
                public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {

                    loadingDialog.dismiss();
                    Service_Notify.logout = true;

                    //Go back to the main activity
                    Intent slideactivity = new Intent(Activity_Logged.this, Activity_Main.class);
                    slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromleft, R.anim.slidetoright).toBundle();
                    startActivity(slideactivity, bndlanimation);
                }

                //Method onError for JSONObject
                @Override
                public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                    Service_Notify.logout = false;
                    loadingDialog.dismiss();
                    errorDialog.show();
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

        } else { //If internet conexion is not working displays an error message
            networkErrorDialog.show();
        }
    }

    //Get the offers to exchange and put in into the left drawer list
    public void getOffersToExchange(){
        _piggate.RequestGetExchange().setListenerRequest(new Piggate.PiggateCallBack() {
            @Override
            public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
            }

            @Override
            public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
            }

            @Override
            public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {
                if (data.length() == 0) //If there are no offers to exchange
                    Application_Notify.exchangeRequest = false;
                else { //If there are offers to exchange
                    exchangeOfferList = new ArrayList<>(); //List of offers to exchange
                    for (int i = 0; i < data.length(); i++) {
                        try {
                            exchangeOfferList.add(new PiggateOffers(data.getJSONObject(i))); //Get the exchange offer list
                        } catch (JSONException e) {
                        }
                    }
                }
            }

            @Override
            public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {
            }
        }).exec();

        //Add the offers to exchange to the left drawer
        mDrawer.removeAllItems();
        for(int i=0; i<exchangeOfferList.size(); i++){
            mDrawer.addItem(new PrimaryDrawerItem()
                    .withName(exchangeOfferList.get(i).getName().toString())
                    .withDescription(exchangeOfferList.get(i).getDescription().toString()));
        }
    }

    //onRefresh method for SwipeRefreshLayout
    public void onRefresh() {
        mSwipeLayout.setRefreshing(true);
        //Get the offers data and put into the list
        updateUIoffers();
        mSwipeLayout.setRefreshing(false); //Update the adapter here
    }

    //onBackPressed method for the activity
    @Override
    public void onBackPressed(){
        Service_Notify.logout = false;
        super.onBackPressed();
    }

    //Method onActivityResult of the activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int id = item.getItemId();
        if(id == R.id.logout){
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
