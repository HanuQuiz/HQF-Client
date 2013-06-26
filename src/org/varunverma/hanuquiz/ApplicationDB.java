package org.varunverma.hanuquiz;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ApplicationDB extends SQLiteOpenHelper{

	private static ApplicationDB appDB;
	private static int DBVersion;
	private static String DBName;
	
	static final String SettingsTable = "Settings";
	static final String QuizTable = "Quiz";
	static final String QuestionsTable = "Questions";
	static final String OptionsTable = "Options";
	static final String AnswersTable = "Answers";
	static final String MetaDataTable = "MetaData";
	
	private SQLiteDatabase data_base;
	
	static ApplicationDB getInstance(Context context){
		
		DBVersion = 1;
		
		String appName = Application.appName;
		DBName = "HQ_" + appName + "_DB";
		
		if(appDB == null){
			appDB = new ApplicationDB(context);
		}
		
		return appDB;
	}
	
	static ApplicationDB getInstance(){
		return appDB;
	}
	
	private ApplicationDB(Context context) {
		
		super(context, DBName, null, DBVersion);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		/*
		 * Create Questions, Options, Answers, MetaData tables.
		 * 
		 * Questions will be a FTS4 table. Others will be normal tables.
		 * Read about FTS : http://www.sqlite.org/fts3.html
		 * 
		 * As an example, I am creating a Settings Table
		 * Note: Use constants as table names
		 */
		
		String createQuestionsTable = "CREATE VIRTUAL TABLE " + QuestionsTable + "  USING fts3(" + //Questions table - FTS4 Virtual Table
		        "ID INTEGER PRIMARY KEY ASC, " +	//Question Id
				"Question VARCHAR(200), " +			//Question Text
		        "Level INT, " +						//Level 
				"Choice INT, " +					//Choice
				"CreatedAt DATETIME " +				//Set the default value of the Created at time stamp
		        ")";
		        
		String createAnswersTable = "CREATE TABLE " + AnswersTable + " (" + 
				"QuestionId INT, " + 		// Question ID
				"OptionId INT " + 			// Option ID
				")";
		        
		String createSettingsTable = "CREATE TABLE " + SettingsTable + " (" + 
				"ParamName VARCHAR(20), " + 		// Parameter Name
				"ParamValue VARCHAR(20)" + 			// Parameter Value
				")";
		
		String createOptionsTable = "CREATE TABLE " + OptionsTable + " (" + 
				"QuestionId INT, " + 		// Question ID
				"OptionId INT, " + 			// Option ID
				"OptionValue VARCHAR(20) " + 			// Option Value
				")";
		
		String createQuestionsMetaTable = "CREATE TABLE " + MetaDataTable + " (" + 
				"QuestionId INT, " + 		// Question ID
				"MetaKey VARCHAR(20), " + 			// Meta Key
				"MetaValue VARCHAR(20) " + 			// Meta Value
				")";
		
		String createQuizTable = "CREATE TABLE " + QuizTable + " (" + 
				"ID INTEGER PRIMARY KEY ASC, " + // Question ID
				"Level INT, " + 					// Level
				"Count INT, " + 					// Count
				"QuestionIds VARCHAR(100), " + 	// Question IDs seperated by comma
				"CreatedAt DATETIME " +
				")";
		
		// create a new table - if not existing
		try {
			// Create Tables.
			Log.i(Application.TAG, "Creating Tables for Version:" + String.valueOf(DBVersion));
				
			db.execSQL(createQuestionsTable);
			db.execSQL(createAnswersTable);			
			db.execSQL(createOptionsTable);
			db.execSQL(createQuestionsMetaTable);
			db.execSQL(createQuizTable);
			db.execSQL(createSettingsTable);
						
			Log.i(Application.TAG, "Tables created successfully");

		} catch (SQLException e) {
			Log.e(Application.TAG, e.getMessage(), e);
		}
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// Nothing to do
		
	}
	
	void openDBForWriting(){
		data_base = appDB.getWritableDatabase();
	}

	synchronized void executeDBTransaction(List<DBContentValues> dbData) throws Exception{
		
		try{
			
			data_base.beginTransaction();
			
			Iterator<DBContentValues> i = dbData.iterator();
			
			while(i.hasNext()){
				
				DBContentValues data = i.next();
				
				if (data.dbOperation == DBContentValues.DBOperation.INSERT) {
					
					data_base.insertOrThrow(data.TableName, null, data.Content);

				}

				if (data.dbOperation == DBContentValues.DBOperation.UPDATE) {
					
					data_base.update(data.TableName, data.Content, data.where, null);

				}

				if (data.dbOperation == DBContentValues.DBOperation.DELETE) {
					
					data_base.delete(data.TableName, data.where, null);

				}

			}
			
			data_base.setTransactionSuccessful();
			data_base.endTransaction();
			
		} catch (Exception e) {

		data_base.endTransaction();
			throw e;
			
		}
		
	}
		
	synchronized void loadSettings(){
		
		/* 
		 * Select from Settings table and load into memory.
		 * Populate the Application->Settings attribute
		 * 
		 */
		
		Application App = Application.getApplicationInstance();
		String ParamName, ParamValue;
		Cursor SettingsCursor = data_base.query(SettingsTable, null, null, null, null, null, null);
		if (SettingsCursor.moveToFirst()) {

			do {
				ParamName = SettingsCursor.getString(SettingsCursor.getColumnIndex("ParamName"));
				ParamValue = SettingsCursor.getString(SettingsCursor.getColumnIndex("ParamValue"));
				App.addParameter(ParamName, ParamValue);
				
			} while (SettingsCursor.moveToNext());
		}

		SettingsCursor.close();
		
	}
	
	synchronized void loadQuizListByLevel(int level) {
		/*
		 * populate them in the QuizManager.quizList
		 * Populate via method: addQuizToList
		 */
		QuizManager qmgr = QuizManager.getInstance();
		String questionIds;
		int questionId, index, quizId;
		String selection = "Level = '" + level + "'";

		Cursor qCursor = data_base.query(QuizTable, null, selection, null, null, null, null);
		if (qCursor.moveToFirst()) {

			do {
				Quiz quiz_obj = new Quiz();

				questionIds = "";
				questionIds = qCursor.getString(qCursor.getColumnIndex("QuestionIds"));
				quizId = qCursor.getInt(qCursor.getColumnIndex("ID"));

				quiz_obj.setLevel(level);
				quiz_obj.setQuizId(quizId);

				index = 0;
				do {
					index = questionIds.indexOf(",");
					if (index <= 0) {
						questionId = Integer.parseInt(questionIds); // No comma found, thats the only questionId left
						quiz_obj.addQuestion(questionId);
						break;
					} else {
						questionId = Integer.parseInt(questionIds.substring(0,index));
						quiz_obj.addQuestion(questionId);
						questionIds = questionIds.substring(index + 1,questionIds.length()); // Shrink the question Ids to fetch the next
					}
				} while (index > 0);

				qmgr.addQuizToList(quiz_obj);
				quiz_obj = null; // Removing object properties, since we're in loop

			} while (qCursor.moveToNext());
		}
		
		qCursor.close();
			
}
		

	
	synchronized List<Question> getQuestionsByIds(String questionIds){
		
		// questionIds is CSV
		String selection = "ID IN (" + questionIds + ")";
		
		List<Question> list = new ArrayList<Question>();
		
		Question question;
		Cursor qCursor = data_base.query(QuestionsTable, null, selection, null, null, null, null);
		
		if(qCursor.moveToFirst()){
			
			do{
				
				question = buildQuestionObject(qCursor);
				list.add(question);
				
			}while(qCursor.moveToNext());
			
		}
		
		qCursor.close();
		
		return list;
	}
	
	private Question buildQuestionObject(Cursor qCursor) {
		
		Question question = new Question();
		
		question.setId(qCursor.getInt(qCursor.getColumnIndex("ID")));
		
		String selection = "QuestionId='" + question.getId() + "'";
		
		// Select Options
		Cursor oCursor = data_base.query(OptionsTable, null, selection, null, null, null, "OptionId ASC");
		if(oCursor.moveToFirst()){
			
			do {
				question.addOption(oCursor.getInt(oCursor.getColumnIndex("OptionId")),
						oCursor.getString(oCursor.getColumnIndex("OptionValue")));
			} while (oCursor.moveToNext());
		}
		
		oCursor.close();

		// Select Answers
		Cursor aCursor = data_base.query(AnswersTable, null, selection, null, null, null, null);
		if (aCursor.moveToFirst()) {

			do {
				question.addAnswer(aCursor.getInt(aCursor.getColumnIndex("OptionId")));
			} while (aCursor.moveToNext());
		}

		aCursor.close();
		
		// Select Meta Data
		String metaKey, metaValue;
		Cursor mCursor = data_base.query(MetaDataTable, null, selection, null, null, null, null);
		if (mCursor.moveToFirst()) {

			do {
				
				metaKey = mCursor.getString(mCursor.getColumnIndex("MetaKey"));
				metaValue = mCursor.getString(mCursor.getColumnIndex("MetaValue"));
				question.addMetaData(metaKey, metaValue);
				
			} while (mCursor.moveToNext());
		}

		mCursor.close();

		return question;
	}

	HashMap<Integer, Date> getQuizArtifacts(String quizIds) {
		
		HashMap<Integer, Date> artifactList = new HashMap<Integer, Date>();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String[] columns = {"ID","CreatedAt"};
		String selection = "ID in (" + quizIds + ")";;
		
		Cursor cursor = data_base.query(QuizTable, columns, selection, null, null, null, null);
		
		if(cursor.moveToFirst()){
			
			do{
				
				try {
					artifactList.put(cursor.getInt(0), df.parse(cursor.getString(1)));
				} catch (ParseException e) {
					Log.w(Application.TAG, e.getMessage());
				}
				
			}while(cursor.moveToNext());
			
		}
		
		cursor.close();
		
		return artifactList;
		
	}
	
	HashMap<Integer, Date> getQuestionArtifacts(String questionIds) {

		HashMap<Integer, Date> artifactList = new HashMap<Integer, Date>();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String[] columns = {"ID","CreatedAt"};
		String selection = "ID in (" + questionIds + ")";;
		
		Cursor cursor = data_base.query(QuestionsTable, columns, selection, null, null, null, null);
		
		if(cursor.moveToFirst()){
			
			do{
				
				try {
					artifactList.put(cursor.getInt(0), df.parse(cursor.getString(1)));
				} catch (ParseException e) {
					Log.w(Application.TAG, e.getMessage());
				}
				
			}while(cursor.moveToNext());
			
		}
		
		cursor.close();
		
		return artifactList;
	}

	HashMap<Integer, String> getUserAnswers(int quizId,	String questionIds) {
		
		HashMap<Integer,String> userAnswers = new HashMap<Integer,String>();
		
		/*
		 * TODO - Pramodh to read the User Answers table
		 * Then populate the hash map and return
		 */
		
		return userAnswers;
	}
	
}