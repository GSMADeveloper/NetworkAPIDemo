package com.gsma.android.networkapidemo.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.gsma.android.networkapidemo.utils.HttpUtils;
import com.gsma.android.networkapidemo.utils.JsonUtils;

/*
 * this class handles the process in which a discovery token is used to obtain the operator endpoints.
 * a request is made to the Discovery Service to get the endpoints for the given discovery token
 */
class ProcessDiscoveryTokenTask extends AsyncTask<Void, Void, JSONObject> {
	private static final String TAG = "ProcessDiscoveryTokenTask";

	String mccmnc; // the discovery token obtained previously from the
	// Discovery Service
	String consumerKey; // API key for the application
	String serviceUri; // base URI for the DiscoveryAPI

	/*
	 * constructor requires a mccmnc from a previous stage of
	 * interacting with the Discovery Service, the application API ConsumerKey
	 * and the base URI of the DiscoveryAPI service
	 */
	public ProcessDiscoveryTokenTask(String mccmnc, String consumerKey,
			String serviceUri) {
		this.mccmnc = mccmnc;
		this.consumerKey = consumerKey;
		this.serviceUri = serviceUri;
	}

	/*
	 * the doInBackground function does the actual background processing
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected JSONObject doInBackground(Void... params) {
		JSONObject errorResponse = null;

		try {

			/*
			 * the DiscoveryAPI base URI is used to request endpoints using the
			 * discovery token
			 */
			
			//http://dt.gsmaoneapiexchange.com/v1/discovery/apis?redirect_uri=http://gsma.com/oneapi&mcc_mnc=262_01
			String phase2Uri = serviceUri + "?mcc_mnc="
					+ mccmnc;
			Log.d(TAG, "mccmnc = " + mccmnc);
			Log.d(TAG, "phase2Uri = " + phase2Uri);

			/*
			 * get an instance of an HttpClient configured to use HTTP Basic
			 * Authorization using the application ConsumerKey
			 */
			HttpClient httpClient = HttpUtils.getHttpClient(phase2Uri,
					consumerKey);

			/*
			 * execute an HTTP GET request on the Discovery Service
			 */
			HttpGet httpRequest = new HttpGet(phase2Uri);
			httpRequest.addHeader("Accept", "application/json");
			HttpResponse httpResponse = httpClient.execute(httpRequest);

			/*
			 * obtain the HTTP Response headers
			 */
			HashMap<String, String> headerMap = HttpUtils
					.getHeaders(httpResponse);

			/*
			 * retrieve the content type of the response (should be JSON), and
			 * get the response body in the form of an InputStream so that the
			 * endpoints can be read and OperatorID process started
			 */
			String contentType = headerMap.get("content-type");

			HttpEntity httpEntity = httpResponse.getEntity();
			InputStream is = httpEntity.getContent();

			/*
			 * read the endpoints and start OperatorID sign-in
			 */
			DiscoveryProcessEndpointsTask readEndpoints = new DiscoveryProcessEndpointsTask(
					contentType, httpResponse, is);
			readEndpoints.execute();

			/*
			 * convert the various internal error types to displayable errors
			 */
		} catch (ClientProtocolException e) {
			errorResponse = JsonUtils.simpleError("ClientProtocolException",
					"ClientProtocolException - " + e.getMessage());
		} catch (IOException e) {
			errorResponse = JsonUtils.simpleError("IOException",
					"IOException - " + e.getMessage());
		}
		return errorResponse;
	}

	/*
	 * on completion of this background task either this task has started the
	 * next part of the process or an error has occurred.
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(JSONObject errorResponse) {
		/*
		 * if there is an error display to the end user
		 */
		if (errorResponse != null) {
			/*
			 * extract the error fields
			 */
			String error = JsonUtils.getJSONStringElement(errorResponse,
					"error");
			String errorDescription = JsonUtils.getJSONStringElement(
					errorResponse, "error_description");
			Log.d(TAG, "error=" + error);
			Log.d(TAG, "error_description=" + errorDescription);

			/*
			 * display to the user
			 */
			MainActivity.mainActivityInstance.displayError(error,
					errorDescription);
		}
	}

}
