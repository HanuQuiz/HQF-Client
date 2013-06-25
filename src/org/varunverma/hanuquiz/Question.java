package org.varunverma.hanuquiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
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
		 * If Error occurs during save, then throw Exception
		 * Remember to maintain the transactional integrity. Either all or none.
		 * Call DB Method executeDBTransactions - It will ensure either all or none.
		 */		
			
		ApplicationDB Appdb = ApplicationDB.getInstance();
		
		// -- Save Question Table --
		List<DBContentValues> transactionData = new ArrayList<DBContentValues>();
		DBContentValues QuestionData = new DBContentValues();
		
		QuestionData.TableName = ApplicationDB.QuestionsTable;
		QuestionData.Content = new ContentValues();
		QuestionData.Content.put("Id", id);
		QuestionData.Content.put("Question", question);
		QuestionData.Content.put("Level", level);
		QuestionData.Content.put("Choice", choiceType);
		QuestionData.dbOperation = DBContentValues.DBOperation.INSERT;
		transactionData.add(QuestionData);
		
		// -- Save Options Table  --
		DBContentValues OptionsData = new DBContentValues();
		Iterator iter_options = options.keySet().iterator();
		
		OptionsData.TableName = ApplicationDB.OptionsTable;
		OptionsData.Content = new ContentValues();
		
		 while(iter_options.hasNext()) 
		 	{
			 Integer option_id = (Integer)iter_options.next();
			 String option_value = (String)options.get(option_id);
			 
			 OptionsData.Content.put("QuestionId", id);
			 OptionsData.Content.put("OptionId", option_id);
			 OptionsData.Content.put("OptionValue", option_value);
			 }

		 OptionsData.dbOperation = DBContentValues.DBOperation.INSERT;
		 transactionData.add(OptionsData);	
		
		// -- Save Answers Table --
		DBContentValues AnswersData = new DBContentValues();
		AnswersData.TableName = ApplicationDB.AnswersTable;
		AnswersData.Content = new ContentValues();
		Iterator iter_ans  = answers.iterator();
				
		while(iter_ans.hasNext()) 
		{
			 Integer answer_id = (Integer)iter_ans.next();
			 AnswersData.Content.put("QuestionId", id);
			 AnswersData.Content.put("OptionId", answer_id);
		 }
		
		 AnswersData.dbOperation = DBContentValues.DBOperation.INSERT;
		transactionData.add(AnswersData);	
			 
		// -- Save Tags (Metadata) --
		DBContentValues MetaData = new DBContentValues();
		MetaData.TableName = ApplicationDB.MetaDataTable;
		MetaData.Content = new ContentValues();
		Iterator iter_tag = tags.iterator();
			 
		while(iter_tag.hasNext()) 
		{
			 String tag = (String)iter_tag.next();
			 AnswersData.Content.put("QuestionId", id);
			 AnswersData.Content.put("MetaKey", "tag");
			 AnswersData.Content.put("MetaValue", tag);
		 }
		
		transactionData.add(AnswersData);	
			 
		boolean success;
		try {
			
			Appdb.executeDBTransaction(transactionData);
			success = true;
			
		} catch (Exception e) {
			success = false;
			throw e;
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