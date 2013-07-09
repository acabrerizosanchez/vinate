package com.example.evernotewine;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;

public class MainActivity extends Activity {

	private static final String CONSUMER_KEY = "toporniz";
	private static final String CONSUMER_SECRET = "20f1829a8d0e4bcb";
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;
    protected EvernoteSession mEvernoteSession;
    // Name of this application, for logging
    private static final String LOGTAG = "EWine";
    private static boolean loginFail = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mEvernoteSession = EvernoteSession.getInstance(this, CONSUMER_KEY, CONSUMER_SECRET, EVERNOTE_SERVICE);
	}

	
	@Override
	protected void onResume(){
		super.onResume();
		//logout(null);
		if(!mEvernoteSession.isLoggedIn()){
			final Dialog dialog = new Dialog(this);
			dialog.setContentView(R.layout.login_window);
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			Button button = (Button) dialog.findViewById(R.id.btnSignIn);
			button.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					login(null);
					dialog.dismiss();
				}
			});
			TextView logmessage = (TextView) dialog.findViewById(R.id.txtsignin);
			if(loginFail){
				logmessage.setText(R.string.login_error);
			}
			else{
				logmessage.setText(R.string.login_message);
			}
			loginFail = false;
			dialog.show();
		}	
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * Method that invokes the activity which performs the login into the evernote server. When returning the method 
	 * onActivityResult is called and then onResume.
	 * @param view
	 */
	public void login(View view) {
	    mEvernoteSession.authenticate(this);
	}
	
	
	/**
	 * Method that executes the logout from the evernote server.
	 *
	 * @param view
	 */
	  public void logout(View view) {
		    try {
		      mEvernoteSession.logOut(this);
		    } catch (InvalidAuthenticationException e) {
		      Log.e(LOGTAG, "Tried to call logout with not logged in", e);
		    }
		    //TODO
		  }
	
	
	  /**
	   * Method invoked after login, that checks if the login process was successful
	   */
	 @Override
	  public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    switch (requestCode) {
	      //Update UI when oauth activity returns result
	      case EvernoteSession.REQUEST_CODE_OAUTH:
	        if (resultCode == Activity.RESULT_OK) {
	        	//TODO
	        	Log.e(LOGTAG, "logged in successfully");
	        	loginFail = false;
	        }
	        else{
	        	loginFail = true;
	        	Log.e(LOGTAG, "logged failed with code "+resultCode);
	        }
	        break;
	    }
	  }

}
