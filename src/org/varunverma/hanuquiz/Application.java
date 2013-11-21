package org.varunverma.hanuquiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.varunverma.CommandExecuter.CommandExecuter;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.MultiCommand;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

/**
 * @author Varun
 *
 * This is a singleton class.
 */
public class Application {
	
	// Activity Code constants.
	public static final int EULA = 1;
	
	// Sender Id - DO NOT CHANGE
	final String SenderId = "492119277184";
	
	private static Application application;
	private CommandExecuter ce;
	private static final int VersionCode = 1;
	
	public static String TAG, appName;
	
	protected Context context;
	protected String appURL;
	
	protected ApplicationDB appDB;
	
	HashMap<String,String> Settings;

	public static Application getApplicationInstance(){
		// A singleton class.
		if(application == null){
			// Create a new instance.
			application = new Application();
		}
		
		return application;
		
	}
	
	protected Application(){
		
		Settings = new HashMap<String,String>();
		
	}
	
	// Set the context of the application.
	public void setContext(Context context){
		
		if(this.context == null){
			
			this.context = context;
			
			// Build Application name
			buildApplicationName();
			
			// DB Name
			String dbName = "HQ_" + appName + "_DB";
			
			// Create DB Instance
			appDB = ApplicationDB.getInstance(context, 2, dbName);
			
			// Initialize DB
			initializeDB();
		}
		
	}
	
	protected void initializeDB(){
		
		appDB.openDBForWriting();
		appDB.loadSettings();
	}
	
	protected void buildApplicationName() {
		// Get the application name
		if(appName == null){

			ApplicationInfo ai = context.getApplicationInfo();
			PackageManager pm = context.getPackageManager();
			appName = (String) pm.getApplicationLabel(ai);
			appName = appName.replaceAll(" ", "");
			TAG = appName;
			
		}
		
		int id = context.getResources().getIdentifier("AppURL", "string", context.getPackageName());
		appURL = context.getResources().getString(id);
		
	}
	
	// Add parameter
	public boolean addParameter(String paramName, String paramValue) {
		
		List<DBContentValues> transactionData = new ArrayList<DBContentValues>();
		DBContentValues data = new DBContentValues();
		
		data.TableName = ApplicationDB.SettingsTable;
		data.Content = new ContentValues();
		data.Content.put("ParamName", paramName);
		data.Content.put("ParamValue", paramValue);
		
		if (Settings.containsKey(paramName)) {
			// Already exists. Update it.
			data.dbOperation = DBContentValues.DBOperation.UPDATE;
			data.where = "ParamName = '" + paramName + "'";
			
		} else {
			// New entry. Create it
			data.dbOperation = DBContentValues.DBOperation.INSERT;
		}
		
		transactionData.add(data);
		
		boolean success;
		try {
			
			appDB.executeDBTransaction(transactionData);
			success = true;
			
		} catch (Exception e) {
			success = false;
		}
		
		if (success) {
			Settings.put(paramName, paramValue);
		}

		return success;

	}

	public boolean removeParameter(String paramName) {
		
		List<DBContentValues> transactionData = new ArrayList<DBContentValues>();
		DBContentValues data = new DBContentValues();
		
		data.TableName = ApplicationDB.SettingsTable;
		data.where = "ParamName = '" + paramName + "'";
		data.dbOperation = DBContentValues.DBOperation.DELETE;
		
		transactionData.add(data);
		
		boolean success;
		try {
			
			appDB.executeDBTransaction(transactionData);
			success = true;
			
		} catch (Exception e) {
			success = false;
		}
		
		if (success) {
			Settings.remove(paramName);
		}

		return success;
	}
	
	public void setEULAResult(boolean result) {
		// Save EULA Result
		addParameter("EULA", String.valueOf(result));
	}
	
	public boolean isEULAAccepted(){
		String eula = Settings.get("EULA");
		if(eula == null || eula.contentEquals("")){
			eula = "false";
		}
		return Boolean.valueOf(Settings.get("EULA"));
	}

	public void registerAppForGCM() {

		// Register this app
		String regId1 = Settings.get("RegistrationId");
		String regId2 = GCMRegistrar.getRegistrationId(context);

		if (regId1 == null || regId1.contentEquals("") || regId2 == null || regId2.contentEquals("")) {
			// Application is not registered
			Log.v(TAG, "Registering app with GCM");

			// Remove parameters
			removeParameter("RegistrationId");
			removeParameter("RegistrationStatus");

			// Register
			GCMRegistrar.register(context, SenderId);
		}
		
	}
	
