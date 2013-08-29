package com.gsma.android.networkapidemo.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.gsma.android.networkapidemo.utils.HttpUtils;
import com.gsma.android.networkapidemo.utils.JsonUtils;

/*
 * this is a background task which makes an initial connection to the discovery service - it will handle a variety of initial response types
 */
class InitialDiscoveryTask extends AsyncTask<Void, Void, JSONObject> {
	private static final String TAG = "InitialDiscoveryTask";

	String serviceUri; // the URI of the discovery service
	String consumerKey; // the consumerKey of the application - used to
	// authorize access
	String mcc; // mobile country code of the user's subscription
	String mnc; // mobile network code of the user's subscription
	String userAgent; // browser user agent string to include in requests

	/*
	 * standard constructor - receives information from MainActivity
	 */
	public InitialDiscoveryTask(String serviceUri, String consumerKey,
			String mcc, String mnc, String userAgent) {
		this.serviceUri = serviceUri;
		this.consumerKey = consumerKey;
		this.mcc = mcc;
		this.mnc = mnc;
		this.userAgent = userAgent;
	}

	/*
	 * the doInBackground function does the actual background processing
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected JSONObject doInBackground(Void... params) {
		JSONObject errorResponse = null;

		Log.d(TAG, "Started discovery process via " + serviceUri);

		Log.d(TAG, "Using MCC=" + mcc);
		Log.d(TAG, "Using MNC=" + mnc);

		/*
		 * sets up the HTTP request with a redirect_uri parameter - in practice
		 * we're looking for a discovery token added to the redirect_uri
		 */
		String phase1Uri = serviceUri + "?redirect_uri=http://gsma.com/oneapi";

		/*
		 * if there are Mobile Country Code and Mobile Network Code values add
		 * as HTTP headers
		 */
		if (mcc != null && mnc != null) {
			phase1Uri = phase1Uri + "&mcc_mnc="+mcc+"_"+mnc;
		}

		HttpGet httpRequest = new HttpGet(phase1Uri);
		
		/* if able to supply mcc & mnc set the accept type header for a JSON response */
		if (mcc != null && mnc != null) {
			httpRequest.addHeader("Accept", "application/json");
		} else {
			httpRequest.addHeader("Accept", "text/html");
		}

		try {

			/*
			 * get an instance of an HttpClient, the helper makes sure HTTP
			 * Basic Authorization uses the consumer Key
			 */
			HttpClient httpClient = HttpUtils.getHttpClient(phase1Uri,
					consumerKey);
			HttpParams httpParams = httpRequest.getParams();
			httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS,Boolean.FALSE);
			httpRequest.setParams(httpParams);
			
			/*
			 * send the HTTP POST request and get the response
			 */
			Log.d(TAG, "Making " + httpRequest.getMethod() + " request to "
					+ httpRequest.getURI());
			HttpResponse httpResponse = httpClient.execute(httpRequest);
			
			Log.d(TAG, "Request completed with status="+httpResponse.getStatusLine().getStatusCode());

			/*
			 * obtain the headers from the httpResponse. Content-Type and
			 * Location are particularly required
			 */
			HashMap<String, String> headerMap = HttpUtils
					.getHeaders(httpResponse);
			String contentType = headerMap.get("content-type");
			String location = headerMap.get("location");

			/*
			 * the status code from the HTTP response is also needed in
			 * processing
			 */
			int statusCode = httpResponse.getStatusLine().getStatusCode();

			Log.d(TAG, "status=" + statusCode + " CT=" + contentType + " Loc="
					+ location + " JSON?" + HttpUtils.isJSON(contentType)
					+ " HTML?" + HttpUtils.isHTML(contentType));

