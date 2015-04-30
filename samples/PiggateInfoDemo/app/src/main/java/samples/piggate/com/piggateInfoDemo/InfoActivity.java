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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;
import com.melnykov.fab.FloatingActionButton;
import com.piggate.sdk.Piggate;
import com.piggate.sdk.PiggateUser;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class InfoActivity extends ActionBarActivity {

    TextView title; //Title of the content
    TextView description; //Description of the content
    SmartImageView image; //Image of the content
    FloatingActionButton videoButton; //Button for launching a video
    ProgressDialog loadingDialog;
    AlertDialog errorDialog;
    AlertDialog networkErrorDialog;

    //Method onCreate of the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artwork);

        //Set action bar fields
        getSupportActionBar().setTitle(PiggateUser.getEmail());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        title = (TextView) findViewById(R.id.InfoTitle);
        description = (TextView) findViewById(R.id.InfoDescription);
        image = (SmartImageView) findViewById(R.id.InfoImage);
        videoButton = (FloatingActionButton) findViewById(R.id.videoButton);

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

        title.setText(getIntent().getExtras().getString("infoTitle"));
        description.setText(getIntent().getExtras().getString("infoDescription"));
        image.setImageUrl(getIntent().getExtras().getString("infoImageURL"));

        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInternetConnection() == true){ //If internet is working

                    //Start the video here
                    Intent slideactivity = new Intent(getApplicationContext(), VideoViewActivity.class);
                    slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    //Information about selected content
                    slideactivity.putExtra("infoTitle", getIntent().getExtras().getString("infoTitle"));
                    slideactivity.putExtra("infoDescription", getIntent().getExtras().getString("infoDescription"));
                    slideactivity.putExtra("infoImageURL", getIntent().getExtras().getString("infoImageURL"));
                    slideactivity.putExtra("infoVideoURL", getIntent().getExtras().getString("infoVideoURL"));
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slidefromright, R.anim.slidetoleft).toBundle();
                    getApplicationContext().startActivity(slideactivity, bndlanimation);
                }
                else{
                    //If internet conexion is not working displays an error message
                    networkErrorDialog.show();
                }
            }
        });
    }

    //onBackPressed method for the activity
    @Override
    public void onBackPressed(){
        backButton();
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

    //Action for the back button (Go back to Activity_Logged)
    public void backButton(){
        Intent slideactivity = new Intent(InfoActivity.this, Activity_Logged.class);
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
