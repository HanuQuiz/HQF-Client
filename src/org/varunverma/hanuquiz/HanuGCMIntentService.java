package org.varunverma.hanuquiz;

import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.MultiCommand;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public abstract class HanuGCMIntentService extends GCMBaseIntentService implements Invoker{
	
	protected HanuGCMIntentService(java.lang.String... senderIds){
		super(Application.getApplicationInstance().SenderId);
	}
	
	@Override
	protected void onError(Context context, String errorId) {
		// Error while registration / un-registration

	}

	protected ResultObject processMessage(Context context, Intent intent) {
		// Message received with Intent
		
		// Initialize the application
		Application app = Application.getApplicationInstance();
		app.setContext(getApplicationContext());
		
		String message = intent.getExtras().getString("message");
		String from = intent.getExtras().getString("from");
		//String collapseKey = intent.getExtras().getString("collapse_key");
		
		// Gen Validation
		if(from.contentEquals("492119277184")){
			
			if(message.contentEquals("PerformSync")){
				// Perform Sync
				return performSync();
			}
			
			if(message.contentEquals("RegisterDevice")){
				// Register Device Again Remove parameters
				app.removeParameter("RegistrationId");
				app.removeParameter("RegistrationStatus");
				
				app.registerAppForGCM();
			}
		}
		
		return new ResultObject();
		
	}
	
	private ResultObject performSync() {

		MultiCommand command = new MultiCommand(this);
			
		FetchArtifactsCommand fetchArtifacts = new FetchArtifactsCommand(this);
		command.addCommand(fetchArtifacts);
		
		DownloadQuestionsCommand downloadQuestions = new DownloadQuestionsCommand(this);
		command.addCommand(downloadQuestions);
		
		DownloadQuizCommand downloadQuiz = new DownloadQuizCommand(this);
		command.addCommand(downloadQuiz);
		
		return command.execute();
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		/*
		 * Device is Registered !
		 * 
		 * 1. Save this Reg Id.
		 * 2. Update this to my server.
		 * 
		 */
		
		// Initialize the application
		Application.getApplicationInstance().setContext(getApplicationContext());
		
		if(regId == null || regId.contentEquals("")){
			return;
		}
		
		Log.v(Application.TAG, "Registration with GCM success");
		Application.getApplicationInstance().addParameter("RegistrationId", regId);
		
		SaveRegIdCommand command = new SaveRegIdCommand(this, regId);
		command.execute();
		
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		// Device is Un-registered
		
		// Initialize the application
		Application.getApplicationInstance().setContext(getApplicationContext());
				
		Log.v(Application.TAG, "Un-Registration with GCM success");
		Application.getApplicationInstance().addParameter("RegistrationId", "");
		
		DeleteRegIdCommand command = new DeleteRegIdCommand(this, regId);
		command.execute();

	}

	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		// Nothing to do
	}

	@Override
	public void ProgressUpdate(ProgressInfo result) {
		// Nothing to do
	}

}