	public boolean isThisFirstUse(){
		
		long count = DatabaseUtils.queryNumEntries(appDB.getWritableDatabase(), ApplicationDB.QuestionsTable);
		if(count > 0){
			return false;
		}
		else{
			return true;
		}		
	}

	public void initializeAppForFirstUse(Invoker caller) {
		
		Log.v(TAG, "Initializing Application for first use");
		
		ce = new CommandExecuter();
		MultiCommand command = new MultiCommand(caller);
		
		ValidateApplicationCommand validate = new ValidateApplicationCommand(caller);
		command.addCommand(validate);
		
		LoadQuestionsFromFileCommand loadFromFile = new LoadQuestionsFromFileCommand(caller);
		command.addCommand(loadFromFile);
		
		FetchArtifactsCommand fetchArtifacts = new FetchArtifactsCommand(caller);
		command.addCommand(fetchArtifacts);
		
		DownloadQuestionsCommand downloadQuestions = new DownloadQuestionsCommand(caller);
		command.addCommand(downloadQuestions);
		
		DownloadQuizCommand downloadQuiz = new DownloadQuizCommand(caller);
		command.addCommand(downloadQuiz);
		
		ce.execute(command);
		
	}

	public void initialize(Invoker caller) {
		
		Log.v(TAG, "Initializing Application for regular use");
		
		ce = new CommandExecuter();
		MultiCommand command = new MultiCommand(caller);
		
		ValidateApplicationCommand validate = new ValidateApplicationCommand(caller);
		command.addCommand(validate);
		
		FetchArtifactsCommand fetchArtifacts = new FetchArtifactsCommand(caller);
		command.addCommand(fetchArtifacts);
		
		DownloadQuestionsCommand downloadQuestions = new DownloadQuestionsCommand(caller);
		command.addCommand(downloadQuestions);
		
		DownloadQuizCommand downloadQuiz = new DownloadQuizCommand(caller);
		command.addCommand(downloadQuiz);
		
		String regStatus = Settings.get("RegistrationStatus");
		String regId = Settings.get("RegistrationId");
		
		if(regId == null || regId.contentEquals("")){
			// Nothing to do.
		}
		else{
			if(regStatus == null || regStatus.contentEquals("")){
				SaveRegIdCommand saveRegId = new SaveRegIdCommand(caller,regId);
				command.addCommand(saveRegId);
			}
		}
		
		/*
		 * TODO - Varun - Check if we need to sync ratings or any other stuff
		 */
		
		ce.execute(command);
		
	}
	
	// Get all Options
	public HashMap<String, String> getSettings() {
		return Settings;
	}

	public void close() {
		// Close Application.
		if(ce != null){
			if(ce.getStatus() != AsyncTask.Status.FINISHED){
				// Pending or Running
				ce.cancel(true);
			}
		}
		
	}

	public List<Quiz> getQuizListByLevel(int level) {
		
		QuizManager.getInstance().clearQuizList();
		appDB.loadQuizListByLevel(level);
		return QuizManager.getInstance().getQuizList();
		
	}

	public void setSyncCategory(String syncCategory) {
		
		Settings.put("SyncTag", syncCategory);
		
	}

	public int getOldFrameworkVersion() {
		
		String versionCode = Settings.get("HanuVersionCode");
		if(versionCode == null || versionCode.contentEquals("")){
			versionCode = "0";
		}
		return Integer.valueOf(versionCode);
	}

	public int getNewFrameworkVersion() {
		return VersionCode;
	}

	public int getOldAppVersion() {
		
		String versionCode = Settings.get("AppVersionCode");
		if(versionCode == null || versionCode.contentEquals("")){
			versionCode = "0";
		}
		return Integer.valueOf(versionCode);
	}

	public void updateVersion() {
		
		// Update Version Since version is updated. We have to register again !
		addParameter("RegistrationId", ""); // Set Reg Id to space.
		GCMRegistrar.unregister(context);
		GCMRegistrar.register(context, SenderId);

		int version;
		try {
			version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			version = 0;
			Log.e(TAG, e.getMessage(), e);
		}
		addParameter("HanuVersionCode", String.valueOf(VersionCode));
		addParameter("AppVersionCode", String.valueOf(version));
		
	}

}