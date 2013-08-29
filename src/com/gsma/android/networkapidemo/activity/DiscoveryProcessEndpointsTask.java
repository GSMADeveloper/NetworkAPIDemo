package com.gsma.android.networkapidemo.activity;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.gsma.android.networkapidemo.utils.HttpUtils;
import com.gsma.android.networkapidemo.utils.JsonUtils;

/*
 * read a set of endpoints from a source InputStream and initiate OperatorID sign-in
 */
class DiscoveryProcessEndpointsTask extends AsyncTask<Void, Void, JSONObject> {
	private static final String TAG = "DiscoveryProcessEndpointsTask";

	String contentType; // Content-Type header
	HttpResponse httpResponse; // the HttpResponse object
	InputStream inputStream; // InputStream from the HttpResponse

	/*
	 * constructor requires the contentType header, HttpResponse and InputStream
	 * from the HttpResponse
	 */
	public DiscoveryProcessEndpointsTask(String contentType,
			HttpResponse httpResponse, InputStream inputStream) {
		this.contentType = contentType;
		this.httpResponse = httpResponse;
		this.inputStream = inputStream;
	}

	/*
	 * the background task firstly identifies this is a JSON response, and
	 * extracts the OperatorID endpoint from the response. it then initiates
	 * OperatorID sign-in
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected JSONObject doInBackground(Void... params) {
		JSONObject errorResponse = null;

		try {
			/*
			 * extract the text content from the HttpResponse body
			 */
			String contents = HttpUtils.getContentsFromInputStream(inputStream);

			Log.d(TAG, "Read " + contents);

			/*
			 * check for the presence of JSON content type
			 */
			if (contentType != null
					&& contentType.toLowerCase().startsWith("application/json")) {
				Log.d(TAG, "Read JSON content");

				/*
				 * convert the text format of JSON
				 */
				Object rawJSON = JsonUtils
						.convertContent(contents, contentType);
				if (rawJSON != null) {
					Log.d(TAG, "Have read the json data");

					/*
					 * there should be a JSONObject at the top level
					 */
					if (rawJSON instanceof JSONObject) {
						JSONObject json = (JSONObject) rawJSON;

						/*
						 * the HTTP status code associated with the response
						 * should be 200 (OK)
						 */
						if (httpResponse.getStatusLine().getStatusCode() == 200) {

							/*
							 * extract the Time To Live value - though unused in
							 * this case it may be used to cache endpoint
							 * information
							 */
							String ttl = JsonUtils.getJSONStringElement(json,
									"ttl");
							Log.d(TAG, "ttl = " + ttl);

							/*
							 * retrieve the top level objects for the operatorid and payment functions
							 */
							JSONObject operatorid = JsonUtils.getJSONObject(
									JsonUtils.getJSONObject(JsonUtils
											.getJSONObject(json, "response"),
											"apis"), "operatorid");

							JSONObject payment = JsonUtils.getJSONObject(
									JsonUtils.getJSONObject(JsonUtils
											.getJSONObject(json, "response"),
											"apis"), "payment");

							/*
							 * the following fields are expected for the
							 * OperatorID endpoint
							 */
							String operatoridauthenticateuri = JsonUtils
									.getJSONStringElement(operatorid,
											"authenticate-uri");
							String operatoriduri = JsonUtils.getJSONStringElement(
									operatorid, "uri");

							Log.d(TAG, "operatorid authenticateuri = " + operatoridauthenticateuri);
							Log.d(TAG, "operatorid uri = " + operatoriduri);

							/*
							 * retrieve the following endpoints for payment 
							 */
							String paymentcharge = JsonUtils
									.getJSONStringElement(payment,
											"charge");
							String paymenttransactionstatus = JsonUtils
									.getJSONStringElement(payment,
											"transactionstatus");

							Log.d(TAG, "payment charge = " + paymentcharge);
							Log.d(TAG, "payment transactionstatus = " + paymenttransactionstatus);

							/*
							 * switch to the discovery complete screen
							 */
							if (ttl != null) {
								Intent intent = new Intent(
										MainActivity.mainActivityInstance,
										DiscoveryCompleteActivity.class);
								intent.putExtra("operatoridauthenticateuri",
										operatoridauthenticateuri);
								intent.putExtra("operatoriduri", operatoriduri);
								intent.putExtra("paymentcharge",
										paymentcharge);
								intent.putExtra("paymenttransactionstatus", paymenttransactionstatus);
								MainActivity.mainActivityInstance
										.startActivity(intent);
							}

							/*
							 * an HTTP status code of 400 or over indicates an
							 * error
							 */
						} else if (httpResponse.getStatusLine().getStatusCode() >= 400) {
							errorResponse = json;
						}

					} // JSONObject test
				} // have converted into an object
			} // content type is application/json

			/*
			 * convert the various internal error types to displayable errors
			 */
		} catch (IOException e) {
			errorResponse = JsonUtils.simpleError("IOException",
					"IOException - " + e.getMessage());
		} catch (JSONException e) {
			errorResponse = JsonUtils.simpleError("JSONException",
					"JSONException - " + e.getMessage());
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
