package org.varunverma.hanuquiz;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONObject;
import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class LoadQuestionsFromFileCommand extends Command {

	LoadQuestionsFromFileCommand(Invoker caller) {
		super(caller);
	}

	@Override
	protected void execute(ResultObject result) throws Exception {
		
		try{
			
			QuestionManager qm = QuestionManager.getInstance();
			
			Context c = Application.getApplicationInstance().context;
			AssetManager assetManager = c.getAssets();
			
			Log.v(Application.TAG, "Loading default qiestions from file");
			InputStream is = assetManager.open("default_data.json");
			InputStreamReader isr;

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

			// Save to DB.
			boolean success = qm.saveQuestionsToDB();

			if (success) {
				result.setResultCode(200);
				ProgressInfo pi = new ProgressInfo("Show UI");
				publishProgress(pi);
			}
			
		}catch (Exception e){
			// We ignore this :D
			Log.e(Application.TAG, e.getMessage(), e);
		}
		
	}
}