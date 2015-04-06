package samples.piggate.com.piggatecard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.piggate.sdk.Piggate;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity{

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
    View cardShadow;
    AlertDialog errorDialog;
    AlertDialog networkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card);

        //Views
        EditCardNumber =  (EditText) findViewById(R.id.cardNumber);
        EditCVC =  (EditText) findViewById(R.id.CVC);
        SpinnerYear = (Spinner) findViewById(R.id.spinerYear);
        SpinnerMonth = (Spinner) findViewById(R.id.spinerMonth);
        validate = (Button) findViewById(R.id.buttonValidate);
        buttonBuy = (Button) findViewById(R.id.buttonBuy);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        cardlayout = (LinearLayout) findViewById(R.id.cardlayout);
        cardShadow = (View) findViewById(R.id.cardShadow);
        buylayout = (LinearLayout) findViewById(R.id.buyLayout);

        errorDialog = new AlertDialog.Builder(MainActivity.this).create();
        errorDialog.setTitle("Validation error");
        errorDialog.setMessage("The credit card is not valid");
        errorDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        networkDialog = new AlertDialog.Builder(this).create();
        networkDialog.setTitle("Network error");
        networkDialog.setMessage("Network is not working");
        networkDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Animations
        final Animation slidetoLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidetoleft);
        final Animation slidetoRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidetoright);
        final Animation slidefromRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidefromright);
        final Animation slidefromLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidefromleft);

        EditCardNumber.setText("4242424242424242");
        EditCVC.setText("123");

        piggate = new Piggate(this);
        piggate.onStart();

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkInternetConnection() == true) {
                    // Credit card for testing: ("4242-4242-4242-4242", 12, 2016, "123")
                    if (piggate.validateCard(EditCardNumber.getText().toString(),
                            Integer.parseInt(SpinnerMonth.getSelectedItem().toString()),
                            Integer.parseInt(SpinnerYear.getSelectedItem().toString()),
                            EditCVC.getText().toString(),
                            MainActivity.this,
                            "Validating",
                            "Wait while the credit card is validated")) {

                        cardlayout.startAnimation(slidetoLeft);
                        cardlayout.setVisibility(LinearLayout.GONE);
                        cardShadow.setVisibility(View.GONE);
                        cardShadow.startAnimation(slidetoLeft);
                        buylayout.setVisibility(View.VISIBLE);
                        buylayout.startAnimation(slidefromRight);
                    } else
                        errorDialog.show();
                }else{
                    networkDialog.show();
                }
            }
        });

        buttonBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Do the buy request to the server
                RequestParams params = new RequestParams();
                params.put("stripeToken", piggate.get_creditCards().get(piggate.get_creditCards().size()-1).getTokenID().toString());
                params.put("amount", "12");
                params.put("offerID", "54cb4e97835f5091502b27d6");

                piggate.BuyRequest(params).setListenerRequest(new Piggate.PiggateCallBack() {
                    @Override
                    public void onComplete(int statusCode, Header[] headers, String msg, JSONObject data) {
                        Log.d("TEST", "Request done!!");
                    }

                    @Override
                    public void onError(int statusCode, Header[] headers, String msg, JSONObject data) {
                        Log.d("TEST", "Request fail!!");
                    }

                    @Override
                    public void onComplete(int statusCode, Header[] headers, String msg, JSONArray data) {
                        Log.d("TEST", "Request done!!");
                    }

                    @Override
                    public void onError(int statusCode, Header[] headers, String msg, JSONArray data) {
                        Log.d("TEST", "Request done!!");
                    }
                }).exec();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buylayout.startAnimation(slidetoRight);
                buylayout.setVisibility(View.GONE);
                cardlayout.setVisibility(View.VISIBLE);
                cardlayout.setAnimation(slidefromLeft);
                cardShadow.setVisibility(View.VISIBLE);
                cardShadow.startAnimation(slidefromLeft);
            }
        });
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
