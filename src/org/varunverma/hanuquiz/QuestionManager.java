package org.varunverma.hanuquiz;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.util.Log;

public class QuestionManager {

	private static QuestionManager instance;
	
	ArrayList<Integer> toDownload;
	ArrayList<Question> toSave;
	
	static QuestionManager getInstance(){
		
		if(instance == null){
			instance = new QuestionManager();
		}
		
		return instance;
	}
	
	private QuestionManager(){
		toDownload = new ArrayList<Integer>();
		toSave = new ArrayList<Question>();
	}
	
	void filterArtifactsForDownload(HashMap<Integer, Date> artifactsList) {
		/*
		 * Load Artifacts from DB
		 */
		toDownload.clear();
		
		Iterator<Integer> i = artifactsList.keySet().iterator();
		String questionIds = "";
		
		if(i.hasNext()){
			questionIds = String.valueOf(i.next());
		}
		
		while(i.hasNext()){
			questionIds += "," + String.valueOf(i.next());
		}
		
		//Log.i(Application.TAG, "Fetching DB artifacts for: " + questionIds);
		HashMap<Integer, Date> dbArtifacts = ApplicationDB.getInstance().getQuestionArtifacts(questionIds);
		
		Entry<Integer,Date> set;
		Date dbDate, date;
		
		Iterator<Entry<Integer,Date>> iterator = artifactsList.entrySet().iterator();
		
		while(iterator.hasNext()){
			
			set = iterator.next();
			date = set.getValue();
			dbDate = dbArtifacts.get(set.getKey());
			
			if(dbDate == null || date.compareTo(dbDate) > 0){
				// DB entry is older. So we must update this.
				toDownload.add(set.getKey());
			}
			else{
				//Log.w(Application.TAG, "Entry with DB date:" + dbDate.toString() + " is removed");
			}
			
		}
		
	}

	boolean saveQuestionsToDB() {
		
		Iterator<Question> i = toSave.iterator();
		boolean allSuccess = true;
		
		while(i.hasNext()){
			
			try {
				// Save to DB
				i.next().saveToDB();
				
			} catch (Exception e) {
				allSuccess = false;
				Log.w(Application.TAG, e.getMessage());
			}
			
		}
		
		return allSuccess;
	}

}