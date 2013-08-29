package com.gsma.android.networkapidemo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.gsma.android.networkapidemo.R;

/*
 * initiate the process of sign-in using the OperatorID API. 
 * the sign-in process is based on the user accessing the operator portal
 * through a browser. It is based on OpenID 2
 * 
 * details on using an external browser are not finalised therefore at the moment
 * this uses a WebView
 */
@SuppressLint("SetJavaScriptEnabled")
public class DiscoveryCompleteActivity extends Activity {
	private static final String TAG = "DiscoveryCompleteActivity";

	String operatoridauthenticateuri; // the authenticateuri value returned from the
	// discovery process - this is the endpoint for
	// OperatorID
	String operatorid; // the uri to an XML file providing information about the
	// OperatorID service
	String paymentcharge; // the uri to the payment::charge endpoint
	String paymenttransactionstatus; // the uri to the payment::transactionstatus endpoint
	
	DiscoveryCompleteActivity discoveryCompleteActivityInstance; // saved copy of this instance -
	// needed when sending an intent

	/*
	 * method called when this activity is created - handles the receiving of
	 * endpoint parameters and setting up the WebView
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		discoveryCompleteActivityInstance = this;
		setContentView(R.layout.activity_discovery_complete);

		/*
		 * receive the endpoint information (post discovery) and store the
		 * received values
		 */
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			operatoridauthenticateuri = extras.getString("operatoridauthenticateuri");
			operatorid = extras.getString("operatoriduri");
			paymentcharge = extras.getString("paymentcharge");
			paymenttransactionstatus = extras.getString("paymenttransactionstatus");
		}
		
		CheckBox discoveryOperatorID = (CheckBox) findViewById(R.id.discoveryOperatorID);
		discoveryOperatorID.setChecked(operatorid!=null);

		CheckBox discoveryPaymentCharge = (CheckBox) findViewById(R.id.discoveryPaymentCharge);
		discoveryPaymentCharge.setChecked(paymentcharge!=null);

		CheckBox discoveryPaymentTransactionStatus = (CheckBox) findViewById(R.id.discoveryPaymentTransactionStatus);
		discoveryPaymentTransactionStatus.setChecked(paymenttransactionstatus!=null);

		Button testOperatorID = (Button) findViewById(R.id.testOperatorID);
		testOperatorID.setVisibility(operatorid!=null?View.VISIBLE:View.INVISIBLE);
	
		Button testPaymentCharge = (Button) findViewById(R.id.testPaymentCharge);
		testPaymentCharge.setVisibility(paymentcharge!=null?View.VISIBLE:View.INVISIBLE);

	}

	/*
	 * when this activity starts
	 * 
	 * @see android.app.Activity#onStart()
	 */
	public void onStart() {
		super.onStart();

	}

	public void testOperatorID(View view) {
		Log.d(TAG, "testOperatorID called");
		
		Intent intent = new Intent(
				MainActivity.mainActivityInstance,
				SignInActivity.class);
		intent.putExtra("authenticateuri",
				operatorid);
		MainActivity.mainActivityInstance
				.startActivity(intent);
	}

	public void testPaymentCharge(View view) {
		Log.d(TAG, "testPaymentCharge called");
		
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context,
				getString(R.string.discoveryNotYetImplemented),
				Toast.LENGTH_LONG);
		toast.show();

	}

}
