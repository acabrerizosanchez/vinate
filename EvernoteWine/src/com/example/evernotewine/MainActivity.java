package com.example.evernotewine;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.thrift.transport.TTransportException;

public class MainActivity extends Activity {

	private static final String CONSUMER_KEY = "toporniz";
	private static final String CONSUMER_SECRET = "20f1829a8d0e4bcb";
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;
    protected EvernoteSession mEvernoteSession;
    // Name of this application, for logging
    private static final String LOGTAG = "EWine";
    private static boolean loginFail = false;
    private EditText mSearchEditText;
    private ArrayList<String> notesNames;
    private ArrayAdapter<String> mAdapter;
    private ListView mResultsListView;
    protected final int DIALOG_PROGRESS = 101;
    private ImageButton btnSearch;
    private ImageButton btnDel;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mEvernoteSession = EvernoteSession.getInstance(this, CONSUMER_KEY, CONSUMER_SECRET, EVERNOTE_SERVICE);
		//Add a listener to the search box
		mSearchEditText = (EditText) this.findViewById(R.id.search_box);
		mSearchEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					findNotesByQuery(mSearchEditText.getText().toString());
                    return true;
                }
                return false;
              }
           });
		
		btnSearch = (ImageButton) this.findViewById(R.id.search_button);
		btnSearch.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				findNotesByQuery(mSearchEditText.getText().toString());
			}
		});
		
		
		btnDel = (ImageButton) this.findViewById(R.id.delete_button);
		btnDel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mSearchEditText.setText("");
			}
		});
		
		
		
		notesNames = new ArrayList();
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, notesNames);
        mResultsListView = (ListView)findViewById(R.id.result_list);
        mResultsListView.setAdapter(mAdapter);
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
	 
	 
	 
	    public void findNotesByQuery(String query) {
	        int offset = 0;
	        int pageSize = 10;

	        NoteFilter filter = new NoteFilter();
	        filter.setOrder(NoteSortOrder.UPDATED.getValue());
	        filter.setWords(query);
	        NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
	        spec.setIncludeTitle(true);

	        mAdapter.clear();

	        showDialog(DIALOG_PROGRESS);
	        try{
	            mEvernoteSession.getClientFactory().createNoteStoreClient()
	                    .findNotesMetadata(filter, offset, pageSize, spec, new OnClientCallback<NotesMetadataList>() {
	                        @Override
	                        public void onSuccess(NotesMetadataList data) {
	                            Toast.makeText(getApplicationContext(), R.string.notes_searched, Toast.LENGTH_LONG).show();
	                            removeDialog(DIALOG_PROGRESS);

	                            for(NoteMetadata note : data.getNotes()) {
	                                String title = note.getTitle();
	                                notesNames.add(title);
	                            }
	                            mAdapter.notifyDataSetChanged();
	                        }

	                        @Override
	                        public void onException(Exception exception) {
	                            onError(exception, "Error listing notes. ", R.string.error_listing_notes);
	                        }
	                    });
	        } catch (TTransportException exception){
	            onError(exception, "Error creating notestore. ", R.string.error_creating_notestore);
	        }
	    }

	    /**
	     * Show log and toast and remove a dialog on Exceptions
	     *
	     */
	    public void onError(Exception exception, String logstr, int id){
	        Log.e(LOGTAG, logstr + exception);
	        Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
	        removeDialog(DIALOG_PROGRESS);
	    }
	 
	 
	 
	 

}
