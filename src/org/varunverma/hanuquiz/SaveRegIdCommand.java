package org.varunverma.hanuquiz;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;

public class SaveRegIdCommand extends Command {
	
	private String regId;
	
	public SaveRegIdCommand(Invoker caller, String id) {
		super(caller);
		regId = id;
	}

	@Override
	protected void execute(ResultObject result) throws Exception {
		
		try{
			
			Log.v(Application.TAG, "Saving GCM RegId with our Server");
			Application app = Application.getApplicationInstance();
			String packageName = app.context.getPackageName();
			
			// Get Primary Email Id
			Account[] accounts = AccountManager.get(app.context).getAccountsByType("com.google");
			String email = accounts[0].name;
			app.addParameter("EMail", email);
			
			// Create a new HttpClient and Post Header  
			HttpClient httpclient = new DefaultHttpClient();
			String url = "http://varunverma.org/HanuGCM/RegisterDevice.php";
			HttpPost httppost = new HttpPost(url);
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
			nameValuePairs.add(new BasicNameValuePair("package", packageName));
			nameValuePairs.add(new BasicNameValuePair("regid", regId));
			nameValuePairs.add(new BasicNameValuePair("email", email));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			//Execute HTTP Post Request  
			HttpResponse response = httpclient.execute(httppost);
			
			// Open Stream for Reading.
			InputStream is = response.getEntity().getContent();
			
			// Read
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = reader.readLine();
			
			if(line != null && line.toString().contentEquals("Success")){
				// Success
				app.addParameter("RegistrationStatus", "Success");
				Log.v(Application.TAG, "GCM RegId saved successfully on our server");
			}
			
		}
		catch (Exception e){
			// Nothing to do
			Log.w(Application.TAG, "Following error occured while saving GCM RegId with our servers:");
			Log.e(Application.TAG, e.getMessage(), e);
		}
	}
}