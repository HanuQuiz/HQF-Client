package org.varunverma.hanuquiz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Quiz {

	private int quizId, level;
	private List<Integer> questions;		// List of Question Ids
	private List<Question> questionsList;	// List of Questions
	
	Quiz(){
		questions = new ArrayList<Integer>();
		questionsList = new ArrayList<Question>();
	}
	
	@Override
	public String toString(){
		return String.valueOf(quizId);
	}
	
	/**
	 * @return the quizId
	 */
	public int getQuizId() {
		return quizId;
	}
	/**
	 * @param quizId the quizId to set
	 */
	void setQuizId(int quizId) {
		this.quizId = quizId;
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
	 * @return the count
	 */
	public int getCount() {	
		return questions.size();
	}
	/**
	 * @return the questions
	 */
	private List<Question> getQuestions() {
		
		if(!questions.isEmpty() && questionsList.isEmpty()){

			/* 
			 * The list of question ids is available in attribute: questions
			 * From this list, select from DB and build Question Object and pass
			 * Call method: ApplicationDB method : getQuestionsByIds(questionIds);
			 */
			
			ApplicationDB Appdb = ApplicationDB.getInstance();
			Iterator Iter = questions.iterator();
			String questionIds = "";
			if(Iter.hasNext())
			{
			 do{
					Integer qid = (Integer)Iter.next();
					questionIds = questionIds + ",";
				}while(Iter.hasNext());
			}
		
			questionsList = Appdb.getQuestionsByIds(questionIds);
			
			
		}
	
		return questionsList;
	}
	/**
	 * @param questions the questions to set
	 */
	void addQuestion(int questionId) {
		questions.add(questionId);
	}
	
	public Question getQuestion(int pos){
		
		if(questionsList.isEmpty()){
			getQuestions();
		}
		
		return questionsList.get(pos);
		
	}
	
	void saveToDB() throws Exception{
		/*
		 * If Error occurs during save, then throw Exception
		 * Call DB Method executeDBTransactions - It will ensure either all or none.
		 */
		
		ApplicationDB Appdb = ApplicationDB.getInstance();
		
		List<DBContentValues> transactionData = new ArrayList<DBContentValues>();
		DBContentValues QuizData = new DBContentValues();
		
		QuizData.TableName = ApplicationDB.QuizTable;
		//QuizData.Content = new ContentValues();	
		QuizData.Content.put("QuizId", quizId);
		QuizData.Content.put("Count", questions.size());
		QuizData.Content.put("Level", level);
		
		Iterator Iter;
		Iter = questions.iterator();
		String questionids = "";
		
		while(Iter.hasNext()) 
		{
			 Question quest = (Question)Iter.next();			 
			 questionids = questionids + "," + quest.getId();
		 }
		
		QuizData.Content.put("QuestionIds", questionids);
		QuizData.dbOperation = DBContentValues.DBOperation.INSERT;
		transactionData.add(QuizData);
		
		boolean success;
		try {
			
			Appdb.executeDBTransaction(transactionData);
			success = true;
			
		} catch (Exception e) {
			success = false;
			throw e;
		}
		
	}
	
}