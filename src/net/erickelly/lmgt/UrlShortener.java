package net.erickelly.lmgt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class UrlShortener {

	private static String API_KEY = "AIzaSyCpBbnzQ3gFoMwXJLZTa7ssaV_CLQ-t78o";
	private static String GET_URL = "https://www.googleapis.com/urlshortener/v1/url?key=";
	
	private static String TAG = "URL shortener";
	
	/*
	 * Fetch the shortend url from goo.gl
	 */
	public static String getShortenedUrl(String url) {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(GET_URL + API_KEY);
		StringBuilder builder = new StringBuilder();
		String result = null;
		
		try {
		    // Add data
		    JSONObject data = new JSONObject();
		    data.put("longUrl", url);

		    //passes the results to a string builder/entity
		    StringEntity se = new StringEntity(data.toString());

		    //sets the post request as the resulting string
		    httppost.setEntity(se);
		    //sets a request header so the page receving the request will know what to do with it
		    httppost.setHeader("Accept", "application/json");
		    httppost.setHeader("Content-type", "application/json");

		    // Execute HTTP Post Request
		    HttpResponse response = httpclient.execute(httppost);
		    
		    StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream contents = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(contents));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(TAG, "Failed to download file");
			}
			
			JSONObject jsonObject = new JSONObject(builder.toString());
		    result = jsonObject.getString("id");

		} catch (ClientProtocolException e) {
		    // TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
}
