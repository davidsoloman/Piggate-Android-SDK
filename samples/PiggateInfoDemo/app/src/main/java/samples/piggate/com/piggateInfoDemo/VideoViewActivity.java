package samples.piggate.com.piggateInfoDemo;

 import android.app.ActivityOptions;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v7.app.ActionBarActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.MediaController;
 import android.widget.Toast;
 import android.widget.VideoView;

 import com.piggate.sdk.PiggateUser;

public class VideoViewActivity extends ActionBarActivity {

     private VideoView videoView;
     private MediaController mController;
     private Uri uriVideo;
    AlertDialog errorDialog;
    AlertDialog networkErrorDialog;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_video_view);
         videoView = (VideoView) findViewById(R.id.videoView);
         mController = new MediaController(this);
         videoView.setMediaController(mController);
         videoView.requestFocus();

         //Set action bar fields
         getSupportActionBar().setTitle(PiggateUser.getEmail());
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setDisplayShowHomeEnabled(true);

         errorDialog = new AlertDialog.Builder(this).create();
         errorDialog.setTitle("Video error");
         errorDialog.setMessage("There is an error playing the video");
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

         if(checkInternetConnection()==true){
             if(getIntent().hasExtra("infoVideoURL"))
                 startPlaying(getIntent().getExtras().getString("infoVideoURL").toString());
             else
                 errorDialog.show(); //Show dialog error
         }
         else {
             networkErrorDialog.show(); //Show dialog error
         }
     }

     void startPlaying(String url) {
         MediaController mediaController = new MediaController(this);
         mediaController.setAnchorView(videoView);
         videoView.setMediaController(mediaController);
         uriVideo = Uri.parse(url);
         videoView.setVideoURI(uriVideo);
         videoView.requestFocus();
         videoView.start();
     }

    //onBackPressed method for the activity
    @Override
    public void onBackPressed(){
        backButton();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
    }

    //Create the options menu for action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    //Action for the back button (Go back to Activity_Logged)
    public void backButton(){
        Intent slideactivity = new Intent(VideoViewActivity.this, InfoActivity.class);
        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //Information about selected content
        slideactivity.putExtra("infoTitle", getIntent().getExtras().getString("infoTitle"));
        slideactivity.putExtra("infoDescription", getIntent().getExtras().getString("infoDescription"));
        slideactivity.putExtra("infoImageURL", getIntent().getExtras().getString("infoImageURL"));
        slideactivity.putExtra("infoVideoURL", getIntent().getExtras().getString("infoVideoURL"));
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
