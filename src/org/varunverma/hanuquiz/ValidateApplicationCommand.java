package org.varunverma.hanuquiz;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ResultObject;

import android.util.Log;

public class ValidateApplicationCommand extends Command {

	public ValidateApplicationCommand(Invoker caller) {
		super(caller);
	}

	@Override
	protected void execute(ResultObject result) throws Exception {
		
		/*
		 * Validate Application.
		 */
		
		Application app = Application.getApplicationInstance();
		
		// Check if already validated.
		String validationTime = app.getSettings().get("ValidationTime");
		if(validationTime == null || validationTime.contentEquals("")){
			validationTime = "1349328720";	// HANU Epoch
		}
		
		Date lastValidationTime = new Date(Long.valueOf(validationTime));
		Date now = new Date();
		
		if(now.getTime() - lastValidationTime.getTime() > 7*24*60*60*1000){
			// We validated more than a week ago !
			
			Log.v(Application.TAG, "Validating for error 420");
			String line = "";
			
			try{
				
				// Create a new HttpClient and Post Header  
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost("http://hanu-droid.varunverma.org/Applications/Validate.php");
				
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
				nameValuePairs.add(new BasicNameValuePair("blogurl", app.appURL));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				
				//Execute HTTP Post Request  
				HttpResponse response = httpclient.execute(httppost);
				
				// Open Stream for Reading.
				InputStream is = response.getEntity().getContent();
				
				// Read
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				line = reader.readLine();
				
			}
			catch (Exception e){
				// Nothing to do.
				Log.w(Application.TAG, "Following error occured while validating for error 420");
				Log.e(Application.TAG, e.getMessage(), e);
			}
			
			if(line.toString().contentEquals("Not Found")){
				// OMG !
				result.setResultCode(420);
				Log.w(Application.TAG, "Validation for error 420 failed. This seems to be an invalid application");
				throw new Exception("This application is not registered with Hanu-Droid. " +
						"Please inform the developer.");
			}
			else if (line.toString().contentEquals("Success")){
				// Success
				app.addParameter("ValidationTime", String.valueOf(now.getTime()));
				Log.v(Application.TAG, "Validation for error 420 was success.");
			}
		}	
	}
}