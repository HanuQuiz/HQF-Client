package org.varunverma.hanuquiz;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import android.util.Log;

import android.content.ContentValues;

public class Quiz {

	public static final Comparator<Quiz> SortByID = new Comparator<Quiz>(){
		@Override
		public int compare(Quiz lhs, Quiz rhs) {
			if(lhs.quizId < rhs.quizId){
				return -1;
			}
			else{
				return 1;
			}
		}};
	
	private int quizId, level, score;
	private String description;
	private QuizStatus status;
	private String createdAt;
	private List<Integer> questions;		// List of Question Ids
	private List<Question> questionsList;	// List of Questions
	
	public enum QuizStatus {
		NotStarted, Paused, Completed;
	}
	
	Quiz(){
		questions = new ArrayList<Integer>();
		questionsList = new ArrayList<Question>();
		createdAt = description = "";
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	void setDescription(String description) {
		this.description = description;
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
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * @return the status
	 */
	public QuizStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	void setStatus(QuizStatus status) {
		this.status = status;
	}

	/**
	 * @return the createdAt
	 */
	public String getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt the createdAt to set
	 */
	void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
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
			Iterator<Integer> Iter = questions.iterator();
			String questionIds = "";
			if (Iter.hasNext()) {
				
				questionIds = String.valueOf(Iter.next());
				
				while (Iter.hasNext()) {
					questionIds = questionIds + "," + String.valueOf(Iter.next());
				}
			}
		
			questionsList = Appdb.getQuestionsByIds(questionIds);
			
			// Get users answers also
			HashMap<Integer,String> userAnswers = new HashMap<Integer,String>();
			userAnswers = Appdb.getUserAnswers(quizId, questionIds);
			
			/*
			 * TODO - Pramodh to set the users answers
			 * Loop on questionList, get the answer from userAnswers
			 * Call the method updateMyAnswer of Question Object
			 */
			
			Iterator<Question> quest_iter = questionsList.listIterator();
			if(quest_iter.hasNext())
			{
				do{
					Question quest = quest_iter.next();
					String myAnswer = userAnswers.get(quest.getId());
					quest.updateMyAnswer(myAnswer);
				}while(quest_iter.hasNext());
			}
					
						
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
		QuizData.Content = new ContentValues();
		QuizData.Content.put("ID", quizId);
		QuizData.Content.put("Description", description);
		QuizData.Content.put("Count", questions.size());
		QuizData.Content.put("Level", level);
		QuizData.Content.put("CreatedAt", createdAt);
		
		Iterator<Integer> Iter;
		Iter = questions.iterator();
		String questionids = "";
		
		if(Iter.hasNext()){
			questionids = String.valueOf(Iter.next());
		}
		while(Iter.hasNext()) 
		{
			 questionids = questionids + "," + String.valueOf(Iter.next());
		 }
		
		QuizData.Content.put("QuestionIds", questionids);
		QuizData.dbOperation = DBContentValues.DBOperation.INSERT;
		transactionData.add(QuizData);
		
		try {
			
			Appdb.executeDBTransaction(transactionData);
			
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	public void evaluateQuiz(){
		
		/*
		 * TODO - Pramodh to evaluate quiz
		 * Evaluate means that user has completed the quiz
		 * and wants to see his score
		 * Loop on all questions and check if the answer is correct or not
		 * Make use of the evaluate method on the Question
		 * For each correct answer - award 1 point.
		 * as an attribute in the Question object.
		 * After evaluation, save the status of the quiz and the score in DB
		 * In the same LUW save the MyAnswers table also
		 */
		
		score = 0; // defaulted
		
		ListIterator<Question> questions = questionsList.listIterator();
		if(questions.hasNext())
		{
			do{
				
				Question quest = new Question( );			
				quest = questions.next();
				if(quest.evaluateQuestion() == true) score += 1;
				
			}while(questions.hasNext());
		}
		
		// -- Score is now evaluated, now save the quiz status & MyAnswers in DB -- //
		// -- Update Quiz Table --
				ApplicationDB Appdb = ApplicationDB.getInstance();
				List<DBContentValues> transactionData = new ArrayList<DBContentValues>();
				DBContentValues QuizTable = new DBContentValues();

				QuizTable.TableName = ApplicationDB.QuizTable;
				QuizTable.Content = new ContentValues();
				QuizTable.Content.put("MyScore", score);
				QuizTable.Content.put("Status", QuizStatus.Completed.toString());
				
				QuizTable.where = "ID = '" + quizId + "'";
				
				QuizTable.dbOperation = DBContentValues.DBOperation.UPDATE;
				transactionData.add(QuizTable);
				
		// -- Update MyAnswers table --
				

				Iterator <Question>Iter = questionsList.iterator();
				
				if(Iter.hasNext())
				{
					do{
						DBContentValues MyAnsTable = new DBContentValues();
						MyAnsTable.TableName = ApplicationDB.MyAnswersTable;
						MyAnsTable.dbOperation = DBContentValues.DBOperation.INSERT;
						
						MyAnsTable.Content = new ContentValues();
						Question quest = new Question();
						
						quest = Iter.next();
						MyAnsTable.Content.put("QuizId", quizId );
						MyAnsTable.Content.put("QuestionId", quest.getId() );
						MyAnsTable.Content.put("MyAnswers", quest.getMyAnswer() );		
						quest = null;
						
						transactionData.add(MyAnsTable);
						}while(Iter.hasNext());
				}

				try {

					Appdb.executeDBTransaction(transactionData);

				} catch (Exception e) {
					Log.e(Application.TAG, e.getMessage(), e);	
				}
		
	}

	public void pause() {
		/*
		 * TODO - Pramodh - to pause the quiz
		 * If this method is called then the user wants to stop the quiz
		 * Set the status as Paused in object and in DB
		 */
		status = QuizStatus.Paused; //Set status of current object to "Paused"
		
		// -- Save Status in DB as well --
		
		ApplicationDB Appdb = ApplicationDB.getInstance();
		List<DBContentValues> transactionData = new ArrayList<DBContentValues>();
		DBContentValues QuizTable = new DBContentValues();

		QuizTable.TableName = ApplicationDB.QuizTable;
		QuizTable.Content = new ContentValues();
		QuizTable.Content.put("Status", QuizStatus.Paused.toString());
		
		QuizTable.where = "ID = '" + quizId + "'";
		
		QuizTable.dbOperation = DBContentValues.DBOperation.UPDATE;
		transactionData.add(QuizTable);
		
		try{
			Appdb.executeDBTransaction(transactionData);
		}catch (Exception e) {
			Log.e(Application.TAG, e.getMessage(), e);	
		}
				
	}
	
}