package org.varunverma.hanuquiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Question {

	private int id, level, choiceType;
	private String question;
	private HashMap<Integer,String> options;
	private List<Integer> answers;
	private List<String> tags;
	
	Question(){
		
		id = 0;
		question = "";
		options = new HashMap<Integer,String>();
		answers = new ArrayList<Integer>();
		tags = new ArrayList<String>();
		
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the choiceType
	 */
	public int getChoiceType() {
		return choiceType;
	}

	/**
	 * @param choiceType the choiceType to set
	 */
	void setChoiceType(int choiceType) {
		this.choiceType = choiceType;
	}

	/**
	 * @return the question
	 */
	public String getQuestion() {
		return question;
	}
	/**
	 * @param question the question to set
	 */
	void setQuestion(String question) {
		this.question = question;
	}
	/**
	 * @return the options
	 */
	public HashMap<Integer,String> getOptions() {
		return options;
	}
	/**
	 * @param options the options to set
	 */
	void addOption(int optionID, String optionValue) {
		options.put(optionID, optionValue);
	}
	/**
	 * @return the answers
	 */
	public List<Integer> getAnswers() {
		return answers;
	}
	/**
	 * @param answers the answers to set
	 */
	void addAnswer(int answer) {
		answers.add(answer);
	}
	/**
	 * @return the Tags
	 */
	public List<String> getTags() {
		return tags;
	}
	/**
	 * @param add the metaData to set
	 */
	void addMetaData(String key, String value) {
		
		if(key.contentEquals("tag")){
			tags.add(value);
		}
		
	}

	void saveToDB() throws Exception{
		/*
		 * TODO - Pramodh - Save Question to DB
		 * If Error occurs during save, then throw Exception
		 * Remember to maintain the transactional integrity. Either all or none.
		 * Call DB Method executeDBTransactions - It will ensure either all or none.
		 */		
		
		try {
		
		ApplicationDB Appdb = ApplicationDB.getInstance();
		Appdb.startTransaction();
		
		Appdb.saveQuestion(id, question, level, choiceType);	
		
		/*
		while (options.hasNext()) {
			options entry = (options) options.next();
		    Integer Id = (Integer)entry.getKey();
		    Integer text = (Integer)entry.getValue();
		    Appdb.saveOptions(id, OptionId, OptionValue)
		}
		
		
		*/
		
		
		}catch(SQLException e) {
			Log.e(Application.TAG, e.getMessage(), e);
	}
		
	}

	public String getHTML() {
		
		String html = "";
		
		// Dummy HTML for testing
		html = "<html><body>" +
				"This is the question." +
				"Select the correct Answer." +
				"</body></html>";
		
		/*
		 * TODO Pramodh - to build html from question data
		 */
		
		return html;
	}
	
}