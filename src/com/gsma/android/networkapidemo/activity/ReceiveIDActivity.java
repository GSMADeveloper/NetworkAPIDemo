package com.gsma.android.networkapidemo.activity;

import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gsma.android.networkapidemo.R;
import com.gsma.android.networkapidemo.utils.ParameterList;

public class ReceiveIDActivity extends Activity {
	private static final String TAG = "ReceiveIDActivity";

	ReceiveIDActivity receiveIDActivityInstance = null;

	String rAssocHandle;
	String rIdentity;
	String rMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		receiveIDActivityInstance = this;
		setContentView(R.layout.activity_signin_complete);
		Log.d(TAG, "onCreate called ");

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Set<String> keyset = extras.keySet();
			if (keyset != null) {
				for (String key : keyset) {
					Object v = extras.get(key);
					if (v != null && v instanceof String) {
						Log.d(TAG, "Received " + key + " = " + v);
					} else {
						Log.d(TAG, "Received " + key + " of type "
								+ v.getClass().getName());
					}
				}
			}
			// rAssocHandle = extras.getString("rAssocHandle");
			// rIdentity = extras.getString("rIdentity");
			// rMode = extras.getString("rMode");
			// Log.d(TAG,"received rAssocHandle = "+rAssocHandle);
			// Log.d(TAG,"received rIdentity = "+rIdentity);
			// Log.d(TAG,"received rMode = "+rMode);
			//
			// TextView signInCompleteHeading=(TextView)
			// findViewById(R.id.signInCompleteHeading);
			// TextView signInCompleteInfo=(TextView)
			// findViewById(R.id.signInCompleteInfo);
			// TextView signInCompleteID=(TextView)
			// findViewById(R.id.signInCompleteID);
			//
			// if ("id_res".equalsIgnoreCase(rMode)) {
			// signInCompleteHeading.setText(getString(R.string.signInCompleteHeading));
			// signInCompleteInfo.setText(getString(R.string.signInCompleteInfo));
			// signInCompleteID.setVisibility(View.VISIBLE);
			// signInCompleteID.setText(rIdentity);
			// } else {
			// signInCompleteHeading.setText(getString(R.string.signInRejectHeading));
			// signInCompleteInfo.setText(getString(R.string.signInRejectInfo));
			// signInCompleteID.setVisibility(View.INVISIBLE);
			// }
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart called ");

		Uri data = getIntent().getData(); 
		String queryPart=data.getQuery();
		
		Log.d(TAG, "Received "+queryPart);
		
		ParameterList parameters=ParameterList.getKeyValuesFromUrl(queryPart);
		
		rAssocHandle = parameters.getValue("openid.assoc_handle");
		rIdentity = parameters.getValue("openid.claimed_id");
		rMode = parameters.getValue("openid.mode");
		
		String[] email=parameters.getAttribute("http://openid.net/schema/contact/internet/email");
		String[] country=parameters.getAttribute("http://openid.net/schema/contact/country/home");
		String[] firstName=parameters.getAttribute("http://openid.net/schema/namePerson/first");
		String[] lastName=parameters.getAttribute("http://openid.net/schema/namePerson/last");
		String[] language=parameters.getAttribute("http://openid.net/schema/language/pref");

		Log.d(TAG, "received AssocHandle = " + rAssocHandle);
		Log.d(TAG, "received Identity = " + rIdentity);
		Log.d(TAG, "received Mode = " + rMode);

		if (country!=null && country.length>0) Log.d(TAG, "Country = "+country[0]);
		if (email!=null && email.length>0) Log.d(TAG, "Email = "+email[0]);
		if (firstName!=null && firstName.length>0) Log.d(TAG, "firstName = "+firstName[0]);
		if (lastName!=null && lastName.length>0) Log.d(TAG, "lastName = "+lastName[0]);
		if (language!=null && language.length>0) Log.d(TAG, "language = "+language[0]);

		/*
		 * locate the various screen elements. heading, information text and
		 * the user ID field
		 */
		TextView signInCompleteHeading = (TextView) findViewById(R.id.signInCompleteHeading);
		TextView signInCompleteInfo = (TextView) findViewById(R.id.signInCompleteInfo);
		TextView signInCompleteID = (TextView) findViewById(R.id.signInCompleteID);
		
		TextView signInCompleteEmailLabel = (TextView) findViewById(R.id.signInCompleteEmailLabel);
		TextView signInCompleteEmailValue = (TextView) findViewById(R.id.signInCompleteEmailValue);
		
		signInCompleteEmailLabel.setVisibility(View.INVISIBLE);
		signInCompleteEmailValue.setVisibility(View.INVISIBLE);

		/*
		 * successful identification
		 */
		if ("id_res".equalsIgnoreCase(rMode)) {

			/*
			 * set the fixed text to indicate a successful identification,
			 * and display the user identity
			 */
			signInCompleteHeading
					.setText(getString(R.string.signInCompleteHeading));
			signInCompleteInfo
					.setText(getString(R.string.signInCompleteInfo));
			signInCompleteID.setVisibility(View.VISIBLE);
			signInCompleteID.setText(rIdentity);
			
			if (email!=null && email.length>0 && email[0].trim().length()>0) {
				signInCompleteEmailLabel.setVisibility(View.VISIBLE);
				signInCompleteEmailValue.setVisibility(View.VISIBLE);
				signInCompleteEmailValue.setText(email[0]);
			}
			
		} else {

			/*
			 * set the fixed text to indicate the user declined
			 * identification
			 */
			signInCompleteHeading
					.setText(getString(R.string.signInRejectHeading));
			signInCompleteInfo
					.setText(getString(R.string.signInRejectInfo));
			signInCompleteID.setVisibility(View.INVISIBLE);
		}
	}
	
	/*
	 * D/ParameterList(16625): Returned openid.ax.type.ext0 = http://axschema.org/pref/language
D/ParameterList(16625): Returned openid.ax.type.ext1 = http://axschema.org/contact/email
D/ParameterList(16625): Returned openid.ax.type.ext2 = http://axschema.org/contact/country/home
D/ParameterList(16625): Returned openid.ax.type.ext3 = http://axschema.org/namePerson/last
D/ParameterList(16625): Returned openid.ax.type.ext4 = http://axschema.org/namePerson/first
D/ParameterList(16625): Returned openid.ax.value.ext0.1 = FR/fre
D/ParameterList(16625): Returned openid.ax.value.ext1.1 = gsma.developer@yahoo.co.uk
D/ParameterList(16625): Returned openid.ax.value.ext2.1 = FR
D/ParameterList(16625): Returned openid.ax.value.ext3.1 = Developer
D/ParameterList(16625): Returned openid.ax.value.ext4.1 = GSMA

	 */

//
//		/*
//		 * Uri data = getIntent().getData(); String scheme = data.getScheme();
//		 * // "http" String host = data.getHost(); // "twitter.com" List<String>
//		 * params = data.getPathSegments();
//		 */
//	}
	
	/*
	 * go back to the main screen
	 */
	public void home(View view) {
		Intent intent = new Intent(receiveIDActivityInstance,
				MainActivity.class);
		receiveIDActivityInstance.startActivity(intent);

	}

}
