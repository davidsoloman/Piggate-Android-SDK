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
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.piggate.sdk.Piggate;
import com.piggate.sdk.PiggateBeacon;
import com.piggate.sdk.PiggateInfo;
import com.piggate.sdk.PiggateUser;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/*
Activity for the logged user. Allows to view the nearby beacon offers
*/
public class Activity_Logged extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    Piggate _piggate; //Object of the Piggate class
    private SwipeRefreshLayout mSwipeLayout; //Layout with refreshing listener
    private RecyclerView mRecyclerView; //Contain the content list
    private InfoAdapter mAdapter; //Adapter for the content list elements
    private ArrayList<PiggateInfo> infoList = new ArrayList<PiggateInfo>(); //Content (info) list
    ProgressDialog loadingDialog;
    AlertDialog errorDialog;
    AlertDialog networkErrorDialog;
    int REQUEST_ENABLE_BT = 4623;
    LinearLayout newBeaconsLayout;
    Animation fadeIn, fadeOut;
    int requestBeaconCounter;

    private BroadcastReceiver bReceiver = new BroadcastReceiver() { //Receive service information
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                if (bundle.getBoolean("BeaconDiscovered")) {
                    newBeaconsLayout.setVisibility(LinearLayout.VISIBLE);
                    newBeaconsLayout.startAnimation(fadeIn);
                }
            }
        }
    };

    //Method onCreate of the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        _piggate=new Piggate(this,null); //Initialize the Piggate object
        setContentView(R.layout.activity_logged);
        getSupportActionBar().setTitle(PiggateUser.getEmail());

        startService(new Intent(getApplicationContext(), Service_Notify.class)); //Start the service

        //Initialize recycler view
        mRecyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        newBeaconsLayout = (LinearLayout)findViewById(R.id.newBeaconsLayout);

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

        networkErrorDialog = new AlertDialog.Builder(this).create();
        networkErrorDialog.setTitle("Network error");
        networkErrorDialog.setMessage("There is an error with the network connection");
        networkErrorDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        fadeIn = AnimationUtils.loadAnimation(Activity_Logged.this, R.anim.fadein);
        fadeOut = AnimationUtils.loadAnimation(Activity_Logged.this, R.anim.fadeout);

        //Check if the bluetooth is switched on
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
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
        updateUIlist();
        registerReceiver(bReceiver, new IntentFilter("serviceIntent"));
    }

    //Method onPause of the activity
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(bReceiver);
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

    //Function that search for nearby beacons and request to the server to get the info list
    synchronized public void updateUIlist(){
        final ArrayList<PiggateBeacon> beacons= PiggateBeacon.getPendingBeacons(); //Get the pending nearby beacons
        if(beacons.size()>0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSwipeLayout.setRefreshing(true); //Finish the refresh spinner
                }
            });
            requestBeaconCounter = 0; //Initialize the request counter
            for(int x=0;x<beacons.size();x++){ //Load offers data from the server using a GET request for each iBeacon
                _piggate.RequestInfo(beacons.get(x)).setListenerRequest(new Piggate.PiggateCallBack() {

                    //Method onComplete for JSONObject
                    @Override
                    public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                        //Unused
                    }

                    //Method onError for JSONObject
                    @Override
                    public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                        //Unused
                    }

                    //Method onComplete for JSONArray
                    @Override
                    public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {
                        requestBeaconCounter++; //Increase the request counter
                        if (requestBeaconCounter == beacons.size()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    infoList = _piggate.getInfo(); //Get the info data and put into the lists
                                    mAdapter = new InfoAdapter(infoList, Activity_Logged.this); //Update the adapter
                                    mRecyclerView.setAdapter(mAdapter); //Reset the RecyclerView
                                    mSwipeLayout.setRefreshing(false); //Finish the refresh spinner
                                }
                            });
                        }
                    }

                    //Method onError for JSONArray
                    @Override
                    public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeLayout.setRefreshing(false); //Finish the refresh spinner
                            }
                        });
                    }
                }).exec();
            }
        }
        else{ //If there's no nearby beacons
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    infoList = _piggate.getInfo(); //Get the info data and put into the lists
                    mAdapter = new InfoAdapter(infoList, Activity_Logged.this); //Update the adapter
                    mRecyclerView.setAdapter(mAdapter); //Reset the RecyclerView
                    mSwipeLayout.setRefreshing(false); //Finish the refresh spinner
                }
            });
        }
    }

    //onRefresh method for SwipeRefreshLayout
    public void onRefresh() {
        newBeaconsLayout.setVisibility(LinearLayout.GONE);
        newBeaconsLayout.startAnimation(fadeOut);
        updateUIlist(); //Get the offers data and put into the list
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
