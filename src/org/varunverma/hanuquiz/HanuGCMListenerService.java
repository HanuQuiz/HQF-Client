package org.varunverma.hanuquiz;

import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.MultiCommand;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public abstract class HanuGCMListenerService extends GcmListenerService implements Invoker{
	
	protected ResultObject processMessage(String from, Bundle data) {
		// Message received with Intent
		
		// Initialize the application
		Application app = Application.getApplicationInstance();
		app.setContext(getApplicationContext());
		
		String message = data.getString("message");
		
		// Gen Validation
		if(from.contentEquals(app.getSenderId())){
			
			if(message.contentEquals("PerformSync")){
				// Perform Sync
				Log.i(Application.TAG, "Message to Perform Sync recieved from GCM");
				return performSync();
			}
			
			if(message.contentEquals("SyncAll")){
				Log.i(Application.TAG, "Message to Perform Sync-All recieved from GCM");
				Application.getApplicationInstance().getSettings().put("LastQuestionsSyncTime", "1349328720");
				Application.getApplicationInstance().getSettings().put("LastQuizSyncTime", "1349328720");
				return performSync();
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
	public void NotifyCommandExecuted(ResultObject result) {
		// Nothing to do
	}

	@Override
	public void ProgressUpdate(ProgressInfo result) {
		// Nothing to do
	}

}