package org.varunverma.hanuquiz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;

public class Question {

	private int id, level, choiceType, point;
	private String question, createdAt;
	private HashMap<Integer,String> options;
	private List<Integer> answers;
	private List<String> tags;
	private String myAnswer;
	
	Question(){
		
		id = 0;
		question = "";
		options = new HashMap<Integer,String>();
		answers = new ArrayList<Integer>();
		tags = new ArrayList<String>();
		point = 0;
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
	
	public String getCreatedAt() {
		return createdAt;
	}

	void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
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
		QuestionData.Content.put("ID", id);
		QuestionData.Content.put("Question", question);
		QuestionData.Content.put("Level", level);
		QuestionData.Content.put("Choice", choiceType);
		QuestionData.Content.put("CreatedAt", createdAt);
		QuestionData.dbOperation = DBContentValues.DBOperation.INSERT;
		transactionData.add(QuestionData);

		// -- Save Options Table --
		Iterator iter_options = options.keySet().iterator();

		while (iter_options.hasNext()) {
			
			DBContentValues OptionsData = new DBContentValues();
			OptionsData.TableName = ApplicationDB.OptionsTable;
			OptionsData.Content = new ContentValues();

			Integer option_id = (Integer) iter_options.next();
			String option_value = options.get(option_id);

			OptionsData.Content.put("QuestionId", id);
			OptionsData.Content.put("OptionId", option_id);
			OptionsData.Content.put("OptionValue", option_value);

			OptionsData.dbOperation = DBContentValues.DBOperation.INSERT;
			transactionData.add(OptionsData);
		}

		// -- Save Answers Table --
		Iterator iter_ans = answers.iterator();

		while (iter_ans.hasNext()) {
			
			DBContentValues AnswersData = new DBContentValues();
			AnswersData.TableName = ApplicationDB.AnswersTable;
			AnswersData.Content = new ContentValues();
			
			Integer answer_id = (Integer) iter_ans.next();
			AnswersData.Content.put("QuestionId", id);
			AnswersData.Content.put("OptionId", answer_id);
			
			AnswersData.dbOperation = DBContentValues.DBOperation.INSERT;
			transactionData.add(AnswersData);
		}

		// -- Save Tags (Metadata) --
		
		Iterator iter_tag = tags.iterator();

		while (iter_tag.hasNext()) {
			
			DBContentValues MetaData = new DBContentValues();
			MetaData.TableName = ApplicationDB.MetaDataTable;
			MetaData.Content = new ContentValues();
			
			String tag = (String) iter_tag.next();
			MetaData.Content.put("QuestionId", id);
			MetaData.Content.put("MetaKey", "tag");
			MetaData.Content.put("MetaValue", tag);
			
			MetaData.dbOperation = DBContentValues.DBOperation.INSERT;
			transactionData.add(MetaData);
			
		}

		try {

			Appdb.executeDBTransaction(transactionData);

		} catch (Exception e) {
			throw e;
		}
				
	}
	
	public void updateMyAnswer(String answer){
		
		myAnswer = answer;
		
	}
	
	
	public String getMyAnswer()
	{
		return myAnswer;
	}
	
	boolean evaluateQuestion(){
		
		/*
		 * TODO - Pramodh to evaluate Question
		 * The users answer is available as an attribute
		 * Compare with the correct answers and evaluate
		 * If correct then return true, else false
		 */
		
		if(myAnswer == null || myAnswer == "") return false; // NO User Answer Found
		
		String[] userAnswers  = myAnswer.split(",");
		
		boolean found = true; //defaulted found == true
		
		Iterator <Integer>Iter = answers.iterator();
		
		if(Iter.hasNext())
		{
			do{
				if( !Arrays.asList(userAnswers).contains(Iter.next().toString()) ) found = false;
			}while(Iter.hasNext());
			
		}
		else found = false;
		
		
		return found;
	}

	public String getHTML() {
		
		String html = "";
		
		html = "<html><body>" +
				question + "<br>" +
				"</body></html>";
		
		return html;
	}
	
}