			/*
			 * process a HTTP 200 (OK) response
			 */
			if (statusCode == 200) {
				/*
				 * if the response content type is json this will contain the
				 * endpoint information
				 */
				if (HttpUtils.isJSON(contentType)) {
					/*
					 * obtain the response body (via the InputStream from the
					 * httpResponse)
					 */
					HttpEntity httpEntity = httpResponse.getEntity();
					InputStream is = httpEntity.getContent();

					/*
					 * read the endpoints and initiate OperatorID sign-in via
					 * background task
					 */
					DiscoveryProcessEndpointsTask readEndpoints = new DiscoveryProcessEndpointsTask(
							contentType, httpResponse, is);
					readEndpoints.execute();

					/*
					 * If HTML content has been returned some form of
					 * intermediate page has been provided, reload the content
					 * in a webview so that the user can interact with the
					 * website
					 */
				} else if (HttpUtils.isHTML(contentType)) {
					Log.d(TAG,
							"Have HTML content - needs to be handled through the browser");

					/*
					 * create a new Intent which will load the current URI in a
					 * WebView before continuing with the discovery process
					 */
					Intent intent = new Intent(
							MainActivity.mainActivityInstance,
							DisplayDiscoveryWebsiteActivity.class);
					intent.putExtra("uri", phase1Uri);
					intent.putExtra("consumerKey", consumerKey);
					intent.putExtra("serviceUri", serviceUri);
					MainActivity.mainActivityInstance.startActivity(intent);
				}

				/*
				 * HTTP status code 302 is a redirect - there should also be a
				 * location header
				 */
			} else if (statusCode == 302 && location != null) {

				Log.d(TAG, "Redirect requested to " + location);

				/*
				 * check for the presence of a discovery token. if present
				 * extract the value
				 */
				if (location.indexOf("discovery_token") > -1) {
					String[] parts = location.split("discovery_token", 2);
					if (parts.length == 2) {
						String discovery_token = parts[1].replaceFirst("=", "")
								.trim();
						Log.d(TAG, "discovery_token = " + discovery_token);

						/*
						 * process the discovery token provided - through a
						 * further background task which will use the discovery
						 * token to fetch the endpoints
						 */
						ProcessDiscoveryTokenTask processTask = new ProcessDiscoveryTokenTask(
								discovery_token, consumerKey, serviceUri);
						processTask.execute();

					} // have a discovery token pair
				} else { // no discovery token component in the URL
					/*
					 * if there is a redirect but no discovery token there is
					 * something trying to redirect the users' browser - so
					 * handle this in a WebView before continuing
					 */
					Intent intent = new Intent(
							MainActivity.mainActivityInstance,
							DisplayDiscoveryWebsiteActivity.class);
					intent.putExtra("uri", location);
					intent.putExtra("consumerKey", consumerKey);
					intent.putExtra("serviceUri", serviceUri);
					MainActivity.mainActivityInstance.startActivity(intent);
				}
				/*
				 * any HTTP status code 400 or above is an error
				 */
			} else if (statusCode >= 400) {
				/*
				 * read the contents of the response body
				 */
				HttpEntity httpEntity = httpResponse.getEntity();
				InputStream is = httpEntity.getContent();
				String contents = HttpUtils.getContentsFromInputStream(is);

				/*
				 * if the response content type is JSON return as the error
				 */
				if (HttpUtils.isJSON(contentType)) {
					Object rawJSON = JsonUtils.convertContent(contents,
							contentType);
					if (rawJSON != null && rawJSON instanceof JSONObject) {
						errorResponse = (JSONObject) rawJSON;
					}
				} else {
					/*
					 * non JSON data - just return the HTTP status code
					 */
					errorResponse = JsonUtils.simpleError("HTTP " + statusCode,
							"HTTP " + statusCode);
				}

			} // is this request a redirection?

			/*
			 * convert the various internal error types to displayable errors
			 */
		} catch (UnsupportedEncodingException e) {
			errorResponse = JsonUtils.simpleError(
					"UnsupportedEncodingException",
					"UnsupportedEncodingException - " + e.getMessage());
		} catch (ClientProtocolException e) {
			Log.d(TAG, "ClientProtocolException="+e.getMessage());
			errorResponse = JsonUtils.simpleError("ClientProtocolException",
					"ClientProtocolException - " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "IOException="+e.getMessage());
			errorResponse = JsonUtils.simpleError("IOException",
					"IOException - " + e.getMessage());
		} catch (JSONException e) {
			Log.d(TAG, "JSONException="+e.getMessage());
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
