package org.varunverma.hanuquiz;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

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

public class FetchArtifactsCommand extends Command {

	FetchArtifactsCommand(Invoker caller) {
		super(caller);
	}

	@Override
	protected void execute(ResultObject result) throws Exception {
		// Fetch the post artifacts.
		
		String baseUrl = Application.getApplicationInstance().appURL;
		String url = baseUrl + "/FetchArtifacts.php";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String questionsSyncTime = Application.getApplicationInstance().getSettings().get("LastQuestionsSyncTime");
		String quizSyncTime = Application.getApplicationInstance().getSettings().get("LastQuizSyncTime");
		
		if(questionsSyncTime == null || questionsSyncTime.contentEquals("")){
			questionsSyncTime = "1349328720";	// HANU Epoch
		}
		
		if(quizSyncTime == null || quizSyncTime.contentEquals("")){
			quizSyncTime = "1349328720";		// HANU Epoch
		}
		
		Date questionSyncDate = new Date(Long.valueOf(questionsSyncTime));
		Date quizSyncDate = new Date(Long.valueOf(quizSyncTime));
		
		boolean justSynced = false;
		
		// If last sync was very recent, no need to check again.
		if( ( (new Date()).getTime() - questionSyncDate.getTime() ) < 5*60*1000 ){
			justSynced = true;
		}
		
		if(!justSynced){
			
			if( ( (new Date()).getTime() - quizSyncDate.getTime() ) < 5*60*1000 ){
				justSynced = true;
			}
			
		}
		
		if(justSynced){
			throw new Exception("Last sync done less than 5 min ago...");
		}
		
		Log.v(Application.TAG, "Trying to fetch artifacts.");
		
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		questionsSyncTime = df.format(questionSyncDate);
		quizSyncTime = df.format(quizSyncDate);
		
		InputStream is;
		InputStreamReader isr;
		
		// Create a new HttpClient and Post Header  
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
		nameValuePairs.add(new BasicNameValuePair("question_sync_time", questionsSyncTime));
		nameValuePairs.add(new BasicNameValuePair("quiz_sync_time", quizSyncTime));
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		    	
		//Execute HTTP Post Request  
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
		
		// Parse the result.
		JSONObject jsonResponse = new JSONObject(builder.toString());
		
		int id;
		Date createdAt;
		HashMap<Integer,Date> questionArtifactsList = new HashMap<Integer,Date>();
		HashMap<Integer,Date> quizArtifactsList = new HashMap<Integer,Date>();
		
		JSONArray questions = jsonResponse.getJSONArray("questions");
		for(int i=0; i<questions.length(); i++){
			
			JSONObject artifact = questions.getJSONObject(i);
			
			id = artifact.getInt("ID");
			createdAt = df.parse(artifact.getString("CreatedAt"));
			questionArtifactsList.put(id, createdAt);
			
		}
		
		JSONArray quiz = jsonResponse.getJSONArray("quizzes");
		for(int i=0; i<quiz.length(); i++){
			
			JSONObject quizArtifact = quiz.getJSONObject(i);
			
			id = quizArtifact.getInt("QuizId");
			createdAt = df.parse(quizArtifact.getString("CreatedAt"));
			quizArtifactsList.put(id, createdAt);
			
		}
		
		// Filter Question Artifacts for download
		Log.v(Application.TAG, "Question Artifacts fetched, will filter now...");
		QuestionManager.getInstance().filterArtifactsForDownload(questionArtifactsList);
		
		Log.v(Application.TAG, "Quiz Artifacts fetched, will filter now...");
		QuizManager.getInstance().filterArtifactsForDownload(quizArtifactsList);
	}

}