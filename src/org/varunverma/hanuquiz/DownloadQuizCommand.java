package org.varunverma.hanuquiz;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ResultObject;

import android.util.Log;

public class DownloadQuizCommand extends Command {

	private QuizManager qm;
	
	DownloadQuizCommand(Invoker caller) {
		super(caller);
	}

	@Override
	protected void execute(ResultObject result) throws Exception {
		
		Application app = Application.getApplicationInstance();
		
		String appURL = app.appURL;
		String url = appURL + "/FetchQuizData.php";
		String quizList = "";
		
		InputStream is;
		InputStreamReader isr;
		
		qm = QuizManager.getInstance();
		
		if(qm.toDownload.isEmpty()){
			Log.i(Application.TAG, "Nothing to download in Quizzes");
			return;
		}
		
		Iterator<Integer> iterator = qm.toDownload.listIterator();
		
		/*
		 * Right now, we are going to download all Quizzes together...
		 * Later on, we can do it in packets of 25
		 */
		
		if(iterator.hasNext()){
			quizList = String.valueOf(iterator.next());
		}
		
		while(iterator.hasNext()){
			quizList += "," + String.valueOf(iterator.next());
		}
		
		Log.v(Application.TAG, "Downloading Quizzes...");

		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		httppost.setHeader("Referer", "HanuQuizRocks");

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("quiz_ids", quizList));
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		// Execute HTTP Post Request
		HttpResponse response = httpclient.execute(httppost);

		// Open Stream for Reading.
		is = response.getEntity().getContent();

		// Get Input Stream Reader.
		isr = new InputStreamReader(is);
		
		BufferedReader reader = new BufferedReader(isr);
		
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		if(builder.toString().contentEquals("null")){
			return;
		}

		// Parse the JSON Response
		JSONArray jsonResponse = new JSONArray(builder.toString());
		
		qm.toSave.clear();
		
		for(int i=0; i<jsonResponse.length(); i++){
			
			JSONObject jsonData = jsonResponse.getJSONObject(i);
			
			JSONObject quizData = jsonData.getJSONObject("quiz");
			JSONArray metaData = jsonData.getJSONArray("meta");
			
			Quiz q = new Quiz();
			
			q.setQuizId(quizData.getInt("QuizId"));
			q.setDescription(quizData.getString("Description"));
			q.setLevel(quizData.getInt("Level"));
			q.setCreatedAt(quizData.getString("CreatedAt"));

			String questions = quizData.getString("QuestionIds");
			String[] questionList = questions.split(",");
			for(int j=0; j<questionList.length; j++){
				q.addQuestion(Integer.valueOf(questionList[j]));
			}
			
			for (int j = 0; j < metaData.length(); j++) {

				JSONObject meta = metaData.getJSONObject(j);
				q.addMetaData(meta.getString("MetaKey"),meta.getString("MetaValue"));

			}

			qm.toSave.add(q);
			
		}
		
		// Save Questions to db.
		boolean success = qm.saveQuizToDB();
		
		if(success){
			
			// If All success, then set sync time to now.
			app.addParameter("LastQuizSyncTime", String.valueOf((new Date()).getTime() - 2*60*1000));
			Log.v(Application.TAG, "All Quizzes downloaded successfully...");
			
		}
		else{
			// Some error occurred !
			Log.w(Application.TAG, "Error occured while downloading some questions !");
			if(app.isThisFirstUse()){
				// If first use, then set to HANU - Epoch
				app.addParameter("LastQuizSyncTime", "1349328720");
			}
			else{
				// leave whatever it was
			}
		}
		
		// Prepare result
		ArrayList<String> quizDescList = new ArrayList<String>();
		
		Iterator<Quiz> i = qm.toSave.iterator();
		while(i.hasNext()){
			quizDescList.add(i.next().getDescription());
		}
		
		result.getData().putInt("QuizzesDownloaded", qm.toSave.size());
		result.getData().putBoolean("ShowNotification", true);
		result.getData().putStringArrayList("QuizDesc", quizDescList);

	}

}