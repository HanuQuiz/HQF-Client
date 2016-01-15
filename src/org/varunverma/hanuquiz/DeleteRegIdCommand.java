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

import android.util.Log;

public class DeleteRegIdCommand extends Command {
	
	private String regId;
	
	public DeleteRegIdCommand(Invoker caller, String id) {
		super(caller);
		regId = id;
	}

	@Override
	protected void execute(ResultObject result) throws Exception {
		
		try{
			
			Log.v(Application.TAG, "Deleting GCM RegId with our Server");
			Application app = Application.getApplicationInstance();
			String packageName = app.context.getPackageName();
			
			// Create a new HttpClient and Post Header  
			HttpClient httpclient = new DefaultHttpClient();
			String url = "http://apps.ayansh.com/HanuGCM/UnRegisterDevice.php";
			HttpPost httppost = new HttpPost(url);
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
			nameValuePairs.add(new BasicNameValuePair("package", packageName));
			nameValuePairs.add(new BasicNameValuePair("regid", regId));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			//Execute HTTP Post Request  
			HttpResponse response = httpclient.execute(httppost);
			
			// Open Stream for Reading.
			InputStream is = response.getEntity().getContent();
			
			// Read
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = reader.readLine();
			
			if(line.toString().contentEquals("Success")){
				// Success
				app.addParameter("RegistrationStatus", ""); // TODO - Varun to figure it out
				Log.v(Application.TAG, "GCM RegId deleted successfully on our server");
			}
			
		}
		catch (Exception e){
			// Nothing to do
			Log.w(Application.TAG, "Following error occured while deleting GCM RegId with our servers:");
			Log.e(Application.TAG, e.getMessage(), e);
		}
	}
}