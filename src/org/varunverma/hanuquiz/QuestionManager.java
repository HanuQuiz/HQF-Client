package org.varunverma.hanuquiz;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.util.Log;

public class QuestionManager {

	private static QuestionManager instance;
	
	ArrayList<Integer> downloadList;
	ArrayList<Question> downloadQuestionList;
	
	static QuestionManager getInstance(){
		
		if(instance == null){
			instance = new QuestionManager();
		}
		
		return instance;
	}
	
	private QuestionManager(){
		downloadList = new ArrayList<Integer>();
		downloadQuestionList = new ArrayList<Question>();
	}
	
	void filterArtifactsForDownload(HashMap<Integer, Date> artifactsList) {
		/*
		 * Load Artifacts from DB
		 */
		downloadList.clear();
		
		Iterator<Integer> i = artifactsList.keySet().iterator();
		String questionIds = "";
		
		if(i.hasNext()){
			questionIds = String.valueOf(i.next());
		}
		
		while(i.hasNext()){
			questionIds += "," + String.valueOf(i.next());
		}
		
		HashMap<Integer, Date> dbArtifacts = ApplicationDB.getInstance().getQuestionArtifacts(questionIds);
		
		Entry<Integer,Date> set;
		Date dbDate, date;
		
		Iterator<Entry<Integer,Date>> iterator = artifactsList.entrySet().iterator();
		
		while(iterator.hasNext()){
			
			set = iterator.next();
			date = set.getValue();
			dbDate = dbArtifacts.get(set.getKey());
			
			if(date.compareTo(dbDate) > 0){
				// DB entry is older. So we must update this.
				downloadList.add(set.getKey());
			}
			
		}
		
	}

	boolean saveQuestionsToDB() {
		
		Iterator<Question> i = downloadQuestionList.iterator();
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