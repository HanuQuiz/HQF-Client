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

public class DownloadQuestionsCommand extends Command {

	private QuestionManager qm;
	
	DownloadQuestionsCommand(Invoker caller) {
		super(caller);
	}

	@Override
	protected void execute(ResultObject result) throws Exception {
		
		Application app = Application.getApplicationInstance();
		
		String appURL = app.appURL;
		String url = appURL + "/FetchQuestionsData.php";
		String questionList = "";
		
		InputStream is;
		InputStreamReader isr;
		
		qm = QuestionManager.getInstance();
		
		Iterator<Integer> iterator = qm.toDownload.listIterator();
		
		/*
		 * Right now, we are going to download all questions together...
		 * Later on, we can do it in packets of 25
		 */
		
		if(iterator.hasNext()){
			questionList = String.valueOf(iterator.next());
		}
		
		while(iterator.hasNext()){
			questionList += "," + String.valueOf(iterator.next());
		}
		
		Log.v(Application.TAG, "Downloading Questions...");

		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("question_ids", questionList));
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
			
			JSONObject questionData = jsonResponse.getJSONObject(i);
			
			JSONObject question = questionData.getJSONObject("question");
			JSONArray options = questionData.getJSONArray("options");
			JSONArray answers = questionData.getJSONArray("answers");
			JSONArray metaData = questionData.getJSONArray("meta");
			
			Question q = new Question();
			
			q.setId(question.getInt("ID"));
			q.setLevel(question.getInt("Level"));
			q.setChoiceType(question.getInt("ChoiceType"));
			q.setQuestion(question.getString("Question"));
			
			for(int j=0; j<options.length(); j++){
				
				JSONObject option = options.getJSONObject(j);
				q.addOption(option.getInt("OptionId"), option.getString("OptionValue"));
				
			}
			
			for(int j=0; j<answers.length(); j++){
				
				JSONObject answer = answers.getJSONObject(j);
				q.addAnswer(answer.getInt("OptionId"));
				
			}
			
			for(int j=0; j<metaData.length(); j++){
				
				JSONObject meta = metaData.getJSONObject(j);
				q.addMetaData(meta.getString("MetaKey"), meta.getString("MetaValue"));
				
			}
			
			qm.toSave.add(q);
			
		}
		
		// Save Questions to db.
		boolean success = qm.saveQuestionsToDB();
		
		if(success){
			
			// If All success, then set sync time to now.
			app.addParameter("LastQuestionsSyncTime", String.valueOf((new Date()).getTime() - 2*60*1000));
			Log.v(Application.TAG, "All Questions downloaded successfully...");
			
		}
		else{
			// Some error occurred !
			Log.w(Application.TAG, "Error occured while downloading some questions !");
			if(app.isThisFirstUse()){
				// If first use, then set to HANU - Epoch
				app.addParameter("LastQuestionsSyncTime", "1349328720");
			}
			else{
				// leave whatever it was
			}
		}
		
		// Prepare result
		result.getData().putInt("QuestionsDownloaded", qm.toDownload.size());
		result.getData().putBoolean("ShowNotification", true);

	}

}