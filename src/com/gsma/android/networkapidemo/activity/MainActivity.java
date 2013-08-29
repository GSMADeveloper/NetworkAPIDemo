package com.gsma.android.networkapidemo.activity;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.gsma.android.networkapidemo.utils.PhoneState;
import com.gsma.android.networkapidemo.utils.PhoneUtils;
import com.gsma.android.networkapidemo.utils.PreferencesUtils;
import com.gsma.android.networkapidemo.R;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	static MainActivity mainActivityInstance = null;
	static String userAgent = null;

	/* the root URI for discovery */
	private static final String DISCOVERYROOTURI = "http://dt.gsmaoneapiexchange.com";

	/*
	 * in the test mode the application will use a default MSISDN/MCC/MNC
	 */
	private static boolean testMode = false;

	/*
	 * has discovery been started - used to avoid making a duplicate request
	 */
	boolean started = false;

	/*
	 * method called when the application first starts.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/*
		 * load defaults from preferences file
		 */
		PreferencesUtils.loadPreferences(this);

		/*
		 * save a copy of the current instance - will be needed later
		 */
		mainActivityInstance = this;

		/*
		 * create a temporary WebView to obtain the user agent string that would
		 * be used by the inbuilt browser - this may be needed
		 */
		WebView fwv = new WebView(mainActivityInstance);
		WebSettings settings = fwv.getSettings();
		userAgent = settings.getUserAgentString();
	}

	/*
	 * on start or return to the main screen reset the screen so that discovery
	 * can be started
	 */
	@Override
	public void onStart() {
		super.onStart();

		/* Reset the text on the start button */
		final TextView startButton = (TextView) findViewById(R.id.startButton);
		startButton.setText(getString(R.string.start));

		/* Reset the flag that stops a duplicate discovery request to be made */
		started = false;
		
		/* Reset the test mode flag */
		testMode = false;

		/*
		 * Set the checkboxes on the main screen so that MCC and MNC are
		 * checked and the test mode checkbox is clear
		 */
		CheckBox mccPrompt = (CheckBox) findViewById(R.id.promptMCC);
		mccPrompt.setChecked(true);

		CheckBox mncPrompt = (CheckBox) findViewById(R.id.promptMNC);
		mncPrompt.setChecked(true);

		CheckBox testModeIndicator = (CheckBox) findViewById(R.id.testMode);
		testModeIndicator.setChecked(false);

		/* Update the phone status */
		checkStatus();
	}

	/*
	 * default method to add a menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/*
	 * get the phone status and display relevant indicators on the home screen
	 */
	public void checkStatus() {

		/*
		 * From the standard Android phone state retrieve values of interest for
		 * display/ discovery
		 */
		PhoneState state = PhoneUtils
				.getPhoneState(
						(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE),
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

		String mcc = state.getMcc(); // Mobile country code
		String mnc = state.getMnc(); // Mobile network code

		boolean connected = state.isConnected(); // Is the device connected to
		// the Internet
		boolean usingMobileData = state.isUsingMobileData(); // Is the device
		// connected using cellular/mobile data
		boolean roaming = state.isRoaming(); // Is the device roaming
		// (international roaming)

		/*
		 * For test mode the MSISDN, MCC and MNC are set from default
		 * preferences
		 */
		if (testMode) {
			mcc = PreferencesUtils.getPreference("DefaultMCC");
			mnc = PreferencesUtils.getPreference("DefaultMNC");
		}

		/* Set the displayed MCC value */
		TextView vMCC = (TextView) findViewById(R.id.valueMCC);
		vMCC.setText(mcc);

		/* Set the displayed MNC value */
		TextView vMNC = (TextView) findViewById(R.id.valueMNC);
		vMNC.setText(mnc);

		/* Set the displayed network status */
		TextView vStatus = (TextView) findViewById(R.id.valueStatus);
		String status = getString(R.string.statusDisconnected);
		if (roaming) {
			status = getString(R.string.statusRoaming);
		} else if (usingMobileData) {
			status = getString(R.string.statusOnNet);
		} else if (connected) {
			status = getString(R.string.statusOffNet);
		}
		vStatus.setText(status);
	}

	/*
	 * handles a restart/ refresh of the discovery process
	 */
	public void restart(View view) {
		/* Reset text on start button */
		final TextView startButton = (TextView) findViewById(R.id.startButton);
		startButton.setText(getString(R.string.start));

		/* Reset the discovery process lock */
		started = false;

		/* Update the phone status */
		checkStatus();
	}

	/*
	 * handles the user toggling test mode
	 */
	public void testModeChanged(View view) {
		/* get the current state of the test mode checkbox */
		final CheckBox testModeIndicator = (CheckBox) findViewById(R.id.testMode);
		testMode = testModeIndicator.isChecked();
		/* reset the discovery process */
		restart(view);
	}

	/*
	 * handler when the user presses the start button - if not currently started
	 * discovery will initiate the discovery process
	 */
	public void startIdentification(View view) {
		/* check that discovery process has not been started already */
		if (!started) {

			Log.d(TAG, "startIdentification called");

			/* get the current phone state */
			PhoneState state = PhoneUtils
					.getPhoneState(
							(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE),
							(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

			/*
			 * retrieve the phone state that assists the discovery process
			 */
			String mcc = state.getMcc();
			String mnc = state.getMnc();

			boolean isConnected = state.isConnected();

			/*
			 * if running in test mode use the defaults from the preferences
			 * file
			 */
			if (testMode) {
				mcc = PreferencesUtils.getPreference("DefaultMCC");
				mnc = PreferencesUtils.getPreference("DefaultMNC");
			}

			/*
			 * if the user has chosen not to use any of the parameters for
			 * discovery set to null. In practice this is not going to happen in
			 * a real application - but useful to show the different parts of
			 * discovery
			 */
			CheckBox mccPrompt = (CheckBox) findViewById(R.id.promptMCC);
			if (!mccPrompt.isChecked())
				mcc = null;

			CheckBox mncPrompt = (CheckBox) findViewById(R.id.promptMNC);
			if (!mncPrompt.isChecked())
				mnc = null;

			Log.d(TAG, "mcc = " + mcc);
			Log.d(TAG, "mnc = " + mnc);
			Log.d(TAG, "isConnected = " + isConnected);

			/*
			 * discovery is only possible of course if the device is connected
			 * to the Internet
			 */
			if (isConnected) {

				/*
				 * discovery has started. Set the text on the start button to
				 * indicate discovery is in progress
				 */
				started = true;

				final TextView startButton = (TextView) findViewById(R.id.startButton);
				startButton.setText(getString(R.string.requesting));

				/*
				 * start the background task which makes the first request to
				 * the discovery service - using the available phone state
				 */
				new InitialDiscoveryTask(DISCOVERYROOTURI
						+ "/v1/discovery/apis",
						PreferencesUtils.getPreference("ConsumerKey"), 
						mcc, mnc, userAgent).execute();

			} else {
				/*
				 * if not connected display an error to the user
				 */
				Context context = getApplicationContext();
				Toast toast = Toast.makeText(context,
						getString(R.string.notConnectedToInternet),
						Toast.LENGTH_LONG);
				toast.show();
			}

		}
	}

	/*
	 * if there is an error any time during discovery it will be displayed via
	 * the displayError function
	 */
	public void displayError(String error, String errorDescription) {
		Toast toast = Toast.makeText(getBaseContext(), errorDescription,
				Toast.LENGTH_LONG);
		toast.show();
	}
}
