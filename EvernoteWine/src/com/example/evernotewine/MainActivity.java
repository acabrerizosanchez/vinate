package com.example.evernotewine;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;

public class MainActivity extends Activity {

	private static final String CONSUMER_KEY = "toporniz";
	private static final String CONSUMER_SECRET = "20f1829a8d0e4bcb";
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;
    protected EvernoteSession mEvernoteSession;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mEvernoteSession = EvernoteSession.getInstance(this, CONSUMER_KEY, CONSUMER_SECRET, EVERNOTE_SERVICE);
	}

	
	@Override
	protected void onResume(){
		super.onResume();
		if(!mEvernoteSession.isLoggedIn()){
			final Dialog dialog = new Dialog(this);
			dialog.setContentView(R.layout.login_window);
			Button button = (Button) dialog.findViewById(R.id.btnSignIn);
			button.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					login(null);
					dialog.dismiss();
				}
			});
			dialog.show();
		}	
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void login(View view) {
	    mEvernoteSession.authenticate(this);
	}

}
